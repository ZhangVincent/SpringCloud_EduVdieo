package com.eduvideo.content.api;

import com.eduvideo.content.model.dto.CoursePreviewDto;
import com.eduvideo.content.service.CoursePublishService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author zkp15
 * @version 1.0
 * @description 课程发布
 * @date 2023/6/20 21:51
 */
@Controller
public class CoursePublishController {

    @Autowired
    private CoursePublishService coursePublishService;

    /***
    * @description 课程预览静态页面
    * @param courseId
    * @return org.springframework.web.servlet.ModelAndView
    * @author zkp15
    * @date 2023/6/20 21:52
    */
    @GetMapping("/coursepreview/{courseId}")
    public ModelAndView preview(@PathVariable("courseId") Long courseId){

        ModelAndView modelAndView = new ModelAndView();
        CoursePreviewDto coursePreviewInfo = coursePublishService.getCoursePreviewInfo(courseId);
        modelAndView.addObject("model",coursePreviewInfo);
        modelAndView.setViewName("course_template");
        return modelAndView;
    }

    /***
    * @description 提交课程审核接口
    * @param courseId
    * @return void
    * @author zkp15
    * @date 2023/6/20 23:08
    */
    @ResponseBody
    @PostMapping("/courseaudit/commit/{courseId}")
    public void commitAudit(@PathVariable("courseId") Long courseId){
        Long companyId = 1232141425L;
        coursePublishService.commitAudit(companyId,courseId);
    }

    /***
    * @description 课程发布的接口
    * @param courseId 
    * @return void
    * @author zkp15
    * @date 2023/6/22 10:40
    */
    @ApiOperation("课程发布")
    @ResponseBody
    @PostMapping ("/coursepublish/{courseId}")
    public void coursepublish(@PathVariable("courseId") Long courseId){
        Long companyId = 1232141425L;
        coursePublishService.publish(companyId,courseId);
    }


}

