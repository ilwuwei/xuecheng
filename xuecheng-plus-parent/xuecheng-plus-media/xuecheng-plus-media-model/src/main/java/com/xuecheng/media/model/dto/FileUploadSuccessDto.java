package com.xuecheng.media.model.dto;


import lombok.Data;

@Data
public class FileUploadSuccessDto {
    private String fileMd5;

    private String fileExt;

    private String objectName;

    private String mediaType;

}
