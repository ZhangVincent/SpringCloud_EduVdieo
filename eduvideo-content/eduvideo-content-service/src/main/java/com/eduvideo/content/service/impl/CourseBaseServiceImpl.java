package com.eduvideo.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.eduvideo.base.exception.EduVideoException;
import com.eduvideo.content.mapper.CourseCategoryMapper;
import com.eduvideo.content.mapper.TeachplanMapper;
import com.eduvideo.content.model.dto.EditCourseDto;
import com.eduvideo.content.model.po.CourseCategory;
import com.eduvideo.content.model.po.CourseMarket;
import com.eduvideo.content.model.po.Teachplan;
import com.eduvideo.content.service.CourseMarketService;
import com.eduvideo.content.service.CourseTeacherService;
import com.eduvideo.content.service.TeachplanService;
import org.apache.commons.lang.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.eduvideo.base.model.PageParams;
import com.eduvideo.base.model.PageResult;
import com.eduvideo.content.mapper.CourseBaseMapper;
import com.eduvideo.content.mapper.CourseMarketMapper;
import com.eduvideo.content.model.dto.AddCourseDto;
import com.eduvideo.content.model.dto.CourseBaseInfoDto;
import com.eduvideo.content.model.dto.QueryCourseParamsDto;
import com.eduvideo.content.model.po.CourseBase;
import com.eduvideo.content.service.CourseBaseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 课程基本信息 服务实现类
 * </p>
 *
 * @author zkp
 */
@Slf4j
@Service
public class CourseBaseServiceImpl extends ServiceImpl<CourseBaseMapper, CourseBase> implements CourseBaseService {

    @Autowired
    private CourseBaseMapper courseBaseMapper;
    @Autowired
    private CourseMarketMapper courseMarketMapper;
    @Autowired
    private CourseMarketService courseMarketService;
    @Autowired
    private CourseCategoryMapper courseCategoryMapper;
    @Autowired
    private TeachplanMapper teachplanMapper;
    @Autowired
    private TeachplanService teachplanService;
    @Autowired
    private CourseTeacherService courseTeacherService;

    @Override
    public PageResult<CourseBase> queryCourseBaseList(PageParams pageParams, QueryCourseParamsDto queryCourseParamsDto) {
        // LambdaQueryWrapper条件查询
        LambdaQueryWrapper<CourseBase> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(!StringUtils.isEmpty(queryCourseParamsDto.getCourseName()), CourseBase::getName, queryCourseParamsDto.getCourseName());
        queryWrapper.eq(!StringUtils.isEmpty(queryCourseParamsDto.getAuditStatus()), CourseBase::getAuditStatus, queryCourseParamsDto.getAuditStatus());
        queryWrapper.eq(!StringUtils.isEmpty(queryCourseParamsDto.getPublishStatus()), CourseBase::getStatus, queryCourseParamsDto.getPublishStatus());
        // Page分页查询
        Page<CourseBase> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());
        // 查询
        Page<CourseBase> pageResult = courseBaseMapper.selectPage(page, queryWrapper);
        // 数据格式转换
        PageResult<CourseBase> courseBasePage = new PageResult<CourseBase>(pageResult.getRecords(), pageResult.getTotal(), pageParams.getPageNo(), pageParams.getPageSize());
        return courseBasePage;
    }

    @Transactional
    @Override
    public CourseBaseInfoDto createCourseBase(Long companyId, AddCourseDto addCourseDto) {
        // 校验数据业务合法性
//        if (addCourseDto.getCharge().equals("201001") && (addCourseDto.getPrice() == null || addCourseDto.getPrice().floatValue() <= 0)) {
//            throw new EduVideoException("选择收费但是没有填写价格");
//        }

        // 数据格式转换，向base和market表中插入数据
        CourseBase courseBase = new CourseBase();
        BeanUtils.copyProperties(addCourseDto, courseBase);
        courseBase.setCompanyId(companyId);
//        courseBase.setCompanyName();
        courseBase.setCreateDate(LocalDateTime.now());
//        courseBase.setChangeDate();
//        courseBase.setChangePeople();
        courseBase.setAuditStatus("202002");
        courseBase.setStatus("203001");
        int insert = courseBaseMapper.insert(courseBase);
        Long courseId = courseBase.getId();

//        CourseMarket courseMarket = new CourseMarket();
//        BeanUtils.copyProperties(addCourseDto, courseMarket);
//        courseMarket.setId(courseId);
//        int insert1 = courseMarketMapper.insert(courseMarket);

        Integer insert1 = addOrUpdateCourseMarket(courseId, addCourseDto);

        if (insert <= 0 || insert1 <= 0) {
            EduVideoException.cast("新增课程基本信息失败");
        }

        return getCourseBaseById(courseId);
    }

    @Override
    public CourseBaseInfoDto getCourseBaseById(Long courseId) {
        // 查询信息
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);

        // 组装数据并返回
        CourseBaseInfoDto courseBaseInfoDto = new CourseBaseInfoDto();
        BeanUtils.copyProperties(courseBase, courseBaseInfoDto);

        if (courseMarket != null) {
            BeanUtils.copyProperties(courseMarket, courseBaseInfoDto);
        }

        CourseCategory courseCategoryMt = courseCategoryMapper.selectById(courseBase.getMt());
        courseBaseInfoDto.setMtName(courseCategoryMt.getName());
        CourseCategory courseCategorySt = courseCategoryMapper.selectById(courseBase.getSt());
        courseBaseInfoDto.setStName(courseCategorySt.getName());

        return courseBaseInfoDto;
    }

    @Transactional
    @Override
    public CourseBaseInfoDto modifyCourseBase(Long companyId, EditCourseDto updateCourseDto) {
        // 根据课程id查询课程信息
        Long courseId = updateCourseDto.getId();
        CourseBase courseBase = courseBaseMapper.selectById(courseId);

        // 校验修改权限
        if (!courseBase.getCompanyId().equals(companyId)) {
            EduVideoException.cast("不是企业用户，没有修改权限");
        }

        // 封装课程信息对象和课程营销对象，并修改数据库
        CourseBase courseBaseUpdate = new CourseBase();
        BeanUtils.copyProperties(updateCourseDto, courseBaseUpdate);
        courseBaseUpdate.setChangeDate(LocalDateTime.now());
        int update = courseBaseMapper.updateById(courseBaseUpdate);

        Integer update1 = addOrUpdateCourseMarket(courseId, updateCourseDto);

        if (update <= 0 || update1 <= 0) {
            EduVideoException.cast("修改课程基本信息失败");
        }

        // 封装数据格式并返回
        return getCourseBaseById(courseId);
    }

    @Transactional
    @Override
    public boolean removeCourseBase(Long companyId, Long courseId) {
        // 校验用户权限
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        if (!courseBase.getCompanyId().equals(companyId)) {
            EduVideoException.cast("不是企业用户，没有删除权限");
        }
        // 删除课程营销信息
        courseMarketService.deleteByCourseId(courseId);

        // 删除课程计划
        teachplanService.deleteByCourseId(courseId);

        // 删除课程教师信息
        courseTeacherService.deleteByCourseId(courseId);

        // 删除课程
        int delete = courseBaseMapper.deleteById(courseId);
        if (delete <= 0) {
            EduVideoException.cast("删除课程基本信息失败");
        }
        return delete > 0;
    }

    /***
     * @description 校验数据业务合法性，新增或修改课程营销数据
     * @param courseId
     * @param dto
     * @return java.lang.Integer
     * @author zkp15
     * @date 2023/6/14 10:58
     */
    private Integer addOrUpdateCourseMarket(Long courseId, AddCourseDto dto) {
        // 校验数据业务合法性
        if (dto.getCharge().equals("201001") && (dto.getPrice() == null || dto.getPrice().floatValue() <= 0)) {
            throw new EduVideoException("选择收费但是没有填写价格");
        }

        // 整理数据格式
        CourseMarket courseMarket = new CourseMarket();
        BeanUtils.copyProperties(dto, courseMarket);
        courseMarket.setId(courseId);
        boolean b = courseMarketService.saveOrUpdate(courseMarket);
        return b ? 1 : 0;
    }
}
