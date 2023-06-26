package com.eduvideo.learning.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.eduvideo.learning.model.dto.MyCourseTableItemDto;
import com.eduvideo.learning.model.dto.MyCourseTableParams;
import com.eduvideo.learning.model.po.XcCourseTables;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author itcast
 */
public interface XcCourseTablesMapper extends BaseMapper<XcCourseTables> {

    public List<MyCourseTableItemDto> myCourseTables( MyCourseTableParams params);
    public int myCourseTablesCount( MyCourseTableParams params);

}
