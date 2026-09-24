package com.zzccaidp.service.documentPlatform.dto;

import lombok.Data;

import java.util.Date;

/**
 * @author zhangtiantian
 * @date 2026/5/11
 */
@Data
public class FileInfoDTO {
    private String fileId;

    private String fileName;

    private String fileSize;

    private String createUser;

    private Date createTime;

    private String bucketName;

    private String path;
}
