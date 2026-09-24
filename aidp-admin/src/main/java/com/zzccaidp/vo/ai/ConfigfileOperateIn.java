package com.zzccaidp.vo.ai;

import com.zzccaidp.vo.ResHeader;
import lombok.Data;

@Data
public class ConfigfileOperateIn extends ResHeader {

   //规则id
  private String id;
  // 下载文件的id
  private String fileid;
  // 删除的文件标识
  private String flag;
}
