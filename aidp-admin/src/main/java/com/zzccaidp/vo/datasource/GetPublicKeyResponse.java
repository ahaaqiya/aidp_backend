package com.zzccaidp.vo.datasource;

import com.zzccaidp.vo.ResHeader;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author zhangtiantian
 * @date 2026/5/8
 */
@Data
@AllArgsConstructor
public class GetPublicKeyResponse extends ResHeader {
    private String pubKey;
}
