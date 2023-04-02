package com.xuecheng.media;

import com.xuecheng.media.config.MinIoProperties;
import io.minio.*;
import io.minio.errors.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Slf4j
public class MinIoTest {


    static MinioClient minioClient =
            MinioClient.builder()
                    .endpoint("http://192.168.100.200:9000")
                    .credentials("QkT9WuCzofyrwlKN", "jbOfAcQtsTK3bt39VlGfVdyUV48BNGnf")
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


    /**
     * 上传分块到minio
     */
    @Test
    public void uploadChunk() throws Exception {
        // 分块路径
        File input = new File("E:\\媒体资源\\视频\\chunk\\");
        // 分块文件
        File[] files = input.listFiles();
        // 根据名字排序分块文件
        List<File> fileList = Arrays.stream(files).sorted(Comparator.comparingInt(f -> Integer.parseInt(f.getName()))).collect(Collectors.toList());

        for (File file : fileList) {
            FileInputStream inputStream = new FileInputStream(file);
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket("testbucket")
                    .object("chunk/" + file.getName())
                    .stream(inputStream, inputStream.available(), -1)
                    .build());
        }
    }

    @Test
    public void mergeChunk() throws Exception {

        List<ComposeSource> composeSources = Stream.iterate(0, i -> ++i)
                .limit(7)
                .map(i -> ComposeSource.builder().bucket("testbucket").object("chunk/" + i).build()).collect(Collectors.toList());

        ComposeObjectArgs composeObjectArgs =
                ComposeObjectArgs
                        .builder()
                        .bucket("testbucket")
                        .object("test.mp4")
                        .sources(composeSources)
                        .build();

        minioClient.composeObject(composeObjectArgs);
    }
}