package com.eduvideo.learning.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.eduvideo.base.exception.EduVideoException;
import com.eduvideo.base.exception.EduVideoException;
import com.eduvideo.base.model.RestResponse;
import com.eduvideo.content.model.dto.TeachplanDto;
import com.eduvideo.content.model.po.CoursePublish;
import com.eduvideo.learning.feignclient.ContentServiceClient;
import com.eduvideo.learning.feignclient.MediaServiceClient;
import com.eduvideo.learning.mapper.XcLearnRecordMapper;
import com.eduvideo.learning.model.dto.XcCourseTablesDto;
import com.eduvideo.learning.model.po.XcLearnRecord;
import com.eduvideo.learning.service.LearningService;
import com.eduvideo.learning.service.MyCourseTablesService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author zkp15
 * @version 1.0
 * @description 学习过程管理service接口
 * @date 2022/10/5 9:08
 */
@Slf4j
@Service
public class LearningServiceImpl implements LearningService {

    @Autowired
    MediaServiceClient mediaServiceClient;

    @Autowired
    ContentServiceClient contentServiceClient;

    @Autowired
    MyCourseTablesService myCourseTablesService;

    @Autowired
    XcLearnRecordMapper learnRecordMapper;

    @Autowired
    private LearningServiceImpl currentProxy;

    @Override
    public RestResponse<String> getVideo(String userId, Long courseId, Long teachplanId, String mediaId) {
        //查询课程信息
        CoursePublish coursepublish = contentServiceClient.getCoursepublish(courseId);
        if (coursepublish == null) {
            EduVideoException.cast("课程信息不存在");
        }

        //校验学习资格
        //判断是否是试学课程
        List<TeachplanDto> teachplans = JSON.parseArray(coursepublish.getTeachplan(), TeachplanDto.class);
        //试学视频直接返回视频地址
        if (isTeachplanPreview(teachplanId, teachplans)) {
            currentProxy.saveLearnRecord(userId, coursepublish, teachplanId);//保存学习记录
            return mediaServiceClient.getPlayUrlByMediaId(mediaId);
        }

        //如果登录
        if (StringUtils.isNotEmpty(userId)) {

            //判断是否选课，根据选课情况判断学习资格
            XcCourseTablesDto xcCourseTablesDto = myCourseTablesService.getLeanringStatus(userId, courseId);
            //学习资格状态 [{"code":"702001","desc":"正常学习"},{"code":"702002","desc":"没有选课或选课后没有支付"},{"code":"702003","desc":"已过期需要申请续期或重新支付"}]
            String learnStatus = xcCourseTablesDto.getLearnStatus();
            if (learnStatus.equals("702001")) {
                currentProxy.saveLearnRecord(userId, coursepublish, teachplanId);//保存学习记录
                return mediaServiceClient.getPlayUrlByMediaId(mediaId);
            } else if (learnStatus.equals("702003")) {
                RestResponse.validfail("您的选课已过期需要申请续期或重新支付");
            }
        }

        //未登录或未选课判断是否收费
        String charge = coursepublish.getCharge();
        if (charge.equals("201000")) {//免费可以正常学习
            currentProxy.saveLearnRecord(userId, coursepublish, teachplanId);//保存学习记录
            return mediaServiceClient.getPlayUrlByMediaId(mediaId);
        }

        return RestResponse.validfail("请购买课程后继续学习");


    }

    //保存学习记录
    @Transactional
    public void saveLearnRecord(String userId, CoursePublish coursepublish, Long teachplanId) {

        //登录下保存学习记录
        if (StringUtils.isNotEmpty(userId)) {
            //课程id
            Long courseId = coursepublish.getId();
            //找到课程计划对应的名称
            String teachplanName = null;
            List<TeachplanDto> teachplans = JSON.parseArray(coursepublish.getTeachplan(), TeachplanDto.class);
            for (TeachplanDto first : teachplans) {
                if (first.getTeachPlanTreeNodes() != null) {
                    for (TeachplanDto second : first.getTeachPlanTreeNodes()) {
                        if (second.getId().equals(teachplanId)) {
                            teachplanName = second.getPname();
                            break;
                        }
                    }
                }
            }

            //初始化
            learnRecordMapper.initLearnRecord(userId, courseId, teachplanId);
            //更新学习记录
            XcLearnRecord learnRecord_u = new XcLearnRecord();
            learnRecord_u.setCourseName(coursepublish.getName());
            learnRecord_u.setLearnDate(LocalDateTime.now());
            learnRecord_u.setTeachplanName(teachplanName);
            int update = learnRecordMapper.update(learnRecord_u, new LambdaQueryWrapper<XcLearnRecord>().eq(XcLearnRecord::getUserId, userId).eq(XcLearnRecord::getCourseId, courseId).eq(XcLearnRecord::getTeachplanId, teachplanId));
            if (update > 0) {
                log.debug("更新学习记录,user:{},{}", userId, learnRecord_u);
            }

        }


    }

    //判断是不是试学课程
    private boolean isTeachplanPreview(Long teachplanId, List<TeachplanDto> teachplans) {
        try {
            for (TeachplanDto first : teachplans) {
                if (first.getTeachPlanTreeNodes() != null) {
                    for (TeachplanDto second : first.getTeachPlanTreeNodes()) {
                        if (second.getId().equals(teachplanId)) {
                            if (second.getIsPreview().equals("1")) {
                                return true;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


}
