package com.easypan.service.impl;

import com.easypan.component.RedisComponent;
import com.easypan.entity.config.AppConfig;
import com.easypan.entity.constants.Constants;
import com.easypan.entity.dto.SessionWebUserDto;
import com.easypan.entity.dto.UploadResultDto;
import com.easypan.entity.dto.UserSpaceDto;
import com.easypan.entity.enums.*;
import com.easypan.entity.po.FileInfo;
import com.easypan.entity.po.UserInfo;
import com.easypan.entity.query.FileInfoQuery;
import com.easypan.entity.query.SimplePage;
import com.easypan.entity.query.UserInfoQuery;
import com.easypan.entity.vo.PaginationResultVO;

import com.easypan.exception.BusinessException;
import com.easypan.mappers.FileInfoMapper;
import com.easypan.mappers.UserInfoMapper;
import com.easypan.service.FileInfoService;

import com.easypan.utils.DateUtil;
import com.easypan.utils.ProcessUtils;
import com.easypan.utils.ScaleFilter;
import com.easypan.utils.StringTools;
import com.sun.org.apache.xpath.internal.operations.Bool;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.DateFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


/**
 * 文件信息 业务接口实现
 */
@Service("fileInfoService")
@Slf4j
public class FileInfoServiceImpl implements FileInfoService {
	
	@Resource
	private FileInfoMapper<FileInfo, FileInfoQuery> fileInfoMapper;
	
	@Resource
	private UserInfoMapper<UserInfo, UserInfoQuery> userInfoMapper;
	@Resource
	private RedisComponent redisComponent;
	
	@Resource
	private AppConfig appConfig;
	
	@Resource
	@Lazy
	private FileInfoServiceImpl fileInfoService;
	
	/**
	 * 根据条件查询列表
	 */
	@Override
	public List<FileInfo> findListByParam(FileInfoQuery param) {
		return this.fileInfoMapper.selectList(param);
	}
	
	/**
	 * 根据条件查询列表
	 */
	@Override
	public Integer findCountByParam(FileInfoQuery param) {
		return this.fileInfoMapper.selectCount(param);
	}
	
	/**
	 * 分页查询方法
	 */
	@Override
	public PaginationResultVO<FileInfo> findListByPage(FileInfoQuery param) {
		int count = this.findCountByParam(param);
		int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();
		
		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<FileInfo> list = this.findListByParam(param);
		PaginationResultVO<FileInfo> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}
	
	/**
	 * 新增
	 */
	@Override
	public Integer add(FileInfo bean) {
		return this.fileInfoMapper.insert(bean);
	}
	
	/**
	 * 批量新增
	 */
	@Override
	public Integer addBatch(List<FileInfo> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.fileInfoMapper.insertBatch(listBean);
	}
	
	/**
	 * 批量新增或者修改
	 */
	@Override
	public Integer addOrUpdateBatch(List<FileInfo> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.fileInfoMapper.insertOrUpdateBatch(listBean);
	}
	
	/**
	 * 根据FileIdAndUserId获取对象
	 */
	@Override
	public FileInfo getFileInfoByFileIdAndUserId(String fileId, String userId) {
		return this.fileInfoMapper.selectByFileIdAndUserId(fileId, userId);
	}
	
	/**
	 * 根据FileIdAndUserId修改
	 */
	@Override
	public Integer updateFileInfoByFileIdAndUserId(FileInfo bean, String fileId, String userId) {
		return this.fileInfoMapper.updateByFileIdAndUserId(bean, fileId, userId);
	}
	
	/**
	 * 根据FileIdAndUserId删除
	 */
	@Override
	public Integer deleteFileInfoByFileIdAndUserId(String fileId, String userId) {
		return this.fileInfoMapper.deleteByFileIdAndUserId(fileId, userId);
	}
	
	/**
	 * 上传文件
	 *
	 * @param webUserDto web用户dto
	 * @param fileId     文件标识
	 * @param file       文件
	 * @param fileName   文件名称
	 * @param filePid    文件父级id
	 * @param fileMd5    文件md5
	 * @param chunkIndex 块索引
	 * @param chunks     块
	 * @return {@link UploadResultDto}
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public UploadResultDto uploadFile(SessionWebUserDto webUserDto, String fileId, MultipartFile file, String fileName, String filePid, String fileMd5,
									  Integer chunkIndex, Integer chunks) {
		File tempFileFolder = null;
		Boolean uploadSuccess = true;
		try {
			UploadResultDto resultDto = new UploadResultDto();
			if (StringTools.isEmpty(fileId)) {
				fileId = StringTools.getRandomString(Constants.LENGTH_10);
			}
			resultDto.setFileId(fileId);
			Date curDate = new Date();
			UserSpaceDto spaceDto = redisComponent.getUserSpaceUse(webUserDto.getUserId());
			if (chunkIndex == 0) {
				FileInfoQuery infoQuery = new FileInfoQuery();
				infoQuery.setFileMd5(fileMd5);
				infoQuery.setSimplePage(new SimplePage(0, 1));
				infoQuery.setStatus(FileStatusEnums.USING.getStatus());
				List<FileInfo> dbFileList = this.fileInfoMapper.selectList(infoQuery);
				//秒传
				if (!dbFileList.isEmpty()) {
					FileInfo dbFile = dbFileList.get(0);
					//判断文件状态
					if (dbFile.getFileSize() + spaceDto.getUseSpace() > spaceDto.getTotalSpace()) {
						throw new BusinessException(ResponseCodeEnum.CODE_904);
					}
					dbFile.setFileId(fileId);
					dbFile.setFilePid(filePid);
					dbFile.setUserId(webUserDto.getUserId());
					dbFile.setFileMd5(null);
					dbFile.setCreateTime(curDate);
					dbFile.setLastUpdateTime(curDate);
					dbFile.setStatus(FileStatusEnums.USING.getStatus());
					dbFile.setDelFlag(FileDelFlagEnums.USING.getFlag());
					dbFile.setFileMd5(fileMd5);
					fileName = autoRename(filePid, webUserDto.getUserId(), fileName);
					dbFile.setFileName(fileName);
					this.fileInfoMapper.insert(dbFile);
					resultDto.setStatus(UploadStatusEnums.UPLOAD_SECONDS.getCode());
					//更新用户空间使用
					updateUserSpace(webUserDto, dbFile.getFileSize());
					
					return resultDto;
				}
			}
			//暂存在临时目录
			String tempFolderName = appConfig.getProjectFolder() + Constants.FILE_FOLDER_TEMP;
			String currentUserFolderName = webUserDto.getUserId() + fileId;
			//创建临时目录
			tempFileFolder = new File(tempFolderName + currentUserFolderName);
			if (!tempFileFolder.exists()) {
				tempFileFolder.mkdirs();
			}
			
			//判断磁盘空间
			Long currentTempSize = redisComponent.getFileTempSize(webUserDto.getUserId(), fileId);
			if (file.getSize() + currentTempSize + spaceDto.getUseSpace() > spaceDto.getTotalSpace()) {
				throw new BusinessException(ResponseCodeEnum.CODE_904);
			}
			
			File newFile = new File(tempFileFolder.getPath() + "/" + chunkIndex);
			file.transferTo(newFile);
			//保存临时大小
			redisComponent.saveFileTempSize(webUserDto.getUserId(), fileId, file.getSize());
			//不是最后一个分片，直接返回
			if (chunkIndex < chunks - 1) {
				resultDto.setStatus(UploadStatusEnums.UPLOADING.getCode());
				return resultDto;
			}
			//最后一个分片上传完成，记录数据库，异步合并分片
			String month = DateUtil.format(curDate, DateTimePatternEnum.YYYYMM.getPattern());
			String fileSuffix = StringTools.getFileSuffix(fileName);
			//真实文件名
			String realFileName = currentUserFolderName + fileSuffix;
			FileTypeEnums fileTypeEnum = FileTypeEnums.getFileTypeBySuffix(fileSuffix);
			//自动重命名
			fileName = autoRename(filePid, webUserDto.getUserId(), fileName);
			FileInfo fileInfo = new FileInfo();
			fileInfo.setFileId(fileId);
			fileInfo.setUserId(webUserDto.getUserId());
			fileInfo.setFileMd5(fileMd5);
			fileInfo.setFileName(fileName);
			fileInfo.setFilePath(month + "/" + realFileName);
			fileInfo.setFilePid(filePid);
			fileInfo.setCreateTime(curDate);
			fileInfo.setLastUpdateTime(curDate);
			fileInfo.setFileCategory(fileTypeEnum.getCategory().getCategory());
			fileInfo.setFileType(fileTypeEnum.getType());
			fileInfo.setStatus(FileStatusEnums.TRANSFER.getStatus());
			fileInfo.setFolderType(FileFolderTypeEnums.FILE.getType());
			fileInfo.setDelFlag(FileDelFlagEnums.USING.getFlag());
			this.fileInfoMapper.insert(fileInfo);
			
			Long totalSize = redisComponent.getFileTempSize(webUserDto.getUserId(), fileId);
			updateUserSpace(webUserDto, totalSize);
			
			resultDto.setStatus(UploadStatusEnums.UPLOAD_FINISH.getCode());
			//事务提交后调用异步方法
			TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
				@Override
				public void afterCommit() {
					fileInfoService.transferFile(fileInfo.getFileId(), webUserDto);
				}
			});
			return resultDto;
		} catch (BusinessException e) {
			uploadSuccess = false;
			log.error("文件上传失败", e);
			throw e;
		} catch (Exception e) {
			uploadSuccess = false;
			log.error("文件上传失败", e);
			throw new BusinessException("文件上传失败");
		} finally {
			//如果上传失败，清除临时目录
			if (tempFileFolder != null && !uploadSuccess) {
				try {
					FileUtils.deleteDirectory(tempFileFolder);
				} catch (IOException e) {
					log.error("删除临时目录失败");
				}
			}
		}
	}
	
	/**
	 * 新建文件夹
	 *
	 * @param filePid      pid
	 * @param fileName 文件名
	 * @param userId   用户id
	 * @return {@link FileInfo}
	 */
	@Override
	public FileInfo newFolder(String filePid, String fileName, String userId) {
		// 检查文件名
		checkFileName(filePid,fileName,userId,FileFolderTypeEnums.FOLDER.getType());
		// 存表
		Date curDate = new Date();
		FileInfo fileInfo = new FileInfo();
		fileInfo.setFileId(StringTools.getRandomString(Constants.LENGTH_10));
		fileInfo.setUserId(userId);
		fileInfo.setFilePid(filePid);
		fileInfo.setFileName(fileName);
		fileInfo.setFolderType(FileFolderTypeEnums.FOLDER.getType());
		fileInfo.setCreateTime(curDate);
		fileInfo.setLastUpdateTime(curDate);
		fileInfo.setStatus(FileStatusEnums.USING.getStatus());
		fileInfo.setDelFlag(FileDelFlagEnums.USING.getFlag());
		this.fileInfoMapper.insert(fileInfo);
		return fileInfo;
	}
	
	/**
	 * 重命名
	 *
	 * @param fileId   文件id
	 * @param userId   用户id
	 * @param fileName 文件名
	 * @return {@link FileInfoQuery}
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public FileInfo rename(String fileId, String userId, String fileName) {
		// 判断是否存在
		FileInfo fileInfo = this.fileInfoMapper.selectByFileIdAndUserId(fileId,userId);
		if (fileInfo == null) {
			throw new BusinessException("文件不存在");
		}
		String filePid = fileInfo.getFilePid();
		checkFileName(filePid,fileName,userId,fileInfo.getFolderType());
		// 获取文件后缀
		if (FileFolderTypeEnums.FILE.getType().equals(fileInfo.getFolderType())) {
			fileName = fileName + StringTools.getFileSuffix(fileInfo.getFileName());
		}
		Date curDate = new Date();
		FileInfo dbInfo = new FileInfo();
		dbInfo.setFileName(fileName);
		dbInfo.setLastUpdateTime(curDate);
		this.fileInfoMapper.updateByFileIdAndUserId(dbInfo,fileId,userId);
		
		FileInfoQuery fileInfoQuery = new FileInfoQuery();
		fileInfoQuery.setFilePid(filePid);
		fileInfoQuery.setUserId(userId);
		fileInfoQuery.setFileName(fileName);
		fileInfoQuery.setDelFlag(FileDelFlagEnums.USING.getFlag());
		Integer count = this.fileInfoMapper.selectCount(fileInfoQuery);
		if (count > 1) {
			throw new BusinessException("文件名" + fileName + "已存在");
		}
		fileInfo.setFileName(fileName);
		fileInfo.setLastUpdateTime(curDate);
		return fileInfo;
	}
	
	/**
	 * 更改文件夹
	 *
	 * @param fileIds 文件ID
	 * @param filePid 文件pid
	 * @param userId  用户id
	 */
	@Override
	public void changeFileFolder(String fileIds, String filePid, String userId) {
		if (fileIds.equals(filePid)) {
			throw new BusinessException(ResponseCodeEnum.CODE_600);
		}
		// 不在根目录下
		if (!Constants.ZERO_STR.equals(filePid)) {
			FileInfo fileInfo = fileInfoService.getFileInfoByFileIdAndUserId(filePid, userId);
			if (fileInfo == null || FileDelFlagEnums.DEL.getFlag().equals(fileInfo.getDelFlag())) {
				throw new BusinessException(ResponseCodeEnum.CODE_600);
			}
		}
		
		String[] fileIdArray = fileIds.split(",");
		FileInfoQuery query = new FileInfoQuery();
		query.setFilePid(filePid);
		query.setUserId(userId);
		List<FileInfo> dbFileList = fileInfoService.findListByParam(query);
	
		Map<String, FileInfo> dbFileNameMap = dbFileList.stream().collect(Collectors.toMap(FileInfo::getFileName, Function.identity(), (file1, file2) -> file2));
		// 查询选中的文件
		query = new FileInfoQuery();
		query.setUserId(userId);
		query.setFileIdArray(fileIdArray);
		List<FileInfo> selectFileList = this.findListByParam(query);
		
		// 将所选文件重命名
		for (FileInfo item : selectFileList) {
			FileInfo rootFileInfo = dbFileNameMap.get(item.getFileName());
			// 文件名已存在，重命名被还原的文件名
			FileInfo updateInfo = new FileInfo();
			if (rootFileInfo != null) {
				String fileName = StringTools.rename(item.getFileName());
				updateInfo.setFileName(fileName);
			}
			updateInfo.setFilePid(filePid);
			this.fileInfoMapper.updateByFileIdAndUserId(updateInfo, item.getFileId(), userId);
		}
	}
	
	/**
	 * 删除file到回收站
	 *
	 * @param userId  用户id
	 * @param fileIds 文件ID
	 */
	@Override
	public void removeFile2RecycleBatch(String userId, String fileIds) {
		
		String[] fileIdArray = fileIds.split(",");
		FileInfoQuery query = new FileInfoQuery();
		query.setUserId(userId);
		query.setFileIdArray(fileIdArray);
		query.setDelFlag(FileDelFlagEnums.USING.getFlag());
		List<FileInfo> fileInfoList = this.fileInfoMapper.selectList(query);
		if (fileInfoList.isEmpty()) {
			return;
		}
		// 删除文件时，要查询所有子文件
		List<String> delFilePidList = new ArrayList<>();
		for (FileInfo fileInfo : fileInfoList) {
			findAllSubFolderFileIdList(delFilePidList, userId, fileInfo.getFileId(), FileDelFlagEnums.USING.getFlag());
		}
		if (!delFilePidList.isEmpty()) {
			FileInfo updateInfo = new FileInfo();
			updateInfo.setDelFlag(FileDelFlagEnums.DEL.getFlag());
			this.fileInfoMapper.updateFileDelFlagBatch(updateInfo, userId, delFilePidList, null, FileDelFlagEnums.USING.getFlag());
		}
		// 将选中的文件更新为回收站状态
		List<String> delFileIdList = Arrays.asList(fileIdArray);
		FileInfo fileInfo = new FileInfo();
		fileInfo.setRecoveryTime(new Date());
		fileInfo.setDelFlag(FileDelFlagEnums.RECYCLE.getFlag());
		this.fileInfoMapper.updateFileDelFlagBatch(fileInfo, userId, null, delFileIdList, FileDelFlagEnums.USING.getFlag());
	}
	
	/**
	 * 恢复文件批处理
	 *
	 * @param userId  用户id
	 * @param fileIds 文件ID
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void recoverFileBatch(String userId, String fileIds) {
		String[] fileIdArray = fileIds.split(",");
		FileInfoQuery query = new FileInfoQuery();
		query.setUserId(userId);
		query.setFileIdArray(fileIdArray);
		query.setDelFlag(FileDelFlagEnums.RECYCLE.getFlag());
		List<FileInfo> fileInfoList = this.fileInfoMapper.selectList(query);
		List<String> delFileSubFolderFileIdList = new ArrayList<>();
		for (FileInfo fileInfo : fileInfoList) {
			if (FileFolderTypeEnums.FOLDER.getType().equals(fileInfo.getFolderType())) {
				findAllSubFolderFileIdList(delFileSubFolderFileIdList, userId, fileInfo.getFileId(),FileDelFlagEnums.DEL.getFlag());
			}
		}
		// 查找所有根目录文件
		query = new FileInfoQuery();
		query.setUserId(userId);
		query.setDelFlag(FileDelFlagEnums.USING.getFlag());
		query.setFilePid(Constants.ZERO_STR);
		List<FileInfo> allRootFileList = this.findListByParam(query);
		Map<String, FileInfo> rootFileMap = allRootFileList.stream().collect(Collectors.toMap(FileInfo::getFileName, Function.identity(), (file1, file2) -> file2));
		// 查询所有所选文件，将目录下所有删除的文件状态更新为使用中
		if (!delFileSubFolderFileIdList.isEmpty()) {
			FileInfo fileInfo = new FileInfo();
			fileInfo.setDelFlag(FileDelFlagEnums.USING.getFlag());
			this.fileInfoMapper.updateFileDelFlagBatch(fileInfo, userId, delFileSubFolderFileIdList, null, FileDelFlagEnums.DEL.getFlag());
		}
		// 将选中文件更新为正常，且更新其父级目录到根目录
		List<String> delFileIdList = Arrays.asList(fileIdArray);
		FileInfo fileInfo = new FileInfo();
		fileInfo.setDelFlag(FileDelFlagEnums.USING.getFlag());
		fileInfo.setFilePid(Constants.ZERO_STR);
		fileInfo.setLastUpdateTime(new Date());
		this.fileInfoMapper.updateFileDelFlagBatch(fileInfo,userId,null, delFileIdList, FileDelFlagEnums.RECYCLE.getFlag());
		// 将所选文件重命名
		for (FileInfo item : fileInfoList) {
			FileInfo rootFileInfo = rootFileMap.get(item.getFileName());
			// 文件名已存在，重命名被还原的文件名
			if (rootFileInfo != null) {
				String filename = StringTools.rename(item.getFileName());
				FileInfo updateInfo = new FileInfo();
				updateInfo.setFileName(filename);
				this.fileInfoMapper.updateByFileIdAndUserId(updateInfo,item.getFileId(), userId);
			}
		}
	}
	
	/**
	 * 删除文件批处理
	 *
	 * @param userId  用户id
	 * @param fileIds 文件ID
	 * @param adminOp 管理员操作
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void delFileBatch(String userId, String fileIds, Boolean adminOp) {
		String[] fileIdArray = fileIds.split(",");
		FileInfoQuery query = new FileInfoQuery();
		query.setUserId(userId);
		query.setFileIdArray(fileIdArray);
		query.setDelFlag(FileDelFlagEnums.RECYCLE.getFlag());
		List<FileInfo> fileInfoList = this.fileInfoMapper.selectList(query);
		
		List<String> delFileSubFileFolderFileIdList = new ArrayList<>();
		// 找到所选文件子目录id
		for (FileInfo fileInfo: fileInfoList) {
			if (FileFolderTypeEnums.FOLDER.getType().equals(fileInfo.getFileType())) {
			findAllSubFolderFileIdList(delFileSubFileFolderFileIdList, userId,fileInfo.getFileId(), FileDelFlagEnums.DEL.getFlag());
			}
		}
		
		// 删除所选文件子目录中的文件
		if (!delFileSubFileFolderFileIdList.isEmpty()) {
			this.fileInfoMapper.delFileBatch(userId, delFileSubFileFolderFileIdList, null, adminOp ? null : FileDelFlagEnums.DEL.getFlag());
		}
		
		// 删除所选文件
		this.fileInfoMapper.delFileBatch(userId, null, Arrays.asList(fileIdArray), adminOp ? null : FileDelFlagEnums.RECYCLE.getFlag());
		
		// 更新用户空间
		Long useSpace = this.fileInfoMapper.selectUseSpace(userId);
		UserInfo userInfo = new UserInfo();
		userInfo.setUseSpace(useSpace);
		this.userInfoMapper.updateByUserId(userInfo, userId);
		
		// 更新缓存
		UserSpaceDto userSpaceDto = redisComponent.getUserSpaceUse(userId);
		userSpaceDto.setUseSpace(useSpace);
		redisComponent.saveUserSpaceUse(userId,userSpaceDto);
	}
	
	@Override
	public void deleteFileByUserId(String userId) {
		this.fileInfoMapper.deleteFileByUserId(userId);
	}
	
	
	/**
	 * 检查根文件pid
	 *
	 * @param rootFilePId 根文件pid
	 * @param shareUserId 共享用户id
	 * @param fileid      fileid
	 */
	@Override
	public void checkRootFilePid(String rootFilePId, String shareUserId, String fileid) {
		if (StringTools.isEmpty(fileid)){
			throw new BusinessException(ResponseCodeEnum.CODE_600);
		}
		if (rootFilePId.equals(fileid)) {
			return;
		}
		checkFilePid(rootFilePId,fileid,shareUserId);
	}
	
	
	/**
	 * 保存共享
	 *
	 * @param shareRootFilePid 共享根文件pid
	 * @param shareFileIds     共享文件ID
	 * @param myFolderId       我文件夹id
	 * @param shareUserId      共享用户id
	 * @param cureentUserId    用户名（&N）：
	 */
	@Override
	@Transactional
	public void saveShare(String shareRootFilePid, String shareFileIds, String myFolderId, String shareUserId, String cureentUserId) {
		String[] shareFileIdArray = shareFileIds.split(",");
		//目标目录文件列表
		FileInfoQuery fileInfoQuery = new FileInfoQuery();
		fileInfoQuery.setUserId(cureentUserId);
		fileInfoQuery.setFilePid(myFolderId);
		List<FileInfo> currentFileList = this.fileInfoMapper.selectList(fileInfoQuery);
		Map<String, FileInfo> currentFileMap = currentFileList.stream().collect(Collectors.toMap(FileInfo::getFileName, Function.identity(), (file1, file2) -> file2));
		//选择的文件
		fileInfoQuery = new FileInfoQuery();
		fileInfoQuery.setUserId(shareUserId);
		fileInfoQuery.setFileIdArray(shareFileIdArray);
		List<FileInfo> shareFileList = this.fileInfoMapper.selectList(fileInfoQuery);
		//重命名选择的文件
		List<FileInfo> copyFileList = new ArrayList<>();
		Date curDate = new Date();
		for (FileInfo item : shareFileList) {
			FileInfo haveFile = currentFileMap.get(item.getFileName());
			if (haveFile != null) {
				item.setFileName(StringTools.rename(item.getFileName()));
			}
			findAllSubFile(copyFileList, item, shareUserId, cureentUserId, curDate, myFolderId);
		}
		this.fileInfoMapper.insertBatch(copyFileList);
		
		//更新空间
		Long useSpace = this.fileInfoMapper.selectUseSpace(cureentUserId);
		UserInfo dbUserInfo = this.userInfoMapper.selectByUserId(cureentUserId);
		if (useSpace > dbUserInfo.getTotalSpace()) {
			throw new BusinessException(ResponseCodeEnum.CODE_904);
		}
		UserInfo userInfo = new UserInfo();
		userInfo.setUseSpace(useSpace);
		this.userInfoMapper.updateByUserId(userInfo, cureentUserId);
		//设置缓存
		UserSpaceDto userSpaceDto = redisComponent.getUserSpaceUse(cureentUserId);
		userSpaceDto.setUseSpace(useSpace);
		redisComponent.saveUserSpaceUse(cureentUserId, userSpaceDto);
	}
	
	private void findAllSubFile(List<FileInfo> copyFileList, FileInfo fileInfo, String sourceUserId, String currentUserId, Date curDate, String newFilePid) {
		String sourceFileId = fileInfo.getFileId();
		fileInfo.setCreateTime(curDate);
		fileInfo.setLastUpdateTime(curDate);
		fileInfo.setFilePid(newFilePid);
		fileInfo.setUserId(currentUserId);
		String newFileId = StringTools.getRandomString(Constants.LENGTH_10);
		fileInfo.setFileId(newFileId);
		copyFileList.add(fileInfo);
		if (FileFolderTypeEnums.FOLDER.getType().equals(fileInfo.getFolderType())) {
			FileInfoQuery query = new FileInfoQuery();
			query.setFilePid(sourceFileId);
			query.setUserId(sourceUserId);
			List<FileInfo> sourceFileList = this.fileInfoMapper.selectList(query);
			for (FileInfo item : sourceFileList) {
				findAllSubFile(copyFileList, item, sourceUserId, currentUserId, curDate, newFileId);
			}
		}
	}
	
	/**
	 * 检查文件pid
	 *
	 * @param rootFilePid 根文件pid
	 * @param fileId      文件id
	 * @param userId      用户id
	 */
	private void checkFilePid(String rootFilePid, String fileId, String userId) {
		FileInfo fileInfo = this.fileInfoMapper.selectByFileIdAndUserId(fileId, userId);
		if (fileInfo == null) {
			throw new BusinessException(ResponseCodeEnum.CODE_600);
		}
		if (Constants.ZERO_STR.equals(fileInfo.getFilePid())) {
			throw new BusinessException(ResponseCodeEnum.CODE_600);
		}
		if (fileInfo.getFilePid().equals(rootFilePid)) {
			return;
		}
		checkFilePid(rootFilePid, fileInfo.getFilePid(), userId);
	}
	
	/**
	 * 递归查找所有子文件夹文件列表
	 *
	 * @param fileIdList 文件id列表
	 * @param userId     用户id
	 * @param fileId     文件id
	 * @param delFlag    德尔旗
	 */
	private void findAllSubFolderFileIdList(List<String> fileIdList, String userId, String fileId, Integer delFlag) {
		fileIdList.add(fileId);
		FileInfoQuery query = new FileInfoQuery();
		query.setUserId(userId);
		query.setFilePid(fileId);
		query.setDelFlag(delFlag);
		query.setFolderType(FileFolderTypeEnums.FOLDER.getType());
		List<FileInfo> fileInfoList = this.fileInfoMapper.selectList(query);
		for (FileInfo fileInfo : fileInfoList) {
			findAllSubFolderFileIdList(fileIdList, userId, fileInfo.getFileId(), delFlag);
		}
	}
	/**
	 * 检查文件名
	 *
	 * @param filePid    文件pid
	 * @param fileName   文件名
	 * @param userId     用户id
	 * @param folderType 文件夹类型
	 */
	private void checkFileName(String filePid, String fileName, String userId, Integer folderType) {
		FileInfoQuery fileInfoQuery = new FileInfoQuery();
		fileInfoQuery.setFolderType(folderType);
		fileInfoQuery.setFileName(fileName);
		fileInfoQuery.setFilePid(filePid);
		fileInfoQuery.setUserId(userId);
		fileInfoQuery.setDelFlag(FileDelFlagEnums.USING.getFlag());
		Integer count = this.fileInfoMapper.selectCount(fileInfoQuery);
		if (count > 0) {
			throw new BusinessException("此目录下已存在同名文件，请修改名称");
		}
	}
	
	/**
	 * 自动重命名
	 *
	 * @param filePid  文件pid
	 * @param userId   用户id
	 * @param fileName 文件名称
	 * @return {@link String}
	 */
	private String autoRename(String filePid, String userId, String fileName) {
		FileInfoQuery fileInfoQuery = new FileInfoQuery();
		fileInfoQuery.setUserId(userId);
		fileInfoQuery.setFilePid(filePid);
		fileInfoQuery.setDelFlag(FileDelFlagEnums.USING.getFlag());
		fileInfoQuery.setFileName(fileName);
		Integer count = this.fileInfoMapper.selectCount(fileInfoQuery);
		if (count > 0) {
			return StringTools.rename(fileName);
		}
		
		return fileName;
	}
	
	private void updateUserSpace(SessionWebUserDto webUserDto, Long totalSize) {
		Integer count = userInfoMapper.updateUserSpace(webUserDto.getUserId(), totalSize, null);
		if (count == 0) {
			throw new BusinessException(ResponseCodeEnum.CODE_904);
		}
		UserSpaceDto spaceDto = redisComponent.getUserSpaceUse(webUserDto.getUserId());
		spaceDto.setUseSpace(spaceDto.getUseSpace() + totalSize);
		redisComponent.saveUserSpaceUse(webUserDto.getUserId(), spaceDto);
	}
	
	/**
	 * 文件转码
	 *
	 * @param fileId     文件标识
	 * @param webUserDto web用户dto
	 */
	@Async
	public void transferFile(String fileId, SessionWebUserDto webUserDto) {
		Boolean transferSuccess = true;
		String targetFilePath = null;
		String cover = null;
		FileTypeEnums fileTypeEnum = null;
		FileInfo fileInfo = fileInfoMapper.selectByFileIdAndUserId(fileId, webUserDto.getUserId());
		try {
			if (fileInfo == null || !FileStatusEnums.TRANSFER.getStatus().equals(fileInfo.getStatus())) {
				return;
			}
			//临时目录
			String tempFolderName = appConfig.getProjectFolder() + Constants.FILE_FOLDER_TEMP;
			String currentUserFolderName = webUserDto.getUserId() + fileId;
			File fileFolder = new File(tempFolderName + currentUserFolderName);
			if (!fileFolder.exists()) {
				fileFolder.mkdirs();
			}
			//文件后缀
			String fileSuffix = StringTools.getFileSuffix(fileInfo.getFileName());
			String month = DateUtil.format(fileInfo.getCreateTime(), DateTimePatternEnum.YYYYMM.getPattern());
			//目标目录
			String targetFolderName = appConfig.getProjectFolder() + Constants.FILE_FOLDER_FILE;
			File targetFolder = new File(targetFolderName + "/" + month);
			if (!targetFolder.exists()) {
				targetFolder.mkdirs();
			}
			//真实文件名
			String realFileName = currentUserFolderName + fileSuffix;
			//真实文件路径
			targetFilePath = targetFolder.getPath() + "/" + realFileName;
			//合并文件
			union(fileFolder.getPath(), targetFilePath, fileInfo.getFileName(), true);
			//视频文件切割
			fileTypeEnum = FileTypeEnums.getFileTypeBySuffix(fileSuffix);
			if (FileTypeEnums.VIDEO == fileTypeEnum) {
				cutFile4Video(fileId, targetFilePath);
				//视频生成缩略图
				cover = month + "/" + currentUserFolderName + Constants.IMAGE_PNG_SUFFIX;
				String coverPath = targetFolderName + "/" + cover;
				ScaleFilter.createCover4Video(new File(targetFilePath), Constants.LENGTH_150, new File(coverPath));
			} else if (FileTypeEnums.IMAGE == fileTypeEnum) {
				//生成缩略图
				cover = month + "/" + realFileName.replace(".", "_.");
				String coverPath = targetFolderName + "/" + cover;
				Boolean created = ScaleFilter.createThumbnailWidthFFmpeg(new File(targetFilePath), Constants.LENGTH_150, new File(coverPath), false);
				if (!created) {
					FileUtils.copyFile(new File(targetFilePath), new File(coverPath));
				}
			}
		} catch (Exception e) {
			log.error("文件转码失败，文件Id:{},userId:{}", fileId, webUserDto.getUserId(), e);
			transferSuccess = false;
		} finally {
			FileInfo updateInfo = new FileInfo();
			updateInfo.setFileSize(new File(targetFilePath).length());
			updateInfo.setFileCover(cover);
			updateInfo.setStatus(transferSuccess ? FileStatusEnums.USING.getStatus() : FileStatusEnums.TRANSFER_FAIL.getStatus());
			fileInfoMapper.updateFileStatusWithOldStatus(fileId, webUserDto.getUserId(), updateInfo, FileStatusEnums.TRANSFER.getStatus());
		}
	}
	
	/**
	 * 合并文件
	 *
	 * @param dirPath    dir路径
	 * @param toFilePath 文件路径
	 * @param fileName   文件名称
	 * @param delSource  是否删除源文件
	 */
	public static void union(String dirPath, String toFilePath, String fileName, boolean delSource) throws BusinessException {
		File dir = new File(dirPath);
		if (!dir.exists()) {
			throw new BusinessException("目录不存在");
		}
		File fileList[] = dir.listFiles();
		File targetFile = new File(toFilePath);
		RandomAccessFile writeFile = null;
		try {
			writeFile = new RandomAccessFile(targetFile, "rw");
			byte[] b = new byte[1024 * 10];
			for (int i = 0; i < fileList.length; i++) {
				int len = -1;
				//创建读块文件的对象
				File chunkFile = new File(dirPath + File.separator + i);
				RandomAccessFile readFile = null;
				try {
					readFile = new RandomAccessFile(chunkFile, "r");
					while ((len = readFile.read(b)) != -1) {
						writeFile.write(b, 0, len);
					}
				} catch (Exception e) {
					log.error("合并分片失败", e);
					throw new BusinessException("合并文件失败");
				} finally {
					readFile.close();
				}
			}
		} catch (Exception e) {
			log.error("合并文件:{}失败", fileName, e);
			throw new BusinessException("合并文件" + fileName + "出错了");
		} finally {
			try {
				if (null != writeFile) {
					writeFile.close();
				}
			} catch (IOException e) {
				log.error("关闭流失败", e);
			}
			if (delSource) {
				if (dir.exists()) {
					try {
						FileUtils.deleteDirectory(dir);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	/**
	 * 分割视频文件
	 *
	 * @param fileId        文件标识
	 * @param videoFilePath 视频文件路径
	 */
	private void cutFile4Video(String fileId, String videoFilePath) {
		//创建同名切片目录
		File tsFolder = new File(videoFilePath.substring(0, videoFilePath.lastIndexOf(".")));
		if (!tsFolder.exists()) {
			tsFolder.mkdirs();
		}
		final String CMD_TRANSFER_2TS = "ffmpeg -y -i %s  -vcodec copy -acodec copy -vbsf h264_mp4toannexb %s";
		final String CMD_CUT_TS = "ffmpeg -i %s -c copy -map 0 -f segment -segment_list %s -segment_time 30 %s/%s_%%4d.ts";
		
		String tsPath = tsFolder + "/" + Constants.TS_NAME;
		//生成.ts
		String cmd = String.format(CMD_TRANSFER_2TS, videoFilePath, tsPath);
		ProcessUtils.executeCommand(cmd, false);
		//生成索引文件.m3u8 和切片.ts
		cmd = String.format(CMD_CUT_TS, tsPath, tsFolder.getPath() + "/" + Constants.M3U8_NAME, tsFolder.getPath(), fileId);
		ProcessUtils.executeCommand(cmd, false);
		//删除index.ts
		new File(tsPath).delete();
	}
}