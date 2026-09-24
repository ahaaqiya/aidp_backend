package com.zzccaidp.service.datasource;

import com.zzccaidp.vo.PageRequest;
import com.zzccaidp.vo.PageResponse;
import com.zzccaidp.vo.datasource.*;

/**
 * @author zhangtiantian
 * @date 2026/5/6
 */
public interface DatasourceManageService {

    CreateDatasourceResponse createDatasource(CreateDatasourceRequest createDatasourceRequest);

    UpdateDatasourceResponse updateDatasource(UpdateDatasourceRequest updateDatasourceRequest);

    DeleteDatasourceResponse deleteDatasource(String datasourceId);

    ActiveDatasourceResponse active(String datasourceId, String isEnabled);

    PageResponse<DatasourceListInfo> listDatasource(PageRequest<ListDatasourceParam> listDatasourceParamRequest);
}
