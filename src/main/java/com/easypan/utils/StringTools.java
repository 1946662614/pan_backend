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
    
    /**
     * 通过md5编码加密
     *
     * @param originString 源字符串
     * @return {@link String}
     */
    public static String encodeByMd5( String originString) {
        return isEmpty(originString) ? null : DigestUtils.md5Hex(originString);
    }
	
	public static boolean pathIsOk(String filePath) {
        if (isEmpty(filePath)) {
            return false;
        }
        if (filePath.contains("../") ||  filePath.contains("..\\")) {
            return false;
        }
        return true;
	}
    
    public static String rename(String fileName) {
        String fileNameReal = getFileNameNoSuffix(fileName);
        String suffix = getFileSuffix(fileName);
        return fileNameReal + "_" + getRandomNumber(Constants.LENGTH_5) + suffix;
    }
    
    public static String getFileSuffix(String fileName) {
        Integer index = fileName.lastIndexOf(".");
        if (index == -1) {
            return "";
        }
        String suffix = fileName.substring(index);
        return suffix;
    }
    
    public static String getFileNameNoSuffix(String fileName) {
        Integer index = fileName.lastIndexOf(".");
        if (index == -1) {
            return fileName;
        }
        fileName = fileName.substring(0, index);
        return fileName;
    }
}
