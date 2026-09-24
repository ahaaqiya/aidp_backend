package com.zzccaidp.vo.ai;

import com.zzccaidp.vo.ResHeader;
import lombok.Data;

import java.util.List;

/**
 * @Description: TODO
 * @Author: WB233500
 * @Createtime: 09:58
 * @Version: 1.0
 */
@Data
public class ConversationsOut extends ResHeader {
    private List<ConversationsVO> data;
}
