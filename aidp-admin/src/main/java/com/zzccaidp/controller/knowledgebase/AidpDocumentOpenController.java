package com.zzccaidp.controller.knowledgebase;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zzccaidp.AidpDocumentService;
import com.zzccaidp.common.FileTypeDetector;
import com.zzccaidp.common.RedisUtil;
import com.zzccaidp.enums.ErrCodeEnum;
import com.zzccaidp.exception.BusinessException;
import com.zzccaidp.in.DocumentAddIn;
import com.zzccaidp.out.DocumentAddOut;
import com.zzccaidp.util.HMacUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;

/**
 * 文档推送 HTTP 开放接口（供 OA 等外部系统以 JSON 方式调用）
 *
 * <p>安全设计说明（HTTP 明文场景下的 HMAC 签名鉴权）：</p>
 * <ul>
 *     <li>签名鉴权：请求头携带 X-Timestamp / X-Nonce / X-Sign，密钥只存在调用方与服务端，永不上网，防伪造/防篡改/防重放；</li>
 *     <li>签名算法：X-Sign = HMACSHA256(X-Timestamp + "|" + X-Nonce + "|" + sha256hex(body)，共享密钥)，双方均使用 UTF-8 编码、hex 小写；</li>
 *     <li>防重放：时间戳偏差超过 5 分钟直接拒绝；nonce 通过 Redis SETNX 原子去重（同一 nonce 仅可使用一次）；</li>
 *     <li>入参校验：操作类型仅允许 add/update/delete、文件后缀走系统支持白名单、S3 路径非空，防止任意路径转发等恶意输入；</li>
 *     <li>登录白名单：本路径需加入 Redis 系统参数 LOGIN_WHITE_URI（经 /sysparams 接口维护），不依赖登录会话。</li>
 * </ul>
 */
@RestController
@RequestMapping("/document/open")
@Slf4j
public class AidpDocumentOpenController {

    /** 允许的操作类型集合（与 AidpDocumentServiceImpl 处理逻辑保持一致） */
    private static final Set<String> OPERATOR_TYPES =
            new HashSet<>(Arrays.asList("add", "update", "delete"));

    /** 签名时间戳允许偏差窗口：5 分钟（超出即视为过期重放） */
    private static final long SIGN_TIMEOUT_MS = 5 * 60 * 1000L;

    /** nonce 去重 key 前缀（Redis 中：zzccAIDP_OPEN_NONCE_{nonce}） */
    private static final String NONCE_KEY_PREFIX = "zzccAIDP_OPEN_NONCE_";

    /** 入参校验剔除明细总量截断上限（防止整批非法时明细撑爆返回体与日志） */
    private static final int REJECT_DETAIL_MAX_LEN = 2000;

    /** 共享密钥（配置项 aidp.open.api-key），仅调用方与本服务持有，绝不随请求传输 */
    @Value("${aidp.open.api-key}")
    private String openApiKey;

    /** 单次推送文档数量上限（配置项 aidp.open.max-doc-count，默认 100） */
    @Value("${aidp.open.max-doc-count:100}")
    private int maxDocCount;

    @Autowired
    private AidpDocumentService aidpDocumentService;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 文档推送（新增/更新/删除 RAG 知识库，异步向量化版）
     *
     * <p>add/update：本接口同步完成「校验 + S3 转存 + 变更记录 + 落库」后立即返回，
     * RagFlow 向量化由后台单线程池逐条补做（状态经管理后台文档列表 task_status 观察）；
     * delete：仍在本请求内同步完成全流程。</p>
     *
     * @param request   当前请求（用于审计来源 IP）
     * @param timestamp 请求头 X-Timestamp：调用方当前毫秒时间戳
     * @param nonce     请求头 X-Nonce：每次请求随机生成的一次性随机串
     * @param sign      请求头 X-Sign：HMAC 签名，算法见类注释
     * @param rawBody   原始 JSON 请求体（参与签名，需与调用方计算签名的字节完全一致）
     * @return 落库受理结果：successSum/failSum/resultcode/resultmsg/docIds（仅反映落库受理，不代表向量化完成）
     */
    @PostMapping("/add")
    public DocumentAddOut addDocument(HttpServletRequest request,
                                      @RequestHeader(value = "X-Timestamp", required = false) String timestamp,
                                      @RequestHeader(value = "X-Nonce", required = false) String nonce,
                                      @RequestHeader(value = "X-Sign", required = false) String sign,
                                      @RequestBody(required = false) String rawBody) {
        long startTime = System.currentTimeMillis();
        String clientIp = getClientIp(request);
        try {
            // 1、HMAC 签名鉴权：请求头完整性 → 时间戳窗口 → 签名比对 → nonce 去重
            verifySignature(timestamp, nonce, sign, rawBody);
            // 2、反序列化请求体
            List<DocumentAddIn> documentAddInList = parseBody(rawBody);
            // 3、入参非空校验
            if (documentAddInList == null || documentAddInList.isEmpty()) {
                throw new BusinessException(ErrCodeEnum.M1001);
            }
            // 4、单次数量上限校验：同步逐条处理，超限会长时间阻塞请求并打爆下游（S3/RagFlow）
            if (documentAddInList.size() > maxDocCount) {
                throw new BusinessException(ErrCodeEnum.M0005,
                        "单次推送文档数量超过上限(" + maxDocCount + ")，请分批推送");
            }
            // 5、逐条校验操作类型/文件后缀/S3 路径，堵住恶意或脏数据入口
            for (DocumentAddIn documentAddIn : documentAddInList) {
                validateItem(documentAddIn);
            }
            // 6、审计日志：记录来源 IP 与文档数量（docIds 明细由服务层逐条日志与 doc_change_record 落库覆盖，避免长行日志）
            log.info("[文档推送开放接口] 开始处理, ip={}, docCount={}",
                    clientIp, documentAddInList.size());
            // 7、委托异步版文档推送服务：先落库快速返回（校验+S3转存+变更记录+落库），
            //    RagFlow 向量化由后台单线程池逐条补做，避免大批量推送时长时间阻塞 HTTP 连接
            //    （同步逐条向量化曾导致千篇级推送 60s+ 超时断连）；delete 操作仍在本请求内同步完成
            DocumentAddOut result = aidpDocumentService.addDocumentAsync(documentAddInList);
            // 8、审计日志：记录落库受理结果与耗时（successSum/failSum 仅反映落库受理，向量化状态见文档列表 task_status）
            log.info("[文档推送开放接口] 落库受理完成(向量化后台进行中), ip={}, resultcode={}, resultmsg={}, successSum={}, failSum={}, 耗时={}ms",
                    clientIp, result.getResultcode(), result.getResultmsg(),
                    result.getSuccessSum(), result.getFailSum(), System.currentTimeMillis() - startTime);
            return result;
        } catch (BusinessException e) {
            // 审计日志：记录被拒绝/校验失败的调用（鉴权失败、入参非法等）
            log.warn("[文档推送开放接口] 调用被拒绝, ip={}, resultcode={}, resultmsg={}, 耗时={}ms",
                    clientIp, e.getErrCode(), e.getErrMsg(), System.currentTimeMillis() - startTime);
            throw e;
        } catch (Exception e) {
            // 审计日志：记录未预期异常
            log.error("[文档推送开放接口] 调用异常, ip={}, 耗时={}ms", clientIp, System.currentTimeMillis() - startTime, e);
            throw e;
        }
    }

    /**
     * 获取调用方来源 IP（优先取 X-Forwarded-For 首个地址，兼容 Nginx 反向代理场景）
     *
     * @param request 当前请求
     * @return 来源 IP
     */
    private String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (StringUtils.isNotBlank(forwarded)) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    /**
     * HMAC 签名鉴权（按序执行，任一步失败即拒绝，防止攻击者提前消耗资源）
     *
     * @param timestamp 调用方毫秒时间戳
     * @param nonce     一次性随机串
     * @param sign      签名值
     * @param rawBody   原始请求体
     */
    private void verifySignature(String timestamp, String nonce, String sign, String rawBody) {
        // ① 请求头完整性校验
        if (StringUtils.isBlank(timestamp) || StringUtils.isBlank(nonce) || StringUtils.isBlank(sign)) {
            throw new BusinessException(ErrCodeEnum.M9002, "缺少签名请求头 X-Timestamp/X-Nonce/X-Sign");
        }
        // ② 时间戳窗口校验：偏差超过 5 分钟视为过期重放，直接拒绝
        long ts;
        try {
            ts = Long.parseLong(timestamp);
        } catch (NumberFormatException e) {
            throw new BusinessException(ErrCodeEnum.M9002, "X-Timestamp 格式错误");
        }
        if (Math.abs(System.currentTimeMillis() - ts) > SIGN_TIMEOUT_MS) {
            throw new BusinessException(ErrCodeEnum.M9002, "请求已过期");
        }
        // ③ 签名比对：X-Sign = HMACSHA256(timestamp + "|" + nonce + "|" + sha256hex(body), 共享密钥)
        String bodySha256 = HMacUtils.getSHA256StrJava(
                rawBody == null ? new byte[0] : rawBody.getBytes(StandardCharsets.UTF_8));
        String expectedSign;
        try {
            expectedSign = HMacUtils.HMACSHA256(timestamp + "|" + nonce + "|" + bodySha256, openApiKey);
        } catch (Exception e) {
            log.error("HMAC 签名计算失败", e);
            throw new BusinessException(ErrCodeEnum.M9002);
        }
        if (!secureEquals(expectedSign, sign)) {
            throw new BusinessException(ErrCodeEnum.M9002, "签名校验失败");
        }
        // ④ nonce 去重：Redis SETNX 原子写入（同 nonce 仅首次成功），防窗口内重放
        Boolean firstUse = redisUtil.stringTemplate.opsForValue()
                .setIfAbsent(NONCE_KEY_PREFIX + nonce, "1", SIGN_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        if (firstUse == null || !firstUse) {
            throw new BusinessException(ErrCodeEnum.M9002, "重复请求");
        }
    }

    /**
     * 反序列化请求体为文档推送入参列表
     *
     * @param rawBody 原始 JSON 字符串
     * @return 文档推送入参列表
     */
    private List<DocumentAddIn> parseBody(String rawBody) {
        if (StringUtils.isBlank(rawBody)) {
            throw new BusinessException(ErrCodeEnum.M1001);
        }
        try {
            return objectMapper.readValue(rawBody, new TypeReference<List<DocumentAddIn>>() {
            });
        } catch (Exception e) {
            throw new BusinessException(ErrCodeEnum.M0005, "请求体 JSON 格式错误");
        }
    }

    /**
     * 单条入参合法性校验
     *
     * @param documentAddIn 待校验的文档推送入参
     */
    private void validateItem(DocumentAddIn documentAddIn) {
        if (documentAddIn == null) {
            throw new BusinessException(ErrCodeEnum.M0005, "推送文档列表中存在空元素");
        }
        // 操作类型枚举校验
        if (StringUtils.isBlank(documentAddIn.getDocOperatorType())
                || !OPERATOR_TYPES.contains(documentAddIn.getDocOperatorType())) {
            throw new BusinessException(ErrCodeEnum.M0005, "docOperatorType 仅支持 add/update/delete");
        }
        // S3 桶内文件路径非空校验（防止传入任意路径触发越权文件转发）
        if (StringUtils.isBlank(documentAddIn.getAwsFilePath())) {
            throw new BusinessException(ErrCodeEnum.M0005, "awsFilePath 不能为空");
        }
        // 文件后缀白名单校验（与系统支持的文档类型对齐）
        String suffix = getSuffix(documentAddIn.getDocName());
        if (suffix == null || !FileTypeDetector.FILE_TYPE_MAP.containsKey(suffix)) {
            throw new BusinessException(ErrCodeEnum.M0005, "docName 文件类型不支持");
        }
    }

    /**
     * 提取文件名后缀（小写）
     *
     * @param docName 文档名称
     * @return 后缀，无 "." 时返回 null
     */
    private String getSuffix(String docName) {
        if (StringUtils.isBlank(docName) || !docName.contains(".")) {
            return null;
        }
        return docName.substring(docName.lastIndexOf(".") + 1).toLowerCase();
    }

    /**
     * 常量时间字符串比较，避免时序攻击泄露密钥
     *
     * @param expected 期望值
     * @param actual   实际值
     * @return 是否相等
     */
    private boolean secureEquals(String expected, String actual) {
        if (expected == null || actual == null) {
            return false;
        }
        return MessageDigest.isEqual(expected.getBytes(), actual.getBytes());
    }
}
