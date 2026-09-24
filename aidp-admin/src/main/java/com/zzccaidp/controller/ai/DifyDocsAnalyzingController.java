package com.zzccaidp.controller.ai;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zzccaidp.context.UserInfoContextHolder;
import com.zzccaidp.dao.ai.AiFileResultDO;
import com.zzccaidp.dao.ai.SysFileInfoDO;
import com.zzccaidp.service.ai.DifyHelperService;
import com.zzccaidp.service.ai.SysFileInfoService;
import com.zzccaidp.vo.ResHeader;
import com.zzccaidp.vo.ai.*;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author liuxiazhang
 * @date 2025/8/25
 */

@Controller
@Api(value = "文档解析")
@RequestMapping("/difyDocsAnalyzing")
@Slf4j
public class DifyDocsAnalyzingController {

    @Resource
    private DifyHelperService difyService;

    @Autowired
    private SysFileInfoService sysFileInfoService;

    /**
     * 文档解析入口 （Streaming Mode）
     *
     * @param wfile         待解析文件
     * @param template      字段映射集文件
     * @param prompt        提示词文本文件
     * @param scene         场景（fast：快思考，deep：慢思考）
     * @param mode          提取模式（1：统一提取；2：提取模式）
     * @param recognizeType 识别方式（1：标准文档识别；2：表格文档识别）
     * @param page          指定文档页码
     */
    @PostMapping(value = "/analyzing", consumes = "multipart/form-data")
    public ResponseEntity analyzing(
            @RequestParam("wfile") MultipartFile wfile,
            @RequestParam(value = "template", required = false) MultipartFile template,
            @RequestParam(value = "prompt", required = false) MultipartFile prompt,
            @RequestParam(value = "scene", required = false) String scene,
            @RequestParam(value = "mode", required = false) String mode,
            @RequestParam(value = "recognizeType", required = false) String recognizeType,
            @RequestParam(value = "page", required = false) String page
    ) {
        DocsAnalyzingReq req = new DocsAnalyzingReq();
        req.setWfile(wfile);
        req.setTemplate(template);
        req.setPrompt(prompt);
        req.setScene(scene);
        req.setMode(mode);
        req.setRecognizeType(recognizeType);
        req.setPage(page);
        return ResponseEntity.ok(difyService.analyzing(req));
    }

    /**
     * 文档解析入口 - 支持Streaming响应
     *
     * @param wfile         待解析文件
     * @param template      字段映射集文件
     * @param prompt        提示词文本文件
     * @param scene         场景（fast：快思考，deep：慢思考）
     * @param mode          提取模式（1：统一提取；2：提取模式）
     * @param recognizeType 识别方式（1：标准文档识别；2：表格文档识别）
     * @param page          指定文档页码
     */
    @PostMapping(value = "/analyzingStreaming", consumes = "multipart/form-data"
            , produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @ResponseBody
    public Flux<String> analyzingStreaming(
            @RequestParam("wfile") MultipartFile wfile,
            @RequestParam(value = "template", required = false) MultipartFile template,
            @RequestParam(value = "prompt", required = false) MultipartFile prompt,
            @RequestParam(value = "scene", required = false) String scene,
            @RequestParam(value = "mode", required = false) String mode,
            @RequestParam(value = "recognizeType", required = false) String recognizeType,
            @RequestParam(value = "page", required = false) String page
    ) {
        DocsAnalyzingReq req = new DocsAnalyzingReq();
        req.setWfile(wfile);
        req.setTemplate(template);
        req.setPrompt(prompt);
        req.setScene(scene);
        req.setMode(mode);
        req.setRecognizeType(recognizeType);
        req.setPage(page);
        return difyService.analyzingStreaming(req);
    }

    /**
     * 文档解析入口 - 支持Streaming响应，将文档识别转换成markdown和json格式返回给前端
     * saveFlieFlag是否临时文件。1-临时文件，0-非临时文件
     *
     * @param file 待解析文件
     */
    @PostMapping(value = "/analyzingStreamingNew", consumes = "multipart/form-data"
            , produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public AgainFileForAwsVO analyzingFileStreaming(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "recognizeType", required = false) String recognizeType,
            @RequestParam(value = "page", required = false) String page,
            @RequestParam(value = "saveFlieFlag", required = false) Integer saveFlieFlag,
            @RequestParam(value = "keyData", required = false) String keyData
    ) {
        return analyzingFileStreaming4Template(file, recognizeType, page, saveFlieFlag, keyData, "");
    }


    /**
     * 文档解析入口 - 支持Streaming响应，将文档识别转换成markdown和json格式返回给前端
     * saveFlieFlag是否临时文件。1-临时文件，0-非临时文件
     *
     * @param file 待解析文件
     */
    @PostMapping(value = "/analyzingStreaming4Template", consumes = "multipart/form-data"
            , produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public AgainFileForAwsVO analyzingFileStreaming4Template(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "recognizeType", required = false) String recognizeType,
            @RequestParam(value = "page", required = false) String page,
            @RequestParam(value = "saveFlieFlag", required = false) Integer saveFlieFlag,
            @RequestParam(value = "keyData", required = false) String keyData,
            @RequestParam(value = "templateId", required = false) String templateId
    ) {
        AgainFileForAwsVO againFileForAwsVO = new AgainFileForAwsVO();
        againFileForAwsVO.setSuccessCode();
        DocsAnalyzingReq req = new DocsAnalyzingReq();
        req.setWfile(file);
        req.setRecognizeType(recognizeType);
        req.setPage(page);
        req.setScene("aiFile");
        JSONObject jsonObject = JSON.parseObject(difyService.analyzing(req)).getJSONObject("data").getJSONObject("outputs");
        againFileForAwsVO.setContent(jsonObject.getJSONObject("content").getString("output"));
        JSONObject contentList = jsonObject.getJSONObject("contentList");
        if (Objects.isNull(contentList)) {
            againFileForAwsVO.setContentList("");
        } else {
            againFileForAwsVO.setContentList(contentList.getString("output"));
        }
        String fileId = "";
        againFileForAwsVO.setType(jsonObject.getJSONObject("type").getString("output"));
        againFileForAwsVO.setFileName(file.getOriginalFilename());
        try {
            //保存文档附件和解析结果
            fileId = difyService.saveFile(saveFlieFlag, file, JSONObject.toJSONString(againFileForAwsVO), keyData, templateId);
        } catch (Exception exception) {
            log.error("保存文件失败", exception);
        }
        againFileForAwsVO.setFileId(fileId);
        return againFileForAwsVO;
    }


    /**
     *
     * @return
     */
    @PostMapping("/getAiFileByUser")
    @ResponseBody
    public SysFileInfoListVO getAiFileByUser() {
        String userId = UserInfoContextHolder.getUserInfo();
        List<SysFileInfoDO> sysFileInfoDOList = sysFileInfoService.selectAiFileByUser(userId);
        if(sysFileInfoDOList.isEmpty()){
            SysFileInfoListVO sysFileInfoListVO = new SysFileInfoListVO();
            sysFileInfoListVO.setSuccessCode();
            sysFileInfoListVO.setScratchFileList(new ArrayList<>());
            sysFileInfoListVO.setHistoryFileList(new ArrayList<>());
            return sysFileInfoListVO;
        }
        List<SysFileInfoVO> sysFileInfoVOList = sysFileInfoDOList.stream()
                .map(sysFileInfoDO -> {
                    SysFileInfoVO sysFileInfoVO = new SysFileInfoVO();
                    BeanUtils.copyProperties(sysFileInfoDO, sysFileInfoVO);
                    return sysFileInfoVO;
                })
                .collect(Collectors.toList());
        List<AiFileResultDO> aiFileResultDOList = difyService.listAiFileHistoryResult(sysFileInfoDOList.stream()
                .map(SysFileInfoDO::getFileId)
                .collect(Collectors.toList()));
        Map<String, AiFileResultDO> map = new HashMap<>();
        List<SysFileInfoVO> historyFileList = new ArrayList<>();
        List<SysFileInfoVO> scratchFileList = new ArrayList<>();

        aiFileResultDOList.forEach(aiFileResultDO -> map.put(aiFileResultDO.getFileId(), aiFileResultDO));
        sysFileInfoVOList.forEach(sysFileInfoVO -> {
            BeanUtils.copyProperties(map.get(sysFileInfoVO.getFileId()), sysFileInfoVO);
            if (sysFileInfoVO.getScratchFile().equals(1)) {
                scratchFileList.add(sysFileInfoVO);
            } else if (sysFileInfoVO.getScratchFile().equals(0)) {
                historyFileList.add(sysFileInfoVO);
            }
        });
        SysFileInfoListVO sysFileInfoListVO = new SysFileInfoListVO();
        sysFileInfoListVO.setSuccessCode();
        sysFileInfoListVO.setScratchFileList(scratchFileList);
        sysFileInfoListVO.setHistoryFileList(historyFileList);
        return sysFileInfoListVO;
    }

    /**
     * 历史文件解析结果查询
     *
     * @return
     */
    @PostMapping("/analysisOfHistoryFile")
    @ResponseBody
    public AiFileResultVO analysisOfHistoryFile(
            @RequestParam(value = "fileId") String fileId
    ) {
        AiFileResultDO aiFileResultDO = difyService.queryAiFileHistoryResult(fileId);
        if (Objects.isNull(aiFileResultDO)) {
            aiFileResultDO = new AiFileResultDO();
        }
        AiFileResultVO aiFileResultVO = new AiFileResultVO();
        BeanUtils.copyProperties(aiFileResultDO, aiFileResultVO);
        aiFileResultVO.setSuccessCode();
        //如果没有历史解析结果，重新解析。
        return aiFileResultVO;
    }

    @PostMapping("/analyzeParsing")
    @ResponseBody
    public AgainFileForAwsVO analyzeParsing(
            @RequestParam(value = "fileId") String fileId,
            @RequestParam(value = "recognizeType", required = false) String recognizeType,
            @RequestParam(value = "page", required = false) String page,
            @RequestParam(value = "keyData", required = false) String keyData
    ) {
        return difyService.againFileForAws(fileId, page, recognizeType);
    }


    /**
     *
     * @return
     */
    @PostMapping("/deleteAiFileByFileId")
    @ResponseBody
    public ResHeader deleteAiFileByFileId(@RequestParam(value = "fileId") List<String> fileIds) {
        return sysFileInfoService.deleteAiFileByID(fileIds);
    }


    /**
     *
     * @return
     */
    @PostMapping("/deleteAiFileByFileId4Template")
    @ResponseBody
    public ResHeader deleteAiFileByFileId(@RequestParam(value = "fileId", required = false) String fileId, String templateId) {
        return sysFileInfoService.deleteAiFileByID(fileId, templateId);
    }

    @GetMapping("/deleteScratchFile")
    @ResponseBody
    public ResHeader deleteScratchFile() {
        ResHeader response = new ResHeader();
        try {
            List<String> fileIdList = sysFileInfoService.selectScratchFileByUserName();
            if (!fileIdList.isEmpty()) {
                sysFileInfoService.deleteAiFileByID(fileIdList);
            }
            response.setSuccessCode();
        } catch (Exception e) {
            log.error("删除临时文件失败", e);
            response.setErrorCode();
        }
        return response;
    }

    @PostMapping("/updateFileScratch")
    @ResponseBody
    public ResHeader updateFileScratch(@RequestBody List<SysFileInfoDO> sysFileInfoDOList) {
        ResHeader response = new ResHeader();
        try {
            sysFileInfoService.updateFileInfo(sysFileInfoDOList);
            response.setSuccessCode();
        } catch (Exception e) {
            log.error("归档临时文件失败", e);
            throw e;
        }
        return response;
    }
}
