package com.eduvideo.learning;

import com.eduvideo.base.model.PageResult;
import com.eduvideo.content.model.po.CoursePublish;
import com.eduvideo.learning.feignclient.ContentServiceClient;
import com.eduvideo.learning.mapper.XcChooseCourseMapper;
import com.eduvideo.learning.model.dto.MyCourseTableItemDto;
import com.eduvideo.learning.model.dto.MyCourseTableParams;
import com.eduvideo.learning.service.MyCourseTablesService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author zkp15
 * @version 1.0
 * @description TODO
 * @date 2022/10/2 10:32
 */
@SpringBootTest
public class Test1 {

    @Autowired
    ContentServiceClient contentServiceClient;

    @Autowired
    MyCourseTablesService myCourseTablesService;

    @Test
    public void test() {
        CoursePublish coursepublish = contentServiceClient.getCoursepublish(118L);
        System.out.println(coursepublish);
    }

    @Test
    public void test2() {
        MyCourseTableParams myCourseTableParams = new MyCourseTableParams();
        myCourseTableParams.setUserId("52");
        PageResult<MyCourseTableItemDto> mycourestabls = myCourseTablesService.mycourestabls(myCourseTableParams);
        System.out.println(mycourestabls);
    }

}
