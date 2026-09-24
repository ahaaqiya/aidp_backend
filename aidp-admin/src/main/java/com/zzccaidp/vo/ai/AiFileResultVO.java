package com.zzccaidp.vo.ai;

import com.zzccaidp.vo.ResHeader;
import lombok.Data;

import java.io.Serializable;

/**
 * @Description: ai文件解析结果保存表
 * @Author: WB233500
 * @Createtime: 16:21
 * @Version: 1.0
 */
@Data
public class AiFileResultVO extends ResHeader implements Serializable {
    private String fileId;
    private String fileExtractionResult;
    private String fileExtractionTemplate;
    private String fileParsingResult;
    private String keyData;
}
