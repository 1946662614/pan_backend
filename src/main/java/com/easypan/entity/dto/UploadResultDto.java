package com.easypan.entity.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.io.Serializable;

/**
 * @ClassName UploadResultDto
 * @Description
 * @Author Henry
 * @Date 2023/8/28 15:40
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class UploadResultDto implements Serializable {
 
	private String fileId;
	private String status;

}
