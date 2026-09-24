package com.zzccaidp.client;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zzccaidp.enums.ErrCodeEnum;
import com.zzccaidp.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import javax.annotation.Resource;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static com.zzccaidp.common.ConfigPropertieCommon.RESULT_CODE;
import static com.zzccaidp.common.ConfigPropertieCommon.RESULT_MSG;
import static com.zzccaidp.enums.ErrCodeEnum.*;

/**
 * @Description:
 * @Author: WB233500
 * @Createtime: 16:08
 * @Version: 1.0
 */
@Service
@Slf4j
public class AgentScopeClientUtil {

    @Resource(name = "ansycWebClient")
    private WebClient webClient;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${agentScope.base.url:http://203.3.54.133:8000/api/v1}")
    private String agentScopeBaseUrl;


    public Flux<String> webClient(String url, Map<String, Object> body) {
        // 是否思考内容的标签
        log.info("流式请求AgentScope,request:{},uri:{}", body, url);
        return webClient.post().uri(agentScopeBaseUrl + url)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(body))
                .accept(MediaType.TEXT_EVENT_STREAM)
                .retrieve()
                .bodyToFlux(String.class)
                .onErrorResume(throwable -> {
                    log.error("流处理异常：", throwable);
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put(RESULT_CODE, ErrCodeEnum.M0010.getErrCode());
                    jsonObject.put(RESULT_MSG, ErrCodeEnum.M0010.getErrMsg());
                    return Flux.just(jsonObject.toJSONString());
                })
                .doOnNext(line -> log.info("接收到流式数据：{}", line))
                .timeout(Duration.ofMillis(60000)).doOnComplete(() -> {
                    log.info("流式处理完成");
                }).doOnError(throwable -> log.error("流处理异常:", throwable));
    }

    public JSONObject getRestTemplateClient(String url, Map<String, String> body) {
        try {
            log.info("get请求AgentScope request:{},uri:{}", body, url);
            JSONObject response = restTemplate.getForObject(agentScopeBaseUrl + url, JSONObject.class, body);
            log.info("get请求AgentScope response:{}", response);
            return setErrorCode(response);
        } catch (Exception e) {
            log.error(ErrCodeEnum.M9998.getErrMsg(), e);
            throw new BusinessException(ErrCodeEnum.M9998);
        }
    }

    public JSONObject postRestTemplateClient(String url, Map<String, String> body) {
        try {
            log.info("post请求AgentScope request:{},uri:{}", body, url);
            JSONObject response = restTemplate.postForObject(agentScopeBaseUrl + url, null, JSONObject.class, body);
            log.info("post请求AgentScope response:{}", response);
            return setErrorCode(response);
        } catch (Exception e) {
            log.error(ErrCodeEnum.M9998.getErrMsg(), e);
            throw new BusinessException(ErrCodeEnum.M9998);
        }
    }

    public JSONObject deleteRestTemplateClient(String url, Map<String, String> body) {
        try {
            log.info("delete请求AgentScope request:{},uri:{}", body, url);
            restTemplate.delete(agentScopeBaseUrl + url, body);
            JSONObject response = new JSONObject();
            response.put(RESULT_MSG, SUCCESS.getErrMsg());
            response.put(RESULT_CODE, SUCCESS.getErrCode());
            return response;
        } catch (Exception e) {
            log.error(ErrCodeEnum.M9998.getErrMsg(), e);
            throw new BusinessException(ErrCodeEnum.M9998);
        }
    }

    public JSONObject setErrorCode(JSONObject jsonObject) {
        if (jsonObject == null) {
            JSONObject error = new JSONObject();
            error.put(RESULT_CODE, M9998.getErrCode());
            error.put(RESULT_MSG, M9998.getErrMsg());
            return error;
        }
        jsonObject.put(RESULT_CODE, jsonObject.getOrDefault("errorCode", M9998.getErrCode()));
        jsonObject.put(RESULT_MSG, jsonObject.getOrDefault("errorMessage", M9998.getErrMsg()));
        return jsonObject;
    }
}
