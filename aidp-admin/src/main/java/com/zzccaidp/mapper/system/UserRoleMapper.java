package com.zzccaidp.mapper.system;

import com.zzccaidp.dao.system.RoleDO;
import com.zzccaidp.dao.system.UserDO;
import com.zzccaidp.dao.system.UserRoleDO;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * UpmsUserRoleMapper实现
 *
 * @author bades
 */
public interface UserRoleMapper extends Mapper<UserRoleDO> {
    List<UserRoleDO> selectByUserId(UserRoleDO userRoleDO);

    void batchInsterUserRole(List<UserRoleDO> userRoleDOList);

    List<RoleDO> listRoleByUserId(List<String> userIdList);

    void deleteRoleByUserId(String userId);

    List<UserDO> listUserByRoleId(String roleId);

    void deleteRoleByRoleId(String roleId);
}
