package com.xuecheng.base.Utils;

import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.http.MediaType;



import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FileUtils {

    /**
     * 生成文件默认存储目录路径 年/月/日/文件名
     *
     * @param fileName
     * @return
     */
    public static String getDefaultFolderPath(String fileName) {
        SimpleDateFormat df = new SimpleDateFormat("yyy-MM-dd");
        String date = df.format(new Date());
        String fileObsPath = date.replace("-", "/") + "/" + fileName;
        return fileObsPath;
    }


    /**
     * 获取文件输入流的md5值
     *
     * @param inputStream
     * @return
     */
    public static String getFileMd5(InputStream inputStream) {
        try {
            String fileMd5 = DigestUtils.md5Hex(inputStream);
            return fileMd5;
        } catch (IOException e) {
            e.printStackTrace();
            return null;

        }
    }


    /**
     * 获取文件后缀
     *
     * @param fileName
     * @return
     */
    public static String getFileExtension(String fileName) {
        if (fileName == null) {
            fileName = "";
        }
        return fileName.substring(fileName.lastIndexOf("."));
    }

    /**
     * 根据文件后缀获取文件类型
     *
     * @param extension
     * @return
     */
    public static String getMediaType(String extension) {
        if (extension == null) {
            extension = "";
        }
        ContentInfo extensionMatch = ContentInfoUtil.findExtensionMatch(extension);
        String mineType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        if (extensionMatch != null) {
            mineType = extensionMatch.getMimeType();
        }
        return mineType;
    }

}
