package com.zzccaidp.client;

import com.alibaba.fastjson.JSONObject;
import com.zzccaidp.enums.ErrCodeEnum;
import com.zzccaidp.exception.BusinessException;
import com.zzccaidp.vo.ai.ChatMessagesIn;
import com.zzccaidp.vo.ai.DelConversationsIn;
import com.zzccaidp.vo.ai.HistoryMessagesIn;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import javax.annotation.Resource;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static com.zzccaidp.common.ConfigPropertieCommon.*;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

/**
 * @Description:
 * @Author: WB233500
 * @Createtime: 14:20
 * @Version: 1.0
 */
@Slf4j
@Service
public class DifyClientUtil {

    @Autowired
    private RestTemplate restTemplate;

    @Resource(name = "ansycWebClient")
    private WebClient webClient;

    @Value("${dify.base.url:}")
    private String difyBaseUrl;

    private static final String BEARER = "Bearer ";

    public Flux<String> webClient(String url, String appKey, ChatMessagesIn chatMessages) {
        // 是否思考内容的标签
        AtomicReference<Boolean> flag = new AtomicReference<>(false);
        log.info("流式请求Dify,url:{},appKey:{},message:{}", url, appKey, chatMessages);
        return webClient.post().uri(difyBaseUrl + url).header(AUTHORIZATION, BEARER + appKey).contentType(MediaType.APPLICATION_JSON).body(BodyInserters.fromValue(chatMessages)).accept(MediaType.TEXT_EVENT_STREAM).retrieve().bodyToFlux(String.class).onErrorResume(throwable -> {
                    log.error("流处理异常：", throwable);
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put(RESULT_CODE, ErrCodeEnum.M0010.getErrCode());
                    jsonObject.put(RESULT_MSG, ErrCodeEnum.M0010.getErrMsg());
                    return Flux.just(jsonObject.toJSONString());
                }).map(line -> {
                    if (line.contains("AI0010")) {
                        return line;
                    }
                    JSONObject jsonObject = JSONObject.parseObject(line);
                    if (jsonObject.containsKey("answer")) {
                        if (jsonObject.getString("answer").contains("<think>")) {
                            flag.set(true);
                        } else if (jsonObject.getString("answer").contains("</think>")) {
                            flag.set(false);
                        }
                        if (flag.get()) {
                            jsonObject.put("think", "1");
                        } else {
                            jsonObject.put("think", "0");
                        }
                    }
                    return jsonObject.toJSONString();
                }).doOnNext(line -> log.info("接收到流式数据：{}", line))
                .timeout(Duration.ofMillis(60000)).doOnComplete(() -> {
                    log.info("流式处理完成");
                }).doOnError(throwable -> log.error("流处理异常:", throwable));
    }

    public JSONObject getRestTemplateClient(String url, String appKey, JSONObject body) {
        JSONObject jsonObject = new JSONObject();
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set(AUTHORIZATION, BEARER + appKey);
            HttpEntity<JSONObject> requestEntity = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    difyBaseUrl + url, HttpMethod.GET, requestEntity, String.class);
            log.info("diyf response:{}", response);
            if (HttpStatus.OK.equals(response.getStatusCode()) && response.getBody() != null) {
                jsonObject.put(RESULT_CODE, ErrCodeEnum.SUCCESS.getErrCode());
                jsonObject.put(RESULT_MSG, ErrCodeEnum.SUCCESS.getErrMsg());
                String responseBody = response.getBody();
                jsonObject.put("body", JSONObject.parseObject(responseBody));
                return jsonObject;
            } else {
                log.error("dify error response: {}", response.getStatusCode());
                throw new BusinessException(ErrCodeEnum.M9998);
            }
        } catch (Exception e) {
            log.error(ErrCodeEnum.M9998.getErrMsg(), e);
            throw new BusinessException(ErrCodeEnum.M9998);
        }
    }

    public JSONObject delRestTemplateClient(String url, String appKey, JSONObject body) {
        JSONObject jsonObject = new JSONObject();
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set(AUTHORIZATION, BEARER + appKey);
            HttpEntity<JSONObject> requestEntity = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    difyBaseUrl + url, HttpMethod.DELETE, requestEntity, String.class);
            log.info("diyf response:{}", response);
            if (HttpStatus.NO_CONTENT.equals(response.getStatusCode())) {
                jsonObject.put(RESULT_CODE, ErrCodeEnum.SUCCESS.getErrCode());
                jsonObject.put(RESULT_MSG, ErrCodeEnum.SUCCESS.getErrMsg());
                String responseBody = response.getBody();
                jsonObject.put("body", JSONObject.parseObject(responseBody));
                return jsonObject;
            } else {
                log.error("dify error response: {}", response.getStatusCode());
                throw new BusinessException(ErrCodeEnum.M9998);
            }
        } catch (Exception e) {
            log.error(ErrCodeEnum.M9998.getErrMsg(), e);
            throw new BusinessException(ErrCodeEnum.M9998);
        }
    }

    public JSONObject postRestTemplateClient(String url, String appKey, JSONObject body) {
        JSONObject jsonObject = new JSONObject();
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set(AUTHORIZATION, BEARER + appKey);
            HttpEntity<JSONObject> requestEntity = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    difyBaseUrl + url, HttpMethod.POST, requestEntity, String.class);
            log.info("diyf response:{}", response);
            if (HttpStatus.OK.equals(response.getStatusCode()) && response.getBody() != null) {
                jsonObject.put(RESULT_CODE, ErrCodeEnum.SUCCESS.getErrCode());
                jsonObject.put(RESULT_MSG, ErrCodeEnum.SUCCESS.getErrMsg());
                String responseBody = response.getBody();
                jsonObject.put("body", JSONObject.parseObject(responseBody));
                return jsonObject;
            } else {
                log.error("dify error response: {}", response.getStatusCode());
                throw new BusinessException(ErrCodeEnum.M9998);
            }
        } catch (Exception e) {
            log.error(ErrCodeEnum.M9998.getErrMsg(), e);
            throw new BusinessException(ErrCodeEnum.M9998);
        }
    }
}
