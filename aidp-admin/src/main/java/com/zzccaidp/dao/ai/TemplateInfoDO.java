package com.zzccaidp.dao.ai;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
@Table(name = "zzccaidp_template_info")
public class TemplateInfoDO implements Serializable {

    /**
     * 模板ID，主键
     */
    @Id
    @Column(name = "id", length = 64)
    private String id;

    /**
     * 规则编号
     */
    @Column(name = "rule_id", length = 32)
    private String ruleId;

    /**
     * 模板名称，模板的唯一标识
     */
    @Column(name = "template_name", length = 255, nullable = false)
    private String templateName;

    /**
     * 模板类型，如“提示词模板”、“JSON模板”等
     */
    @Column(name = "template_type", length = 50, nullable = false)
    private String templateType;

    /**
     * 模板内容，存储模板的具体内容
     */
    @Column(name = "template_content")
    private String templateContent;
    /**
     * 提示词内容
     */
    @Column(name = "prompt_content")
    private String promptContent;

    /**
     * 创建人，记录模板的创建者
     */
    @Column(name = "creator", length = 50)
    private String creator;

    /**
     * 创建时间，记录模板的创建时间
     */
    @Column(name = "create_time")
    private LocalDateTime createTime;

    /**
     * 修改人，记录模板的最后修改者
     */
    @Column(name = "modifier", length = 50)
    private String modifier;

    /**
     * 修改时间，记录模板的最后修改时间
     */
    @Column(name = "modify_time")
    private LocalDateTime modifyTime;

    /**
     * 模板描述
     */
    @Column(name = "template_desc")
    private String templateDesc;

    /**
     * 模板状态（0-未启用，1-启用）
     */
    @Column(name = "status")
    private String status;

    /**
     * 所属部门
     */
    @Column(name = "dept_id")
    private String deptId;

    @Transient
    private String lastFileId;
}
