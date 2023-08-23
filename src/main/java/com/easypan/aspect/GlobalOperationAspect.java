package com.easypan.aspect;

import com.easypan.annotation.GlobalInterceptor;
import com.easypan.annotation.VerifyParam;
import com.easypan.entity.config.AppConfig;
import com.easypan.entity.constants.Constants;
import com.easypan.entity.dto.SessionWebUserDto;
import com.easypan.entity.enums.ResponseCodeEnum;
import com.easypan.entity.po.UserInfo;
import com.easypan.exception.BusinessException;
import com.easypan.service.UserInfoService;
import com.easypan.utils.StringTools;
import com.easypan.utils.VerifyUtils;
import io.netty.util.collection.ByteCollections;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.binding.MapperMethod;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.mvc.condition.RequestConditionHolder;

import javax.annotation.Resource;
import javax.security.auth.login.AppConfigurationEntry;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;
import java.util.function.ObjLongConsumer;

@Aspect
@Component("globalOperationAspect")
@Slf4j
public class GlobalOperationAspect {
	
	private static final String TYPE_STRING = "java.lang.String";
	private static final String TYPE_INTEGER = "java.lang.Integer";
	private static final String TYPE_LONG = "java.lang.Long";

	// 定义切点
	/**
	 * 请求拦截
	 */
	@Pointcut("@annotation(com.easypan.annotation.GlobalInterceptor)")
	private void requestInterceptor() {
	
	}
	
	// 事件通知
	/**
	 * @param point
	 * @throws BusinessException
	 *  所有添加了GlobalInterceptor注解的方法执行前都会进入此方法
	 */
	@Before("requestInterceptor()")
	public void interceptorDo(JoinPoint point) throws BusinessException {
		try {
			Object target = point.getTarget();
			Object[] arguments = point.getArgs();
			String methodName = point.getSignature().getName();
			Class<?>[] parameterTypes = ((MethodSignature) point.getSignature()).getMethod().getParameterTypes();
			Method method = target.getClass().getMethod(methodName, parameterTypes);
			GlobalInterceptor interceptor = method.getAnnotation(GlobalInterceptor.class);
			if (interceptor == null) {
				return;
			}
			/**
			 * TODO 校验登录
			 */
			
			/**
			 * 校验参数
			 */
			if (interceptor.checkParams()){
				validateParams(method,arguments);
			}
		} catch (BusinessException e) {
			log.error("全局拦截器异常", e);
		} catch (Exception e) {
			log.error("全局拦截器异常", e);
			throw new BusinessException(ResponseCodeEnum.CODE_500);
		} catch (Throwable e) {
			log.error("全局拦截器异常", e);
			throw new BusinessException(ResponseCodeEnum.CODE_500);
		}
	}
	
	private void validateParams(Method m, Object[] arguments) throws BusinessException {
		Parameter[] parameters = m.getParameters();
		for (int i = 0; i < parameters.length; i++) {
			Parameter parameter = parameters[i];
			Object value = arguments[i];
			VerifyParam verifyParam = parameter.getAnnotation(VerifyParam.class);
			if (verifyParam == null) {
				continue;
			}
			//基本数据类型
			if (TYPE_STRING.equals(parameter.getParameterizedType().getTypeName())
						|| TYPE_LONG.equals(parameter.getParameterizedType().getTypeName())
						|| TYPE_INTEGER.equals(parameter.getParameterizedType().getTypeName())) {
				checkValue(value, verifyParam);
				//如果传递的是对象
			} else {
				checkObjValue(parameter, value);
			}
		}
	}
	
	private void checkObjValue(Parameter parameter, Object value) {
		try {
			String typeName = parameter.getParameterizedType().getTypeName();
			Class<?> classz = Class.forName(typeName);
			Field[] fields = classz.getDeclaredFields();
			for (Field field : fields) {
				VerifyParam fieldVerifyParam = field.getAnnotation(VerifyParam.class);
				if (fieldVerifyParam == null) {
					continue;
				}
				field.setAccessible(true);
				Object resultValue = field.get(value);
				checkValue(resultValue, fieldVerifyParam);
			}
		} catch (BusinessException e) {
			log.error("参数校验失败", e);
		} catch (Exception  e) {
			log.error("参数校验失败", e);
			throw new BusinessException(ResponseCodeEnum.CODE_600);
		}
	}
	
	/**
	 * 校验参数
	 * @param value
	 * @param verifyParam
	 */
	private void checkValue(Object value, VerifyParam verifyParam) {
		Boolean isEmpty = value == null || StringTools.isEmpty(value.toString());
		Integer length = value == null ? 0 : value.toString().length();
		
		// 校验为空
		if (isEmpty && verifyParam.required()) {
			throw new BusinessException(ResponseCodeEnum.CODE_600);
		}
		
		// 校验长度
		if (!isEmpty && (verifyParam.max() != -1 && verifyParam.max() < length
								 || verifyParam.min() != -1 && verifyParam.min() > length)) {
			throw new BusinessException(ResponseCodeEnum.CODE_600);
		}
		
		// 校验正则
		if (!isEmpty && !StringTools.isEmpty(verifyParam.regex().getRegex())
					&& !VerifyUtils.verify(verifyParam.regex(),String.valueOf(value))) {
			throw new BusinessException(ResponseCodeEnum.CODE_600);
		}
	}
}