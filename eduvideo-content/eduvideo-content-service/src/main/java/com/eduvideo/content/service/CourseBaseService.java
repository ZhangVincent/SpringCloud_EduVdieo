package com.eduvideo.content.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.eduvideo.base.model.PageParams;
import com.eduvideo.base.model.PageResult;
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
}
