package com.zzccaidp.dao.ai;

import lombok.Data;

import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @Description:
 * @Author: WB233500
 * @Createtime: 16:03
 * @Version: 1.0
 */
@Data
@Table(name = "zzccaidp_sys_file")
public class SysFileInfoDO implements Serializable {

    @Id
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
}
