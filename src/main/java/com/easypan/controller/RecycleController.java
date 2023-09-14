package com.easypan.controller;

import com.easypan.annotation.GlobalInterceptor;
import com.easypan.annotation.VerifyParam;
import com.easypan.entity.dto.SessionWebUserDto;
import com.easypan.entity.enums.FileDelFlagEnums;
import com.easypan.entity.po.FileInfo;
import com.easypan.entity.query.FileInfoQuery;
import com.easypan.entity.vo.PaginationResultVO;
import com.easypan.entity.vo.ResponseVO;
import com.easypan.service.FileInfoService;
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
@RestController("recycleController")
@RequestMapping("/recycle")
@Slf4j
public class RecycleController extends ABaseController{
	@Resource
	private FileInfoService fileInfoService;
	
	/**
	 * 加载回收站列表
	 *
	 * @param session  一场
	 * @param pageNo   第页
	 * @param pageSize 页面大小
	 * @return {@link ResponseVO}
	 */
	@RequestMapping("/loadRecycleList")
	@GlobalInterceptor
	public ResponseVO loadRecycleList(HttpSession session, Integer pageNo, Integer pageSize) {
		FileInfoQuery query = new FileInfoQuery();
		query.setPageNo(pageNo);
		query.setPageSize(pageSize);
		query.setUserId(getUserInfoSession(session).getUserId());
		query.setOrderBy("recovery_time desc");
		query.setDelFlag(FileDelFlagEnums.RECYCLE.getFlag());
		PaginationResultVO<FileInfo> result = fileInfoService.findListByPage(query);
		return getSuccessResponseVO(convert2PaginationVO(result,FileInfo.class));
	}
	
	@RequestMapping("/recoverFile")
	@GlobalInterceptor
	public ResponseVO recoverFile(HttpSession session, @VerifyParam(required = true) String fileIds) {
		SessionWebUserDto webUserDto = getUserInfoSession(session);
		fileInfoService.recoverFileBatch(webUserDto.getUserId(), fileIds);
		return getSuccessResponseVO(null);
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
