package com.easypan.controller;

import com.easypan.annotation.GlobalInterceptor;
import com.easypan.annotation.VerifyParam;
import com.easypan.component.RedisComponent;
import com.easypan.entity.dto.SessionWebUserDto;
import com.easypan.entity.dto.SysSettingsDto;
import com.easypan.entity.po.FileInfo;
import com.easypan.entity.po.FileShare;
import com.easypan.entity.query.FileShareQuery;
import com.easypan.entity.query.UserInfoQuery;
import com.easypan.entity.vo.PaginationResultVO;
import com.easypan.entity.vo.ResponseVO;
import com.easypan.entity.vo.UserInfoVO;
import com.easypan.service.FileInfoService;
import com.easypan.service.FileShareService;
import com.easypan.service.UserInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

/**
 * 管理员控制器
 *
 * @ClassName Controller
 * @Description
 * @Author Henry
 * @Date 2023/9/14 10:30
 */
@RestController("adminController")
@RequestMapping("/admin")
@Slf4j
public class AdminController extends ABaseController{
	@Resource
	private FileInfoService fileInfoService;
	@Resource
	private FileShareService fileShareService;
	@Resource
	private UserInfoService userInfoService;
	@Resource
	private RedisComponent redisComponent;
	
	/**
	 * 获取系统设置
	 *
	 * @return {@link ResponseVO}
	 */
	@RequestMapping("/getSysSettings")
	@GlobalInterceptor(checkParams = true, checkAdmin = true)
	public ResponseVO getSysSettings() {
		return getSuccessResponseVO(redisComponent.getSysSettings());
	}
	
	/**
	 * 保存系统设置
	 *
	 * @return {@link ResponseVO}
	 */
	@RequestMapping("/saveSysSettings")
	@GlobalInterceptor(checkParams = true, checkAdmin = true)
	public ResponseVO saveSysSettings(@VerifyParam(required = true) String registerEmailTitle,
									  @VerifyParam(required = true) String registerEmailContent,
									  @VerifyParam(required = true) Integer userInitUseSpace) {
		SysSettingsDto sysSettingsDto = new SysSettingsDto();
		sysSettingsDto.setUserInitUseSpace(userInitUseSpace);
		sysSettingsDto.setRegisterEmailTitle(registerEmailTitle);
		sysSettingsDto.setRegisterEmailContent(registerEmailContent);
		redisComponent.saveSysSettingsDto(sysSettingsDto);
		return getSuccessResponseVO(null);
	}
	
	/**
	 * 加载用户列表
	 *
	 * @param userInfoQuery 用户信息查询
	 * @return {@link ResponseVO}
	 */
	@RequestMapping("/loadUserList")
	@GlobalInterceptor(checkParams = true, checkAdmin = true)
	public ResponseVO loadUserList(UserInfoQuery userInfoQuery) {
		userInfoQuery.setOrderBy("join_time desc");
		PaginationResultVO resultVO =  userInfoService.findListByPage(userInfoQuery);
		return getSuccessResponseVO(convert2PaginationVO(resultVO, UserInfoVO.class));
	}
	
	/**
	 * 更新用户状态
	 *
	 * @param userId 用户id
	 * @param status 地位
	 * @return {@link ResponseVO}
	 */
	@RequestMapping("/updateUserStatus")
	@GlobalInterceptor(checkParams = true, checkAdmin = true)
	public ResponseVO updateUserStatus(@VerifyParam(required = true) String userId,
									   @VerifyParam(required = true) Integer status) {
		userInfoService.updateUserStatus(userId, status);
		return getSuccessResponseVO(null);
	}
	
	@RequestMapping("/updateUserSpace")
	@GlobalInterceptor(checkParams = true, checkAdmin = true)
	public ResponseVO updateUserSpace(@VerifyParam(required = true) String userId,
									   @VerifyParam(required = true) Integer changeSpace) {
		userInfoService.updateUserSpace(userId, changeSpace);
		return getSuccessResponseVO(null);
	}
}
