package com.easypan.component;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName RedisUtils
 * @Description Redis工具类
 * @Author Henry
 * @Date 2023/8/11 14:53
 * @Version 1.0
 */
@Slf4j
@Component("redisUtils")
public class RedisUtils<V> {
	@Resource
	private RedisTemplate<String, V> redisTemplate;
	
	
	
	/**
	 * 获取key
	 *
	 * @param key 关键
	 * @return {@link V}
	 */
	public V get(String key) {
		return key == null ? null : redisTemplate.opsForValue().get(key);
	}
	
	/**
	 * 设置值
	 *
	 * @param key   key
	 * @param value 值
	 * @return boolean
	 */
	public boolean set(String key, V value) {
		try {
			redisTemplate.opsForValue().set(key, value);
			return true;
		} catch (Exception e) {
			log.error("设置redisKey:{},value:{} 失败", key, value);
			return false;
		}
	}
	
	/**
	 * 设置过期时间
	 *
	 * @param key   关键
	 * @param value 价值
	 * @param time  时间
	 * @return boolean
	 */
	public boolean setex(String key, V value, Long time) {
		try {
			if (time > 0) {
				redisTemplate.opsForValue().set(key, value, time, TimeUnit.SECONDS);
			} else {
				set(key, value);
			}
			return true;
		} catch (Exception e) {
			log.error("设置redisKey:{},value:{} 失败", key, value);
			return false;
		}
	}
	
}
