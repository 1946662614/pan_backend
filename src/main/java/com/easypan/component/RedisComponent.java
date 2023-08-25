package com.easypan.component;

import com.easypan.entity.constants.Constants;
import com.easypan.entity.dto.SysSettingsDto;
import com.easypan.entity.dto.UserSpaceDto;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @ClassName RedisComponent
 * @Description
 * @Author Henry
 * @Date 2023/8/11 15:04
 * @Version 1.0
 */
@Component("redisComponent")
public class RedisComponent {
	
	@Resource
	 private RedisUtils redisUtils;
	
	public SysSettingsDto getSysSettings() {
		SysSettingsDto sysSettingsDto = (SysSettingsDto) redisUtils.get(Constants.REDIS_KEY_SYS_SETTING);
		if (sysSettingsDto == null) {
			sysSettingsDto = new SysSettingsDto();
			redisUtils.set(Constants.REDIS_KEY_SYS_SETTING, sysSettingsDto);
		}
		return sysSettingsDto;
	}
	
	/**
	 * 保存用户使用空间
	 *
	 * @param userId       用户id
	 * @param userSpaceDto 用户空间dto
	 */
	public void saveUserSpaceUse(String userId, UserSpaceDto userSpaceDto) {
		redisUtils.setex(Constants.REDIS_KEY_USER_SPACE_USE + userId, userSpaceDto, Constants.REDIS_KEY_EXPIRES_DAY);
	}
	
	/**
	 * 获取用户使用空间
	 *
	 * @param userId 用户id
	 * @return {@link UserSpaceDto}
	 */
	public UserSpaceDto getUserSpaceUse(String userId) {
		UserSpaceDto userSpaceDto = (UserSpaceDto) redisUtils.get(Constants.REDIS_KEY_USER_SPACE_USE + userId);
		if (userSpaceDto == null) {
			userSpaceDto = new UserSpaceDto();
			// todo 查询当前用户已经上传文件大小总和
			userSpaceDto.setUseSpace(0L);
			userSpaceDto.setTotalSpace(getSysSettings().getUserInitUseSpace() * Constants.MB);
			saveUserSpaceUse(userId, userSpaceDto);
		}
		return userSpaceDto;
	}
}
