package com.zzccaidp.controller.ai;

import com.zzccaidp.dao.ai.TemplateInfoDO;
import com.zzccaidp.enums.ErrCodeEnum;
import com.zzccaidp.service.ai.TemplateInfoService;
import com.zzccaidp.vo.PageResponse;
import com.zzccaidp.vo.ResHeader;
import com.zzccaidp.vo.ai.TemplateInfoVO;
import com.zzccaidp.vo.ai.TemplateListVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/template")
@Slf4j
public class TemplateInfoController {

    @Autowired
    private TemplateInfoService templateInfoService;

    @PostMapping("/add")
    public ResHeader add(@RequestBody TemplateInfoVO templateInfoVo) {
        return templateInfoService.addTemplateInfoDO(templateInfoVo);
    }

    @PostMapping("/delete")
    public ResHeader delete(@RequestBody TemplateInfoVO templateInfoVo) {
        ResHeader response = new ResHeader();
        templateInfoService.deleteTemplateInfoDO(templateInfoVo);
        response.setSuccessCode();
        return response;
    }

    @PostMapping("/update")
    public ResHeader update(@RequestBody TemplateInfoVO templateInfoVo) {
        ResHeader response = new ResHeader();
        try {
            templateInfoService.updateTemplateInfoDO(templateInfoVo);
            response.setSuccessCode();
        } catch (Exception e) {
            response.setResultcode(ErrCodeEnum.M0019.getErrCode());
            response.setResultmsg(e.getMessage());
        }
        return response;
    }

    @PostMapping("/getList")
    public PageResponse<TemplateInfoDO> getAllTemplateInfos(@RequestBody TemplateInfoVO templateInfoVo) {
        return templateInfoService.getAllTemplateInfoDOs(templateInfoVo);
    }

    @PostMapping("/getAll")
    public TemplateListVO getAll() {
        return templateInfoService.getAllTemplateList();
    }

}


