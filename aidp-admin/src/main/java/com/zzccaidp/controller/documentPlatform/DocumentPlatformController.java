package com.zzccaidp.controller.documentPlatform;

import com.zzccaidp.service.documentPlatform.DocumentPlatformService;
import com.zzccaidp.vo.documentPlatform.GetPreviewUrlResponse;
import com.zzccaidp.vo.documentPlatform.NotifyCallbackResponse;
import com.zzccaidp.vo.documentPlatform.PreviewCallbackResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

/**
 * @author zhangtiantian
 * @date 2026/2/9
 */
@RestController
@RequestMapping("/documentPlatform")
@Slf4j
public class DocumentPlatformController {

    @Autowired
    private DocumentPlatformService documentPlatformService;

    @GetMapping(value = "/wps/v1/3rd/file/info")
    public PreviewCallbackResponse documentPreviewCallBack(@RequestHeader("X-Weboffice-File-Id") String fileId, @RequestParam(name = "_w_third_previewSource") String previewSource) {
        return documentPlatformService.documentPreviewCallBack(fileId, previewSource);
    }

    @PostMapping(value = "/wps/v1/3rd/online")
    public NotifyCallbackResponse documentOnlineCallBack(@RequestHeader("X-Weboffice-File-Id") String fileId) {
        return new NotifyCallbackResponse(0, "OK");
    }

    @PostMapping(value = "/wps/v1/3rd/onnotify")
    public NotifyCallbackResponse documentNotifyCallBack(@RequestHeader("X-Weboffice-File-Id") String fileId) {
        return new NotifyCallbackResponse(0, "OK");
    }


    @GetMapping("/wps/downloadFile")
    public void downloadFile(@RequestParam String fileId, @RequestParam String previewSource, HttpServletResponse httpServletResponse) {
        documentPlatformService.downloadFile(fileId, previewSource, httpServletResponse);
    }


    @GetMapping("/getPreviewUrl")
    public GetPreviewUrlResponse getPreviewUrl(@RequestParam("fileId") String fileId, @RequestParam("previewSource") String previewSource){
        return documentPlatformService.getPreviewUrl(fileId, previewSource);
    }
}
