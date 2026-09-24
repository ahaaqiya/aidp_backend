package com.zzccaidp.mapper.system;

import com.zzccaidp.dao.system.UserDO;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * UpmsUserMapper实现
 *
 * @author bades
 */
public interface UserMapper extends Mapper<UserDO> {
    List<UserDO> selectUserAll(UserDO user);

    UserDO selectUserByUserName(UserDO userDO);

    void batchInsterUser(List<UserDO> userDOList);
}
