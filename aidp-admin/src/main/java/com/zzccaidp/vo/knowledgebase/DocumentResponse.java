package com.zzccaidp.vo.knowledgebase;

import cn.hutool.db.PageResult;
import com.zzccaidp.vo.ResHeader;

import java.util.List;
import java.util.Map;

public class DocumentResponse extends ResHeader {

    private DocumentOut data;
    private List<DocumentOut> dataList;
    private PageResult<DocumentOut> pageData;
    private List<Map<String, Object>> chunks;
    private List<Map<String, Object>> logs;

    public static <T> DocumentResponse success() {
        DocumentResponse response = new DocumentResponse();
        response.setSuccessCode();
        return response;
    }

    public static <T> DocumentResponse success(DocumentOut data) {
        DocumentResponse response = new DocumentResponse();
        response.setSuccessCode();
        response.setData(data);
        return response;
    }

    public static <T> DocumentResponse success(List<DocumentOut> dataList) {
        DocumentResponse response = new DocumentResponse();
        response.setSuccessCode();
        response.setDataList(dataList);
        return response;
    }

    public static <T> DocumentResponse success(PageResult<DocumentOut> pageData) {
        DocumentResponse response = new DocumentResponse();
        response.setSuccessCode();
        response.setPageData(pageData);
        return response;
    }

    public static <T> DocumentResponse success(List<Map<String, Object>> data, boolean isChunks) {
        DocumentResponse response = new DocumentResponse();
        response.setSuccessCode();
        if (isChunks) {
            response.setChunks(data);
        } else {
            response.setLogs(data);
        }
        return response;
    }

    public static <T> DocumentResponse success(String msg) {
        DocumentResponse response = new DocumentResponse();
        response.setSuccessCode();
        response.setResultmsg(msg);
        return response;
    }

    public static <T> DocumentResponse success(String msg, DocumentOut data) {
        DocumentResponse response = new DocumentResponse();
        response.setSuccessCode();
        response.setResultmsg(msg);
        response.setData(data);
        return response;
    }

    public static <T> DocumentResponse success(String msg, List<DocumentOut> dataList) {
        DocumentResponse response = new DocumentResponse();
        response.setSuccessCode();
        response.setResultmsg(msg);
        response.setDataList(dataList);
        return response;
    }

    public static <T> DocumentResponse success(String msg, PageResult<DocumentOut> pageData) {
        DocumentResponse response = new DocumentResponse();
        response.setSuccessCode();
        response.setResultmsg(msg);
        response.setPageData(pageData);
        return response;
    }

    public static <T> DocumentResponse failed(String msg) {
        DocumentResponse response = new DocumentResponse();
        response.setErrorCode();
        response.setResultmsg(msg);
        return response;
    }

    public DocumentOut getData() {
        return data;
    }

    public void setData(DocumentOut data) {
        this.data = data;
    }

    public List<DocumentOut> getDataList() {
        return dataList;
    }

    public void setDataList(List<DocumentOut> dataList) {
        this.dataList = dataList;
    }

    public PageResult<DocumentOut> getPageData() {
        return pageData;
    }

    public void setPageData(PageResult<DocumentOut> pageData) {
        this.pageData = pageData;
    }

    public List<Map<String, Object>> getChunks() {
        return chunks;
    }

    public void setChunks(List<Map<String, Object>> chunks) {
        this.chunks = chunks;
    }

    public List<Map<String, Object>> getLogs() {
        return logs;
    }

    public void setLogs(List<Map<String, Object>> logs) {
        this.logs = logs;
    }
}