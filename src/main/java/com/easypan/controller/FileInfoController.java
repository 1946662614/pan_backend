package com.easypan.controller;


import com.easypan.annotation.GlobalInterceptor;
import com.easypan.entity.enums.FileCategoryEnums;
import com.easypan.entity.enums.FileDelFlagEnums;
import com.easypan.entity.query.FileInfoQuery;
import com.easypan.entity.vo.FileInfoVO;
import com.easypan.entity.vo.PaginationResultVO;
import com.easypan.entity.vo.ResponseVO;
import com.easypan.service.FileInfoService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;


/**
 * 文件信息 Controller
 */
@RestController("fileInfoController")
@RequestMapping("/file")
public class FileInfoController  extends ABaseController{
	
	@Resource
	private FileInfoService fileInfoService;
	
	/**
	 * 根据条件分页查询
	 *
	 * @param session  会话
	 * @param query    查询
	 * @param category 类别
	 * @return {@link ResponseVO}
	 */
	@RequestMapping("/loadDataList")
	@GlobalInterceptor
	public ResponseVO loadDataList(HttpSession session, FileInfoQuery query, String category) {
		FileCategoryEnums categoryEnums = FileCategoryEnums.getByCode(category);
		if (categoryEnums != null) {
			query.setFileCategory(categoryEnums.getCategory());
		}
		query.setUserId(getUserInfoSession(session).getUserId());
		query.setOrderBy("last_update_time desc");
		query.setDelFlag(FileDelFlagEnums.USING.getFlag());
		PaginationResultVO result = fileInfoService.findListByPage(query);
		return getSuccessResponseVO(convert2PaginationVO(result, FileInfoVO.class));
	}

}