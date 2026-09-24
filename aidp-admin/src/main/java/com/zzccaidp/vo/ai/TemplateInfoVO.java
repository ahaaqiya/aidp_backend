package com.zzccaidp.vo.ai;

import com.zzccaidp.vo.PageRequest;
import lombok.Data;
import org.springframework.data.annotation.Id;

import java.io.Serializable;

@Data
public class TemplateInfoVO extends PageRequest implements Serializable {

        /**
         * 模板ID，主键
         */
        @Id
        private String id;

        /**
         * 规则编号
         */
        private String ruleId;

        /**
         * 模板名称，模板的唯一标识
         */
        private String templateName;

        /**
         * 模板类型，如“提示词模板”、“JSON模板”等
         */
        private String templateType;

        /**
         * 模板内容，存储模板的具体内容
         */
        private String templateContent;
        /**
         * 模板描述
         */
        private String templateDesc;

        /**
         * 模板状态（0-未启用，1-启用）
         */
        private String status;

        private String lastFileId;

}
