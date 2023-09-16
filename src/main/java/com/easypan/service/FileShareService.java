package com.easypan.service;

import com.easypan.annotation.VerifyParam;
import com.easypan.entity.dto.SessionShareDto;
import com.easypan.entity.po.FileShare;
import com.easypan.entity.query.FileShareQuery;
import com.easypan.entity.vo.PaginationResultVO;

import java.util.List;


/**
 * 分享信息 业务接口
 */
public interface FileShareService {

    /**
     * 根据条件查询列表
     */
    List<FileShare> findListByParam(FileShareQuery param);

    /**
     * 根据条件查询列表
     */
    Integer findCountByParam(FileShareQuery param);

    /**
     * 分页查询
     */
    PaginationResultVO<FileShare> findListByPage(FileShareQuery param);

    /**
     * 新增
     */
    Integer add(FileShare bean);

    /**
     * 批量新增
     */
    Integer addBatch(List<FileShare> listBean);

    /**
     * 批量新增/修改
     */
    Integer addOrUpdateBatch(List<FileShare> listBean);

    /**
     * 根据ShareId查询对象
     */
    FileShare getFileShareByShareId(String shareId);


    /**
     * 根据ShareId修改
     */
    Integer updateFileShareByShareId(FileShare bean, String shareId);


    /**
     * 根据ShareId删除
     */
    Integer deleteFileShareByShareId(String shareId);
    
    
    /**
     * 保存文件分享
     *
     * @param fileShare 文件共享
     */
    void saveShare(FileShare fileShare);
    
    /**
     * 批量删除文件共享
     *
     * @param shareIdArray 共享id数组
     * @param userId       用户id
     */
    void deleteFileShareBatch(String[] shareIdArray, String userId);
    
    /**
     * 检查共享代码
     *
     * @param shareId 共享id
     * @param code    密码
     * @return {@link SessionShareDto}
     */
    SessionShareDto checkShareCode(String shareId, String code);
}