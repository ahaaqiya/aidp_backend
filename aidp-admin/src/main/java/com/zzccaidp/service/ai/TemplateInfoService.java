package com.zzccaidp.service.ai;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.zzccaidp.common.SnowflakeUtil;
import com.zzccaidp.context.UserInfoContextHolder;
import com.zzccaidp.dao.ai.RuleConfigDO;
import com.zzccaidp.dao.ai.TemplateInfoDO;
import com.zzccaidp.dao.ai.TemplateLastFileDO;
import com.zzccaidp.dao.system.UserDO;
import com.zzccaidp.enums.ErrCodeEnum;
import com.zzccaidp.mapper.ai.RuleConfigMapper;
import com.zzccaidp.mapper.ai.TemplateInfoMapper;
import com.zzccaidp.mapper.ai.TemplateLastFileMapper;
import com.zzccaidp.vo.PageResponse;
import com.zzccaidp.vo.ResHeader;
import com.zzccaidp.vo.ai.TemplateInfoVO;
import com.zzccaidp.vo.ai.TemplateListVO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.log4j.Log4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Log4j
public class TemplateInfoService {

    @Autowired
    private TemplateInfoMapper templateInfoMapper;
    //JSON模板中的模板提示词的key
    private static final String TEMPLATE_KEY = "textAreazzccAI";
    //模板内容中占位符
    @Value("${dify.api.docs.analyzing.regexcontent:%%content}")
    private String REGEX_CONTENT;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private RuleConfigMapper ruleConfigMapper;

    @Autowired
    private TemplateLastFileMapper templateLastFileMapper;


    @Transactional
    public ResHeader addTemplateInfoDO(TemplateInfoVO templateInfo) {
        ResHeader response = new ResHeader();
        List<TemplateInfoDO> templateInfoList = templateInfoMapper.select(new TemplateInfoDO()
                .setTemplateName(templateInfo.getTemplateName())
                .setTemplateType(templateInfo.getTemplateType())
                .setCreator(UserInfoContextHolder.getUserInfo()));
        if (!CollectionUtils.isEmpty(templateInfoList)) {
            response.setResResult(ErrCodeEnum.M2013.getErrCode(), ErrCodeEnum.M2013.getErrMsg());
            return response;
        }
        try {
            TemplateInfoDO templateInfoDO = new TemplateInfoDO();
            BeanUtil.copyProperties(templateInfo, templateInfoDO);
            templateInfoDO.setId(SnowflakeUtil.nextIdStr());
            templateInfoDO.setCreator(UserInfoContextHolder.getUserInfo());
            templateInfoDO.setModifier(UserInfoContextHolder.getUserInfo());
            templateInfoDO.setRuleId("");
            // 设置模板信息的状态为未启用状态
            templateInfoDO.setStatus("0");
            //模板类型为提示词模式直接保存提示词内容（0-提示词模式，1-JSON模板）
            if ("0".equals(templateInfo.getTemplateType())) {
                templateInfoDO.setPromptContent(templateInfo.getTemplateContent());
            } else if ("1".equals(templateInfo.getTemplateType())) {
                //将json格式模板内容不为空的转成提示词保存到数据库中
                if (StringUtils.isNotEmpty(templateInfo.getTemplateContent())) {
                    templateInfoDO.setPromptContent(JsonToString(templateInfo.getTemplateContent()));
                }
            }
            templateInfoDO.setCreateTime(LocalDateTime.now());
            templateInfoDO.setModifyTime(LocalDateTime.now());
            UserDO userDO = UserInfoContextHolder.getUser();
            templateInfoDO.setCreator(userDO.getUserName());
            templateInfoDO.setDeptId(userDO.getDeptId());
            templateInfoMapper.insertSelective(templateInfoDO);
            // 关联最后一次使用的fileId;
            this.saveLastFileId(templateInfoDO.getId(), templateInfo.getLastFileId());
            response.setSuccessCode();
        } catch (Exception e) {
            log.error("模板信息添加失败" + e);
            response.setResResult(ErrCodeEnum.M9001.getErrCode(), ErrCodeEnum.M9001.getErrMsg());
        }
        return response;
    }

    private void saveLastFileId(String templateId, String lastFileId) {
        if (StringUtils.isBlank(lastFileId)) {
            return;
        }
        templateLastFileMapper.deleteByTemplateIdAndUserId(templateId, UserInfoContextHolder.getUserInfo());
        TemplateLastFileDO templateLastFileDO = new TemplateLastFileDO();
        templateLastFileDO.setCreateTime(new Date());
        templateLastFileDO.setUserId(UserInfoContextHolder.getUserInfo());
        templateLastFileDO.setId(SnowflakeUtil.nextIdStr());
        templateLastFileDO.setTemplateId(templateId);
        templateLastFileDO.setFileId(lastFileId);
        templateLastFileMapper.insert(templateLastFileDO);
    }


    @Transactional
    public void deleteTemplateInfoDO(TemplateInfoVO templateInfoVo) {
        //判断模板是否被使用
        List<RuleConfigDO> ruleConfigList = ruleConfigMapper.select(new RuleConfigDO().setTemplateId(templateInfoVo.getId()));
        if (!CollectionUtils.isEmpty(ruleConfigList)) {
            //将使用规则的模板id置空
            ruleConfigMapper.updateRuleConfigDOByTemplateId(new RuleConfigDO().setTemplateId(templateInfoVo.getId()));
        }
        //删除模板信息
        templateInfoMapper.delete(new TemplateInfoDO().setId(templateInfoVo.getId()));
        // 删除模板关联文件表
        templateLastFileMapper.deleteByTemplateIdAndUserId(templateInfoVo.getId(), UserInfoContextHolder.getUserInfo());
    }


    public void updateTemplateInfoDO(TemplateInfoVO templateInfoVo) throws JsonProcessingException {
        TemplateInfoDO templateInfo = new TemplateInfoDO();
        BeanUtil.copyProperties(templateInfoVo, templateInfo);
        templateInfo.setModifier(UserInfoContextHolder.getUserInfo());
        templateInfo.setModifyTime(LocalDateTime.now());
        // 将模板信息对象的内容转换为提示词字符串并设置到模板信息实体中
        templateInfo.setPromptContent(JsonToString(templateInfoVo.getTemplateContent()));
        templateInfoMapper.updateByPrimaryKeySelective(templateInfo);
    }


    public PageResponse<TemplateInfoDO> getAllTemplateInfoDOs(TemplateInfoVO templateInfo) {
        PageHelper.startPage(templateInfo.getPageNum(), templateInfo.getPageSize());
        PageResponse<TemplateInfoDO> response = new PageResponse<>();
        // 构造查询条件
        Example example = new Example(TemplateInfoDO.class);
        Example.Criteria criteria = example.createCriteria();
        if (StringUtils.isNotBlank(templateInfo.getTemplateName())) {
            criteria.andLike("templateName", "%" + templateInfo.getTemplateName() + "%");
        }
        if (StringUtils.isNotBlank(templateInfo.getTemplateDesc())) {
            criteria.andLike("templateDesc", "%" + templateInfo.getTemplateDesc() + "%");
        }
        criteria.andEqualTo("creator",UserInfoContextHolder.getUserInfo());
        // 添加排序条件，按创建时间和更新时间倒序
        example.setOrderByClause("modify_time DESC, create_time DESC");
        List<TemplateInfoDO> permissionList = templateInfoMapper.selectByExample(example);
        PageInfo<TemplateInfoDO> pageInfo = new PageInfo<>(permissionList);
        response.setTotalPage(pageInfo.getPages());
        response.setTotalCount(pageInfo.getTotal());
        response.setPageNum(pageInfo.getPageNum());
        response.setPageSize(pageInfo.getPageSize());
        this.getLastFileId(permissionList);
        response.setRecords(permissionList);
        response.setSuccessCode();
        return response;
    }

    private void getLastFileId(List<TemplateInfoDO> permissionList) {
        if (CollectionUtils.isEmpty(permissionList)) {
            return;
        }
        List<TemplateLastFileDO> templateLastFileDOList = templateLastFileMapper.selectByUserId(UserInfoContextHolder.getUserInfo());

        Map<String, String> templateIdReFileIdMap = templateLastFileDOList.stream().collect(Collectors.toMap(TemplateLastFileDO::getTemplateId, TemplateLastFileDO::getFileId, (k1, k2) -> k2));

        permissionList.forEach(permissionInfo -> {
            permissionInfo.setLastFileId(templateIdReFileIdMap.getOrDefault(permissionInfo.getId(), ""));
        });
    }


    public TemplateListVO getAllTemplateList() {
        TemplateListVO response = new TemplateListVO();
        TemplateInfoDO templateInfoBo = new TemplateInfoDO();
        templateInfoBo.setStatus("1");
        UserDO userDO = UserInfoContextHolder.getUser();
        templateInfoBo.setCreator(userDO.getUserName());
        response.setList(templateInfoMapper.select(templateInfoBo));
        response.setSuccessCode();
        return response;
    }

    /**
     * 将json格式的参数配置转换为字符串
     *
     * @param json
     * @return
     */
    public String JsonToString(String json) throws JsonProcessingException {
        if (json == null || json.trim().isEmpty()) {
            log.error("输入的json参数为空");
            return "";
        }
        String result = "";
        log.info("开始处理json参数");
        // 将JSON数组字符串转换为JSONArray对象
        JSONArray jsonArray = new JSONArray(json);
        String template = "";
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (int j = 0; j < jsonArray.size(); j++) {
            JSONObject jsonObject = jsonArray.getJSONObject(j);
            String paramKey = jsonObject.get("paramKey").toString();
            String paramDesc = jsonObject.getOrDefault("paramDesc", "").toString();
            String paramKeyword = jsonObject.getOrDefault("paramKeyword", "").toString();
            if (TEMPLATE_KEY.equals(paramKey)) {
                template = paramDesc;
                continue;
            }
            ++i;
            sb.append(i).append(". ").append(paramKey).append(":");
            if (StringUtils.isNotBlank(paramKeyword)) {
                sb.append("提取").append(paramKeyword).append("的相关内容\n");
            }
            if (StringUtils.isNotBlank(paramDesc)) {
                sb.append(nullToEmpty(paramDesc)).append("\n");
            }
            if (StringUtils.isAllBlank(paramDesc, paramKeyword)) {
                sb.setLength(sb.length() - 1);
                sb.append("\n");
            }
        }
        if (template.contains(REGEX_CONTENT)) {
            result = template.replaceAll(REGEX_CONTENT, sb.toString());
        } else {
            log.error("模板中未找到占位符,仅使用字段提取描述为提示词");
            result = sb.toString();
        }
        log.info("拼接完成后提示词为 ：" + result);
        return result;
    }

    private String nullToEmpty(String str) {
        return str != null ? str : "";
    }
}
