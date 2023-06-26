package com.eduvideo.learning.service;

import com.eduvideo.base.model.PageResult;
import com.eduvideo.learning.model.dto.MyCourseTableItemDto;
import com.eduvideo.learning.model.dto.MyCourseTableParams;
import com.eduvideo.learning.model.dto.XcChooseCourseDto;
import com.eduvideo.learning.model.dto.XcCourseTablesDto;
import com.eduvideo.learning.model.po.XcChooseCourse;
import com.eduvideo.learning.model.po.XcCourseTables;
import com.sun.xml.internal.bind.v2.TODO;

/**
 * @author zkp15
 * @version 1.0
 * @description 我的课程表service接口
 * @date 2022/10/2 16:07
 */
public interface MyCourseTablesService {

    /***
     * @description 根据用户id和课程id选课
     * @param userId
     * @param courseId
     * @return com.eduvideo.learning.model.dto.XcChooseCourseDto
     * @author zkp15
     * @date 2023/6/26 20:41
     */
    public XcChooseCourseDto addChooseCourse(String userId, Long courseId);

    /**
     * @param userId
     * @param courseId
     * @return XcCourseTablesDto 学习资格状态 [{"code":"702001","desc":"正常学习"},
     *                                       {"code":"702002","desc":"没有选课或选课后没有支付"},
     *                                       {"code":"702003","desc":"已过期需要申请续期或重新支付"}]
     * @description 判断学习资格
     * @author zkp15
     * @date 2022/10/3 7:37
     */
    public XcCourseTablesDto getLeanringStatus(String userId, Long courseId);


    public boolean saveChooseCourseStauts(String choosecourseId);

    public PageResult<MyCourseTableItemDto> mycourestabls(MyCourseTableParams params);

}
