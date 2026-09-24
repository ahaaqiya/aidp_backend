package com.zzccaidp.service.documentPlatform;

import com.zzccaidp.vo.documentPlatform.GetPreviewUrlResponse;
import com.zzccaidp.vo.documentPlatform.PreviewCallbackResponse;

import javax.servlet.http.HttpServletResponse;

/**
 * @author zhangtiantian
 * @date 2026/2/9
 */
public interface DocumentPlatformService {
    /**
     * 文件预览回调
     * @param fileId 文件Id
     * @return PreviewCallbackResponse
     */
    PreviewCallbackResponse documentPreviewCallBack(String fileId, String previewSource);

    /**
     *
     * @param fileId 文件Id
     * @param httpServletResponse HttpServletResponse
     */
    void downloadFile(String fileId, String previewSource, HttpServletResponse httpServletResponse);

    /**
     * 获取文件预览地址
     * @param fileId 文件Id
     * @return GetPreviewUrlResponse
     */
    GetPreviewUrlResponse getPreviewUrl(String fileId, String previewSource);
}
