package com.easypan.service;

import com.easypan.entity.dto.SessionWebUserDto;
import org.springframework.stereotype.Service;

/**
 * @ClassName UserInfoService
 * @Author Henry
 * @Date 2023/8/10 17:41
 * @Version 1.0
 */
@Service("userInfoService")
public interface UserInfoService {
	
	void register(String email, String nickName, String password, String emailCode);
	
	SessionWebUserDto login(String email, String password);
	
	void resetPwd(String email, String emailCode, String password);
}
