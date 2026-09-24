package com.zzccaidp.controller.system;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.DeleteObjectsRequest;
import com.amazonaws.services.s3.model.DeleteObjectsResult;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.zzccaidp.common.SnowflakeUtil;
import com.zzccaidp.context.UserInfoContextHolder;
import com.zzccaidp.dao.ai.SysFileInfoDO;
import com.zzccaidp.enums.ErrCodeEnum;
import com.zzccaidp.exception.BusinessException;
import com.zzccaidp.mapper.ai.SysFileInfoMapper;
import com.zzccaidp.service.ai.SysFileInfoService;
import com.zzccaidp.vo.ResHeader;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.activation.MimetypesFileTypeMap;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import static com.zzccaidp.enums.ErrCodeEnum.M9995;

/**
 * @author bades
 */
@RequestMapping("/file")
@RestController
@Api(value = "文件上传页面")
@Slf4j
public class SysFileDataController {

    @Autowired
    private SysFileInfoService sysFileInfoService;

    @Autowired
    private SysFileInfoMapper sysFileInfoMapper;

    @Autowired
    private AmazonS3 amazonS3;

    @Value("${bades.file.aws-s3.bucketName}")
    private String bucketName;

    @ApiOperation(value = "下载")
    @GetMapping(value = "/download")
    public void download(@RequestParam("fileId") String fileId, HttpServletResponse response) throws UnsupportedEncodingException {
        try {
            // 获取文件
            SysFileInfoDO fileInfo = sysFileInfoService.getSysFileInfo(fileId);

            // 拉取桶文件
            S3Object object = amazonS3.getObject(bucketName, fileInfo.getFilePath());

            StreamUtils.copy(object.getObjectContent(), response.getOutputStream());
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Content-Disposition", "attachment;filename=\"" + URLEncoder.encode(fileInfo.getFileName(), "UTF-8") + "\"");
            response.addHeader("Content-Length", String.valueOf(object.getObjectMetadata().getContentLength()));
            response.setContentType(new MimetypesFileTypeMap().getContentType(fileInfo.getFileName()));
        }catch (Exception e){
            throw new BusinessException(M9995);
        }
    }



    @ApiOperation("文件上传")
    @PostMapping("/upload")
    public ResHeader upload(MultipartFile file) throws IOException {
        ResHeader out = new ResHeader();
        String id = String.valueOf(SnowflakeUtil.nextId());
        ObjectMetadata metadata = new ObjectMetadata();
        String[] strs = Objects.requireNonNull(file.getOriginalFilename()).split("\\.");
        metadata.setContentLength(file.getSize());
        amazonS3.putObject(bucketName, "chat/" + id+"." + strs[strs.length-1], file.getInputStream(), metadata);
        SysFileInfoDO sysFileInfoDO = new SysFileInfoDO();
        sysFileInfoDO.setFileId(id);
        sysFileInfoDO.setFilePath("chat/" + id+"." + strs[strs.length-1]);
        sysFileInfoDO.setFileSize(String.valueOf(file.getSize()));
        sysFileInfoDO.setFileName(file.getOriginalFilename());
        sysFileInfoDO.setFileStorageTyps("aws");
        sysFileInfoDO.setCreateTime(LocalDateTime.now());
        //文件保存中的文件所属业务类型，用以区分文件是哪些业务的，方便查询用户下文件列表
        sysFileInfoDO.setFileBusinessTyps("chat");
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
        sysFileInfoDO.setScratchFile(0);
        sysFileInfoService.insertFile(sysFileInfoDO);
        log.info("上传解析文件到aws成功，文件信息[{}]", sysFileInfoDO);
        out.setSuccessCode();
        out.setResultmsg(id);
        return out;
    }

    @ApiOperation("文件删除")
    @PostMapping("/delete")
    public ResHeader delete(@RequestParam(value = "fileId") List<String> fileIds) throws IOException {
        ResHeader response = new ResHeader();
        try {
            // 批量查询文件信息
            List<SysFileInfoDO> sysFileInfoList = sysFileInfoMapper.selectByPrimaryKeys(fileIds);

            // 提取需要批量删除的桶文件路径
            String[] keys = sysFileInfoList.stream()
                    .filter(info -> Objects.nonNull(info.getFilePath())
                            && !info.getFilePath().trim().isEmpty())
                    .map(SysFileInfoDO::getFilePath)
                    .filter(key -> !key.trim().isEmpty())
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
            response.setSuccessCode();
        } catch (Exception e) {
            response.setResultcode(ErrCodeEnum.M9017.getErrCode());
            response.setResultmsg(ErrCodeEnum.M9017.getErrMsg());
        }
        return response;
    }

}
