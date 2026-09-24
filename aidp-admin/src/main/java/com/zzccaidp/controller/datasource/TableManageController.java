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
 * @date 2026/5/6
 */
@RestController
@RequestMapping("/datasource/table")
public class TableManageController {

    @Autowired
    private TableManageService tableManageService;

    @PostMapping(value = "/create")
    public CreateTableResponse create(@RequestBody @Valid CreateTableRequest createTableRequest){
        return tableManageService.createTable(createTableRequest);
    }

    @PostMapping(value = "/update")
    public UpdateTableResponse update(@RequestBody @Valid UpdateTableRequest updateTableRequest){
        return tableManageService.updateTable(updateTableRequest);
    }


    @PostMapping(value = "/delete/{tableId}")
    public DeleteTableResponse delete(@PathVariable String tableId){
        return tableManageService.deleteTable(tableId);
    }

    @PostMapping(value = "/list")
    public PageResponse<TableListInfo> list(@RequestBody @Valid PageRequest<ListTableParam> listTableParamRequest){
        return tableManageService.listTable(listTableParamRequest);
    }

    @GetMapping(value = "/getPermission/{tableId}")
    public GetTablePermissionResponse getPermission(@PathVariable String tableId){
        return tableManageService.getPermission(tableId);
    }

    @PostMapping(value = "/savePermission")
    public SaveTablePermissionResponse savePermission(@RequestBody SaveTablePermissionRequest saveTablePermissionRequest){
        return tableManageService.savePermission(saveTablePermissionRequest);
    }
}


