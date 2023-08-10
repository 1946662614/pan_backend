package com.easypan.mappers;


import com.easypan.entity.po.EmailCode;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* @author 嘻精
* @description 针对表【email_code(邮箱验证码)】的数据库操作Mapper
* @createDate 2023-08-10 15:56:55
* @Entity generator.domain.EmailCode
*/
public interface EmailCodeMapper<T, P> extends BaseMapper<T, P> {

    int deleteByPrimaryKey(Long id);


    int insertSelective(EmailCode record);

    EmailCode selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(EmailCode record);

    int updateByPrimaryKey(EmailCode record);
    
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
    
    /**
     * 禁用邮件代码
     *
     * @param email 电子邮件
     */

    void disableEmailCode(@Param("email") String email);
}
