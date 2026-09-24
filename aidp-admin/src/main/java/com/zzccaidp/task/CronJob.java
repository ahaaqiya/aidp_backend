package com.zzccaidp.task;

import com.zzccaidp.common.DateUtil;
import com.zzccaidp.mapper.system.PersonMapper;
import com.zzccaidp.service.knowledgebase.DocumentService;
import com.zzccaidp.service.system.PersonService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static com.zzccaidp.constants.DateConstant.DATE_PATTERN_DIGIT;

@Component
@EnableScheduling
@Slf4j
public class CronJob {

    @Autowired
    private PersonService personService;
    @Autowired
    private PersonMapper personMapper;
    @Autowired
    private DocumentService documentService;

    /**
     * 对OA的人员数据进行同步
     * 每天凌晨 2点触发一次
     */
    //@Scheduled(cron = "0/10 * * * * ?")
    @Scheduled(cron = "0 0 4 * * ?")
    public void syncOaPersonTask() throws InterruptedException {
        // 查出当天OA同步的人员数据
        List<String> list1 = personMapper.getOaPerWorkcodeListByStatus();

        // 查出AIDP库中现有的OA人员数据
        List<String> list2 = personMapper.getPerWorkcodeListByStatus();

        List<String> insertList = new ArrayList<>(list1);
        insertList.removeAll(list2);

        List<String> deleteList = new ArrayList<>(list2);
        deleteList.removeAll(list1);

        // 筛选出新入职的人员
        personService.insertUserForOa(insertList);

        // 筛选出离职的人员
        personService.deleteUserForOa(deleteList);
    }

    /**
     * 同步OA发文文档向量化
     *
     *
     */
    @Scheduled(cron = "0 30 2 * * ?")
    public void syncOaDocVerctor() {
        documentService.syncOaDocVerctor(DateUtil.getSysDate(DATE_PATTERN_DIGIT));
    }
}
