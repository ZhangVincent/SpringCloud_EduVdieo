package com.eduvideo.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.eduvideo.base.exception.EduVideoException;
import com.eduvideo.content.mapper.TeachplanMapper;
import com.eduvideo.content.mapper.TeachplanMediaMapper;
import com.eduvideo.content.model.dto.BindTeachplanMediaDto;
import com.eduvideo.content.model.po.Teachplan;
import com.eduvideo.content.model.po.TeachplanMedia;
import com.eduvideo.content.service.TeachplanMediaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author zkp
 */
@Slf4j
@Service
public class TeachplanMediaServiceImpl extends ServiceImpl<TeachplanMediaMapper, TeachplanMedia> implements TeachplanMediaService {

    @Autowired
    private TeachplanMapper teachplanMapper;
    @Autowired
    private TeachplanMediaMapper teachplanMediaMapper;

    @Transactional
    @Override
    public TeachplanMedia associationMedia(BindTeachplanMediaDto bindTeachplanMediaDto) {
        // 参数校验

        // 根据teachplanid查询教学计划 如果是二级目录就可以绑定媒资
        Long teachplanId = bindTeachplanMediaDto.getTeachplanId();
        Teachplan teachplan = teachplanMapper.selectById(teachplanId);
        if (teachplan == null || teachplan.getGrade() != 2) {
            EduVideoException.cast("教学计划不存在或不是二级目录");
        }

        // 根据teachplanid删除原有的关系
        LambdaQueryWrapper<TeachplanMedia> queryWrapper = new LambdaQueryWrapper<TeachplanMedia>().eq(TeachplanMedia::getTeachplanId, teachplanId);
        int delete = teachplanMediaMapper.delete(queryWrapper);

        // 添加新的teachplanmedia并返回
        TeachplanMedia teachplanMedia = new TeachplanMedia();
        teachplanMedia.setCourseId(teachplan.getCourseId());
        teachplanMedia.setTeachplanId(teachplanId);
        teachplanMedia.setMediaFilename(bindTeachplanMediaDto.getFileName());
        teachplanMedia.setMediaId(bindTeachplanMediaDto.getMediaId());
        teachplanMedia.setCreateDate(LocalDateTime.now());
        int insert = teachplanMediaMapper.insert(teachplanMedia);
        if (insert < 0) {
            EduVideoException.cast("绑定教学计划失败，请重试");
        }
        return teachplanMedia;
    }
}
