package com.zzccaidp.service.system;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.zzccaidp.common.SnowflakeUtil;
import com.zzccaidp.constants.FileConfigConstant;
import com.zzccaidp.context.UserInfoContextHolder;
import com.zzccaidp.dao.bo.PersonInfoBO;
import com.zzccaidp.dao.system.DepartmentDO;
import com.zzccaidp.dao.system.RoleDO;
import com.zzccaidp.dao.system.UserDO;
import com.zzccaidp.dao.system.UserRoleDO;
import com.zzccaidp.enums.ErrCodeEnum;
import com.zzccaidp.exception.BusinessException;
import com.zzccaidp.mapper.system.DepartmentMapper;
import com.zzccaidp.mapper.system.PersonMapper;
import com.zzccaidp.mapper.system.UserMapper;
import com.zzccaidp.util.StringUtil;
import com.zzccaidp.vo.PageRequest;
import com.zzccaidp.vo.PageResponse;
import com.zzccaidp.vo.system.*;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description:
 * @Author: WB233500
 * @Createtime: 14:53
 * @Version: 1.0
 */
@Service
public class UserService {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private UserRoleService userRoleService;
    @Autowired
    private OrgService orgService;
    @Autowired
    private DepartmentMapper departmentMapper;
    @Autowired
    private PersonMapper personMapper;

    @Value("${aidp.headphoto.path}")
    private String headphotoPath;
    @Value("${aidp.bigicon.path}")
    private String bigIconPath;

    //大头像为空默认名称
    public static final String NULL_IMAGE = "null.jpg";
    //值为0
    public static final String INVALID_RESOURCE_ID = "0";

    public PageResponse<UserOut> list(PageRequest<UserIn> pageRequest) {
        PageResponse<UserOut> page = new PageResponse<>();
        PageHelper.startPage(pageRequest.getPageNum(), pageRequest.getPageSize());

        List<UserDO> userDOList = userMapper.select(BeanUtil.copyProperties(pageRequest.getData(), UserDO.class));

        // 获取所有PersonInfoBO对象
        List<PersonInfoBO> perList = personMapper.selectPersonInfoAll("");
        List<PersonInfoBO> perListAuthType = personMapper.selectUserByAuthType();
        perList.addAll(perListAuthType);

        // 将perList转换为Map，以便快速查找
        Map<String, PersonInfoBO> personInfoMap = perList.stream()
                .collect(Collectors.toMap(PersonInfoBO::getWorkCode, personInfoBO -> personInfoBO));

        // 为userDOList中的每个UserDO对象设置avatar属性和bigIcon属性
        userDOList.forEach(userDO -> {
            Optional.ofNullable(personInfoMap.get(userDO.getUserName()))
                    .ifPresent(x -> {
                        userDO.setAvatar(headphotoPath + converPath(x.getIcon()));
                        userDO.setBigicon(buildIconPath(x.getResourceimageid(), x.getWorkCode())); // 设置bigIcon属性值为"123"
                    });
        });

        List<UserOut> userOutList = BeanUtil.copyToList(userDOList, UserOut.class);
        //设置人员角色
        PageInfo<UserDO> pageInfo = new PageInfo<>(userDOList);
        page.setTotalPage(pageInfo.getPages());
        page.setTotalCount(pageInfo.getTotal());
        setUserRole(userOutList);
        page.setRecords(userOutList);
        page.setSuccessCode();
        return page;
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

    private void setUserRole(List<UserOut> userDOList) {
        //根据人员ID查询所有角色信息
        List<String> userIdList = userDOList.stream().map(UserOut::getId).collect(Collectors.toList());
        userDOList.forEach(userOut -> {
            List<RoleDO> roleDOList = userRoleService.listRoleByUserId(Collections.singletonList(userOut.getId()));
            List<RoleOut> roleOutList = BeanUtil.copyToList(roleDOList, RoleOut.class);
            userOut.setRoleOutList(roleOutList);
        });
        userDOList.forEach(userOut -> {
            DepartmentDO departmentDO = departmentMapper.selectByPrimaryKey(userOut.getDeptId());
            userOut.setDeptName(Objects.isNull(departmentDO) ? "行外人员部门" : departmentDO.getName());
        });

    }

    public void add(UserIn userIn) {
        // 校验userIn对象userName属性值是否存在于perList中 PersonInfoBO对象的workCode中
        boolean userNameExists = isUserNameExist(userIn);
        if (userNameExists) {
            throw new BusinessException(ErrCodeEnum.M0020);
        }

        UserDO userDO = BeanUtil.copyProperties(userIn, UserDO.class);
        userDO.setGmtCreateUser(UserInfoContextHolder.getUserInfo());
        userDO.setGmtModifiedUser(UserInfoContextHolder.getUserInfo());
        userDO.setGmtCreate(LocalDateTime.now());
        userDO.setGmtModified(LocalDateTime.now());

        String encryptedPassword = BCrypt.hashpw("123456", BCrypt.gensalt());
        userDO.setPassword(encryptedPassword);

        userMapper.insert(userDO);
    }

    public boolean isUserNameExist(UserIn userIn) {
        // 获取所有PersonInfoBO对象
        List<PersonInfoBO> perList = personMapper.selectPersonInfoAll("");
        List<PersonInfoBO> perListAuthType = personMapper.selectUserByAuthType();
        perList.addAll(perListAuthType);

        // 检查 userIn 的 userName 是否存在于 perList 的 workCode 中
        return perList.stream()
                .anyMatch(personInfoBO -> personInfoBO.getWorkCode().equals(userIn.getUserName()));
    }


    public void delete(UserIn userIn) {
        UserDO userDO = BeanUtil.copyProperties(userIn, UserDO.class);
        userMapper.delete(userDO);
    }

    public void update(UserIn userIn) {
        UserDO userDO = BeanUtil.copyProperties(userIn, UserDO.class);
        userDO.setGmtModifiedUser(UserInfoContextHolder.getUserInfo());
        userDO.setGmtModified(LocalDateTime.now());
        userMapper.updateByPrimaryKey(userDO);
    }

    public void addRole(UserRoleIn userRoleIn) {
        userRoleService.deleteRoleByUserId(userRoleIn.getId());
        if (Objects.isNull(userRoleIn.getRoles()) || userRoleIn.getRoles().isEmpty()) {
            return;
        }
        List<UserRoleDO> userRoleDOList = userRoleIn.getRoles().stream()
                .map(userRole -> {
                    UserRoleDO userRoleDO = new UserRoleDO();
                    userRoleDO.setId(SnowflakeUtil.nextIdStr());
                    userRoleDO.setGmtCreateUser(UserInfoContextHolder.getUserInfo());
                    userRoleDO.setGmtModifiedUser(UserInfoContextHolder.getUserInfo());
                    userRoleDO.setGmtCreate(LocalDateTime.now());
                    userRoleDO.setGmtModified(LocalDateTime.now());
                    userRoleDO.setUserId(userRoleIn.getId());
                    userRoleDO.setRoleId(userRole);
                    return userRoleDO;
                }).collect(Collectors.toList());
        userRoleService.batchInsterUserRole(userRoleDOList);
    }
}
