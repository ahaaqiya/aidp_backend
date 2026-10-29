package com.zzccaidp.common;

import cn.hutool.core.io.IoUtil;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.zzccaidp.dao.knowledgebase.DocumentDO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @Description:
 * @Author: WB233500
 * @Createtime: 09:31
 * @Version: 1.0
 */
@Component
@Slf4j
public class AmazonS3Util {
    @Autowired
    private AmazonS3 amazonS3;

    //单次批量删除最大限制
    int MAX_BATCH = 1000;

    public void deleteFile(String fastDfsFileId, String backName) {
        amazonS3.deleteObject(backName, fastDfsFileId);
        log.info("删除文档成功，文档ID：{}", fastDfsFileId);
    }

    /**
     * 清理 RagFlow 数据集的独立桶（整桶清空后删除桶本身）。
     *
     * <p>背景：RagFlow 0.27.x 删除 dataset 时仅清理向量与自身 DB 记录，不删物理文件
     * （源码 internal/service/dataset/crud.go#deleteDataset 无任何 storage 删除调用；
     * 单文档删除链路 document_crud.go#L533 有 removeObjectBestEffort，孤儿仅产生于整库删除）。
     * 本部署为独立桶形态（RagFlow storage 未配置统一桶，minio.go#resolveBucketAndPath
     * bucket 参数即真实桶名）：每个 dataset 独占「桶名=dataset_id」的桶，
     * 桶内含文档文件与解析/chunk 图片，整桶清删即彻底回收。</p>
     *
     * <p>实现：S3 协议删桶前必须先清空对象——分页列出全部对象（1000/页）逐批删除，
     * 全部清空后调用 deleteBucket 删除桶本身。</p>
     *
     * @param datasetId 要清理的 RagFlow 数据集ID（即桶名）
     * @return 删除的对象总数
     */
    public int purgeRagFlowDatasetFiles(String datasetId) {
        ListObjectsV2Request listRequest = new ListObjectsV2Request().withBucketName(datasetId);
        ListObjectsV2Result listResult;
        int totalDeleted = 0;
        do {
            listResult = amazonS3.listObjectsV2(listRequest);
            List<String> keys = listResult.getObjectSummaries().stream()
                    .map(S3ObjectSummary::getKey)
                    .collect(Collectors.toList());
            if (!keys.isEmpty()) {
                batchDeleteFile(keys, datasetId);
                totalDeleted += keys.size();
            }
            listRequest.setContinuationToken(listResult.getNextContinuationToken());
        } while (listResult.isTruncated());
        amazonS3.deleteBucket(datasetId);
        log.info("清空并删除 ragflow 数据集独立桶成功，桶名：{}，删除对象数：{}", datasetId, totalDeleted);
        return totalDeleted;
    }

    public void batchDeleteFile(List<String> fastDfsFileIdList, String backName) {
        int size = fastDfsFileIdList.size();
        for(int start = 0; start<size ; start += MAX_BATCH){
            int end = Math.min(start + MAX_BATCH,size);
            List<String> subList = fastDfsFileIdList.subList(start,end);

            List<DeleteObjectsRequest.KeyVersion> keyVersions = new ArrayList<>();
            for(String key:subList){
                keyVersions.add(new DeleteObjectsRequest.KeyVersion(key));
            }
            DeleteObjectsRequest deleteObjectsRequest = new DeleteObjectsRequest(backName);
            deleteObjectsRequest.setKeys(keyVersions);
            deleteObjectsRequest.setQuiet(true);
            amazonS3.deleteObjects(deleteObjectsRequest);
        }
        log.info("批量删除文档成功，文档ID：{}", fastDfsFileIdList);
    }
    public String uploadFile(byte[] fileContent, String suffix, String backName) {
        String awsFilePath = UUID.randomUUID().toString().replaceAll("-", "") + "." + suffix;
        InputStream inputStream = new ByteArrayInputStream(fileContent);
        //ObjectMetadata里面
        ObjectMetadata metadata = new ObjectMetadata();
        //使用流上传的话，不设置文件长度会有WARN警告日志
        metadata.setContentLength(fileContent.length);
        amazonS3.putObject(backName, awsFilePath, inputStream, metadata);
        log.info("上传文档成，文档ID：{}", awsFilePath);
        return awsFilePath;
    }

    public byte[] downloadFile(String awsFilePath, String backName) {
        S3Object object = null;
        //缓冲区数组
        byte[] bytes = null;
        try {
            //获取此文件在桶内的相关内容
            object = amazonS3.getObject(backName, awsFilePath);
            //请务必注意，S3Object使用完必须关闭！！！此部分try-catch-finally代码请根据自己的业务逻辑自行调整，但请务必记得关闭S3Object
            S3ObjectInputStream objectInputStream = object.getObjectContent();
            return IoUtil.readBytes(objectInputStream);
        } catch (Exception e) {
            log.error("下载xsky文件失败,文档id：{}", awsFilePath, e);
            if (object != null) {
                try {
                    object.close();
                } catch (IOException ioException) {
                    log.error("关闭S3Object错误", ioException);
                }
            }
        } finally {
            if (object != null) {
                try {
                    object.close();
                } catch (IOException ioException) {
                    log.error("关闭S3Object错误", ioException);
                }
            }
        }
        return bytes;
    }

    /**
     * 单文件上传
     *
     * @param inputStream 文件输入流
     * @param objectKey 文件唯一标识key
     * @return
     */
    public static PutObjectResult uploadFile(AmazonS3 amazonS3, String bucketName, InputStream inputStream, String objectKey, long fileLength) {
        ObjectMetadata meta = new ObjectMetadata();
        meta.setContentLength(fileLength);
        return amazonS3.putObject(bucketName, objectKey, inputStream, meta);
    }

    /**
     * 从aws中获取对象（不限于文件）并输出为byte数组
     *
     * @param objectKey
     * @return
     */
    public static byte[] getObjectByAws(AmazonS3 amazonS3, String bucketName, String objectKey) throws IOException {
        GetObjectRequest request = new GetObjectRequest(bucketName, objectKey);
        return toByteArray(amazonS3.getObject(request).getObjectContent());
    }

    public static S3ObjectInputStream getInputStreamByAws(AmazonS3 amazonS3, String bucketName, String objectKey) throws IOException {
        GetObjectRequest request = new GetObjectRequest(bucketName, objectKey);
        return amazonS3.getObject(request).getObjectContent();
    }

    /**
     * inputStream转为byte[]
     *
     * @param input
     * @return
     * @throws IOException
     */
    public static byte[] toByteArray(InputStream input) throws IOException {

        try(ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[4096];
            int n = 0;
            while(-1 != (n = input.read(buffer))){
                outputStream.write(buffer,0, n);
            }
            return outputStream.toByteArray();
        }
    }
}
