package com.easypan.mapper;


import com.easypan.domain.UserInfo;

/**
* @author 嘻精
* @description 针对表【user_info(用户信息)】的数据库操作Mapper
* @createDate 2023-08-04 16:16:54
* @Entity generator.domain.UserInfo
*/
public interface UserInfoMapper {
    
    /**
     * 按主键删除
     *
     * @param id id
     * @return int
     */
    int deleteByPrimaryKey(Long id);
    
    /**
     * 插入
     *
     * @param record 记录
     * @return int
     */
    int insert(UserInfo record);
    
    /**
     * 插入选择性
     *
     * @param record 记录
     * @return int
     */
    int insertSelective(UserInfo record);
    
    /**
     * 选择通过主键
     *
     * @param id id
     * @return {@link UserInfo}
     */
    UserInfo selectByPrimaryKey(Long id);
    
    /**
     * 更新主键选择性
     *
     * @param record 记录
     * @return int
     */
    int updateByPrimaryKeySelective(UserInfo record);
    
    /**
     * 更新主键
     *
     * @param record 记录
     * @return int
     */
    int updateByPrimaryKey(UserInfo record);

}
