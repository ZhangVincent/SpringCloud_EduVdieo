package com.eduvideo.content.model.dto;

import com.eduvideo.content.model.po.CourseTeacher;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

/**
 * @author zkp15
 * @version 1.0
 * @description 教师查询响应体
 * @date 2023/6/14 21:05
 */
@Data
@ToString
public class QueryCourseTeacherDto extends CourseTeacher implements Serializable {

}
