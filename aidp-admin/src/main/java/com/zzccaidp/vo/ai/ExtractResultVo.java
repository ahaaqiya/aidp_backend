package com.zzccaidp.vo.ai;


import com.alibaba.fastjson.JSONObject;
import com.zzccaidp.vo.ResHeader;
import lombok.Data;

import java.io.Serializable;

/**
 * 应用管理实体类
 */
@Data
public class ExtractResultVo extends ResHeader implements Serializable  {

    /**
     * 提取结果
     */
    private JSONObject result;


}

