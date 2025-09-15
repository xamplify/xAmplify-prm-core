package com.xtremand.aws;

import java.io.Serializable;

import org.springframework.web.multipart.MultipartFile;

public class AWSInputDTO implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4438126481741322245L;
	
	private boolean generateThumbnailWithFile;
	
	private transient MultipartFile originalFie;
	
	private transient MultipartFile thumbnailFile;
	
	private Integer userId;
	
	private Integer companyId;
	
	private String filePathSuffix;
	
	private String fileType;

	public boolean isGenerateThumbnailWithFile() {
		return generateThumbnailWithFile;
	}

	public void setGenerateThumbnailWithFile(boolean generateThumbnailWithFile) {
		this.generateThumbnailWithFile = generateThumbnailWithFile;
	}

	public MultipartFile getOriginalFie() {
		return originalFie;
	}

	public void setOriginalFie(MultipartFile originalFie) {
		this.originalFie = originalFie;
	}

	public MultipartFile getThumbnailFile() {
		return thumbnailFile;
	}

	public void setThumbnailFile(MultipartFile thumbnailFile) {
		this.thumbnailFile = thumbnailFile;
	}

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public String getFilePathSuffix() {
		return filePathSuffix;
	}

	public void setFilePathSuffix(String filePathSuffix) {
		this.filePathSuffix = filePathSuffix;
	}

	public String getFileType() {
		return fileType;
	}

	public void setFileType(String fileType) {
		this.fileType = fileType;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public Integer getCompanyId() {
		return companyId;
	}

	public void setCompanyId(Integer companyId) {
		this.companyId = companyId;
	}

}
