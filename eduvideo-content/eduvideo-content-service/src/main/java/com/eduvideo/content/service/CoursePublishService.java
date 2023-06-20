package com.eduvideo.content.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.eduvideo.content.model.dto.CoursePreviewDto;
import com.eduvideo.content.model.po.CoursePublish;

/**
 * <p>
 * 课程发布 服务类
 * </p>
 *
 * @author zkp
 * @since 2023-06-12
 */
public interface CoursePublishService extends IService<CoursePublish> {

    /***
     * @description 获取课程预览基本信息
     * @param courseId
     * @return com.eduvideo.content.model.dto.CoursePreviewDto
     * @author zkp15
     * @date 2023/6/20 21:57
     */
    public CoursePreviewDto getCoursePreviewInfo(Long courseId);

    /***
     * @description 提交审核的方法
     * @param companyId
     * @param courseId
     * @return void
     * @author zkp15
     * @date 2023/6/20 23:09
     */
    public void commitAudit(Long companyId, Long courseId);
}
