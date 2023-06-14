package com.eduvideo.content.api;

import com.eduvideo.content.model.dto.QueryCourseTeacherDto;
import com.eduvideo.content.model.po.CourseTeacher;
import com.eduvideo.content.service.CourseTeacherService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author zkp15
 * @version 1.0
 * @description 教师信息管理接口
 * @date 2023/6/14 21:02
 */
@Api(value = "教师信息管理接口", tags = "教师信息管理接口")
@RestController
public class CourseTeacherController {

    @Autowired
    private CourseTeacherService courseTeacherService;

    @GetMapping("/courseTeacher/list/{courseId}")
    public List<QueryCourseTeacherDto> queryCourseTeacher(@PathVariable Long courseId){
        return courseTeacherService.queryCourseTeacher(courseId);
    }

    @PostMapping("/courseTeacher")
    public QueryCourseTeacherDto addCourseTeacher(@RequestBody CourseTeacher courseTeacher){
        return courseTeacherService.addCourseTeacher(courseTeacher);
    }

    @DeleteMapping("/courseTeacher/course/{courseId}/{teacherId}")
    public void removeCourseTeacher(@PathVariable Long courseId,@PathVariable Long teacherId){
        courseTeacherService.removeCourseTeacher(courseId,teacherId);
    }
}
