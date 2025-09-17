package com.xtremand.util.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class HibernateSQLQueryResultRequestDTO {

	private String queryString;

	private List<QueryParameterDTO> queryParameterDTOs = new ArrayList<>();

	private List<QueryParameterListDTO> queryParameterListDTOs = new ArrayList<>();

	private String searchQueryString;

	private String groupByQueryString = "";

	private String sortQueryString = "";

	private Class<?> classInstance;

	private List<String> searchColumns = new ArrayList<>();
	
	private String rowCountQueryString;
	
	private Integer rowCount;	

}
