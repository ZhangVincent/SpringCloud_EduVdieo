package com.eduvideo.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.eduvideo.content.model.dto.TeachplanDto;
import com.eduvideo.content.model.po.Teachplan;

import java.util.List;

/**
 * <p>
 * 课程计划 Mapper 接口
 * </p>
 *
 * @author zkp
 */
public interface TeachplanMapper extends BaseMapper<Teachplan> {
    public List<TeachplanDto> selectTreeNodes(Long courseId);
}
