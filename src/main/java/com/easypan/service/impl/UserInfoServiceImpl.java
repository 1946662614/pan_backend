package com.easypan.service.impl;

import com.easypan.component.RedisComponent;
import com.easypan.entity.config.AppConfig;
import com.easypan.entity.constants.Constants;
import com.easypan.entity.dto.QQInfoDto;
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
import com.easypan.utils.JsonUtils;
import com.easypan.utils.OKHttpUtils;
import com.easypan.utils.StringTools;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.reflection.ArrayUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sun.text.normalizer.UnicodeSet;

import javax.annotation.Resource;
import javax.security.auth.login.AppConfigurationEntry;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.Map;

/**
 * @ClassName UserInfoServiceImpl
 * @Author Henry
 * @Date 2023/8/10 17:41
 * @Version 1.0
 */
@Service
@Slf4j
public class UserInfoServiceImpl implements UserInfoService {
	
	@Resource
	private UserInfoMapper<UserInfo, UserInfoQuery> userInfoMapper;
	
	@Resource
	private EmailCodeService emailCodeService;
	
	@Resource
	private RedisComponent redisComponent;
	
	@Resource
	private AppConfig appConfig;
	
	/**
	 * 根据UserId修改
	 *
	 * @param bean
	 * @param userId
	 */
	@Override
	public Integer updateUserInfoByUserId(UserInfo bean, String userId) {
		return this.userInfoMapper.updateByUserId(bean, userId);
	}
	
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
		sessionWebUserDto.setUserId(userInfo.getUserId());
		if (ArrayUtils.contains(appConfig.getAdminEmails().split(","),email)) {
			sessionWebUserDto.setAdmin(true);
		} else {
			sessionWebUserDto.setAdmin(false);
		}
		// 用户空间
		UserSpaceDto userSpaceDto = new UserSpaceDto();
		// TODO 查询当前用户已经上传穿文件大小总和
		// userSpaceDto.setUserSpace();
		userSpaceDto.setUseSpace(0L);
		userSpaceDto.setTotalSpace(userInfo.getTotalSpace());
		redisComponent.saveUserSpaceUse(userInfo.getUserId(),userSpaceDto);
		return sessionWebUserDto;
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
	
	@Override
	public SessionWebUserDto qqLogin(String code) {
		String accessToken = getQQAccessToken(code);
		String openId = getQQOpenId(accessToken);
		UserInfo user = this.userInfoMapper.selectByQqOpenId(openId);
		String avatar = null;
		if (null == user) {
			QQInfoDto qqInfo = getQQUserInfo(accessToken, openId);
			user = new UserInfo();
			
			String nickName = qqInfo.getNickname();
			nickName = nickName.length() > Constants.LENGTH_150 ? nickName.substring(0, 150) : nickName;
			avatar = StringTools.isEmpty(qqInfo.getFigureurl_qq_2()) ? qqInfo.getFigureurl_qq_1() : qqInfo.getFigureurl_qq_2();
			Date curDate = new Date();
			
			//上传头像到本地
			user.setQqOpenId(openId);
			user.setJoinTime(curDate);
			user.setNickName(nickName);
			user.setQqAvatar(avatar);
			user.setUserId(StringTools.getRandomString(Constants.LENGTH_10));
			user.setLastLoginTime(curDate);
			user.setStatus(UserStatusEnum.ENABLE.getStatus());
			user.setUseSpace(0L);
			user.setTotalSpace(redisComponent.getSysSettings().getUserInitUseSpace() * Constants.MB);
			this.userInfoMapper.insert(user);
			user = userInfoMapper.selectByQqOpenId(openId);
		} else {
			UserInfo updateInfo = new UserInfo();
			updateInfo.setLastLoginTime(new Date());
			avatar = user.getQqAvatar();
			this.userInfoMapper.updateByQqOpenId(updateInfo, openId);
		}
		if (UserStatusEnum.DISABLE.getStatus().equals(user.getStatus())) {
			throw new BusinessException("账号被禁用无法登录");
		}
		SessionWebUserDto sessionWebUserDto = new SessionWebUserDto();
		sessionWebUserDto.setUserId(user.getUserId());
		sessionWebUserDto.setNickName(user.getNickName());
		sessionWebUserDto.setAvatar(avatar);
		if (ArrayUtils.contains(appConfig.getAdminEmails().split(","), user.getEmail() == null ? "" : user.getEmail())) {
			sessionWebUserDto.setAdmin(true);
		} else {
			sessionWebUserDto.setAdmin(false);
		}
		
		UserSpaceDto userSpaceDto = new UserSpaceDto();
		// userSpaceDto.setUseSpace(fileInfoService.getUserUseSpace(user.getUserId()));
		userSpaceDto.setTotalSpace(user.getTotalSpace());
		redisComponent.saveUserSpaceUse(user.getUserId(), userSpaceDto);
		return sessionWebUserDto;
	}
	
	private String getQQAccessToken(String code) {
		/**
		 * 返回结果是字符串 access_token=*&expires_in=7776000&refresh_token=* 返回错误 callback({UcWebConstants.VIEW_OBJ_RESULT_KEY:111,error_description:"error msg"})
		 */
		String accessToken = null;
		String url = null;
		try {
			url = String.format(appConfig.getQqUrlAccessToken(), appConfig.getQqAppId(), appConfig.getQqAppKey(), code, URLEncoder.encode(appConfig
																																				  .getQqUrlRedirect(), "utf-8"));
		} catch (UnsupportedEncodingException e) {
			log.error("encode失败");
		}
		String tokenResult = OKHttpUtils.getRequest(url);
		if (tokenResult == null || tokenResult.indexOf(Constants.VIEW_OBJ_RESULT_KEY) != -1) {
			log.error("获取qqToken失败:{}", tokenResult);
			throw new BusinessException("获取qqToken失败");
		}
		String[] params = tokenResult.split("&");
		if (params != null && params.length > 0) {
			for (String p : params) {
				if (p.indexOf("access_token") != -1) {
					accessToken = p.split("=")[1];
					break;
				}
			}
		}
		return accessToken;
	}
	
	
	private String getQQOpenId(String accessToken) throws BusinessException {
		// 获取openId
		String url = String.format(appConfig.getQqUrlOpenId(), accessToken);
		String openIDResult = OKHttpUtils.getRequest(url);
		String tmpJson = this.getQQResp(openIDResult);
		if (tmpJson == null) {
			log.error("调qq接口获取openID失败:tmpJson{}", tmpJson);
			throw new BusinessException("调qq接口获取openID失败");
		}
		Map jsonData = JsonUtils.convertJson2Obj(tmpJson, Map.class);
		if (jsonData == null || jsonData.containsKey(Constants.VIEW_OBJ_RESULT_KEY)) {
			log.error("调qq接口获取openID失败:{}", jsonData);
			throw new BusinessException("调qq接口获取openID失败");
		}
		return String.valueOf(jsonData.get("openid"));
	}
	
	
	private QQInfoDto getQQUserInfo(String accessToken, String qqOpenId) throws BusinessException {
		String url = String.format(appConfig.getQqUrlUserInfo(), accessToken, appConfig.getQqAppId(), qqOpenId);
		String response = OKHttpUtils.getRequest(url);
		if (StringUtils.isNotBlank(response)) {
			QQInfoDto qqInfo = JsonUtils.convertJson2Obj(response, QQInfoDto.class);
			if (qqInfo.getRet() != 0) {
				log.error("qqInfo:{}", response);
				throw new BusinessException("调qq接口获取用户信息异常");
			}
			return qqInfo;
		}
		throw new BusinessException("调qq接口获取用户信息异常");
	}
	
	private String getQQResp(String result) {
		if (StringUtils.isNotBlank(result)) {
			int pos = result.indexOf("callback");
			if (pos != -1) {
				int start = result.indexOf("(");
				int end = result.lastIndexOf(")");
				String jsonStr = result.substring(start + 1, end - 1);
				return jsonStr;
			}
		}
		return null;
	}
}
