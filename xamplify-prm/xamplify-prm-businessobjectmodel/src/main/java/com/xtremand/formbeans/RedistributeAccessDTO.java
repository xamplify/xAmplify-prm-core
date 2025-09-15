package com.xtremand.formbeans;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;

@Data
public class RedistributeAccessDTO implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -6690202995499897663L;

	@JsonIgnore
	private Integer count;
	
	private Integer superiorId;
	
	private boolean hasAccess;

}
