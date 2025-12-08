package com.xtremand.lead.dto;

import java.util.ArrayList;
import java.util.List;

import com.xtremand.salesforce.dto.SfCustomFieldsDataDTO;

import lombok.Data;

@Data
public class LeadSyncDetailsDto {

    private String firstName;
    private String lastName;
    private String company;
    private String email;
    private String phone;
    private String website;
    private String street;
    private String city;
    private String state;
    private String country;
    private String postalCode;
    private String title;
    private String industry;
    private String region;
    private String createdByCompanyName;
    private String createdForCompanyName;
    private String pipelineName;
    private String pipelineStageName;
    private String externalPipelineId;
    private String externalPipelineStageId;
    private String referenceId;
    private List<SfCustomFieldsDataDTO> sfCustomFieldsData = new ArrayList<>();
    
}
