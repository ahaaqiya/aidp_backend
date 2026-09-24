package com.zzccaidp.mapper.system;

import com.zzccaidp.dao.bo.OrgBO;
import com.zzccaidp.dao.system.OrgDO;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * @Description:
 * @Author: WB233500
 * @Createtime: 09:55
 * @Version: 1.0
 */
public interface OrgMapper extends Mapper<OrgDO> {

    List<OrgBO> selectOrgAllNew(@Param("orgId") String orgId);

}
