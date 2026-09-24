package com.zzccaidp.out;

import lombok.Data;

import java.io.Serializable;

/**
 * 文档推送出参（原属 zzccimp-aid-api 模块）
 */
@Data
public class DocumentAddOut implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 成功条数 */
    private String successSum;

    /** 失败条数 */
    private String failSum;

    /** 返回码 */
    private String resultcode;

    /** 返回消息 */
    private String resultmsg;

    /** 失败的文档ID列表（逗号分隔） */
    private String docIds;
}
