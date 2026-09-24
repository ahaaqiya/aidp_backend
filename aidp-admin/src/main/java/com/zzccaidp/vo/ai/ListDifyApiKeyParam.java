package com.zzccaidp.vo.ai;

import lombok.Data;

/**
 * @author zhangtiantian
 * @date 2026/4/13
 */
@Data
public class ListDifyApiKeyParam {

    private String keyType;

    private String keyName;

    private String apiKey;

    private String difyAppType;
}
