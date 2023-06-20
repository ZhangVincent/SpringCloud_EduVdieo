package com.eduvideo.content.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.eduvideo.content.model.dto.BindTeachplanMediaDto;
import com.eduvideo.content.model.po.TeachplanMedia;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author zkp
 * @since 2023-06-12
 */
public interface TeachplanMediaService extends IService<TeachplanMedia> {
    /***
    * @description 媒资关系绑定方法
    * @param bindTeachplanMediaDto
    * @return com.eduvideo.content.model.po.TeachplanMedia
    * @author zkp15
    * @date 2023/6/20 15:37
    */
    public TeachplanMedia associationMedia(BindTeachplanMediaDto bindTeachplanMediaDto);
}
