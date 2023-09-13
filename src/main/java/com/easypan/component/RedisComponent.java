package com.easypan.component;

import com.easypan.entity.constants.Constants;
import com.easypan.entity.dto.DownloadFileDto;
import com.easypan.entity.dto.SysSettingsDto;
import com.easypan.entity.dto.UserSpaceDto;
import com.easypan.entity.po.FileInfo;
import com.easypan.entity.query.FileInfoQuery;
import com.easypan.mappers.FileInfoMapper;
import org.springframework.boot.autoconfigure.amqp.RabbitTemplateConfigurer;
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
	@Resource
	private FileInfoMapper<FileInfo, FileInfoQuery> fileInfoMapper;
	
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
			Long useSpace = fileInfoMapper.selectUseSpace(userId);
			userSpaceDto.setUseSpace(useSpace);
			userSpaceDto.setTotalSpace(getSysSettings().getUserInitUseSpace() * Constants.MB);
			saveUserSpaceUse(userId, userSpaceDto);
		}
		return userSpaceDto;
	}
	
	public void saveFileTempSize(String userId, String fileId, Long fileSize) {
		Long currentSize = getFileTempSize(userId, fileId);
		redisUtils.setex(Constants.REDIS_KEY_USER_FILE_TEMP_SIZE + userId + fileId, currentSize + fileSize, Long.valueOf(Constants.REDIS_KEY_EXPIRES_ONE_HOUR));
	}
	
	/**
	 * 获取临时文件大小
	 *
	 * @param userId 用户id
	 * @param fileId 文件标识
	 * @return {@link Long}
	 */
	public Long getFileTempSize(String userId, String fileId) {
		Long currentSize = getFileSizeFromRedis(Constants.REDIS_KEY_USER_FILE_TEMP_SIZE + userId + fileId);
		return currentSize;
	}
	
	public Long getFileSizeFromRedis(String key) {
		Object size = redisUtils.get(key);
		if (size == null) {
			return 0L;
		}
		if (size instanceof Integer){
			return ((Integer) size).longValue();
		}else if (size instanceof Long){
			return (Long) size;
		}
		return 0L;
	}
	
	/**
	 * 保存下载代码
	 *
	 * @param code            密码
	 * @param downloadFileDto 下载文件dto
	 */
	public void saveDownloadCode(String code, DownloadFileDto downloadFileDto){
		redisUtils.setex(Constants.REDIS_KEY_DOWNLOAD + code,downloadFileDto, Long.valueOf(Constants.REDIS_KEY_EXPIRES_FIVE_MIN));
	}
	
	/**
	 * 获取下载dto
	 *
	 * @param code 密码
	 * @return {@link DownloadFileDto}
	 */
	public DownloadFileDto getDownloadDto(String code) {
		return (DownloadFileDto) redisUtils.get(Constants.REDIS_KEY_DOWNLOAD + code);
	}
}
