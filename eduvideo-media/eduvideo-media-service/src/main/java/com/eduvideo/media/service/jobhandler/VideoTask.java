package com.eduvideo.media.service.jobhandler;

import com.eduvideo.base.utils.Mp4VideoUtil;
import com.eduvideo.media.model.po.MediaProcess;
import com.eduvideo.media.service.MediaFileProcessService;
import com.eduvideo.media.service.MediaFileService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author zkp15
 * @version 1.0
 * @description 视频格式转换类
 * @date 2023/6/19 21:06
 */
@Slf4j
@Component
public class VideoTask {

    @Autowired
    private MediaFileService mediaFileService;

    @Autowired
    private MediaFileProcessService mediaFileProcessService;

    @Value("${videoprocess.ffmpegpath}")
    private String ffmpegpath;

    /***
    * @description 视频格式转化方法，由xxljob进行分布式任务控制
    *              在调度中心添加任务，获取分片参数，根据分片参数查询任务列表，根据任务个数创建线程池
     *             从minio下载需要转化的视频，使用videoutil对视频进行转化，如果转化失败记录结果，如果成功，上传到minio再记录结果
     *             由于使用多线程并发处理，每个待处理视频创建一个线程，所以使用CountDownLatch记录任务完成情况，并设置一个最长等待时间
    * @return void
    * @author zkp15
    * @date 2023/6/19 21:15
    */
    @XxlJob("videoJobHander")
    public void videoJobHander() throws Exception {

        // 分片参数
        int shardIndex = XxlJobHelper.getShardIndex();
        int shardTotal = XxlJobHelper.getShardTotal();
        List<MediaProcess> mediaProcessList = null;
        int size = 0;

        try {
            //取出2条记录，一次处理视频数量不要超过cpu核心数
            mediaProcessList = mediaFileProcessService.getMediaProcessList(shardIndex, shardTotal, 2);
            size = mediaProcessList.size();
            log.debug("取出待处理视频任务{}条", size);
            if (size < 0) {
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        //启动size个线程的线程池
        ExecutorService threadPool = Executors.newFixedThreadPool(size);
        //计数器
        CountDownLatch countDownLatch = new CountDownLatch(size);
        //将处理任务加入线程池
        mediaProcessList.forEach(mediaProcess -> {
            threadPool.execute(() -> {
                //下边是处理逻辑
                //桶
                String bucket = mediaProcess.getBucket();
                //存储路径
                String filePath = mediaProcess.getFilePath();
                //原始视频的md5值
                String fileId = mediaProcess.getFileId();
                //原始文件名称
                String filename = mediaProcess.getFilename();

                String status = mediaProcess.getStatus();

                if ("2".equals(status)) {
                    log.debug("视频已经处理成功，不再处理,文件:{},路径:{}", filename, filePath);
                    return;
                }

                //将要处理的文件下载到服务器上
                File originalFile = null;
                //处理结束的视频文件
                File mp4File = null;

                try {
                    originalFile = File.createTempFile("original", null);
                    mp4File = File.createTempFile("mp4", ".mp4");
                } catch (IOException e) {
                    log.error("处理视频前创建临时文件失败");
                    countDownLatch.countDown();
                    return;
                }
                try {
                    //下载文件
                    mediaFileService.downloadFileFromMinIO(originalFile, mediaProcess.getBucket(), mediaProcess.getFilePath());
                } catch (Exception e) {
                    log.error("处理视频前下载原始文件:{},出错:{}", mediaProcess.getFilePath(), e.getMessage());
                    countDownLatch.countDown();
                    return;
                }
                //视频处理结果
                String result = null;
                try {
                    //开始处理视频
                    Mp4VideoUtil videoUtil = new Mp4VideoUtil(ffmpegpath, originalFile.getAbsolutePath(), mp4File.getName(), mp4File.getAbsolutePath());
                    //开始视频转换，成功将返回success
                    result = videoUtil.generateMp4();
                } catch (Exception e) {
                    log.error("处理视频文件:{},出错:{}", mediaProcess.getFilePath(), e.getMessage());
                    countDownLatch.countDown();
                    return;
                }
                if (!result.equals("success")) {
                    //记录错误信息
                    log.error("处理视频失败,视频地址:{},错误信息:{}", bucket + filePath, result);
                    mediaFileProcessService.saveProcessFinishStatus(mediaProcess.getId(), "3", fileId, null, result);
                    countDownLatch.countDown();
                    return;
                }

                //将mp4上传至minio
                //文件路径
                String objectName = null;
                try {
                    objectName = getFilePath(fileId, ".mp4");
                    mediaFileService.addMediaFilesToMinIO(mp4File.getAbsolutePath(), bucket, objectName);
                } catch (Exception e) {
                    log.error("上传视频失败,视频地址:{},错误信息:{}", bucket + objectName, e.getMessage());
                    countDownLatch.countDown();
                    return;
                }

                try {
                    //访问url
                    String url = "/" + bucket + "/" + objectName;
                    //将url存储至数据，并更新状态为成功，并将待处理视频记录删除存入历史
                    mediaFileProcessService.saveProcessFinishStatus(mediaProcess.getId(), "2", fileId, url, null);
                } catch (Exception e) {
                    log.error("视频信息入库失败,视频地址:{},错误信息:{}", bucket + objectName, e.getMessage());
                }
                countDownLatch.countDown();
            });
        });
        //等待,给一个充裕的超时时间,防止无限等待，到达超时时间还没有处理完成则结束任务
        countDownLatch.await(30, TimeUnit.MINUTES);
    }

    private String getFilePath(String fileMd5, String fileExt) {
        return fileMd5.substring(0, 1) + "/" + fileMd5.substring(1, 2) + "/" + fileMd5 + "/" + fileMd5 + fileExt;
    }

}

