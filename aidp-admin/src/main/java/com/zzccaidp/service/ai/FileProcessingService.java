package com.zzccaidp.service.ai;

import com.alibaba.fastjson.JSONObject;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.zzccaidp.async.TransactionAsyncExecutor;
import com.zzccaidp.common.AmazonS3Util;
import com.zzccaidp.common.FileUtils;
import com.zzccaidp.dao.ai.DifyApiKeyConfigDO;
import com.zzccaidp.dao.ai.MultipartFilesWrapper;
import com.zzccaidp.dao.ai.RuleConfigDO;
import com.zzccaidp.dao.ai.TemplateInfoDO;
import com.zzccaidp.enums.ErrCodeEnum;
import com.zzccaidp.exception.BusinessException;
import com.zzccaidp.mapper.ai.DifyApiKeyConfigMapper;
import com.zzccaidp.mapper.ai.RuleConfigMapper;
import com.zzccaidp.mapper.ai.TemplateInfoMapper;
import com.zzccaidp.vo.ai.AsyncFileRecognitionOut;
import com.zzccaidp.vo.ai.DocsAnalyzingReq;
import com.zzccaidp.vo.ai.FXDocsAnalyzingReq;
import com.zzccaidp.vo.ai.FileRecognitionIn;
import com.google.gson.*;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FileProcessingService {

    private static final Logger log = LoggerFactory.getLogger(FileProcessingService.class);
    private static final String TEMPLATE_FILE_NAME = "template.xlsx";
    private static final String PROMPT_FILE_NAME = "prompt.txt";

    // 定义分割符为常量，提高代码可读性
    private static final String KEY_VALUE_DELIMITER = "\\|\\|";
    //识别结果中包含的分隔符
    private static final String VALUE_PART_DELIMITER = "==";
    //DIFY工作流识别结果中包含的分隔符
    private static final String END_START_REGEX = "\\[END\\]\\s*\\[START\\]";
    //换行符
    private static final String NEW_LINE_REGEX = "\n";
    private static final String separator = "/";

    private static final String zzccFX_CHANNEL = "zzccFX";

    @Resource
    private DifyHelperService difyService;
    @Resource
    private NewDifyHelperService newDifyService;

    @Autowired
    private RuleConfigService ruleConfigService;

    @Autowired
    private DifyApiKeyConfigMapper difyApiKeyConfigMapper;

    @Autowired
    private RuleConfigMapper ruleConfigMapper;

    //综合管理平台信创aws桶
    @Autowired
    private AmazonS3 amazonS3;

    @Autowired
    private TransactionAsyncExecutor transactionAsyncExecutor;
    //综合管理平台使用aws桶名称
    @Value("${bades.file.aws-s3.bucketName}")
    private String bucketName;

    @Autowired
    private TransLogService transLogService;


    @Autowired
    private TemplateInfoMapper templateInfoMapper;

    private static final JsonParser jsonParser = new JsonParser(); // 使用静态实例来复用JsonParser对象

    // 定义常量场景配置中配置的参数
    private static final String PARAM_KEY = "paramKey";
    private static final String PARAM_VALUE = "paramValue";


    /**
     * 文件识别服务入参（浙银理财分销定制化接口）
     *
     * @param fileRecognitionIn
     */

    public AsyncFileRecognitionOut processFileszzccFX(FileRecognitionIn fileRecognitionIn) throws BusinessException {
        AsyncFileRecognitionOut recognitionOut = new AsyncFileRecognitionOut();
        //获取需要识别文件的调用方桶名称
        String tagBucketName = "";
        String upLoadBucketName = "";
        //识别结果上传路径
        String uploadResultPath = "";
        //文件识别服务入参文件
        MultipartFilesWrapper files = null;
        //获取规则配置
        RuleConfigDO ruleConfig = fetchRuleConfig(fileRecognitionIn.getRuleId());
        //获取参数配置
        String paramsJsonString = ruleConfig.getParametersConfig();
        //获取规则配置标志(新旧模式切换)
        String flag = ruleConfig.getFlag();
        // 验证JSON字符串的有效性
        if (paramsJsonString == null || paramsJsonString.isEmpty()) {
            log.error("参数配置字符串为空或无效");
            recognitionOut.setResResult(ErrCodeEnum.M1001.getErrCode(), ErrCodeEnum.M1001.getErrMsg());
            return recognitionOut; // 提早返回，避免进一步操作
        }
        //从配置参数中获取tagBucketName和uploadResultPath
        JsonParser jsonParser = new JsonParser();
        JsonArray jsonArray = jsonParser.parse(paramsJsonString).getAsJsonArray();
        tagBucketName = getParameterValue(jsonArray, "tagBucketName");
        upLoadBucketName = getParameterValue(jsonArray, "upLoadBucketName");
        uploadResultPath = getParameterValue(jsonArray, "uploadResultPath");

        if (StringUtils.isEmpty(upLoadBucketName)) {
            log.error("upLoadBucketName为空，上传识别结果桶不能为空！");
            recognitionOut.setResResult(ErrCodeEnum.M1001.getErrCode(), "上传识别结果桶不能为空");
            return recognitionOut;
        }
        if (StringUtils.isEmpty(tagBucketName)) {
            log.error("tagBucketName为空，下载识别文件桶不能为空！");
            recognitionOut.setResResult(ErrCodeEnum.M1001.getErrCode(), "下载识别文件桶不能为空");
            return recognitionOut;
        }
        if (StringUtils.isEmpty(uploadResultPath)) {
            log.error("uploadResultPath为空，上传识别结果文件路径为空！");
            recognitionOut.setFilepath("");
            recognitionOut.setResResult(ErrCodeEnum.M1049.getErrCode(), "上传识别结果文件路径为空");
            return recognitionOut;
        }
        try {
            if ("1".equals(flag)) {
                log.info("开始使用旧模式识别");
                //准备识别配置文件
                files = prepareFiles(fileRecognitionIn.getFileId(), tagBucketName, ruleConfig);
                String recognitionResult = submitAsyncTask(files, ruleConfig);
                if (StringUtils.isEmpty(recognitionResult)) {
                    log.error("识别结果为空，请检查！", fileRecognitionIn.getFileId());
                    recognitionOut.setResResult(ErrCodeEnum.M2011.getErrCode(), ErrCodeEnum.M2011.getErrMsg());
                    return recognitionOut;
                } else {
                    //上传识别结果到指定桶
                    String uploadFilePath = excuteUploadzzccFX(uploadResultPath, recognitionResult, fileRecognitionIn.getRuleId(), upLoadBucketName);
                    if (StringUtils.isEmpty(uploadFilePath)) {
                        recognitionOut.setResResult(ErrCodeEnum.M3005.getErrCode(), ErrCodeEnum.M3005.getErrMsg());
                        return recognitionOut;
                    }
                    recognitionOut.setFilepath(uploadFilePath);
                    recognitionOut.setSuccessCode();
                }
            } else { //新模式
                log.info("开始使用新模式识别");
                String recognitionResult = newSubmitAsyncTask4LCFX(fileRecognitionIn, tagBucketName, ruleConfig);
                if (StringUtils.isEmpty(recognitionResult)) {
                    log.error("新DIFY工作流识别结果为空，请检查！", fileRecognitionIn.getFileId());
                    recognitionOut.setResResult(ErrCodeEnum.M2011.getErrCode(), ErrCodeEnum.M2011.getErrMsg());
                    return recognitionOut;
                } else {
                    //使用新模式解析识别结果并上传识别结果到指定桶
                    String uploadFilePath = excuteNewUploadzzccFX(uploadResultPath, recognitionResult, fileRecognitionIn.getRuleId(), upLoadBucketName);
                    if (StringUtils.isEmpty(uploadFilePath)) {
                        recognitionOut.setResResult(ErrCodeEnum.M3005.getErrCode(), ErrCodeEnum.M3005.getErrMsg());
                        return recognitionOut;
                    }
                    recognitionOut.setFilepath(uploadFilePath);
                    recognitionOut.setSuccessCode();
                    log.info("新DIFY工作流识别结果：{}", recognitionResult);
                }
            }
        } catch (Exception e) {
            log.error("综合管理平台新DIFY工作流异步识别任务失败", e);
            recognitionOut.setResResult(ErrCodeEnum.M9018.getErrCode(), ErrCodeEnum.M9018.getErrMsg());
            return recognitionOut;
        }
        return recognitionOut;
    }

    private String newSubmitAsyncTask4LCFX(FileRecognitionIn fileRecognitionIn, String tagBucketName, RuleConfigDO ruleConfig) {
        String result = "";
        try {
            //要识别的文件从调用方系统下载文件
            List<MultipartFile> wfileList = downloadWfileList(tagBucketName, fileRecognitionIn.getFileId(), fileRecognitionIn.getChl());
            //提示词文件从db获取
//            MultipartFile prompt = newDownloadPrompt(ruleConfig);
            //
            String promptJsonStr = this.getPrompt(ruleConfig);
            //构建请求参数
            FXDocsAnalyzingReq req = buildFXDocsAnalyzingReq(wfileList, promptJsonStr, null, ruleConfig);
            //调用DIFY平台识别服务
            String difyResult = newDifyService.analyzingNewDifyApi4FX(req, ruleConfig.getDifyApiKey());
            log.info("新DIFY工作流识别任务执行识别结果: {}", difyResult);
            if (!StringUtils.isBlank(difyResult)) {
                JSONObject jsonObject = JSONObject.parseObject(difyResult);
                result = jsonObject.getJSONObject("data").getJSONObject("outputs").get("output").toString();
            } else {
                log.error("新DIFY工作流识别结果为空");
                throw new BusinessException(ErrCodeEnum.M9018);
            }
            log.info("新DIFY工作流识别任务解析后结果: {}", result);
        } catch (Exception e) {
            log.error("新DIFY工作流识别任务执行失败", e);
            throw new BusinessException(ErrCodeEnum.M9018, e);
        }
        return result;
    }

    private String getPrompt(RuleConfigDO ruleConfig) {
        log.info("新DIFY工作流识别任务开始获取提示词JsonStr,规则编号: {}", ruleConfig.getRuleId());
        String promptContent = "";
        TemplateInfoDO templateInfo = templateInfoMapper.selectOne(new TemplateInfoDO().setId(ruleConfig.getTemplateId()));
        if (ObjectUtils.isEmpty(templateInfo)) {
            return StringUtils.EMPTY;
        }
        return templateInfo.getTemplateContent();
    }

    private FXDocsAnalyzingReq buildFXDocsAnalyzingReq(List<MultipartFile> wfileList, String promptJsonStr, MultipartFile prompt, RuleConfigDO ruleConfig) {
        FXDocsAnalyzingReq req = new FXDocsAnalyzingReq();
        req.setWfile(wfileList);
        req.setPromptJsonStr(promptJsonStr);
        req.setPrompt(prompt);
        req.setScene(ruleConfig.getScene());
        req.setMode(ruleConfig.getMode());
        req.setRecognizeType(ruleConfig.getRecognizeType());
        req.setPage(ruleConfig.getPage());
        return req;
    }

    private String getParameterValue(JsonArray jsonArray, String key) {
        for (JsonElement jsonElement : jsonArray) {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            if (key.equals(jsonObject.get(PARAM_KEY).getAsString())) {
                return jsonObject.get(PARAM_VALUE).getAsString();
            }
        }
        return null; // 如果找不到对应的键，返回null
    }

    private String getParameterValue(JsonArray jsonArray, String key, String defaultValue) {
        for (JsonElement jsonElement : jsonArray) {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            if (key.equals(jsonObject.get(PARAM_KEY).getAsString())) {
                return jsonObject.get(PARAM_VALUE).getAsString();
            }
        }
        return defaultValue; // 如果找不到对应的键，返回null
    }

    /**
     * 处理识别结果字符串并按照zzccFX(浙银理财分销)要求返回CSV
     *
     * @param uploadResultPath
     * @param recognitionResult
     */
    private String excuteUploadzzccFX(String uploadResultPath, String recognitionResult, String ruleId, String tagBucketName) {
        String uploadFilePath = "";
        List<String> resultPathList = new ArrayList<>();
        //解析响应字符串并组装为CSV
        String[] parts = recognitionResult.split(END_START_REGEX);
        for (String part : parts) {
            if (!part.trim().isEmpty()) {
                String[] lines = part.split(NEW_LINE_REGEX);
                //默认识别结果文件名
                String productCode = "";
                String key = "";
                for (String line : lines) {
                    if (line.startsWith("prd_code")) {
                        String[] keyValue = line.split(KEY_VALUE_DELIMITER);
                        if (keyValue.length == 2 && keyValue[1].contains(VALUE_PART_DELIMITER)) {
                            String[] prdCodeStr = keyValue[1].split(VALUE_PART_DELIMITER);
                            if (prdCodeStr.length > 0) {
                                productCode = prdCodeStr[0].trim();
                            }
                        }
                    }
                }
                //默认产品代码为空，不上传当前识别结果
                if (StringUtils.isBlank(productCode)) {
                    log.error("解析识别结果【产品代码】字段失败,不上传当前产品识别结果");
                    continue;
                } else {
                    key = uploadResultPath + ruleId + separator + productCode + ".csv";
                    resultPathList.add(key);
                    log.info("识别结果上传路径: " + key);
                }
                // 对上传路径做安全校验
                if (!isValidPath(uploadResultPath)) {
                    throw new IllegalArgumentException("不合法的上传路径！");
                }
                //上传识别结果文件到aws
                uploadResult(writeCSV(part.trim()), key, tagBucketName);
            }
        }
        uploadFilePath = resultPathList.stream().collect(Collectors.joining(","));
        return uploadFilePath;
    }


    /**
     * 处理识别结果字符串并按照zzccFX(新模式)
     *
     * @param uploadResultPath
     * @param recognitionResult
     */
    private String excuteNewUploadzzccFX(String uploadResultPath, String recognitionResult, String ruleId, String tagBucketName) {
        String uploadFilePath = "";
        String keyList = "";
        List<String> resultPathList = new ArrayList<>();
        JsonParser jsonParser = new JsonParser();
        // 对上传路径做安全校验
        if (!isValidPath(uploadResultPath)) {
            throw new IllegalArgumentException("不合法的上传路径！");
        }
        //解析响应字符串并组装为CSV
        JsonArray jsonArray = jsonParser.parse(recognitionResult).getAsJsonArray();

        jsonArray.forEach(jsonElement -> {
            StringBuilder csvBuilder = new StringBuilder();
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            String prdCode = "";
            String key = "";
            if (jsonObject.has("prd_code")) {
                prdCode = jsonObject.get("prd_code").getAsString();
            }
            //默认产品代码为空，不上传当前识别结果
            if (StringUtils.isBlank(prdCode)) {
                log.error("解析识别结果【产品代码】字段失败,不上传当前产品识别结果");
                return; // 返回，不执行后续操作
            } else {
                key = uploadResultPath + ruleId + separator + prdCode + ".csv";
                log.info("识别结果上传路径: " + key);
                resultPathList.add(key);
            }
            //遍历jsonObject的键值对，将键值对写入CSV
            Iterator<String> keys = jsonObject.keySet().iterator();
            while (keys.hasNext()) {
                String jsonKey = keys.next();
                csvBuilder.append(jsonKey).append(',').append(jsonObject.get(jsonKey)).append("\n");
            }
            //上传识别结果文件到aws
            uploadResult(csvBuilder.toString(), key, tagBucketName);
        });
        uploadFilePath = resultPathList.stream().collect(Collectors.joining(","));
        return uploadFilePath;
    }

    private void processJsonElement(JsonObject jsonObject, String uploadResultPath, String ruleId, List<String> resultPathList, String tagBucketName) {
        String prdCode = jsonObject.has("prd_code") ? jsonObject.get("prd_code").getAsString() : "";

        if (StringUtils.isBlank(prdCode)) {
            log.error("解析识别结果【产品代码】字段失败,不上传当前产品识别结果");
            return;
        }

        StringBuilder csvContent = new StringBuilder();
        jsonObject.entrySet().forEach(entry -> {
            csvContent.append(entry.getKey()).append(',').append(entry.getValue()).append(',');
        });
        if (csvContent.length() > 0) {
            csvContent.setLength(csvContent.length() - 1); // 移除最后一个逗号
        }

        String key = uploadResultPath + ruleId + separator + prdCode + ".csv";
        resultPathList.add(key);
        log.info("识别结果上传路径: " + key);

        uploadResult(csvContent.toString(), key, tagBucketName);
    }

    /**
     * 将JSON字符串转换为CSV格式
     *
     * @param jsonString JSON字符串
     * @return CSV格式的字符串
     */
    private String convertJsonToCsv(String jsonString) {
        StringBuilder csvBuilder = new StringBuilder();
        JsonParser jsonParser = new JsonParser();
        JsonArray jsonArray = jsonParser.parse(jsonString).getAsJsonArray();
        jsonArray.forEach(jsonElement -> {
            if (jsonElement.isJsonObject()) {
                JsonObject jsonObject = jsonElement.getAsJsonObject();
                if (jsonObject.has("prd_code")) {
                    String prdCode = jsonObject.get("prd_code").getAsString();
                    csvBuilder.append(prdCode).append(",");
                }
                csvBuilder.append(jsonObject.toString()).append("\n");
            } else {
                csvBuilder.append(jsonElement.toString()).append("\n");
            }
        });

        // 获取JSON对象的所有键并作为CSV头
        JsonObject firstObject = jsonArray.get(0).getAsJsonObject();
        List<String> keys = new ArrayList<>(firstObject.keySet());
        csvBuilder.append(String.join(",", keys)).append("\n");

        // 遍历JSONArray，将每个JSONObject的值添加到CSV中
        for (JsonElement jsonElement : jsonArray) {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            List<String> values = keys.stream()
                    .map(key -> jsonObject.get(key).getAsString())
                    .collect(Collectors.toList());
            csvBuilder.append(String.join(",", values)).append("\n");
        }

        return csvBuilder.toString();
    }

    // 对生成结果上传路径校验
    private boolean isValidPath(String path) {
        // 验证路径是否合法，防止路径遍历攻击等
        return path != null && !path.contains("..");
    }


    /**
     * 解析识别结果并生成CSV
     *
     * @param product
     * @return
     */
    private static String writeCSV(String product) {
        StringBuilder result = new StringBuilder();

        // 尝试使用BufferedReader逐行读取，以提高大文本处理的效率
        try (BufferedReader reader = new BufferedReader(new StringReader(product))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] keyValue = line.split(KEY_VALUE_DELIMITER);
                if (keyValue != null && keyValue.length >= 2) {
                    String key = keyValue[0].trim();
                    // 限制分割次数的split方法，避免不必要的全串扫描
                    String valuePart = keyValue[1].split(VALUE_PART_DELIMITER, 2)[0].trim();
                    result.append(key).append(",").append(valuePart).append(NEW_LINE_REGEX);
                }
            }
        } catch (IOException e) {
            log.error("解析识别结果为CSV失败", e);
            throw new BusinessException(ErrCodeEnum.M3005);
        }

        return result.toString();
    }

    private void uploadResult(String result, String key, String tagBucketName) {
        log.info("开始上传识别结果" + result);
        try (InputStream inputStream = new ByteArrayInputStream(result.getBytes(StandardCharsets.UTF_8))) {
            ObjectMetadata metadata = new ObjectMetadata();
            amazonS3.putObject(tagBucketName, key, inputStream, metadata);
            log.info("识别结果上传成功: {}", key);
        } catch (Exception e) {
            log.error("上传结果失败", e);
            throw new BusinessException(ErrCodeEnum.M1035);
        }
    }


    /**
     * 文件识别服务（同步接口）
     *
     * @param fileId
     * @param ruleId
     */

    public String processFiles(String fileId, String ruleId) throws BusinessException {
        String tagBucketName = "";
        MultipartFilesWrapper files = null;
        RuleConfigDO ruleConfig = null;
        try {
            ruleConfig = fetchRuleConfig(ruleId); // 假设此方法已具备异常处理机制
            String paramsJsonString = ruleConfig.getParametersConfig();
            JsonParser jsonParser = new JsonParser();
            JsonArray jsonArray = jsonParser.parse(paramsJsonString).getAsJsonArray();
            for (JsonElement jsonElement : jsonArray) {
                JsonObject jsonObject = jsonElement.getAsJsonObject();
                if ("tagBucketName".equals(jsonObject.get("paramKey").getAsString())) {
                    tagBucketName = jsonObject.get("paramValue").getAsString();
                    log.info("调用方目标桶名称: " + tagBucketName);
                }
            }
            if (StringUtils.isEmpty(tagBucketName)) {
                log.error("tagBucketName为空，下载识别文件失败请检查！");
                throw new BusinessException(ErrCodeEnum.M1036);
            }
            // 下载文件
            files = prepareFiles(fileId, tagBucketName, ruleConfig);

        } catch (IOException e) {
            log.error("处理文件时发生IO异常", e);
            throw new BusinessException(ErrCodeEnum.ERROR, e);
        } catch (Exception e) {
            // 处理其他可能的异常
            log.error("处理文件时发生未知异常", e);
            throw new BusinessException(ErrCodeEnum.M1036, e);
        }
        return submitAsyncTask(files, ruleConfig);
    }

    private void validateInputs(String fileId, String ruleId) {
        if (StringUtils.isBlank(fileId) || StringUtils.isBlank(ruleId)) {
            throw new BusinessException(ErrCodeEnum.M1000);
        }
    }

    private RuleConfigDO fetchRuleConfig(String ruleId) {
        // 根据规则id查询规则信息
        RuleConfigDO ruleConfig = ruleConfigMapper.getRuleConfigByRuleId(ruleId);
        DifyApiKeyConfigDO difyApiKeyConfigDO = difyApiKeyConfigMapper.selectByPrimaryKey(ruleConfig.getDifyApiKeyId());
        ruleConfig.setDifyApiKey(difyApiKeyConfigDO.getApiKey());
        return ruleConfig;
    }

    private MultipartFilesWrapper prepareFiles(String fileId, String tagBucketName, RuleConfigDO ruleConfig) throws IOException {
        //要识别的文件从调用方系统下载文件
        MultipartFile wfile = downloadWfile(tagBucketName, fileId, "");
        //模板文件从综合管理台aws桶下载
        MultipartFile template = downloadTemplate(ruleConfig);
        //提示词文件从综合管理台aws桶下载
        MultipartFile prompt = downloadPrompt(ruleConfig);
        return new MultipartFilesWrapper(wfile, template, prompt);
    }

    /**
     * 从调用方桶下载要识别的文件
     *
     * @param tagBucketName
     * @param fileId
     * @return
     */
    private MultipartFile downloadWfile(String tagBucketName, String fileId, String chl) {
        try {
            log.info("开始下载识别文件: {}", fileId);
            S3ObjectSummary summary = null;
            //获取
            List<S3ObjectSummary> fileList = listFilesInS3Folder(tagBucketName, fileId, chl);
            if (!CollectionUtils.isEmpty(fileList)) {
                summary = fileList.get(0);
            }
            //根据objectkey解析文件名称
            String fileName = parseFileName(summary.getKey());


            return getWfileMultipartFile(fileName, tagBucketName, summary.getKey(), chl);
        } catch (AmazonS3Exception | IOException e) {
            log.error("AWS下载文件失败，请检查！", e);
            throw new BusinessException(ErrCodeEnum.M1036, e);
        }
    }


    /**
     * 从调用方桶下载要识别的文件
     *
     * @param tagBucketName
     * @param fileId
     * @return
     */
    private List<MultipartFile> downloadWfileList(String tagBucketName, String fileId, String chl) {
        try {
            log.info("开始下载识别文件: {}", fileId);
            S3ObjectSummary summary = null;
            //获取
            List<S3ObjectSummary> fileList = listFilesInS3Folder(tagBucketName, fileId, chl);
            List<MultipartFile> muFileList = new ArrayList<>();
            if (CollectionUtils.isEmpty(fileList)) {
                return muFileList;
            }
            for (S3ObjectSummary s3ObjectSummary : fileList) {
                muFileList.add(getWfileMultipartFile(parseFileName(s3ObjectSummary.getKey()), tagBucketName, s3ObjectSummary.getKey(), chl));
            }
            return muFileList;
        } catch (AmazonS3Exception | IOException e) {
            log.error("AWS下载文件失败，请检查！", e);
            throw new BusinessException(ErrCodeEnum.M1036, e);
        }
    }

    private String parseFileName(String fileId) {
        String[] sArr = fileId.split("/");
        if (sArr.length == 0) {
            log.error("解析fileid失败: {}", fileId);
            throw new BusinessException(ErrCodeEnum.M1036);
        }
        return sArr[sArr.length - 1];
    }

    /**
     * 从aws桶下载模板文件
     *
     * @param ruleConfig
     * @return
     * @throws IOException
     */
    private MultipartFile downloadTemplate(RuleConfigDO ruleConfig) throws IOException {
        //如果有字段映射文件，则向DIFY平台template.xlsx模板文件使用字段映射文件，否则使用提取字段文件
        String fileid = StringUtils.isBlank(ruleConfig.getFieldMappingFileid()) ?
                ruleConfig.getExtractFieldFileid() : ruleConfig.getFieldMappingFileid();
        return StringUtils.isBlank(fileid) ? null : getMultipartFile(TEMPLATE_FILE_NAME, bucketName, fileid);
    }

    private MultipartFile downloadPrompt(RuleConfigDO ruleConfig) throws IOException {
        return StringUtils.isBlank(ruleConfig.getPromptFileid()) ?
                null : getMultipartFile(PROMPT_FILE_NAME, bucketName, ruleConfig.getPromptFileid());
    }


    private MultipartFile newDownloadPrompt(RuleConfigDO ruleConfig) throws IOException {
        log.info("新DIFY工作流识别任务开始生成提示词文件,规则编号: {}", ruleConfig.getRuleId());
        String promptContent = "";
        TemplateInfoDO templateInfo = templateInfoMapper.selectOne(new TemplateInfoDO().setId(ruleConfig.getTemplateId()));
        if (!ObjectUtils.isEmpty(templateInfo)) {
            promptContent = templateInfo.getPromptContent();
        }
        return FileUtils.convertStringToMultipartFile(promptContent, PROMPT_FILE_NAME, "text/plain");
    }

    private String submitAsyncTask(MultipartFilesWrapper files, RuleConfigDO ruleConfig) {
        String result = "";
        try {
            DocsAnalyzingReq req = buildDocsAnalyzingReq(files.getWfile(), files.getTemplate(), files.getPrompt(), ruleConfig);
            String difyResult = newDifyService.analyzingNewDifyApi(req, ruleConfig.getDifyApiKey());
            log.info("DIFY识别任务执行识别结果: {}", difyResult);
            if (!StringUtils.isBlank(difyResult)) {
                JSONObject jsonObject = JSONObject.parseObject(difyResult);
                result = jsonObject.getJSONObject("data").getJSONObject("outputs").get("output").toString();
            } else {
                log.error("DIFY识别结果为空");
                throw new BusinessException(ErrCodeEnum.M9018);
            }
            log.info("DIFY识别任务解析后结果: {}", result);
        } catch (Exception e) {
            log.error("DIFY识别任务执行失败", e);
            throw new BusinessException(ErrCodeEnum.M9018, e);
        }
        return result;
    }

    /**
     * 新版本工作流识别
     *
     * @param fileRecognitionIn
     * @param ruleConfig
     * @return
     */
    private String newSubmitAsyncTask(FileRecognitionIn fileRecognitionIn, String tagBucketName, RuleConfigDO ruleConfig) {
        String result = "";
        try {
            //要识别的文件从调用方系统下载文件
            MultipartFile wfile = downloadWfile(tagBucketName, fileRecognitionIn.getFileId(), fileRecognitionIn.getChl());
            //提示词文件从综合管理台aws桶下载
            MultipartFile prompt = newDownloadPrompt(ruleConfig);
            //构建请求参数
            DocsAnalyzingReq req = buildDocsAnalyzingReq(wfile, null, prompt, ruleConfig);
            //调用DIFY平台识别服务
            String difyResult = newDifyService.analyzingNewDifyApi(req, ruleConfig.getDifyApiKey());
            log.info("新DIFY工作流识别任务执行识别结果: {}", difyResult);
            if (!StringUtils.isBlank(difyResult)) {
                JSONObject jsonObject = JSONObject.parseObject(difyResult);
                result = jsonObject.getJSONObject("data").getJSONObject("outputs").get("output").toString();
            } else {
                log.error("新DIFY工作流识别结果为空");
                throw new BusinessException(ErrCodeEnum.M9018);
            }
            log.info("新DIFY工作流识别任务解析后结果: {}", result);
        } catch (Exception e) {
            log.error("新DIFY工作流识别任务执行失败", e);
            throw new BusinessException(ErrCodeEnum.M9018, e);
        }
        return result;
    }

    private DocsAnalyzingReq buildDocsAnalyzingReq(MultipartFile wfile, MultipartFile template,
                                                   MultipartFile prompt, RuleConfigDO ruleConfig) {
        DocsAnalyzingReq req = new DocsAnalyzingReq();
        req.setWfile(wfile);
        req.setTemplate(template);
        req.setPrompt(prompt);
        req.setScene(ruleConfig.getScene());
        req.setMode(ruleConfig.getMode());
        req.setRecognizeType(ruleConfig.getRecognizeType());
        req.setPage(ruleConfig.getPage());
        return req;
    }

    public S3Object getS3Object(String bucketName, String fileid) throws BusinessException {
        //获取文件
        List<S3ObjectSummary> fileList = listFilesInS3Folder(bucketName, fileid, "");
        if (CollectionUtils.isEmpty(fileList)) {
            log.error("文件不存在: {}", fileid);
            throw new BusinessException(ErrCodeEnum.M1036);
        }
        //产品工厂确定目前每个产品目录下只会有一个文件，所以直接取第一个文件
        S3ObjectSummary summary = fileList.get(0);
        //获取文件对象
        S3Object object = amazonS3.getObject(summary.getBucketName(), summary.getKey());
        if (object == null) {
            log.error("文件不存在: {}", fileid);
            throw new BusinessException(ErrCodeEnum.M1036);
        }
        return object;
    }

    /**
     * 获取指定路径下的所有文件
     *
     * @param bucketName Bucket名称
     * @param prefix     文件路径前缀
     * @return 文件对象列表
     */
    private List<S3ObjectSummary> listFilesInS3Folder(String bucketName, String prefix, String chl) {
        List<S3ObjectSummary> fileList = new ArrayList<>();

        try {
            ListObjectsV2Request req = new ListObjectsV2Request()
                    .withBucketName(bucketName)
                    .withPrefix(prefix)
                    .withMaxKeys(50); // 性能优化限制最大键数，根据实际情况调整

            ListObjectsV2Result result;

            do {
                if (StringUtils.isBlank(chl) || zzccFX_CHANNEL.equals(chl)) {
                    result = amazonS3.listObjectsV2(req);
                } else {
                    result = amazonS3.listObjectsV2(req);
                }
                for (S3ObjectSummary objectSummary : result.getObjectSummaries()) {
                    //跳过文件夹
                    if (objectSummary.getKey().endsWith("/")) {
                        continue;
                    }
                    fileList.add(objectSummary);
                }

                req.setContinuationToken(result.getNextContinuationToken());
            } while (result.isTruncated());
        } catch (Exception e) {
            // 处理异常，例如日志记录或抛出自定义异常
            log.error("列出S3文件失败: {}", e.getMessage());
            throw new RuntimeException("列出S3文件失败", e);
        }

        return fileList;
    }

    /**
     * 配置文件下载(使用综合管理台AWS)
     *
     * @param fileName
     * @param bucketName
     * @param fileid
     * @return
     * @throws IOException
     */
    public MultipartFile getMultipartFile(String fileName, String bucketName, String fileid) throws IOException {
        log.info("开始下载配置文件: {} 桶名称：{}", fileid, bucketName);
        byte[] fileContent = AmazonS3Util.getObjectByAws(amazonS3, bucketName, fileid);
        // 将字节数组转换为MultipartFile对象
        try {
            return new MockMultipartFile("file", fileName, null, fileContent);
        } catch (Exception e) {
            log.error("文件转换失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 从调用方桶获取wfile识别文件
     *
     * @param fileName
     * @param bucketName
     * @param fileid
     * @return
     * @throws IOException
     */
    public MultipartFile getWfileMultipartFile(String fileName, String bucketName, String fileid, String chl) throws IOException {
        byte[] fileContent = null;
        if (StringUtils.isBlank(chl) || zzccFX_CHANNEL.equals(chl)) {
            fileContent = AmazonS3Util.getObjectByAws(amazonS3, bucketName, fileid);
        } else {
            fileContent = AmazonS3Util.getObjectByAws(amazonS3, bucketName, fileid);
        }
        // 将字节数组转换为MultipartFile对象
        try {
            return new MockMultipartFile("file", fileName, null, fileContent);
        } catch (Exception e) {
            log.error("文件转换失败: {}", e.getMessage());
            return null;
        }
    }


    /**
     * AI文件识别处理（公共方法）
     *
     * @param fileRecognitionIn
     * @return
     */

    public AsyncFileRecognitionOut processFilesPublic(FileRecognitionIn fileRecognitionIn) {
        log.info("外部系统调用综合管理平台AI文档识别接口开始,识别文件路径: {}", fileRecognitionIn.getFileId());
        AsyncFileRecognitionOut recognitionOut = new AsyncFileRecognitionOut();
        try {
            // 获取规则配置
            RuleConfigDO ruleConfig = fetchRuleConfig(fileRecognitionIn.getRuleId());
            // 验证规则配置
            if (!validateAndSetConfig(ruleConfig, recognitionOut)) {
                return recognitionOut; // 若配置验证失败，则提前返回
            }
            // 解析参数配置
            String paramsJsonString = ruleConfig.getParametersConfig();
            JsonArray jsonArray = parseParamsJson(paramsJsonString, recognitionOut);
            if (jsonArray == null) {
                return recognitionOut; // 若JSON解析失败，则提前返回
            }
            // 从配置参数中提取必要字段
            String tagBucketName = getParameterValue(jsonArray, "tagBucketName");
            String upLoadBucketName = getParameterValue(jsonArray, "upLoadBucketName");
            String uploadResultPath = getParameterValue(jsonArray, "uploadResultPath");
            // 验证提取的字段
            if (!validateFields(upLoadBucketName, tagBucketName, uploadResultPath, recognitionOut)) {
                return recognitionOut; // 若字段验证失败，则提前返回
            }
            //执行识别操作主流程
            processRecognition(fileRecognitionIn, tagBucketName, upLoadBucketName, uploadResultPath, ruleConfig, recognitionOut);
            log.info("外部系统调用综合管理平台AI文档识别接口结束,识别文件路径: {}", fileRecognitionIn.getFileId());
        } catch (Exception e) {
            handleException(e, recognitionOut);
        }
        return recognitionOut;
    }

    /**
     * AI文件识别处理主流程（通用渠道渠道）
     *
     * @param fileRecognitionIn
     * @return
     */
    private void processRecognition(FileRecognitionIn fileRecognitionIn, String tagBucketName, String upLoadBucketName, String uploadResultPath, RuleConfigDO ruleConfig, AsyncFileRecognitionOut recognitionOut) {
        //执行新DIFY工作流识别并获取结果
        String recognitionResult = newSubmitAsyncTask(fileRecognitionIn, tagBucketName, ruleConfig);
        if (StringUtils.isEmpty(recognitionResult)) {
            logAndSetError("新DIFY工作流识别结果为空，请检查！", fileRecognitionIn.getFileId(), ErrCodeEnum.M2011, recognitionOut);
        } else {
            //解析识别结果并上传识别结果到指定桶
            String uploadFilePath = excuteNewUploadPublic(uploadResultPath, recognitionResult, fileRecognitionIn, upLoadBucketName, ruleConfig);
            if (StringUtils.isEmpty(uploadFilePath)) {
                logAndSetError("上传识别结果文件路径为空！", "", ErrCodeEnum.M3005, recognitionOut);
            } else {
                recognitionOut.setFilepath(uploadFilePath);
                recognitionOut.setSuccessCode();
                log.info("新DIFY工作流识别结果：{}", recognitionResult);
            }
        }
    }

    private JsonArray parseParamsJson(String paramsJsonString, AsyncFileRecognitionOut recognitionOut) {
        if (paramsJsonString == null || paramsJsonString.isEmpty()) {
            logAndSetError("参数配置字符串为空或无效", "", ErrCodeEnum.M1001, recognitionOut);
            return null;
        }
        try {
            JsonArray jsonArray = jsonParser.parse(paramsJsonString).getAsJsonArray();
            return jsonArray;
        } catch (JsonParseException e) {
            logAndSetError("参数配置JSON解析失败", e.getMessage(), ErrCodeEnum.M1001, recognitionOut);
            return null;
        }
    }

    private boolean validateFields(String upLoadBucketName, String tagBucketName, String uploadResultPath, AsyncFileRecognitionOut recognitionOut) {
        if (StringUtils.isEmpty(upLoadBucketName)) {
            logAndSetError("upLoadBucketName为空，上传识别结果桶不能为空！", "", ErrCodeEnum.M1001, recognitionOut);
            return false;
        }
        if (StringUtils.isEmpty(tagBucketName)) {
            logAndSetError("tagBucketName为空，下载识别文件桶不能为空！", "", ErrCodeEnum.M1001, recognitionOut);
            return false;
        }
        if (StringUtils.isEmpty(uploadResultPath)) {
            logAndSetError("uploadResultPath为空，上传识别结果文件路径为空！", "", ErrCodeEnum.M1049, recognitionOut);
            return false;
        }
        return true;
    }

    private boolean validateAndSetConfig(RuleConfigDO ruleConfig, AsyncFileRecognitionOut recognitionOut) {
        if (ruleConfig == null) {
            logAndSetError("规则配置不存在", "", ErrCodeEnum.M1001, recognitionOut);
            return false;
        }
        return true;
    }

    private void handleException(Exception e, AsyncFileRecognitionOut recognitionOut) {
        log.error("外部系统调用综合管理平台DIFY工作流异步识别任务失败", e);
        recognitionOut.setResResult(ErrCodeEnum.M9018.getErrCode(), ErrCodeEnum.M9018.getErrMsg());
    }

    private void logAndSetError(String errorMessage, String details, ErrCodeEnum errCodeEnum, AsyncFileRecognitionOut recognitionOut) {
        log.error(errorMessage + " 细节: {}", details);
        recognitionOut.setResResult(errCodeEnum.getErrCode(), errorMessage);
    }

    /**
     * 处理识别结果字符串(通用渠道)
     *
     * @param uploadResultPath
     * @param recognitionResult
     */
    private String excuteNewUploadPublic(String uploadResultPath, String recognitionResult, FileRecognitionIn fileRecognitionIn, String upLoadBucketName, RuleConfigDO ruleConfig) {
        String uploadFilePath = "";
        String keyList = "";
        List<String> resultPathList = new ArrayList<>();
        JsonParser jsonParser = new JsonParser();
        // 对上传路径做安全校验
        if (!isValidPath(uploadResultPath)) {
            throw new IllegalArgumentException("不合法的上传路径！");
        }
        //解析响应字符串并组装为CSV
        String parametersConfig = ruleConfig.getParametersConfig();
        JsonArray config = jsonParser.parse(parametersConfig).getAsJsonArray();

        // 从配置参数中提取必要字段
        String resultFormat = getParameterValue(config, "resultFormat");
        String csvDelimiter = getParameterValue(config, "csvDelimiter", ",");
        JsonArray jsonArray = jsonParser.parse(recognitionResult).getAsJsonArray();
        if ("csv".equals(resultFormat)) {
            // 收集所有 JSON 对象
            List<JsonObject> jsonObjects = new ArrayList<>();
            jsonArray.forEach(jsonElement -> jsonObjects.add(jsonElement.getAsJsonObject()));

            // 确定 CSV 的列头
            Set<String> headers = new LinkedHashSet<>();
            jsonObjects.forEach(jsonObject -> headers.addAll(jsonObject.keySet()));

            // 生成 CSV 内容
            StringBuilder csvBuilder = new StringBuilder();
            csvBuilder.append(String.join(csvDelimiter, headers)).append("\n");

            for (JsonObject jsonObject : jsonObjects) {
                for (String header : headers) {
                    csvBuilder.append(jsonObject.get(header) != null ? jsonObject.get(header).getAsString() : "")
                            .append(csvDelimiter);
                }
                csvBuilder.deleteCharAt(csvBuilder.length() - 1); // 删除最后一个逗号
                csvBuilder.append("\n");
            }
            // 生成文件名
            String fileName = fileRecognitionIn.getFlowid();
            String key = uploadResultPath + fileRecognitionIn.getRuleId() + separator + fileName + ".csv";
            resultPathList.add(key);
            uploadResultPublic(csvBuilder.toString(), key, upLoadBucketName);
        } else {
            Gson gson = new Gson();
            String jsonString = gson.toJson(jsonArray);
            String fileName = fileRecognitionIn.getFlowid();
            String key = uploadResultPath + fileRecognitionIn.getRuleId() + separator + fileName + ".json";
            resultPathList.add(key);
            uploadResultPublic(jsonString, key, upLoadBucketName);
        }
        uploadFilePath = resultPathList.stream().collect(Collectors.joining(","));
        log.info("通用渠道识别完成，识别结果上传路径：{}", uploadFilePath);
        return uploadFilePath;
    }

    private void uploadResultPublic(String result, String key, String upLoadBucketName) {
        log.info("开始上传识别结果" + result);
        try (InputStream inputStream = new ByteArrayInputStream(result.getBytes(StandardCharsets.UTF_8))) {
            ObjectMetadata metadata = new ObjectMetadata();
            amazonS3.putObject(upLoadBucketName, key, inputStream, metadata);
            log.info("识别结果上传成功: {}", key);
        } catch (Exception e) {
            log.error("上传结果失败", e);
            throw new BusinessException(ErrCodeEnum.M1035);
        }
    }

}
