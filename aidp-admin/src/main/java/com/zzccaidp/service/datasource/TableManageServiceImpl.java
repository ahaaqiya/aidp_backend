package com.zzccaidp.service.datasource;

import com.zzccaidp.common.SnowflakeUtil;
import com.zzccaidp.common.DateUtil;
import com.zzccaidp.context.UserInfoContextHolder;
import com.zzccaidp.dao.datasource.DataSourceConfigDO;
import com.zzccaidp.dao.datasource.TableConfigDO;
import com.zzccaidp.dao.datasource.TableFilterRuleDO;
import com.zzccaidp.dao.datasource.TablePermissionDO;
import com.zzccaidp.enums.ErrCodeEnum;
import com.zzccaidp.exception.ValidException;
import com.zzccaidp.mapper.datasource.DataSourceConfigMapper;
import com.zzccaidp.mapper.datasource.TableConfigMapper;
import com.zzccaidp.mapper.datasource.TableFilterRuleMapper;
import com.zzccaidp.mapper.datasource.TablePermissionMapper;
import com.zzccaidp.util.ResUtils;
import com.zzccaidp.util.StringUtil;
import com.zzccaidp.vo.PageRequest;
import com.zzccaidp.vo.PageResponse;
import com.zzccaidp.vo.datasource.*;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author zhangtiantian
 * @date 2026/5/6
 */
@Service
@Slf4j
public class TableManageServiceImpl implements TableManageService {

    @Autowired
    private TableConfigMapper tableConfigMapper;

    @Autowired
    private TableFilterRuleMapper tableFilterRuleMapper;

    @Autowired
    private DataSourceConfigMapper dataSourceConfigMapper;

    @Autowired
    private TablePermissionMapper tablePermissionMapper;

    @Override
    public CreateTableResponse createTable(CreateTableRequest createTableRequest) {
        // 参数校验
        this.validTableParam(createTableRequest, "");

        // 校验数据源存在性
        this.validDatasourceExist(createTableRequest.getDatasourceId());

        // 保存数据
        this.insertTable(createTableRequest);

        // 构建响应
        return ResUtils.success(CreateTableResponse.class);
    }


    @Override
    public UpdateTableResponse updateTable(UpdateTableRequest updateTableRequest) {
        // 参数校验
        this.validTableParam(updateTableRequest, updateTableRequest.getTableId());

        // 数据源存在性校验
        this.validDatasourceExist(updateTableRequest.getDatasourceId());

        // 表存在性校验
        TableConfigDO tableConfigDO = this.validTableExist(updateTableRequest.getTableId());

        // 更新数据
        this.updateTableConfig(tableConfigDO, updateTableRequest);

        // 构建响应
        return ResUtils.success(UpdateTableResponse.class);
    }

    @Override
    @Transactional
    public DeleteTableResponse deleteTable(String tableId) {
        // 存在性校验
        this.validTableExist(tableId);

        // 删除
        tableConfigMapper.deleteByPrimaryKey(tableId);

        // 删除权限
        this.deleteTablePermissionByTableId(tableId);

        // 删除规则
        this.deleteTableRuleByTableId(tableId);

        // 构建响应
        return ResUtils.success(DeleteTableResponse.class);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void clearTableByDatasourceId(String datasourceId) {
        if (StringUtil.isBlank(datasourceId)) {
            return;
        }
        // 删除表
        this.deleteTableByDatasourceId(datasourceId);
        // 删除权限
        this.deleteTablePermissionByDatasourceId(datasourceId);
        // 删除规则
        this.deleteTableRuleByDatasourceId(datasourceId);
        Example example = new Example(TableConfigDO.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("datasourceId", datasourceId);
        tablePermissionMapper.deleteByExample(criteria);
    }


    @Override
    public PageResponse<TableListInfo> listTable(PageRequest<ListTableParam> listTableParamRequest) {
        // 分页查询
        Page<TableConfigDO> tableConfigDOPage = PageHelper.startPage(listTableParamRequest.getPageNum(), listTableParamRequest.getPageSize())
                .doSelectPage(() -> tableConfigMapper.listTable(listTableParamRequest.getData()));

        // 结果集构建返回
        return this.buildTablePageResponse(tableConfigDOPage);
    }

    @Override
    public GetTablePermissionResponse getPermission(String tableId) {
        // 表存在性校验
        this.validTableExist(tableId);

        // 获取表权限信息
        List<TablePermissionDO> tablePermissionDOList = this.getPermissionInfo(tableId);

        // 信息组装
        return this.buildTablePermissionResponse(tableId, tablePermissionDOList);
    }


    @Override
    public SaveTablePermissionResponse savePermission(SaveTablePermissionRequest saveTablePermissionRequest) {
        // 数据源存在性校验
        this.validDatasourceExist(saveTablePermissionRequest.getDatasourceId());

        // 表存在性校验
        this.validTableExist(saveTablePermissionRequest.getTableId());

        // 权限清空
        this.clearPermission(saveTablePermissionRequest.getTableId());

        // 权限保存
        this.batchInsertPermissionInfo(saveTablePermissionRequest);

        return ResUtils.success(SaveTablePermissionResponse.class);
    }

    @Override
    public CreateTableRuleResponse createTableRule(CreateTableRuleRequest createTableRuleRequest) {
        // 参数校验
        this.validTableRuleParam(createTableRuleRequest, "");

        // 数据源存在性校验
        this.validDatasourceExist(createTableRuleRequest.getDatasourceId());

        // 表存在性校验
        this.validTableExist(createTableRuleRequest.getTableId());

        // 保存数据
        this.insertTableRule(createTableRuleRequest);

        // 构建响应
        return ResUtils.success(CreateTableRuleResponse.class);
    }


    @Override
    public UpdateTableRuleResponse updateTableRule(UpdateTableRuleRequest updateTableRuleRequest) {
        // 参数校验
        this.validTableRuleParam(updateTableRuleRequest, updateTableRuleRequest.getTableId());

        // 数据源存在性校验
        this.validDatasourceExist(updateTableRuleRequest.getDatasourceId());

        // 表存在性校验
        this.validTableExist(updateTableRuleRequest.getTableId());

        // 规则存在性校验
        TableFilterRuleDO tableFilterRuleDO = this.validTableRuleExist(updateTableRuleRequest.getRuleId());

        // 更新数据
        this.updateTableRuleConfig(tableFilterRuleDO, updateTableRuleRequest);

        // 构建响应
        return ResUtils.success(UpdateTableRuleResponse.class);
    }


    @Override
    public DeleteTableRuleResponse deleteTableRule(String ruleId) {
        // 存在性校验
        this.validTableRuleExist(ruleId);

        // 删除
        tableFilterRuleMapper.deleteByPrimaryKey(ruleId);

        // 删除表

        // 构建响应
        return ResUtils.success(DeleteTableRuleResponse.class);
    }

    @Override
    public PageResponse<TableRuleListInfo> listTableRule(PageRequest<ListTableRuleParam> listTableRuleParamRequest) {
        // 分页查询
        Page<TableFilterRuleDO> tableFilterRuleDOPage = PageHelper.startPage(listTableRuleParamRequest.getPageNum(), listTableRuleParamRequest.getPageSize())
                .doSelectPage(() -> tableFilterRuleMapper.listTableRule(listTableRuleParamRequest.getData()));

        // 结果集构建返回
        return this.buildTableRulePageResponse(tableFilterRuleDOPage);
    }


    public void clearPermission(String tableId) {
        Example example = new Example(TablePermissionDO.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("tableId", tableId);
        tablePermissionMapper.deleteByExample(example);
    }

    private void batchInsertPermissionInfo(SaveTablePermissionRequest saveTablePermissionRequest) {
        if (CollectionUtils.isEmpty(saveTablePermissionRequest.getTablePermissionInfoList())) {
            return;
        }
        List<TablePermissionDO> tablePermissionDOList = saveTablePermissionRequest.getTablePermissionInfoList()
                .stream().map(tablePermissionInfo -> {
                    TablePermissionDO tablePermissionDO = new TablePermissionDO();
                    tablePermissionDO.setCreateTime(new Date());
                    tablePermissionDO.setDatasourceId(saveTablePermissionRequest.getDatasourceId());
                    tablePermissionDO.setTableId(saveTablePermissionRequest.getTableId());
                    tablePermissionDO.setPrincipalType(tablePermissionInfo.getPrincipalType());
                    tablePermissionDO.setPrincipalId(tablePermissionInfo.getPrincipalId());
                    tablePermissionDO.setCreateUser(UserInfoContextHolder.getUserInfo());
                    tablePermissionDO.setId(String.valueOf(SnowflakeUtil.nextId()));
                    return tablePermissionDO;
                }).collect(Collectors.toList());

        tablePermissionMapper.batchInsert(tablePermissionDOList);
    }


    private PageResponse<TableListInfo> buildTablePageResponse(Page<TableConfigDO> tableConfigDOPage) {
        // 分页结果构建
        PageResponse<TableListInfo> pageResponse = ResUtils.buildPageRes(TableListInfo.class, tableConfigDOPage);

        // 无结果集
        if (CollectionUtils.isEmpty(tableConfigDOPage.getResult())) {
            pageResponse.setRecords(new ArrayList<>());
            return pageResponse;
        }

        pageResponse.setRecords(tableConfigDOPage.getResult().stream().map(tableConfigDO -> {
            TableListInfo tableListInfo = new TableListInfo();
            tableListInfo.setTableId(tableConfigDO.getId());
            tableListInfo.setTableName(tableConfigDO.getTableName());
            tableListInfo.setTableDescription(tableConfigDO.getTableDescription());
            tableListInfo.setCreateTime(DateUtil.getDateString(tableConfigDO.getCreateTime()));
            tableListInfo.setUpdateTime(DateUtil.getDateString(tableConfigDO.getUpdateTime()));
            return tableListInfo;
        }).collect(Collectors.toList()));
        return pageResponse;
    }


    private PageResponse<TableRuleListInfo> buildTableRulePageResponse(Page<TableFilterRuleDO> tableFilterRuleDOPage) {
        // 分页结果构建
        PageResponse<TableRuleListInfo> pageResponse = ResUtils.buildPageRes(TableRuleListInfo.class, tableFilterRuleDOPage);

        // 无结果集
        if (CollectionUtils.isEmpty(tableFilterRuleDOPage.getResult())) {
            pageResponse.setRecords(new ArrayList<>());
            return pageResponse;
        }

        pageResponse.setRecords(tableFilterRuleDOPage.getResult().stream().map(tableFilterRuleDO -> {
            TableRuleListInfo tableRuleListInfo = new TableRuleListInfo();
            tableRuleListInfo.setTableId(tableFilterRuleDO.getTableId());
            tableRuleListInfo.setDatasourceId(tableFilterRuleDO.getDatasourceId());
            tableRuleListInfo.setRuleName(tableFilterRuleDO.getRuleName());
            tableRuleListInfo.setRuleDescription(tableFilterRuleDO.getRuleDescription());
            tableRuleListInfo.setRuleCondition(tableFilterRuleDO.getRuleCondition());
            tableRuleListInfo.setCreateTime(DateUtil.getDateString(tableFilterRuleDO.getCreateTime()));
            tableRuleListInfo.setUpdateTime(DateUtil.getDateString(tableFilterRuleDO.getUpdateTime()));
            tableRuleListInfo.setRuleId(tableFilterRuleDO.getId());
            return tableRuleListInfo;
        }).collect(Collectors.toList()));
        return pageResponse;
    }


    private void insertTable(CreateTableRequest createTableRequest) {
        TableConfigDO tableConfigDO = new TableConfigDO();
        tableConfigDO.setId(String.valueOf(SnowflakeUtil.nextId()));
        tableConfigDO.setDatasourceId(createTableRequest.getDatasourceId());
        tableConfigDO.setTableName(createTableRequest.getTableName());
        tableConfigDO.setTableDescription(createTableRequest.getTableDescription());
        tableConfigDO.setCreateUser(UserInfoContextHolder.getUserInfo());
        tableConfigDO.setUpdateUser(UserInfoContextHolder.getUserInfo());
        tableConfigDO.setCreateTime(new Date());
        tableConfigDO.setUpdateTime(new Date());
        tableConfigMapper.insert(tableConfigDO);
    }


    private void insertTableRule(CreateTableRuleRequest createTableRuleRequest) {
        TableFilterRuleDO tableFilterRuleDO = new TableFilterRuleDO();
        tableFilterRuleDO.setId(String.valueOf(SnowflakeUtil.nextId()));
        tableFilterRuleDO.setDatasourceId(createTableRuleRequest.getDatasourceId());
        tableFilterRuleDO.setTableId(createTableRuleRequest.getTableId());
        tableFilterRuleDO.setRuleCondition(createTableRuleRequest.getRuleCondition());
        tableFilterRuleDO.setRuleName(createTableRuleRequest.getRuleName());
        tableFilterRuleDO.setRuleDescription(createTableRuleRequest.getRuleDescription());
        tableFilterRuleDO.setCreateUser(UserInfoContextHolder.getUserInfo());
        tableFilterRuleDO.setUpdateUser(UserInfoContextHolder.getUserInfo());
        tableFilterRuleDO.setCreateTime(new Date());
        tableFilterRuleDO.setUpdateTime(new Date());
        tableFilterRuleMapper.insert(tableFilterRuleDO);
    }

    private void validTableParam(CreateTableRequest createTableRequest, String tableId) {
        Example nameExample = new Example(TableConfigDO.class);
        Example.Criteria nameCriteria = nameExample.createCriteria();
        nameCriteria.andEqualTo("tableName", createTableRequest.getTableName());
        nameCriteria.andEqualTo("datasourceId", createTableRequest.getDatasourceId());
        nameCriteria.andNotEqualTo("id", tableId);
        List<TableConfigDO> tableConfigDOList = tableConfigMapper.selectByExample(nameExample);
        if (!CollectionUtils.isEmpty(tableConfigDOList)) {
            log.error("表名称已被使用, {}", tableConfigDOList);
            throw new ValidException(ErrCodeEnum.M8004);
        }
    }


    private void validTableRuleParam(CreateTableRuleRequest createTableRuleRequest, String ruleId) {
        Example nameExample = new Example(TableFilterRuleDO.class);
        Example.Criteria nameCriteria = nameExample.createCriteria();
        nameCriteria.andEqualTo("ruleName", createTableRuleRequest.getRuleName());
        nameCriteria.andEqualTo("datasourceId", createTableRuleRequest.getTableId());
        nameCriteria.andNotEqualTo("id", ruleId);
        List<TableFilterRuleDO> tableFilterRuleDOList = tableFilterRuleMapper.selectByExample(nameExample);
        if (!CollectionUtils.isEmpty(tableFilterRuleDOList)) {
            log.error("表规则名称已被使用, {}", tableFilterRuleDOList);
            throw new ValidException(ErrCodeEnum.M8006);
        }
    }

    private void validDatasourceExist(String dataSourceId) {
        DataSourceConfigDO dataSourceConfigDO = dataSourceConfigMapper.selectByPrimaryKey(dataSourceId);
        if (ObjectUtils.isEmpty(dataSourceConfigDO)) {
            log.error("数据源不存在:{}", dataSourceId);
            throw new ValidException(ErrCodeEnum.M8002);
        }
    }

    private TableFilterRuleDO validTableRuleExist(String ruleId) {
        TableFilterRuleDO tableFilterRuleDO = tableFilterRuleMapper.selectByPrimaryKey(ruleId);
        if (ObjectUtils.isEmpty(tableFilterRuleDO)) {
            log.error("数据源不存在:{}", ruleId);
            throw new ValidException(ErrCodeEnum.M8007);
        }
        return tableFilterRuleDO;
    }


    private void updateTableConfig(TableConfigDO tableConfigDO, UpdateTableRequest updateTableRequest) {
        tableConfigDO.setTableName(updateTableRequest.getTableName());
        tableConfigDO.setTableDescription(updateTableRequest.getTableDescription());
        tableConfigDO.setUpdateUser(UserInfoContextHolder.getUserInfo());
        tableConfigDO.setUpdateTime(new Date());
        tableConfigMapper.updateByPrimaryKeySelective(tableConfigDO);
    }

    private void updateTableRuleConfig(TableFilterRuleDO tableFilterRuleDO, UpdateTableRuleRequest updateTableRuleRequest) {
        tableFilterRuleDO.setRuleName(updateTableRuleRequest.getRuleName());
        tableFilterRuleDO.setRuleCondition(updateTableRuleRequest.getRuleCondition());
        tableFilterRuleDO.setRuleDescription(updateTableRuleRequest.getRuleDescription());
        tableFilterRuleDO.setUpdateUser(UserInfoContextHolder.getUserInfo());
        tableFilterRuleDO.setUpdateTime(new Date());
        tableFilterRuleMapper.updateByPrimaryKeySelective(tableFilterRuleDO);
    }

    private TableConfigDO validTableExist(String tableId) {
        TableConfigDO tableConfigDO = tableConfigMapper.selectByPrimaryKey(tableId);
        if (ObjectUtils.isEmpty(tableConfigDO)) {
            log.error("表不存在:{}", tableId);
            throw new ValidException(ErrCodeEnum.M8005);
        }
        return tableConfigDO;
    }


    private GetTablePermissionResponse buildTablePermissionResponse(String tableId, List<TablePermissionDO> tablePermissionDOList) {
        // 初始化响应
        GetTablePermissionResponse getTablePermissionResponse = new GetTablePermissionResponse();
        getTablePermissionResponse.setTableId(tableId);

        // 空集合
        if (CollectionUtils.isEmpty(tablePermissionDOList)) {
            getTablePermissionResponse.setTablePermissionInfoList(new ArrayList<>());
            return ResUtils.success(getTablePermissionResponse);
        }

        // 对象转换
        List<TablePermissionInfo> tablePermissionInfoList = tablePermissionDOList.stream().map(tablePermissionDO -> {
            TablePermissionInfo tablePermissionInfo = new TablePermissionInfo();
            tablePermissionInfo.setPrincipalId(tablePermissionDO.getPrincipalId());
            tablePermissionInfo.setPrincipalType(tablePermissionDO.getPrincipalType());
            return tablePermissionInfo;
        }).collect(Collectors.toList());
        getTablePermissionResponse.setTablePermissionInfoList(tablePermissionInfoList);
        return ResUtils.success(getTablePermissionResponse);
    }

    private List<TablePermissionDO> getPermissionInfo(String tableId) {
        Example example = new Example(TablePermissionDO.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("tableId", tableId);
        return tablePermissionMapper.selectByExample(example);
    }

    private void deleteTableByDatasourceId(String dataSourceId) {
        Example example = new Example(TableConfigDO.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("datasourceId", dataSourceId);
        tablePermissionMapper.deleteByExample(example);
    }

    private void deleteTablePermissionByDatasourceId(String datasourceId) {
        Example example = new Example(TablePermissionDO.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("datasourceId", datasourceId);
        tablePermissionMapper.deleteByExample(example);
    }

    private void deleteTableRuleByDatasourceId(String datasourceId) {
        Example example = new Example(TableFilterRuleDO.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("datasourceId", datasourceId);
        tableFilterRuleMapper.deleteByExample(example);
    }


    private void deleteTableRuleByTableId(String tableId) {
        Example example = new Example(TableFilterRuleDO.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("tableId", tableId);
        tableFilterRuleMapper.deleteByExample(example);
    }

    private void deleteTablePermissionByTableId(String tableId) {
        Example example = new Example(TablePermissionDO.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("tableId", tableId);
        tablePermissionMapper.deleteByExample(example);
    }
}
