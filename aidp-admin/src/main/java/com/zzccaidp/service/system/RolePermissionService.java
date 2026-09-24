package com.zzccaidp.service.system;

import com.zzccaidp.dao.system.RolePermissionDO;
import com.zzccaidp.mapper.system.RolePermissionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author bades
 */
@Service
public class RolePermissionService {
    @Autowired
    private RolePermissionMapper rolePermissionMapper;

    public void batchInsterRolePermission(List<RolePermissionDO> rolePermissionDOList) {
        rolePermissionMapper.batchInsterRolePermission(rolePermissionDOList);
    }

    public void deleteRolePermissionByRoleId(List<String> roleIdList) {
        rolePermissionMapper.deleteByRoleIdList(roleIdList);
    }

}
