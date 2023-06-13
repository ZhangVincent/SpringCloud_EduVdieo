package com.eduvideo.content.model.dto;

import com.eduvideo.content.model.po.CourseCategory;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author zkp15
 * @version 1.0
 * @description 返回的目录是一个树形结构
 * @date 2023/6/13 15:54
 */
@Data
public class CourseCategoryTreeDto extends CourseCategory implements Serializable {
    private List childrenTreeNodes;
}
