package com.eduvideo.learning.api;

import com.eduvideo.base.exception.EduVideoException;
import com.eduvideo.base.model.PageResult;
import com.eduvideo.base.model.RestResponse;
import com.eduvideo.content.model.po.CourseBase;
import com.eduvideo.learning.model.dto.MyCourseTableItemDto;
import com.eduvideo.learning.model.dto.MyCourseTableParams;
import com.eduvideo.learning.model.dto.XcChooseCourseDto;
import com.eduvideo.learning.model.dto.XcCourseTablesDto;
import com.eduvideo.learning.service.MyCourseTablesService;
import com.eduvideo.learning.util.SecurityUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author zkp15
 * @version 1.0
 * @description 我的课程表接口
 * @date 2022/10/2 14:52
 */
@Api(value = "我的课程表接口", tags = "我的课程表接口")
@Slf4j
@RestController
public class MyCourseTablesController {

    @Autowired
    MyCourseTablesService myCourseTablesService;

    @ApiOperation("添加选课")
    @PostMapping("/choosecourse/{courseId}")
    public XcChooseCourseDto addChooseCourse(@PathVariable("courseId") Long courseId) {
        //从JWT令牌中获取用户id
        SecurityUtil.XcUser user = SecurityUtil.getUser();
        if(user == null){
            EduVideoException.cast("请登录后继续选课");
        }
        String userId = user.getId();
        return  myCourseTablesService.addChooseCourse(userId, courseId);

    }
    @ApiOperation("查询学习资格")
    @PostMapping("/choosecourse/learnstatus/{courseId}")
    public XcCourseTablesDto getLearnstatus(@PathVariable("courseId") Long courseId) {
        //登录用户
        SecurityUtil.XcUser user = SecurityUtil.getUser();
        if(user == null){
            EduVideoException.cast("请登录后继续选课");
        }
        String userId = user.getId();
        return  myCourseTablesService.getLeanringStatus(userId, courseId);

    }

    @ApiOperation("我的课程表")
    @GetMapping("/mycoursetable")
    public PageResult<MyCourseTableItemDto> mycoursetable(MyCourseTableParams params) {
        //登录用户
        SecurityUtil.XcUser user = SecurityUtil.getUser();
        if(user == null){
            EduVideoException.cast("请登录后继续选课");
        }
        String userId = user.getId();
        params.setUserId(userId);
        return  myCourseTablesService.mycourestabls(params);

    }




}
