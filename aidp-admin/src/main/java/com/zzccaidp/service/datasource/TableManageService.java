package com.zzccaidp.service.datasource;

import com.zzccaidp.vo.PageRequest;
import com.zzccaidp.vo.PageResponse;
import com.zzccaidp.vo.datasource.*;

/**
 * @author zhangtiantian
 * @date 2026/5/6
 */
public interface TableManageService {

    CreateTableResponse createTable(CreateTableRequest createTableRequest);

    UpdateTableResponse updateTable(UpdateTableRequest updateTableRequest);

    DeleteTableResponse deleteTable(String tableId);

    void clearTableByDatasourceId(String datasourceId);

    PageResponse<TableListInfo> listTable(PageRequest<ListTableParam> listTableParamRequest);

    GetTablePermissionResponse getPermission(String tableId);

    SaveTablePermissionResponse savePermission(SaveTablePermissionRequest saveTablePermissionRequest);

    CreateTableRuleResponse createTableRule(CreateTableRuleRequest createTableRuleRequest);

    UpdateTableRuleResponse updateTableRule(UpdateTableRuleRequest updateTableRuleRequest);

    DeleteTableRuleResponse deleteTableRule(String ruleId);

    PageResponse<TableRuleListInfo> listTableRule(PageRequest<ListTableRuleParam> listTableRuleParamRequest);
}
