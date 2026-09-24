package com.zzccaidp.controller.knowledgebase;

import com.zzccaidp.service.knowledgebase.PersonalDocumentService;
import com.zzccaidp.vo.PageResponse;
import com.zzccaidp.vo.knowledgebase.DocumentOut;
import com.zzccaidp.vo.knowledgebase.DocumentResponse;
import com.zzccaidp.vo.knowledgebase.PersonalSyncStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 个人知识库文档接口。
 * <p>
 * 前端契约：
 * - GET  /personalDocument/list       分页查询（pageNum/pageSize/keyword/channel）
 * - POST /personalDocument/upload     多文件上传（multipart/form-data，字段名 files）
 * - POST /personalDocument/delete     删除文档（body: { docIds: [业务文档ID] }）
 * - POST /personalDocument/process    触发向量化（body: { docIds: [业务文档ID] }）
 * - POST /personalDocument/sync       提交同步任务（body 可选：{ startTime: 重新同步起点 }，空=普通增量）
 * - GET  /personalDocument/syncStatus 查询同步状态
 * - GET  /personalDocument/lastSyncTime 查询上次同步水位时间（展示与「重新同步」起点默认值）
 */
@RestController
@RequestMapping("/personalDocument")
@Slf4j
public class PersonalDocumentController {

    @Autowired
    private PersonalDocumentService personalDocumentService;

    @GetMapping("/list")
    public PageResponse<DocumentOut> list(
            @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "channel", required = false) String channel) {
        return personalDocumentService.list(pageNum, pageSize, keyword, channel);
    }

    @PostMapping("/upload")
    public DocumentResponse upload(@RequestParam("files") MultipartFile[] files) {
        List<DocumentOut> result = personalDocumentService.upload(files);
        return DocumentResponse.success("上传成功", result);
    }

    /**
     * 删除个人知识库文档（物理删除：ragflow 向量 + XSKY 文件 + document 记录三处同步清理）
     *
     * @param params 请求体，{@code docIds} 为业务文档ID列表
     */
    @PostMapping("/delete")
    @SuppressWarnings("unchecked")
    public DocumentResponse delete(@RequestBody Map<String, Object> params) {
        Object docIds = params.get("docIds");
        personalDocumentService.delete(docIds instanceof List ? (List<String>) docIds : null);
        return DocumentResponse.success("删除成功");
    }

    /**
     * 触发个人知识库文档向量化（支持手动重跑，失败文档可重试）。
     *
     * @param params 请求体，{@code docIds} 为业务文档ID列表
     */
    @PostMapping("/process")
    @SuppressWarnings("unchecked")
    public DocumentResponse process(@RequestBody Map<String, Object> params) {
        Object docIds = params.get("docIds");
        List<DocumentOut> result = personalDocumentService.process(docIds instanceof List ? (List<String>) docIds : null);
        return DocumentResponse.success("向量化任务已启动", result);
    }

    /**
     * 提交个人知识库同步任务（异步执行）。
     * <p>
     * body 为空或 startTime 为空：普通增量同步（从上次同步水位起）；
     * body 携带 startTime（yyyy-MM-dd HH:mm:ss）：重新同步（从该时间点起重放，不推进水位），
     * 用于恢复 AIDP 侧被删除/缺失的文档。body 可选，兼容不带请求体的旧前端调用。
     *
     * @param params 请求体，可选；{@code startTime} 为重新同步起点时间
     */
    @PostMapping("/sync")
    @SuppressWarnings("unchecked")
    public DocumentResponse sync(@RequestBody(required = false) Map<String, Object> params) {
        Object startTime = params == null ? null : params.get("startTime");
        personalDocumentService.sync(startTime instanceof String ? (String) startTime : null);
        return DocumentResponse.success("同步任务已提交");
    }

    /**
     * 查询当前用户个人知识库的上次增量同步水位时间（来自 zsk 侧）。
     * 供前端展示「上次同步时间」并作为「重新同步」起点的默认值；从未同步过返回 null。
     */
    @GetMapping("/lastSyncTime")
    public DocumentResponse lastSyncTime() {
        return DocumentResponse.success(personalDocumentService.getLastSyncTime());
    }

    @GetMapping("/syncStatus")
    public PersonalSyncStatus syncStatus() {
        return personalDocumentService.getSyncStatus();
    }
}
