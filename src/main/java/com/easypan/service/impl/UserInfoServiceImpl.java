package com.easypan.service.impl;

import com.easypan.component.RedisComponent;
import com.easypan.entity.config.AppConfig;
import com.easypan.entity.constants.Constants;
import com.easypan.entity.dto.SessionWebUserDto;
import com.easypan.entity.dto.SysSettingsDto;
import com.easypan.entity.dto.UserSpaceDto;
import com.easypan.entity.enums.UserStatusEnum;
import com.easypan.entity.po.UserInfo;
import com.easypan.entity.query.UserInfoQuery;
import com.easypan.exception.BusinessException;
import com.easypan.mappers.UserInfoMapper;
import com.easypan.service.EmailCodeService;
import com.easypan.service.UserInfoService;
import com.easypan.utils.StringTools;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.ibatis.reflection.ArrayUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sun.text.normalizer.UnicodeSet;

import javax.annotation.Resource;
import javax.security.auth.login.AppConfigurationEntry;
import java.util.Date;

/**
 * @ClassName UserInfoServiceImpl
 * @Author Henry
 * @Date 2023/8/10 17:41
 * @Version 1.0
 */
@Service
public class UserInfoServiceImpl implements UserInfoService {
	
	@Resource
	private UserInfoMapper<UserInfo, UserInfoQuery> userInfoMapper;
	
	@Resource
	private EmailCodeService emailCodeService;
	
	@Resource
	private RedisComponent redisComponent;
	
	@Resource
	private AppConfig appConfig;
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void register(String email, String nickName, String password, String emailCode) {
		UserInfo userInfo = this.userInfoMapper.selectByEmail(email);
		if (userInfo != null) {
			throw new BusinessException("邮箱账号已存在");
		}
		UserInfo nickNameUser = this.userInfoMapper.selectByNickName(nickName);
		if (nickNameUser != null) {
			throw new BusinessException("昵称已存在");
		}
		
		// 校验邮箱验证码
		emailCodeService.checkCode(email, emailCode);
		
		String userId = StringTools.getRandomNumber(Constants.LENGTH_10);
		userInfo = new UserInfo();
		userInfo.setUserId(userId);
		userInfo.setNickName(nickName);
		userInfo.setEmail(email);
		userInfo.setPassword(StringTools.encodeByMd5(password));
		userInfo.setJoinTime(new Date());
		userInfo.setStatus(UserStatusEnum.ENABLE.getStatus());
		userInfo.setUseSpace(0L);
		SysSettingsDto sysSettingsDto = redisComponent.getSysSettings();
		userInfo.setTotalSpace(sysSettingsDto.getUserInitUseSpace() * Constants.MB);
		this.userInfoMapper.insert(userInfo);
	}
	
	/**
	 * 登录
	 *
	 * @param email    电子邮件
	 * @param password 密码
	 * @return {@link SessionWebUserDto}
	 */
	@Override
	public SessionWebUserDto login(String email, String password) {
		UserInfo userInfo = this.userInfoMapper.selectByEmail(email);
		if (userInfo == null || !userInfo.getPassword().equals(password)) {
			throw new BusinessException("账号或密码错误");
		}
		
		if (UserStatusEnum.DISABLE.getStatus().equals(userInfo.getStatus())) {
			throw new BusinessException("账号已禁用");
		}
		UserInfo updateInfo = new UserInfo();
		updateInfo.setLastLoginTime(new Date());
		this.userInfoMapper.updateByUserId(updateInfo, userInfo.getUserId());
		
		SessionWebUserDto sessionWebUserDto = new SessionWebUserDto();
		sessionWebUserDto.setNickName(userInfo.getNickName());
		sessionWebUserDto.setUserId(updateInfo.getUserId());
		if (ArrayUtils.contains(appConfig.getAdminEmails().split(","),email)) {
			sessionWebUserDto.setAdmin(true);
		} else {
			sessionWebUserDto.setAdmin(false);
		}
		// 用户空间
		UserSpaceDto userSpaceDto = new UserSpaceDto();
		// TODO
		// userSpaceDto.setUserSpace();
		userSpaceDto.setTotalSpace(userSpaceDto.getTotalSpace());
		redisComponent.saveUserSpaceUse(userInfo.getUserId(),userSpaceDto);
		return null;
	}
	
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void resetPwd(String email, String emailCode, String password) {
		UserInfo userInfo = this.userInfoMapper.selectByEmail(email);
		if (userInfo == null ) {
			throw new BusinessException("邮箱不存在");
		}
		emailCodeService.checkCode(email,emailCode);
		UserInfo updateInfo = new UserInfo();
		updateInfo.setPassword(StringTools.encodeByMd5(password));
		this.userInfoMapper.updateByEmail(updateInfo,email);
	}
}
