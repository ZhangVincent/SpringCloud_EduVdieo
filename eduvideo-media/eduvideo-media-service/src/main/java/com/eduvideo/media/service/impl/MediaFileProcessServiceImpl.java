package com.eduvideo.media.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.eduvideo.media.mapper.MediaFilesMapper;
import com.eduvideo.media.mapper.MediaProcessHistoryMapper;
import com.eduvideo.media.mapper.MediaProcessMapper;
import com.eduvideo.media.model.po.MediaFiles;
import com.eduvideo.media.model.po.MediaProcess;
import com.eduvideo.media.model.po.MediaProcessHistory;
import com.eduvideo.media.service.MediaFileProcessService;
import com.eduvideo.media.service.MediaFileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author zkp15
 * @version 1.0
 * @description 待处理视频类
 * @date 2023/6/19 20:54
 */
@Slf4j
@Service
public class MediaFileProcessServiceImpl implements MediaFileProcessService {

    @Autowired
    private MediaProcessMapper mediaProcessMapper;

    @Autowired
    private MediaFilesMapper mediaFilesMapper;

    @Autowired
    private MediaProcessHistoryMapper mediaProcessHistoryMapper;

    @Override
    public List<MediaProcess> getMediaProcessList(int shardIndex, int shardTotal, int count) {
        return mediaProcessMapper.selectListByShardIndex(shardIndex,shardTotal,count);
    }

    @Override
    public void saveProcessFinishStatus(Long taskId, String status, String fileId, String url, String errorMsg) {
        //查出任务，如果不存在则直接返回
        MediaProcess mediaProcess = mediaProcessMapper.selectById(taskId);
        if(mediaProcess == null){
            return ;
        }

        //处理失败，更新任务处理结果
        LambdaQueryWrapper<MediaProcess> queryWrapperById = new LambdaQueryWrapper<MediaProcess>().eq(MediaProcess::getId, taskId);
        if(status.equals("3")){
            MediaProcess mediaProcess_u = new MediaProcess();
            mediaProcess_u.setStatus("3");
            mediaProcess_u.setErrormsg(errorMsg);
            mediaProcessMapper.update(mediaProcess_u,queryWrapperById);
            return ;
        }

        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileId);
        if(mediaFiles!=null){
            mediaFiles.setUrl(url);
            mediaFilesMapper.updateById(mediaFiles);
        }
        //处理成功，更新url和状态
        mediaProcess.setUrl(url);
        mediaProcess.setStatus("2");
        mediaProcess.setFinishDate(LocalDateTime.now());
        mediaProcessMapper.updateById(mediaProcess);

        //添加到历史记录
        MediaProcessHistory mediaProcessHistory = new MediaProcessHistory();
        BeanUtils.copyProperties(mediaProcess, mediaProcessHistory);
        Integer count = mediaProcessHistoryMapper.selectCount(null);
        mediaProcessHistory.setId((long) (count+1));

        mediaProcessHistoryMapper.insert(mediaProcessHistory);
        //删除mediaProcess
        mediaProcessMapper.deleteById(mediaProcess.getId());
    }
}
