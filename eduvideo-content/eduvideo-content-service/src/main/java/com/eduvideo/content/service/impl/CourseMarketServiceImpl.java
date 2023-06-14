package com.eduvideo.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.eduvideo.base.exception.EduVideoException;
import com.eduvideo.content.mapper.CourseMarketMapper;
import com.eduvideo.content.model.po.CourseMarket;
import com.eduvideo.content.service.CourseMarketService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 课程营销信息 服务实现类
 * </p>
 *
 * @author zkp
 */
@Slf4j
@Service
public class CourseMarketServiceImpl extends ServiceImpl<CourseMarketMapper, CourseMarket> implements CourseMarketService {
    @Autowired
    private CourseMarketMapper courseMarketMapper;

    @Override
    public void deleteByCourseId(Long courseId) {
        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);
        if (courseMarket==null){
            return;
        }
        int delete = courseMarketMapper.deleteById(courseId);
        if (delete<=0){
            EduVideoException.cast("删除课程营销信息失败");
        }
    }
}
