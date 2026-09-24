package com.zzccaidp.vo.knowledgebase;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;

/**
 * @Description:
 * @Author: WB233500
 * @Createtime: 16:02
 * @Version: 1.0
 */
@Data
public class DocChangeRecordIn {
    private String vectorId;

    private String docId;

    private String docName;

    private String channel;

    private String metadata;

    private String backName;

    private String awsFilePath;
}
