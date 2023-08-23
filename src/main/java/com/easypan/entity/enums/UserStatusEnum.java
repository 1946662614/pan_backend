package com.easypan.entity.enums;

/**
 * 用户状态枚举
 *
 * @ClassName UserStatusEnum
 * @Description
 * @Author Henry
 * @Date 2023/8/23 15:14
 */

public enum UserStatusEnum {
	
	DISABLE(0, "禁用"),
	ENABLE(1, "启用");
	
	
	private Integer status;
	private String desc;
	
	UserStatusEnum(Integer status, String desc) {
		this.status = status;
		this.desc = desc;
	}
	
	public static UserStatusEnum getByStatus(Integer status) {
		for (UserStatusEnum item : UserStatusEnum.values()) {
			if (item.getStatus().equals(status)) {
				return item;
			}
		}
		return null;
	}
	
	public Integer getStatus() {
		return status;
	}
	
	public String getDesc() {
		return desc;
	}
	
	public void setDesc(String desc) {
		this.desc = desc;
	}
}
