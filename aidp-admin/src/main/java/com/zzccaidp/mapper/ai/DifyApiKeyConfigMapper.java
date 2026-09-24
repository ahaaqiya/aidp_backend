package com.zzccaidp.mapper.ai;

import com.zzccaidp.dao.ai.DifyApiKeyConfigDO;
import com.zzccaidp.vo.ai.ListDifyApiKeyParam;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * @author zhangtiantian
 * @date 2026/4/13
 */
public interface DifyApiKeyConfigMapper extends Mapper<DifyApiKeyConfigDO> {


    List<DifyApiKeyConfigDO> listDifyApiKey(ListDifyApiKeyParam data);
}
