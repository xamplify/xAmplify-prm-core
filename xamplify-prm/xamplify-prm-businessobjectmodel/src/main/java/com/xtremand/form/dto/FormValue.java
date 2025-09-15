package com.xtremand.form.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class FormValue implements Comparable<FormValue> {

	private String value;
	
	private int order;
	
	private boolean downloadable;
	
	private String downloadLink;
	
	private List<String> values;
	
	private int columnOrder;
	
	private int id;

	@Override
	public int compareTo(FormValue o) {
		if(this.getOrder()<o.getOrder()){
			return -1;
		}else if(this.getOrder() == o.getOrder()){
			return 0;
		}else{
			return 1;
		}
	}


	
}
