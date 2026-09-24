package com.zzccaidp.controller.ai;

import com.alibaba.fastjson.JSONObject;
import com.zzccaidp.dao.ai.AiFileResultDO;
import com.zzccaidp.enums.ErrCodeEnum;
import com.zzccaidp.mapper.ai.AiFileResultMapper;
import com.zzccaidp.mapper.ai.RuleConfigMapper;
import com.zzccaidp.service.ai.DifyHelperService;
import com.zzccaidp.service.ai.FileProcessingService;
import com.zzccaidp.service.ai.RuleConfigService;
import com.zzccaidp.service.ai.TemplateInfoService;
import com.zzccaidp.vo.ai.ExtractResultVo;
import com.zzccaidp.vo.ai.NewDocsAnalyzingReq;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author guoqiang
 * @description DifyApiController
 * @date 2025/10/22
 */
@Slf4j
@Api(value = "对外提供AI相关的API")
@RequestMapping("/aiApi")
@RestController
public class AIExtractDataApiController {

    @Resource
    private DifyHelperService difyService;

    @Autowired
    private RuleConfigService ruleConfigService;

    @Autowired
    private RuleConfigMapper ruleConfigMapper;
    @Autowired
    private AiFileResultMapper aiFileResultMapper;
    @Autowired
    private FileProcessingService fileProcessingService;
    @Autowired
    private TemplateInfoService templateInfoService;


    /**
     * 智能抽取 - 按照字段提取
     *
     * @param fileContent     待解析文本内容
     * @param fileId          要抽取的文件id
     * @param type            场景类型标记
     * @param wfileTextList   分页文本内容
     * @param templateContent 模板类型
     * @param scene           场景（fast：快思考，deep：慢思考）
     * @param mode            提取模式（1：统一提取；2：提取模式）
     */

    @PostMapping(value = "/analyzingByfield", consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ExtractResultVo analyzingByfield(
            @RequestParam(value = "fileContent", required = true) String fileContent,
            @RequestParam(value = "fileId", required = true) String fileId,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "wfileTextList", required = false) String wfileTextList,
            @RequestParam(value = "templateContent", required = false) String templateContent,
            @RequestParam(value = "scene", required = false) String scene,
            @RequestParam(value = "mode", required = false) String mode
    ) {
        ExtractResultVo resultVo = new ExtractResultVo();
        try {
            validateInputParameters(fileContent, fileId); // 参数校验

            String promptContent = templateInfoService.JsonToString(templateContent); // 转换逻辑分离

            log.info("智能抽取开始，提示词为：{}", promptContent);

            NewDocsAnalyzingReq req = createAnalyzingRequest(fileContent, promptContent, scene, mode, type, wfileTextList);

            String difyResult = difyService.analyzingNew(req); // 异步处理可在此处考虑

            if (!StringUtils.isBlank(difyResult)) {
                String result = parseDifyResult(difyResult); // 解析服务响应
                updateAiFileResult(fileId, templateContent, result); // 更新数据库操作分离
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("fileId",fileId);
                jsonObject.put("result",result);
                resultVo.setResult(jsonObject);
                resultVo.setSuccessCode();
            } else {
                resultVo.setResResult(ErrCodeEnum.M9018.getErrCode(), ErrCodeEnum.M9018.getErrMsg());
            }
        } catch (IllegalArgumentException e) { // 针对参数校验失败的异常处理
            log.error("参数校验失败", e);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("fileId",fileId);
            resultVo.setResult(jsonObject);
            resultVo.setResResult(ErrCodeEnum.M1000.getErrCode(), ErrCodeEnum.M1000.getErrMsg());
        } catch (Exception e) {
            log.error("智能抽取失败", e);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("fileId",fileId);
            resultVo.setResult(jsonObject);
            resultVo.setResResult(ErrCodeEnum.M9018.getErrCode(), ErrCodeEnum.M9018.getErrMsg());
        }
        return resultVo;
    }

    private void validateInputParameters(String fileContent, String fileId) {
        // 参数校验
        if (StringUtils.isBlank(fileContent)) {
            throw new IllegalArgumentException("fileContent不能为空");
        }
        if (StringUtils.isBlank(fileId)) {
            throw new IllegalArgumentException("fileId不能为空");
        }
    }

    private NewDocsAnalyzingReq createAnalyzingRequest(String fileContent, String promptContent, String scene, String mode, String type, String wfileTextList) {
        NewDocsAnalyzingReq req = new NewDocsAnalyzingReq();
        req.setFileContent(fileContent);
        req.setPrompt(promptContent);
        req.setScene(scene);
        req.setMode(mode);
        req.setType(type);
        req.setWfileTextList(wfileTextList);
        return req;
    }

    private String parseDifyResult(String difyResult) {
        JSONObject jsonObject = JSONObject.parseObject(difyResult);
        return jsonObject.getJSONObject("data").getJSONObject("outputs").get("output").toString();
    }

    private void updateAiFileResult(String fileId, String templateContent, String result) {
        AiFileResultDO aiFileResultDO = new AiFileResultDO();
        aiFileResultDO.setFileId(fileId);
        aiFileResultDO.setFileExtractionTemplate(templateContent);
        aiFileResultDO.setFileExtractionResult(result);
        aiFileResultMapper.updateByPrimaryKeySelective(aiFileResultDO);
        log.info("智能抽取结果为：{}", result);
    }


}


