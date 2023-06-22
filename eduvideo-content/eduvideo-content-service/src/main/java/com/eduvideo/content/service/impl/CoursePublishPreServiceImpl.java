package com.eduvideo.content.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.eduvideo.base.exception.EduVideoException;
import com.eduvideo.content.config.MultipartSupportConfig;
import com.eduvideo.content.feignclient.MediaServiceClient;
import com.eduvideo.content.mapper.CoursePublishPreMapper;
import com.eduvideo.content.model.dto.CoursePreviewDto;
import com.eduvideo.content.model.po.CoursePublishPre;
import com.eduvideo.content.service.CoursePublishPreService;
import com.eduvideo.content.service.CoursePublishService;
import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * 课程发布 服务实现类
 * </p>
 *
 * @author zkp
 */
@Slf4j
@Service
public class CoursePublishPreServiceImpl extends ServiceImpl<CoursePublishPreMapper, CoursePublishPre> implements CoursePublishPreService {

}
