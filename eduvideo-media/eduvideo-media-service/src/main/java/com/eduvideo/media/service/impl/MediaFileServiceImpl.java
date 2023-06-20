package com.eduvideo.media.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.eduvideo.base.exception.EduVideoException;
import com.eduvideo.base.model.PageParams;
import com.eduvideo.base.model.PageResult;
import com.eduvideo.media.mapper.MediaFilesMapper;
import com.eduvideo.media.mapper.MediaProcessMapper;
import com.eduvideo.media.model.dto.QueryMediaParamsDto;
import com.eduvideo.media.model.dto.RestResponse;
import com.eduvideo.media.model.dto.UploadFileParamsDto;
import com.eduvideo.media.model.dto.UploadFileResultDto;
import com.eduvideo.media.model.po.MediaFiles;
import com.eduvideo.media.model.po.MediaProcess;
import com.eduvideo.media.service.MediaFileService;
import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.UploadObjectArgs;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/***
 * @description 媒体文件增删改查接口
 * @author zkp15
 * @date 2023/6/16 10:30
 */
@Slf4j
@Service
public class MediaFileServiceImpl implements MediaFileService {

    @Autowired
    private MediaFilesMapper mediaFilesMapper;

    @Autowired
    private MinioClient minioClient;

    //普通文件桶
    @Value("${minio.bucket.files}")
    private String bucket_Files;

    //大文件桶
    @Value("${minio.bucket.videofiles}")
    private String bucket_videoFiles;

    @Autowired
    private MediaFileService currentProxy;

    @Autowired
    private MediaProcessMapper mediaProcessMapper;


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
            MediaFiles mediaFiles = currentProxy.addMediaFilesToDb(companyId, uploadFileParamsDto, objectName, fileId, bucket_Files);

            // 整理返回体格式
            UploadFileResultDto uploadFileResultDto = new UploadFileResultDto();
            BeanUtils.copyProperties(mediaFiles, uploadFileResultDto);
            return uploadFileResultDto;

        } catch (Exception e) {
            e.printStackTrace();
            EduVideoException.cast("上传文件系统和数据库的过程中出现错误，请稍后重试:" + e.getMessage());
        }

        return null;
    }

    @Transactional
    @Override
    public MediaFiles addMediaFilesToDb(Long companyId, UploadFileParamsDto uploadFileParamsDto, String objectName, String fileId, String bucket_Files) {
        //根据文件名称取出媒体类型
        //扩展名
        String extension = null;
        if (objectName.indexOf(".") >= 0) {
            extension = objectName.substring(objectName.lastIndexOf("."));
        }
        //获取扩展名对应的媒体类型
        String contentType = getMimeTypeByExtension(extension);

        // 从数据库中查询文件，根据md5查，如果存在那就不用改，如果不存在，就插入
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileId);
        if (mediaFiles == null) {
            mediaFiles = new MediaFiles();
            //拷贝基本信息
            BeanUtils.copyProperties(uploadFileParamsDto, mediaFiles);
            mediaFiles.setId(fileId);
            mediaFiles.setFileId(fileId);
            mediaFiles.setCompanyId(companyId);

            //图片及mp4文件设置url
            if (contentType.indexOf("image") >= 0 || contentType.indexOf("mp4") >= 0) {
                mediaFiles.setUrl("/" + bucket_Files + "/" + objectName);
            }

            mediaFiles.setBucket(bucket_Files);
            mediaFiles.setCreateDate(LocalDateTime.now());
            mediaFiles.setStatus("1");
            // 保存到数据库中
            int insert = mediaFilesMapper.insert(mediaFiles);
            if (insert < 0) {
                EduVideoException.cast("保存文件信息到数据库失败，请重试");
            }
//            int i = 1 / 0;

            //如果是avi视频添加到视频待处理表
            if (contentType.equals("video/x-msvideo")) {
                MediaProcess mediaProcess = new MediaProcess();
                BeanUtils.copyProperties(mediaFiles, mediaProcess);
                mediaProcess.setStatus("1");//未处理
                mediaProcess.setFilePath(objectName);
                mediaProcessMapper.insert(mediaProcess);
            }

        }
        return mediaFiles;
    }

    @Override
    public RestResponse<Boolean> checkFile(String fileMd5) {
        // 根据md5值查数据库，根据数据库信息查文件系统，如果二者任一不存在就返回false
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);
        if (mediaFiles == null) {
            return RestResponse.success(false);
        }

        String bucket = mediaFiles.getBucket();
        String filePath = mediaFiles.getFilePath();

        return checkFilesExits(bucket, filePath);
    }

    @Override
    public RestResponse<Boolean> checkChunk(String fileMd5, int chunkIndex) {
        // 根据文件md5值获取文件分块目录，检查是否存在分块文件，存在返回TRUE，不存在返回FALSE
        String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);
        String chunkFilePath = chunkFileFolderPath + chunkIndex;

        return checkFilesExits(bucket_videoFiles, chunkFilePath);
    }

    /***
     * @description 根据bucket和文件路径查询文件是否存在，存在返回TRUE，不存在返回FALSE，使用RestResponse<Boolean>格式返回
     * @param bucket
     * @param chunkFilePath
     * @return com.eduvideo.media.model.dto.RestResponse<java.lang.Boolean>
     * @author zkp15
     * @date 2023/6/16 18:25
     */
    private RestResponse<Boolean> checkFilesExits(String bucket, String chunkFilePath) {
        try {
            InputStream object = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucket)
                            .object(chunkFilePath)
                            .build());
            if (object == null) {
                return RestResponse.success(false);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return RestResponse.success(false);
        }
        return RestResponse.success(true);
    }

    /***
     * @description 根据大文件的md5值获取大文件的目录
     * @param fileMd5
     * @return java.lang.String
     * @author zkp15
     * @date 2023/6/16 18:13
     */
    private String getChunkFileFolderPath(String fileMd5) {
        return fileMd5.substring(0, 1) + "/" + fileMd5.substring(1, 2) + "/" + fileMd5 + "/" + "chunk" + "/";
    }

    @Override
    public RestResponse uploadChunk(String fileMd5, int chunk, byte[] bytes) {
        // 获取分块文件路径
        String chunkFilePath = getChunkFileFolderPath(fileMd5) + chunk;

        // 上传到minio
        try {
            //将文件存储至minIO
            addMediaFilesToMinIO(bucket_videoFiles, bytes, chunkFilePath);
            return RestResponse.success(true);
        } catch (Exception e) {
            e.printStackTrace();
            log.debug("上传分块文件:{},失败:{}", chunkFilePath, e.getMessage());
        }
        return RestResponse.validfail(false, "上传分块失败");

    }

    @Override
    public RestResponse mergechunks(Long companyId, String fileMd5, int chunkTotal, UploadFileParamsDto uploadFileParamsDto) {
        // 下载所有的分块文件
        File[] chunkFiles = getChunkFiles(fileMd5, chunkTotal);

        // 将所有的分块文件进行合并
        String filename = uploadFileParamsDto.getFilename();
        String extName = filename.substring(filename.lastIndexOf("."));
        File mergeFile = null;
        try {
            mergeFile = File.createTempFile(fileMd5, extName);
        } catch (IOException e) {
            e.printStackTrace();
            EduVideoException.cast("合并文件过程中创建临时文件出错");
        }

        // 这里的大try finally是为了在结束时删除临时File文件
        try {
            // 合并文件
            byte[] bytes = new byte[1024];
            try (RandomAccessFile raf_write = new RandomAccessFile(mergeFile, "rw");) {
                for (File chunkFile : chunkFiles) {
                    try (FileInputStream chunkFileStream = new FileInputStream(chunkFile);) {
                        int len = -1;
                        while ((len = chunkFileStream.read(bytes)) != -1) {
                            //向合并后的文件写
                            raf_write.write(bytes, 0, len);
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                EduVideoException.cast("合并文件过程中出错");
            }
            log.debug("合并文件完成{}", mergeFile.getAbsolutePath());
            uploadFileParamsDto.setFileSize(mergeFile.length());

            // 对文件进行校验
            try (InputStream mergeFileInputStream = new FileInputStream(mergeFile);) {
                //对文件进行校验，通过比较md5值
                String newFileMd5 = DigestUtils.md5Hex(mergeFileInputStream);
                if (!fileMd5.equalsIgnoreCase(newFileMd5)) {
                    //校验失败
                    EduVideoException.cast("合并文件校验失败");
                }
                log.debug("合并文件校验通过{}", mergeFile.getAbsolutePath());
            } catch (Exception e) {
                e.printStackTrace();
                //校验失败
                EduVideoException.cast("合并文件校验异常");
            }

            // 上传合并文件到文件系统
            String filePath = getFilePathByMd5(fileMd5, extName);
            try {
                addMediaFilesToMinIO(mergeFile.getAbsolutePath(), bucket_videoFiles, filePath);
                log.debug("合并文件上传MinIO完成{}", mergeFile.getAbsolutePath());
            } catch (Exception e) {
                e.printStackTrace();
                EduVideoException.cast("合并文件时上传文件出错");
            }

            // 保存文件信息到数据库
            MediaFiles mediaFiles = currentProxy.addMediaFilesToDb(companyId, uploadFileParamsDto, filePath, fileMd5, bucket_videoFiles);
            if (mediaFiles == null) {
                EduVideoException.cast("媒资文件入库出错");
            }

            // 返回响应体
            return RestResponse.success();

        } finally {
            // 删除过程中临时文件
            for (File file : chunkFiles) {
                if (file.exists()) {
                    file.delete();
                }
            }
            if (mergeFile.exists()) {
                mergeFile.delete();
            }

        }
    }

    @Override
    public MediaFiles getFileById(String id) {
        return mediaFilesMapper.selectById(id);
    }

    private String getFilePathByMd5(String fileMd5, String fileExt) {
        return fileMd5.substring(0, 1) + "/" + fileMd5.substring(1, 2) + "/" + fileMd5 + "/" + fileMd5 + fileExt;
    }


    /***
     * @description 下载分块文件
     * @param fileMd5
     * @param chunkTotal
     * @return java.io.File[]
     * @author zkp15
     * @date 2023/6/16 19:43
     */
    private File[] getChunkFiles(String fileMd5, int chunkTotal) {
        // 根据md5确定文件目录
        String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);
        File[] files = new File[chunkTotal];
        // 创建临时文件保存字节流
        for (int i = 0; i < chunkTotal; i++) {
            String chunkFilePath = chunkFileFolderPath + i;

            File tempFile = null;
            try {
                tempFile = File.createTempFile("chunk" + i, null);
            } catch (IOException e) {
                e.printStackTrace();
                EduVideoException.cast("创建临时文件出错");
            }
            downloadFileFromMinIO(tempFile, bucket_videoFiles, chunkFilePath);
            files[i] = tempFile;
        }
        // 返回文件数组
        return files;
    }

    /***
     * @description 从目标桶和路径下载文件到chunkFile中
     * @param file
     * @param bucket
     * @param objectName
     * @return File
     * @author zkp15
     * @date 2023/6/16 19:52
     */
    public File downloadFileFromMinIO(File file, String bucket, String objectName) {
        // 下载文件，通过输入输出字节流的方式保存到file文件，字节流的创建在try()中完成，就不用关闭字节流了
        GetObjectArgs getObjectArgs = GetObjectArgs.builder().bucket(bucket).object(objectName).build();
        try (
                InputStream inputStream = minioClient.getObject(getObjectArgs);
                FileOutputStream outputStream = new FileOutputStream(file);
        ) {
            IOUtils.copy(inputStream, outputStream);
            return file;
        } catch (Exception e) {
            e.printStackTrace();
            EduVideoException.cast("查询分块文件出错");
        }
        return file;
    }

    /***
     * @description 将文件上传到minIO，传入文件在磁盘中的绝对路径
     * @param filePath
     * @param bucket
     * @param objectName
     * @return void
     * @author zkp15
     * @date 2023/6/16 20:13
     */
    public void addMediaFilesToMinIO(String filePath, String bucket, String objectName) {
        //扩展名
        String extension = null;
        if (objectName.indexOf(".") >= 0) {
            extension = objectName.substring(objectName.lastIndexOf("."));
        }
        //获取扩展名对应的媒体类型
        String contentType = getMimeTypeByExtension(extension);
        try {
            minioClient.uploadObject(
                    UploadObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectName)
                            .filename(filePath)
                            .contentType(contentType)
                            .build());
        } catch (Exception e) {
            e.printStackTrace();
            EduVideoException.cast("上传文件到文件系统出错");
        }
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
            EduVideoException.cast("上传文件到文件系统minio出错: " + e.getMessage());
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
