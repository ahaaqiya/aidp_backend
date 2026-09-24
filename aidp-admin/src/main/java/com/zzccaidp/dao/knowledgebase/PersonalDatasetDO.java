package com.zzccaidp.dao.knowledgebase;

import lombok.Data;

import java.util.Date;
import javax.persistence.*;

/**
 * 个人知识库数据集映射表：记录「用户工号 → ragflow数据集ID」的一对一映射。
 * 用于个人知识库「一人一个dataset」方案的懒创建与复用：
 * 首次上传时创建个人 dataset 并落此表，后续上传直接复用，避免重复建库。
 */
@Data
@Table(name = "personal_dataset")
public class PersonalDatasetDO {
    /**
     * 主键ID，自增
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 用户工号（个人知识库的权限隔离标识）
     */
    @Column(name = "user_code")
    private String userCode;

    /**
     * ragflow数据集ID（一人一个dataset）
     */
    @Column(name = "dataset_id")
    private String datasetId;

    /**
     * 创建时间
     */
    @Column(name = "create_time")
    private Date createTime;

    /**
     * 更新时间
     */
    @Column(name = "update_time")
    private Date updateTime;
}
