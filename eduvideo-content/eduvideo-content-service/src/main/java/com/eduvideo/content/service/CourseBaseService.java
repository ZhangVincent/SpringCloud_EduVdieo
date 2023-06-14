package com.eduvideo.content.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.eduvideo.base.model.PageParams;
import com.eduvideo.base.model.PageResult;
import com.eduvideo.content.model.dto.AddCourseDto;
import com.eduvideo.content.model.dto.CourseBaseInfoDto;
import com.eduvideo.content.model.dto.EditCourseDto;
import com.eduvideo.content.model.dto.QueryCourseParamsDto;
import com.eduvideo.content.model.po.CourseBase;

/**
 * <p>
 * 课程基本信息 服务类
 * </p>
 *
 * @author zkp
 * @since 2023-06-12
 */
public interface CourseBaseService extends IService<CourseBase> {
    /***
     * @description 课程查询接口
     * @param pageParams
     * @param queryCourseParamsDto
     * @return 返回分页数据
     * @author zkp15
     * @date 2023/6/12 22:56
     */
    public PageResult<CourseBase> queryCourseBaseList(PageParams pageParams, QueryCourseParamsDto queryCourseParamsDto);

    /***
    * @description 新增课程接口
    * @param companyId
     * @param addCourseDto
    * @return com.eduvideo.content.model.dto.CourseBaseInfoDto
    * @author zkp15
    * @date 2023/6/13 19:56
    */
    public CourseBaseInfoDto createCourseBase(Long companyId,AddCourseDto addCourseDto);

    /***
     * @description 根据课程id查询课程信息和课程营销信息，组装数据格式并返回
     * @param courseId
     * @return com.eduvideo.content.model.dto.CourseBaseInfoDto
     * @author zkp15
     * @date 2023/6/14 10:38
     */
    public CourseBaseInfoDto getCourseBaseById(Long courseId) ;

    /***
    * @description 课程信息修改接口
    * @param companyId
     * @param updateCourseDto
    * @return com.eduvideo.content.model.dto.CourseBaseInfoDto
    * @author zkp15
    * @date 2023/6/14 10:41
    */
    CourseBaseInfoDto modifyCourseBase(Long companyId, EditCourseDto updateCourseDto);

    /***
    * @description 删除课程信息
    * @param companyId
    * @param courseId
     * @return void
    * @author zkp15
    * @date 2023/6/14 17:32
    */
    boolean removeCourseBase(Long companyId, Long courseId);
}
