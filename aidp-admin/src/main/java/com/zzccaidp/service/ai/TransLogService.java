package com.zzccaidp.service.ai;

import com.zzccaidp.common.BeanUtils;
import com.zzccaidp.constants.DateConstant;
import com.zzccaidp.dao.ai.TransLog;
import com.zzccaidp.mapper.ai.TransLogMapper;
import com.zzccaidp.vo.PageResponse;
import com.zzccaidp.vo.ai.TransLogPageRes;
import com.zzccaidp.vo.ai.TransLogSearchReq;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * @author bades
 */
@Service
public class TransLogService {

    private TransLogMapper transLogMapper;

    public TransLogService(TransLogMapper transLogMapper) {
        this.transLogMapper = transLogMapper;
    }

    public void insertTransLogMapper(TransLog log) {
        transLogMapper.insert(log);
    }

    public PageResponse<TransLogPageRes> page(TransLogSearchReq searchReq) {
        //日期格式
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DateConstant.DATETIME_PATTERN_DEFAULT);
        PageResponse<TransLogPageRes> response = new PageResponse<>();
        List<TransLogPageRes> roleVoList = new ArrayList<>();
        Example example = new Example(TransLog.class);
        criteria(example, searchReq);
        // 分页查询交易信息
        PageHelper.startPage(searchReq.getPageNum(), searchReq.getPageSize(), true);
        List<TransLog> roleList = transLogMapper.selectByExample(example);
        roleList.forEach(log -> {
            TransLogPageRes logPageRes = BeanUtils.copy(log, new TransLogPageRes());
            logPageRes.setStartTime(log.getStartTime().format(formatter));
            // 修改日期格式--yyyy-MM-dd HH:mm:ss
            logPageRes.setEndTime(log.getEndTime().format(formatter));
            roleVoList.add(logPageRes);
        });
        PageInfo<TransLog> pageInfo = new PageInfo<>(roleList);
        response.setTotalPage(pageInfo.getPages());
        response.setTotalCount(pageInfo.getTotal());
        response.setRecords(roleVoList);
        response.setSuccess();
        return response;
    }

    public Example.Criteria criteria(Example example, TransLogSearchReq searchReq) {
        Example.Criteria criteria = example.createCriteria();
        example.orderBy(TransLog.F_START_TIME).desc();
        example.orderBy(TransLog.F_TRANS_FLOW_ID).desc();

        // 渠道号
        if (!searchReq.getChl().isEmpty()) {
            criteria.andLike(TransLog.F_CHL, searchReq.getChl());
        }
        // 场景服务号
        if (!searchReq.getRuleId().isEmpty()) {
            criteria.andLike(TransLog.F_RULD_ID, searchReq.getRuleId());
        }

        // 流水编号
        if (!searchReq.getTransFlowId().isEmpty()) {
            criteria.andLike(TransLog.F_TRANS_FLOW_ID, searchReq.getTransFlowId());
        }

        //开始日期
        if ((!searchReq.getStartTime().isEmpty()) && (!searchReq.getEndTime().isEmpty())) {
            criteria.andBetween(TransLog.F_START_TIME, searchReq.getStartTime(), searchReq.getEndTime());
        }
        return criteria;
    }

}
