package com.xtremand.form.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class FormLabelDTORow implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3829397169394062550L;
	/**
	 * 
	 */

	private List<FormLabelDTO> formLabelDTOs = new ArrayList<>();

}