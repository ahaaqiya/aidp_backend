package com.zzccaidp.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author bades
 */
@RestController
@RequestMapping("/test")
@Slf4j
@Api("springmvc的demo")
public class DemoController {

	@ApiOperation("hello world")
	@PostMapping(value = "/hello")
	public String insertData(String data) {
		return "Hello World!";
	}
}
