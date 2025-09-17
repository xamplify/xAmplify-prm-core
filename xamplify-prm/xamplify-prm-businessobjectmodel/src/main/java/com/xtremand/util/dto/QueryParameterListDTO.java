package com.xtremand.util.dto;

import java.util.List;

import lombok.Data;

@Data
public class QueryParameterListDTO {

	private String key;

	private List<?> values;

	public QueryParameterListDTO(String key, List<?> values) {
		super();
		this.key = key;
		this.values = values;
	}

}
