package com.easypan.controller;

import com.easypan.annotation.GlobalInterceptor;
import com.easypan.annotation.VerifyParam;
import com.easypan.entity.constants.Constants;
import com.easypan.entity.dto.SessionShareDto;
import com.easypan.entity.dto.SessionWebUserDto;
import com.easypan.entity.enums.FileDelFlagEnums;
import com.easypan.entity.enums.ResponseCodeEnum;
import com.easypan.entity.po.FileInfo;
import com.easypan.entity.po.FileShare;
import com.easypan.entity.po.UserInfo;
import com.easypan.entity.query.FileInfoQuery;
import com.easypan.entity.vo.FileInfoVO;
import com.easypan.entity.vo.PaginationResultVO;
import com.easypan.entity.vo.ResponseVO;
import com.easypan.entity.vo.ShareInfoVO;
import com.easypan.exception.BusinessException;
import com.easypan.service.FileInfoService;
import com.easypan.service.FileShareService;
import com.easypan.service.UserInfoService;
import com.easypan.utils.CopyTools;
import java.util.Date;

import com.easypan.utils.StringTools;
import org.springframework.http.ReactiveHttpInputMessage;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * @ClassName WebShareController
 * @Description
 * @Author Henry
 * @Date 2023/9/16 15:07
 */
@RestController("webShareController")
@RequestMapping("showShare")
public class WebShareController extends  CommonFileController{
	
	@Resource
	private FileInfoService fileInfoService;
	
	@Resource
	FileShareService fileShareService;
	
	@Resource
	UserInfoService userInfoService;
	
	/**
	 * 获取共享登录信息
	 *
	 * @param session 一场
	 * @param shareId 共享id
	 * @return {@link ResponseVO}
	 */
	@RequestMapping("/getShareLoginInfo")
	@GlobalInterceptor(checkLogin = false, checkParams = true)
	public ResponseVO getShareLoginInfo(HttpSession session,
								   @VerifyParam(required = true) String shareId) {
		SessionShareDto sessionShareDto = getSessionShareFormSession(session, shareId);
		if (sessionShareDto == null) {
			return getSuccessResponseVO(null);
		}
		ShareInfoVO shareInfoVO = getShareInfoCommon(shareId);
		// 判断是否是当前用户分享的文件
		SessionWebUserDto userDto = getUserInfoSession(session);
		if (userDto != null && userDto.getUserId().equals(sessionShareDto.getShareUserId())) {
			shareInfoVO.setCurrentUser(true);
		} else {
			shareInfoVO.setCurrentUser(false);
		}
		return getSuccessResponseVO(getShareInfoCommon(shareId));
	}
	
	
	/**
	 * 获取共享信息
	 *
	 * @param shareId 共享id
	 * @return {@link ResponseVO}
	 */
	@RequestMapping("/getShareInfo")
	@GlobalInterceptor(checkLogin = false, checkParams = true)
	public ResponseVO getShareInfo(@VerifyParam(required = true) String shareId) {
		
		return getSuccessResponseVO(getShareInfoCommon(shareId));
	}
	
	@RequestMapping("/checkShareCode")
	@GlobalInterceptor(checkLogin = false, checkParams = true)
	public ResponseVO checkShareCode(HttpSession session,
									 @VerifyParam(required = true) String shareId,
									 @VerifyParam(required = true) String code) {
		SessionShareDto sessionShareDto = fileShareService.checkShareCode(shareId, code);
		session.setAttribute(Constants.SESSION_SHARE_KEY + shareId, sessionShareDto);
		return getSuccessResponseVO(null);
	}
	
	
	/**
	 * 加载文件列表
	 *
	 * @param session 一场
	 * @param shareId 共享id
	 * @param filePid 文件pid
	 * @return {@link ResponseVO}
	 */
	@RequestMapping("/loadFileList")
	@GlobalInterceptor(checkLogin = false, checkParams = true)
	public ResponseVO loadFileList(HttpSession session,
								   @VerifyParam(required = true) String shareId, String filePid) {
		SessionShareDto shareSessionDto = checkShare(session, shareId);
		FileInfoQuery query = new FileInfoQuery();
		if (!StringTools.isEmpty(filePid) && !Constants.ZERO_STR.equals(filePid)) {
			fileInfoService.checkRootFilePid(filePid, shareSessionDto.getShareUserId(), shareSessionDto.getFileId());
			query.setFilePid(filePid);
		} else {
			query.setFileId(shareSessionDto.getFileId());
		}
		query.setUserId(shareSessionDto.getShareUserId());
		query.setOrderBy("last_update_time desc");
		query.setDelFlag(FileDelFlagEnums.USING.getFlag());
		PaginationResultVO resultVO = fileInfoService.findListByPage(query);
		return getSuccessResponseVO(convert2PaginationVO(resultVO, FileInfoVO.class));
	}
	
	/**
	 *
	 *
	 * @param session 一场
	 * @param shareId 共享id
	 * @return {@link SessionShareDto}
	 */
	private SessionShareDto checkShare(HttpSession session, String shareId) {
		SessionShareDto sessionShareDto = getSessionShareFormSession(session, shareId);
		if (sessionShareDto == null) {
			throw new BusinessException(ResponseCodeEnum.CODE_903);
		}
		if (sessionShareDto.getExpireTime() != null && new Date().after(sessionShareDto.getExpireTime())) {
			throw new BusinessException(ResponseCodeEnum.CODE_902);
		}
		return sessionShareDto;
	}
	
	/**
	 * 获取共享信息
	 *
	 * @param shareId 共享id
	 * @return {@link ShareInfoVO}
	 */
	private ShareInfoVO getShareInfoCommon(String shareId) {
		FileShare share = fileShareService.getFileShareByShareId(shareId);
		if (null == share || (share.getExpireTime() != null && new Date().after(share.getExpireTime()))) {
			throw new BusinessException(ResponseCodeEnum.CODE_902.getMsg());
		}
		ShareInfoVO shareInfoVO = CopyTools.copy(share, ShareInfoVO.class);
		FileInfo fileInfo = fileInfoService.getFileInfoByFileIdAndUserId(share.getFileId(), share.getUserId());
		if (fileInfo == null || !FileDelFlagEnums.USING.getFlag().equals(fileInfo.getDelFlag())) {
			throw new BusinessException(ResponseCodeEnum.CODE_902.getMsg());
		}
		shareInfoVO.setFileName(fileInfo.getFileName());
		UserInfo userInfo = userInfoService.getUserInfoByUserId(share.getUserId());
		shareInfoVO.setNickName(userInfo.getNickName());
		shareInfoVO.setAvatar(userInfo.getQqAvatar());
		shareInfoVO.setUserId(userInfo.getUserId());
		return shareInfoVO;
	}
	
	/**
	 * 获取文件夹信息
	 *
	 * @param session 一场
	 * @param path    路径
	 * @return {@link ResponseVO}
	 */
	@RequestMapping("/getFolderInfo")
	@GlobalInterceptor(checkParams = true)
	public ResponseVO getFolderInfo( HttpSession session,
									 @VerifyParam(required = true) String path,
									 @VerifyParam(required = true) String shareId) {
		SessionShareDto shareDto = checkShare(session,shareId);
		return super.getFolderInfo(path, shareDto.getShareUserId());
	}
	
	/**
	 * 获取文件
	 *
	 * @param response 回答
	 * @param session  一场
	 * @param shareId  共享id
	 * @param fileId   文件id
	 */
	@RequestMapping("/getFile/{shareId}/{fileId}")
	@GlobalInterceptor(checkParams = true, checkAdmin = true)
	public void getFile(HttpServletResponse response,
						HttpSession session,
						@PathVariable("shareId") @VerifyParam(required = true) String shareId,
						@PathVariable("fileId") @VerifyParam(required = true) String fileId) {
		SessionShareDto shareDto = checkShare(session,shareId);
		super.getFile(response, fileId, shareDto.getShareUserId());
	}
	
	
	/**
	 * 获取视频信息
	 *
	 * @param response 回答
	 * @param session  一场
	 * @param shareId  共享id
	 * @param fileId   文件id
	 */
	@RequestMapping("/ts/getVideoInfo/{shareId}/{fileId}")
	@GlobalInterceptor(checkParams = true, checkAdmin = true)
	public void getVideoInfo(HttpServletResponse response,
							 HttpSession session,
							 @PathVariable("shareId") @VerifyParam(required = true) String shareId,
							 @PathVariable("fileId") @VerifyParam(required = true) String fileId) {
		SessionShareDto shareDto = checkShare(session,shareId);
		super.getFile(response, fileId, shareDto.getShareUserId());
	}
	
	@RequestMapping("/createDownloadUrl/{shareId}/{fileId}")
	@GlobalInterceptor(checkLogin = false, checkParams = true)
	public ResponseVO createDownloadUrl(HttpSession session,
										@PathVariable("shareId") @VerifyParam(required = true) String shareId,
										@PathVariable("fileId") @VerifyParam(required = true) String fileId) {
		SessionShareDto shareSessionDto = checkShare(session, shareId);
		return super.createDownloadUrl(fileId, shareSessionDto.getShareUserId());
	}
	
	/**
	 * 下载
	 *
	 * @param request
	 * @param response
	 * @throws Exception
	 */
	@RequestMapping("/download/{code}")
	@GlobalInterceptor(checkLogin = false, checkParams = true)
	public void download(HttpServletRequest request, HttpServletResponse response,
						 @PathVariable("code") @VerifyParam(required = true) String code) throws Exception {
		super.download(request, response, code);
	}
	
	/**
	 * 保存分享
	 *
	 * @param session
	 * @param shareId
	 * @param shareFileIds
	 * @param myFolderId
	 * @return
	 */
	@RequestMapping("/saveShare")
	@GlobalInterceptor(checkParams = true)
	public ResponseVO saveShare(HttpSession session,
								@VerifyParam(required = true) String shareId,
								@VerifyParam(required = true) String shareFileIds,
								@VerifyParam(required = true) String myFolderId) {
		SessionShareDto shareSessionDto = checkShare(session, shareId);
		SessionWebUserDto webUserDto = getUserInfoSession(session);
		if (shareSessionDto.getShareUserId().equals(webUserDto.getUserId())) {
			throw new BusinessException("自己分享的文件无法保存到自己的网盘");
		}
		fileInfoService.saveShare(shareSessionDto.getFileId(), shareFileIds, myFolderId, shareSessionDto.getShareUserId(), webUserDto.getUserId());
		return getSuccessResponseVO(null);
	}

}
