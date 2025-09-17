package com.xtremand.flexi.fields.dto;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.xtremand.util.dto.CreatedTimeConverter;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
@JsonPropertyOrder({ "id", "fieldName" })
public class FlexiFieldResponseDTO extends CreatedTimeConverter implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7608101986420381408L;

	private Integer id;

	private String fieldName;

	private String fieldValue;

	private String createdBy;

}
