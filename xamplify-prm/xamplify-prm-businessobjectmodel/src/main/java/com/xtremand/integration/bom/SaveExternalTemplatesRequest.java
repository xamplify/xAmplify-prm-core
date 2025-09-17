package com.xtremand.integration.bom;

import java.util.List;

import com.xtremand.formbeans.ExternalEmailTemplateDTO;

import lombok.Data;

@Data
public class SaveExternalTemplatesRequest {
	private String type;
	private Integer userId;
	private List<ExternalEmailTemplateDTO> templates;
	
}
