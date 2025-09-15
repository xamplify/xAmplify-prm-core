package com.xtremand.partner.journey.dto;

import com.xtremand.partner.journey.bom.TriggerComponentType;

import lombok.Data;

@Data
public class TriggerComponentDTO {
	private Integer id;	
	private String key; 
	private String value;
	private TriggerComponentType type;
}
