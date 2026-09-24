package com.zzccaidp.integration.ragflow.request;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * @author zhangtiantian
 * @date 2026/4/2
 */
@Data
@AllArgsConstructor
public class DeleteDocumentRequest {

    private List<String> ids;
}
