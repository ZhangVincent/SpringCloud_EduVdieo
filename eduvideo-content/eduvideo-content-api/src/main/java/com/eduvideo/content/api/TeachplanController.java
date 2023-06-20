package com.eduvideo.content.api;

import com.eduvideo.content.model.dto.BindTeachplanMediaDto;
import com.eduvideo.content.model.dto.SaveTeachplanDto;
import com.eduvideo.content.model.dto.TeachplanDto;
import com.eduvideo.content.service.TeachplanMediaService;
import com.eduvideo.content.service.TeachplanService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author zkp15
 * @version 1.0
 * @description 查询课程计划信息
 * @date 2023/6/14 16:47
 */
@Api(value = "课程计划编辑接口", tags = "课程计划编辑接口")
@RestController
public class TeachplanController {

    @Autowired
    private TeachplanService teachplanService;

    @Autowired
    private TeachplanMediaService teachplanMediaService;

    @ApiOperation("查询课程计划树形结构")
    @ApiImplicitParam(value = "courseId", name = "课程Id", required = true, dataType = "Long", paramType = "path")
    @GetMapping("/teachplan/{courseId}/tree-nodes")
    public List<TeachplanDto> getTreeNodes(@PathVariable Long courseId) {
        return teachplanService.findTeachplanTree(courseId);
    }

    @PostMapping("/teachplan")
    public void saveTeachplan(@RequestBody SaveTeachplanDto saveTeachplanDto) {
        teachplanService.saveTeachplan(saveTeachplanDto);
    }

    @DeleteMapping("/teachplan/{teachplanId}")
    public void removeTeachPlan(@PathVariable Long teachplanId) {
        teachplanService.removeTeachPlan(teachplanId);
    }

    @PostMapping("/teachplan/movedown/{teachplanId}")
    public void movedownTeachPlan(@PathVariable Long teachplanId) {
        teachplanService.movedownTeachPlan(teachplanId);
    }

    @PostMapping("/teachplan/moveup/{teachplanId}")
    public void moveupTeachPlan(@PathVariable Long teachplanId) {
        teachplanService.moveupTeachPlan(teachplanId);
    }

    @ApiOperation(value = "课程计划和媒资信息绑定")
    @PostMapping("/teachplan/association/media")
    public void associationMedia(@RequestBody BindTeachplanMediaDto bindTeachplanMediaDto) {
        teachplanMediaService.associationMedia(bindTeachplanMediaDto);
    }

}
