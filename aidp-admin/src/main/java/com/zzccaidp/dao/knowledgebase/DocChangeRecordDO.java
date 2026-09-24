package com.zzccaidp.dao.knowledgebase;

import lombok.Data;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author zhangtiantian
 * @date 2026/3/27
 */
@Table(name = "doc_change_record")
@Data
public class DocChangeRecordDO {

    @Id
    private String id;

    @Column(name = "vector_id")
    private String vectorId;

    @Column(name = "doc_id")
    private String docId;

    @Column(name = "doc_name")
    private String docName;

    @Column(name = "channel")
    private String channel;

    @Column(name = "metadata")
    private String metadata;

    @Column(name = "back_name")
    private String backName;

    @Column(name = "aws_file_path")
    private String awsFilePath;

    @Column(name = "operator_type")
    private String operatorType;
}
