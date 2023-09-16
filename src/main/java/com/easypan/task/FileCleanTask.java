package com.easypan.task;

import com.easypan.entity.enums.FileDelFlagEnums;
import com.easypan.entity.po.FileInfo;
import com.easypan.entity.query.FileInfoQuery;
import com.easypan.service.FileInfoService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @ClassName FileCleanTask
 * @Description
 * @Author Henry
 * @Date 2023/9/16 17:49
 */
@Component
public class FileCleanTask {
	@Resource
	private FileInfoService fileInfoService;
	
	@Scheduled(fixedDelay = 1000 * 60 * 3)
	public void excute() {
		FileInfoQuery query = new FileInfoQuery();
		query.setDelFlag(FileDelFlagEnums.RECYCLE.getFlag());
		query.setQueryExpire(true);
		List<FileInfo> fileInfoList = fileInfoService.findListByParam(query);
		Map<String, List<FileInfo>> fileInfoMap = fileInfoList.stream().collect(Collectors.groupingBy(FileInfo::getUserId));
		for (Map.Entry<String , List<FileInfo>> entry : fileInfoMap.entrySet()) {
			List<String> fileIds = entry.getValue().stream().map(p -> p.getFileId()).collect(Collectors.toList());
			fileInfoService.delFileBatch(entry.getKey(), String.join(",", fileIds), false);
		}
	}
}
