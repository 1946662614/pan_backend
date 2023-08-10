package com.easypan.service;

/**
 * @ClassName EmailCodeService
 * @Description TODO
 * @Author Henry
 * @Date 2023/8/10 16:31
 * @Version 1.0
 */

public interface EmailCodeService {
	
	/**
	 * 发送电子邮件编码
	 *
	 * @param toEmail 电子邮件
	 * @param type    类型
	 */
	void sendEmailCode(String toEmail, Integer type);
	
}
