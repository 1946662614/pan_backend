package com.easypan.controller;

import com.easypan.annotation.GlobalInterceptor;
import com.easypan.annotation.VerifyParam;
import com.easypan.component.RedisComponent;
import com.easypan.entity.config.AppConfig;
import com.easypan.entity.constants.Constants;
import com.easypan.entity.dto.CreateImageCode;
import com.easypan.entity.dto.SessionWebUserDto;
import com.easypan.entity.dto.UserSpaceDto;
import com.easypan.entity.enums.VerifyRegexEnum;
import com.easypan.entity.po.UserInfo;
import com.easypan.entity.vo.ResponseVO;
import com.easypan.exception.BusinessException;
import com.easypan.service.EmailCodeService;
import com.easypan.service.UserInfoService;
import com.easypan.utils.StringTools;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import static com.easypan.entity.constants.Constants.*;


/**
 * @ClassName AccountController
 * @Author Henry
 * @Date 2023/8/10 15:09
 * @Version 1.0
 */
@RestController("userInfoController")
@Slf4j
public class AccountController extends ABaseController{
	
	private static final String CONTENT_TYPE = "Content-Type";
	private static final String CONTENT_TYPE_VALUE = "application/json;charset=UTF-8";
	@Resource
	private EmailCodeService emailCodeService;
	
	@Resource
	private UserInfoService userInfoService;
	
	@Resource
	private AppConfig appConfig;
	
	@Resource
	private RedisComponent redisComponent;
	/**
	 * 校验码
	 * 生成验证码图片并写入response输出流。
	 *
	 * @param response HttpServletResponse对象,用于设置响应头信息并输出图片数据
	 * @param session  HttpSession对象,用于保存生成的验证码字符串
	 * @param type     验证码类型,null或0表示保存到CHECK_CODE_KEY,其他值保存到CHECK_CODE_KEY_EMAIL
	 * @throws IOException 如果输出图片数据时发生IO异常
	 *                     <p>
	 *                     该方法首先创建一个验证码图片生成器对象CreateImageCode,设置图片大小、字符数量等参数。
	 *                     然后设置response的头信息,指定不缓存响应内容,并设置contentType为图片格式。
	 *                     接着调用验证码生成器的getCode()方法生成验证码字符串,根据type参数保存到不同的session属性中。
	 *                     最后调用生成器的write()方法将验证码图片数据写入到response的输出流中。
	 *                     这样就可以生成验证码图片并输出到浏览器,同时验证码的值被保存到session中供后续验证使用。
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
	
	/**
	 * 发送电子邮件代码
	 *
	 * @param email     电子邮件
	 * @param session   会话
	 * @param checkCode 校验码
	 * @param type      类型
	 * @return {@link ResponseVO}
	 */
	@RequestMapping("/sendEmailCode")
	@GlobalInterceptor(checkParams = true)
	public ResponseVO sendEmailCode(@VerifyParam(required = true, regex = VerifyRegexEnum.EMAIL, max = 150)String email,
									@VerifyParam(required = true)HttpSession session, String checkCode,
									@VerifyParam(required = true)Integer type) {
		try {
			if (!checkCode.equalsIgnoreCase((String) session.getAttribute(Constants.CHECK_CODE_KEY_EMAIL))) {
				throw new BusinessException("图片验证码不正确");
			}
			emailCodeService.sendEmailCode(email, type);
			return getSuccessResponseVO(null);
		} finally {
			session.removeAttribute(Constants.CHECK_CODE_KEY_EMAIL);
		}
	}
	
	
	/**
	 * 注册
	 *
	 * @param email     电子邮件
	 * @param nickName  昵称
	 * @param password  通过单词
	 * @param checkCode 校验码
	 * @param emailCode 电子邮件代码
	 * @return {@link ResponseVO}
	 */
	@RequestMapping("/register")
	@GlobalInterceptor(checkParams = true)
	public ResponseVO register(@VerifyParam(required = true, regex = VerifyRegexEnum.EMAIL, max = 150)String email,
							   @VerifyParam(required = true) String nickName,
							   @VerifyParam(required = true, regex = VerifyRegexEnum.PASSWORD, min = 8,max = 18) String password,
							   @VerifyParam(required = true) String checkCode,
							   @VerifyParam(required = true) String emailCode,
							   HttpSession session) {
		try {
			if (!checkCode.equalsIgnoreCase((String) session.getAttribute(Constants.CHECK_CODE_KEY))) {
				throw new BusinessException("图片验证码不正确");
			}
			userInfoService.register(email, nickName, password, emailCode);
			return getSuccessResponseVO(null);
		} finally {
			session.removeAttribute(Constants.CHECK_CODE_KEY);
		}
	}
	
	
	/**
	 * 登录
	 *
	 * @param email     电子邮件
	 * @param password  密码
	 * @param checkCode 校验码
	 * @param session   会话
	 * @return {@link ResponseVO}
	 */
	@RequestMapping("/login")
	@GlobalInterceptor(checkParams = true)
	public ResponseVO login(@VerifyParam(required = true)String email,
							   @VerifyParam(required = true) String password,
							   @VerifyParam(required = true) String checkCode,
							   HttpSession session) {
		try {
			if (!checkCode.equalsIgnoreCase((String) session.getAttribute(Constants.CHECK_CODE_KEY))) {
				throw new BusinessException("图片验证码不正确");
			}
			SessionWebUserDto sessionWebUserDto = userInfoService.login(email, password);
			session.setAttribute(Constants.SESSION_KEY,sessionWebUserDto);
			return getSuccessResponseVO(sessionWebUserDto);
		} finally {
			session.removeAttribute(Constants.CHECK_CODE_KEY);
		}
	}
	
	/**
	 * 注册
	 * @param email
	 * @param password
	 * @param checkCode
	 * @param emailCode
	 * @param session
	 * @return
	 */
	@RequestMapping("/resetPwd")
	@GlobalInterceptor(checkParams = true)
	public ResponseVO resetPwd(@VerifyParam(required = true, regex = VerifyRegexEnum.EMAIL, max = 150)String email,
							   @VerifyParam(required = true, regex = VerifyRegexEnum.PASSWORD, min = 8,max = 18) String password,
							   @VerifyParam(required = true) String checkCode,
							   @VerifyParam(required = true) String emailCode,
							   HttpSession session) {
		try {
			if (!checkCode.equalsIgnoreCase((String) session.getAttribute(Constants.CHECK_CODE_KEY))) {
				throw new BusinessException("图片验证码不正确");
			}
			userInfoService.resetPwd(email,emailCode,password);
			return getSuccessResponseVO(null);
		} finally {
			session.removeAttribute(Constants.CHECK_CODE_KEY);
		}
	}
	
	/**
	 * 获取用户头像
	 * @param response
	 * @param userId
	 */
	@RequestMapping("/getAvatar/{userId}")
	@GlobalInterceptor(checkParams = true)
	public void getAvatar(HttpServletResponse response, @VerifyParam(required = true)@PathVariable("userId")String userId) {
		
			String avatarFolderName = Constants.FILE_FOLDER_FILE + Constants.FILE_FOLDER_AVATAR_NAME;
			File folder = new File(appConfig.getProjectFolder() + avatarFolderName);
			// 判断是否存在此路径
			if (!folder.exists()) {
				folder.mkdirs();
			}
			// 读取头像
			String avatarPath = appConfig.getProjectFolder() + avatarFolderName + userId + AVATAR_SUFFIX;
			File file = new File(avatarPath);
			if (!file.exists()) {
				// 不存在头像，读取默认头像
				if (!new File(appConfig.getProjectFolder() + avatarFolderName + AVATAR_DEFAULT).exists()) {
					// 没有则返回提示
					printNoDefaultImage(response);
					return;
				}
				// 有则读取默认头像
				avatarPath = appConfig.getProjectFolder() + avatarFolderName + AVATAR_DEFAULT;
			}
			response.setContentType("image/jpg");
			// 读取文件
			readFile(response, avatarPath);
			
		
	}
	
	private void printNoDefaultImage(HttpServletResponse response) {
		response.setHeader(CONTENT_TYPE, CONTENT_TYPE_VALUE);
		response.setStatus(HttpStatus.OK.value());
		PrintWriter writer = null;
		try {
			writer = response.getWriter();
			writer.print("请在头像目录下放置默认头像default_avatar.jpg");
			writer.close();
		} catch (Exception e) {
			log.error("输出无默认图失败", e);
		} finally {
			writer.close();
		}
	}
	
	/**
	 * 获取用户信息
	 * @param session
	 * @return
	 */
	@RequestMapping("/getUserInfo")
	@GlobalInterceptor
	public ResponseVO getUserInfo(HttpSession session) {
		SessionWebUserDto sessionWebUserDto = getUserInfoSession(session);
		return getSuccessResponseVO(sessionWebUserDto);
	}
	
	/**
	 * 获取用户使用空间
	 *
	 * @param session 会话
	 * @return {@link ResponseVO}
	 */
	@RequestMapping("/getUseSpace")
	@GlobalInterceptor
	public ResponseVO getUseSpace(HttpSession session) {
		SessionWebUserDto sessionWebUserDto = getUserInfoSession(session);
		UserSpaceDto userSpaceUse = redisComponent.getUserSpaceUse(sessionWebUserDto.getUserId());
		return getSuccessResponseVO(userSpaceUse);
	}
	
	/**
	 * 注销
	 *
	 * @param session 会话
	 * @return {@link ResponseVO}
	 */
	@RequestMapping("/logout")
	@GlobalInterceptor
	public ResponseVO logout(HttpSession session) {
		session.invalidate();
		return getSuccessResponseVO(null);
	}
	
	
	/**
	 * 更新用户头像
	 *
	 * @param session 会话
	 * @param avatar  头像
	 * @return {@link ResponseVO}
	 */
	@RequestMapping("/updateUserAvatar")
	@GlobalInterceptor(checkParams = true)
	public ResponseVO updateUserAvatar(HttpSession session, MultipartFile avatar) {
		SessionWebUserDto webUserDto = getUserInfoSession(session);
		String baseFolder = appConfig.getProjectFolder() + FILE_FOLDER_FILE;
		File targetFileFolder = new File(baseFolder + FILE_FOLDER_AVATAR_NAME);
		if (!targetFileFolder.exists()) {
			targetFileFolder.mkdirs();
		}
		File targetFile = new File(targetFileFolder.getPath() + "/" + webUserDto.getUserId() + AVATAR_SUFFIX);
		try {
			avatar.transferTo(targetFile);
		} catch (Exception e) {
			log.error("上传头像失败", e);
		}
		
		UserInfo userInfo = new UserInfo();
		userInfo.setQqAvatar("");
		userInfoService.updateUserInfoByUserId(userInfo, webUserDto.getUserId());
		webUserDto.setAvatar(null);
		session.setAttribute(SESSION_KEY, webUserDto);
		return getSuccessResponseVO(null);
	}
	
	/**
	 * 更新密码
	 *
	 * @param session  会话
	 * @param password 密码
	 * @return {@link ResponseVO}
	 */
	@RequestMapping("/updatePassword")
	@GlobalInterceptor(checkParams = true)
	public ResponseVO updatePassword(HttpSession session,
									 @VerifyParam(required = true, regex = VerifyRegexEnum.PASSWORD, min = 8, max = 18) String password) {
		SessionWebUserDto sessionWebUserDto = getUserInfoSession(session);
		UserInfo userInfo = new UserInfo();
		userInfo.setPassword(StringTools.encodeByMd5(password));
		userInfoService.updateUserInfoByUserId(userInfo, sessionWebUserDto.getUserId());
		return getSuccessResponseVO(null);
	}









	
 
}
