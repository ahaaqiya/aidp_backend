package com.zzccaidp.mapper.knowledgebase;

import com.zzccaidp.dao.knowledgebase.DocTypeDO;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface DocTypeMapper extends Mapper<DocTypeDO> {
    DocTypeDO selectById(String id);

    DocTypeDO selectByCode(String code);

    List<DocTypeDO> selectAll();

    List<DocTypeDO> searchByKeyword(String keyword);

    int insert(DocTypeDO docType);

    int update(DocTypeDO docType);

    int updateChunkMethod(@Param("id") String id, @Param("chunkMethod") String chunkMethod);

    int deleteById(@Param("id") String id);

    int countAll();

    boolean existsByCode(String code);

    List<DocTypeDO> getDocTypeListByDocId(@Param("docId") String docId);


}