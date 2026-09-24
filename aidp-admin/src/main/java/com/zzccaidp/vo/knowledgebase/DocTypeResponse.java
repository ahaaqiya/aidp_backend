package com.zzccaidp.vo.knowledgebase;

import com.zzccaidp.vo.ResHeader;

import java.util.List;

public class DocTypeResponse extends ResHeader {

    private DocTypeOut data;
    private List<DocTypeOut> dataList;

    public static <T> DocTypeResponse success() {
        DocTypeResponse response = new DocTypeResponse();
        response.setSuccessCode();
        return response;
    }

    public static <T> DocTypeResponse success(DocTypeOut data) {
        DocTypeResponse response = new DocTypeResponse();
        response.setSuccessCode();
        response.setData(data);
        return response;
    }

    public static <T> DocTypeResponse success(List<DocTypeOut> dataList) {
        DocTypeResponse response = new DocTypeResponse();
        response.setSuccessCode();
        response.setDataList(dataList);
        return response;
    }

    public static <T> DocTypeResponse success(String msg) {
        DocTypeResponse response = new DocTypeResponse();
        response.setSuccessCode();
        response.setResultmsg(msg);
        return response;
    }

    public static <T> DocTypeResponse success(String msg, DocTypeOut data) {
        DocTypeResponse response = new DocTypeResponse();
        response.setSuccessCode();
        response.setResultmsg(msg);
        response.setData(data);
        return response;
    }

    public static <T> DocTypeResponse success(String msg, List<DocTypeOut> dataList) {
        DocTypeResponse response = new DocTypeResponse();
        response.setSuccessCode();
        response.setResultmsg(msg);
        response.setDataList(dataList);
        return response;
    }

    public static <T> DocTypeResponse failed(String msg) {
        DocTypeResponse response = new DocTypeResponse();
        response.setErrorCode();
        response.setResultmsg(msg);
        return response;
    }

    public DocTypeOut getData() {
        return data;
    }

    public void setData(DocTypeOut data) {
        this.data = data;
    }

    public List<DocTypeOut> getDataList() {
        return dataList;
    }

    public void setDataList(List<DocTypeOut> dataList) {
        this.dataList = dataList;
    }
}