package com.xtremand.util.dto;

import java.util.ResourceBundle;

import org.springframework.beans.factory.annotation.Value;

public class ResourceBundlePropertyReaderDTO {

	@Value("${spring.profiles.active}")
	private String profiles;

	private ResourceBundlePropertyReaderDTO() {

	}

	private static ResourceBundle applicationProperties = ResourceBundle.getBundle("application");

	public static String getVodPath() {
		return applicationProperties.getString("media_base_path");

	}

}
