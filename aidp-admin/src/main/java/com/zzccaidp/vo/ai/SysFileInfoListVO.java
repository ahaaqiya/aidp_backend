package com.zzccaidp.vo.ai;

import com.zzccaidp.vo.ResHeader;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @Description: TODO
 * @Author: WB233500
 * @Createtime: 11:22
 * @Version: 1.0
 */
@Data
public class SysFileInfoListVO extends ResHeader implements Serializable {

    List<SysFileInfoVO> historyFileList;

    List<SysFileInfoVO> scratchFileList;
}
