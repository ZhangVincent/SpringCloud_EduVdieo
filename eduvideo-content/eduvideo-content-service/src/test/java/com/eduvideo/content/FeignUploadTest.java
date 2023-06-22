package com.eduvideo.content;

import com.eduvideo.content.config.MultipartSupportConfig;
import com.eduvideo.content.feignclient.MediaServiceClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

/**
 * @author zkp15
 * @version 1.0
 * @description 保存静态文件到minio测试
 * @date 2023/6/22 16:50
 */
@SpringBootTest
public class FeignUploadTest {

    @Autowired
    private MediaServiceClient mediaServiceClient;

    //远程调用，上传文件
    @Test
    public void test() {

        MultipartFile multipartFile = MultipartSupportConfig.getMultipartFile(new File("C:\\Users\\zkp15\\Documents\\test.html"));
        mediaServiceClient.uploadFile(multipartFile, "course", "test.html");
    }

}

