package com.easypan.controller;


import com.easypan.annotation.GlobalInterceptor;
import com.easypan.annotation.VerifyParam;
import com.easypan.entity.dto.SessionWebUserDto;
import com.easypan.entity.dto.UploadResultDto;
import com.easypan.entity.enums.FileCategoryEnums;
import com.easypan.entity.enums.FileDelFlagEnums;
import com.easypan.entity.enums.FileFolderTypeEnums;
import com.easypan.entity.po.FileInfo;
import com.easypan.entity.query.FileInfoQuery;
import com.easypan.entity.vo.FileInfoVO;
import com.easypan.entity.vo.PaginationResultVO;
import com.easypan.entity.vo.ResponseVO;
import com.easypan.service.FileInfoService;
import com.easypan.utils.CopyTools;
import com.easypan.utils.StringTools;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.List;


/**
 * 文件信息 Controller
 */
@RestController("fileInfoController")
@RequestMapping("/file")
public class FileInfoController  extends CommonFileController{
	
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
	
	/**
	 * 上传文件
	 *
	 * @param session    会话
	 * @param fileId     文件标识
	 * @param file       文件
	 * @param fileName   文件名称
	 * @param filePid    文件父级id
	 * @param fileMd5    文件md5
	 * @param chunkIndex 块索引
	 * @param chunks     块
	 * @return {@link ResponseVO}
	 */
	@RequestMapping("/uploadFile")
	@GlobalInterceptor(checkParams = true)
	public ResponseVO uploadFile(HttpSession session,
								 String fileId,
								 MultipartFile file,
								 @VerifyParam(required = true) String fileName,
								 @VerifyParam(required = true) String filePid,
								 @VerifyParam(required = true) String fileMd5,
								 @VerifyParam(required = true) Integer chunkIndex,
								 @VerifyParam(required = true) Integer chunks
								 ) {
		SessionWebUserDto webUserDto = getUserInfoSession(session);
		UploadResultDto resultDto = fileInfoService.uploadFile(webUserDto, fileId, file, fileName, filePid, fileMd5, chunkIndex, chunks);
		return getSuccessResponseVO(resultDto);
	}
	
	@RequestMapping("/getImage/{imageFolder}/{imageName}")
	@GlobalInterceptor(checkParams = true)
	public void getImage(HttpServletResponse response, @PathVariable("imageFolder") String imageFolder, @PathVariable("imageName") String imageName) {
		super.getImage(response, imageFolder, imageName);
	}
	
	@RequestMapping("/ts/getVideoInfo/{fileId}")
	@GlobalInterceptor(checkParams = true)
	public void getVideoInfo(HttpServletResponse response, HttpSession session, @PathVariable("fileId") @VerifyParam(required = true) String fileId) {
		SessionWebUserDto webUserDto = getUserInfoSession(session);
		super.getFile(response, fileId, webUserDto.getUserId());
	}
	
	/**
	 * 获取文件
	 *
	 * @param response 回答
	 * @param session  一场
	 * @param fileId   文件id
	 */
	@RequestMapping("/getFile/{fileId}")
	@GlobalInterceptor(checkParams = true)
	public void getFile(HttpServletResponse response, HttpSession session, @PathVariable("fileId") @VerifyParam(required = true) String fileId) {
		SessionWebUserDto webUserDto = getUserInfoSession(session);
		super.getFile(response, fileId, webUserDto.getUserId());
	}
	
	/**
	 * 新建文件夹
	 *
	 * @param session  一场
	 * @param filePid  文件pid
	 * @param fileName 文件名
	 * @return {@link ResponseVO}
	 */
	@RequestMapping("/newFoloder")
	@GlobalInterceptor(checkParams = true)
	public ResponseVO newFoloder( HttpSession session,@VerifyParam(required = true) String filePid,
							@VerifyParam(required = true) String fileName) {
		SessionWebUserDto webUserDto = getUserInfoSession(session);
		FileInfo fileInfo = fileInfoService.newFolder(filePid, fileName, webUserDto.getUserId());
		return getSuccessResponseVO(CopyTools.copy(fileInfo, FileInfoVO.class));
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
									 @VerifyParam(required = true) String path) {
		SessionWebUserDto webUserDto = getUserInfoSession(session);
		return super.getFolderInfo(path, webUserDto.getUserId());
	}
	
	
	/**
	 * 重命名
	 *
	 * @param session  一场
	 * @param fileId   文件id
	 * @param fileName 文件名
	 * @return {@link ResponseVO}
	 */
	@RequestMapping("/rename")
	@GlobalInterceptor(checkParams = true)
	public ResponseVO rename( HttpSession session,
									 @VerifyParam(required = true) String fileId,
							         @VerifyParam(required = true) String fileName) {
		SessionWebUserDto webUserDto = getUserInfoSession(session);
		FileInfo fileInfo = fileInfoService.rename(fileId, webUserDto.getUserId(), fileName);
		return getSuccessResponseVO(CopyTools.copy(fileInfo, FileInfoVO.class));
	}
	
	/**
	 * 加载所有文件夹
	 *
	 * @param session        一场
	 * @param filePid        文件pid
	 * @param currentFileIds 当前文件ID
	 * @return {@link ResponseVO}
	 */
	@RequestMapping("/loadAllFolder")
	@GlobalInterceptor(checkParams = true)
	public ResponseVO loadAllFolder( HttpSession session,
							  @VerifyParam(required = true) String filePid, String currentFileIds) {
		SessionWebUserDto webUserDto = getUserInfoSession(session);
		FileInfoQuery infoQuery = new FileInfoQuery();
		infoQuery.setFilePid(filePid);
		if (!StringTools.isEmpty(currentFileIds)) {
			infoQuery.setExcludeFileIdArray(currentFileIds.split(","));
		}
		infoQuery.setFolderType(FileFolderTypeEnums.FOLDER.getType());
		infoQuery.setDelFlag(FileDelFlagEnums.USING.getFlag());
		infoQuery.setOrderBy("create_time desc");
		List<FileInfo> fileInfoList = fileInfoService.findListByParam(infoQuery);
		return getSuccessResponseVO(CopyTools.copyList(fileInfoList, FileInfoVO.class));
	}
	
	/**
	 * 更改文件夹
	 *
	 * @param session 一场
	 * @param fileIds 文件ID
	 * @param filePid 文件pid
	 * @return {@link ResponseVO}
	 */
	@RequestMapping("/changeFileFolder")
	@GlobalInterceptor(checkParams = true)
	public ResponseVO changeFileFolder( HttpSession session,
									    @VerifyParam(required = true) String fileIds,
										@VerifyParam(required = true) String filePid) {
		SessionWebUserDto webUserDto = getUserInfoSession(session);
		fileInfoService.changeFileFolder(fileIds,filePid, webUserDto.getUserId());
		return getSuccessResponseVO(null);
	}
	
	/**
	 * 创建下载url
	 *
	 * @param session 一场
	 * @param fileId  文件id
	 * @return {@link ResponseVO}
	 */
	@RequestMapping("/createDownloadUrl/{fileId}")
	@GlobalInterceptor(checkParams = true)
	public ResponseVO createDownloadUrl( HttpSession session,
										@VerifyParam(required = true) @PathVariable("fileId") String fileId) {
		SessionWebUserDto webUserDto = getUserInfoSession(session);
		return super.createDownloadUrl(fileId, webUserDto.getUserId());
	}
	
	/**
	 * 下载
	 *
	 * @param request  要求
	 * @param response 回答
	 * @param code     密码
	 * @throws Exception 例外
	 */
	@RequestMapping("/download/{code}")
	@GlobalInterceptor(checkParams = true, checkLogin = false)
	public void download(HttpServletRequest request, HttpServletResponse response,
							   @VerifyParam(required = true) @PathVariable("code") String code) throws Exception {
		super.download(request, response, code);
	}
	
	@RequestMapping("/delFile")
	@GlobalInterceptor(checkParams = true)
	public ResponseVO delFile( HttpSession session,
										 @VerifyParam(required = true) String fileIds) {
		SessionWebUserDto webUserDto = getUserInfoSession(session);
		fileInfoService.removeFile2RecycleBatch(webUserDto.getUserId(), fileIds);
		return getSuccessResponseVO(null);
	}
	
	
	
	
	
	
	
	
	
	
	
}