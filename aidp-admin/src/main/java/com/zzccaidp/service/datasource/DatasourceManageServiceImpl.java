package com.zzccaidp.service.datasource;

import com.zzccaidp.common.SnowflakeUtil;
import com.zzccaidp.common.DateUtil;
import com.zzccaidp.context.UserInfoContextHolder;
import com.zzccaidp.dao.datasource.DataSourceConfigDO;
import com.zzccaidp.enums.ErrCodeEnum;
import com.zzccaidp.exception.ValidException;
import com.zzccaidp.mapper.datasource.DataSourceConfigMapper;
import com.zzccaidp.util.ResUtils;
import com.zzccaidp.vo.PageRequest;
import com.zzccaidp.vo.PageResponse;
import com.zzccaidp.vo.datasource.*;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author zhangtiantian
 * @date 2026/5/6
 */
@Slf4j
@Service
public class DatasourceManageServiceImpl implements DatasourceManageService {

    @Autowired
    private DataSourceConfigMapper dataSourceConfigMapper;

    @Autowired
    private TableManageService tableManageService;


    @Override
    public CreateDatasourceResponse createDatasource(CreateDatasourceRequest createDatasourceRequest) {
        // 参数校验
        this.validParam(createDatasourceRequest, "");

        // 保存数据
        this.insertDatasource(createDatasourceRequest);

        // 构建响应
        return ResUtils.success(CreateDatasourceResponse.class);
    }


    @Override
    public UpdateDatasourceResponse updateDatasource(UpdateDatasourceRequest updateDatasourceRequest) {
        // 参数校验
        this.validParam(updateDatasourceRequest, updateDatasourceRequest.getDatasourceId());

        // 存在性校验
        DataSourceConfigDO dataSourceConfigDO = this.validExist(updateDatasourceRequest.getDatasourceId());

        // 更新数据
        this.updateDataSourceConfig(dataSourceConfigDO, updateDatasourceRequest);

        // 构建响应
        return ResUtils.success(UpdateDatasourceResponse.class);
    }


    @Override
    @Transactional
    public DeleteDatasourceResponse deleteDatasource(String datasourceId) {
        // 存在性校验
        this.validExist(datasourceId);

        // 删除数据源
        dataSourceConfigMapper.deleteByPrimaryKey(datasourceId);

        // 删除表
        tableManageService.clearTableByDatasourceId(datasourceId);

        // 构建响应
        return ResUtils.success(DeleteDatasourceResponse.class);
    }


    @Override
    public ActiveDatasourceResponse active(String datasourceId, String isEnabled) {
        // 存在性校验
        DataSourceConfigDO dataSourceConfigDO = this.validExist(datasourceId);

        // 状态更新
        this.activeDatasourceConfig(dataSourceConfigDO, isEnabled);

        // 构建响应
        return ResUtils.success(ActiveDatasourceResponse.class);

    }

    @Override
    public PageResponse<DatasourceListInfo> listDatasource(PageRequest<ListDatasourceParam> listDatasourceParamRequest) {
        // 分页查询
        Page<DataSourceConfigDO> dataSourceConfigDOPage = PageHelper.startPage(listDatasourceParamRequest.getPageNum(), listDatasourceParamRequest.getPageSize())
                .doSelectPage(() -> dataSourceConfigMapper.listDatasource(listDatasourceParamRequest.getData()));

        // 结果集构建返回
        return this.buildPageResponse(dataSourceConfigDOPage);
    }

    private PageResponse<DatasourceListInfo> buildPageResponse(Page<DataSourceConfigDO> dataSourceConfigDOPage) {
        // 分页结果构建
        PageResponse<DatasourceListInfo> pageResponse = ResUtils.buildPageRes(DatasourceListInfo.class, dataSourceConfigDOPage);

        // 无结果集
        if (CollectionUtils.isEmpty(dataSourceConfigDOPage.getResult())) {
            pageResponse.setRecords(new ArrayList<>());
            return pageResponse;
        }

        pageResponse.setRecords(dataSourceConfigDOPage.getResult().stream().map(dataSourceConfigDO -> {
            DatasourceListInfo datasourceListInfo = new DatasourceListInfo();
            datasourceListInfo.setDatasourceDescription(dataSourceConfigDO.getDatasourceDescription());
            datasourceListInfo.setDatasourceDatabase(dataSourceConfigDO.getDatasourceDatabase());
            datasourceListInfo.setCreateTime(DateUtil.getDateString(dataSourceConfigDO.getCreateTime()));
            datasourceListInfo.setUpdateTime(DateUtil.getDateString(dataSourceConfigDO.getUpdateTime()));
            datasourceListInfo.setDatasourceHost(dataSourceConfigDO.getDatasourceHost());
            datasourceListInfo.setDatasourceId(dataSourceConfigDO.getId());
            datasourceListInfo.setDatasourcePort(dataSourceConfigDO.getDatasourcePort());
            datasourceListInfo.setDatasourceUser(dataSourceConfigDO.getDatasourceUser());
            datasourceListInfo.setDatasourceName(dataSourceConfigDO.getDatasourceName());
            datasourceListInfo.setDatasourceType(dataSourceConfigDO.getDatasourceType());
            return datasourceListInfo;
        }).collect(Collectors.toList()));
        return pageResponse;
    }


    private void insertDatasource(CreateDatasourceRequest createDatasourceRequest) {
        DataSourceConfigDO dataSourceConfigDO = new DataSourceConfigDO();
        dataSourceConfigDO.setId(String.valueOf(SnowflakeUtil.nextId()));
        dataSourceConfigDO.setDatasourceDatabase(createDatasourceRequest.getDatasourceDatabase());
        dataSourceConfigDO.setDatasourceDescription(createDatasourceRequest.getDatasourceDescription());
        dataSourceConfigDO.setDatasourcePort(createDatasourceRequest.getDatasourcePort());
        dataSourceConfigDO.setDatasourceName(createDatasourceRequest.getDatasourceName());
        dataSourceConfigDO.setDatasourceHost(createDatasourceRequest.getDatasourceHost());
        dataSourceConfigDO.setDatasourcePassword(createDatasourceRequest.getDatasourcePassword());
        dataSourceConfigDO.setDatasourceUser(createDatasourceRequest.getDatasourceUser());
        dataSourceConfigDO.setDatasourceType(createDatasourceRequest.getDatasourceType());
        dataSourceConfigDO.setIsEnabled("1");
        dataSourceConfigDO.setCreateUser(UserInfoContextHolder.getUserInfo());
        dataSourceConfigDO.setUpdateUser(UserInfoContextHolder.getUserInfo());
        dataSourceConfigDO.setCreateTime(new Date());
        dataSourceConfigDO.setUpdateTime(new Date());
        dataSourceConfigMapper.insert(dataSourceConfigDO);

    }

    private void validParam(CreateDatasourceRequest createDatasourceRequest, String datasourceId) {
        Example nameExample = new Example(DataSourceConfigDO.class);
        Example.Criteria nameCriteria = nameExample.createCriteria();
        nameCriteria.andEqualTo("datasourceName", createDatasourceRequest.getDatasourceName());
        nameCriteria.andNotEqualTo("id", datasourceId);
        List<DataSourceConfigDO> dataSourceConfigDOList = dataSourceConfigMapper.selectByExample(nameExample);
        if (!CollectionUtils.isEmpty(dataSourceConfigDOList)) {
            log.error("数据源名称名称已被使用, {}", dataSourceConfigDOList);
            throw new ValidException(ErrCodeEnum.M8001);
        }
    }


    private DataSourceConfigDO validExist(String dataSourceId) {
        DataSourceConfigDO dataSourceConfigDO = dataSourceConfigMapper.selectByPrimaryKey(dataSourceId);
        if (ObjectUtils.isEmpty(dataSourceConfigDO)) {
            log.error("数据源不存在:{}", dataSourceId);
            throw new ValidException(ErrCodeEnum.M8002);
        }
        return dataSourceConfigDO;
    }

    private void updateDataSourceConfig(DataSourceConfigDO dataSourceConfigDO, UpdateDatasourceRequest updateDatasourceRequest) {
        dataSourceConfigDO.setDatasourcePort(updateDatasourceRequest.getDatasourcePort());
        dataSourceConfigDO.setDatasourceHost(updateDatasourceRequest.getDatasourceHost());
        dataSourceConfigDO.setDatasourcePassword(updateDatasourceRequest.getDatasourcePassword());
        dataSourceConfigDO.setDatasourceUser(updateDatasourceRequest.getDatasourceUser());
        dataSourceConfigDO.setDatasourceDatabase(updateDatasourceRequest.getDatasourceDatabase());
        dataSourceConfigDO.setDatasourceDescription(updateDatasourceRequest.getDatasourceDescription());
        dataSourceConfigDO.setDatasourceName(updateDatasourceRequest.getDatasourceName());
        dataSourceConfigDO.setUpdateUser(UserInfoContextHolder.getUserInfo());
        dataSourceConfigDO.setUpdateTime(new Date());
        dataSourceConfigMapper.updateByPrimaryKeySelective(dataSourceConfigDO);
    }


    private void activeDatasourceConfig(DataSourceConfigDO dataSourceConfigDO, String isEnabled) {
        dataSourceConfigDO.setIsEnabled(isEnabled);
        dataSourceConfigDO.setUpdateUser(UserInfoContextHolder.getUserInfo());
        dataSourceConfigDO.setUpdateTime(new Date());
        dataSourceConfigDO.setIsEnabled(isEnabled);
        dataSourceConfigMapper.updateByPrimaryKeySelective(dataSourceConfigDO);
    }
}
