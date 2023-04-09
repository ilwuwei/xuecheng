package com.xuecheng.media.utils;

import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.media.config.MinIoProperties;
import io.minio.*;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

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
     * 从minio下载文件
     *
     * @param bucket
     * @param objectPath
     * @return
     */
    public InputStream getObjectStream(String bucket, String objectPath) {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectPath)
                            .build()
            );
        } catch (Exception e) {
            e.printStackTrace();
            log.error("文件下载失败, bucket: {}, objectName: {}", bucket, objectPath);
            throw new XueChengPlusException("文件下载失败");
        }
    }


    /**
     * 合并文件
     *
     * @param bucket
     * @param objectList
     */
    public void mergerObject(String bucket, List<String> objectList, String mergerFileName) {

        List<ComposeSource> composeSourceList =
                objectList.stream()
                        .map(file -> ComposeSource.builder().bucket(bucket).object(file).build())
                        .collect(Collectors.toList());
        try {
            minioClient.composeObject(
                    ComposeObjectArgs
                            .builder()
                            .bucket(bucket)
                            .object(mergerFileName)
                            .sources(composeSourceList)
                            .build()
            );
            log.info("文件合并成功");
        } catch (Exception e) {
            log.info("文件合并失败");
            throw new XueChengPlusException("文件合并失败");
        }
    }


    /**
     * 批量删除文件
     *
     * @param bucket
     * @param objectList
     */
    public void removeBatchObject(String bucket, List<String> objectList) {
        List<DeleteObject> deleteObjects = objectList
                .stream()
                .map(file -> new DeleteObject(file))
                .collect(Collectors.toList());

        Iterable<Result<DeleteError>> results = minioClient.removeObjects(
                RemoveObjectsArgs
                        .builder()
                        .bucket(bucket)
                        .objects(deleteObjects)
                        .build()
        );
        results.forEach(r -> {
            try {
                r.get();
            } catch (Exception e) {
                throw new XueChengPlusException("文件删除失败");
            }
        });
        log.info("文件删除成功, 删除文件数量: {}", objectList.size());
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
