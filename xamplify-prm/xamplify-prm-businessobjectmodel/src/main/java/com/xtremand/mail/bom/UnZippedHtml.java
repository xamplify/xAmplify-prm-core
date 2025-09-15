package com.xtremand.mail.bom;

import java.io.Serializable;
import java.util.List;

public class UnZippedHtml implements Serializable {
	

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private boolean multipleHtmlsFound;
	
	private List<String> images;
	
	private String htmlFilePath;
	
	private List<String> cssPath;

	public boolean isMultipleHtmlsFound() {
		return multipleHtmlsFound;
	}

	public void setMultipleHtmlsFound(boolean multipleHtmlsFound) {
		this.multipleHtmlsFound = multipleHtmlsFound;
	}

	public List<String> getImages() {
		return images;
	}

	public void setImages(List<String> images) {
		this.images = images;
	}

	public String getHtmlFilePath() {
		return htmlFilePath;
	}

	public void setHtmlFilePath(String htmlFilePath) {
		this.htmlFilePath = htmlFilePath;
	}

	public List<String> getCssPath() {
		return cssPath;
	}

	public void setCssPath(List<String> cssPath) {
		this.cssPath = cssPath;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	

}
