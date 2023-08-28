package com.easypan.service;

import com.easypan.entity.dto.SessionWebUserDto;
import com.easypan.entity.dto.UploadResultDto;
import com.easypan.entity.po.FileInfo;
import com.easypan.entity.query.FileInfoQuery;
import com.easypan.entity.vo.PaginationResultVO;
import com.sun.org.apache.xpath.internal.operations.Mult;
import org.apache.ibatis.annotations.Param;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;


/**
 * 文件信息 业务接口
 */
public interface FileInfoService {

    /**
     * 根据条件查询列表
     */
    List<FileInfo> findListByParam(FileInfoQuery param);

    /**
     * 根据条件查询列表
     */
    Integer findCountByParam(FileInfoQuery param);

    /**
     * 分页查询
     */
    PaginationResultVO<FileInfo> findListByPage(FileInfoQuery param);

    /**
     * 新增
     */
    Integer add(FileInfo bean);

    /**
     * 批量新增
     */
    Integer addBatch(List<FileInfo> listBean);

    /**
     * 批量新增/修改
     */
    Integer addOrUpdateBatch(List<FileInfo> listBean);

    /**
     * 根据FileIdAndUserId查询对象
     */
    FileInfo getFileInfoByFileIdAndUserId(String fileId, String userId);


    /**
     * 根据FileIdAndUserId修改
     */
    Integer updateFileInfoByFileIdAndUserId(FileInfo bean, String fileId, String userId);


    /**
     * 根据FileIdAndUserId删除
     */
    Integer deleteFileInfoByFileIdAndUserId(String fileId, String userId);
    
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
    UploadResultDto uploadFile (SessionWebUserDto webUserDto, String fileId, MultipartFile file, String fileName, String filePid,
                                String fileMD5, Integer chunkIndex, Integer chunks);

    
}