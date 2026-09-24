package com.zzccaidp.vo.ai;

import com.zzccaidp.vo.ResHeader;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.persistence.Id;
import java.io.Serializable;

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
public class RuleConfigInfoVo extends ResHeader implements Serializable {

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

    /**
     * difyApiKeyId
     */
    private String difyApiKeyId;

}
