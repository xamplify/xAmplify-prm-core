package com.xtremand.deal.dto;

import java.util.ArrayList;
import java.util.List;

import com.xtremand.salesforce.dto.SfCustomFieldsDataDTO;

import lombok.Data;

@Data
public class DealSyncDetailsDto {

    private String title;
    private Double amount;
    private String closeDate;
    private String pipelineName;
    private String pipelineStageName;
    private String externalPipelineId;
    private String externalPipelineStageId;
    private String referenceId;
    private List<SfCustomFieldsDataDTO> sfCustomFieldsData = new ArrayList<>();
    
}
