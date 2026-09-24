package com.zzccaidp.dao.ai;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * @Description: ai文件解析结果保存表
 * @Author: WB233500
 * @Createtime: 16:21
 * @Version: 1.0
 */
@Data
@Table(name = "zzccaidp_ai_file_result")
public class AiFileResultDO implements Serializable {
    @Id
    @Column(name="file_id")
    private String fileId;
    @Column(name="file_extraction_result")
    private String fileExtractionResult;
    @Column(name="file_extraction_template")
    private String fileExtractionTemplate;
    @Column(name="file_parsing_result")
    private String fileParsingResult;
    @Column(name="key_data")
    private String keyData;
    @Column(name="user_id")
    private String userId;
    @Column(name="dept_id")
    private String deptId;
}
