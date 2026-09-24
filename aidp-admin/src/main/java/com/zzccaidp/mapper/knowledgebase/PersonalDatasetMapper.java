package com.zzccaidp.mapper.knowledgebase;

import com.zzccaidp.dao.knowledgebase.PersonalDatasetDO;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

/**
 * 个人知识库数据集映射表 Mapper。
 * 继承通用 Mapper 提供 insertSelective/selectByPrimaryKey 等能力，
 * 自定义 selectByUserCode 按工号查询个人 dataset 映射。
 */
public interface PersonalDatasetMapper extends Mapper<PersonalDatasetDO> {

    /**
     * 按用户工号查询个人 dataset 映射
     *
     * @param userCode 用户工号
     * @return 映射记录，不存在时返回 null
     */
    PersonalDatasetDO selectByUserCode(@Param("userCode") String userCode);
}
