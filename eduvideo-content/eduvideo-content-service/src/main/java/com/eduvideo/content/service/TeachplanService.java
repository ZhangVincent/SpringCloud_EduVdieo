package com.eduvideo.content.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.eduvideo.content.model.dto.SaveTeachplanDto;
import com.eduvideo.content.model.dto.TeachplanDto;
import com.eduvideo.content.model.po.Teachplan;

import java.util.List;

/**
 * <p>
 * 课程计划 服务类
 * </p>
 *
 * @author zkp
 * @since 2023-06-12
 */
public interface TeachplanService extends IService<Teachplan> {
    /***
     * @description 查询课程计划列表
     * @param courseId
     * @return java.util.List<com.eduvideo.content.model.dto.TeachplanDto>
     * @author zkp15
     * @date 2023/6/14 17:18
     */
    public List<TeachplanDto> findTeachplanTree(Long courseId);

    /***
    * @description 对课程教学目录的添加和修改
    * @param teachplanDto
    * @return void
    * @author zkp15
    * @date 2023/6/14 18:57
    */
    void saveTeachplan(SaveTeachplanDto teachplanDto);

    /***
    * @description 删除课程计划的接口，
    * @param teachplanId 课程计划的id
    * @return void
    * @author zkp15
    * @date 2023/6/14 19:38
    */
    void removeTeachPlan(Long teachplanId);

    /***
    * @description 向下移动课程计划
    * @param teachplanId
    * @return 向下移动课程计划
    * @author zkp15
    * @date 2023/6/14 20:39
    */
    void movedownTeachPlan(Long teachplanId);

    /***
    * @description 向上移动课程计划
    * @param teachplanId
    * @return void
    * @author zkp15
    * @date 2023/6/14 20:39
    */
    void moveupTeachPlan(Long teachplanId);

    void deleteByCourseId(Long courseId);
}
