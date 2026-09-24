package com.zzccaidp.service.system;

import com.zzccaidp.common.SnowflakeUtil;
import com.zzccaidp.constants.FileConfigConstant;
import com.zzccaidp.context.UserInfoContextHolder;
import com.zzccaidp.dao.bo.DepartmentBO;
import com.zzccaidp.dao.bo.OrgBO;
import com.zzccaidp.dao.bo.PersonInfoBO;
import com.zzccaidp.dao.system.PersonDO;
import com.zzccaidp.dao.system.UserDO;
import com.zzccaidp.dao.system.UserRoleDO;
import com.zzccaidp.mapper.system.DepartmentMapper;
import com.zzccaidp.mapper.system.OrgMapper;
import com.zzccaidp.mapper.system.PersonMapper;
import com.zzccaidp.mapper.system.UserMapper;
import com.zzccaidp.util.StringUtil;
import com.zzccaidp.vo.system.PersonPageQueryRequest;
import com.zzccaidp.vo.system.PersonPageQueryResponse;
import com.zzccaidp.vo.system.TreeNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description: 用户
 * @Author: WB211928
 * @Createtime: 2026-04-29 09:24
 * @Version: 1.0
 */
@Slf4j
@Service
public class PersonService {

    @Autowired
    private PersonMapper personMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private DepartmentMapper departmentMapper;
    @Autowired
    private OrgMapper orgMapper;

    @Value("${aidp.headphoto.path}")
    private String headphotoPath;
    @Value("${aidp.bigicon.path}")
    private String bigIconPath;

    //大头像为空默认名称
    public static final String NULL_IMAGE = "null.jpg";
    //值为0
    public static final String INVALID_RESOURCE_ID = "0";

    public PersonPageQueryResponse selectPersonInfoAll(PersonPageQueryRequest personPageQueryRequest) {

        PersonPageQueryResponse personPageQueryResponse = new PersonPageQueryResponse();

        log.info("[PersonQueryService] selectPersonInfoAll 请求入参：{}", personPageQueryRequest);
        try {
            List<TreeNode> response = new ArrayList<>();
            List<TreeNode> response01;
            List<TreeNode> response02;
            List<TreeNode> response03;

            List<PersonInfoBO> perList = personMapper.selectPersonInfoAll("");
            List<PersonInfoBO> perListAuthType = personMapper.selectUserByAuthType();
            perList.addAll(perListAuthType);

            List<DepartmentBO> deptList;
            List<OrgBO> orgList;
            if (Objects.isNull(personPageQueryRequest) || Objects.isNull(personPageQueryRequest.getCurrentLoginDeptid())) {
                deptList = departmentMapper.selectDeptAllNew("");
                orgList = orgMapper.selectOrgAllNew("");
            } else {
                log.info("[PersonQueryService] selectDeptAllNew SQL查询入参：{}", personPageQueryRequest.getCurrentLoginDeptid());
                deptList = departmentMapper.selectDeptAllNew(personPageQueryRequest.getCurrentLoginDeptid());
                orgList = orgMapper.selectOrgAllNew(deptList.get(0).getPid());
            }
            DepartmentBO deptBO = new DepartmentBO();
            deptBO.setId("xn");
            deptBO.setName("行外人员部门");
            deptBO.setPid("21");
            deptBO.setPidName("浙银理财有限责任公司");
            deptBO.setSupDeptId("0");
            deptList.add(deptBO);
            Map<String, String> map = new HashMap<>();
            userMapper.selectAll().forEach(var -> {
                map.put(var.getUserName(), var.getId());
            });
            response01 = perList.stream().map(x -> {
                String id = x.getId();
                String workCode = x.getWorkCode();
                String label = x.getName();
                String pid = x.getDeptId() + "_dept";
                String pidName = x.getDeptName();
                String icon = x.getIcon();
                String allPath = headphotoPath + converPath(icon);
                String bigicon = buildIconPath(x.getResourceimageid(), x.getWorkCode());
                String pinyin = x.getPinyin();
                return new TreeNode(workCode, id, label, pid, pidName, false, allPath, bigicon, pinyin, map.get(workCode));
            }).collect(Collectors.toList());

            response02 = deptList.stream().map(x -> {
                String id = x.getId() + "_dept";
                String label = x.getName();
                String pid = x.getPid() + "_org";
                String pidname = x.getPidName();
                return new TreeNode(id, null, label, pid, pidname, false);
            }).collect(Collectors.toList());

            response03 = orgList.stream().map(x -> {
                String id = x.getId() + "_org";
                String label = x.getName();
                String pid = x.getPid() + "_org";
                String pidname = x.getPidName();
                return new TreeNode(id, null, label, pid, pidname, false);
            }).collect(Collectors.toList());

            response.addAll(response01);
            response.addAll(response02);
            response.addAll(response03);

            List<TreeNode> treeNodes = buildTree(response);

            personPageQueryResponse.setPersonList(treeNodes);
            personPageQueryResponse.setSuccessCode();
        } catch (Exception e) {
            log.error("[PersonQueryService] selectPersonInfoAll 查询异常; error message", e);
            personPageQueryResponse.setErrorCode();
        }

        return personPageQueryResponse;
    }

    public PersonPageQueryResponse selectOrgInfoAll(PersonPageQueryRequest personPageQueryRequest) {

        PersonPageQueryResponse personPageQueryResponse = new PersonPageQueryResponse();

        try {

            List<TreeNode> response = new ArrayList<>();
            List<TreeNode> response02;
            List<TreeNode> response03;

            List<OrgBO> orgList = orgMapper.selectOrgAllNew("");
            List<DepartmentBO> deptList = departmentMapper.selectDeptAllNew("");
            DepartmentBO deptBO = new DepartmentBO();
            deptBO.setId("xn");
            deptBO.setName("行外人员部门");
            deptBO.setPid("21");
            deptBO.setPidName("浙银理财有限责任公司");
            deptBO.setSupDeptId("0");
            deptList.add(deptBO);

            response02 = deptList.stream().map(x -> {
                String id = x.getId() + "_dept";
                String label = x.getName();
                String pid = x.getPid() + "_org";
                String pidname = x.getPidName();
                return new TreeNode(id, id, label, pid, pidname, false);
            }).collect(Collectors.toList());

            response03 = orgList.stream().map(x -> {
                String id = x.getId() + "_org";
                String label = x.getName();
                String pid = x.getPid() + "_org";
                String pidname = x.getPidName();
                return new TreeNode(id, id, label, pid, pidname, true);
            }).collect(Collectors.toList());

            response.addAll(response02);
            response.addAll(response03);

            List<TreeNode> treeNodes = buildTreeByMap(response);

            personPageQueryResponse.setOrgList(treeNodes);
            personPageQueryResponse.setSuccessCode();

        } catch (Exception e) {
            log.error("[PersonQueryService] selectOrgInfoAll 查询异常; error message", e);
            personPageQueryResponse.setErrorCode();
        }
        return personPageQueryResponse;
    }

    public List<TreeNode> buildTreeByMap(List<TreeNode> nodes) {
        List<TreeNode> roots = new ArrayList<>();
        Map<String, TreeNode> nodeMap = new HashMap<>();

        for (TreeNode node : nodes) {
            nodeMap.put(node.getId(), node);
            //如果TreeNode构造函数未初始化children,需要再此处处理
            if (node.getChildren() == null) {
                node.setChildren(new ArrayList<>());
            }
        }

        for (TreeNode node : nodes) {
            String pid = node.getPid();
            if (pid == null || "0_org".equals(pid)) {
                roots.add(node);
            } else {
                TreeNode parent = nodeMap.get(pid);
                if (parent != null) {
                    parent.getChildren().add(node);
                }
            }
        }

        return roots;
    }

    public String converPath(String iconStr) {
        String resultUrl = "null.jpg";
        if (!StringUtils.isEmpty(iconStr)) {
            String[] split = iconStr.split("/");
            if (split.length == 4) {
                resultUrl = split[3].contains(".") ? split[3] : resultUrl;
            } else {
                resultUrl = split[2].contains(".") ? split[2] : resultUrl;
            }
        }
        return resultUrl;
    }

    private String buildIconPath(String resourceimageid, String workCode) {
        if (StringUtil.isNotEmpty(resourceimageid) &&
                !INVALID_RESOURCE_ID.equals(resourceimageid)) {
            return bigIconPath + workCode + FileConfigConstant.FILE_NAME_SUFFIX;
        } else {
            return bigIconPath + NULL_IMAGE;
        }
    }

    /**
     * 递归创建树形结构
     */
    public List<TreeNode> buildTree(List<TreeNode> nodes) {
        return nodes.stream().filter(node -> node.getPid() == null || "0_org".equals(node.getPid()))
                .peek(root -> root.setChildren(findChildren(root, nodes)))
                .collect(Collectors.toList());
    }

    /**
     * 递归查找子节点
     *
     * @return
     */
    public List<TreeNode> findChildren(TreeNode parent, List<TreeNode> nodes) {
        return nodes.stream().filter(node -> parent.getId().equals(node.getPid()))
                .peek(child -> child.setChildren(findChildren(child, nodes)))
                .collect(Collectors.toList());
    }

    public void insertUserForOa(List<String> workcodeList) {
        try {
            if (workcodeList.size() > 0 && workcodeList != null) {

                List<Map<String, Object>> personDOListMap = personMapper.listPersonByUserId(workcodeList);

                List<UserDO> userDOList = personDOListMap.stream().map(map -> {
                    String WORKCODE = (String) map.get("WORKCODE");
                    Integer SUBCOMPANYID1 = (Integer) map.get("SUBCOMPANYID1");
                    String LASTNAME = (String) map.get("LASTNAME");
                    String SEX = (String) map.get("SEX");
                    Integer DEPARTMENTID = (Integer) map.get("DEPARTMENTID");

                    UserDO userRoleDO = new UserDO();
                    userRoleDO.setId(SnowflakeUtil.nextIdStr());
                    userRoleDO.setDelFlag(false);
                    userRoleDO.setGmtCreate(LocalDateTime.now());
//                    userRoleDO.setGmtCreateUser(UserInfoContextHolder.getUserInfo());
                    userRoleDO.setGmtCreateUser("admin");

                    userRoleDO.setUserName(WORKCODE);
                    userRoleDO.setOrgId(String.valueOf(SUBCOMPANYID1));

                    userRoleDO.setPassword("$2a$10$s.CivquZc9PCFG/rJ7HdyeUDi1Rzi0PSHoPBLHb07fLVmpLDbocjfCOzXLLFPjPhS3cEXrT1LVQBTWPRy");
                    userRoleDO.setRealName(LASTNAME);
                    userRoleDO.setAuthType("1");
                    userRoleDO.setSex(Integer.parseInt(SEX));
                    userRoleDO.setStatus(0);
                    userRoleDO.setDeptId(String.valueOf(DEPARTMENTID));
                    return userRoleDO;
                }).collect(Collectors.toList());

                userMapper.batchInsterUser(userDOList);
            }
        } catch (Exception e) {
            log.error("新增用户失败！", e);
        }
    }

    public void deleteUserForOa(List<String> workcodeList) {
        try {
            if (workcodeList.size() > 0 && workcodeList != null) {
                personMapper.deleteUserForOa(workcodeList);
            }
        } catch (Exception e) {
            log.error("删除用户失败！", e);
        }
    }

}
