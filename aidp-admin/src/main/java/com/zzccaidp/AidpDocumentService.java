package com.zzccaidp;

import com.zzccaidp.in.DocumentAddIn;
import com.zzccaidp.out.DocumentAddOut;

import java.util.List;

/**
 * AIDP 文档服务对外接口（本地接口，原属 zzccimp-aid-api 模块）
 */
public interface AidpDocumentService {

    /**
     * 文档推送更新（加入/更新/删除 RAG 知识库）
     */
    DocumentAddOut addDocument(List<DocumentAddIn> documentAddInList);

    /**
     * 文档推送更新（异步向量化版，供 HTTP 开放接口使用）：
     * 同步阶段仅做「校验 + S3 转存 + 变更记录 + 落库」后立即返回，
     * RagFlow 向量化由后台单线程池逐条补做；delete 操作仍保持全同步。
     * 返回结果中的 successSum/failSum 仅反映落库受理结果，不代表向量化完成。
     */
    DocumentAddOut addDocumentAsync(List<DocumentAddIn> documentAddInList);
}
