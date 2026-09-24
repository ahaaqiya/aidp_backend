package com.zzccaidp.service.ragFlow;

import com.zzccaidp.dao.knowledgebase.DocTypeDO;
import com.zzccaidp.dao.knowledgebase.DocumentDO;
import com.zzccaidp.service.ragFlow.dto.RagFlowPipelineDTO;

import java.util.List;
import java.util.Map;


/**
 * @author zhangtiantian
 * @date 2026/5/8
 */
public interface RagFlowService {

    void file2RagFlowPipeline(RagFlowPipelineDTO ragFlowPipelineDTO);

    boolean deleteRagDocument(String docId);

    boolean deleteRagDataset(DocTypeDO docTypeDO);

    void updateMetadata(String docId, Map<String, Object> oldMetadata);

    boolean submitVectorTask(String id,String operatorType);

    void updateProcessingDocumentsStatus(List<DocumentDO> documentDOList);

    /**
     * 查询 ragflow 文档解析状态（run 字段）。
     * <p>
     * 与 {@link #updateProcessingDocumentsStatus(List)} 的区别：本方法直接接收 datasetId，
     * 不依赖 doc_type 表反查，适用于 datasetId 存放在 document.own_dataset_id 的个人知识库文档。
     *
     * @param vectorId  ragflow 文档ID（document.vector_id）
     * @param datasetId ragflow 数据集ID（document.own_dataset_id）
     * @return ragflow 官方 run 状态：0/UNSTART-未开始，1/RUNNING-处理中，2/CANCEL-已取消，
     *         3/DONE-成功，4/FAIL-失败，5/SCHEDULE-排队中；未查询到返回 null
     */
    String getDocumentRunStatus(String vectorId, String datasetId);
}
