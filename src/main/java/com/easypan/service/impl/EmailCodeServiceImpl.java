package com.easypan.service.impl;

import com.easypan.component.RedisComponent;
import com.easypan.entity.config.AppConfig;
import com.easypan.entity.constants.Constants;
import com.easypan.entity.dto.SysSettingsDto;
import com.easypan.entity.po.EmailCode;
import com.easypan.entity.po.UserInfo;
import com.easypan.entity.query.EmailCodeQuery;
import com.easypan.entity.query.UserInfoQuery;
import com.easypan.exception.BusinessException;
import com.easypan.mappers.EmailCodeMapper;
import com.easypan.mappers.UserInfoMapper;
import com.easypan.service.EmailCodeService;
import com.easypan.utils.StringTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.Date;

/**
 * @ClassName EmailCodeServiceImpl
 * @Description
 * @Author Henry
 * @Date 2023/8/10 16:32
 * @Version 1.0
 */
@Service
public class EmailCodeServiceImpl implements EmailCodeService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(EmailCodeServiceImpl.class);
	
	@Resource
	private UserInfoMapper<UserInfo, UserInfoQuery> userInfoMapper;
	@Resource
	private EmailCodeMapper<EmailCode, EmailCodeQuery> emailCodeMapper;
	
	@Resource
	private JavaMailSender javaMailSender;
	
	@Resource
	private AppConfig  appConfig;
	
	@Resource
	private RedisComponent redisComponent;
	
	/**
	 * 发送电子邮件编码
	 *
	 * @param email 电子邮件
	 * @param type    类型
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void sendEmailCode(String email, Integer type) {
		if (type == Constants.ZERO) {
			UserInfo userInfo = userInfoMapper.selectByEmail(email);
			if (userInfo != null) {
				throw new BusinessException("邮箱已存在!");
			}
		}
		String code = StringTools.getRandomNumber(Constants.LENGTH_5);
		//  send email code
		sendEmailCode(email, code);
		// 将之前的验证码都置为无效
		emailCodeMapper.disableEmailCode(email);
		// 插入新数据
		EmailCode emailCode = new EmailCode();
		emailCode.setCode(code);
		emailCode.setEmail(email);
		emailCode.setStatus(Constants.ZERO);
		emailCode.setCreate_time(new Date());
		emailCodeMapper.insert(emailCode);
	}
	
	/**
	 * 校验邮箱验证码
	 *
	 * @param email 电子邮件
	 * @param code  代码
	 */
	@Override
	public void checkCode(String email, String code) {
		EmailCode emailCode = this.emailCodeMapper.selectByEmailAndCode(email, code);
		if (emailCode == null) {
			throw new BusinessException("邮箱验证码不正确");
		}
		if (emailCode.getStatus() == 1
					|| System.currentTimeMillis() - emailCode.getCreate_time().getTime() > Constants.LENGTH_15 * 1000 * 60) {
			throw new BusinessException("邮箱验证码已失效");
		}
		emailCodeMapper.disableEmailCode(email);
	}
	
	private void sendEmailCode(String email, String code) {
		try {
			MimeMessage message = javaMailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, true);
			helper.setFrom(appConfig.getSendUserName());
			helper.setTo(email);
			SysSettingsDto sysSettingsDto = redisComponent.getSysSettings();
			helper.setSubject(sysSettingsDto.getRegisterMailTitle());
			helper.setText(String.format(sysSettingsDto.getRegisterMailContent(),code));
			helper.setSentDate(new Date());
			javaMailSender.send(message);
		} catch (Exception e) {
			LOGGER.error("邮件发送失败", e);
			throw new BusinessException("邮件发送失败");
		}
	}
}
