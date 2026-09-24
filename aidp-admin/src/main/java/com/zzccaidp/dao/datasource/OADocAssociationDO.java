package com.zzccaidp.dao.datasource;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Table;
import java.util.Date;

/**
 * @Description: OA文档与文档目录关联关系
 * @Author: WB233500
 * @Createtime: 08:55
 * @Version: 1.0
 */
@Table(name = "oa_doc_association")
@Data
public class OADocAssociationDO {
    @Column(name = "doc_id")
    private String docId;
    @Column(name = "doc_index_id")
    private String docIndexId;
    @Column(name = "create_time")
    private Date createTime;

    public OADocAssociationDO(String docId, String docIndexId, Date createTime) {
        this.docId = docId;
        this.docIndexId = docIndexId;
        this.createTime = createTime;
    }
}
