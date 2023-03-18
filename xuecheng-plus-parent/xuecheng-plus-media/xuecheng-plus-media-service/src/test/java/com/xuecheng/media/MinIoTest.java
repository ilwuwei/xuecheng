package com.xuecheng.media;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.UploadObjectArgs;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;

public class MinIoTest {

    static MinioClient minioClient =
            MinioClient.builder()
                    .endpoint("http://192.168.100.200:9000")
                    .credentials("H4uq69VT3xrExcds", "fsyKTarxKWq7BjamYNReK3rfexI7gBw8")
                    .build();


    @Test
    public void upload02() {
        try {
            FileInputStream inputStream = new FileInputStream("E:\\Wallpaper\\00.jpg");
            PutObjectArgs putObjectArgs = PutObjectArgs.builder()
                    .bucket("mediafiles")
                    .object("1.png")
                    .stream(inputStream, inputStream.available(), -1)
                    .contentType("image/png")
                    .build();
            minioClient.putObject(putObjectArgs);
            System.out.println("上传成功");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("上传失败");
        }
    }

    @Test
    public void upload01() {
        try {
            UploadObjectArgs testbucket = UploadObjectArgs.builder()
                    .bucket("mediafiles")
                    .object("001/test001.mp4")//添加子目录
                    .filename("E:\\Captures\\一人之下.mp4")
                    .contentType("video/mp4")//默认根据扩展名确定文件内容类型，也可以指定
                    .build();
            minioClient.uploadObject(testbucket);
            System.out.println("上传成功");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("上传失败");
        }

    }
}