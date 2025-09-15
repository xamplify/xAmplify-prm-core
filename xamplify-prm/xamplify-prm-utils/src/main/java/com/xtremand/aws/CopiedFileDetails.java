package com.xtremand.aws;

import java.io.File;
import java.io.Serializable;

public class CopiedFileDetails implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 3727546488240888536L;

	private String completeName;
	
	private String completeThumbnailName;
	
	private File file;
	
	private String folderPath;
	
	private String copiedImageFilePath;
	
	private String thumbnailFilePath;
	
	private String updatedFileName;
	
	/**XNFR-735**/
	private boolean isFromEmailActivity;
	
	private boolean isFromDomainMediaResource;

	public String getCompleteName() {
		return completeName;
	}

	public void setCompleteName(String completeName) {
		this.completeName = completeName;
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public String getFolderPath() {
		return folderPath;
	}

	public void setFolderPath(String folderPath) {
		this.folderPath = folderPath;
	}

	public String getCopiedImageFilePath() {
		return copiedImageFilePath;
	}

	public void setCopiedImageFilePath(String copiedImageFilePath) {
		this.copiedImageFilePath = copiedImageFilePath;
	}

	public String getUpdatedFileName() {
		return updatedFileName;
	}

	public void setUpdatedFileName(String updatedFileName) {
		this.updatedFileName = updatedFileName;
	}

	public String getCompleteThumbnailName() {
		return completeThumbnailName;
	}

	public void setCompleteThumbnailName(String completeThumbnailName) {
		this.completeThumbnailName = completeThumbnailName;
	}

	public String getThumbnailFilePath() {
		return thumbnailFilePath;
	}

	public void setThumbnailFilePath(String thumbnailFilePath) {
		this.thumbnailFilePath = thumbnailFilePath;
	}
	
	/**XNFR-735**/
	public void setIsFromEmailActivity(boolean isFromEmailActivity) {
		this.isFromEmailActivity = isFromEmailActivity;
	}
	
	public boolean isFromEmailActivity() {
		return isFromEmailActivity;
	}
	
	/** XNFR-780 **/
	public boolean isFromDomainMediaResource() {
		return isFromDomainMediaResource;
	}

	public void setFromDomainMediaResource(boolean isFromDomainMediaResource) {
		this.isFromDomainMediaResource = isFromDomainMediaResource;
	}
	
}
