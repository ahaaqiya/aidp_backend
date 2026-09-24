package com.zzccaidp.controller.system;

import com.zzccaidp.service.system.OrgService;
import com.zzccaidp.vo.PageRequest;
import com.zzccaidp.vo.PageResponse;
import com.zzccaidp.vo.ResHeader;
import com.zzccaidp.vo.system.OrgIn;
import com.zzccaidp.vo.system.OrgOut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Description: 机构管理
 * @Author: WB233500
 * @Createtime: 09:50
 * @Version: 1.0
 */

@RestController()
@RequestMapping("/org")
public class OrgController {

    @Autowired
    private OrgService orgService;

    @PostMapping("/list")
    public PageResponse<OrgOut> list(@RequestBody PageRequest<OrgIn> pageRequest) {
        return orgService.list(pageRequest);
    }

    @PostMapping("/add")
    public ResHeader add(@RequestBody OrgIn orgIn) {
        ResHeader out = new ResHeader();
        out.setSuccessCode();
        orgService.add(orgIn);
        return out;
    }

    @PostMapping("/delete")
    public ResHeader delete(@RequestBody OrgIn orgIn) {
        ResHeader out = new ResHeader();
        out.setSuccessCode();
        orgService.delete(orgIn);
        return out;
    }

    @PostMapping("/update")
    public ResHeader update(@RequestBody OrgIn orgIn) {
        ResHeader out = new ResHeader();
        out.setSuccessCode();
        orgService.update(orgIn);
        return out;
    }
}
