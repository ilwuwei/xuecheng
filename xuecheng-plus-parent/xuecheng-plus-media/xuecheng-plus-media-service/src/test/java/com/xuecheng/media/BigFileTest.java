package com.xuecheng.media;

import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;


public class BigFileTest {


    /**
     * 视频分块
     */
    @Test
    public void testChunk() throws Exception {
        // 源文件路径
        File file = new File("E:\\媒体资源\\视频\\test.mp4");
        // 分块之后文件存放路径
        File output = new File("E:\\媒体资源\\视频\\chunk\\");
        if (!output.exists()) {
            output.mkdirs();
        }
        // 分块大小
        int size = 1024 * 1024 * 5;
        // 分块数量
        int chunkNum = (int) Math.ceil(file.length() * 1.0 / size);
        FileInputStream inputStream = new FileInputStream(file);
        byte[] bytes = new byte[1024];
        for (int i = 0; i < chunkNum; i++) {
            int len;
            File outputFile = new File(output, String.valueOf(i));
            FileOutputStream outputStream = new FileOutputStream(outputFile);
            while ((len = inputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, len);
                if (outputFile.length() >= size) {
                    break;
                }
            }
            outputStream.close();
        }
        inputStream.close();
    }


    /**
     * 合并分块
     */
    @Test
    public void testMerge() throws Exception {
        // 分块路径
        File input = new File("E:\\媒体资源\\视频\\chunk\\");

        // 分块文件
        File[] files = input.listFiles();
        // 根据名字排序分块文件
        List<File> fileList = Arrays.stream(files).sorted(Comparator.comparingInt(f -> Integer.parseInt(f.getName()))).collect(Collectors.toList());

        FileOutputStream outputStream = new FileOutputStream(new File("E:\\媒体资源\\视频\\test_copy.mp4"));

        byte[] bytes = new byte[1024];
        // 合并文件
        for (File file : fileList) {
            FileInputStream inputStream = new FileInputStream(file);
            int len;
            while ((len = inputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, len);
            }
            inputStream.close();
        }
        outputStream.close();
    }
}
