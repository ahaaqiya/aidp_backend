package com.zzccaidp.integration.ragflow.request;

import com.alibaba.fastjson.parser.ParserConfig;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

/**
 * @author zhangtiantian
 * @date 2026/3/24
 */
// RAGFlow 接口字段为下划线命名，需将驼峰字段序列化为下划线，否则 embedding_model/chunk_method
// 等字段会被 RAGFlow 的 pydantic 校验以 “Extra inputs are not permitted” 拒绝
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Data
public class CreateDataSetRequest {

    /*
    名称
     */
    private String name;

    /*
    头像 base64
     */
    private String avatar;

    /*
    描述
     */
    private String description;

    /*
    嵌入模型
     */
    private String embeddingModel;

    /*
    权限 me/team
     */
    private String permission;

    /*
    分块方式
     */
    private String chunkMethod;

    /*
    解析配置
     */
    private ParserConfig parserConfig;

    /*
    Pipeline模式
     */
    private String parseType;

    /*
    pipelineId
     */
    private String pipelineId;
}
