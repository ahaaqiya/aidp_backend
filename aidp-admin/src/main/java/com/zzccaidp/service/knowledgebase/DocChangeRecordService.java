package com.zzccaidp.service.knowledgebase;

import cn.hutool.core.bean.BeanUtil;
import com.zzccaidp.dao.knowledgebase.DocChangeRecordDO;
import com.zzccaidp.mapper.knowledgebase.DocChangeRecordMapper;
import com.zzccaidp.vo.PageRequest;
import com.zzccaidp.vo.PageResponse;
import com.zzccaidp.vo.knowledgebase.DocChangeRecordIn;
import com.zzccaidp.vo.knowledgebase.DocChangeRecordOut;
import com.zzccaidp.vo.knowledgebase.RagResourceIn;
import com.zzccaidp.vo.knowledgebase.RagResourceOut;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Description:
 * @Author: WB233500
 * @Createtime: 15:58
 * @Version: 1.0
 */
@Service
public class DocChangeRecordService {
    @Autowired
    private DocChangeRecordMapper docChangeRecordMapper;

    public PageResponse<DocChangeRecordOut> list(PageRequest<DocChangeRecordIn> pageRequest) {
        PageResponse<DocChangeRecordOut> page = new PageResponse<>();
        PageHelper.startPage(pageRequest.getPageNum(), pageRequest.getPageSize());
        List<DocChangeRecordOut> docChangeRecordOutList = BeanUtil.copyToList(docChangeRecordMapper.select(
                        BeanUtil.copyProperties(pageRequest.getData(), DocChangeRecordDO.class)),
                DocChangeRecordOut.class
        );
        PageInfo<DocChangeRecordOut> pageInfo = new PageInfo<>(docChangeRecordOutList);
        page.setTotalPage(pageInfo.getPages());
        page.setTotalCount(pageInfo.getTotal());
        page.setRecords(docChangeRecordOutList);
        page.setSuccessCode();
        return page;
    }

    public void addDocChangeRecord(DocChangeRecordDO docChangeRecordDO) {
        docChangeRecordMapper.insertSelective(docChangeRecordDO);
    }
}
