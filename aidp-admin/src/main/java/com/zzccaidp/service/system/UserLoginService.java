package com.zzccaidp.service.system;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson.JSON;
import cn.hutool.crypto.digest.BCrypt;
import com.zzccaidp.ssoc.SsocService;
import com.zzccaidp.common.RedisUtil;
import com.zzccaidp.common.SnowflakeUtil;
import com.zzccaidp.constants.FileConfigConstant;
import com.zzccaidp.dao.bo.PersonInfoBO;
import com.zzccaidp.dao.system.*;
import com.zzccaidp.enums.ErrCodeEnum;
import com.zzccaidp.exception.BusinessException;
import com.zzccaidp.mapper.system.DepartmentMapper;
import com.zzccaidp.mapper.system.OrgMapper;
import com.zzccaidp.mapper.system.PersonMapper;
import com.zzccaidp.mapper.system.UserMapper;
import com.zzccaidp.util.StringUtil;
import com.zzccaidp.vo.ReqHeader;
import com.zzccaidp.vo.system.PermissionTree;
import com.zzccaidp.vo.system.UserLoginIn;
import com.zzccaidp.vo.system.UserLoginOut;
import com.zzccaidp.vo.system.UserSSoLoginIn;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Description:
 * @Author: WB233500
 * @Createtime: 11:53
 * @Version: 1.0
 */
@Slf4j
@Service
public class UserLoginService {
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private UserRoleService userRoleService;
    @Autowired
    private PersonMapper personMapper;
    @Autowired
    private PermissionService permissionService;
    @Resource(name = "ssocService")
    private SsocService ssocService;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private DepartmentMapper departmentMapper;

    @Value("${login.sessionTime:600000}")
    private long loginSessionTimeOut;
    @Value("${aidp.headphoto.path}")
    private String headphotoPath;
    @Value("${aidp.bigicon.path}")
    private String bigIconPath;

    //大头像为空默认名称
    public static final String NULL_IMAGE = "null.jpg";
    //值为0
    public static final String INVALID_RESOURCE_ID = "0";
    /**
     * 用户登录
     *
     * @param in
     * @return
     * @throws
     */
    public UserLoginOut login(UserLoginIn in) throws BusinessException {
        UserLoginOut out = new UserLoginOut();
        //校验用户
        UserDO dbUser = userMapper.selectUserByUserName(BeanUtil.copyProperties(in, UserDO.class));
        DepartmentDO departmentDO = departmentMapper.selectByPrimaryKey(dbUser.getDeptId());
        dbUser.setDeptName(departmentDO == null ? StringUtils.EMPTY : departmentDO.getName());
        if (ObjectUtils.isEmpty(dbUser)) {
            throw new BusinessException(ErrCodeEnum.M0001);
        }
        //校验密码
        this.checkUserPassword(in.getUserName(), in.getPassword(), dbUser.getPassword());

        // 获取所有PersonInfoBO对象
        PersonInfoBO user01 = personMapper.selectPersonInfoAllByWorkcode(dbUser.getUserName());
        PersonInfoBO user02 = personMapper.selectUserByWorkcode(dbUser.getUserName());
        PersonInfoBO personInfoBO = user01 == null ? user02 : user01;

        if(user01 == null && user02 == null){
            personInfoBO = new PersonInfoBO();
            log.info("---用户登录数据有问题，输出返回参数[{}]---[{}]", user01,user02);
        }

        //初始化用户信息
        this.initUserInfo(dbUser, out, personInfoBO);

        redisUtil.addSeesionByToken(out.getToken(), JSON.toJSONString(out), loginSessionTimeOut);
        log.info("---用户登录，输出返回参数[{}]---", out);
        return out;
    }

    public UserLoginOut ssologin(UserSSoLoginIn in) throws BusinessException {
        UserLoginOut out = new UserLoginOut();
        // 校验免密登录参数
        String userId = ssocService.getSsoUserId(in.getSsoAuthSM(), in.getSsoSignSM());
        log.info("---用户免密登录，从统一身份获取用户id[{}]---", userId);
        if (StringUtils.isEmpty(userId)) {
            throw new BusinessException(ErrCodeEnum.M0003);
        } else {
            // 获取用户信息
            UserDO userDO = new UserDO();
            userDO.setUserName(userId);
            UserDO dbUser = userMapper.selectOne(userDO);
            if (ObjectUtils.isEmpty(dbUser)) {
                throw new BusinessException(ErrCodeEnum.M0001);
            }
            DepartmentDO departmentDO = departmentMapper.selectByPrimaryKey(dbUser.getDeptId());
            dbUser.setDeptName(departmentDO == null ? StringUtils.EMPTY : departmentDO.getName());

            // 获取所有PersonInfoBO对象
            PersonInfoBO user01 = personMapper.selectPersonInfoAllByWorkcode(dbUser.getUserName());
            PersonInfoBO user02 = personMapper.selectUserByWorkcode(dbUser.getUserName());
            PersonInfoBO personInfoBO = user01 == null ? user02 : user01;

            if(user01 == null && user02 == null){
                personInfoBO = new PersonInfoBO();
                log.info("---用户登录数据有问题，输出返回参数[{}]---[{}]", user01,user02);
            }

            this.initUserInfo(dbUser, out, personInfoBO);

            redisUtil.addSeesionByToken(out.getToken(), JSON.toJSONString(out), loginSessionTimeOut);
            log.info("---用户免密登录，输出返回参数[{}]---", out);
        }

        return out;
    }

    public UserLoginOut getMenuByloginId(String loginId) throws BusinessException {
        UserLoginOut out = new UserLoginOut();
        //初始化用户菜单信息
        this.initUserMenuInfo(loginId, out);
        log.info("---用户登录，输出返回参数[{}]---", out);
        return out;
    }

    public void loginOut(ReqHeader in) throws BusinessException {
        redisUtil.getSessionByToken(in.getToken(), 0);
    }

    private void checkUserPassword(String userName, String password, String localPasswordHashed) {
        if (!BCrypt.checkpw(password, localPasswordHashed)) {
            log.error("密码认证错误,用户[{}] 当前输入[{}] 本地[{}]", userName, password, localPasswordHashed);
            throw new BusinessException(ErrCodeEnum.M0002);
        }
    }

    private List<String> getRoleIdByUserId(String id) {
        UserRoleDO userRoleDO = new UserRoleDO();
        userRoleDO.setUserId(id);
        return userRoleService.list(userRoleDO).stream().map(UserRoleDO::getRoleId).collect(Collectors.toList());
    }

    private List<PermissionTree> getMenuListByRoleIdList(List<String> roleList) {
        return permissionService.getPermissionTreeNew(roleList);
    }

    private void initUserInfo(UserDO dbUser, UserLoginOut out,PersonInfoBO personInfoBO) {
        // 缓存token-用户
        String token = SnowflakeUtil.nextIdStr();
        //根据用户id返回用户角色ID列表
        List<String> roleList = getRoleIdByUserId(dbUser.getId());
        //必须有
        if (!roleList.contains("1") && !roleList.contains("1779106234")) {
            throw new BusinessException(ErrCodeEnum.M0001);
        }
        //根据用户角色ID列表返回用户菜单列表
        List<PermissionTree> menuList = getMenuListByRoleIdList(roleList);
        // 返回报文
        //工号
        out.setUsername(dbUser.getUserName());
        out.setOrgId(dbUser.getOrgId());
        out.setDeptId(dbUser.getDeptId());
        out.setSex(dbUser.getSex());
        out.setAuthType(dbUser.getAuthType());
        //姓名
        out.setRealName(dbUser.getRealName());
        out.setUserid(String.valueOf(dbUser.getId()));
        out.setToken(token);
        out.setSuccessCode();
        out.setRoleIds(roleList);
        out.setMenuList(menuList);
        out.setDeptId(dbUser.getDeptId());
        out.setDeptName(dbUser.getDeptName());
        out.setIcon(headphotoPath + this.converPath(personInfoBO.getIcon()));
        out.setBigicon(this.buildIconPath(personInfoBO.getResourceimageid(), personInfoBO.getWorkCode()));
    }

    private void initUserMenuInfo(String loginId, UserLoginOut out) {
        //根据用户id返回用户角色ID列表
        List<String> roleList = getRoleIdByUserId(loginId);
        //根据用户角色ID列表返回用户菜单列表
        List<PermissionTree> menuList = getMenuListByRoleIdList(roleList);
        // 返回报文
        out.setSuccessCode();
        out.setRoleIds(roleList);
        out.setMenuList(menuList);
    }

    public String converPath(String iconStr) {
        String resultUrl = "null.jpg";
        if (!org.springframework.util.StringUtils.isEmpty(iconStr)) {
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

}
