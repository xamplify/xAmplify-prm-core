package com.xtremand.lead.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;

@Data
@JsonInclude(value=Include.NON_NULL)
public class PipelineDto {
	private Integer id;
	private String name;
	private String type;
	private List<PipelineStageDto> stages;
	private Integer userId;
	private Integer companyId;
	private boolean isPrivate = false;
	private boolean isDefault = false;
	private boolean isSalesforcePipeline = false;
	private boolean canUpdate = false;
	private boolean canDelete = false;
	private boolean canSync = false;
	private boolean canDeleteStages = false;
	private String createdByName;
	private String createdByEmail;
	private String createdTime;
	private String integrationType;
	private boolean isCrmPipeline = false;
	private boolean enablePrivateCheckBox = false;
	private boolean showSinglePublicPipelineMessage = false;
}
