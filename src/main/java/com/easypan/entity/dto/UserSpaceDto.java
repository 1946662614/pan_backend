package com.easypan.entity.dto;

import lombok.Data;
import org.springframework.context.annotation.Primary;

import java.io.Serializable;

/**
 * @ClassName UserSpaceDto
 * @Description
 * @Author Henry
 * @Date 2023/8/23 15:47
 */
@Data
public class UserSpaceDto implements Serializable {
	private Long userSpace;
	private Long totalSpace;
}
