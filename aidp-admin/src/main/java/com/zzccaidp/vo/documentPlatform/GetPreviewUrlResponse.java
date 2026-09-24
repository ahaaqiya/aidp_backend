package com.zzccaidp.vo.documentPlatform;

import com.zzccaidp.vo.ResHeader;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * @author zhangtiantian
 * @date 2026/2/9
 */
@Data
@AllArgsConstructor
public class GetPreviewUrlResponse extends ResHeader implements Serializable {
    private static final long serialVersionUID = 7659591394579720502L;
    private String previewUrl;
}
