package com.zzccaidp.constants;

/**
 * @author zhangtiantian
 * @date 2026/5/8
 */
public class RagFlowConstant {
    public final static Integer HTTP_SUCCESS = 0;

    public final static String UPLOAD_DOCUMENT_URI = "/api/v1/datasets/{dataset_id}/documents";

    public final static String UPDATE_DOCUMENT_URI = "/api/v1/datasets/{dataset_id}/documents/{document_id}";

    public final static String CHUNKS_DOCUMENT_URI = "/api/v1/datasets/{dataset_id}/chunks";

    public final static String DELETE_DOCUMENT_URI = "/api/v1/datasets/{dataset_id}/documents";

    public final static String UPDATE_DOCUMENT_METADATA_URI = "/api/v1/datasets/{dataset_id}/metadata/update";

    public final static String DELETE_DATASET_URI = "/api/v1/datasets";

    public final static String CREATE_DATASET_URI = "/api/v1/datasets";
}
