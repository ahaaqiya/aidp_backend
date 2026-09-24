package com.zzccaidp.mapper.system;

import com.zzccaidp.dao.system.PermissionDO;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * PermissionMapper实现
 *
 * @author bades
 */
public interface PermissionMapper extends Mapper<PermissionDO> {
    /**
     *
     * @param permissionDO
     * @return
     */
    Integer getOrderIdByPidOrder(PermissionDO permissionDO);

    List<String> listPermissionByOrderId(Integer startId, Integer endId);

    void deleteByIdList(List<String> idList);

    List<PermissionDO> listPermissionByRoleId(List<String> roleIdList);

    List<PermissionDO> listPermissionByRoleIdNew(List<String> roleIdList);
}
