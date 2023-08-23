package com.easypan.utils;


import com.easypan.entity.constants.Constants;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.RandomStringUtils;

public class StringTools {
    
    /**
     * 得到随机字符串
     *
     * @param count 数
     * @return {@link String}
     */
    public static final String getRandomString(Integer count) {
        return RandomStringUtils.random(count, true, true);
    }
    
    /**
     * 获取随机数
     *
     * @param count 数
     * @return {@link String}
     */
    public static final String getRandomNumber(Integer count) {
        return RandomStringUtils.random(count, false, true);
    }
    
    /**
     * String是否为空
     *
     * @param str
     * @return boolean
     */
    public static boolean isEmpty(String str) {
        if (str == null || "".equals(str) || "null".equals(str) || "\u0000".equals(str)) {
            return true;
        } else if ("".equals(str.trim())) {
            return true;
        }
        return false;
    }
}
