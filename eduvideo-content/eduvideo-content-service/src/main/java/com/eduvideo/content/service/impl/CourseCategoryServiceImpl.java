package com.eduvideo.content.service.impl;

import com.eduvideo.content.model.dto.CourseCategoryTreeDto;
import com.eduvideo.content.model.po.CourseCategory;
import com.eduvideo.content.mapper.CourseCategoryMapper;
import com.eduvideo.content.service.CourseCategoryService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 课程分类 服务实现类
 * </p>
 *
 * @author zkp
 */
@Slf4j
@Service
public class CourseCategoryServiceImpl extends ServiceImpl<CourseCategoryMapper, CourseCategory> implements CourseCategoryService {

    @Autowired
    private CourseCategoryMapper courseCategoryMapper;

    @Override
    public List<CourseCategoryTreeDto> queryTreeNodes(String id) {
        List<CourseCategoryTreeDto> courseCategoryTreeDtos = courseCategoryMapper.selectTreeNodes(id);

        List<CourseCategoryTreeDto> list = new ArrayList<>();
        for (CourseCategoryTreeDto dto : courseCategoryTreeDtos) {
            if (dto.getId().equals(id)) {
                list.add(findChildrenNodes(dto, courseCategoryTreeDtos));
                break;
            }
        }
        return list.get(0).getChildrenTreeNodes();
    }

    private CourseCategoryTreeDto findChildrenNodes(CourseCategoryTreeDto parentNode, List<CourseCategoryTreeDto> courseCategoryTreeDtos) {
        courseCategoryTreeDtos.stream().forEach(c -> {
            if (c.getParentid().equals(parentNode.getId())) {
                if (parentNode.getChildrenTreeNodes()==null){
                    parentNode.setChildrenTreeNodes(new ArrayList());
                }
                parentNode.getChildrenTreeNodes().add(findChildrenNodes(c, courseCategoryTreeDtos));
            }
        });
        return parentNode;
    }
}
