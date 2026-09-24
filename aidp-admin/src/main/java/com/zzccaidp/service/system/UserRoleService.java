package com.zzccaidp.service.system;

import com.zzccaidp.dao.system.RoleDO;
import com.zzccaidp.dao.system.UserDO;
import com.zzccaidp.dao.system.UserRoleDO;
import com.zzccaidp.mapper.system.UserRoleMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * UpmsUserRoleService接口
 *
 * @author bades
 */
@Service
public class UserRoleService {

    private UserRoleMapper userRoleMapper;

    @Autowired
    public UserRoleService(UserRoleMapper userRoleMapper) {
        this.userRoleMapper = userRoleMapper;
    }

    public List<UserDO> listUser(String id) {
        return userRoleMapper.listUserByRoleId(id);
    }

    public List<UserRoleDO> list(UserRoleDO userRoleDO) {
        return userRoleMapper.selectByUserId(userRoleDO);
    }

    public void add(UserRoleDO userRoleDO) {
        userRoleMapper.insertSelective(userRoleDO);
    }

    public void batchInsterUserRole(List<UserRoleDO> userRoleDOList) {
        userRoleMapper.batchInsterUserRole(userRoleDOList);
    }

    public List<RoleDO> listRoleByUserId(List<String> userIdList) {
        return userRoleMapper.listRoleByUserId(userIdList);
    }

    public void deleteRoleByUserId(String userId) {
        userRoleMapper.deleteRoleByUserId(userId);
    }

    public void deleteRoleByRoleId(String roleId) {
        userRoleMapper.deleteRoleByRoleId(roleId);
    }
}
