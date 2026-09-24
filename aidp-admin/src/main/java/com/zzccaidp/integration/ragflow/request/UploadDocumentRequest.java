package com.zzccaidp.integration.ragflow.request;

import lombok.Data;

/**
 * @author zhangtiantian
 * @date 2026/3/24
 */
@Data
public class UploadDocumentRequest {

    /*
    aws桶路径
     */
    private String filePath;

    /*
    数据集Id
     */
    private String datasetId;

    /*
    文件名称
     */
    private String fileName;


    private String bucketName;
}
