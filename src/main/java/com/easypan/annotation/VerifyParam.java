package com.easypan.annotation;

import com.easypan.entity.enums.VerifyRegexEnum;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER, ElementType.FIELD})
public @interface VerifyParam {
	
	/**
	 * 最小值
	 *
	 * @return int
	 */
	int min() default -1;
	
	/**
	 * 最大值
	 *
	 * @return int
	 */
	int max() default -1;
	
	/**
	 * 是否必传
	 *
	 * @return boolean
	 */
	boolean required() default  false;
	
	/**
	 * 正则校验
	 *
	 * @return {@link VerifyRegexEnum}
	 */
	VerifyRegexEnum regex() default VerifyRegexEnum.NO;

}