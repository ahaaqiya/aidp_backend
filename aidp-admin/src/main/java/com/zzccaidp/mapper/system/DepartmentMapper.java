package com.zzccaidp.mapper.system;

import com.zzccaidp.dao.bo.DepartmentBO;
import com.zzccaidp.dao.system.DepartmentDO;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/** 部门Mapper
 * @author zhangtiantian
 * @date 2025/4/1
 */
public interface DepartmentMapper extends Mapper<DepartmentDO> {

    List<DepartmentBO> selectDeptAllNew(@Param("deptId") String deptId);

}
