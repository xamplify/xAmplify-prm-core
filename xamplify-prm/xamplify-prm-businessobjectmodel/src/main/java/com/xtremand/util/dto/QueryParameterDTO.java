package com.xtremand.util.dto;

import lombok.Data;

@Data
public class QueryParameterDTO {

	public QueryParameterDTO(String key, Object value) {
		super();
		this.key = key;
		this.value = value;
	}

	private String key;

	private Object value;

}
