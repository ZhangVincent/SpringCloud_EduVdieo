package com.eduvideo.content.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.eduvideo.base.exception.CommonError;
import com.eduvideo.base.exception.EduVideoException;
import com.eduvideo.content.config.MultipartSupportConfig;
import com.eduvideo.content.feignclient.MediaServiceClient;
import com.eduvideo.content.mapper.CourseBaseMapper;
import com.eduvideo.content.mapper.CoursePublishMapper;
import com.eduvideo.content.mapper.CoursePublishPreMapper;
import com.eduvideo.content.model.dto.CourseBaseInfoDto;
import com.eduvideo.content.model.dto.CoursePreviewDto;
import com.eduvideo.content.model.dto.TeachplanDto;
import com.eduvideo.content.model.po.*;
import com.eduvideo.content.service.CourseBaseService;
import com.eduvideo.content.service.CourseMarketService;
import com.eduvideo.content.service.CoursePublishService;
import com.eduvideo.content.service.TeachplanService;
import com.eduvideo.messagesdk.model.po.MqMessage;
import com.eduvideo.messagesdk.service.MqMessageService;
import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 课程发布 服务实现类
 * </p>
 *
 * @author zkp
 */
@Slf4j
@Service
public class CoursePublishServiceImpl extends ServiceImpl<CoursePublishMapper, CoursePublish> implements CoursePublishService {

    @Autowired
    private CourseBaseMapper courseBaseMapper;

    @Autowired
    private CourseBaseService courseBaseService;

    @Autowired
    private CourseMarketService courseMarketService;

    @Autowired
    private TeachplanService teachplanService;

    @Autowired
    private CoursePublishPreMapper coursePublishPreMapper;

    @Autowired
    private CoursePublishMapper coursePublishMapper;

    @Autowired
    private MqMessageService mqMessageService;

    @Autowired
    private MediaServiceClient mediaServiceClient;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    @Override
    public CoursePreviewDto getCoursePreviewInfo(Long courseId) {
        CoursePreviewDto coursePreviewDto = new CoursePreviewDto();
        coursePreviewDto.setCourseBase(courseBaseService.getCourseBaseById(courseId));
        coursePreviewDto.setTeachplans(teachplanService.findTeachplanTree(courseId));
        return coursePreviewDto;
    }

//    @Override
//    public CoursePreviewDto getCoursePreviewInfoCache(Long courseId) {
//        //查询缓存
//        Object jsonObj = redisTemplate.opsForValue().get("course:" + courseId);
//        if (jsonObj != null) {
//            String jsonString = jsonObj.toString();
////            System.out.println("=================从缓存查=================");
//            CoursePreviewDto coursePreviewDto = JSON.parseObject(jsonString, CoursePreviewDto.class);
//            return coursePreviewDto;
//        } else {
////            System.out.println("从数据库查询...");
//            //从数据库查询
//            CoursePreviewDto coursePreviewDto = new CoursePreviewDto();
//            coursePreviewDto.setCourseBase(courseBaseService.getCourseBaseById(courseId));
//            coursePreviewDto.setTeachplans(teachplanService.findTeachplanTree(courseId));
////            if(coursePreviewDto!=null){
//            redisTemplate.opsForValue().set("course:" + courseId, JSON.toJSONString(coursePreviewDto), 300 + new Random().nextInt(100), TimeUnit.SECONDS);
////            }
//            return coursePreviewDto;
//        }
//
//    }

    // 分布式redis锁，Redisson
    @Override
    public CoursePreviewDto getCoursePreviewInfoCache(Long courseId) {
        //查询缓存
        String jsonString = (String) redisTemplate.opsForValue().get("course:" + courseId);
        if (!StringUtils.isEmpty(jsonString)) {
            System.out.println("=================从缓存查=================");
            CoursePreviewDto coursePreviewDto = JSON.parseObject(jsonString, CoursePreviewDto.class);
            return coursePreviewDto;
        } else {
            //每门课程设置一个锁
            RLock lock = redissonClient.getLock("coursequerylock:" + courseId);
            //获取锁
            lock.lock();
            try {
                jsonString = (String) redisTemplate.opsForValue().get("course:" + courseId);
                if (!StringUtils.isEmpty(jsonString)) {
                    CoursePreviewDto coursePreviewDto = JSON.parseObject(jsonString, CoursePreviewDto.class);
                    return coursePreviewDto;
                }

                System.out.println("从数据库查询...");
                //从数据库查询
                CoursePreviewDto coursePreviewDto = new CoursePreviewDto();
                coursePreviewDto.setCourseBase(courseBaseService.getCourseBaseById(courseId));
                coursePreviewDto.setTeachplans(teachplanService.findTeachplanTree(courseId));

                redisTemplate.opsForValue().set("course:" + courseId, JSON.toJSONString(coursePreviewDto), 300 + new Random().nextInt(100), TimeUnit.SECONDS);

                return coursePreviewDto;
            } finally {
                //释放锁
                lock.unlock();
            }

        }

    }

    @Override
    public void commitAudit(Long companyId, Long courseId) {
        // 对已提交审核的课程不允许提交审核。
        CourseBase courseBase = courseBaseService.getById(courseId);
        if (courseBase.getAuditStatus().equals("202003")) {
            EduVideoException.cast("当前为等待审核状态，审核完成可以再次提交。");
        }

        // 本机构只允许提交本机构的课程。
        if (!companyId.equals(courseBase.getCompanyId())) {
            EduVideoException.cast("不是课程所属机构，没有提交权限");
        }

        // 没有上传图片不允许提交审核。
        if (StringUtils.isEmpty(courseBase.getPic())) {
            EduVideoException.cast("请上传课程图片");
        }

        // 没有添加课程计划不允许提交审核
        LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<Teachplan>().eq(Teachplan::getCourseId, courseId);
        int count = teachplanService.count(queryWrapper);
        if (count <= 0) {
            EduVideoException.cast("请提交课程教学计划");
        }

        // 查询课程基本信息、课程营销信息、课程计划信息等课程相关信息，整合为课程预发布信息。
        CoursePublishPre coursePublishPre = new CoursePublishPre();
        CourseBaseInfoDto courseBaseInfoDto = courseBaseService.getCourseBaseById(courseId);
        BeanUtils.copyProperties(courseBaseInfoDto, coursePublishPre);

        CourseMarket courseMarket = courseMarketService.getById(courseId);
        coursePublishPre.setMarket(JSON.toJSONString(courseMarket));

        List<TeachplanDto> teachplanTree = teachplanService.findTeachplanTree(courseId);
        coursePublishPre.setTeachplan(JSON.toJSONString(teachplanTree));


        // 向课程预发布表course_publish_pre插入一条记录，如果已经存在则更新，审核状态为：已提交。
        coursePublishPre.setStatus("202003");
        //教学机构id
        coursePublishPre.setCompanyId(companyId);
        //提交时间
        coursePublishPre.setCreateDate(LocalDateTime.now());
        CoursePublishPre coursePublishPreUpdate = coursePublishPreMapper.selectById(courseId);
        if (coursePublishPreUpdate == null) {
            //添加课程预发布记录
            coursePublishPreMapper.insert(coursePublishPre);
        } else {
            coursePublishPreMapper.updateById(coursePublishPre);
        }

        // 更新课程基本表course_base课程审核状态为：已提交
        courseBase.setAuditStatus("202003");
        courseBaseService.updateById(courseBase);
    }

    @Transactional
    @Override
    public void publish(Long companyId, Long courseId) {

        //约束校验
        //查询课程预发布表
        CoursePublishPre coursePublishPre = coursePublishPreMapper.selectById(courseId);
        if (coursePublishPre == null) {
            EduVideoException.cast("请先提交课程审核，审核通过才可以发布");
        }
        //本机构只允许提交本机构的课程
        if (!coursePublishPre.getCompanyId().equals(companyId)) {
            EduVideoException.cast("不允许提交其它机构的课程。");
        }


        //课程审核状态
        String auditStatus = coursePublishPre.getStatus();
        //审核通过方可发布
        if (!"202004".equals(auditStatus)) {
            EduVideoException.cast("操作失败，课程审核通过方可发布。");
        }

        //保存课程发布信息到课程发布表中，并更新课程基本信息表
        saveCoursePublish(courseId);

        //保存消息表
        saveCoursePublishMessage(courseId);

        //删除课程预发布表对应记录
        coursePublishPreMapper.deleteById(courseId);

    }

    /**
     * @param courseId 课程id
     * @return void
     * @description 保存课程发布信息
     * @author zkp15
     * @date 2022/9/20 16:32
     */
    private void saveCoursePublish(Long courseId) {
        //整合课程发布信息
        //查询课程预发布表
        CoursePublishPre coursePublishPre = coursePublishPreMapper.selectById(courseId);
        if (coursePublishPre == null) {
            EduVideoException.cast("课程预发布数据为空");
        }

        CoursePublish coursePublish = new CoursePublish();

        //拷贝到课程发布对象
        BeanUtils.copyProperties(coursePublishPre, coursePublish);
        coursePublish.setStatus("203002");
        CoursePublish coursePublishUpdate = coursePublishMapper.selectById(courseId);
        if (coursePublishUpdate == null) {
            coursePublishMapper.insert(coursePublish);
        } else {
            coursePublishMapper.updateById(coursePublish);
        }
        //更新课程基本表的发布状态
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        courseBase.setStatus("203002");
        courseBaseMapper.updateById(courseBase);

    }

    /**
     * @param courseId 课程id
     * @return void
     * @description 保存消息表记录，稍后实现
     * @author zkp15
     * @date 2022/9/20 16:32
     */
    private void saveCoursePublishMessage(Long courseId) {
        MqMessage mqMessage = mqMessageService.addMessage("course_publish", String.valueOf(courseId), null, null);
        if (mqMessage == null) {
            EduVideoException.cast(CommonError.UNKOWN_ERROR);
        }
    }

    @Override
    public File generateCourseHtml(Long courseId) {

        //静态化文件
        File htmlFile = null;

        try {
            //配置freemarker
            Configuration configuration = new Configuration(Configuration.getVersion());

            //加载模板
            //选指定模板路径,classpath下templates下
            //得到classpath路径
            String classpath = this.getClass().getResource("/").getPath();
            configuration.setDirectoryForTemplateLoading(new File(classpath + "/templates/"));
            //设置字符编码
            configuration.setDefaultEncoding("utf-8");

            //指定模板文件名称
            Template template = configuration.getTemplate("course_template.ftl");

            //准备数据
            CoursePreviewDto coursePreviewInfo = this.getCoursePreviewInfo(courseId);

            Map<String, Object> map = new HashMap<>();
            map.put("model", coursePreviewInfo);

            //静态化
            //参数1：模板，参数2：数据模型
            String content = FreeMarkerTemplateUtils.processTemplateIntoString(template, map);
//            System.out.println(content);
            //将静态化内容输出到文件中
            InputStream inputStream = IOUtils.toInputStream(content);
            //创建静态化文件
            htmlFile = File.createTempFile("course", ".html");
            log.debug("课程静态化，生成静态文件:{}", htmlFile.getAbsolutePath());
            //输出流
            FileOutputStream outputStream = new FileOutputStream(htmlFile);
            IOUtils.copy(inputStream, outputStream);
        } catch (Exception e) {
            log.error("课程静态化异常:{}", e.toString());
            EduVideoException.cast("课程静态化异常");
        }

        return htmlFile;
    }

    @Override
    public void uploadCourseHtml(Long courseId, File file) {
        MultipartFile multipartFile = MultipartSupportConfig.getMultipartFile(file);
        String course = mediaServiceClient.uploadFile(multipartFile, "course", courseId + ".html");
        if (course == null) {
            EduVideoException.cast("上传静态文件异常");
        }
    }

    @Override
    public CoursePublish getCoursePublish(Long courseId) {
        CoursePublish coursePublish = coursePublishMapper.selectById(courseId);
        return coursePublish;
    }

}
