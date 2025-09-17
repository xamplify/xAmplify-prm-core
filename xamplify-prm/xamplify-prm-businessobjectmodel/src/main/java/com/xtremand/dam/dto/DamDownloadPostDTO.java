package com.xtremand.dam.dto;

import java.io.Serializable;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class DamDownloadPostDTO extends DamDownloadMappedDTO implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 5727724857198574942L;

	private String alias;
	
	private Integer userId;
	
	

}
