package com.xtremand.integration.dto;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class ConnectWiseProductDTO implements Serializable {


	/**
	 * 
	 */
	private static final long serialVersionUID = -4327432339779402522L;

	private Long id;

	private String name;

	private String identifier;

	private String description;

	private Double price;

	private Double cost;

	private Integer quantity;
}
