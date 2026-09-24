package com.zzccaidp.vo.ai;

import com.zzccaidp.vo.ResHeader;
import lombok.Data;

import java.io.Serializable;

/**
 * @Description: TODO
 * @Author: WB233500
 * @Createtime: 11:19
 * @Version: 1.0
 */
@Data
public class AgainFileForAwsVO extends ResHeader implements Serializable {
    private String content;
    private String contentList;
    private String type;
    private String fileName;
    private String fileId;
}
