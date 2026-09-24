package com.zzccaidp.mapper.datasource;

import com.zzccaidp.dao.datasource.OADocAssociationDO;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * @Description:
 * @Author: WB233500
 * @Createtime: 09:03
 * @Version: 1.0
 */
public interface OADocAssociationMapper extends Mapper<OADocAssociationDO> {
    List<OADocAssociationDO> listDocIdByDocIndexId(List<String> docIndexList);

    void insertOaDocAssociation(List<OADocAssociationDO> oaDocAssociationDOList);
}
