package com.eduvideo.media.model.dto;

import lombok.Data;

/**
 * @author zkp15
 * @version 1.0
 * @description service层调用的更加通用的上传文件请求参数
 * @date 2023/6/15 23:14
 */
@Data
public class UploadFileParamsDto {
    /**
     * 文件名称
     */
    private String filename;

    /**
     * 文件content-type
     */
    private String contentType;

    /**
     * 文件类型（文档，音频，视频）
     */
    private String fileType;
    /**
     * 文件大小
     */
    private Long fileSize;

    /**
     * 标签
     */
    private String tags;

    /**
     * 上传人
     */
    private String username;

    /**
     * 备注
     */
    private String remark;
}
