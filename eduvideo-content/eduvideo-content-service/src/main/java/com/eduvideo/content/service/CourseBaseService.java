package com.eduvideo.content.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.eduvideo.base.model.PageParams;
import com.eduvideo.base.model.PageResult;
import com.eduvideo.content.model.dto.AddCourseDto;
import com.eduvideo.content.model.dto.CourseBaseInfoDto;
import com.eduvideo.content.model.dto.QueryCourseParamsDto;
import com.eduvideo.content.model.po.CourseBase;
import org.springframework.web.bind.annotation.RequestBody;

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
}
