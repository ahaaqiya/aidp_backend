package com.zzccaidp.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.time.Duration;

/**
 * @Description: \
 * @Author: WB233500
 * @Createtime: 14:27
 * @Version: 1.0
 */
@RestController
@RequestMapping("/flux")
@Slf4j
@Api("springmvc的demo")
public class DemoFluxController {

    @ApiOperation("hello world")
    @GetMapping(value = "/hello", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> insertData(String data) {
        String message = "你好，这是一个来自bades的打字机效果演示！\n你看，文字像打印机一样，正在一个一个字出现";
        //return Flux.fromArray(message.split("")).delayElements(Duration.ofMillis(150));
        Flux<String> flux = Flux.create(sink -> {
            for (int i = 0; i < 20; i++) {
                sink.next(String.valueOf(i));
            }
            sink.complete();
        });
        flux = flux.delayElements(Duration.ofMillis(150));
        return flux;
    }
}
