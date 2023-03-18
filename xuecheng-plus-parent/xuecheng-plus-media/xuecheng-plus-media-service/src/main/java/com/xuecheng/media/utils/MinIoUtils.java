package com.xuecheng.media.utils;

import com.xuecheng.base.Utils.FileUtils;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.media.config.MinIoProperties;
import com.xuecheng.media.model.dto.FileUploadSuccessDto;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.FileInputStream;
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
     * 将文件写入minio
     *
     * @param filedata
     * @param bucket
     * @return
     */
    public String uploadMediaFilesToMinIO(String bucket, MultipartFile filedata) {
        String objectName = null;
        try {
            InputStream inputStream = filedata.getInputStream();
            String fileName = filedata.getOriginalFilename();
            String fileMd5 = FileUtils.getFileMd5(inputStream);
            String extension = FileUtils.getFileExtension(fileName);
            objectName = FileUtils.getDefaultFolderPath(fileMd5 + extension);
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .stream(filedata.getInputStream(), filedata.getSize(), -1)
                    .contentType(filedata.getContentType())
                    .build());
            log.info("文件上传成功, bucket：{},  objectName: {}", bucket, objectName);
            return objectName;
        } catch (Exception e) {
            log.error("文件上传失败, bucket：{},  objectName: {}", bucket, objectName);
            e.printStackTrace();
            throw new XueChengPlusException("文件上传失败");
        }
    }
}
