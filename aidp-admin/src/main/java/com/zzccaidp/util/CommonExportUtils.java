package com.zzccaidp.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

import javax.activation.MimetypesFileTypeMap;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


@Slf4j
public class CommonExportUtils {


    public static void defaultDealResponse(HttpServletResponse resp,String fileName,String length) throws UnsupportedEncodingException {
        resp.setCharacterEncoding("UTF-8");
        resp.setHeader("Content-Disposition", "attachment;filename=\"" + URLEncoder.encode(fileName, "UTF-8") + "\"");
        resp.addHeader("Content-Length", length);
        resp.setContentType(new MimetypesFileTypeMap().getContentType(fileName));
    }

    /**
     * 通用文件下载方法
     * @param fileName 文件名
     * @param bytes 文件内容
     * @param resp 响应
     * @throws IOException
     */
    public static void defaultDownLoadFile(String fileName, byte[] bytes, HttpServletResponse resp) throws IOException {
        String length=String.valueOf(bytes.length);
        defaultDealResponse(resp,fileName,length);
        try (ServletOutputStream output = resp.getOutputStream()) {
            IOUtils.write(bytes, output);
            output.flush();
        } catch (IOException e) {
            String errMsg = "文件下载报错！";
            log.error(errMsg,e);
        }
    }


    /**
     * 指定路径文件下载
     * @param filePath 文件地址
     * @param resp 前端response
     */
    public static void downloadFile(String filePath,String fileName ,HttpServletResponse resp) throws IOException {
        File file = new File(filePath);
        resp.reset();
        String length=String.valueOf(file.length());
        defaultDealResponse(resp,fileName,length);
        try(InputStream inputStream= new BufferedInputStream(new FileInputStream(file));
            ServletOutputStream output = resp.getOutputStream()){
            byte[] buffer = new byte[inputStream.available()];
            inputStream.read(buffer);
            IOUtils.write(buffer, output);
            output.flush();
        }catch (Exception e){
            String errMsg = "文件下载报错！";
            log.error(errMsg,e);
        }
    }

    /**
     * 是否是指定文件类型中
     * @param fileName 文件名
     * @param supportFiles 文件类型
     * @return 结果
     */
    public static  boolean isShowSupport(String fileName,String []supportFiles){
        String[] nameComponent=fileName.split("\\.");
        String fileType=nameComponent[nameComponent.length-1];
        List<String> supportFileList=new ArrayList<>(Arrays.asList(supportFiles));
        return supportFileList.contains(fileType);
    }


}

