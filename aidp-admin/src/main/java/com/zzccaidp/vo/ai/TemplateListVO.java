package com.zzccaidp.vo.ai;

import com.zzccaidp.dao.ai.TemplateInfoDO;
import com.zzccaidp.vo.ResHeader;
import lombok.Data;

import java.util.List;

@Data
public class TemplateListVO extends ResHeader {
        //返回结果集
        private List<TemplateInfoDO> list;
}
