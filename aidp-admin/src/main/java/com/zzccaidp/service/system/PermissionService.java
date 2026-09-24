package com.zzccaidp.service.system;

import cn.hutool.core.bean.BeanUtil;
import com.zzccaidp.context.UserInfoContextHolder;
import com.zzccaidp.dao.system.PermissionDO;
import com.zzccaidp.enums.ErrCodeEnum;
import com.zzccaidp.exception.BusinessException;
import com.zzccaidp.mapper.system.PermissionMapper;
import com.zzccaidp.mapper.system.RolePermissionMapper;
import com.zzccaidp.vo.PageRequest;
import com.zzccaidp.vo.PageResponse;
import com.zzccaidp.vo.system.PermissionIn;
import com.zzccaidp.vo.system.PermissionOut;
import com.zzccaidp.vo.system.PermissionTree;
import com.zzccaidp.vo.system.PermissionTreeIn;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author bades
 */
@Service
@Slf4j
public class PermissionService {

    @Autowired
    private PermissionMapper permissionMapper;

    @Autowired
    private RolePermissionMapper rolePermissionMapper;

    public PageResponse<PermissionOut> list(PageRequest<PermissionIn> pageRequest) {
        PageResponse<PermissionOut> page = new PageResponse<>();
        PageHelper.startPage(pageRequest.getPageNum(), pageRequest.getPageSize());
        List<PermissionDO> permissionDOList = permissionMapper.select(BeanUtil.copyProperties(pageRequest.getData(), PermissionDO.class));
        List<PermissionOut> permissionOutList = BeanUtil.copyToList(permissionDOList, PermissionOut.class);
        PageInfo<PermissionDO> pageInfo = new PageInfo<>(permissionDOList);
        page.setTotalPage(pageInfo.getPages());
        page.setTotalCount(pageInfo.getTotal());
        page.setRecords(permissionOutList);
        page.setSuccessCode();
        return page;
    }

    public void add(PermissionIn permissionIn) {
        PermissionDO permissionDO = BeanUtil.copyProperties(permissionIn, PermissionDO.class);
        permissionDO.setGmtCreateUser(UserInfoContextHolder.getUserInfo());
        permissionDO.setGmtModifiedUser(UserInfoContextHolder.getUserInfo());
        permissionDO.setGmtCreate(LocalDateTime.now());
        permissionDO.setGmtModified(LocalDateTime.now());
        //生成排序号和层级
        createOrderIdAndLevel(permissionDO);
        permissionMapper.insert(permissionDO);
    }

    @Transactional
    public void delete(PermissionIn permissionIn) {
        PermissionDO permissionDO = BeanUtil.copyProperties(permissionIn, PermissionDO.class);
        deleteSonIdList(permissionDO);
    }


    public void update(PermissionIn permissionIn) {
        PermissionDO permissionDO = BeanUtil.copyProperties(permissionIn, PermissionDO.class);
        permissionDO.setGmtModifiedUser(UserInfoContextHolder.getUserInfo());
        permissionDO.setGmtModified(LocalDateTime.now());
        permissionMapper.updateByPrimaryKey(permissionDO);
    }


    /**
     * 菜单树
     *
     * @param nodes
     * @return List<PermissionTreeOut>
     */
    public List<PermissionTree> buildTreeByMap(List<PermissionTree> nodes) {
        // 构建树结构
        Map<String, PermissionTree> nodeMap = new HashMap<>();
        for (PermissionTree node : nodes) {
            nodeMap.put(String.valueOf(node.getId()), node);
        }
        List<PermissionTree> treeNodes = new ArrayList<>();
        for (PermissionTree node : nodes) {
            // 根节点
            if ("0".equals(node.getPid())) {
                buildTree(node, nodeMap);
                treeNodes.add(node);
            }
        }
        return treeNodes;
    }

    private void buildTree(PermissionTree node, Map<String, PermissionTree> nodeMap) {
        List<PermissionTree> children = new ArrayList<>();
        for (PermissionTree child : nodeMap.values()) {
            if (child.getPid().equals(node.getId())) {
                children.add(child);
                buildTree(child, nodeMap);
            }
        }
        node.setChildren(children);
    }

    /**
     * 层级最大为3，1为最顶层菜单。父id是0；
     *
     * @param permissionDO
     */
    private void createOrderIdAndLevel(PermissionDO permissionDO) {
        //根据父id查询当前ID的最大值
        Integer orderId = permissionMapper.getOrderIdByPidOrder(permissionDO);
        //顶层菜单层级为1
        if ("0".equals(permissionDO.getPid())) {
            permissionDO.setLevel(1);
            if (orderId == null) {
                permissionDO.setOrderId(110000);
            } else {
                permissionDO.setOrderId(orderId + 10000);
            }
        } else {
            PermissionDO pPermissionDO = permissionMapper.selectByPrimaryKey(permissionDO.getPid());
            permissionDO.setLevel(pPermissionDO.getLevel() + 1);
            if (permissionDO.getLevel().equals(3)) {
                if (orderId == null) {
                    permissionDO.setOrderId(pPermissionDO.getOrderId() + 1);
                } else {
                    permissionDO.setOrderId(orderId + 1);
                }
            } else if (permissionDO.getLevel().equals(2)) {
                if (orderId == null) {
                    permissionDO.setOrderId(pPermissionDO.getOrderId() + 100);
                } else {
                    permissionDO.setOrderId(orderId + 100);
                }
            } else {
                throw new BusinessException(ErrCodeEnum.M0004);
            }
        }
    }

    /**
     * 联机删除时获取所有下属菜单的id列表
     *
     */
    private void deleteSonIdList(PermissionDO permissionDO) {
        //排序号从当前菜单本身一直到下一个同级菜单的排序号
        Integer startId = 0;
        Integer endId = 0;
        if (permissionDO.getLevel().equals(1)) {
            startId = permissionDO.getOrderId();
            endId = permissionDO.getOrderId() + 10000;
        } else if (permissionDO.getLevel().equals(2)) {
            startId = permissionDO.getOrderId();
            endId = permissionDO.getOrderId() + 100;
        } else if (permissionDO.getLevel().equals(3)) {
            startId = permissionDO.getOrderId();
            endId = permissionDO.getOrderId() + 1;
        }
        List<String> idList = permissionMapper.listPermissionByOrderId(startId, endId);
        permissionMapper.deleteByIdList(idList);
        rolePermissionMapper.deleteByPerIdList(idList);
    }

    public List<PermissionTree> getPermissionTree(PermissionTreeIn permissionTreeIn) {
        List<PermissionDO> permissionDOList = permissionMapper.listPermissionByRoleId(permissionTreeIn.getRoleIdList());
        List<PermissionTree> permissionTreeList = permissionDOList.stream()
                .map(permissionDO -> {
                    PermissionTree permissionTree = BeanUtil.copyProperties(permissionDO, PermissionTree.class);
                    permissionTree.setChildren(new ArrayList<>());
                    return permissionTree;
                })
                .collect(Collectors.toList());
        return this.buildTreeByMap(permissionTreeList);
    }

    public List<PermissionTree> getPermissionTreeNew(List<String> list) {
        List<PermissionDO> permissionDOList;

        if(list.size()>0){
            permissionDOList = permissionMapper.listPermissionByRoleIdNew(list);
        }else{
            permissionDOList = new ArrayList<>();
        }

        List<PermissionTree> permissionTreeList = permissionDOList.stream()
                .map(permissionDO -> {
                    PermissionTree permissionTree = BeanUtil.copyProperties(permissionDO, PermissionTree.class);
                    permissionTree.setChildren(new ArrayList<>());
                    return permissionTree;
                })
                .collect(Collectors.toList());
        return this.buildTreeByMap(permissionTreeList);
    }

    public List<String> listPermissionByRole(PermissionTreeIn permissionTreeIn) {
        return permissionMapper.listPermissionByRoleId(permissionTreeIn.getRoleIdList()).stream()
                .map(PermissionDO::getId).collect(Collectors.toList());
    }
}
