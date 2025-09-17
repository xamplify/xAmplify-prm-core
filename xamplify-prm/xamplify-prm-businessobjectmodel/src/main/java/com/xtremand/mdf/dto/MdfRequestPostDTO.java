package com.xtremand.mdf.dto;

import java.io.Serializable;

import com.xtremand.form.submit.dto.FormSubmitDTO;

import lombok.Data;

@Data
public class MdfRequestPostDTO implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 3760309637743549610L;

	private Integer userId;
	
	private FormSubmitDTO formSubmitDto;
	
	private Integer vendorCompanyId;
	
	private Integer partnerCompanyId;
	

}
