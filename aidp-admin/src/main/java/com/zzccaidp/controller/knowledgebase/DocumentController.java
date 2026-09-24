package com.zzccaidp.controller.knowledgebase;

import cn.hutool.core.bean.BeanUtil;
import com.zzccaidp.common.DateUtil;
import com.zzccaidp.dao.knowledgebase.DocumentDO;
import com.zzccaidp.service.knowledgebase.DocumentService;
import com.zzccaidp.vo.PageRequest;
import com.zzccaidp.vo.PageResponse;
import com.zzccaidp.vo.ResHeader;
import com.zzccaidp.vo.knowledgebase.DocumentIn;
import com.zzccaidp.vo.knowledgebase.DocumentOut;
import com.zzccaidp.vo.knowledgebase.DocumentResponse;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.zzccaidp.constants.DateConstant.DATE_PATTERN_DIGIT;

@RestController
@RequestMapping("/document")
@Slf4j
public class DocumentController {

    @Autowired
    private DocumentService documentService;

    @PostMapping("/list")
    public PageResponse<DocumentOut> list(@RequestBody PageRequest<DocumentIn> pageRequest) {
        return documentService.list(pageRequest);
    }

    @GetMapping("/{id}")
    public DocumentResponse getById(@PathVariable Long id) {
        DocumentDO doc = documentService.findById(id);
        if (doc == null) {
            return DocumentResponse.failed("文档不存在");
        }
        return DocumentResponse.success(convertToOut(doc));
    }

    @PostMapping("/doc-type/update")
    public DocumentResponse updateDocType(@RequestBody Map<String, Object> params) {
        Long id = ((Number) params.get("id")).longValue();
        String docType = (String) params.get("docType");
        String docTypeName = (String) params.get("docTypeName");
        DocumentDO doc = documentService.updateDocType(id, docType, docTypeName);
        if (doc == null) {
            return DocumentResponse.failed("文档不存在");
        }
        return DocumentResponse.success("更新成功", convertToOut(doc));
    }

    @PostMapping("/doc-type/updateList")
    public DocumentResponse updateDocTypeList(@RequestBody Map<String, Object> params) {
        List<Number> idList = ((List<Number>) params.get("id"));
        String docType = (String) params.get("docType");
        String docTypeName = (String) params.get("docTypeName");
        idList.forEach(id -> {
            try {
                DocumentDO doc = documentService.updateDocType(id.longValue(), docType, docTypeName);
            } catch (Exception e) {
                log.error("更新失败，文档id：{}，文档类型：{}，文档名称：{}", id, docType, docTypeName);
            }
        });
        return DocumentResponse.success("更新成功");
    }

    @PostMapping("/process")
    public DocumentResponse process(@RequestBody Map<String, Object> params) {
        Long id = ((Number) params.get("id")).longValue();
        DocumentDO doc = documentService.process(id);
        if (doc == null) {
            return DocumentResponse.failed("文档不存在");
        }
        if (doc.getTaskStatus() == "failed") {
            return DocumentResponse.failed("向量化任务启动失败");
        }
        return DocumentResponse.success("向量化任务已启动", convertToOut(doc));
    }

    @PostMapping("/processList")
    public DocumentResponse processList(@RequestBody Map<String, Object> params) {
        List<Number> idList = (List<Number>) params.get("id");
        idList.forEach(var -> {
            DocumentDO doc = documentService.process(var.longValue());
            if (doc == null) {
                log.error("文档不存在:{}", var);
            }
        });
        return DocumentResponse.success("向量化任务已启动");
    }

    @PostMapping("/retry")
    public DocumentResponse retry(@RequestBody Map<String, Object> params) {
        Long id = ((Number) params.get("id")).longValue();
        DocumentDO doc = documentService.retry(id);
        if (doc == null) {
            return DocumentResponse.failed("文档不存在");
        }
        return DocumentResponse.success("重试任务已启动", convertToOut(doc));
    }

    @PostMapping("/enable-vector")
    public DocumentResponse enableVector(@RequestBody Map<String, Object> params) {
        Long id = ((Number) params.get("id")).longValue();
        DocumentDO doc = documentService.enableVector(id);
        if (doc == null) {
            return DocumentResponse.failed("文档不存在");
        }
        return DocumentResponse.success("已开启向量化", convertToOut(doc));
    }

    @PostMapping("/disable-vector")
    public DocumentResponse disableVector(@RequestBody Map<String, Object> params) {
        Long id = ((Number) params.get("id")).longValue();
        DocumentDO doc = documentService.disableVector(id);
        if (doc == null) {
            return DocumentResponse.failed("文档不存在");
        }
        return DocumentResponse.success("已关闭向量化", convertToOut(doc));
    }

    @PostMapping("/clean-vector")
    public DocumentResponse cleanVector(@RequestBody Map<String, Object> params) {
        Long id = ((Number) params.get("id")).longValue();
        DocumentDO doc = documentService.cleanVector(id);
        if (doc == null) {
            return DocumentResponse.failed("文档不存在");
        }
        return DocumentResponse.success("向量数据已清理", convertToOut(doc));
    }

    @PostMapping("/delete")
    public DocumentResponse delete(@RequestBody Map<String, Object> params) {
        Long id = ((Number) params.get("id")).longValue();
        if (documentService.delete(id)) {
            return DocumentResponse.success("删除成功");
        }
        return DocumentResponse.failed("删除失败，文档不存在");
    }

    @PostMapping("/deleteList")
    public DocumentResponse deleteList(@RequestBody Map<String, Object> params) {
        List<Number> idList = (List<Number>) params.get("id");
        idList.forEach(var -> {
            try {
                if (documentService.delete(var.longValue())) {
                    log.info("删除成功，id：{}", var);
                }
            } catch (Exception e) {
                log.error("删除失败，id：{}", var);
            }
        });
        return DocumentResponse.success("删除成功");
    }

    @PostMapping("/batch/enable-vector")
    public DocumentResponse batchEnableVector(@RequestBody Map<String, Object> params) {
        @SuppressWarnings("unchecked")
        List<Long> ids = (List<Long>) params.get("ids");
        List<DocumentDO> result = documentService.batchEnableVector(ids);
        List<DocumentOut> output = result.stream().map(this::convertToOut).collect(Collectors.toList());
        return DocumentResponse.success("批量开启向量化成功", output);
    }

    @PostMapping("/batch/disable-vector")
    public DocumentResponse batchDisableVector(@RequestBody Map<String, Object> params) {
        @SuppressWarnings("unchecked")
        List<Long> ids = (List<Long>) params.get("ids");
        List<DocumentDO> result = documentService.batchDisableVector(ids);
        List<DocumentOut> output = result.stream().map(this::convertToOut).collect(Collectors.toList());
        return DocumentResponse.success("批量关闭向量化成功", output);
    }

    @PostMapping("/batch/process")
    public DocumentResponse batchReprocess(@RequestBody Map<String, Object> params) {
        @SuppressWarnings("unchecked")
        List<Long> ids = (List<Long>) params.get("ids");
        List<DocumentDO> result = documentService.batchReprocess(ids);
        List<DocumentOut> output = result.stream().map(this::convertToOut).collect(Collectors.toList());
        return DocumentResponse.success("批量向量化任务启动", output);
    }

    @PostMapping("/metadata/update")
    public DocumentResponse updateMetadata(@RequestBody Map<String, Object> params) {
        Long id = ((Number) params.get("id")).longValue();
        @SuppressWarnings("unchecked")
        Map<String, Object> metadata = (Map<String, Object>) params.get("metadata");
        DocumentDO doc = documentService.updateMetadata(id, metadata);
        if (doc == null) {
            return DocumentResponse.failed("文档不存在");
        }
        return DocumentResponse.success("元数据更新成功", convertToOut(doc));
    }

    @GetMapping("/{id}/chunks")
    public DocumentResponse getChunks(@PathVariable Long id) {
        DocumentDO doc = documentService.findById(id);
        if (doc == null) {
            return DocumentResponse.failed("文档不存在");
        }

        List<Map<String, Object>> chunks = new ArrayList<>();
        chunks.add(createChunk(1, "文档介绍和概述", "AI门户是一个集成了多种AI能力的平台，为用户提供智能对话、知识管理、应用开发等功能。本文档将详细介绍AI门户的各项功能特性和使用方法。\n\n主要功能包括：\n1. 智能对话系统\n2. 知识库管理\n3. 应用开发平台\n4. 数据分析工具\n\n通过这些功能，用户可以快速构建自己的AI应用，提高工作效率。"));
        chunks.add(createChunk(2, "智能对话系统功能", "智能对话系统支持多轮对话、上下文理解、知识检索等功能。用户可以通过自然语言与AI进行交互，获取所需信息或完成任务。\n\n系统特点：\n- 支持多种对话模式\n- 智能上下文理解\n- 实时知识检索\n- 多语言支持"));
        chunks.add(createChunk(3, "知识库管理功能", "知识库管理功能允许用户上传、组织、检索各类文档资料。支持多种文件格式，包括PDF、Word、Markdown等。\n\n核心功能：\n- 文档上传与管理\n- 智能分类与标签\n- 全文检索\n- 版本控制"));

        return DocumentResponse.success(chunks, true);
    }

    private Map<String, Object> createChunk(int chunkId, String summary, String content) {
        Map<String, Object> chunk = new HashMap<>();
        chunk.put("chunkId", chunkId);
        chunk.put("summary", summary);
        chunk.put("content", content);
        chunk.put("charCount", content.length());
        return chunk;
    }

    @GetMapping("/{id}/logs")
    public DocumentResponse getLogs(@PathVariable Long id) {
        DocumentDO doc = documentService.findById(id);
        if (doc == null) {
            return DocumentResponse.failed("文档不存在");
        }

        List<Map<String, Object>> logs = new ArrayList<>();
        logs.add(createLog("2026-04-14 10:01:23", "同步任务：检测到文档内容更新，MD5变更", "源文件MD5从 a1b2c3d4e5f6g7h8i9j0 变更为 a1b2c3d4e5f6g7h8i9j1", "warning"));
        logs.add(createLog("2026-04-14 10:02:15", "向量化任务：开始上传至RAGFlow...", "正在从XSky下载文件并上传到RAGFlow解析服务", "info"));
        logs.add(createLog("2026-04-14 10:02:48", "向量化任务：解析成功", "文档已成功解析为3个分片，向量数据已存储", "success"));
        logs.add(createLog("2026-04-14 10:03:00", "元数据更新：完成", "文档元数据已更新，可见范围已同步", "success"));

        return DocumentResponse.success(logs, false);
    }

    private Map<String, Object> createLog(String time, String title, String detail, String type) {
        Map<String, Object> log = new HashMap<>();
        log.put("time", time);
        log.put("title", title);
        log.put("detail", detail);
        log.put("type", type);
        return log;
    }

    @PostMapping("/upload")
    public DocumentResponse upload(
            @RequestParam("docType") String docType,
            @RequestParam("sourceId") Long sourceId,
            @RequestParam("files") MultipartFile[] files) {

        List<DocumentDO> docs = documentService.upload(docType, sourceId, files);
        List<DocumentOut> output = docs.stream().map(this::convertToOut).collect(Collectors.toList());
        return DocumentResponse.success("文档上传成功", output);
    }

    @PostMapping("/{id}/download")
    public void downloadDocument(@PathVariable Long id, HttpServletResponse response) {
        documentService.downloadFile(id, response);
    }

    private DocumentOut convertToOut(DocumentDO source) {
        DocumentOut out = new DocumentOut();
        out.setId(source.getId());
        out.setDocId(source.getDocId());
        out.setDataSourceId(source.getDataSourceId());
        out.setChannel(source.getChannel());
        out.setName(source.getName());
        out.setFileType(source.getFileType());
        out.setDocType(source.getDocType());
        out.setDocTypeName(source.getDocTypeName());
        out.setSourceName(source.getSourceName());
        out.setSourceStatus(source.getSourceStatus());
        out.setSwitchStatus(source.getSwitchStatus());
        out.setTaskStatus(source.getTaskStatus());
        out.setLastProcessTime(source.getLastProcessTime());
        out.setMd5(source.getMd5());
        out.setFileSize(source.getFileSize());
        out.setCreator(source.getCreator());
        out.setCreateTime(source.getCreateTime());
        out.setModifier(source.getModifier());
        out.setUpdateTime(source.getUpdateTime());
        out.setVisibility(source.getVisibility());
        out.setXskyPath(source.getXskyPath());
        out.setBucketName(source.getBucketName());
        out.setBucketPath(source.getBucketPath());
        out.setVectorEnabled(source.getVectorEnabled());
        out.setVectorId(source.getVectorId());
        out.setMetadata(source.getMetadata());
        out.setStatus(source.getStatus());
        // 正文/附件标识：附件时返回所属正文标题，供前端列表打标展示
        out.setParentDocName(source.getParentDocName());
        out.setIsAttachment(source.getIsAttachment());
        return out;
    }

    @GetMapping("/syncOaFile")
    public ResHeader syncOaFile(@RequestBody String date) {
        ResHeader resHeader = new ResHeader();
        documentService.syncOaDocVerctor(date);
        resHeader.setSuccessCode();
        return resHeader;
    }
}