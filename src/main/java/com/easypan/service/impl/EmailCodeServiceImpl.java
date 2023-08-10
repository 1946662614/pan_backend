package com.easypan.service.impl;

import com.easypan.entity.constants.Constants;
import com.easypan.entity.po.EmailCode;
import com.easypan.entity.po.UserInfo;
import com.easypan.entity.query.EmailCodeQuery;
import com.easypan.entity.query.UserInfoQuery;
import com.easypan.exception.BusinessException;
import com.easypan.mappers.EmailCodeMapper;
import com.easypan.mappers.UserInfoMapper;
import com.easypan.service.EmailCodeService;
import com.easypan.utils.StringTools;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;

/**
 * @ClassName EmailCodeServiceImpl
 * @Description TODO
 * @Author Henry
 * @Date 2023/8/10 16:32
 * @Version 1.0
 */

public class EmailCodeServiceImpl implements EmailCodeService {
	
	@Resource
	private UserInfoMapper<UserInfo, UserInfoQuery> userInfoMapper;
	@Resource
	private EmailCodeMapper<EmailCode, EmailCodeQuery> emailcodeMapper;
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
		// TODO: send email code
		
		// 将之前的验证码都置为无效
		emailcodeMapper.disableEmailCode(email);
		// 插入新数据
		EmailCode emailCode = new EmailCode();
		emailCode.setCode(code);
		emailCode.setEmail(email);
		emailCode.setStatus(Constants.ZERO);
		emailCode.setCreate_time(new Date());
		emailcodeMapper.insert(emailCode);
	}
}
