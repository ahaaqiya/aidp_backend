package com.zzccaidp.integration.documentPlatform.response;

import com.zzccaidp.integration.documentPlatform.dto.DocPlatformPreviewUrlDataDTO;
import lombok.Data;

/**
 * @author zhangtiantian
 * @date 2026/5/9
 */
@Data
public class DocPlatformPreviewUrlResponse {

    private DocPlatformPreviewUrlDataDTO data;

    private String code;

    private String msg;

    private long request_time;

    private String request_id;

    private long response_time;
}
