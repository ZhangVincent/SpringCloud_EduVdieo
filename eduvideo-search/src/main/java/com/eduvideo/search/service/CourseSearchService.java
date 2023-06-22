package com.eduvideo.search.service;

import com.eduvideo.base.model.PageParams;
import com.eduvideo.base.model.PageResult;
import com.eduvideo.search.dto.SearchCourseParamDto;
import com.eduvideo.search.dto.SearchPageResultDto;
import com.eduvideo.search.po.CourseIndex;

/**
 * @description 课程搜索service
 * @author zkp15
 * @date 2022/9/24 22:40
 * @version 1.0
 */
public interface CourseSearchService {


    /**
     * @description 搜索课程列表
     * @param pageParams 分页参数
     * @param searchCourseParamDto 搜索条件
     * @return com.eduvideo.base.model.PageResult<com.eduvideo.search.po.CourseIndex> 课程列表
     * @author zkp15
     * @date 2022/9/24 22:45
    */
    SearchPageResultDto<CourseIndex> queryCoursePubIndex(PageParams pageParams, SearchCourseParamDto searchCourseParamDto);

 }
