package com.easypan.controller;

import com.easypan.entity.Constants;
import com.easypan.entity.dto.CreateImageCode;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * @ClassName AccountController
 * @Description TODO
 * @Author Henry
 * @Date 2023/8/10 15:09
 * @Version 1.0
 */
@RestController("userInfoController")
public class AccountController {
	
	/**
	 * 生成验证码图片并写入response输出流。
	 *
	 * @param response HttpServletResponse对象,用于设置响应头信息并输出图片数据
	 * @param session HttpSession对象,用于保存生成的验证码字符串
	 * @param type 验证码类型,null或0表示保存到CHECK_CODE_KEY,其他值保存到CHECK_CODE_KEY_EMAIL
	 * @throws IOException 如果输出图片数据时发生IO异常
	 *
	 * 该方法首先创建一个验证码图片生成器对象CreateImageCode,设置图片大小、字符数量等参数。
	 * 然后设置response的头信息,指定不缓存响应内容,并设置contentType为图片格式。
	 * 接着调用验证码生成器的getCode()方法生成验证码字符串,根据type参数保存到不同的session属性中。
	 * 最后调用生成器的write()方法将验证码图片数据写入到response的输出流中。
	 * 这样就可以生成验证码图片并输出到浏览器,同时验证码的值被保存到session中供后续验证使用。
	 */
	@RequestMapping("/checkCode")
	public void checkCode(HttpServletResponse response, HttpSession session, Integer type) throws IOException {
		CreateImageCode VCode = new CreateImageCode(130, 38, 5, 10);
		response.setHeader("Pragma", "No-cache");//设置响应头信息，告诉浏览器不要缓存此内容
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expire", 0);
		response.setContentType("image/jpeg");
		String code = VCode.getCode();
		if (type == null || type == 0) {
			session.setAttribute(Constants.CHECK_CODE_KEY, code);
		} else {
			session.setAttribute(Constants.CHECK_CODE_KEY_EMAIL, code);
		}
		VCode.write(response.getOutputStream());
	}
	
	
	
 
 
	























	
 
}
