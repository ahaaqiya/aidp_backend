package com.zzccaidp.service.system;

import cn.hutool.core.bean.BeanUtil;
import com.zzccaidp.common.SnowflakeUtil;
import com.zzccaidp.context.UserInfoContextHolder;
import com.zzccaidp.dao.system.*;
import com.zzccaidp.mapper.system.DepartmentMapper;
import com.zzccaidp.mapper.system.RoleMapper;
import com.zzccaidp.mapper.system.RolePermissionMapper;
import com.zzccaidp.vo.PageRequest;
import com.zzccaidp.vo.PageResponse;
import com.zzccaidp.vo.system.*;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * UpmsRoleService接口
 *
 * @author bades
 */
@Service
@Log4j
public class RoleService {
    @Autowired
    private RoleMapper roleMapper;
    @Autowired
    private UserRoleService userRoleService;
    @Autowired
    private RolePermissionService rolePermissionService;
    @Autowired
    private DepartmentMapper departmentMapper;

    public PageResponse<RoleOut> list(PageRequest<RoleIn> pageRequest) {
        PageResponse<RoleOut> page = new PageResponse<>();
        PageHelper.startPage(pageRequest.getPageNum(), pageRequest.getPageSize());
        List<RoleDO> roleDOList = roleMapper.select(BeanUtil.copyProperties(pageRequest.getData(), RoleDO.class));
        List<RoleOut> roleOutList = BeanUtil.copyToList(roleDOList, RoleOut.class);
        PageInfo<RoleDO> pageInfo = new PageInfo<>(roleDOList);
        page.setTotalPage(pageInfo.getPages());
        page.setTotalCount(pageInfo.getTotal());
        page.setRecords(roleOutList);
        page.setSuccessCode();
        return page;
    }

    public void add(RoleIn roleIn) {
        RoleDO roleDO = BeanUtil.copyProperties(roleIn, RoleDO.class);
        roleDO.setGmtCreateUser(UserInfoContextHolder.getUserInfo());
        roleDO.setGmtModifiedUser(UserInfoContextHolder.getUserInfo());
        roleDO.setGmtCreate(LocalDateTime.now());
        roleDO.setGmtModified(LocalDateTime.now());
        roleDO.setSystem("false");
        roleMapper.insert(roleDO);
    }

    public void delete(RoleIn roleIn) {
        RoleDO roleDO = BeanUtil.copyProperties(roleIn, RoleDO.class);
        roleMapper.delete(roleDO);
    }

    public void update(RoleIn roleIn) {
        RoleDO roleDO = BeanUtil.copyProperties(roleIn, RoleDO.class);
        roleDO.setGmtModifiedUser(UserInfoContextHolder.getUserInfo());
        roleDO.setGmtModified(LocalDateTime.now());
        roleMapper.updateByPrimaryKey(roleDO);
    }

    public void addPermission(RolePermissionIn rolePermissionIn) {
        // 根据角色ID先删除原有的关联关系
        rolePermissionService.deleteRolePermissionByRoleId(Collections.singletonList(rolePermissionIn.getId()));
        if (rolePermissionIn.getPermissions().isEmpty()) {
            return;
        }
        List<RolePermissionDO> rolePermissionDOList = rolePermissionIn.getPermissions().stream()
                .map(permissionId -> {
                    RolePermissionDO rolePermissionDO = new RolePermissionDO();
                    rolePermissionDO.setId(SnowflakeUtil.nextIdStr());
                    rolePermissionDO.setGmtCreateUser(UserInfoContextHolder.getUserInfo());
                    rolePermissionDO.setGmtModifiedUser(UserInfoContextHolder.getUserInfo());
                    rolePermissionDO.setGmtCreate(LocalDateTime.now());
                    rolePermissionDO.setGmtModified(LocalDateTime.now());
                    rolePermissionDO.setPermissionId(permissionId);
                    rolePermissionDO.setStatus("1");
                    rolePermissionDO.setRoleId(rolePermissionIn.getId());
                    return rolePermissionDO;
                }).collect(Collectors.toList());

        List<RolePermissionDO> rolePermissionHalfDOList = rolePermissionIn.getHalfPermissions().stream()
                .map(permissionId -> {
                    RolePermissionDO rolePermissionDO = new RolePermissionDO();
                    rolePermissionDO.setId(SnowflakeUtil.nextIdStr());
                    rolePermissionDO.setGmtCreateUser(UserInfoContextHolder.getUserInfo());
                    rolePermissionDO.setGmtModifiedUser(UserInfoContextHolder.getUserInfo());
                    rolePermissionDO.setGmtCreate(LocalDateTime.now());
                    rolePermissionDO.setGmtModified(LocalDateTime.now());
                    rolePermissionDO.setPermissionId(permissionId);
                    rolePermissionDO.setStatus("0");
                    rolePermissionDO.setRoleId(rolePermissionIn.getId());
                    return rolePermissionDO;
                }).collect(Collectors.toList());

        rolePermissionDOList.addAll(rolePermissionHalfDOList);
        rolePermissionService.batchInsterRolePermission(rolePermissionDOList);
    }

    public void addUser(RoleUserIn roleUserIn) {
        userRoleService.deleteRoleByRoleId(roleUserIn.getId());
        if (Objects.isNull(roleUserIn.getUserIdList()) || roleUserIn.getUserIdList().isEmpty()) {
            return;
        }
        List<UserRoleDO> userRoleDOList = roleUserIn.getUserIdList().stream()
                .map(userRole -> {
                    UserRoleDO userRoleDO = new UserRoleDO();
                    userRoleDO.setId(SnowflakeUtil.nextIdStr());
                    userRoleDO.setGmtCreateUser(UserInfoContextHolder.getUserInfo());
                    userRoleDO.setGmtModifiedUser(UserInfoContextHolder.getUserInfo());
                    userRoleDO.setGmtCreate(LocalDateTime.now());
                    userRoleDO.setGmtModified(LocalDateTime.now());
                    userRoleDO.setUserId(userRole);
                    userRoleDO.setRoleId(roleUserIn.getId());
                    return userRoleDO;
                }).collect(Collectors.toList());
        userRoleService.batchInsterUserRole(userRoleDOList);
    }

    public List<RoleUserVO> listUser(RoleIn roleIn) {
        List<UserDO> roleDOList = userRoleService.listUser(roleIn.getId());
        return roleDOList.stream().map(var -> {
            RoleUserVO roleUserOut = new RoleUserVO();
            roleUserOut.setLabel(var.getRealName());
            roleUserOut.setWorkCode(var.getUserName());
            roleUserOut.setId(var.getId());
            DepartmentDO departmentDO = departmentMapper.selectByPrimaryKey(var.getDeptId());
            roleUserOut.setPidName(Objects.isNull(departmentDO) ? "行外人员部门" : departmentDO.getName());
            return roleUserOut;
        }).collect(Collectors.toList());
    }
}
