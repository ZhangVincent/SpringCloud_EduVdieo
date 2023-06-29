package com.eduvideo.content.api;

import com.alibaba.fastjson.JSON;
import com.eduvideo.content.model.dto.CourseBaseInfoDto;
import com.eduvideo.content.model.dto.CoursePreviewDto;
import com.eduvideo.content.model.dto.TeachplanDto;
import com.eduvideo.content.model.po.CoursePublish;
import com.eduvideo.content.service.CourseBaseService;
import com.eduvideo.content.service.CoursePublishService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author zkp15
 * @version 1.0
 * @description 课程公开查询接口
 * @date 2023/6/20 22:03
 */
@Api(value = "课程公开查询接口",tags = "课程公开查询接口")
@RestController
//@RequestMapping("/open")
public class CourseOpenController {

    @Autowired
    private CourseBaseService courseBaseService;

    @Autowired
    private CoursePublishService coursePublishService;


    @GetMapping("/course/whole/{courseId}")
    public CoursePreviewDto getPreviewInfo(@PathVariable("courseId") Long courseId) {
        //获取课程预览信息
//        CoursePreviewDto coursePreviewInfo = coursePublishService.getCoursePreviewInfo(courseId);
        CoursePreviewDto coursePreviewInfo = coursePublishService.getCoursePreviewInfoCache(courseId);
        return coursePreviewInfo;
    }

//    @ApiOperation("获取课程发布信息")
//    @GetMapping("/course/whole/{courseId}")
//    public CoursePreviewDto getCoursePublish(@PathVariable("courseId") Long courseId) {
//        //查询课程发布信息
//        CoursePublish coursePublish = coursePublishService.getCoursePublish(courseId);
//        if (coursePublish == null) {
//            return new CoursePreviewDto();
//        }
//
//        //课程基本信息
//        CourseBaseInfoDto courseBase = new CourseBaseInfoDto();
//        BeanUtils.copyProperties(coursePublish, courseBase);
//        //课程计划
//        List<TeachplanDto> teachplans = JSON.parseArray(coursePublish.getTeachplan(), TeachplanDto.class);
//        CoursePreviewDto coursePreviewInfo = new CoursePreviewDto();
//        coursePreviewInfo.setCourseBase(courseBase);
//        coursePreviewInfo.setTeachplans(teachplans);
//        return coursePreviewInfo;
//    }


}

