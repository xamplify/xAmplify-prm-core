package com.xtremand.dam.dto;

import java.io.Serializable;

import org.springframework.util.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;

@Data
public class DamPreviewDTO implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -5439736600015418798L;

	private String name;
	
	@Getter(value = AccessLevel.NONE)
	private String assetPath;
	
	private boolean beeTemplate;
	
	@JsonIgnore
	private Integer createdBy;
	
	@Getter(value = AccessLevel.NONE)
	private String htmlBody;
	
	private Integer id;

	private String alias;

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public String getAssetPath() {
		return StringUtils.hasText(assetPath) ? assetPath.trim() : "";
	}

	public String getHtmlBody() {
		return StringUtils.hasText(htmlBody) ? htmlBody.trim() : "";
	}

}
