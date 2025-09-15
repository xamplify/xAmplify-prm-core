package com.xtremand.dam.dto;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.xtremand.util.dto.CreatedTimeConverter;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@JsonPropertyOrder({ "id", "assetName", "assetType" })
public class ShareAssetsResponseDTO extends CreatedTimeConverter implements Serializable {
	/**
	* 
	*/
	private static final long serialVersionUID = -3228382927716021090L;

	private Integer id;

	private String assetName;

	private String assetType;

	private Integer createdUserId;

	private String createdBy;

}
