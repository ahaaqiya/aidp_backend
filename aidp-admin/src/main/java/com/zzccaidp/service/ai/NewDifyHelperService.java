package com.zzccaidp.service.ai;

import com.amazonaws.services.s3.AmazonS3;
import com.zzccaidp.context.UserInfoContextHolder;
import com.zzccaidp.dto.ai.FXNewDocsAnalyzingApiDTO;
import com.zzccaidp.dto.ai.FXNewDocsAnalyzingApiInputDTO;
import com.zzccaidp.dto.ai.NewDocsAnalyzingApiDTO;
import com.zzccaidp.dto.ai.NewDocsAnalyzingApiInputDTO;
import com.zzccaidp.vo.ai.DocsAnalyzingReq;
import com.zzccaidp.vo.ai.FXDocsAnalyzingReq;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

/**
 * 新Dify接口调用工作流
 *
 * @author WB255485
 * @date 2026-03-11
 */
@Slf4j
@Service
public class NewDifyHelperService {

    /**
     * Dify基础URL
     */
    @Value("${dify.base.url:}")
    private String difyBaseUrl;


    /**
     * 新Dify API 快思考密钥 (新工作流)
     */
    @Value("${dify.api.docs.analyzingNew.fast.key:}")
    private String difyNewApiFastKey;

    /**
     * Dify API 慢思考密钥
     */
    @Value("${dify.api.docs.analyzing.read.timeout:600000}")
    private Integer difyApiReadTimeout;

    /**
     * dify 文件上传URI
     */
    private static final String UPLOAD_FILE_URI = "/files/upload";

    /**
     * dify workflows uri
     */
    private static final String WORKFLOWS_RUN_URI = "/workflows/run";

    /**
     * 鉴权 + 获取用户标识
     *
     * @return
     */
    private String getUser() {
        String userLogin = UserInfoContextHolder.getUserInfo();
        return userLogin != null ? userLogin : "dify-query";
    }

    private RestTemplate restTemplate() {
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(difyApiReadTimeout);
        factory.setConnectionRequestTimeout(600000);
        return new RestTemplate(factory);
    }

    /**
     * 新版接口调用工作流不区分模型 (分销通过接口调用dify工作流)（Blocking Mode）
     *
     * @param req
     * @return
     */
    public String analyzingNewDifyApi(DocsAnalyzingReq req, String apiKey) {
        req.setUser(getUser());

        NewDocsAnalyzingApiDTO dto = getDocsNewAnalyzingApiDTO(req, apiKey);
        dto.setUser(req.getUser());

        if (dto == null) {
            return "";
        }
        dto.setResponseMode("blocking");

        String url = difyBaseUrl + WORKFLOWS_RUN_URI;
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
//            headers.set("Authorization", "Bearer " + getNewExtractionApiKey(req.getScene()));
            //DIFY工作流根据scene值不同区分
            headers.set("Authorization", "Bearer " + apiKey);
            HttpEntity<NewDocsAnalyzingApiDTO> requestEntity = new HttpEntity<>(dto, headers);
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
     * 新版接口调用工作流不区分模型 (分销通过接口调用dify工作流)（Blocking Mode）
     *
     * @param req
     * @return
     */
    public String analyzingNewDifyApi4FX(FXDocsAnalyzingReq req, String apiKey) {
        req.setUser(getUser());

        FXNewDocsAnalyzingApiDTO dto = getDocsNewAnalyzingApiDTO4FX(req, apiKey);
        dto.setUser(req.getUser());

        if (dto == null) {
            return "";
        }
        dto.setResponseMode("blocking");

        String url = difyBaseUrl + WORKFLOWS_RUN_URI;
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
//            headers.set("Authorization", "Bearer " + getNewExtractionApiKey(req.getScene()));
            //DIFY工作流根据scene值不同区分
            headers.set("Authorization", "Bearer " + apiKey);
            HttpEntity<FXNewDocsAnalyzingApiDTO> requestEntity = new HttpEntity<>(dto, headers);
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
    private NewDocsAnalyzingApiDTO getDocsNewAnalyzingApiDTO(DocsAnalyzingReq req, String apiKey) {
        if (req.getWfile() == null || req.getWfile().isEmpty()) {
            return null;
        }
        NewDocsAnalyzingApiDTO docsAnalyzingDTO = new NewDocsAnalyzingApiDTO();
        docsAnalyzingDTO.setUser(req.getUser());

        NewDocsAnalyzingApiInputDTO docsAnalyzingInputDTO = new NewDocsAnalyzingApiInputDTO();
        docsAnalyzingInputDTO.setMode(req.getMode());
        docsAnalyzingInputDTO.setPage(req.getPage());
        docsAnalyzingInputDTO.setRecognizeType(req.getRecognizeType());
        docsAnalyzingInputDTO.setWfile(getFileInfo(req, req.getWfile(), apiKey));
        if (req.getTemplate() != null && !req.getTemplate().isEmpty()) {
            docsAnalyzingInputDTO.setTemplate(getFileInfo(req, req.getTemplate(), apiKey));
        }
        if (req.getPrompt() != null && !req.getPrompt().isEmpty()) {
            docsAnalyzingInputDTO.setPrompt(getFileInfo(req, req.getPrompt(), apiKey));
        }
        docsAnalyzingInputDTO.setScene(req.getScene());
        docsAnalyzingDTO.setInputs(docsAnalyzingInputDTO);
        return docsAnalyzingDTO;
    }


    /**
     * 上传文件到Dify并获取文档信息
     *
     * @param req
     * @return
     */
    private FXNewDocsAnalyzingApiDTO getDocsNewAnalyzingApiDTO4FX(FXDocsAnalyzingReq req, String apiKey) {
        if (CollectionUtils.isEmpty(req.getWfile())) {
            return null;
        }
        FXNewDocsAnalyzingApiDTO docsAnalyzingDTO = new FXNewDocsAnalyzingApiDTO();
        docsAnalyzingDTO.setUser(req.getUser());

        FXNewDocsAnalyzingApiInputDTO docsAnalyzingInputDTO = new FXNewDocsAnalyzingApiInputDTO();
        docsAnalyzingInputDTO.setMode(req.getMode());
        docsAnalyzingInputDTO.setPage(req.getPage());
        docsAnalyzingInputDTO.setRecognizeType(req.getRecognizeType());
        docsAnalyzingInputDTO.setWfile(getFileInfo4Fx(req, req.getWfile(), apiKey));
        if (req.getTemplate() != null && !req.getTemplate().isEmpty()) {
            docsAnalyzingInputDTO.setTemplate(getFileInfo4Fx(req, req.getTemplate(), apiKey));
        }
        if (req.getPrompt() != null && !req.getPrompt().isEmpty()) {
            docsAnalyzingInputDTO.setPrompt(getFileInfo4Fx(req, req.getPrompt(), apiKey));
        }
        if (StringUtils.isNotBlank(req.getPromptJsonStr())) {
            docsAnalyzingInputDTO.setPromptJsonStr(req.getPromptJsonStr());
        }
        docsAnalyzingInputDTO.setScene(req.getScene());
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
    private Map<String, String> getFileInfo(DocsAnalyzingReq req, MultipartFile file, String apiKey) {
        String difyFileId = uploadFile(req.getUser(), file, apiKey);
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
     * 组装下一步文件解析需要的信息
     *
     * @param req
     * @param fileList
     * @return
     */
    private List<Map<String, String>> getFileInfo4Fx(FXDocsAnalyzingReq req, List<MultipartFile> fileList, String apiKey) {
        List<Map<String, String>> fileInfoList = new ArrayList<>();

        for (MultipartFile multipartFile : fileList) {
            String difyFileId = uploadFile(req.getUser(), multipartFile, apiKey);
            if (difyFileId == null || "".equals(difyFileId)) {
                continue;
            }
            String fileName = multipartFile.getOriginalFilename();
            String fileExtName = fileName.substring(fileName.lastIndexOf(".") + 1);

            Map<String, String> fileInfo = new HashMap<>();
            fileInfo.put("type", getFileType(fileExtName));
            fileInfo.put("upload_file_id", difyFileId);
            fileInfo.put("transfer_method", "local_file");
            fileInfoList.add(fileInfo);
        }

        return fileInfoList;
    }


    /**
     * 组装下一步文件解析需要的信息
     *
     * @param req
     * @param file
     * @return
     */
    private Map<String, String> getFileInfo4Fx(FXDocsAnalyzingReq req, MultipartFile file, String apiKey) {
        String difyFileId = uploadFile(req.getUser(), file, apiKey);
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

    /**
     * 上传文件到Dify
     *
     * @param user 用户标识
     * @param file 要上传的文件
     * @return 上传结果
     */
    private String uploadFile(String user, MultipartFile file, String apiKey) {
        String url = difyBaseUrl + UPLOAD_FILE_URI;
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            headers.set("Authorization", "Bearer " + apiKey);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            ByteArrayResource resource = new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            };
            body.add("file", resource);
            body.add("user", user);
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
            log.error("文件上传异常，用户: {}, 文件名: {}", user, file.getOriginalFilename(), e);
            return null;
        }
    }

}
