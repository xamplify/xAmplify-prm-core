package com.xtremand.form.dto;

import java.util.ArrayList;
import java.util.List;

import com.xtremand.form.submit.dto.FormSubmitFieldsValuesDTO;

import lombok.Data;

@Data
public class FormValuesDTO {
	
	private List<FormSubmitFieldsValuesDTO> fields = new ArrayList<>();
}
