package com.zzccaidp.vo.knowledgebase;

import lombok.Data;

/**
 * @Description:
 * @Author: WB233500
 * @Createtime: 16:02
 * @Version: 1.0
 */
@Data
public class DocChangeRecordOut {
    private String vectorId;

    private String docId;

    private String docName;

    private String channel;

    private String metadata;

    private String backName;

    private String awsFilePath;
}
