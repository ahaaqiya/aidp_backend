package com.zzccaidp.integration.documentPlatform;

import com.alibaba.csp.sentinel.util.StringUtil;
import com.zzccaidp.common.FileTypeDetector;
import com.zzccaidp.constants.DateConstant;
import com.zzccaidp.dao.system.SystemParamsDO;
import com.zzccaidp.enums.DocPreviewSourceEnum;
import com.zzccaidp.enums.ErrCodeEnum;
import com.zzccaidp.exception.BusinessException;
import com.zzccaidp.exception.CryptoException;
import com.zzccaidp.integration.documentPlatform.response.DocPlatformPreviewUrlResponse;
import com.zzccaidp.mapper.system.SystemParamsMapper;
import com.zzccaidp.util.HMacUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * @author zhangtiantian
 * @date 2026/5/9
 */
@Service
@Slf4j
public class DocPlatformIntegrationServiceImpl implements DocPlatformIntegrationService {

    private static final String DOCUMENT_PLATFORM_PREVIEW_TYPE = "DOCUMENT_PLATFORM_PREVIEW_TYPE";

    private static final String DOCUMENT_PLATFORM_PREVIEW_TYPE_DEFAULT = "ordinary";

    private static final String DOCUMENT_PLATFORM_CHL_DEFAULT = "zzccAIDP";

    private static final String DOCUMENT_PLATFORM_CHL = "DOCUMENT_PLATFORM_CHL";

    @Value("${documentPlatform.accessKey}")
    private String accessKey;

    @Value("${documentPlatform.secretKey}")
    private String secretKey;

    @Value("${documentPlatform.endpoint}")
    private String documentPlatformEndpoint;

    @Value("${documentPlatform.getPreviewUrl.uri}")
    private String documentPlatformGetPreviewUri;


    @Autowired
    private SystemParamsMapper systemParamsMapper;


    @Override
    public DocPlatformPreviewUrlResponse getPreviewUrl(String fileId, String fileName, DocPreviewSourceEnum source) {
        // http请求文档中台
        return this.httpGetPreviewUrl(fileId, fileName, source);
    }


    /**
     * http请求文档中台获取预览链接
     *
     * @param fileId 文件Id
     * @return DocPlatformPreviewUrlDTO
     */
    private DocPlatformPreviewUrlResponse httpGetPreviewUrl(String fileId, String fileName, DocPreviewSourceEnum source) {
        // 创建 RestTemplate 实例
        RestTemplate restTemplate = new RestTemplate();

        // 创建 HttpHeaders 对象
        HttpHeaders headers = new HttpHeaders();

        // 设置请求 URL
        String param = "?file_id=" + fileId +
                "&type=" +
                this.getDocTypeForView(fileName) +
                "&preview_mode=" +
                this.getDocPreviewType() +
                "&_w_third_providerSys=" +
                this.getChl() +
                "&_w_third_previewSource=" +
                source.getValue()
                ;
        String uri = documentPlatformGetPreviewUri.replace("{$file_id}", fileId) + param;
        String apiUrl = documentPlatformEndpoint + uri;

        // 设置请求头
        String contentType = "application/json";
        String wpsDocDate = this.getWpsDocDate();
        headers.set("Content-Type", contentType);
        headers.set("Wps-Docs-Date", wpsDocDate);
        headers.set("Wps-Docs-Authorization", this.getWpsDocsAuthorization(contentType, wpsDocDate, uri));

        // 构建请求实体
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
        ResponseEntity<DocPlatformPreviewUrlResponse> response;
        try {
            response = restTemplate.exchange(apiUrl, HttpMethod.GET, requestEntity, DocPlatformPreviewUrlResponse.class);
        } catch (Exception e) {
            log.error("获取文档中台预览链接失败", e);
            throw new BusinessException(ErrCodeEnum.M7101);
        }
        // 请求
        if (response.getBody() == null || !response.getStatusCode().is2xxSuccessful()) {
            log.error("获取文档中台预览链接失败:{}", response);
            throw new BusinessException(ErrCodeEnum.M7101);
        }
        return response.getBody();
    }



    /**
     * 根据文件名称获取文件类型
     *
     * @param fileName 文件名称
     * @return 类型
     */
    private String getDocTypeForView(String fileName) {
        if (StringUtil.isBlank(fileName)) {
            return StringUtil.EMPTY;
        }

        // 获取文件后缀
        String fileExtension = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();

        // 查找文件类型
        return FileTypeDetector.FILE_TYPE_MAP.getOrDefault(fileExtension, StringUtil.EMPTY);
    }

    /**
     * 获取doc认证参数
     *
     * @return 认证串
     */
    private String getWpsDocsAuthorization(String contentType, String date, String uri) {
        //hmac-sha256(secret_key, Ver + HttpMethod + URI + Content-Type + Wps-Date + sha256(HttpBody))
        try {
            String signatureBuilder = "WPS-4GET" +
                    uri.replace("/open", "") +
                    contentType +
                    date +
                    "";
            return String.format("WPS-4 %s:%s", accessKey, HMacUtils.HMACSHA256(signatureBuilder, secretKey));
        } catch (Exception e) {
            throw new CryptoException(ErrCodeEnum.M9996);
        }
    }

    /**
     * 获取签名日期
     *
     * @return 当前时间
     */
    private String getWpsDocDate() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DateConstant.HTTP_DATE_TIME_FORMAT, Locale.US);
        return formatter.format(ZonedDateTime.now(ZoneOffset.UTC));
    }


    /**
     * 获取预览类型
     *
     * @return 预览类型， 默认ordinary
     */
    private String getDocPreviewType() {
        SystemParamsDO sysParamsDO = systemParamsMapper.selectByParamKey(DOCUMENT_PLATFORM_PREVIEW_TYPE);
        if (sysParamsDO == null || StringUtils.isBlank(sysParamsDO.getParamsValue())) {
            return DOCUMENT_PLATFORM_PREVIEW_TYPE_DEFAULT;
        }
        return sysParamsDO.getParamsValue();
    }


    /**
     * 获取调用文档中台渠道
     *
     * @return 渠道 默认zzccIMP
     */
    private String getChl() {
        SystemParamsDO sysParamsDO = systemParamsMapper.selectByParamKey(DOCUMENT_PLATFORM_CHL);
        if (sysParamsDO == null || StringUtils.isBlank(sysParamsDO.getParamsValue())) {
            return DOCUMENT_PLATFORM_CHL_DEFAULT;
        }
        return sysParamsDO.getParamsValue();
    }
}
