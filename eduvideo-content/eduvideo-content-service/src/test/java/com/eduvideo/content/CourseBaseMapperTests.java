package com.eduvideo.content;

import com.eduvideo.base.model.PageParams;
import com.eduvideo.base.model.PageResult;
import com.eduvideo.content.mapper.CourseBaseMapper;
import com.eduvideo.content.model.dto.QueryCourseParamsDto;
import com.eduvideo.content.model.po.CourseBase;
import com.eduvideo.content.service.CourseBaseService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

/**
 * @author zkp15
 * @version 1.0
 * @description 测试类
 * @date 2023/6/12 21:01
 */
@SpringBootTest
public class CourseBaseMapperTests {
    @Autowired
    private CourseBaseMapper courseBaseMapper;

    @Autowired
    private CourseBaseService courseBaseService;

    @Test
    void testCourseBaseMapper(){
        List<CourseBase> courseBases = courseBaseMapper.selectList(null);
        System.err.println(courseBases.size());
    }

    @Test
    void testCourseBaseService(){
        QueryCourseParamsDto courseParamsDto = new QueryCourseParamsDto();
//        courseParamsDto.setAuditStatus("202001");
//        courseParamsDto.setPublishStatus("203001");
        PageResult<CourseBase> pageResult = courseBaseService.queryCourseBaseList(new PageParams(), courseParamsDto);
        System.err.println(pageResult);
    }
}
