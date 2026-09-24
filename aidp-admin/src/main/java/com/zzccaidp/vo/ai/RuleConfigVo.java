package com.zzccaidp.vo.ai;

import com.zzccaidp.vo.PageRequest;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

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
@Table(name = "rule_config")
public class RuleConfigVo extends PageRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @Id
    private String id;

    /**
     * 规则编号
     */
    private String ruleId;

    /**
     * 规则名称
     */
    private String ruleName;

    /**
     * 规则描述
     */
    private String ruleDescription;

    /**
     * 提取字段
     */
    private String extractFieldFileid;

    /**
     * 字段映射
     */
    private String fieldMappingFileid;
    /**
     * 提示词
     */
    private String promptFileid;

    /**
     * 应用id
     */
    private String appid;

    /**
     * 场景（fast：快思考，deep：慢思考）
     */
    private String scene;

    /**
     * 提取模式（1：统一提取；2：提取模式）
     */
    private String mode;

    /**
     * 识别方式（1：标准文档识别；2：表格文档识别）
     */
    private String recognizeType;

    /**
     * 指定文档页码
     */
    private String page;

    /**
     * 参数配置
     */
    private String parametersConfig;

    /**
     * 创建人
     */
    private String createdBy;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createdTime;

    /**
     * 修改人
     */
    private String modifiedBy;

    /**
     * 修改时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date modifiedTime;

    /**
     * 模板id
     */
    private String templateId;

    /**
     * 新旧模式标识
     */
    private String flag;

    /**
     * 状态（0-未启用，1-启用）
     */
    private String status;


    private String difyApiKeyId;
}
