package com.eduvideo.learning.feignclient;

import com.eduvideo.content.model.po.CoursePublish;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * @author zkp15
 * @version 1.0
 * @description 内容管理服务远程接口
 * @date 2022/9/20 20:29
 */
@FeignClient(value = "content-api", fallbackFactory = ContentServiceClientFallbackFactory.class)
@RequestMapping("/content")
public interface ContentServiceClient {

    @ResponseBody
    @GetMapping("/r/coursepublish/{courseId}")
    public CoursePublish getCoursepublish(@PathVariable("courseId") Long courseId);

}
