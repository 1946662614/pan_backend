package com.easypan.service;

import com.easypan.entity.dto.SessionWebUserDto;
import com.easypan.entity.po.UserInfo;
import com.easypan.entity.query.UserInfoQuery;
import com.easypan.entity.vo.PaginationResultVO;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @ClassName UserInfoService
 * @Author Henry
 * @Date 2023/8/10 17:41
 * @Version 1.0
 */
@Service("userInfoService")
public interface UserInfoService {
	
	/**
	 * 根据条件查询列表
	 */
	List<UserInfo> findListByParam(UserInfoQuery param);
	
	/**
	 * 根据条件查询列表
	 */
	Integer findCountByParam(UserInfoQuery param);
	
	/**
	 * 分页查询
	 */
	PaginationResultVO<UserInfo> findListByPage(UserInfoQuery param);
	
	/**
	 * 根据UserId修改
	 */
	Integer updateUserInfoByUserId(UserInfo bean, String userId);
	
	void register(String email, String nickName, String password, String emailCode);
	
	SessionWebUserDto login(String email, String password);
	
	void resetPwd(String email, String emailCode, String password);
	
	SessionWebUserDto qqLogin(String code);
	
	/**
	 * 更新用户状态
	 *
	 * @param userId 用户id
	 * @param status 地位
	 */
	void updateUserStatus(String userId, Integer status);
	
	void updateUserSpace(String userId, Integer changeSpace);
}
