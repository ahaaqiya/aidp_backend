package com.zzccaidp.vo.documentPlatform;

import lombok.Data;

/**
 * @author zhangtiantian
 * @date 2026/2/10
 */
@Data
public class UserAcl {
    private Integer print;
    private Integer rename;
    private Integer history;
    private Integer export;
    private Integer copy;
}
