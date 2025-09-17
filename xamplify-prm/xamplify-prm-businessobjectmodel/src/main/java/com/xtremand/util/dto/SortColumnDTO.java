package com.xtremand.util.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class SortColumnDTO implements Serializable {

	

	public SortColumnDTO(String columnName, String databaseColumnName, boolean defaultSortColumn, boolean checkNulls,
			boolean orderArray) {
		super();
		this.columnName = columnName;
		this.databaseColumnName = databaseColumnName;
		this.defaultSortColumn = defaultSortColumn;
		this.checkNulls = checkNulls;
		this.orderArray = orderArray;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 7831547899707074119L;
	
	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	private String columnName;
	
	private String databaseColumnName;
	
	private boolean defaultSortColumn;

	private boolean checkNulls;
	
	private boolean orderArray;



}
