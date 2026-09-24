package com.zzccaidp.dao.ai;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

/**
 * @author zhangtiantian
 * @date 2026/5/7
 */
@Table(name = "zzccaidp_template_last_file")
public class TemplateLastFileDO {

    @Id
    private String id;

    @Column(name = "file_id")
    private String fileId;

    @Column(name = "template_id")
    private String templateId;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "create_time")
    private Date createTime;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}
