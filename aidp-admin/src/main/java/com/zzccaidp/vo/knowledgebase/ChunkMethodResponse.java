package com.zzccaidp.vo.knowledgebase;

import com.zzccaidp.vo.ResHeader;

import java.util.List;
import java.util.Map;

/**
 * 切片方式字典响应（GET /doc_type/chunk_methods），
 * 结构与 DocTypeResponse 保持一致：resultcode/resultmsg + dataList（[{value,label}]）。
 */
public class ChunkMethodResponse extends ResHeader {

    private List<Map<String, String>> dataList;

    public static ChunkMethodResponse success(List<Map<String, String>> dataList) {
        ChunkMethodResponse response = new ChunkMethodResponse();
        response.setSuccessCode();
        response.setDataList(dataList);
        return response;
    }

    public static ChunkMethodResponse failed(String msg) {
        ChunkMethodResponse response = new ChunkMethodResponse();
        response.setErrorCode();
        response.setResultmsg(msg);
        return response;
    }

    public List<Map<String, String>> getDataList() {
        return dataList;
    }

    public void setDataList(List<Map<String, String>> dataList) {
        this.dataList = dataList;
    }
}
