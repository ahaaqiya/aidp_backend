package com.zzccaidp.vo.knowledgebase;

import lombok.Data;

@Data
public class RagFlowDocumentDetail {
    private String id;
    private String name;
    private String location;
    private long size;
    private String type;
    private String chunkMethod;
    private String run;
}
