package com.eduvideo;

import org.mybatis.spring.annotation.MapperScan;
import org.mybatis.spring.annotation.MapperScans;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages={"com.eduvideo.content.feignclient"})
public class EduvideoContentServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(EduvideoContentServiceApplication.class, args);
    }

}
