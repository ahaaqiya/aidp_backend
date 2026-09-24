package com.zzccaidp.service.ai;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.DeleteObjectsRequest;
import com.amazonaws.services.s3.model.DeleteObjectsResult;
import com.zzccaidp.context.UserInfoContextHolder;
import com.zzccaidp.dao.ai.SysFileInfoDO;
import com.zzccaidp.mapper.ai.AiFileResultMapper;
import com.zzccaidp.mapper.ai.SysFileInfoMapper;
import com.zzccaidp.enums.ErrCodeEnum;
import com.zzccaidp.exception.BusinessException;
import com.zzccaidp.mapper.ai.TemplateLastFileMapper;
import com.zzccaidp.service.system.SystemParamsService;
import com.zzccaidp.vo.ResHeader;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static com.zzccaidp.constants.SystemParamsConstant.AI_FILE_UPLOAD_MAX;

/**
 * @Description: TODO
 * @Author: WB233500
 * @Createtime: 16:05
 * @Version: 1.0
 */
@Service
@Log4j
public class SysFileInfoService {
    @Autowired
    private AmazonS3 amazonS3;

    @Value("${bades.file.aws-s3.bucketName}")
    private String bucketName;
    @Autowired
    private SysFileInfoMapper sysFileInfoMapper;
    @Autowired
    private AiFileResultMapper aiFileResultMapper;
    @Autowired
    private SystemParamsService sysParamsService;
    @Autowired
    private TemplateLastFileMapper templateLastFileMapper;

    
    public void insertFile(SysFileInfoDO sysFileInfoDO) {
        sysFileInfoMapper.insert(sysFileInfoDO);
    }

    
    public List<SysFileInfoDO> selectAiFileByUser(String userId) {
        return sysFileInfoMapper.selectAiFileByUser("aiFile", userId);
    }


    /**
     * 根据桶的 key 删除文件
     *
     * @param fileIds 文件 ID 列表
     */
    
    @Transactional
    public ResHeader deleteAiFileByID(List<String> fileIds) {
        ResHeader response = new ResHeader();
        try {
            // 批量查询文件信息
            List<SysFileInfoDO> sysFileInfoList = sysFileInfoMapper.selectByPrimaryKeys(fileIds);
            // 提取需要批量删除的桶文件路径
            String[] keys = sysFileInfoList.stream()
                    .map(SysFileInfoDO::getFilePath)  // 获取字段值
                    .filter(Objects::nonNull)    // 过滤掉null值
                    .toArray(String[]::new);     // 转换为String数组

            // 批量删除S3对象
            DeleteObjectsRequest deleteObjectsRequest = new DeleteObjectsRequest(bucketName)
                    .withKeys(keys);
            DeleteObjectsResult deleteObjectsResult = amazonS3.deleteObjects(deleteObjectsRequest);
            deleteObjectsResult.getDeletedObjects().forEach(deletedObject -> {
                log.info("删除S3对象成功:" + deletedObject.getKey());
            });
            //删除数据库记录
            sysFileInfoMapper.deleteByPrimaryKeys(fileIds);

            //删除文件对应的解析结果
            aiFileResultMapper.deleteByPrimaryKeys(fileIds);
            response.setSuccessCode();
        } catch (Exception e) {
            response.setResultcode(ErrCodeEnum.M9017.getErrCode());
            response.setResultmsg(ErrCodeEnum.M9017.getErrMsg());
        }
        return response;
    }

    
    @Transactional
    public ResHeader deleteAiFileByID(String fileId, String templateId) {
        templateLastFileMapper.deleteByTemplateIdAndUserId(templateId, UserInfoContextHolder.getUserInfo());
        return this.deleteAiFileByID(Collections.singletonList(fileId));
    }

    
    public void updateFileInfo(List<SysFileInfoDO> sysFileInfoDOList) {
        //获取每个人可以上传的附件数量
        Integer size = Integer.parseInt(sysParamsService.getSysParam(AI_FILE_UPLOAD_MAX).getParamsValue());
        List<SysFileInfoDO> sysFileInfoDOS = this.selectAiFileByUser(UserInfoContextHolder.getUserInfo());
        Integer count = (int) sysFileInfoDOS.stream()
                .filter(sysFileInfoDO -> Integer.valueOf(0).equals(sysFileInfoDO.getScratchFile())).count();
        if (size.compareTo(count) <= 0) {
            throw new BusinessException(ErrCodeEnum.M4003);
        }
        sysFileInfoDOList.forEach(sysFileInfoDO -> sysFileInfoMapper.updateFileInfo(sysFileInfoDO));
    }

    
    public List<String> selectScratchFileByUserName() {
        String userName = UserInfoContextHolder.getUserInfo();
        return sysFileInfoMapper.selectScratchFileByUserName(userName);
    }

    
    public SysFileInfoDO getSysFileInfo(String fileId) {
        return sysFileInfoMapper.selectByPrimaryKey(fileId);
    }
}
