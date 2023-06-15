package com.eduvideo.media.service;

import com.eduvideo.base.model.PageParams;
import com.eduvideo.base.model.PageResult;
import com.eduvideo.media.model.dto.QueryMediaParamsDto;
import com.eduvideo.media.model.dto.UploadFileParamsDto;
import com.eduvideo.media.model.dto.UploadFileResultDto;
import com.eduvideo.media.model.po.MediaFiles;

/**
 * @description 媒资文件管理业务类
 * @author Mr.M
 * @date 2022/9/10 8:55
 * @version 1.0
 */
public interface MediaFileService {

 /**
  * @description 媒资文件查询方法
  * @param pageParams 分页参数
  * @param queryMediaParamsDto 查询条件
  * @return com.eduvideo.base.model.PageResult<com.eduvideo.media.model.po.MediaFiles>
  * @author Mr.M
  * @date 2022/9/10 8:57
 */
 public PageResult<MediaFiles> queryMediaFiels(Long companyId,PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto);


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

}
