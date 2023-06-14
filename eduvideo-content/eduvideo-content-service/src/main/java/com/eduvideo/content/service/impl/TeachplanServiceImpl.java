package com.eduvideo.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.eduvideo.base.exception.EduVideoException;
import com.eduvideo.content.mapper.TeachplanMapper;
import com.eduvideo.content.model.dto.SaveTeachplanDto;
import com.eduvideo.content.model.dto.TeachplanDto;
import com.eduvideo.content.model.po.Teachplan;
import com.eduvideo.content.service.TeachplanService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * <p>
 * 课程计划 服务实现类
 * </p>
 *
 * @author zkp
 */
@Slf4j
@Service
public class TeachplanServiceImpl extends ServiceImpl<TeachplanMapper, Teachplan> implements TeachplanService {

    @Autowired
    private TeachplanMapper teachplanMapper;

    @Override
    public List<TeachplanDto> findTeachplayTree(Long courseId) {
        return teachplanMapper.selectTreeNodes(courseId);
    }

    @Override
    public void saveTeachplan(SaveTeachplanDto teachplanDto) {
        // 根据id查询课程计划，如果为null，就新增，反之修改
        Long id = teachplanDto.getId();

        if (id == null) {
            Teachplan teachplan = new Teachplan();
            BeanUtils.copyProperties(teachplanDto, teachplan);

            // 新增的课程需要设置orderById，查询同一个课程id和同一个父id下的所有课程数量，然后设置这个数量+1为排序id
            teachplan.setOrderby(getTeachplanCount(teachplanDto));

            int insert = teachplanMapper.insert(teachplan);
            if (insert <= 0) {
                EduVideoException.cast("新增课程计划失败");
            }
        } else {
            Teachplan teachplan = teachplanMapper.selectById(id);
            BeanUtils.copyProperties(teachplanDto, teachplan);

            // 修改的课程需要设置修改时间
            teachplan.setChangeDate(LocalDateTime.now());

            int update = teachplanMapper.updateById(teachplan);
            if (update <= 0) {
                EduVideoException.cast("修改课程计划失败");
            }
        }

    }

    @Override
    public void removeTeachPlan(Long teachplanId) {
        Teachplan teachplan = teachplanMapper.selectById(teachplanId);
        if (teachplan == null) {
            EduVideoException.cast("目标课程已删除，请刷新重试");
        }
        if (teachplan.getParentid() == 0L) {
            // 删除第一级别的章时要求章下边没有小节方可删除。
            LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Teachplan::getParentid, teachplan.getId());
            Integer count = teachplanMapper.selectCount(queryWrapper);
            if (count > 0) {
                EduVideoException.cast("课程计划信息还有子级信息，无法删除操作");
            }
            int delete = teachplanMapper.deleteById(teachplanId);
            if (delete <= 0) {
                EduVideoException.cast("删除失败，请刷新重试");
            }
        } else {
            // 删除第二级别的小节的同时需要将其它关联的视频信息也删除。
            int delete = teachplanMapper.deleteById(teachplanId);
            if (delete <= 0) {
                EduVideoException.cast("删除失败，请刷新重试");
            }
            // TODO 删除关联的视频信息
        }
    }

    @Transactional
    @Override
    public void movedownTeachPlan(Long teachplanId) {
        // 根据teachplanId查找课程计划
        Teachplan teachplan = teachplanMapper.selectById(teachplanId);

        // 根据parentid和orderByID+i查找下一个课程计划
        Long parentid = teachplan.getParentid();
        LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Teachplan::getParentid, parentid);
        queryWrapper.orderBy(true, true, Teachplan::getOrderby);
        List<Teachplan> teachplans = teachplanMapper.selectList(queryWrapper);
        Integer orderby = teachplan.getOrderby();
        Teachplan teachplanNext = null;
        for (int i = 0; i < teachplans.size(); i++) {
            if (teachplans.get(i).getOrderby().equals(orderby) && i < teachplans.size() - 1) {
                teachplanNext = teachplans.get(i + 1);
                break;
            }
        }

        // 将两个课程计划的排序字段值交换
        if (teachplanNext == null) {
            EduVideoException.cast("当前课程计划已经是最后一个了！");
        }
        teachplan.setOrderby(teachplanNext.getOrderby());
        teachplanNext.setOrderby(orderby);
        int update = teachplanMapper.updateById(teachplan);
        int update1 = teachplanMapper.updateById(teachplanNext);
        if (update <= 0 || update1 <= 0) {
            EduVideoException.cast("排序失败，请稍后重试");
        }
    }

    @Transactional
    @Override
    public void moveupTeachPlan(Long teachplanId) {
        // 根据teachplanId查找课程计划
        Teachplan teachplan = teachplanMapper.selectById(teachplanId);

        // 根据parentid和orderByID+i查找下一个课程计划
        Long parentid = teachplan.getParentid();
        LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Teachplan::getParentid, parentid);
        queryWrapper.orderBy(true, true, Teachplan::getOrderby);
        List<Teachplan> teachplans = teachplanMapper.selectList(queryWrapper);
        Integer orderby = teachplan.getOrderby();
        Teachplan teachplanPer = null;
        for (int i = 0; i < teachplans.size(); i++) {
            if (teachplans.get(i).getOrderby().equals(orderby) && i > 0) {
                teachplanPer = teachplans.get(i - 1);
                break;
            }
        }

        // 将两个课程计划的排序字段值交换
        if (teachplanPer == null) {
            EduVideoException.cast("当前课程计划已经是第一个了！");
        }
        teachplan.setOrderby(teachplanPer.getOrderby());
        teachplanPer.setOrderby(orderby);
        int update = teachplanMapper.updateById(teachplan);
        int update1 = teachplanMapper.updateById(teachplanPer);
        if (update <= 0 || update1 <= 0) {
            EduVideoException.cast("排序失败，请稍后重试");
        }
    }

    @Override
    public void deleteByCourseId(Long courseId) {
        LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Teachplan::getCourseId, courseId);
        Integer count = teachplanMapper.selectCount(queryWrapper);
        if (count == 0) {
            return;
        }
        int delete = teachplanMapper.delete(queryWrapper);
        if (delete <= 0) {
            EduVideoException.cast("删除课程计划信息失败");
        }
    }

    /***
     * @description 获取最新的排序号
     * @param teachplanDto
     * @return java.lang.Integer
     * @author zkp15
     * @date 2023/6/14 19:11
     */
    private Integer getTeachplanCount(SaveTeachplanDto teachplanDto) {
        // 新增课程的orderByID需要根据课程id和parentid进行查询
        Long courseId = teachplanDto.getCourseId();
        Long parentId = teachplanDto.getParentid();
        LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Teachplan::getCourseId, courseId);
        queryWrapper.eq(Teachplan::getParentid, parentId);
        Integer count = teachplanMapper.selectCount(queryWrapper);
        return count + 1;
    }
}
