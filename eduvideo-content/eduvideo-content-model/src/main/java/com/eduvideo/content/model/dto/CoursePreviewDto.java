package com.eduvideo.content.model.dto;

import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * @author zkp15
 * @version 1.0
 * @description 预览静态页面响应体
 * @date 2023/6/20 21:56
 */
@Data
@ToString
public class CoursePreviewDto {

    //课程基本信息,课程营销信息
    CourseBaseInfoDto courseBase;


    //课程计划信息
    List<TeachplanDto> teachplans;

    //师资信息暂时不加...


}
