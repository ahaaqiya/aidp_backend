package com.zzccaidp.service.knowledgebase;

import com.zzccaidp.dao.knowledgebase.PersonalDatasetDO;
import com.zzccaidp.enums.ErrCodeEnum;
import com.zzccaidp.exception.BusinessException;
import com.zzccaidp.integration.ragflow.RagFlowIntegrationService;
import com.zzccaidp.integration.ragflow.request.CreateDataSetRequest;
import com.zzccaidp.integration.ragflow.response.CreateDataSetResponse;
import com.zzccaidp.mapper.knowledgebase.PersonalDatasetMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * 个人知识库数据集服务：维护「工号 → 个人 dataset_id」映射（一人一库）。
 * <p>
 * 独立成 Service 的原因：个人文档的手动上传（{@link PersonalDocumentService}）与
 * zsk 推送落库（{@code com.zzccaidp.facade.AidpDocumentServiceImpl}）都需要解析用户 dataset，
 * 抽公共依赖可避免 Service 之间形成循环依赖。
 */
@Slf4j
@Service
public class PersonalDatasetService {

    @Autowired
    private PersonalDatasetMapper personalDatasetMapper;

    @Autowired
    private RagFlowIntegrationService ragFlowIntegrationService;

    /**
     * 个人 dataset 使用的嵌入模型（必须与公共库一致，否则向量空间不匹配导致跨库检索错乱）
     */
    @Value("${ragFlow.embeddingModel:BAAI/bge-large-zh-v1.5}")
    private String embeddingModel;

    /**
     * 懒创建/复用指定用户的个人 dataset。
     * <p>
     * 优先从 personal_dataset 表查询已有映射；不存在时调用 ragflow 创建 dataset 并落表。
     * 并发场景由 user_code 唯一索引兜底（重复插入会回滚，用户重试即可）。
     *
     * @param userCode 用户工号
     * @return ragflow 数据集ID
     */
    public String getOrCreatePersonalDataset(String userCode) {
        String datasetId = getPersonalDatasetId(userCode);
        if (StringUtils.isNotBlank(datasetId)) {
            return datasetId;
        }

        CreateDataSetRequest request = new CreateDataSetRequest();
        request.setName(userCode + "_个人知识库");
        request.setDescription("个人知识库");
        request.setEmbeddingModel(embeddingModel);
        request.setPermission("me");
        request.setChunkMethod("naive");

        CreateDataSetResponse response = ragFlowIntegrationService.createDataSet(request);
        if (response == null || StringUtils.isBlank(response.getId())) {
            log.error("创建个人知识库数据集失败，userCode：{}", userCode);
            throw new BusinessException(ErrCodeEnum.M7007);
        }

        PersonalDatasetDO pd = new PersonalDatasetDO();
        pd.setUserCode(userCode);
        pd.setDatasetId(response.getId());
        pd.setCreateTime(new Date());
        pd.setUpdateTime(new Date());
        personalDatasetMapper.insertSelective(pd);
        return response.getId();
    }

    /**
     * 仅查询指定用户已存在的个人 dataset，不存在返回 null（不触发创建）。
     *
     * @param userCode 用户工号
     * @return ragflow 数据集ID，未创建时返回 null
     */
    public String getPersonalDatasetId(String userCode) {
        if (StringUtils.isBlank(userCode)) {
            return null;
        }
        PersonalDatasetDO existing = personalDatasetMapper.selectByUserCode(userCode);
        if (existing == null) {
            return null;
        }
        return existing.getDatasetId();
    }
}
