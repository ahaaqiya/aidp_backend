package com.zzccaidp.controller.knowledgebase;

import com.zzccaidp.service.knowledgebase.RagResourceService;
import com.zzccaidp.vo.PageRequest;
import com.zzccaidp.vo.PageResponse;
import com.zzccaidp.vo.ResHeader;
import com.zzccaidp.vo.knowledgebase.RagResourceIn;
import com.zzccaidp.vo.knowledgebase.RagResourceOut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Description:
 * @Author: WB233500
 * @Createtime: 09:16
 * @Version: 1.0
 */
@RestController()
@RequestMapping("/rag_resource")
public class RagResourceController {
    @Autowired
    private RagResourceService ragResourceService;

    @PostMapping("/list")
    public PageResponse<RagResourceOut> list(@RequestBody PageRequest<RagResourceIn> pageRequest) {
        return ragResourceService.list(pageRequest);
    }

    @PostMapping("/add")
    public ResHeader add(@RequestBody RagResourceIn ragResourceIn) {
        ResHeader out = new ResHeader();
        out.setSuccessCode();
        ragResourceService.add(ragResourceIn);
        return out;
    }

    @PostMapping("/delete")
    public ResHeader delete(@RequestBody RagResourceIn ragResourceIn) {
        ResHeader out = new ResHeader();
        out.setSuccessCode();
        ragResourceService.delete(ragResourceIn);
        return out;
    }

    @PostMapping("/update")
    public ResHeader update(@RequestBody RagResourceIn ragResourceIn) {
        ResHeader out = new ResHeader();
        out.setSuccessCode();
        ragResourceService.update(ragResourceIn);
        return out;
    }
}
