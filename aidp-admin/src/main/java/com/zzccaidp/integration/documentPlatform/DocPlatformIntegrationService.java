package com.zzccaidp.integration.documentPlatform;

import com.zzccaidp.enums.DocPreviewSourceEnum;
import com.zzccaidp.integration.documentPlatform.response.DocPlatformPreviewUrlResponse;

/**
 * @author zhangtiantian
 * @date 2026/5/9
 */
public interface DocPlatformIntegrationService {

    /**
     * 获取文件预览地址
     * @param fileId 文件Id
     * @return GetPreviewUrlResponse
     */
    DocPlatformPreviewUrlResponse getPreviewUrl(String fileId, String fileName, DocPreviewSourceEnum source);
}
