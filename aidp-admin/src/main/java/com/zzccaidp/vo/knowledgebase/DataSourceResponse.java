package com.zzccaidp.vo.knowledgebase;

import com.zzccaidp.vo.ResHeader;

import java.util.List;

public class DataSourceResponse extends ResHeader {

    private DataSourceOut data;
    private List<DataSourceOut> dataList;

    public static <T> DataSourceResponse success() {
        DataSourceResponse response = new DataSourceResponse();
        response.setSuccessCode();
        return response;
    }

    public static <T> DataSourceResponse success(DataSourceOut data) {
        DataSourceResponse response = new DataSourceResponse();
        response.setSuccessCode();
        response.setData(data);
        return response;
    }

    public static <T> DataSourceResponse success(List<DataSourceOut> dataList) {
        DataSourceResponse response = new DataSourceResponse();
        response.setSuccessCode();
        response.setDataList(dataList);
        return response;
    }

    public static <T> DataSourceResponse success(String msg) {
        DataSourceResponse response = new DataSourceResponse();
        response.setSuccessCode();
        response.setResultmsg(msg);
        return response;
    }

    public static <T> DataSourceResponse success(String msg, DataSourceOut data) {
        DataSourceResponse response = new DataSourceResponse();
        response.setSuccessCode();
        response.setResultmsg(msg);
        response.setData(data);
        return response;
    }

    public static <T> DataSourceResponse success(String msg, List<DataSourceOut> dataList) {
        DataSourceResponse response = new DataSourceResponse();
        response.setSuccessCode();
        response.setResultmsg(msg);
        response.setDataList(dataList);
        return response;
    }

    public static <T> DataSourceResponse failed(String msg) {
        DataSourceResponse response = new DataSourceResponse();
        response.setErrorCode();
        response.setResultmsg(msg);
        return response;
    }

    public DataSourceOut getData() {
        return data;
    }

    public void setData(DataSourceOut data) {
        this.data = data;
    }

    public List<DataSourceOut> getDataList() {
        return dataList;
    }

    public void setDataList(List<DataSourceOut> dataList) {
        this.dataList = dataList;
    }
}
