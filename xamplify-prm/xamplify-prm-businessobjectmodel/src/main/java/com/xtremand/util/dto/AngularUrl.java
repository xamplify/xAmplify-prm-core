package com.xtremand.util.dto;

import com.xtremand.vanity.url.dto.VanityUrlDetailsDTO;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class AngularUrl extends VanityUrlDetailsDTO {
	
	private String url;

}
