package com.eduvideo.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.eduvideo.base.exception.EduVideoException;
import com.eduvideo.content.mapper.CourseTeacherMapper;
import com.eduvideo.content.model.dto.QueryCourseTeacherDto;
import com.eduvideo.content.model.po.CourseTeacher;
import com.eduvideo.content.service.CourseTeacherService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 课程-教师关系表 服务实现类
 * </p>
 *
 * @author zkp
 */
@Slf4j
@Service
public class CourseTeacherServiceImpl extends ServiceImpl<CourseTeacherMapper, CourseTeacher> implements CourseTeacherService {
    @Autowired
    private CourseTeacherMapper courseTeacherMapper;

    @Override
    public List<QueryCourseTeacherDto> queryCourseTeacher(Long courseId) {
        LambdaQueryWrapper<CourseTeacher> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseTeacher::getCourseId, courseId);
        List<CourseTeacher> courseTeachers = courseTeacherMapper.selectList(queryWrapper);
        List<QueryCourseTeacherDto> queryCourseTeacherDtos = new ArrayList<>();
        courseTeachers.stream().forEach(c -> {
            QueryCourseTeacherDto queryCourseTeacherDto = new QueryCourseTeacherDto();
            BeanUtils.copyProperties(c,queryCourseTeacherDto);
            queryCourseTeacherDtos.add(queryCourseTeacherDto);
        });
        return queryCourseTeacherDtos;
    }

    @Override
    public QueryCourseTeacherDto addCourseTeacher(CourseTeacher courseTeacher) {
        // 如果有id，就是添加，反之为修改
        if (courseTeacher.getId() == null) {
            // 添加创建时间
            courseTeacher.setCreateDate(LocalDateTime.now());
            int insert = courseTeacherMapper.insert(courseTeacher);
            if (insert <= 0) {
                EduVideoException.cast("添加教师失败，请稍后重试");
            }
        } else {
            int update = courseTeacherMapper.updateById(courseTeacher);
            if (update <= 0) {
                EduVideoException.cast("修改教师信息失败，请稍后重试");
            }
        }

        // 整合返回类型
        QueryCourseTeacherDto queryCourseTeacherDto = new QueryCourseTeacherDto();
        BeanUtils.copyProperties(courseTeacher, queryCourseTeacherDto);
        return queryCourseTeacherDto;
    }

    @Override
    public void removeCourseTeacher(Long courseId, Long teacherId) {
        // 删除对应课程id和教师id的记录
        LambdaQueryWrapper<CourseTeacher> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseTeacher::getCourseId, courseId);
        queryWrapper.eq(CourseTeacher::getId, teacherId);
        int delete = courseTeacherMapper.delete(queryWrapper);
        if (delete <= 0) {
            EduVideoException.cast("删除教师信息失败，请刷新后重试");
        }
    }

    @Override
    public void deleteByCourseId(Long courseId) {
        LambdaQueryWrapper<CourseTeacher> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseTeacher::getCourseId, courseId);
        Integer count = courseTeacherMapper.selectCount(queryWrapper);
        if (count == 0) {
            return;
        }
        int delete = courseTeacherMapper.delete(queryWrapper);
        if (delete <= 0) {
            EduVideoException.cast("课程老师信息删除失败");
        }
    }
}
