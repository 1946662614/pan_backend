package com.easypan.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @ClassName TestController
 * @Description TODO
 * @Author Henry
 * @Date 2023/8/4 15:37
 * @Version 1.0
 */
@RestController
public class TestController {
	@RequestMapping("/test")
	public String test() {
		return "Hello World";
	}

}
