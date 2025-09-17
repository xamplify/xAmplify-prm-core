package com.xtremand.tag.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.xtremand.common.bom.CompanyProfile;
import com.xtremand.util.dto.UserDetailsUtilDTO;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
@EqualsAndHashCode(callSuper = false)
public class TagDTO extends UserDetailsUtilDTO implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -8584180177419313109L;

	private Integer id;
	
	private List<String> tagNames = new ArrayList<>();
	
	private String tagName;
	
	private Integer createdBy;
	
	private Integer updatedBy;
	
	private List<Integer> tagIds = new ArrayList<>();
	
	private String createdDateInUTCString;

	private Date createdTime;
		
	private Date updatedTime;
	
	private CompanyProfile companyProfile;
	
	private Integer companyId;
	
	private Integer userId;

	
}
