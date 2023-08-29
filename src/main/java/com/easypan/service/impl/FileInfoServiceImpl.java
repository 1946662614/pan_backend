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
import com.easypan.utils.StringTools;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.context.annotation.Lazy;
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
	 * @param fileMD5    文件md5
	 * @param chunkIndex 块索引
	 * @param chunks     块
	 * @return {@link UploadResultDto}
	 */
	@Override
	@Transactional
	public UploadResultDto uploadFile(SessionWebUserDto webUserDto, String fileId, MultipartFile file, String fileName,
									  String filePid, String fileMD5, Integer chunkIndex, Integer chunks) {
		UploadResultDto resultDto = null;
		Boolean uploadSuccess = true;
		File tempFileFolder = null;
		try {
			resultDto = new UploadResultDto();
			if (StringTools.isEmpty(fileId)) {
				fileId = StringTools.getRandomString(Constants.LENGTH_10);
			}
			resultDto.setFileId(fileId);
			Date curDate = new Date();
			
			UserSpaceDto spaceDto = redisComponent.getUserSpaceUse(webUserDto.getUserId());
			
			if (chunkIndex == 0) {
				FileInfoQuery infoQuery = new FileInfoQuery();
				infoQuery.setFileMd5(fileMD5);
				infoQuery.setSimplePage(new SimplePage(0, 1));
				infoQuery.setStatus(FileStatusEnums.USING.getStatus());
				// 秒传
				List<FileInfo> dbFileList = this.fileInfoMapper.selectList(infoQuery);
				if (!dbFileList.isEmpty()) {
					FileInfo dbFile = dbFileList.get(0);
					// 判断文件大小
					if (dbFile.getFileSize() + spaceDto.getUseSpace() > spaceDto.getTotalSpace()) {
						// 网盘空间不足
						throw new BusinessException(ResponseCodeEnum.CODE_904);
					}
					dbFile.setFileId(fileId);
					dbFile.setFilePid(webUserDto.getUserId());
					dbFile.setCreateTime(curDate);
					dbFile.setLastUpdateTime(curDate);
					dbFile.setStatus(FileStatusEnums.USING.getStatus());
					dbFile.setDelFlag(FileDelFlagEnums.USING.getFlag());
					dbFile.setFileMd5(fileMD5);
					// 文件重命名
					fileName = autoRename(filePid, webUserDto.getUserId(), fileName);
					dbFile.setFileName(fileName);
					this.fileInfoMapper.insert(dbFile);
					resultDto.setStatus(UploadStatusEnums.UPLOAD_SECONDS.getCode());
					// 更新用户使用空间
					updateUserSpace(webUserDto, dbFile.getFileSize());
					return resultDto;
				}
			}
			// 判断磁盘空间
			Long currentTempSize = redisComponent.getFileTempSize(webUserDto.getUserId(), fileId);
			if (file.getSize() + currentTempSize + spaceDto.getUseSpace() > spaceDto.getTotalSpace()) {
				throw new BusinessException(ResponseCodeEnum.CODE_904);
			}
			// 暂存临时目录
			String tempFolderName = appConfig.getProjectFolder() + Constants.FILE_FOLDER_TEMP;
			String currentUserFolderName = webUserDto.getUserId() + fileId;
			
			tempFileFolder = new File(tempFolderName + currentUserFolderName);
			if (!tempFileFolder.exists()) {
				tempFileFolder.mkdirs();
			}
			
			// 文件上传
			File newFile = new File(tempFileFolder.getPath() + "/" + chunkIndex);
			file.transferTo(newFile);
			if (chunkIndex < chunks - 1) {
				resultDto.setStatus(UploadStatusEnums.UPLOADING.getCode());
				// 保存临时大小
				redisComponent.saveFileTempSize(webUserDto.getUserId(), fileId, file.getSize());
				return resultDto;
			}
			// 最后一个分片上传完成，记录数据库，异步合并分片
			String month = DateUtil.format(new Date(), DateTimePatternEnum.YYYYMM.getPattern());
			String fileSuffix = StringTools.getFileSuffix(fileName);
			// 真实文件名
			String realFileName = currentUserFolderName + fileSuffix;
			FileTypeEnums fileTypeEnums = FileTypeEnums.getFileTypeBySuffix(fileSuffix);
			// 自动重命名
			fileName = autoRename(filePid, webUserDto.getUserId(), fileName);
			
			FileInfo fileInfo = new FileInfo();
			fileInfo.setFileId(fileId);
			fileInfo.setUserId(webUserDto.getUserId());
			fileInfo.setFileMd5(fileMD5);
			fileInfo.setFileName(fileName);
			fileInfo.setFilePath(month + "/" + realFileName);
			fileInfo.setFilePid(filePid);
			fileInfo.setCreateTime(curDate);
			fileInfo.setLastUpdateTime(curDate);
			fileInfo.setFileCategory(fileTypeEnums.getCategory().getCategory());
			fileInfo.setFileType(fileTypeEnums.getType());
			fileInfo.setStatus(FileStatusEnums.TRANSFER.getStatus());
			fileInfo.setFolderType(FileFolderTypeEnums.FILE.getType());
			fileInfo.setDelFlag(FileDelFlagEnums.USING.getFlag());
			this.fileInfoMapper.insert(fileInfo);
			// 更新使用空间
			Long totalSize = redisComponent.getFileTempSize(webUserDto.getUserId(), fileId);
			updateUserSpace(webUserDto, totalSize);
			resultDto.setStatus(UploadStatusEnums.UPLOAD_FINISH.getCode());
			
			// 整个上传事务提交后再调用转码方法
			TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
				@Override
				public void afterCommit() {
					fileInfoService.transferFile(fileInfo.getFileId(), webUserDto);
				}
			});
			
			return resultDto;
			
		} catch (BusinessException e) {
			log.error("文件上传失败", e);
			uploadSuccess = false;
			throw e;
		} catch (Exception e) {
			log.error("文件上传失败", e);
			uploadSuccess = false;
		} finally {
			// 上传失败删除临时目录
			if (!uploadSuccess && tempFileFolder != null) {
				try {
					FileUtils.deleteDirectory(tempFileFolder);
				} catch (IOException e) {
					log.error("删除临时目录失败", e);
				}
			}
		}
		
		return resultDto;
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
			fileName = StringTools.rename(fileName);
		}
		return fileName;
	}
	
	private void updateUserSpace(SessionWebUserDto webUserDto, Long useSpace) {
		Integer count = userInfoMapper.updateUserSpace(webUserDto.getUserId(), useSpace, null);
		// 更新失败，空间不足
		if (count == 0) {
			throw new BusinessException(ResponseCodeEnum.CODE_904);
		}
		// 更新redis
		UserSpaceDto spaceDto = redisComponent.getUserSpaceUse(webUserDto.getUserId());
		spaceDto.setUseSpace(spaceDto.getUseSpace() + useSpace);
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
		FileInfo fileInfo = this.fileInfoMapper.selectByFileIdAndUserId(fileId, webUserDto.getUserId());
		try {
			// 文件为空或非转码状态
			if (fileInfo == null || FileStatusEnums.TRANSFER.getStatus().equals(fileInfo.getStatus())) {
				return;
			}
			// 临时目录
			String tempFolderName = appConfig.getProjectFolder() + Constants.FILE_FOLDER_TEMP;
			String currentUserFolderName = webUserDto.getUserId() + fileId;
			File fileFolder = new File(tempFolderName + currentUserFolderName);
			// 取得文件后缀
			String fileSuffix = StringTools.getFileSuffix(fileInfo.getFileName());
			String month = DateUtil.format(fileInfo.getCreateTime(), DateTimePatternEnum.YYYYMM.getPattern());
			// 目标目录
			String targetFolderName = appConfig.getProjectFolder() + Constants.FILE_FOLDER_FILE;
			File targetFolder = new File(targetFolderName + "/" + month);
			if (!targetFolder.exists()) {
				targetFolder.mkdirs();
			}
			// 真实文件名
			String realFileName = currentUserFolderName + fileSuffix;
			targetFilePath = targetFolder.getPath() + "/" + realFileName;
			
			// 合并文件
			union(fileFolder.getPath(), targetFilePath, fileInfo.getFileName(),true);
			
			// 视频文件切割
			
		}catch (Exception e) {
			log.error("文件转码失败，文件ID：{}，userId：{}", fileId, webUserDto.getUserId(), e);
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
	private void union(String dirPath, String toFilePath, String fileName, Boolean delSource) {
		File dir = new File(dirPath);
		if (!dir.exists()) {
			throw new BusinessException("目录不存在");
		}
		
		File[] fileList = dir.listFiles();
		File targetFile = new File(toFilePath);
		RandomAccessFile writerFile = null;
		try {
			writerFile = new RandomAccessFile(targetFile, "rw");
			byte[] b = new byte[1024 * 10];
			for (int i = 0; i < fileList.length; i++) {
				int len = -1;
				File chunkFile = new File(dirPath + "/" + i);
				RandomAccessFile readFile = null;
				try {
					readFile = new RandomAccessFile(chunkFile, "r");
					while ((len = readFile.read(b)) != -1) {
						writerFile.write(b,0,len);
					}
				} catch (Exception e) {
					log.error("分片合并失败！", e);
					throw new BusinessException("分片合并失败！");
				}finally {
					readFile.close();
				}
			}
		} catch (Exception e) {
			log.error("合并文件：{}失败！",fileName, e);
			throw new BusinessException("合并文件" + fileName + "出错了");
		} finally {
			if (writerFile != null) {
				try {
					writerFile.close();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
			if (delSource && dir.exists()) {
				try {
					FileUtils.deleteDirectory(dir);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}
	}
}