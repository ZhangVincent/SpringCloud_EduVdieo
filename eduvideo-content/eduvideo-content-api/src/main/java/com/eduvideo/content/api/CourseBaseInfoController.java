package com.eduvideo.content.api;

import com.eduvideo.base.exception.ValidationGroups;
import com.eduvideo.base.model.PageParams;
import com.eduvideo.base.model.PageResult;
import com.eduvideo.content.model.dto.AddCourseDto;
import com.eduvideo.content.model.dto.CourseBaseInfoDto;
import com.eduvideo.content.model.dto.QueryCourseParamsDto;
import com.eduvideo.content.model.dto.EditCourseDto;
import com.eduvideo.content.model.po.CourseBase;
import com.eduvideo.content.service.CourseBaseService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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

    /***
    * @description 添加课程接口，使用idea插件httpclient测试
    * @param addCourseDto
    * @return com.eduvideo.content.model.dto.CourseBaseInfoDto
    * @author zkp15
    * @date 2023/6/13 23:27
    */
    @PostMapping("/course")
    public CourseBaseInfoDto createCourseBase(@RequestBody @Validated({ValidationGroups.Insert.class}) AddCourseDto addCourseDto) {
        Long companyId = 1232141425L;
        return courseBaseService.createCourseBase(companyId, addCourseDto);
    }

    /***
    * @description 修改课程信息之前根据id查询课程信息
    * @param courseId
    * @return com.eduvideo.content.model.dto.CourseBaseInfoDto
    * @author zkp15
    * @date 2023/6/14 21:26
    */
    @GetMapping("/course/{courseId}")
    public CourseBaseInfoDto getCourseBaseById(@PathVariable Long courseId) {
        return courseBaseService.getCourseBaseById(courseId);
    }

    /***
    * @description 修改课程信息
    * @param updateCourseDto
    * @return com.eduvideo.content.model.dto.CourseBaseInfoDto
    * @author zkp15
    * @date 2023/6/14 21:27
    */
    @PutMapping("/course")
    public CourseBaseInfoDto modifyCourseBase(@RequestBody @Validated({ValidationGroups.Update.class}) EditCourseDto updateCourseDto) {
        Long companyId = 1232141425L;
        return courseBaseService.modifyCourseBase(companyId, updateCourseDto);
    }

    /***
    * @description 删除课程信息
    * @param courseId
    * @return void
    * @author zkp15
    * @date 2023/6/14 21:27
    */
    @DeleteMapping("course/{courseId}")
    public void removeCourseBase(@PathVariable Long courseId){
        Long companyId=1232141425L;
        boolean b = courseBaseService.removeCourseBase(companyId, courseId);
    }
}
