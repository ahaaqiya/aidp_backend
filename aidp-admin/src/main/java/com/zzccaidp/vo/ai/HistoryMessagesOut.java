package com.zzccaidp.vo.ai;

import com.zzccaidp.vo.ResHeader;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @Description:
 * @Author: WB233500
 * @Createtime: 14:11
 * @Version: 1.0
 */
@Setter
@Getter
public class HistoryMessagesOut extends ResHeader {
    List<HistoryMessagesVO> data;
}
