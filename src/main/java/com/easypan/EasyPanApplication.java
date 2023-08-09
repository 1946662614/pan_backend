package com.easypan;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @ClassName EasyPanApplication
 * @Description 启动类
 * @Author Henry
 * @Date 2023/8/4 15:11
 * @Version 1.0
 */


@SpringBootApplication(scanBasePackages = {"com.easypan"})
@EnableAsync // 异步调用
@EnableTransactionManagement // 事务
@EnableScheduling // 开启定时任务
public class EasyPanApplication {
	public static void main(String[] args) {
		SpringApplication.run(EasyPanApplication.class, args);
	}
}
