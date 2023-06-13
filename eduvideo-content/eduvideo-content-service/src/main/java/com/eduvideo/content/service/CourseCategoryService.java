package com.eduvideo.content.service;

import com.eduvideo.content.mapper.CourseCategoryMapper;
import com.eduvideo.content.model.dto.CourseCategoryTreeDto;
import com.eduvideo.content.model.po.CourseCategory;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * <p>
 * 课程分类 服务类
 * </p>
 *
 * @author zkp
 * @since 2023-06-13
 */
public interface CourseCategoryService extends IService<CourseCategory> {
    public List<CourseCategoryTreeDto> queryTreeNodes(String id);
}
