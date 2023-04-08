package com.xuecheng.media.utils;

import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.media.config.MinIoProperties;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.StatObjectArgs;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.InputStream;

@Slf4j
@Component
public class MinIoUtils {

    @Autowired
    MinIoProperties minIoProperties;

    private static MinioClient minioClient;


    /**
     * 初始化minio配置
     */
    @PostConstruct
    public void init() {
        try {
            minioClient = MinioClient.builder()
                    .endpoint(minIoProperties.getEndpoint())
                    .credentials(minIoProperties.getAccessKey(), minIoProperties.getSecretKey())
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
            log.error("初始化minio配置异常");
        }
    }




    /**
     * 通过文件流上传文件到minio
     *
     * @param bucket      桶
     * @param objectName  文件路径
     * @param contentType 上传文件类型
     * @param stream      文件流
     */
    public void uploadObject(String bucket, String objectName, String contentType, InputStream stream) {
        if (contentType == null) {
            contentType = "application/octet-stream";
        }
        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .stream(stream, stream.available(), -1)
                    .contentType(contentType)
                    .build());
            log.info("文件上传成功, bucket：{},  objectName: {}", bucket, objectName);
        } catch (Exception e) {
            log.error("文件上传失败, bucket: {}, objectName: {}", bucket, objectName);
            e.printStackTrace();
            throw new XueChengPlusException("文件上传失败");
        }
    }


    /**
     * 检测文件是否存在于minio
     *
     * @param bucket
     * @param objectName
     * @return
     */
    public boolean checkObjectIsExists(String bucket, String objectName) {
        try {
            minioClient.statObject(
                    StatObjectArgs.builder().bucket(bucket).object(objectName).build()
            );
        } catch (Exception e) {
            return false;
        }
        return true;
    }
}
