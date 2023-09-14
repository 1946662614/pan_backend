package com.easypan.mappers;

import com.easypan.entity.po.FileInfo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 文件信息 数据库操作接口
 */
public interface FileInfoMapper<T, P> extends BaseMapper<T, P> {

    /**
     * 根据FileIdAndUserId更新
     */
    Integer updateByFileIdAndUserId(@Param("bean") T t, @Param("fileId") String fileId, @Param("userId") String userId);


    /**
     * 根据FileIdAndUserId删除
     */
    Integer deleteByFileIdAndUserId(@Param("fileId") String fileId, @Param("userId") String userId);


    /**
     * 根据FileIdAndUserId获取对象
     */
    T selectByFileIdAndUserId(@Param("fileId") String fileId, @Param("userId") String userId);


    void updateFileStatusWithOldStatus(@Param("fileId") String fileId, @Param("userId") String userId, @Param("bean") T t,
                                       @Param("oldStatus") Integer oldStatus);
    
    /**
     * 批量更新文件del标志
     *
     * @param fileInfo    文件简介
     * @param userId      用户id
     * @param filePidList 文件pid列表
     * @param fileIdList  文件id列表
     * @param oldDelFlag  旧德尔旗
     */
    void updateFileDelFlagBatch(@Param("bean") FileInfo fileInfo,
                                @Param("userId") String userId,
                                @Param("filePidList") List<String> filePidList,
                                @Param("fileIdList") List<String> fileIdList,
                                @Param("oldDelFlag") Integer oldDelFlag);


    void delFileBatch(@Param("userId") String userId,
                      @Param("filePidList") List<String> filePidList,
                      @Param("fileIdList") List<String> fileIdList,
                      @Param("oldDelFlag") Integer oldDelFlag);
    
    /**
     * 查询用户使用空间
     *
     * @param userId 用户id
     * @return {@link Long}
     */
    Long selectUseSpace(@Param("userId") String userId);

    void deleteFileByUserId(@Param("userId") String userId);
    
    
}
