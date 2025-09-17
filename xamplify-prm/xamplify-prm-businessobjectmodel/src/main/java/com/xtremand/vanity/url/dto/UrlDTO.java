package com.xtremand.vanity.url.dto;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

@Data
public class UrlDTO implements Serializable {
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -1803802423432975742L;

	private String vanityUrl;
	
	private String url;
	
	private Integer id;
	
	private String alias;
	
	private Date createdTime;

}
