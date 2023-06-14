package com.eduvideo.content.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.eduvideo.content.model.dto.QueryCourseTeacherDto;
import com.eduvideo.content.model.po.CourseTeacher;

import java.util.List;

/**
 * <p>
 * 课程-教师关系表 服务类
 * </p>
 *
 * @author zkp
 * @since 2023-06-12
 */
public interface CourseTeacherService extends IService<CourseTeacher> {

    /***
    * @description 查询教师接口
    * @param courseId
    * @return com.eduvideo.content.model.dto.QueryCourseTeacherDto
    * @author zkp15
    * @date 2023/6/14 21:10
    */
    List<QueryCourseTeacherDto> queryCourseTeacher(Long courseId);

    /***
    * @description 添加或者修改教师请求接口
    * @param courseTeacher
    * @return com.eduvideo.content.model.dto.QueryCourseTeacherDto
    * @author zkp15
    * @date 2023/6/14 21:12
    */
    QueryCourseTeacherDto addCourseTeacher(CourseTeacher courseTeacher);

    /***
    * @description 删除对应课程的老师信息
    * @param courseId
     * @param teacherId
    * @return void
    * @author zkp15
    * @date 2023/6/14 21:23
    */
    void removeCourseTeacher(Long courseId, Long teacherId);

    void deleteByCourseId(Long courseId);
}
