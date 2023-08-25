package com.easypan.annotation;

import org.springframework.web.bind.annotation.Mapping;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Mapping
public @interface GlobalInterceptor {
	
	/**
	 * 是否要校验参数
	 *
	 * @return boolean
	 */
	boolean checkParams() default false;
	
	/**
	 * 是否需要登录
	 * @return
	 */
	boolean checkLogin() default false;
	
	/**
	 * 校验超级管理员
	 *
	 * @return boolean
	 */
	boolean checkAdmin() default false;
}