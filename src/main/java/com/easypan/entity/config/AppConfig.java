package com.easypan.entity.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/**
 * @ClassName AppConfig
 * @Description TODO
 * @Author Henry
 * @Date 2023/8/11 14:43
 * @Version 1.0
 */
@Data
@Component("appConfig")
public class AppConfig {
	@Value("${spring.mail.username:}")
	private String sendUserName;
	
	@Value("${admin.emails:}")
	private String adminEmails;
	
	@Value("${dev:false}")
	private Boolean dev;
	
	@Value("${project.folder}")
	private String projectFolder;
	
	@Value("${qq.app.id:}")
	private String qqAppId;
	
	@Value("${qq.app.key:}")
	private String qqAppKey;
	
	
	@Value("${qq.url.authorization:}")
	private String qqUrlAuthorization;
	
	
	@Value("${qq.url.access.token:}")
	private String qqUrlAccessToken;
	
	
	@Value("${qq.url.openid:}")
	private String qqUrlOpenId;
	
	@Value("${qq.url.user.info:}")
	private String qqUrlUserInfo;
	
	@Value("${qq.url.redirect:}")
	private String qqUrlRedirect;
	
}
