package com.eduvideo.media.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.eduvideo.base.exception.EduVideoException;
import com.eduvideo.base.model.PageParams;
import com.eduvideo.base.model.PageResult;
import com.eduvideo.media.mapper.MediaFilesMapper;
import com.eduvideo.media.model.dto.QueryMediaParamsDto;
import com.eduvideo.media.model.dto.UploadFileParamsDto;
import com.eduvideo.media.model.dto.UploadFileResultDto;
import com.eduvideo.media.model.po.MediaFiles;
import com.eduvideo.media.service.MediaFileService;
import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.errors.*;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/***
 * @description 媒体文件增删改查接口
 * @author zkp15
 * @date 2023/6/16 10:30
 */
@Service
public class MediaFileServiceImpl implements MediaFileService {

    @Autowired
    private MediaFilesMapper mediaFilesMapper;

    @Autowired
    private MinioClient minioClient;

    //普通文件桶
    @Value("${minio.bucket.files}")
    private String bucket_Files;


    @Override
    public PageResult<MediaFiles> queryMediaFiels(Long companyId, PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto) {

        //构建查询条件对象
        LambdaQueryWrapper<MediaFiles> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(!StringUtils.isEmpty(queryMediaParamsDto.getFilename()), MediaFiles::getFilename, queryMediaParamsDto.getFilename());
        queryWrapper.eq(!StringUtils.isEmpty(queryMediaParamsDto.getAuditStatus()), MediaFiles::getAuditStatus, queryMediaParamsDto.getAuditStatus());
        queryWrapper.eq(!StringUtils.isEmpty(queryMediaParamsDto.getFileType()), MediaFiles::getFileType, queryMediaParamsDto.getFileType());

        //分页对象
        Page<MediaFiles> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());
        // 查询数据内容获得结果
        Page<MediaFiles> pageResult = mediaFilesMapper.selectPage(page, queryWrapper);
        // 获取数据列表
        List<MediaFiles> list = pageResult.getRecords();
        // 获取数据总数
        long total = pageResult.getTotal();
        // 构建结果集
        PageResult<MediaFiles> mediaListResult = new PageResult<>(list, total, pageParams.getPageNo(), pageParams.getPageSize());
        return mediaListResult;

    }

    @Transactional
    @Override
    public UploadFileResultDto uploadFile(Long companyId, UploadFileParamsDto uploadFileParamsDto, byte[] bytes, String folder, String objectName) {
        if (!companyId.equals(1232141425L)) {
            EduVideoException.cast("不是企业用户，不能添加文件");
        }

        // 根据folder和objectName的值确定文件上传路径和名称
        if (StringUtils.isEmpty(folder)) {
            folder = getFileFolder(new Date(), true, true, true);
        } else if (folder.indexOf("/") < 0) {
            folder += "/";
        }

        //生成文件id，文件的md5值
        // md5的好处是，生成长度相同，相同输入得到相同输出
        String fileId = DigestUtils.md5Hex(bytes);
        //文件名称
        String filename = uploadFileParamsDto.getFilename();
        if (StringUtils.isEmpty(objectName)) {
            objectName = fileId + filename.substring(filename.lastIndexOf("."));
        }
        //对象名称
        objectName = folder + objectName;

        // 保存文件到minion中 保存文件信息到数据库中 整合数据格式并返回
        try {
            // 上传到文件系统
            addMediaFilesToMinIO(bucket_Files, bytes, objectName);

            // 上传到数据库中
            MediaFiles mediaFiles = addMediaFilesToDb(companyId, uploadFileParamsDto, objectName, fileId, bucket_Files);

            // 整理返回体格式
            UploadFileResultDto uploadFileResultDto = new UploadFileResultDto();
            BeanUtils.copyProperties(mediaFiles, uploadFileResultDto);
            return uploadFileResultDto;

        } catch (Exception e) {
            e.printStackTrace();
            EduVideoException.cast("上传过程中出现错误，请稍后重试");
        }

        return null;
    }

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
    @NotNull
    private MediaFiles addMediaFilesToDb(Long companyId, UploadFileParamsDto uploadFileParamsDto, String objectName, String fileId, String bucket_Files) {
        // 从数据库中查询文件，根据md5查，如果存在那就不用改，如果不存在，就插入
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileId);
        if (mediaFiles == null) {
            mediaFiles = new MediaFiles();
            //拷贝基本信息
            BeanUtils.copyProperties(uploadFileParamsDto, mediaFiles);
            mediaFiles.setId(fileId);
            mediaFiles.setFileId(fileId);
            mediaFiles.setCompanyId(companyId);
            mediaFiles.setUrl("/" + bucket_Files + "/" + objectName);
            mediaFiles.setBucket(bucket_Files);
            mediaFiles.setCreateDate(LocalDateTime.now());
            mediaFiles.setStatus("1");
            // 保存到数据库中
            int insert = mediaFilesMapper.insert(mediaFiles);
            if (insert < 0) {
                EduVideoException.cast("保存文件信息到数据库失败，请重试");
            }
        }
        return mediaFiles;
    }

    /***
     * @description 上传byte字节流到minio
     * @param bucket_Files 上传的桶的名称
     * @param bytes 上传文件的byte数组
     * @param objectName 上传的路径和名称
     * @return void
     * @author zkp15
     * @date 2023/6/16 10:40
     */
    private void addMediaFilesToMinIO(String bucket_Files, byte[] bytes, String objectName) {
        // byte数组转化成byte字节流
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);

        // 如果传入的objectName包含".***"，就设置为扩展名，否则为null
        String extension = null;
        if (objectName.indexOf(".") >= 0) {
            extension = objectName.substring(objectName.lastIndexOf("."));
        }
        // 根据扩展名获取contenttype
        String contentType = getMimeTypeByExtension(extension);

        try {
            // 由于文件不在系统内，将字节流保存在minion中，使用putobjectargs
            PutObjectArgs putObjectArgs = PutObjectArgs.builder().bucket(bucket_Files).object(objectName)
                    //-1表示文件分片按5M(不小于5M,不大于5T),分片数量最大10000，
                    .stream(byteArrayInputStream, byteArrayInputStream.available(), -1)
                    .contentType(contentType)
                    .build();
            minioClient.putObject(putObjectArgs);
        } catch (Exception e) {
            e.printStackTrace();
            EduVideoException.cast("上传文件到minio出错: "+e.getMessage());
        }
    }

    /***
     * @description 通过文件后缀扩展名获取文件类型contentType
     * @param extension
     * @return java.lang.String
     * @author zkp15
     * @date 2023/6/16 10:35
     */
    private String getMimeTypeByExtension(String extension) {
        // 默认类型为未知类型的二进制流"application/octet-stream"
        String contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;

        // 如果有extension，就根据 ContentInfoUtil.findExtensionMatch(extension).getMimeType() 的方法获取类型
        if (StringUtils.isNotEmpty(extension)) {
            ContentInfo extensionMatch = ContentInfoUtil.findExtensionMatch(extension);
            if (extensionMatch != null) {
                contentType = extensionMatch.getMimeType();
            }
        }

        return contentType;
    }


    /***
     * @description 根据日期拼接目录
     * @param date
     * @param year
     * @param month
     * @param day
     * @return java.lang.String
     * @author zkp15
     * @date 2023/6/15 23:24
     */
    private String getFileFolder(Date date, boolean year, boolean month, boolean day) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        //获取当前日期字符串
        String dateString = sdf.format(new Date());
        //取出年、月、日
        String[] dateStringArray = dateString.split("-");
        StringBuffer folderString = new StringBuffer();
        if (year) {
            folderString.append(dateStringArray[0]);
            folderString.append("/");
        }
        if (month) {
            folderString.append(dateStringArray[1]);
            folderString.append("/");
        }
        if (day) {
            folderString.append(dateStringArray[2]);
            folderString.append("/");
        }
        return folderString.toString();
    }

}
