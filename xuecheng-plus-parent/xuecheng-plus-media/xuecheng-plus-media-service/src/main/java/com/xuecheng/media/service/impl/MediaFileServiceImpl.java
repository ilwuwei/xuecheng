package com.xuecheng.media.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuecheng.base.Utils.FileUtils;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.base.model.RestResponse;
import com.xuecheng.media.mapper.MediaFilesMapper;
import com.xuecheng.media.model.dto.QueryMediaParamsDto;
import com.xuecheng.media.model.dto.UploadFileParamsDto;
import com.xuecheng.media.model.dto.UploadFileResultDto;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.service.MediaFileService;
import com.xuecheng.media.utils.MinIoUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
public class MediaFileServiceImpl implements MediaFileService {

    @Autowired
    MediaFilesMapper mediaFilesMapper;

    @Autowired
    MinIoUtils minioUtils;

    @Autowired
    MediaFileService currentProxy;

    //普通文件桶
    @Value("${minio.bucket.files}")
    private String BUCKET_FILES;

    @Value("${minio.bucket.videofiles}")
    private String VIDEO_FILES;

    @Override
    public PageResult<MediaFiles> queryMediaFiles(Long companyId, PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto) {

        //构建查询条件对象
        LambdaQueryWrapper<MediaFiles> queryWrapper = new LambdaQueryWrapper<>();
        //分页对象
        Page<MediaFiles> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());
        // 查询数据内容获得结果
        Page<MediaFiles> pageResult = mediaFilesMapper.selectPage(page, queryWrapper);

        return PageResult.handlerIPage(pageResult);

    }

    @Override
    @Transactional
    public MediaFiles addMediaFilesToDb(Long companyId, String fileMd5, UploadFileParamsDto uploadFileParamsDto, String bucket, String objectName) {
        //从数据库查询文件
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);
        if (mediaFiles == null) {
            mediaFiles = new MediaFiles();
            //拷贝基本信息
            BeanUtils.copyProperties(uploadFileParamsDto, mediaFiles);
            mediaFiles.setId(fileMd5);
            mediaFiles.setFileId(fileMd5);
            mediaFiles.setCompanyId(companyId);
            mediaFiles.setUrl("/" + bucket + "/" + objectName);
            mediaFiles.setBucket(bucket);
            mediaFiles.setFilePath(objectName);
            mediaFiles.setCreateDate(LocalDateTime.now());
            mediaFiles.setAuditStatus("002003");
            mediaFiles.setStatus("1");
            //保存文件信息到文件表
            int insert = mediaFilesMapper.insert(mediaFiles);
            if (insert < 0) {
                log.error("保存文件信息到数据库失败,{}", mediaFiles.toString());
                XueChengPlusException.cast("保存文件信息失败");
            }
            log.debug("保存文件信息到数据库成功,{}", mediaFiles);
        }
        return mediaFiles;

    }

    @Override
    public UploadFileResultDto uploadFile(Long companyId, UploadFileParamsDto uploadFileParamsDto, MultipartFile filedata) {
        try {
            String fileName = filedata.getOriginalFilename();
            String fileMd5 = FileUtils.getFileMd5(filedata.getInputStream());
            String extension = FileUtils.getFileExtension(fileName);
            String objectName = FileUtils.getDefaultFolderPath(fileMd5 + extension);
            // 上传文件到minio
            minioUtils.uploadObject(BUCKET_FILES, objectName, filedata.getContentType(), filedata.getInputStream());
            // 保存文件信息到数据库
            UploadFileResultDto uploadFileResultDto = new UploadFileResultDto();
            MediaFiles mediaFiles = currentProxy.addMediaFilesToDb(companyId, fileMd5, uploadFileParamsDto, BUCKET_FILES, objectName);
            BeanUtils.copyProperties(mediaFiles, uploadFileResultDto);
            return uploadFileResultDto;
        } catch (IOException e) {
            e.printStackTrace();
            throw new XueChengPlusException("文件上传失败");
        }

    }

    @Override
    public RestResponse<Boolean> checkFile(String fileMd5) {
        // 检查文件信息是否存在数据库
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);
        if (mediaFiles != null) {
            String bucket = mediaFiles.getBucket();
            String filePath = mediaFiles.getFilePath();
            // 检查文件是否存在
            boolean isExists = minioUtils.checkObjectIsExists(bucket, filePath);
            return RestResponse.success(isExists);
        }
        // 文件不存在
        return RestResponse.success(false);
    }


    @Override
    public RestResponse<Boolean> checkChunk(String fileMd5, int chunkIndex) {
        String chunkFileFolderPath = this.getChunkFileFolderPath(fileMd5);
        // 分块文件路径
        String chunkFilePath = chunkFileFolderPath + chunkIndex;
        boolean isExists = minioUtils.checkObjectIsExists(VIDEO_FILES, chunkFilePath);
        return RestResponse.success(isExists);
    }

    @Override
    public RestResponse<Boolean> uploadFileChuck(InputStream stream, String fileMd5, int chunk) {
        // 获取分块文件上传路径
        String chunkFilePath = this.getChunkFileFolderPath(fileMd5) + chunk;
        minioUtils.uploadObject("video", chunkFilePath, null, stream);
        return RestResponse.success();
    }


    @Override
    public RestResponse mergeChunks(Long companyId, String fileMd5, int chunkTotal, UploadFileParamsDto uploadFileParamsDto) {
        //得到分块文件目录
        String chunkFileFolderPath = this.getChunkFileFolderPath(fileMd5);
        //得到分块文件的路径
        List<String> chunkFileList = Stream.iterate(0, i -> ++i)
                .limit(chunkTotal)
                .map(i -> chunkFileFolderPath + i)
                .collect(Collectors.toList());
        // 合并后的文件名
        String filename = uploadFileParamsDto.getFilename();
        String extension = FileUtils.getFileExtension(filename);
        // 合并文件的路径
        String objectPath = this.getFilePathByMd5(fileMd5, extension);
        // 合并文件
        minioUtils.mergerObject(VIDEO_FILES, chunkFileList, objectPath);
        // ====验证md5====
        InputStream stream = minioUtils.getObjectStream(VIDEO_FILES, objectPath);
        String md5 = FileUtils.getFileMd5(stream);
        if (!fileMd5.equals(md5)) {
            XueChengPlusException.cast("文件上传失败");
        }
        // 清除分块文件
        minioUtils.removeBatchObject(VIDEO_FILES, chunkFileList);
        // 文件信息入库
        currentProxy.addMediaFilesToDb(companyId, fileMd5, uploadFileParamsDto, VIDEO_FILES, objectPath);
        return RestResponse.success(true);
    }

    /**
     * 得到分块文件的目录
     *
     * @param fileMd5
     * @return
     */
    private String getChunkFileFolderPath(String fileMd5) {
        return fileMd5.substring(0, 1) + "/" + fileMd5.substring(1, 2) + "/" + fileMd5 + "/" + "chunk" + "/";
    }

    /**
     * 得到合并后的文件的地址
     *
     * @param fileMd5 文件id即md5值
     * @param fileExt 文件扩展名
     * @return
     */
    private String getFilePathByMd5(String fileMd5, String fileExt) {
        return fileMd5.substring(0, 1) + "/" + fileMd5.substring(1, 2) + "/" + fileMd5 + "/" + fileMd5 + fileExt;
    }


}
