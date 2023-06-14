package com.eduvideo.content.model.dto;

import com.eduvideo.content.model.po.Teachplan;
import com.eduvideo.content.model.po.TeachplanMedia;
import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * @author zkp15
 * @version 1.0
 * @description 课程计划
 * @date 2023/6/14 16:46
 */
@Data
@ToString
public class TeachplanDto extends Teachplan {

    //课程计划关联的媒资信息
    TeachplanMedia teachplanMedia;

    //子结点
    List<TeachplanDto> teachPlanTreeNodes;
}
