package com.xtremand.lead.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;

@Data
@JsonInclude(value=Include.NON_NULL)
public class PipelineStageDto {
	private Integer id;
	private String stageName;
	private boolean defaultStage = false;
	private boolean won = false;
	private boolean lost = false;
	private Integer pipelineId;
	private Integer displayIndex;
	private boolean canDelete = false;
	private boolean nonInteractive = false;
	private String externalPipelineStageId;
	private String externalPipelineId;
	private boolean isPrivate = false;
	private Long externalPipelineStepId;
	private Integer companyId;
}
