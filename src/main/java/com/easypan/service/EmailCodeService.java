package com.easypan.service;

/**
 * @ClassName EmailCodeService
 * @Author Henry
 * @Date 2023/8/10 16:31
 * @Version 1.0
 */

public interface EmailCodeService {
	
	/**
	 * 发送电子邮件编码
	 *
	 * @param email 电子邮件
	 * @param type    类型
	 */
	void sendEmailCode(String email, Integer type);
	
	/**
	 * 校验邮箱验证码
	 *
	 * @param email 电子邮件
	 * @param code  代码
	 */
	void checkCode(String email, String code);
}
