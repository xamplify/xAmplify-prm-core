package com.xtremand.util.dto;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Data;

@Data
@JsonPropertyOrder({ "totalRecords" })
public class PaginatedDTO {

	private List<?> list = new ArrayList<>();

	private Integer totalRecords;

}
