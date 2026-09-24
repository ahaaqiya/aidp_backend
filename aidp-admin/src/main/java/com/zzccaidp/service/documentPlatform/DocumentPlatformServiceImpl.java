package com.zzccaidp.service.documentPlatform;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3Object;
import com.zzccaidp.common.DateUtil;
import com.zzccaidp.dao.ai.SysFileInfoDO;
import com.zzccaidp.dao.knowledgebase.DocChangeRecordDO;
import com.zzccaidp.dao.knowledgebase.DocumentDO;
import com.zzccaidp.enums.DocPreviewSourceEnum;
import com.zzccaidp.enums.ErrCodeEnum;
import com.zzccaidp.exception.BusinessException;
import com.zzccaidp.integration.documentPlatform.DocPlatformIntegrationService;
import com.zzccaidp.integration.documentPlatform.response.DocPlatformPreviewUrlResponse;
import com.zzccaidp.mapper.ai.SysFileInfoMapper;
import com.zzccaidp.mapper.knowledgebase.DocumentMapper;
import com.zzccaidp.service.documentPlatform.dto.FileInfoDTO;
import com.zzccaidp.util.ResUtils;
import com.zzccaidp.vo.documentPlatform.GetPreviewUrlResponse;
import com.zzccaidp.vo.documentPlatform.PreviewCallbackResponse;
import com.zzccaidp.vo.documentPlatform.PreviewFile;
import com.zzccaidp.vo.documentPlatform.PreviewUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import tk.mybatis.mapper.entity.Example;

import javax.activation.MimetypesFileTypeMap;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.List;
import java.util.Objects;


/**
 * @author zhangtiantian
 * @date 2026/2/9
 */
@Service
@Slf4j
public class DocumentPlatformServiceImpl implements DocumentPlatformService {
    private static final String DOCUMENT_PLATFORM_PREVIEW_USER_PERMISSION_DEFAULT = "read";

    @Value("${documentPlatform.forDownload.url}")
    private String documentPlatformDownloadUrl;

    @Value("${bades.file.aws-s3.bucketName}")
    private String bucketName;


    @Autowired
    private AmazonS3 amazonS3;

    @Autowired
    private DocumentMapper documentMapper;

    @Autowired
    private DocPlatformIntegrationService docPlatformIntegrationService;

    @Autowired
    private SysFileInfoMapper sysFileInfoMapper;


    @Override
    public PreviewCallbackResponse documentPreviewCallBack(String fileId, String previewSource) {

        // 获取文件信息 根据不同的类型
        FileInfoDTO fileInfo = this.getFileInfo(fileId, previewSource);

        // 返回
        return this.buildPreviewCallbackResponse(fileInfo, previewSource);
    }


    @Override
    public void downloadFile(String fileId, String previewSource, HttpServletResponse response) {
        // 获取文件
        FileInfoDTO fileInfo = this.getFileInfo(fileId, previewSource);

        // 拉取桶文件
        S3Object object = amazonS3.getObject(fileInfo.getBucketName(), fileInfo.getPath());

        // 将输入流复制到响应输出流
        this.streamToResponse(object.getObjectContent(), response);

        // 设置response
        this.buildDownloadResponse(response, fileInfo, object.getObjectMetadata().getContentLength());
    }


    /**
     * 构建回调响应体
     *
     * @param fileInfo 文件信息
     * @return PreviewCallbackResponse
     */
    private PreviewCallbackResponse buildPreviewCallbackResponse(FileInfoDTO fileInfo, String previewSource) {
        PreviewCallbackResponse previewCallbackResponse = new PreviewCallbackResponse();

        // 文件信息
        PreviewFile previewFile = new PreviewFile();
        previewFile.setId(fileInfo.getFileId());
        previewFile.setName(fileInfo.getFileName());
        previewFile.setVersion(1);
        previewFile.setSize(Integer.valueOf(fileInfo.getFileSize()));
        previewFile.setDownload_url(String.format(documentPlatformDownloadUrl, fileInfo.getFileId(), previewSource));
        previewFile.setCreator("admin");
        previewFile.setCreate_time((int)fileInfo.getCreateTime().toInstant().getEpochSecond());
        previewFile.setModifier("admin");
        previewFile.setModify_time(previewFile.getCreate_time());
        previewFile.setPreview_pages(0);

        //用户权限
        PreviewUser previewUser = new PreviewUser();
        previewUser.setId("admin");
        previewUser.setName("admin");
        previewUser.setPermission(DOCUMENT_PLATFORM_PREVIEW_USER_PERMISSION_DEFAULT);

        previewCallbackResponse.setUser(previewUser);
        previewCallbackResponse.setFile(previewFile);
        return previewCallbackResponse;
    }


    /**
     * 构建下载文件响应
     *
     * @param response      http response
     * @param fileInfo      文件信息
     * @param contentLength 文件长度
     */
    private void buildDownloadResponse(HttpServletResponse response, FileInfoDTO fileInfo, Long contentLength) {
        try {
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Content-Disposition", "attachment;filename=\"" + URLEncoder.encode(fileInfo.getFileName(), "UTF-8") + "\"");
            response.addHeader("Content-Length", String.valueOf(contentLength));
            response.setContentType(new MimetypesFileTypeMap().getContentType(fileInfo.getFileName()));
        } catch (Exception e) {
            log.error("文件下载设置响应失败", e);
            throw new BusinessException(ErrCodeEnum.M7104);
        }

    }

    /**
     * 流转换
     *
     * @param inputStream input流
     * @param response    响应
     */
    private void streamToResponse(InputStream inputStream, HttpServletResponse response) {
        try (InputStream in = inputStream) {
            StreamUtils.copy(in, response.getOutputStream());
        } catch (IOException e) {
            log.error("inputStream写入response失败");
            throw new BusinessException(ErrCodeEnum.M7104);
        }
    }

    @Override
    public GetPreviewUrlResponse getPreviewUrl(String fileId, String previewSource) {

        // http请求文档中台
        FileInfoDTO fileInfo = this.getFileInfo(fileId, previewSource);
        DocPlatformPreviewUrlResponse response = docPlatformIntegrationService.getPreviewUrl(fileId, fileInfo.getFileName(), DocPreviewSourceEnum.getEnumByVal(previewSource));

        return ResUtils.success(new GetPreviewUrlResponse(response.getData().getLink()));
    }



    /**
     * 获取文件信息
     *
     * @param fileId 文件Id
     * @return SysFileInfoDO
     */
    private FileInfoDTO getFileInfo(String fileId, String previewSource) {
        DocPreviewSourceEnum docPreviewSourceEnum = DocPreviewSourceEnum.getEnumByVal(previewSource);
        switch (docPreviewSourceEnum) {
            case KNOW:
                return this.getKnowFileInfo(fileId);
            case SYSTEM:
                return this.getSystemFileInfo(fileId);
            default:
                log.error("文件类型不存在，fileId：{}", fileId);
                throw new BusinessException(ErrCodeEnum.M7103);
        }
    }

    private FileInfoDTO getSystemFileInfo(String fileId) {
        Example example = new Example(DocumentDO.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("vectorId", fileId);
        SysFileInfoDO sysFileInfoDO = sysFileInfoMapper.selectByPrimaryKey(fileId);
        if (Objects.isNull(sysFileInfoDO)) {
            log.error("文件不存在，fileId:{}", fileId);
            throw new BusinessException(ErrCodeEnum.M7102);
        }
        FileInfoDTO fileInfoDTO = new FileInfoDTO();
        fileInfoDTO.setFileId(sysFileInfoDO.getFileId());
        fileInfoDTO.setFileName(sysFileInfoDO.getFileName());
        fileInfoDTO.setCreateTime(DateUtil.getDate(sysFileInfoDO.getCreateTime()));
        fileInfoDTO.setCreateUser(sysFileInfoDO.getCreateUser());
        fileInfoDTO.setFileSize(sysFileInfoDO.getFileSize());
        fileInfoDTO.setBucketName(bucketName);
        fileInfoDTO.setPath(sysFileInfoDO.getFilePath());
        return fileInfoDTO;
    }

    private FileInfoDTO getKnowFileInfo(String docId) {
        Example example = new Example(DocumentDO.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("vectorId", docId);
        List<DocumentDO> documentDOList = documentMapper.selectByExample(example);
        if (CollectionUtils.isNotEmpty(documentDOList)) {
            DocumentDO documentDO = documentDOList.get(0);
            FileInfoDTO fileInfoDTO = new FileInfoDTO();
            fileInfoDTO.setFileId(documentDO.getVectorId());
            fileInfoDTO.setFileName(documentDO.getName());
            fileInfoDTO.setCreateTime(documentDO.getCreateTime());
            fileInfoDTO.setCreateUser(documentDO.getCreator());
            fileInfoDTO.setFileSize(documentDO.getFileSize());
            fileInfoDTO.setBucketName(documentDO.getBucketName());
            fileInfoDTO.setPath(documentDO.getBucketPath());
            return fileInfoDTO;
        }
        log.error("文件不存在，fileId:{}", docId);
        throw new BusinessException(ErrCodeEnum.M7102);
    }

}

