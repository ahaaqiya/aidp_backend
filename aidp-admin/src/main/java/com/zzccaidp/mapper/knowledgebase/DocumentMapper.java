package com.zzccaidp.mapper.knowledgebase;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zzccaidp.dao.knowledgebase.DocTypeDO;
import com.zzccaidp.dao.knowledgebase.DocumentDO;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface DocumentMapper extends Mapper<DocumentDO> {
    DocumentDO selectById(@Param("id") Long id);

    DocumentDO selectByDocId(@Param("docId") String docId);

    List<DocumentDO> selectByDataSourceId(Long dataSourceId);

    List<DocumentDO> selectAll();

    List<DocumentDO> selectByCondition(DocumentDO queryDO);

    /**
     * 个人知识库专用条件查询（与公共 selectByCondition 隔离）。
     * <p>
     * 过滤条件：docType='personal' + creator=当前工号（必传），channel/keyword 可选；
     * 返回结果中的 ownDatasetId 取自 document.own_dataset_id，供个人库向量化状态回刷使用。
     *
     * @param queryDO 查询条件（docType/creator/channel/keyword）
     * @return 个人知识库文档列表
     */
    List<DocumentDO> selectPersonalByCondition(DocumentDO queryDO);

    /**
     * 个人知识库专用单条查询（与公共 selectByDocId 隔离，不关联 doc_type 表）。
     *
     * @param docId 业务文档ID
     * @return 个人知识库文档，含 ownDatasetId
     */
    DocumentDO selectPersonalByDocId(@Param("docId") String docId);

    /**
     * 个人知识库专用「业务文档ID + 渠道」单条查询（与公共 findByDocIdAndChannel 隔离）。
     * <p>
     * 不关联 doc_type 表（该表不登记 personal），datasetId 取文档自身的 own_dataset_id，
     * 供 zsk 回调 AIDP 的提取/删除链路使用。
     *
     * @param docId   业务文档ID
     * @param channel 来源渠道
     * @return 个人知识库文档，含 datasetId（来自 own_dataset_id）
     */
    DocumentDO selectPersonalByDocIdAndChannel(@Param("docId") String docId, @Param("channel") String channel);

    List<DocumentDO> search(@Param("dataSourceId") Long dataSourceId,
                            @Param("taskStatus") String taskStatus,
                            @Param("fileType") String fileType,
                            @Param("docType") String docType,
                            @Param("keyword") String keyword);

    int insert(DocumentDO document);

    int updateDocType(@Param("id") Long id, @Param("docType") String docType, @Param("docTypeName") String docTypeName);

    int updateTaskStatus(@Param("id") Long id, @Param("taskStatus") String taskStatus);

    int updateVectorStatus(@Param("id") Long id, @Param("vectorEnabled") String vectorEnabled, @Param("switchStatus") Boolean switchStatus);

    int updateMetadata(@Param("id") Long id, @Param("metadata") String metadata);

    int deleteById(Long id);

    int batchDeleteByDocTypeId(@Param("docTypeId") String docTypeId);

    int batchUpdateTaskStatus(@Param("documents") List<DocumentDO> documents);


    int countAll();

    int countSearch(@Param("dataSourceId") Long dataSourceId,
                    @Param("taskStatus") String taskStatus,
                    @Param("fileType") String fileType,
                    @Param("docType") String docType,
                    @Param("keyword") String keyword);

    List<String> selectDocChannel(String docTypeCode);

    List<JSONObject> selectTypeDistribution(String channel);

    Integer selectDocCountByDocType(String code);

    List<String> selectDataSourceCountByDocType(String code);

    Integer selectDocCountByChannel(String channel);

    DocumentDO findByDocIdAndChannel(String docId, String channel);
}