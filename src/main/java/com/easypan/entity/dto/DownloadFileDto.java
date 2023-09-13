package com.easypan.entity.dto;

import lombok.Data;

/**
 * @ClassName DownloadFileDto
 * @Description
 * @Author Henry
 * @Date 2023/9/13 14:13
 */
@Data
public class DownloadFileDto {
	private String downloadCode;
	private String fileId;
	private String fileName;
	private String filePath;
}
