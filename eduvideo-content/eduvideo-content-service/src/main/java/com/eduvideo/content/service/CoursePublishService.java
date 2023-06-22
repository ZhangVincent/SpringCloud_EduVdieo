package com.eduvideo.content.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.eduvideo.content.model.dto.CoursePreviewDto;
import com.eduvideo.content.model.po.CoursePublish;

import java.io.File;

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

    /***
    * @description 课程发布方法，从预发布表中查询课程信息插入课程发布表中，向mq_message表中插入消息，更新课程基本信息表审核状态，删除预发布表
    * @param companyId
     * @param courseId
    * @return void
    * @author zkp15
    * @date 2023/6/22 10:44
    */
    public void publish(Long companyId,Long courseId);

    /***
     * @description 生成静态页面
     * @param courseId
     * @return File
     * @author zkp15
     * @date 2023/6/22 18:17
     */
    public File generateCourseHtml(Long courseId);

    /***
     * @description 将课程静态页面上传到文件管理系统
     * @param courseId
     * @param file
     * @return void
     * @author zkp15
     * @date 2023/6/22 18:17
     */
    public void  uploadCourseHtml(Long courseId,File file);
}
