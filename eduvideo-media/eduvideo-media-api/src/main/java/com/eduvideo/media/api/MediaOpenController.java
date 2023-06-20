package com.eduvideo.media.api;

import com.eduvideo.base.exception.EduVideoException;
import com.eduvideo.media.model.dto.RestResponse;
import com.eduvideo.media.model.po.MediaFiles;
import com.eduvideo.media.service.MediaFileService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author zkp15
 * @version 1.0
 * @description 媒资文件管理接口
 * @date 2023/6/20 22:05
 */
@Api(value = "媒资文件管理接口", tags = "媒资文件管理接口")
@RestController
@RequestMapping("/open")
public class MediaOpenController {

    @Autowired
    MediaFileService mediaFileService;

    @ApiOperation("预览文件")
    @GetMapping("/preview/{mediaId}")
    public RestResponse<String> getPlayUrlByMediaId(@PathVariable String mediaId) {

        MediaFiles mediaFiles = mediaFileService.getFileById(mediaId);
        if (mediaFiles == null || StringUtils.isEmpty(mediaFiles.getUrl())) {
            EduVideoException.cast("视频还没有转码处理");
        }
        return RestResponse.success(mediaFiles.getUrl());

    }

}

