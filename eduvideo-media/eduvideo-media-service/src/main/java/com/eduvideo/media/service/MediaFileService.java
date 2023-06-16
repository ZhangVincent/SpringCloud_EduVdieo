package com.eduvideo.media.service;

import com.eduvideo.base.model.PageParams;
import com.eduvideo.base.model.PageResult;
import com.eduvideo.media.model.dto.QueryMediaParamsDto;
import com.eduvideo.media.model.dto.RestResponse;
import com.eduvideo.media.model.dto.UploadFileParamsDto;
import com.eduvideo.media.model.dto.UploadFileResultDto;
import com.eduvideo.media.model.po.MediaFiles;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author zkp15
 * @version 1.0
 * @description 媒资文件管理业务类
 * @date 2022/9/10 8:55
 */
public interface MediaFileService {

    /**
     * @param pageParams          分页参数
     * @param queryMediaParamsDto 查询条件
     * @return com.eduvideo.base.model.PageResult<com.eduvideo.media.model.po.MediaFiles>
     * @description 媒资文件查询方法
     * @author zkp15
     * @date 2022/9/10 8:57
     */
    public PageResult<MediaFiles> queryMediaFiels(Long companyId, PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto);


    /***
     * @description 上传文件保存在分布式文件系统minion和数据库MySQL的接口
     * @param companyId 企业id
     * @param uploadFileParamsDto 上传文件信息
     * @param bytes 上传文件字节流，这里不用springmvc的MultipartFile是为了降低框架间耦合
     * @param folder 上传目录
     * @param objectName 上传文件名
     * @return com.eduvideo.media.model.dto.UploadFileResultDto
     * @author zkp15
     * @date 2023/6/15 23:16
     */
    public UploadFileResultDto uploadFile(Long companyId, UploadFileParamsDto uploadFileParamsDto, byte[] bytes, String folder, String objectName);


    /***
     * @description 将媒体文件信息上传到数据库中
     * @param companyId
     * @param uploadFileParamsDto
     * @param objectName
     * @param fileId
     * @param bucket_Files
     * @return com.eduvideo.media.model.po.MediaFiles
     * @author zkp15
     * @date 2023/6/16 11:03
     */
    public MediaFiles addMediaFilesToDb(Long companyId, UploadFileParamsDto uploadFileParamsDto, String objectName, String fileId, String bucket_Files);


    /**
     * @param fileMd5 文件的md5
     * @return com.xuecheng.base.model.RestResponse<java.lang.Boolean> false不存在，true存在
     * @description 检查文件是否存在
     * @author zkp15
     * @date 2022/9/13 15:38
     */
    public RestResponse<Boolean> checkFile(String fileMd5);

    /**
     * @param fileMd5    文件的md5
     * @param chunkIndex 分块序号
     * @return com.xuecheng.base.model.RestResponse<java.lang.Boolean> false不存在，true存在
     * @description 检查分块是否存在
     * @author zkp15
     * @date 2022/9/13 15:39
     */
    public RestResponse<Boolean> checkChunk(String fileMd5, int chunkIndex);

    /**
     * @param fileMd5 文件md5
     * @param chunk   分块序号
     * @param bytes   文件字节
     * @return com.xuecheng.base.model.RestResponse
     * @description 上传分块
     * @author zkp15
     * @date 2022/9/13 15:50
     */
    public RestResponse uploadChunk(String fileMd5, int chunk, byte[] bytes);


    /**
     * @param companyId           机构id
     * @param fileMd5             文件md5
     * @param chunkTotal          分块总和
     * @param uploadFileParamsDto 文件信息
     * @return com.xuecheng.base.model.RestResponse
     * @description 合并分块
     * @author zkp15
     * @date 2022/9/13 15:56
     */
    public RestResponse mergechunks(Long companyId, String fileMd5, int chunkTotal, UploadFileParamsDto uploadFileParamsDto);

}
