package com.zzccaidp.vo.ai;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @Description:
 * @Author: WB233500
 * @Createtime: 17:14
 * @Version: 1.0
 */
@Data
public class SysFileInfoVO {

    private String fileId;
    private String fileType;
    private String fileName;
    private String filePath;
    private String fileBusinessTyps;
    private String fileStorageTyps;
    private String createUser;
    private String updateUser;
    private String isDelete;
    private String fileSize;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer scratchFile;

    private String fileExtractionResult;
    private String fileExtractionTemplate;
    private String fileParsingResult;
    private String keyData;
}
