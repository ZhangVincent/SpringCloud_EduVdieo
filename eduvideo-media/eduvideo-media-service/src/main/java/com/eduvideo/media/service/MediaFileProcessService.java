package com.eduvideo.media.service;

import com.eduvideo.media.model.po.MediaProcess;

import java.util.List;

/**
 * @author zkp15
 * @version 1.0
 * @description 待处理视频的方法接口
 * @date 2023/6/19 20:53
 */
public interface MediaFileProcessService {
    /***
    * @description 根据任务处理器序列获取待处理的视频
    * @param shardIndex
     * @param shardTotal
     * @param count
    * @return java.util.List<com.eduvideo.media.model.po.MediaProcess>
    * @author zkp15
    * @date 2023/6/19 20:53
    */
    public List<MediaProcess> getMediaProcessList(int shardIndex, int shardTotal, int count);

    /***
    * @description 任务处理完成需要更新任务处理结果
    * @param taskId 
     * @param status 
     * @param fileId 
     * @param url 
     * @param errorMsg 
    * @return void
    * @author zkp15
    * @date 2023/6/19 20:56
    */
    void saveProcessFinishStatus(Long taskId,String status,String fileId,String url,String errorMsg);
}
