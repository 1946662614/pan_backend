package com.easypan.aspect;

import com.easypan.exception.BusinessException;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Component("globalOperationAspect")
public class GlobalOperationAspect {
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
		Object target = point.getTarget();
	}
}