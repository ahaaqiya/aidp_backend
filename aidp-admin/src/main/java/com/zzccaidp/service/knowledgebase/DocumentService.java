package com.zzccaidp.service.knowledgebase;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson.JSONObject;
import com.zzccaidp.AidpDocumentService;
import com.zzccaidp.common.AmazonS3Util;
import com.zzccaidp.common.DateUtil;
import com.zzccaidp.common.SystemParamUtil;
import com.zzccaidp.context.UserInfoContextHolder;
import com.zzccaidp.dao.datasource.OADocAssociationDO;
import com.zzccaidp.dao.knowledgebase.DocTypeDO;
import com.zzccaidp.dao.knowledgebase.DocumentDO;
import com.zzccaidp.exception.BusinessException;
import com.zzccaidp.in.DocumentAddIn;
import com.zzccaidp.mapper.datasource.OADocAssociationMapper;
import com.zzccaidp.mapper.knowledgebase.DataSourceMapper;
import com.zzccaidp.mapper.knowledgebase.DocTypeMapper;
import com.zzccaidp.mapper.knowledgebase.DocumentMapper;
import com.zzccaidp.service.ragFlow.RagFlowService;
import com.zzccaidp.util.CommonExportUtils;
import com.zzccaidp.vo.PageRequest;
import com.zzccaidp.vo.PageResponse;
import com.zzccaidp.vo.knowledgebase.DocumentIn;
import com.zzccaidp.vo.knowledgebase.DocumentOut;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static com.zzccaidp.common.ConfigPropertieCommon.FILE_PATH_SEPARATOR;
import static com.zzccaidp.constants.DateConstant.DATE_PATTERN_DIGIT;
import static com.zzccaidp.enums.ErrCodeEnum.*;

@Slf4j
@Service
public class DocumentService {

    @Autowired
    private DocumentMapper documentMapper;

    @Autowired
    private DocTypeMapper docTypeMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AidpDocumentService aidpDocumentService;

    @Autowired
    private AmazonS3Util amazonS3Util;

    @Autowired
    private RagFlowService ragFlowService;

    @Autowired
    private DataSourceMapper dataSourceMapper;

    @Autowired
    private OADocAssociationMapper oaDocAssociationMapper;


    @Value("${bades.file.aws-s3.bucketName}")
    private String backName;

    @Autowired
    private SystemParamUtil systemParamUtil;

    private static final String OA_UPLOAD_FILE_PATH = "OA_UPLOAD_FILE_PATH";

    /**
     * 根据条件查询文档列表（用于分页查询）
     */
    public PageResponse<DocumentOut> list(PageRequest<DocumentIn> pageRequest) {
        PageResponse<DocumentOut> page = new PageResponse<>();
        // 开启分页并保证下一行查询立即执行
        PageHelper.startPage(pageRequest.getPageNum(), pageRequest.getPageSize());
        // 强制触发查询（通过size()获取实际数据）
        List<DocumentDO> documentDOList = documentMapper.selectByCondition(
                BeanUtil.copyProperties(pageRequest.getData(), DocumentDO.class)
        );

        // 必须立即处理分页结果（PageHelper的PageInfo会自动获取总数）
        PageInfo<DocumentDO> pageInfo = new PageInfo<>(documentDOList);

        ragFlowService.updateProcessingDocumentsStatus(documentDOList);
        List<DocumentOut> outList = BeanUtil.copyToList(pageInfo.getList(), DocumentOut.class);

        // 直接使用PageInfo中的分页信息
        page.setPageNum(pageInfo.getPageNum());
        page.setPageSize(pageInfo.getPageSize());
        page.setTotalPage(pageInfo.getPages());
        page.setTotalCount(pageInfo.getTotal());  // 修复后的正确总数
        page.setRecords(outList);
        page.setSuccessCode();

        return page;
    }

    public DocumentDO findById(Long id) {
        return documentMapper.selectById(id);
    }

    public DocumentDO findByDocId(String docId) {
        return documentMapper.selectByDocId(docId);
    }

    public DocumentDO findByDocIdAndChannel(String docId, String channel) {
        return documentMapper.findByDocIdAndChannel(docId, channel);
    }

    /**
     * 个人知识库专用「业务文档ID + 渠道」查询（与公共 findByDocIdAndChannel 隔离）。
     * 不连 doc_type 表，datasetId 取文档自身的 own_dataset_id。channel 为个人库渠道时使用。
     *
     * @param docId   业务文档ID
     * @param channel 来源渠道
     * @return 个人知识库文档
     */
    public DocumentDO selectPersonalByDocIdAndChannel(String docId, String channel) {
        return documentMapper.selectPersonalByDocIdAndChannel(docId, channel);
    }

    @Transactional(rollbackFor = Exception.class)
    public DocumentDO updateDocType(Long id, String docType, String docTypeName) {
        DocumentDO doc = documentMapper.selectById(id);
        if (doc == null) {
            return null;
        }
        if (docType.equals("pending_document")) {
            throw new RuntimeException("无法从有效数据集移动至人工审核库");
        }
        if (doc.getVectorId() != null && !doc.getVectorId().isEmpty()) {
            boolean success = ragFlowService.deleteRagDocument(doc.getDocId());
            if (!success) {
                log.error("向量库文档删除失败，docId={}", doc.getDocId());
                throw new RuntimeException("更新文档类型时，向量库数据删除失败");
            }
            doc.setVectorId(null);
            doc.setTaskStatus("pending");
            log.info("向量库文档删除成功，docId={}", doc.getDocId());
        }
        doc.setDocType(docType);
        doc.setDocTypeName(docTypeName);
        documentMapper.updateByPrimaryKey(doc);
        return doc;
    }

    @Transactional
    public DocumentDO process(Long id) {
        DocumentDO doc = documentMapper.selectById(id);
        if (doc != null) {
            if (Objects.equals(doc.getDocType(), "pending_document")) {
                throw new BusinessException(M0015);
            }
            documentMapper.updateTaskStatus(id, "processing");
            boolean success = ragFlowService.submitVectorTask(doc.getDocId(), "add");
            if (success) {
                doc.setTaskStatus("processing");
            } else {
                doc.setTaskStatus("failed");
                documentMapper.updateTaskStatus(doc.getId(), "failed");
            }
        }
        return doc;
    }

    @Transactional
    public DocumentDO retry(Long id) {
        DocumentDO doc = documentMapper.selectById(id);
        if (doc != null) {
            documentMapper.updateTaskStatus(id, "processing");
            doc.setTaskStatus("processing");
        }
        return doc;
    }

    @Transactional
    public DocumentDO enableVector(Long id) {
        DocumentDO doc = documentMapper.selectById(id);
        if (doc != null) {
            documentMapper.updateVectorStatus(id, "enabled", true);
            doc.setVectorEnabled("enabled");
            doc.setSwitchStatus(true);
        }
        return doc;
    }

    @Transactional
    public DocumentDO disableVector(Long id) {
        DocumentDO doc = documentMapper.selectById(id);
        if (doc != null) {
            documentMapper.updateVectorStatus(id, "disabled", false);
            doc.setVectorEnabled("disabled");
            doc.setSwitchStatus(false);
        }
        return doc;
    }

    @Transactional
    public DocumentDO cleanVector(Long id) {
        DocumentDO doc = documentMapper.selectById(id);
        if (doc != null) {
            documentMapper.updateTaskStatus(id, "");
            doc.setTaskStatus("");
        }
        return doc;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean delete(Long id) {
        DocumentDO doc = documentMapper.selectById(id);
        if (doc == null) {
            return false;
        }
        try {
            if (doc.getVectorId() != null && !doc.getVectorId().isEmpty()) {
                boolean success = ragFlowService.deleteRagDocument(doc.getDocId());
                if (!success) {
                    log.error("删除ragFlow文件失败");
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    return false;
                }
                doc.setVectorId(null);
            }
            if (doc.getBucketPath() != null && !doc.getBucketPath().isEmpty()) {
                amazonS3Util.deleteFile(doc.getBucketPath(), doc.getBucketName());
                doc.setBucketPath(null);
            }
            documentMapper.updateByPrimaryKey(doc);
            documentMapper.deleteById(id);
        } catch (Exception e) {
            log.error("删除文档失败", e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return false;
        }
        return true;
    }

    @Transactional
    public List<DocumentDO> batchEnableVector(List<Long> ids) {
        return ids.stream().map(this::enableVector).collect(Collectors.toList());
    }

    @Transactional
    public List<DocumentDO> batchDisableVector(List<Long> ids) {
        return ids.stream().map(this::disableVector).collect(Collectors.toList());
    }

    @Transactional
    public List<DocumentDO> batchReprocess(List<Long> ids) {
        return ids.stream().map(this::process).collect(Collectors.toList());
    }

    @Transactional(rollbackFor = Exception.class)
    public DocumentDO updateMetadata(Long id, Map<String, Object> metadata) {
        DocumentDO doc = documentMapper.selectById(id);
        if (doc != null) {
            try {
                String metadataJson = objectMapper.writeValueAsString(metadata);
                Map<String, Object> oldMetadataJson = objectMapper.readValue(doc.getMetadata(),
                        new TypeReference<Map<String, Object>>() {
                        });
                documentMapper.updateMetadata(id, metadataJson);
                if (doc.getVectorId() != null && !doc.getVectorId().isEmpty()) {
                    ragFlowService.updateMetadata(doc.getDocId(), oldMetadataJson);
                }
                doc.setMetadata(metadataJson);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("更新元数据JSON转换失败", e);
            }
        }
        return doc;
    }

    public List<DocumentDO> upload(String docType, Long sourceId, MultipartFile[] files) {
        List<DocumentDO> docs = new ArrayList<>();
        String user = UserInfoContextHolder.getUserInfo();
        LocalDateTime now = LocalDateTime.now();
        try {
            for (MultipartFile file : files) {
                DocumentDO doc = new DocumentDO();
                DocTypeDO docTypeDO = docTypeMapper.selectById(docType);
                doc.setDocId("DOC-" + System.currentTimeMillis() + "-" + docs.size());
                doc.setDataSourceId(sourceId); // 本地上传没有数据源ID
                doc.setChannel("LOCAL"); // 本地上传渠道标识

                String originalFilename = FilenameUtils.getName(file.getOriginalFilename());
                String extension = FilenameUtils.getExtension(originalFilename);
                byte[] fileContent = file.getBytes(); // 优化：大文件需改用流式处理

                String filePath = amazonS3Util.uploadFile(fileContent, extension, backName);
                doc.setName(originalFilename);
                doc.setFileType(getFileType(originalFilename));
                doc.setDocType(docTypeDO.getCode());
                doc.setDocTypeName(docTypeDO.getName());
                doc.setSourceName(dataSourceMapper.selectById(sourceId).getName());
                doc.setSourceStatus("normal"); // 默认正常状态
                doc.setTaskStatus("pending");
                doc.setSwitchStatus(false);
                doc.setVectorEnabled("disabled");
                //doc.setMd5(generateMD5()); // 修复MD5生成逻辑
                doc.setFileSize(Integer.toString(fileContent.length));
                doc.setCreator(user);
                doc.setModifier(user);
                doc.setXskyPath("/xsky/local/" + originalFilename);
                doc.setBucketName(backName); // 默认存储桶名称
                doc.setBucketPath(filePath); // 默认桶内路径
                doc.setVectorId(null); // 初始无向量ID
                doc.setCreateTime(new Date());
                doc.setUpdateTime(new Date());
                doc.setStatus("active");
                doc.setMetadata("{}");

                documentMapper.insert(doc);
                docs.add(doc);
            }
        } catch (IOException e) {
            log.error("文件上传失败", e); // 优化：使用日志框架记录异常
            throw new RuntimeException("文件上传失败", e); // 优化：抛出运行时异常
        }
        return docs;
    }

    public void downloadFile(Long id, HttpServletResponse resp) {
        try {
            DocumentDO documentDO = documentMapper.selectByPrimaryKey(id);
            if (documentDO == null || null == documentDO.getBucketPath() || documentDO.getBucketPath().isEmpty()) {
                log.error("文档不存在");
                return;
            }
            downloadFile(documentDO.getBucketPath(), documentDO.getName(), resp);
        } catch (Exception e) {
            log.error("下载文档失败", e);
        }
    }

    public void downloadFile(String storeFileId, String fileName, HttpServletResponse resp) {

        byte[] fileContent = amazonS3Util.downloadFile(storeFileId, backName);

        if (fileContent == null || fileContent.length == 0) {
            log.error("文件内容为空");
            return;
        }
        try {
            CommonExportUtils.defaultDownLoadFile(fileName, fileContent, resp);
        } catch (IOException e) {
            log.error("下载文档失败", e);
        }
    }

    private String getFileType(String fileName) {
        if (fileName == null) return "other";
        String ext = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        switch (ext) {
            case "pdf":
                return "pdf";
            case "doc":
            case "docx":
                return "docx";
            case "xls":
            case "xlsx":
                return "xlsx";
            case "txt":
                return "txt";
            case "md":
                return "markdown";
            default:
                return "other";
        }
    }

    private String formatFileSize(long size) {
        if (size < 1024) return size + " B";
        if (size < 1024 * 1024) return String.format("%.1f KB", size / 1024.0);
        return String.format("%.1f MB", size / (1024.0 * 1024));
    }

//    private String generateMD5() {
//        String chars = "abcdef0123456789";
//        StringBuilder sb = new StringBuilder();
//        Random random = new Random();
//        for (int i = 0; i < 32; i++) {
//            sb.append(chars.charAt(random.nextInt(chars.length())));
//        }
//        return sb.toString();
//    }


    private String generateFileSize() {
        Random random = new Random();
        double size = 0.5 + (random.nextDouble() * 4.5);
        return String.format("%.1f MB", size);
    }

    public void addDocument(DocumentDO documentDO) {
        documentMapper.insertSelective(documentDO);
    }

    public void updateByDocId(DocumentDO sourceDoc) {
        documentMapper.updateByPrimaryKeySelective(sourceDoc);
    }

    public void deleteByDocId(Long id) {
        documentMapper.deleteById(id);
    }

    public void syncOaDocVerctor(String date) {
        if(Objects.isNull(date) || date.isEmpty()){
            date = DateUtil.getSysDate(DATE_PATTERN_DIGIT);
        }
        //如果文件夹不存在，说明文件不再当前服务器，直接返回。
        String filePath = systemParamUtil.getValue(OA_UPLOAD_FILE_PATH) + systemParamUtil.getValue(FILE_PATH_SEPARATOR) + date;
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            log.info("当前服务器不存在文件目录：{}", filePath);
            return;
        }
        //解压zip
        unzip(filePath);
        try {
            List<DocumentAddIn> documentAddInList = new ArrayList<>();
            //文件权限信息map
            Map<String, List<String>> map = new HashMap<>();
            //读取文件列表信息，删除信息，权限信息。
            readFilePermissionFromCTM(map, filePath);
            //处理新增的文件
            addFileHandler(documentAddInList, map, filePath);
            //处理删除的文件
            delFileHandler(documentAddInList, map, filePath);
            aidpDocumentService.addDocument(documentAddInList);
        } catch (IOException e) {
            log.error("拉取OA上传发文向量化失败", e);
        }
    }

    private void unzip(String destDir) {
        log.info("开始解压OA发文");
        try (ZipFile zipFile = new ZipFile(destDir + systemParamUtil.getValue(FILE_PATH_SEPARATOR) + "FWfj.zip", StandardCharsets.UTF_8)) {
            File destDirectory = new File(destDir);
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry zipEntry = entries.nextElement();
                File outFile = new File(destDir, zipEntry.getName());

                String canonicalDestPath = outFile.getCanonicalPath();
                String canonicalDestDir = destDirectory.getCanonicalPath();
                if (!canonicalDestPath.startsWith(canonicalDestDir + File.separator)) {
                    continue;
                }

                if (zipEntry.isDirectory()) {
                    outFile.mkdirs();
                } else {
                    outFile.getParentFile().mkdirs();
                    try (InputStream is = zipFile.getInputStream(zipEntry)) {
                        FileOutputStream fos = new FileOutputStream(outFile);
                        byte[] bytes = new byte[8192];
                        int len;
                        while ((len = is.read(bytes)) > 0) {
                            fos.write(bytes, 0, len);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("OA发文解压失败");
            throw new RuntimeException(e);
        }
    }

    private void delFileHandler(List<DocumentAddIn> documentAddInList, Map<String, List<String>> map, String filePath) {
        log.info("开始处理文档删除信息");
        try {
            List<String> stringList = Files.readAllLines(Paths.get(filePath + systemParamUtil.getValue(FILE_PATH_SEPARATOR) + "OA_ABODOCPERMISSION_DATA.UTF8.del"), StandardCharsets.UTF_8);
            if (stringList.isEmpty()) {
                return;
            }
            List<OADocAssociationDO> oaDocAssociationDOList = oaDocAssociationMapper.listDocIdByDocIndexId(stringList);
            oaDocAssociationDOList.forEach(oaDocAssociationDO -> {
                try {
                    DocumentAddIn documentAddIn = new DocumentAddIn();
                    documentAddIn.setDocId(oaDocAssociationDO.getDocId());
                    documentAddIn.setChannel("zzccXBGG");
                    documentAddIn.setBackName("zzccXBGG");
                    documentAddIn.setDocOperatorType("delete");
                    documentAddInList.add(documentAddIn);
                } catch (RuntimeException e) {
                    log.error("OA文档废止记录处理失败:{}，", oaDocAssociationDO, e);
                }
            });
        } catch (RuntimeException | IOException e) {
            log.error("处理OA文档废止失败，", e);
        }
    }


    private void addFileHandler(List<DocumentAddIn> documentAddInList, Map<String, List<String>> map, String filePath) throws IOException {
        log.info("开始处理文档信息");
        List<String> stringList = Files.readAllLines(Paths.get(filePath + systemParamUtil.getValue(FILE_PATH_SEPARATOR) + "OA_FWFJ_UTF8.txt"), StandardCharsets.UTF_8);
        stringList.forEach(str -> {
            try {
                String[] strings = str.split("\\|\\|");
                DocumentAddIn documentAddIn = new DocumentAddIn();
                documentAddIn.setDocId(strings[0]);
                documentAddIn.setDocName(strings[3]);
                documentAddIn.setChannel("zzccXBGG");
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("permission", String.join(",", Objects.isNull(map.get(strings[1])) ? new ArrayList<>() : map.get(strings[1])));
                documentAddIn.setMetadata(jsonObject.toJSONString());
                documentAddIn.setBackName("zzccXBGG");
                documentAddIn.setAwsFilePath(filePath + systemParamUtil.getValue(FILE_PATH_SEPARATOR) + strings[1] + systemParamUtil.getValue(FILE_PATH_SEPARATOR) + strings[2] + "." + strings[5]);
                documentAddIn.setDocOperatorType("add");
                documentAddInList.add(documentAddIn);
                oaDocAssociationMapper.insert(new OADocAssociationDO(strings[0], strings[1], new Date()));
            } catch (RuntimeException e) {
                log.error("文档处理失败，{}", str, e);
            }
        });
    }

    /**
     * OA上传权限文件OA_DOCPERMISSION_ALL.txt，第一列是文件目录id，第二列是OA人员ID，第三列是人员工号
     *
     * @param map
     * @param filePath
     */
    private void readFilePermissionFromCTM(Map<String, List<String>> map, String filePath) {
        AtomicInteger i = new AtomicInteger();
        log.info("开始处理文档权限");
        try {
            List<String> stringList = Files.readAllLines(Paths.get(filePath + systemParamUtil.getValue(FILE_PATH_SEPARATOR) + "OA_DOCPERMISSION_UTF8.txt"), StandardCharsets.UTF_8);
            stringList.forEach(var -> {
                try {
                    i.getAndIncrement();
                    log.debug("开始处理第[{}]行权限信息", i);
                    String[] strings = var.split("\\|\\|");
                    List<String> list = map.get(strings[0]);
                    if (Objects.isNull(list)) {
                        list = new ArrayList<>();
                        map.put(strings[0], list);
                    }
                    list.add(strings[2]);
                } catch (Exception e) {
                    log.error("读取第[{}]行权限信息失败，结束本次循环", i);
                }
            });
        } catch (IOException ex) {
            log.error("读取OA权限文件失败");
            throw new RuntimeException(ex);
        }
    }
}