package com.eduvideo.content.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author zkp15
 * @version 1.0
 * @description 修改课程提交的数据比新增课程多了一项课程id，因为修改课程需要针对某个课程进行修改。
 * @date 2023/6/14 10:25
 */
@Data
@ApiModel(value="UpdateCourseDto", description="修改课程基本信息")
public class EditCourseDto extends AddCourseDto{
    @ApiModelProperty(value = "课程名称", required = true)
    private Long id;
}
