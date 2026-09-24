package com.zzccaidp.dao.ai;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 规则配置表
 * </p>
 *
 * @author WB255485
 * @since 2025-11-19
 */
@Data
@Accessors(chain = true)
@Table(name = "zzccaidp_rule_config")
public class RuleConfigDO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @Id
    @Column(name = "id", length = 64)
    private String id;

    /**
     * 规则编号
     */
    @Column(name = "rule_id", length = 32, nullable = false)
    private String ruleId;
    /**
     * 规则编号
     */
    @Column(name = "rule_name", length = 128, nullable = false)
    private String ruleName;

    /**
     * 规则描述
     */
    @Column(name = "rule_description", length = 255)
    private String ruleDescription;

    /**
     * 提取字段
     */
    @Column(name = "extract_field_fileid", length = 255)
    private String extractFieldFileid;

    /**
     * 字段映射
     */
    @Column(name = "field_mapping_fileid", length = 255)
    private String fieldMappingFileid;
    /**
     * 提示词
     */
    @Column(name = "prompt_fileid", length = 255)
    private String promptFileid;

    /**
     * 应用id
     */
    @Column(name = "appid", length = 64)
    private String appid;

    /**
     * 场景（fast：快思考，deep：慢思考）
     */
    @Column(name = "scene", length = 10)
    private String scene;

    /**
     * 提取模式（1：统一提取；2：提取模式）
     */
    @Column(name = "mode", length = 20)
    private String mode;

    /**
     * 识别方式（1：标准文档识别；2：表格文档识别）
     */
    @Column(name = "recognizeType", length = 10)
    private String recognizeType;

    /**
     * 指定文档页码
     */
    @Column(name = "page", length = 10)
    private String page;

    /**
     * 参数配置
     */
    @Column(name = "parameters_config", length = 500)
    private String parametersConfig;

    /**
     * 创建人
     */
    @Column(name = "created_by", length = 50)
    private String createdBy;

    /**
     * 创建时间
     */
    @Column(name = "created_time", nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createdTime;

    /**
     * 修改人
     */
    @Column(name = "modified_by", length = 50)
    private String modifiedBy;

    /**
     * 修改时间
     */
    @Column(name = "modified_time", nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime modifiedTime;
    /**
     * 修改时间
     */
    @Column(name = "template_id", nullable = false)
    private String templateId;
    /**
     * 新旧模式标识
     */
    @Column(name = "flag", nullable = false)
    private String flag;

    /**
     * 状态（0-未启用，1-启用）
     */
    @Column(name = "status", nullable = false)
    private String status;


    /**
     * difyApiKeyId
     */
    @Column(name = "dify_api_key_id")
    private String difyApiKeyId;


    /**
     * difyApiKey
     */
    @Transient
    private String difyApiKey;
}
