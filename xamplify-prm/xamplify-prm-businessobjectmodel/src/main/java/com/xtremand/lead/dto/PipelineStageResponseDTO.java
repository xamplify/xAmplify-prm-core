package com.xtremand.lead.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class PipelineStageResponseDTO implements Serializable {
	/**
	* 
	*/
	private static final long serialVersionUID = 3850689183420721451L;
	
	private Integer id;
	
	private String stageName;

}
