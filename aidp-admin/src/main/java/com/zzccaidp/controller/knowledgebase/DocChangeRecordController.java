package com.zzccaidp.controller.knowledgebase;

import com.zzccaidp.service.knowledgebase.DocChangeRecordService;
import com.zzccaidp.vo.PageRequest;
import com.zzccaidp.vo.PageResponse;
import com.zzccaidp.vo.knowledgebase.DocChangeRecordIn;
import com.zzccaidp.vo.knowledgebase.DocChangeRecordOut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Description:
 * @Author: WB233500
 * @Createtime: 15:57
 * @Version: 1.0
 */
@RestController
@RequestMapping("/doc_change_record")
public class DocChangeRecordController {
    @Autowired
    private DocChangeRecordService docChangeRecordService;

    @PostMapping("/list")
    public PageResponse<DocChangeRecordOut> list(@RequestBody PageRequest<DocChangeRecordIn> pageRequest) {
        return docChangeRecordService.list(pageRequest);
    }

}
