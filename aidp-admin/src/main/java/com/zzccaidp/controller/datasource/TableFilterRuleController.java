package com.zzccaidp.controller.datasource;

import com.zzccaidp.service.datasource.TableManageService;
import com.zzccaidp.vo.PageRequest;
import com.zzccaidp.vo.PageResponse;
import com.zzccaidp.vo.datasource.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * @author zhangtiantian
 * @date 2026/5/13
 */
@RestController
@RequestMapping("/datasource/tableFilterRule")
public class TableFilterRuleController {

    @Autowired
    private TableManageService tableManageService;

    @PostMapping(value = "/create")
    public CreateTableRuleResponse create(@RequestBody @Valid CreateTableRuleRequest createTableRuleRequest){
        return tableManageService.createTableRule(createTableRuleRequest);
    }

    @PostMapping(value = "/update")
    public UpdateTableRuleResponse update(@RequestBody @Valid UpdateTableRuleRequest updateTableRuleRequest){
        return tableManageService.updateTableRule(updateTableRuleRequest);
    }


    @PostMapping(value = "/delete/{ruleId}")
    public DeleteTableRuleResponse delete(@PathVariable String ruleId){
        return tableManageService.deleteTableRule(ruleId);
    }

    @PostMapping(value = "/list")
    public PageResponse<TableRuleListInfo> list(@RequestBody @Valid PageRequest<ListTableRuleParam> listTableRuleParamRequest){
        return tableManageService.listTableRule(listTableRuleParamRequest);
    }

}
