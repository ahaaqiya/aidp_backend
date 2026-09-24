package com.zzccaidp.service.ai;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.zzccaidp.common.SnowflakeUtil;
import com.zzccaidp.context.UserInfoContextHolder;
import com.zzccaidp.dao.ai.AiFileResultDO;
import com.zzccaidp.dao.ai.SysFileInfoDO;
import com.zzccaidp.dao.ai.TemplateLastFileDO;
import com.zzccaidp.dao.system.UserDO;
import com.zzccaidp.dto.ai.DocsAnalyzingDTO;
import com.zzccaidp.dto.ai.DocsAnalyzingInputDTO;
import com.zzccaidp.dto.ai.NewDocsAnalyzingDTO;
import com.zzccaidp.dto.ai.NewDocsAnalyzingInputDTO;
import com.zzccaidp.mapper.ai.AiFileResultMapper;
import com.zzccaidp.mapper.ai.TemplateLastFileMapper;
import com.zzccaidp.service.system.SystemParamsService;
import com.zzccaidp.vo.ai.AgainFileForAwsVO;
import com.zzccaidp.vo.ai.DocsAnalyzingReq;
import com.zzccaidp.vo.ai.NewDocsAnalyzingReq;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.ChannelOption;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.netty.http.client.HttpClient;
import org.springframework.mock.web.MockMultipartFile;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

/**
 * @author liuxiazhang
 * @date 2025/8/26
 */
@Slf4j
@Service
public class DifyHelperService {

    /**
     * Dify基础URL
     */
    @Value("${dify.base.url:}")
    private String difyBaseUrl;

    /**
     * Dify API 快思考密钥
     */
    @Value("${dify.api.docs.analyzing.fast.key:}")
    private String difyApiFastKey;


    /**
     * 新Dify API 密钥 (新工作流快慢思考在工作流中判断)
     */
    @Value("${dify.api.docs.analyzingNew.fast.key:}")
    private String difyNewApiFastKey;

    /**
     * 字段提取模型（快思考）
     */
    @Value("${dify.api.docs.fieldextraction.fast.key:}")
    private String difyNewExtractionFastKey;


    /**
     * Dify API 慢思考密钥
     */
    @Value("${dify.api.docs.analyzing.deep.key:}")
    private String difyApiDeepKey;


    /**
     * 新字段提取模型（慢思考）
     */
    @Value("${dify.api.docs.fieldextraction.deep.key:}")
    private String difyNewExtractionDeepKey;

    /**
     * Dify API 慢思考密钥
     */
    @Value("${dify.api.docs.analyzing.read.timeout:600000}")
    private Integer difyApiReadTimeout;

    @Value("${dify.api.docs.analyzing.maxInMemorySize:20971520}")
    private Integer maxInMemorySize;
    /**
     * 新文档识别apiKey
     */
    @Value("${dify.api.docs.analyzing.aiFile.key:app-sIZ2O65rbYhV8pRIBpl3vFjy}")
    private String difyAiFile;

    @Autowired
    private AmazonS3 amazonS3;

    @Value("${bades.file.aws-s3.bucketName}")
    private String bucketName;

    @Autowired
    private SysFileInfoService sysFileInfoService;

    @Autowired
    private AiFileResultMapper aiFileResultMapper;
    @Autowired
    private SystemParamsService sysParamsService;

    @Autowired
    private TemplateLastFileMapper templateLastFileMapper;
    /**
     * dify 文件上传URI
     */
    private static final String UPLOAD_FILE_URI = "/files/upload";
    /**
     * dify workflows uri
     */
    private static final String WORKFLOWS_RUN_URI = "/workflows/run";


    /**
     * 文档解析 （Blocking Mode）
     *
     * @param req
     * @return
     */
    public String analyzing(DocsAnalyzingReq req) {
        req.setUser(getUser());

        DocsAnalyzingDTO dto = getDocsAnalyzingDTO(req);
        if (dto == null) {
            return "";
        }
        dto.setResponseMode("blocking");

        String url = difyBaseUrl + WORKFLOWS_RUN_URI;
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + getApiKey(req.getScene()));
            HttpEntity<DocsAnalyzingDTO> requestEntity = new HttpEntity<>(dto, headers);
            ResponseEntity<String> response = restTemplate().exchange(
                    url, HttpMethod.POST, requestEntity, String.class);
            if (HttpStatus.OK.equals(response.getStatusCode()) && response.getBody() != null) {
                String responseBody = response.getBody();
                log.info("解析文件响应: {}", responseBody);
                return responseBody;
            } else {
                log.error("解析文件失败，响应状态: {}", response.getStatusCode());
                return null;
            }
        } catch (Exception e) {
            log.error("解析文件失败: ", e);
            return null;
        }
    }

    /**
     * 文档解析 （Streaming Mode）
     *
     * @param req 请求参数
     */
    public Flux<String> analyzingStreaming(DocsAnalyzingReq req) {
        req.setUser(getUser());

        DocsAnalyzingDTO dto = getDocsAnalyzingDTO(req);
        if (dto == null) {
            return Flux.empty();
        }

        dto.setResponseMode("streaming");
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.TCP_NODELAY, true)
                .responseTimeout(Duration.ofMillis(difyApiReadTimeout));

        ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(maxInMemorySize))
                .build();
        WebClient webClient = WebClient.builder()
                .baseUrl(difyBaseUrl)
                .defaultHeader("Authorization", "Bearer " + getApiKey(req.getScene()))
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .exchangeStrategies(strategies)
                .build();

        return webClient.post()
                .uri(WORKFLOWS_RUN_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(dto))
                .accept(MediaType.TEXT_EVENT_STREAM)
                .retrieve()
                .bodyToFlux(String.class)
                .onErrorResume(throwable -> {
                    log.error("流处理异常：", throwable);
                    return Flux.empty();
                })
                .doOnNext(line -> log.debug("接收到流式数据：{}", line))
                .takeUntil(line -> line.contains("\"status\":\"succeeded\""))
                .timeout(Duration.ofMillis(difyApiReadTimeout))
                .doOnComplete(() -> {
                    log.info("流式处理完成");
                })
                .doOnError(throwable -> log.error("流处理异常:", throwable));
    }

    /**
     * 鉴权 + 获取用户标识
     *
     * @return
     */
    private String getUser() {
        String userLogin = UserInfoContextHolder.getUserInfo();
        return userLogin != null ? userLogin : "dify-query";
    }

    /**
     * 上传文件到Dify并获取文档信息
     *
     * @param req
     * @return
     */
    private DocsAnalyzingDTO getDocsAnalyzingDTO(DocsAnalyzingReq req) {
        if (req.getWfile() == null || req.getWfile().isEmpty()) {
            return null;
        }
        DocsAnalyzingDTO docsAnalyzingDTO = new DocsAnalyzingDTO();
        docsAnalyzingDTO.setUser(req.getUser());

        DocsAnalyzingInputDTO docsAnalyzingInputDTO = new DocsAnalyzingInputDTO();
        docsAnalyzingInputDTO.setMode(req.getMode());
        docsAnalyzingInputDTO.setPage(req.getPage());
        docsAnalyzingInputDTO.setRecognizeType(req.getRecognizeType());
        docsAnalyzingInputDTO.setWfile(getFileInfo(req, req.getWfile()));
        if (req.getTemplate() != null && !req.getTemplate().isEmpty()) {
            docsAnalyzingInputDTO.setTemplate(getFileInfo(req, req.getTemplate()));
        }
        if (req.getPrompt() != null && !req.getPrompt().isEmpty()) {
            docsAnalyzingInputDTO.setPrompt(getFileInfo(req, req.getPrompt()));
        }
        docsAnalyzingDTO.setInputs(docsAnalyzingInputDTO);
        return docsAnalyzingDTO;
    }

    /**
     * 组装下一步文件解析需要的信息
     *
     * @param req
     * @param file
     * @return
     */
    private Map<String, String> getFileInfo(DocsAnalyzingReq req, MultipartFile file) {
        String difyFileId = uploadFile(req, file);
        if (difyFileId == null || "".equals(difyFileId)) {
            return null;
        }
        String fileName = file.getOriginalFilename();
        String fileExtName = fileName.substring(fileName.lastIndexOf(".") + 1);

        Map<String, String> fileInfo = new HashMap<>();
        fileInfo.put("type", getFileType(fileExtName));
        fileInfo.put("upload_file_id", difyFileId);
        fileInfo.put("transfer_method", "local_file");
        return fileInfo;
    }

    /**
     * 上传文件到Dify
     *
     * @param req  用户标识
     * @param file 要上传的文件
     * @return 上传结果
     */
    private String uploadFile(DocsAnalyzingReq req, MultipartFile file) {
        String url = difyBaseUrl + UPLOAD_FILE_URI;
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            headers.set("Authorization", "Bearer " + getApiKey(req.getScene()));

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            ByteArrayResource resource = new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            };
            body.add("file", resource);
            body.add("user", req.getUser());
            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate().exchange(
                    url, HttpMethod.POST, requestEntity, String.class);

            if (Arrays.asList(HttpStatus.OK, HttpStatus.CREATED).contains(response.getStatusCode()) && response.getBody() != null) {
                String responseBody = response.getBody();
                log.info("文件上传响应: {}", responseBody);
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode jsonNode = objectMapper.readTree(responseBody);
                return jsonNode.get("id") != null ? jsonNode.get("id").asText() : null;
            } else {
                log.error("文件上传失败，响应状态: {}", response.getStatusCode());
                return null;
            }
        } catch (Exception e) {
            log.error("文件上传异常，用户: {}, 文件名: {}", req.getUser(), file.getOriginalFilename(), e);
            return null;
        }
    }

    /**
     * 获取dify不通模型的apiKey
     *
     * @param scene
     * @return
     */
    private String getApiKey(String scene) {
        String apiKey = "";
        switch (scene) {
            case "deep":
                apiKey = difyApiDeepKey;
                break;
            case "aiFile":
                apiKey = difyAiFile;
                break;
            default:
                apiKey = difyApiFastKey;
        }
        return apiKey;
    }

    /**
     * 文档类型在DIFY的映射
     *
     * @param fileExtName
     * @return
     */
    private String getFileType(String fileExtName) {
        List<String> documentTypes = Arrays.asList("TXT", "MD", "MARKDOWN", "PDF", "HTML", "XLSX", "XLS", "DOCX"
                , "CSV", "EML", "MSG", "PPTX", "PPT", "XML", "EPUB");
        List<String> imageTypes = Arrays.asList("JPG", "JPEG", "PNG", "GIF", "WEBP", "SVG");
        List<String> audioTypes = Arrays.asList("MP3", "M4A", "WAV", "WEBM", "AMR");
        List<String> videoTypes = Arrays.asList("MP4", "MOV", "MPEG", "MPGA");
        List<String> customTypes = Arrays.asList("DOC");

        String extName = fileExtName.toUpperCase(Locale.ROOT);
        if (documentTypes.contains(extName)) {
            return "document";
        }
        if (imageTypes.contains(extName)) {
            return "image";
        }
        if (audioTypes.contains(extName)) {
            return "audio";
        }
        if (videoTypes.contains(extName)) {
            return "video";
        }
        if (customTypes.contains(extName)) {
            return "custom";
        }
        return "";
    }

    private RestTemplate restTemplate() {
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(difyApiReadTimeout);
        factory.setConnectionRequestTimeout(600000);
        return new RestTemplate(factory);
    }


    /**
     * 新版本文档解析 (字段提取不区分快慢思考)（Blocking Mode）
     *
     * @param req
     * @return
     */
    public String analyzingNew(NewDocsAnalyzingReq req) {
        req.setUser(getUser());

        NewDocsAnalyzingDTO dto = getDocsNewAnalyzingDTO(req);
        dto.setUser(req.getUser());

        if (dto == null) {
            return "";
        }
        dto.setResponseMode("blocking");

        String url = difyBaseUrl + WORKFLOWS_RUN_URI;
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + difyNewExtractionFastKey);
            HttpEntity<NewDocsAnalyzingDTO> requestEntity = new HttpEntity<>(dto, headers);
            ResponseEntity<String> response = restTemplate().exchange(
                    url, HttpMethod.POST, requestEntity, String.class);
            if (HttpStatus.OK.equals(response.getStatusCode()) && response.getBody() != null) {
                String responseBody = response.getBody();
                log.info("解析文件响应: {}", responseBody);
                return responseBody;
            } else {
                log.error("解析文件失败，响应状态: {}", response.getStatusCode());
                return null;
            }
        } catch (Exception e) {
            log.error("解析文件失败: ", e);
            return null;
        }
    }

    /**
     * 上传文件到Dify并获取文档信息
     *
     * @param req
     * @return
     */
    private NewDocsAnalyzingDTO getDocsNewAnalyzingDTO(NewDocsAnalyzingReq req) {
        if (StringUtils.isBlank(req.getFileContent())) {
            return null;
        }
        NewDocsAnalyzingDTO docsAnalyzingDTO = new NewDocsAnalyzingDTO();
        docsAnalyzingDTO.setUser(req.getUser());

        NewDocsAnalyzingInputDTO docsAnalyzingInputDTO = new NewDocsAnalyzingInputDTO();
        docsAnalyzingInputDTO.setMode(req.getMode());
        docsAnalyzingInputDTO.setType(req.getType());
        docsAnalyzingInputDTO.setWfileText(req.getFileContent());
        docsAnalyzingInputDTO.setWfileTextList(req.getWfileTextList());
        if (req.getPrompt() != null && !req.getPrompt().isEmpty()) {
            docsAnalyzingInputDTO.setPrompt(req.getPrompt());
        }
        docsAnalyzingInputDTO.setScene(req.getScene());
        docsAnalyzingDTO.setInputs(docsAnalyzingInputDTO);
        return docsAnalyzingDTO;
    }

    /**
     * 获取dify不通模型的apiKey
     *
     * @param scene
     * @return
     */
    private String getNewExtractionApiKey(String scene) {
        return "newdeep".equals(scene) ? difyNewExtractionDeepKey : difyNewExtractionFastKey;
    }

    @Transactional
    public String saveFile(Integer saveFlieFlag, MultipartFile file, String parsingResule, String keyData, String templateId) throws IOException {
        String id = String.valueOf(SnowflakeUtil.nextId());
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());
        amazonS3.putObject(bucketName, "aiFile/" + id, file.getInputStream(), metadata);
        SysFileInfoDO sysFileInfoDO = new SysFileInfoDO();
        sysFileInfoDO.setFileId(id);
        sysFileInfoDO.setFilePath("aiFile/" + id);
        sysFileInfoDO.setFileSize(String.valueOf(file.getSize()));
        sysFileInfoDO.setFileName(file.getOriginalFilename());
        sysFileInfoDO.setFileStorageTyps("aws");
        sysFileInfoDO.setCreateTime(LocalDateTime.now());
        //文件保存中的文件所属业务类型，用以区分文件是哪些业务的，方便查询用户下文件列表
        sysFileInfoDO.setFileBusinessTyps("aiFile");
        String fileName = sysFileInfoDO.getFileName();
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex != -1 && lastDotIndex < fileName.length() - 1) {
            String fileExtension = fileName.substring(lastDotIndex + 1);
            sysFileInfoDO.setFileType(fileExtension);
        } else {
            // 处理没有扩展名的情况
            sysFileInfoDO.setFileType("");
        }
        sysFileInfoDO.setCreateUser(UserInfoContextHolder.getUserInfo());
        sysFileInfoDO.setScratchFile(saveFlieFlag);
        sysFileInfoService.insertFile(sysFileInfoDO);
        log.info("上传解析文件到aws成功，文件信息[{}]", sysFileInfoDO);

        AiFileResultDO aiFileResultDO = new AiFileResultDO();
        aiFileResultDO.setFileId(id);
        aiFileResultDO.setFileParsingResult(parsingResule);
        aiFileResultDO.setKeyData(keyData);
        UserDO userDO = UserInfoContextHolder.getUser();
        aiFileResultDO.setUserId(userDO.getUserName());
        aiFileResultDO.setDeptId(userDO.getDeptId());
        aiFileResultMapper.insert(aiFileResultDO);

        // 保存模板+当前用户最后一次使用的文件
        if (StringUtils.isNotBlank(templateId)) {
            this.saveLastFile(templateId, id, UserInfoContextHolder.getUserInfo());
        }
        return id;
    }

    private void saveLastFile(String templateId, String fileId, String userId) {
        // 先删除已有的
        templateLastFileMapper.deleteByTemplateIdAndUserId(templateId, userId);
        TemplateLastFileDO templateLastFileDO = new TemplateLastFileDO();
        templateLastFileDO.setUserId(userId);
        templateLastFileDO.setId(String.valueOf(SnowflakeUtil.nextId()));
        templateLastFileDO.setFileId(fileId);
        templateLastFileDO.setTemplateId(templateId);
        templateLastFileDO.setCreateTime(new Date());
        templateLastFileMapper.insert(templateLastFileDO);

    }

    /**
     * 二次解析历史文件
     *
     * @param fileId
     * @return
     */
    public AgainFileForAwsVO againFileForAws(String fileId, String page, String recognizeType) {
        try {
            SysFileInfoDO sysFileInfoDO = sysFileInfoService.getSysFileInfo(fileId);
            String filePath = sysFileInfoDO.getFilePath();
            S3ObjectInputStream s3ObjectInputStream = amazonS3.getObject(bucketName, filePath).getObjectContent();
            MultipartFile wfile = new MockMultipartFile(sysFileInfoDO.getFileName(), sysFileInfoDO.getFileName(), "text/plain", s3ObjectInputStream);
            DocsAnalyzingReq req = new DocsAnalyzingReq();
            req.setWfile(wfile);
            req.setRecognizeType(recognizeType);
            req.setPage(page);
            req.setScene("aiFile");
            JSONObject jsonObject = JSON.parseObject(analyzing(req)).getJSONObject("data").getJSONObject("outputs");
            AgainFileForAwsVO againFileForAwsVO = new AgainFileForAwsVO();
            againFileForAwsVO.setSuccessCode();
            againFileForAwsVO.setContent(jsonObject.getJSONObject("content").getString("output"));
            againFileForAwsVO.setType(jsonObject.getJSONObject("type").getString("output"));
            JSONObject contentList = jsonObject.getJSONObject("contentList");
            if (Objects.isNull(contentList)) {
                againFileForAwsVO.setContentList("");
            } else {
                againFileForAwsVO.setContentList(contentList.getString("output"));
            }
            return againFileForAwsVO;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public AiFileResultDO queryAiFileHistoryResult(String fileId) {
        return aiFileResultMapper.selectByPrimaryKey(fileId);
    }

    public List<AiFileResultDO> listAiFileHistoryResult(List<String> fileId) {
        return aiFileResultMapper.listAiFileHistoryResult(fileId);
    }

}
