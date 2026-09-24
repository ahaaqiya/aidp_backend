package com.zzccaidp.vo.knowledgebase;

import lombok.Data;

import java.util.List;

@Data
public class RagFlowDocumentListDetail {
    private List<RagFlowDocumentDetail> docs;
    private int total;
}
