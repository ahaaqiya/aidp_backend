package com.zzccaidp.mapper.system;

import com.zzccaidp.dao.system.PersonDO;
import com.zzccaidp.dao.bo.PersonInfoBO;
import com.zzccaidp.dao.system.RoleDO;
import com.zzccaidp.dao.system.UserDO;
import com.zzccaidp.dao.system.UserRoleDO;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;
import java.util.Map;

/**
 *
 * @author bades
 */
public interface PersonMapper extends Mapper<PersonDO> {

    /**
     * 查询所有在职人员信息
     * @return PersonInfoBO
     */
    List<PersonInfoBO>  selectPersonInfoAll(@Param("workCode") String workCode);

    PersonInfoBO  selectPersonInfoAllByWorkcode(@Param("workCode") String workCode);

    List<String> getOaPerWorkcodeListByStatus();

    List<String> getPerWorkcodeListByStatus();

    List<Map<String,Object>> listPersonByUserId(@Param("list") List<String> list);

    void insertUserForOa(@Param("list") List<String> list);

    void deleteUserForOa(@Param("list") List<String> list);

    List<PersonInfoBO>  selectUserByAuthType();

    PersonInfoBO  selectUserByWorkcode(@Param("workCode") String workCode);
}
