package com.eduvideo.content.api;

import com.eduvideo.base.model.PageParams;
import com.eduvideo.base.model.PageResult;
import com.eduvideo.content.model.dto.QueryCourseParamsDto;
import com.eduvideo.content.model.po.CourseBase;
import com.eduvideo.content.service.CourseBaseService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author zkp15
 * @version 1.0
 * @description 课程信息http接口
 * @date 2023/6/12 19:55
 */
@Api(value = "课程信息编辑接口", tags = "课程信息编辑接口")
@RestController
public class CourseBaseInfoController {

    @Autowired
    private CourseBaseService courseBaseService;

    /***
    * @description 使用swagger测试接口，访问http://localhost:63040/content/swagger-ui.html
    * @param pageParams
     * @param queryCourseParamsDto
    * @return 查询课程信息分页
    * @author zkp15
    * @date 2023/6/13 9:39
    */
    @ApiOperation("课程查询接口")
    @PostMapping("/course/list")
    public PageResult<CourseBase> list(PageParams pageParams, @RequestBody QueryCourseParamsDto queryCourseParamsDto) {
        PageResult<CourseBase> courseBasePageResult = courseBaseService.queryCourseBaseList(pageParams, queryCourseParamsDto);
        return courseBasePageResult;
    }
}
