package com.eduvideo.content.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.eduvideo.base.exception.EduVideoException;
import com.eduvideo.content.mapper.CoursePublishMapper;
import com.eduvideo.content.mapper.CoursePublishPreMapper;
import com.eduvideo.content.model.dto.CourseBaseInfoDto;
import com.eduvideo.content.model.dto.CoursePreviewDto;
import com.eduvideo.content.model.dto.TeachplanDto;
import com.eduvideo.content.model.po.*;
import com.eduvideo.content.service.CourseBaseService;
import com.eduvideo.content.service.CourseMarketService;
import com.eduvideo.content.service.CoursePublishService;
import com.eduvideo.content.service.TeachplanService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 课程发布 服务实现类
 * </p>
 *
 * @author zkp
 */
@Slf4j
@Service
public class CoursePublishServiceImpl extends ServiceImpl<CoursePublishMapper, CoursePublish> implements CoursePublishService {

    @Autowired
    private CourseBaseService courseBaseService;

    @Autowired
    private CourseMarketService courseMarketService;

    @Autowired
    private TeachplanService teachplanService;

    @Autowired
    private CoursePublishPreMapper coursePublishPreMapper;

    @Override
    public CoursePreviewDto getCoursePreviewInfo(Long courseId) {
        CoursePreviewDto coursePreviewDto = new CoursePreviewDto();
        coursePreviewDto.setCourseBase(courseBaseService.getCourseBaseById(courseId));
        coursePreviewDto.setTeachplans(teachplanService.findTeachplanTree(courseId));
        return coursePreviewDto;
    }

    @Override
    public void commitAudit(Long companyId, Long courseId) {
        // 对已提交审核的课程不允许提交审核。
        CourseBase courseBase = courseBaseService.getById(courseId);
        if (courseBase.getAuditStatus().equals("202003")){
            EduVideoException.cast("当前为等待审核状态，审核完成可以再次提交。");
        }

        // 本机构只允许提交本机构的课程。
        if (!companyId.equals(courseBase.getCompanyId())){
            EduVideoException.cast("不是课程所属机构，没有提交权限");
        }

        // 没有上传图片不允许提交审核。
        if (StringUtils.isEmpty(courseBase.getPic())){
            EduVideoException.cast("请上传课程图片");
        }

        // 没有添加课程计划不允许提交审核
        LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<Teachplan>().eq(Teachplan::getCourseId, courseId);
        int count = teachplanService.count(queryWrapper);
        if (count<=0){
            EduVideoException.cast("请提交课程教学计划");
        }

        // 查询课程基本信息、课程营销信息、课程计划信息等课程相关信息，整合为课程预发布信息。
        CoursePublishPre coursePublishPre = new CoursePublishPre();
        CourseBaseInfoDto courseBaseInfoDto = courseBaseService.getCourseBaseById(courseId);
        BeanUtils.copyProperties(courseBaseInfoDto,coursePublishPre);

        CourseMarket courseMarket = courseMarketService.getById(courseId);
        coursePublishPre.setMarket(JSON.toJSONString(courseMarket));

        List<TeachplanDto> teachplanTree = teachplanService.findTeachplanTree(courseId);
        coursePublishPre.setTeachplan(JSON.toJSONString(teachplanTree));


        // 向课程预发布表course_publish_pre插入一条记录，如果已经存在则更新，审核状态为：已提交。
        coursePublishPre.setStatus("202003");
        //教学机构id
        coursePublishPre.setCompanyId(companyId);
        //提交时间
        coursePublishPre.setCreateDate(LocalDateTime.now());
        CoursePublishPre coursePublishPreUpdate = coursePublishPreMapper.selectById(courseId);
        if(coursePublishPreUpdate == null){
            //添加课程预发布记录
            coursePublishPreMapper.insert(coursePublishPre);
        }else{
            coursePublishPreMapper.updateById(coursePublishPre);
        }

        // 更新课程基本表course_base课程审核状态为：已提交
        courseBase.setAuditStatus("202003");
        courseBaseService.updateById(courseBase);
    }
}
