package com.easypan.mappers;


import com.easypan.entity.po.UserInfo;

import java.util.List;

/**
* @author 嘻精
* @description 针对表【user_info(用户信息)】的数据库操作Mapper
* @createDate 2023-08-04 16:16:54
* @Entity generator.domain.UserInfo
*/
public interface UserInfoMapper<T, P> extends BaseMapper<T, P> {
    
    /**
     * 按主键删除
     *
     * @param id id
     * @return int
     */
    int deleteByPrimaryKey(Long id);
    
    
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
    
    /**
     * 根据邮箱查询
     * @param email
     * @return
     */
    UserInfo selectByEmail(String email);
    
    /**
     * insert:(插入). <br/>
     *
     * @param t
     */
    @Override
    default Integer insert(T t) {
        return null;
    }
    
    /**
     * insertOrUpdate:(插入或者更新). <br/>
     *
     * @param t
     */
    @Override
    default Integer insertOrUpdate(T t) {
        return null;
    }
    
    /**
     * insertBatch:(批量插入). <br/>
     *
     * @param list
     */
    @Override
    default Integer insertBatch(List<T> list) {
        return null;
    }
    
    /**
     * insertOrUpdateBatch:(批量插入或更新). <br/>
     *
     * @param list
     */
    @Override
    default Integer insertOrUpdateBatch(List<T> list) {
        return null;
    }
    
    /**
     * selectList:(根据参数查询集合). <br/>
     *
     * @param p
     */
    @Override
    default List<T> selectList(P p) {
        return null;
    }
    
    /**
     * selectCount:(根据集合查询数量). <br/>
     *
     * @param p
     */
    @Override
    default Integer selectCount(P p) {
        return null;
    }
}
