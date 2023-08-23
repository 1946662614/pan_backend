package com.easypan.utils;


import com.easypan.entity.enums.VerifyRegexEnum;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VerifyUtils {
    
    /**
     * 验证正则表达式
     *
     * @param regex 正则表达式
     * @param value 价值
     * @return boolean
     *  这个方法接收一个正则表达式字符串和要验证的字符串。
     *  先检查值是否为空,如果为空则返回false。
     *  然后将正则表达式编译成Pattern,用来匹配传入的字符串。
     *  如果匹配成功,返回true,否则返回false
     */
    public static boolean verify(String regex, String value) {
        if (StringTools.isEmpty(value)) {
            return false;
        }
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(value);
        return matcher.matches();
    }
    
    /**
     * 验证正则
     *
     * @param regex 正则表达式
     * @param value 价值
     * @return boolean
     *  这个方法接收一个VerifyRegexEnum对象和要验证的字符串。
     *  它简单的调用了第一个verify方法,传入VerifyRegexEnum中的正则表达式字符串
     */
    public static boolean verify(VerifyRegexEnum regex, String value) {
        return verify(regex.getRegex(), value);
    }
    
}

