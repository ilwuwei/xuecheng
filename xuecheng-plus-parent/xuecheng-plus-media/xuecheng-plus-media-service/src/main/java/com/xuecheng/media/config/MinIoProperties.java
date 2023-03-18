package com.xuecheng.media.config;


import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class MinIoProperties {

    /**
     * minio地址+端口号
     */
    @Value("${minio.endpoint}")
    private String endpoint;

    /**
     * minio用户名
     */
    @Value("${minio.accessKey}")
    private String accessKey;

    /**
     * minio密码
     */
    @Value("${minio.secretKey}")
    private String secretKey;


    /**
     * mediaFile
     */
    @Value("${minio.bucket.files}")
    private String mediaFile;

    /**
     * video
     */
    @Value("${minio.bucket.videofiles}")
    private String video;


}

