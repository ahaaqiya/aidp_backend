package com.zzccaidp.controller.knowledgebase;

import cn.hutool.core.bean.BeanUtil;
import com.zzccaidp.common.DateUtil;
import com.zzccaidp.dao.knowledgebase.DataSourceDO;
import com.zzccaidp.service.knowledgebase.DataSourceService;
import com.zzccaidp.service.knowledgebase.DocumentService;
import com.zzccaidp.vo.PageRequest;
import com.zzccaidp.vo.PageResponse;
import com.zzccaidp.vo.knowledgebase.DataSourceIn;
import com.zzccaidp.vo.knowledgebase.DataSourceOut;
import com.zzccaidp.vo.knowledgebase.DataSourceResponse;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.zzccaidp.constants.DateConstant.DATE_PATTERN_DIGIT;

@RestController
@RequestMapping("/data_source")
public class DataSourceController {

    @Autowired
    private DataSourceService dataSourceService;

    @Autowired
    private DocumentService documentService;

    @PostMapping("/list")
    public PageResponse<DataSourceOut> list(@RequestBody PageRequest<DataSourceIn> pageRequest) {
        PageResponse<DataSourceOut> page = new PageResponse<>();

        PageHelper.startPage(pageRequest.getPageNum(), pageRequest.getPageSize());
        List<DataSourceDO> dataSourceDOList = dataSourceService.findAll(pageRequest.getData() != null ? pageRequest.getData().getKeyword() : null)
                .stream()
                .peek(var -> {
                    var.setTypeDistribution(dataSourceService.selectTypeDistribution(var.getChannel()));
                    var.setDocumentCount(dataSourceService.selectDocCountByDocType(var.getChannel()));
                })
                .collect(Collectors.toList());
        List<DataSourceOut> dataSourceOutList = BeanUtil.copyToList(
                dataSourceDOList,
                DataSourceOut.class
        );

        PageInfo<DataSourceOut> pageInfo = new PageInfo<>(dataSourceOutList);
        page.setPageNum(pageRequest.getPageNum());
        page.setPageSize(pageRequest.getPageSize());
        page.setTotalPage(pageInfo.getPages());
        page.setTotalCount(pageInfo.getTotal());
        page.setRecords(dataSourceOutList);
        page.setSuccessCode();

        return page;
    }

    @GetMapping("/{id}")
    public DataSourceResponse getById(@PathVariable Long id) {
        DataSourceDO ds = dataSourceService.findById(id);
        if (ds == null) {
            return DataSourceResponse.failed("数据源不存在");
        }
        return DataSourceResponse.success(convertToOut(ds));
    }

    @PostMapping("/create")
    public DataSourceResponse create(@RequestBody DataSourceIn request) {
        DataSourceDO ds = dataSourceService.create(request);
        return DataSourceResponse.success("创建成功", convertToOut(ds));
    }

    @PostMapping("/update")
    public DataSourceResponse update(@RequestBody Map<String, Object> params) {
        Long id = ((Number) params.get("id")).longValue();
        DataSourceIn request = new DataSourceIn();
        request.setName((String) params.get("name"));
        request.setDescription((String) params.get("description"));
        request.setConfig((String) params.get("config"));
        DataSourceDO ds = dataSourceService.update(id, request);
        if (ds == null) {
            return DataSourceResponse.failed("数据源不存在");
        }
        return DataSourceResponse.success("更新成功", convertToOut(ds));
    }

    @PostMapping("/delete")
    public DataSourceResponse delete(@RequestBody Map<String, Object> params) {
        Long id = ((Number) params.get("id")).longValue();
        if (dataSourceService.delete(id)) {
            return DataSourceResponse.success("删除成功");
        }
        return DataSourceResponse.failed("删除失败，数据源不存在");
    }

    @PostMapping("/sync")
    public DataSourceResponse sync(@RequestBody Map<String, Object> params) {
        /*Long id = ((Number) params.get("id")).longValue();
        DataSourceDO ds = dataSourceService.sync(id);
        if (ds == null) {
            return DataSourceResponse.failed("数据源不存在");
        }*/
        String channel = ((String) params.get("channel"));
        if ("zzccXBGG".equals(channel)) {
            documentService.syncOaDocVerctor((String) params.get("date"));
        }
        return DataSourceResponse.success("同步启动成功");
    }

    @GetMapping("/search")
    public DataSourceResponse search(@RequestParam(required = false) String keyword) {
        List<DataSourceDO> result = dataSourceService.search(keyword);
        List<DataSourceOut> output = result.stream().map(this::convertToOut).collect(Collectors.toList());
        return DataSourceResponse.success(output);
    }

    @PostMapping("/repo/create")
    public DataSourceResponse createRepo(@RequestBody Map<String, Object> request) {
        DataSourceDO ds = dataSourceService.createRepo(request);
        return DataSourceResponse.success("资源库创建成功", convertToOut(ds));
    }

    private DataSourceOut convertToOut(DataSourceDO source) {
        DataSourceOut out = new DataSourceOut();
        out.setId(source.getId());
        out.setName(source.getName());
        out.setChannel(source.getChannel());
        out.setDescription(source.getDescription());
        out.setDocumentCount(source.getDocumentCount());
        out.setLastSyncTime(source.getLastSyncTime());
        out.setTypeDistribution(source.getTypeDistribution());
        out.setCreateTime(source.getCreateTime());
        out.setUpdateTime(source.getUpdateTime());
        out.setConfig(source.getConfig());
        return out;
    }
}