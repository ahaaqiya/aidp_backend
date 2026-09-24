package com.zzccaidp.mapper.system;

import com.zzccaidp.dao.system.RolePermissionDO;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * UpmsRolePermissionMapper实现
 *
 * @author bades
 */
public interface RolePermissionMapper extends Mapper<RolePermissionDO> {
    void deleteByIdList(List<String> idList);

    void deleteByPerIdList(List<String> perIdList);

    void deleteByRoleIdList(List<String> roledList);

    void batchInsterRolePermission(List<RolePermissionDO> rolePermissionDOList);
}
