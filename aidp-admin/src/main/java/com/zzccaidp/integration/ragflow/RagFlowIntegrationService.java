package com.zzccaidp.integration.ragflow;

import com.zzccaidp.integration.ragflow.request.*;
import com.zzccaidp.integration.ragflow.response.*;
import com.zzccaidp.vo.knowledgebase.RagFlowDocumentListDetail;

/**
 * @author zhangtiantian
 * @date 2026/3/24
 */

public interface RagFlowIntegrationService {

    CreateDataSetResponse createDataSet(CreateDataSetRequest createDataSetRequest);

    UploadDocumentResponse uploadDocument(UploadDocumentRequest uploadDocumentRequest);

    ChunkDocumentResponse chunkDocument(ChunkDocumentRequest chunkDocumentRequest);

    UpdateDocumentResponse updateDocument(UpdateDocumentRequest updateDocumentRequest);

    UpdateDocMetadataResponse updateDocumentMetadata(UpdateDocMetadataRequest updateDocMetadataRequest);

    RagFlowResponse<DeleteDocumentResponse> deleteRagDocument(String docId, String datasetId);

    RagFlowResponse<DeleteDatasetResponse> deleteRagDataset(String datasetId);

    RagFlowDocumentListDetail getRagDocumentDetail(String vectorId, String datasetId);
}
