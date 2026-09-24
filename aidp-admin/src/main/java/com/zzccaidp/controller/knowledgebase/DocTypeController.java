package com.zzccaidp.controller.knowledgebase;

import cn.hutool.core.bean.BeanUtil;
import com.zzccaidp.dao.knowledgebase.DocTypeDO;
import com.zzccaidp.mapper.knowledgebase.DocumentMapper;
import com.zzccaidp.service.knowledgebase.DocTypeService;
import com.zzccaidp.vo.PageRequest;
import com.zzccaidp.vo.PageResponse;
import com.zzccaidp.vo.knowledgebase.DocTypeIn;
import com.zzccaidp.vo.knowledgebase.DocTypeOut;
import com.zzccaidp.vo.knowledgebase.DocTypeResponse;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/doc_type")
public class DocTypeController {

    @Autowired
    private DocTypeService docTypeService;

    @PostMapping("/list")
    public PageResponse<DocTypeOut> list(@RequestBody PageRequest<DocTypeIn> pageRequest) {
        PageResponse<DocTypeOut> page = new PageResponse<>();

        PageHelper.startPage(pageRequest.getPageNum(), pageRequest.getPageSize());
        List<DocTypeDO> docTypeDOList = docTypeService.findAll(pageRequest.getData() != null ? pageRequest.getData().getName() : null)
                .stream()
                .peek(var -> {
                    var.setSources(docTypeService.selectDocChannel(var.getCode()));
                    var.setCount(docTypeService.selectDocCountByDocType(var.getCode()));
                    var.setSourceCount(docTypeService.selectDataSourceCountByDocType(var.getCode()));
                })
                .collect(Collectors.toList());
        List<DocTypeOut> docTypeOutList = BeanUtil.copyToList(
                docTypeDOList,
                DocTypeOut.class
        );

        PageInfo<DocTypeOut> pageInfo = new PageInfo<>(docTypeOutList);
        page.setPageNum(pageRequest.getPageNum());
        page.setPageSize(pageRequest.getPageSize());
        page.setTotalPage(pageInfo.getPages());
        page.setTotalCount(pageInfo.getTotal());
        page.setRecords(docTypeOutList);
        page.setSuccessCode();

        return page;
    }

    @GetMapping("/{id}")
    public DocTypeResponse getById(@PathVariable String id) {
        DocTypeOut docType = docTypeService.findById(id);
        docType.setCount(docTypeService.selectDocCountByDocType(docType.getCode()));
        if (docType == null) {
            return DocTypeResponse.failed("文档类型不存在");
        }
        return DocTypeResponse.success(docType);
    }

    @PostMapping("/create")
    public DocTypeResponse create(@RequestBody DocTypeIn request) {
        DocTypeOut result = BeanUtil.copyProperties(docTypeService.create(request), DocTypeOut.class);
        return DocTypeResponse.success("创建成功", result);
    }

    @PostMapping("/update")
    public DocTypeResponse update(@RequestBody DocTypeIn request) {
        DocTypeOut result = BeanUtil.copyProperties(docTypeService.update(request.getCode(), request), DocTypeOut.class);
        if (result == null) {
            return DocTypeResponse.failed("文档类型不存在");
        }
        return DocTypeResponse.success("更新成功", result);
    }

    @PostMapping("/delete")
    public DocTypeResponse delete(@RequestBody Map<String, Object> params) {
        String id = (String) params.get("id");
        if (docTypeService.delete(id)) {
            return DocTypeResponse.success("删除成功");
        }
        return DocTypeResponse.failed("删除失败，文档类型不存在");
    }

    @GetMapping("/search")
    public DocTypeResponse search(@RequestParam(required = false) String keyword) {
        List<DocTypeDO> result = docTypeService.search(keyword);
        List<DocTypeOut> output = BeanUtil.copyToList(result, DocTypeOut.class);
        return DocTypeResponse.success(output);
    }

    @PostMapping("/chunk-method/update")
    public DocTypeResponse updateChunkMethod(@RequestBody Map<String, Object> params) {
        String id = (String) params.get("id");
        String chunkMethod = (String) params.get("chunkMethod");
        DocTypeOut docType = BeanUtil.copyProperties(docTypeService.updateChunkMethod(id, chunkMethod), DocTypeOut.class);
        if (docType == null) {
            return DocTypeResponse.failed("文档类型不存在");
        }
        return DocTypeResponse.success("分片方式更新成功", docType);
    }
}