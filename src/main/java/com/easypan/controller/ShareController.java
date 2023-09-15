package com.easypan.controller;

import com.easypan.annotation.GlobalInterceptor;
import com.easypan.annotation.VerifyParam;
import com.easypan.entity.dto.SessionWebUserDto;
import com.easypan.entity.enums.FileDelFlagEnums;
import com.easypan.entity.po.FileInfo;
import com.easypan.entity.po.FileShare;
import com.easypan.entity.query.FileInfoQuery;
import com.easypan.entity.query.FileShareQuery;
import com.easypan.entity.vo.PaginationResultVO;
import com.easypan.entity.vo.ResponseVO;
import com.easypan.service.FileInfoService;
import com.easypan.service.FileShareService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

/**
 * @ClassName RecycleController
 * @Description
 * @Author Henry
 * @Date 2023/9/14 10:30
 */
@RestController("shareController")
@RequestMapping("/share")
@Slf4j
public class ShareController extends ABaseController{
	@Resource
	private FileShareService fileShareService;
	
	/**
	 * 加载分享列表
	 * @param session
	 * @param query
	 * @return
	 */
	@RequestMapping("/loadShareList")
	@GlobalInterceptor
	public ResponseVO loadShareList(HttpSession session, FileShareQuery query) {
		query.setOrderBy("share_time desc");
		SessionWebUserDto webUserDto = getUserInfoSession(session);
		query.setUserId(webUserDto.getUserId());
		query.setQueryFileName(true);
		PaginationResultVO result = fileShareService.findListByPage(query);
		return getSuccessResponseVO(result);
	}
	
	/**
	 * 共享文件
	 *
	 * @param session   一场
	 * @param fileId    文件id
	 * @param validType 有效类型
	 * @param code      密码
	 * @return {@link ResponseVO}
	 */
	@RequestMapping("/shareFile")
	@GlobalInterceptor(checkParams = true)
	public ResponseVO shareFile(HttpSession session,
								@VerifyParam(required = true) String fileId,
								@VerifyParam(required = true) Integer validType,
								String code) {
		SessionWebUserDto webUserDto = getUserInfoSession(session);
		FileShare share = new FileShare();
		share.setFileId(fileId);
		share.setValidType(validType);
		share.setCode(code);
		share.setUserId(webUserDto.getUserId());
		fileShareService.saveShare(share);
		return getSuccessResponseVO(share);
	}
	
	/**
	 * 取消分享
	 *
	 * @param session  一场
	 * @param shareIds 共享ID
	 * @return {@link ResponseVO}
	 */
	@RequestMapping("/cancelShare")
	@GlobalInterceptor(checkParams = true)
	public ResponseVO cancelShare(HttpSession session,
								@VerifyParam(required = true) String shareIds) {
		SessionWebUserDto webUserDto = getUserInfoSession(session);;
		fileShareService.deleteFileShareBatch(shareIds.split(","), webUserDto.getUserId());
		return getSuccessResponseVO(null);
	}
	
}
