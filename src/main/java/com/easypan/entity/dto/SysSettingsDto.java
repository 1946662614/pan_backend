package com.easypan.entity.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.io.Serializable;

/**
 * @ClassName SysSettingsDto
 * @Author Henry
 * @Date 2023/8/11 15:06
 * @Version 1.0
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SysSettingsDto implements Serializable {
	private String registerMailTitle = "邮箱验证码";
	
	private String registerMailContent = "您好，您的邮箱验证码是: %s ,15分钟有效";
	
	// 用户初始化空间大小
	private Integer userInitUseSpace = 5;
	
}
