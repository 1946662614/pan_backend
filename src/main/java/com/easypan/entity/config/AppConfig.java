package com.easypan.entity.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
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
	
}
