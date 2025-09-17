package com.xtremand.lead.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class PipelineResponseDTO implements Serializable {
	/**
	* 
	*/
	private static final long serialVersionUID = 2606707205053046056L;

	private Integer id;

	private String name;
	
	private boolean isDefault = false;

}
