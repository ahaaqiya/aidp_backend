package com.zzccaidp.service.ai;

import cn.hutool.core.io.IoUtil;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.DeleteObjectsRequest;
import com.amazonaws.services.s3.model.DeleteObjectsResult;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.zzccaidp.common.SnowflakeUtil;
import com.zzccaidp.common.BeanUtils;
import com.zzccaidp.context.UserInfoContextHolder;
import com.zzccaidp.dao.ai.DifyApiKeyConfigDO;
import com.zzccaidp.dao.ai.RuleConfigDO;
import com.zzccaidp.enums.ErrCodeEnum;
import com.zzccaidp.exception.BusinessException;
import com.zzccaidp.mapper.ai.DifyApiKeyConfigMapper;
import com.zzccaidp.mapper.ai.RuleConfigMapper;
import com.zzccaidp.vo.PageResponse;
import com.zzccaidp.vo.ai.*;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import tk.mybatis.mapper.entity.Example;

import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Slf4j
public class RuleConfigService{

    @Autowired
    private RuleConfigMapper ruleConfigMapper;

    @Autowired
    private AmazonS3 amazonS3;

    @Value("${bades.file.aws-s3.bucketName}")
    private String bucketName;

    @Value("${aws.uploadConfigPath}")
    private String uploadConfigPath = "upload_fileconfig/";


    @Autowired
    private DifyApiKeyConfigMapper difyApiKeyConfigMapper;


    private static final String FLAG_TQZD = "TQZD";
    private static final String FLAG_ZDYS = "ZDYS";
    private static final String FLAG_TSC = "TSC";
    private static final String EXTRACTFIELD_FILE = "extractFieldFile";
    private static final String FIELDMAPPING_FILE = "fieldMappingFile";
    private static final String PROMPT_FILE = "promptFile";


    
    public PageResponse<RuleConfigDO> getList(RuleConfigVo ruleConfigVo) {
        PageHelper.startPage(ruleConfigVo.getPageNum(), ruleConfigVo.getPageSize());
        PageResponse<RuleConfigDO> response = new PageResponse<>();
        // 构造查询条件
        Example example = new Example(RuleConfigDO.class);
        Example.Criteria criteria = example.createCriteria();
        if (StringUtils.isNotBlank(ruleConfigVo.getRuleId())) {
            criteria.andLike("ruleId", "%" + ruleConfigVo.getRuleId() + "%");
        }
        if (StringUtils.isNotBlank(ruleConfigVo.getRuleName())) {
            criteria.andLike("ruleName", "%" + ruleConfigVo.getRuleName() + "%");
        }
        if (StringUtils.isNotBlank(ruleConfigVo.getAppid())) {
            criteria.andLike("appid", "%" + ruleConfigVo.getAppid() + "%");
        }
        if (StringUtils.isNotBlank(ruleConfigVo.getRuleDescription())) {
            criteria.andLike("ruleDescription", "%" + ruleConfigVo.getRuleDescription() + "%");
        }
        // 添加排序条件，按创建时间和更新时间倒序
        example.setOrderByClause("modified_time DESC,created_time DESC");
        List<RuleConfigDO> permissionList = ruleConfigMapper.selectByExample(example);
        PageInfo<RuleConfigDO> pageInfo = new PageInfo<>(permissionList);
        response.setTotalPage(pageInfo.getPages());
        response.setTotalCount(pageInfo.getTotal());
        response.setPageNum(pageInfo.getPageNum());
        response.setPageSize(pageInfo.getPageSize());
        response.setRecords(permissionList);
        response.setSuccess();
        return response;
    }

    /**
     * 查询所有规则信息
     * @param
     * @return
     */
    
    public RuleConfigListVo getAll(String type) {
        RuleConfigListVo result = new RuleConfigListVo();
        List<RuleConfigInfoVo> ruleConfigListVoList = new ArrayList<>();
        // 构造查询条件
        Example example = new Example(RuleConfigDO.class);
        // 添加排序条件，按创建时间和更新时间倒序
        Example.Criteria criteria = example.createCriteria();
        //添加状态条件为已启用
        criteria.andEqualTo("status", "1");
        example.setOrderByClause("created_time DESC, modified_time DESC");
        List<RuleConfigDO> permissionList = ruleConfigMapper.selectByExample(example);
        permissionList.forEach(ruleConfig -> {
            RuleConfigInfoVo ruleConfigVo = new RuleConfigInfoVo();
            BeanUtils.copy(ruleConfig, ruleConfigVo);
            ruleConfigVo.setRuleId(ruleConfig.getRuleId());
            ruleConfigVo.setRuleDescription(ruleConfig.getRuleDescription());
            ruleConfigVo.setDifyApiKeyId(ruleConfig.getDifyApiKeyId());
            ruleConfigListVoList.add(ruleConfigVo);
        });

        if (StringUtils.isNotBlank(type)) {
            // 构造查询条件
            Example difyApiKeyExample = new Example(DifyApiKeyConfigDO.class);
            // 添加排序条件，按创建时间和更新时间倒序
            Example.Criteria difyApiKeyCriteria = difyApiKeyExample.createCriteria();

            //添加状态条件为已启用
            difyApiKeyCriteria.andEqualTo("keyType", type);
            List<DifyApiKeyConfigDO> difyApiKeyConfigDOlist = difyApiKeyConfigMapper.selectByExample(difyApiKeyExample);
            if (CollectionUtils.isNotEmpty(difyApiKeyConfigDOlist)) {
                // 获取对应的keyId
                List<String> keyIdList = difyApiKeyConfigDOlist.stream().map(DifyApiKeyConfigDO::getId).collect(Collectors.toList());
                ruleConfigListVoList.removeIf(ruleConfigInfoVo -> keyIdList.contains(ruleConfigInfoVo.getDifyApiKeyId()));
            }
        }

        result.setSuccessCode();
        result.setRuleConfigListVo(ruleConfigListVoList);
        return result;
    }

    
    public RuleConfigInfoVo getRuleConfigInfo(RuleConfigVo ruleConfigVo) {
        RuleConfigInfoVo ruleConfigInfoVo = new RuleConfigInfoVo();
        RuleConfigDO ruleConfig = new RuleConfigDO();
        BeanUtils.copy(ruleConfigVo, ruleConfig);
        RuleConfigDO ruleConfig1 = ruleConfigMapper.selectOne(ruleConfig);
        BeanUtils.copy(ruleConfig1, ruleConfigInfoVo);
        return ruleConfigInfoVo;
    }

    
    public void add(RuleConfigVo ruleConfigVo) {
        // 判断规则id是否已经存在
        RuleConfigDO ruleConfigQuery = new RuleConfigDO();
        ruleConfigQuery.setRuleId(ruleConfigVo.getRuleId());
        List<RuleConfigDO> ruleConfigs = ruleConfigMapper.select(ruleConfigQuery);
        if(CollectionUtils.isNotEmpty(ruleConfigs)) {
            throw new BusinessException(ErrCodeEnum.M9014);
        }
        try{
            String user = StringUtils.isBlank(UserInfoContextHolder.getUserInfo()) ? "" :UserInfoContextHolder.getUserInfo();
            RuleConfigDO ruleConfig = new RuleConfigDO();
            BeanUtils.copy(ruleConfigVo, ruleConfig);
            String id = String.valueOf(SnowflakeUtil.nextId());
            ruleConfig.setId(id);
            // 设置规则配置的状态为未启用状态
            ruleConfig.setStatus("0");
            ruleConfig.setCreatedTime(LocalDateTime.now());
            ruleConfig.setCreatedBy(user);
            ruleConfig.setModifiedTime(LocalDateTime.now());
            ruleConfig.setModifiedBy(user);
            ruleConfigMapper.insert(ruleConfig);
        } catch (Exception e) {
            log.error("新增文档识别规则配置失败", e);
            throw new BusinessException(ErrCodeEnum.ERROR);
        }
    }

    
    @Transactional
    public void update(RuleConfigVo ruleConfigVo) {
        if (UserInfoContextHolder.getUserInfo() == null) {
            throw new BusinessException(ErrCodeEnum.M0401);
        }
        String user = StringUtils.defaultIfBlank(UserInfoContextHolder.getUserInfo(), "");
        if(StringUtils.isBlank(ruleConfigVo.getId())) {
            throw new BusinessException(ErrCodeEnum.M9015);
        }
        // 判断规则id是否已经存在
        RuleConfigDO ruleConfigOld = ruleConfigMapper.selectByPrimaryKey(ruleConfigVo.getId());
        if (Objects.isNull(ruleConfigOld)) {
            throw new BusinessException(ErrCodeEnum.M9015);
        }
        try {
            // 处理文件修改删除逻辑
            processFileDeletionIfNeeded(ruleConfigOld, ruleConfigVo);
            // 更新规则配置
            RuleConfigDO ruleConfig = new RuleConfigDO();
            BeanUtils.copy(ruleConfigVo, ruleConfig);
            ruleConfig.setModifiedTime(LocalDateTime.now());
            ruleConfig.setModifiedBy(user);
            ruleConfigMapper.updateByPrimaryKeySelective(ruleConfig);
        } catch (Exception e) {
            // 记录日志或进行异常处理
            log.error("规则场景配置修改失败", e);
            throw new BusinessException(ErrCodeEnum.M9001);
        }
    }

    private void processFileDeletionIfNeeded(RuleConfigDO oldConfig, RuleConfigVo newConfig) {
        deleteFileIfChanged(oldConfig.getExtractFieldFileid(), newConfig.getExtractFieldFileid());
        deleteFileIfChanged(oldConfig.getFieldMappingFileid(), newConfig.getFieldMappingFileid());
        deleteFileIfChanged(oldConfig.getPromptFileid(), newConfig.getPromptFileid());
    }

    private void deleteFileIfChanged(String oldFileId, String newFileId) {
        //如果旧文件ID存在，新文件ID为空，则删除旧文件
        if (!StringUtils.isBlank(oldFileId) && StringUtils.isBlank(newFileId)) {
            amazonS3.deleteObject(bucketName, oldFileId);
        }
    }

    
    public void updateByRuleId(RuleConfigVo ruleConfigVo) {
        RuleConfigDO ruleConfig = new RuleConfigDO();
        BeanUtils.copy(ruleConfigVo, ruleConfig);
        String user = StringUtils.isBlank(UserInfoContextHolder.getUserInfo()) ? "" :UserInfoContextHolder.getUserInfo();
        ruleConfig.setModifiedTime(LocalDateTime.now());
        ruleConfig.setModifiedBy(user);
        ruleConfigMapper.updateRuleConfigDOByRuleId(ruleConfig);
    }

    
    @Transactional
    public void delete(RuleConfigVo ruleConfigVo) {
        RuleConfigDO ruleConfig = ruleConfigMapper.selectByPrimaryKey(ruleConfigVo.getId());
        if (Objects.isNull(ruleConfig)) {
            throw new BusinessException(ErrCodeEnum.M9015);
        }
        // 需要删除的AWS文件key
        List<String> keys = prepareKeysForDeletion(ruleConfig);
        try {
            if (!keys.isEmpty()) { // 只有当存在要删除的键时才执行删除操作
                // 批量删除S3对象
                DeleteObjectsRequest deleteObjectsRequest = new DeleteObjectsRequest(bucketName)
                        .withKeys(keys.toArray(new String[0]));
                DeleteObjectsResult deleteObjectsResult = amazonS3.deleteObjects(deleteObjectsRequest);
                deleteObjectsResult.getDeletedObjects().forEach(deletedObject -> {
                    log.info("删除S3对象成功: {}", deletedObject.getKey());
                });
            }
            // 删除数据库记录
            ruleConfigMapper.deleteByPrimaryKey(ruleConfigVo.getId());
        } catch (AmazonServiceException e) {
            // Amazon S3 specific exception.
            log.error("删除S3对象出错: {}", keys, e);
            throw new BusinessException(translateAmazonS3Exception(e));
        } catch (Exception e) {
            // 其他异常
            log.error("删除过程中发生错误", e);
            throw new BusinessException(ErrCodeEnum.ERROR, "删除错误", e);
        }
    }

    private List<String> prepareKeysForDeletion(RuleConfigDO ruleConfig) {
        return Arrays.asList(
                        ruleConfig.getExtractFieldFileid(),
                        ruleConfig.getFieldMappingFileid(),
                        ruleConfig.getPromptFileid())
                .stream()
                .filter(StringUtils::isNotBlank) // 过滤掉空字符串
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * 根据Amazon S3异常类型映射到合适的业务异常。
     * @param e Amazon S3异常
     * @return 映射后的业务异常
     */
    private ErrCodeEnum translateAmazonS3Exception(AmazonServiceException e) {
        // 根据e的错误码或者错误类型，返回不同的ErrCodeEnum值
        // 这里只是一个示例，具体实现需要根据实际的业务需求和Amazon S3的异常类型来设计
        if ("NoSuchKey".equals(e.getErrorCode())) {
            return ErrCodeEnum.M9017;
        }
        return ErrCodeEnum.ERROR;
    }

    /**
     * 文件上传
     * @param file
     * @param flag
     * @param ruleid
     * @param request
     * @return
     */

    public ConfigfileUploadOut upload(MultipartFile file, String flag, String ruleid, HttpServletRequest request) {
        log.info("上传配置文件 start！");
        ConfigfileUploadOut uploadOut = new ConfigfileUploadOut();
        //fileid为文件在桶内绝对路径
        String fileid = "";
        // 防止路径遍历攻击，确保没有 "..",“/” 等相对路径元素
        if (ruleid.contains("..") ||  ruleid.contains("/")) {
            throw new BusinessException(ErrCodeEnum.M9002);
        }
        if (flag.isEmpty()) {
            log.info("上传文件标识符为空！");
            throw new BusinessException(ErrCodeEnum.M9016);
        }
        if (file.isEmpty()) {
            log.info("上传配置文件为空！");
            throw new BusinessException(ErrCodeEnum.M1035);
        }
        try {
            //如果fileid不为空，则删除原来的文件
            if (!StringUtils.isBlank(fileid)) {
                //根据fileid删除文件
                amazonS3.deleteObject(bucketName, fileid);
            }
            //原始文件名
            String orgName = file.getOriginalFilename();
            //文件类型
            String fileType = orgName.substring(orgName.lastIndexOf(".") + 1);
            // 编码文件名防止中文路径
            String encodedFileName = URLEncoder.encode(file.getOriginalFilename(), StandardCharsets.UTF_8.toString());
            RuleConfigVo ruleConfigVo = new RuleConfigVo();
            ruleConfigVo.setRuleId(ruleid);
            //上传的提取字段文件
            if(flag.equals(FLAG_TQZD)){
                String extractFieldFilename = encodedFileName;
                //上传提取字段文件到桶的绝对路径
                fileid = uploadConfigPath+ruleid+"/"+FLAG_TQZD+"/"+extractFieldFilename;
                ruleConfigVo.setExtractFieldFileid(fileid);
            } else if (flag.equals(FLAG_ZDYS)) {
                String fieldMappingFileName = encodedFileName;
                //上传字段映射文件到桶的绝对路径
                fileid = uploadConfigPath+ruleid+"/"+FLAG_ZDYS+"/"+fieldMappingFileName;
                ruleConfigVo.setFieldMappingFileid(fileid);
            } else if (flag.equals(FLAG_TSC)) {
                String promptFileName = encodedFileName;
                //上传字段映射文件到桶的绝对路径
                fileid = uploadConfigPath + ruleid+ "/" +FLAG_TSC+"/"+promptFileName;
                ruleConfigVo.setPromptFileid(fileid);
            } else {
                log.info("只能上传提取字段、字段映射、提示词配置文件！");
                throw new BusinessException(ErrCodeEnum.M1035);
            }
            //ObjectMetadata里面
            ObjectMetadata metadata = new ObjectMetadata();
            //使用流上传的话，不设置文件长度会有WARN警告日志
            metadata.setContentLength(file.getSize());
            amazonS3.putObject(bucketName, fileid, file.getInputStream(), metadata);
            //更新规则配置信息
            this.updateByRuleId(ruleConfigVo);
            log.info("上传文件结束，更新配置信息！"+ruleConfigVo.toString());
            uploadOut.setFileid(fileid);
            uploadOut.setSuccessCode();
            return uploadOut;
        } catch (AmazonServiceException e) {
            // Amazon S3异常
            log.error("上传文件失败，请检查！"+e);
            throw new BusinessException(translateAmazonS3Exception(e));
        } catch (Exception e) {
            log.info("上传文件失败，请检查！"+e);
            throw new BusinessException(ErrCodeEnum.M1035);
        }
    }

    /**
     * 文件下载
     * @param configfileDownLoadIn
     * @return
     */
    
    public ResponseEntity<?> download(ConfigfileDownLoadIn configfileDownLoadIn) {
        log.info("文档识别场景规则配置下载文件 start！");
        if (StringUtils.isBlank(configfileDownLoadIn.getFileid())) { // 防止目录遍历攻击
            log.error("fileid为空或非法！");
            throw new BusinessException(ErrCodeEnum.M1036);
        }
        InputStream inputStream = null;
        S3Object object = null;
        try {
            //获取文件
            object = amazonS3.getObject(bucketName, configfileDownLoadIn.getFileid());
            if (object == null) {
                log.error("文件不存在: {}", configfileDownLoadIn.getFileid());
                throw new BusinessException(ErrCodeEnum.M1036);
            }
            //获取文件流
            inputStream = object.getObjectContent();
            byte[] fileByte = null;
            fileByte = IoUtil.readBytes(inputStream);
            //解析filename
            String[] sArr = configfileDownLoadIn.getFileid().split("/");
            if (sArr.length == 0) {
                log.error("解析fileid失败: {}", configfileDownLoadIn.getFileid());
                throw new BusinessException(ErrCodeEnum.M1036);
            }
            String fileName = sArr[sArr.length - 1];
            log.info("综合管理平台下载配置文件名[{}]", fileName);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                    .body(fileByte);
        } catch (Exception e) {
            log.error("下载文件失败，请检查！", e);
            throw new BusinessException(ErrCodeEnum.M1036, e);
        } finally {
            if (object != null) {
                try {
                    object.close();
                } catch (Exception e) {
                    log.warn("关闭S3Object失败", e);
                }
            }
        }
    }

    /**
     * @param configfileOperateIn
     */
    
    @Transactional
    public void deleteFile(ConfigfileOperateIn configfileOperateIn) {
        log.info("删除aws文件 start！ "+configfileOperateIn.getFileid());
        try{
            RuleConfigDO ruleConfig = ruleConfigMapper.selectByPrimaryKey(configfileOperateIn.getId());
            if(Objects.isNull(ruleConfig)){
                log.error("规则配置信息不存在！");
                throw new BusinessException(ErrCodeEnum.M9017);
            }
            //删除文件
            amazonS3.deleteObject(bucketName, configfileOperateIn.getFileid());
            //更新规则配置信息修改删除的文件fileid
            if(configfileOperateIn.getFlag().contains(FLAG_TQZD)) {
                ruleConfig.setExtractFieldFileid("");
            } else if (configfileOperateIn.getFlag().contains(FLAG_ZDYS)) {
                ruleConfig.setFieldMappingFileid("");
            } else if (configfileOperateIn.getFlag().contains(FLAG_TSC)){
                ruleConfig.setPromptFileid("");
            }
            ruleConfig.setModifiedTime(LocalDateTime.now());
            ruleConfigMapper.updateByPrimaryKeySelective(ruleConfig);
        } catch (Exception e) {
            log.error("删除aws文件失败！", e);
            throw new BusinessException(ErrCodeEnum.M9017);
        }

    }


}
