package com.zzccaidp.controller.datasource;

import com.zzccaidp.service.datasource.DatasourceManageService;
import com.zzccaidp.util.ResUtils;
import com.zzccaidp.vo.PageRequest;
import com.zzccaidp.vo.PageResponse;
import com.zzccaidp.vo.datasource.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * @author zhangtiantian
 * @date 2026/5/6
 */
@RestController
@RequestMapping("/datasource")
public class DatasourceManageController {

    @Autowired
    private DatasourceManageService datasourceManageService;

    @Value("${datasource.password.sm2.pubKey}")
    private String datasourcePasswordPubKey;

    @PostMapping(value = "/create")
    public CreateDatasourceResponse create(@RequestBody @Valid CreateDatasourceRequest createDatasourceRequest){
        return datasourceManageService.createDatasource(createDatasourceRequest);
    }

    @PostMapping(value = "/update")
    public UpdateDatasourceResponse update(@RequestBody @Valid UpdateDatasourceRequest updateDatasourceRequest){
        return datasourceManageService.updateDatasource(updateDatasourceRequest);
    }


    @PostMapping(value = "/delete/{datasourceId}")
    public DeleteDatasourceResponse delete(@PathVariable String datasourceId){
        return datasourceManageService.deleteDatasource(datasourceId);
    }

    @PostMapping(value = "/active/{datasourceId}")
    public ActiveDatasourceResponse active(@PathVariable String datasourceId, @RequestParam(required = true) String isEnabled){
        return datasourceManageService.active(datasourceId, isEnabled);
    }

    @PostMapping(value = "/list")
    public PageResponse<DatasourceListInfo> list(@RequestBody @Valid PageRequest<ListDatasourceParam> listDatasourceParamRequest){
        return datasourceManageService.listDatasource(listDatasourceParamRequest);
    }

    @GetMapping(value = "/getPublicKey")
    public GetPublicKeyResponse getPublicKey(){
        return ResUtils.success(new GetPublicKeyResponse(datasourcePasswordPubKey));
    }


}
