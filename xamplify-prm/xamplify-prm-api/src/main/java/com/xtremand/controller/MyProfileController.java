package com.xtremand.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.xtremand.deal.service.DealService;
import com.xtremand.formbeans.XtremandResponse;
import com.xtremand.integration.dto.CustomCrmValidationRequestDto;
import com.xtremand.integration.service.IntegrationWrapperService;
import com.xtremand.lead.service.LeadService;

@RestController
@RequestMapping(value = "/myProfile/")
public class MyProfileController {
	
	@Autowired
    private IntegrationWrapperService integrationWrapperService;

    @Autowired
    private LeadService leadService;
    
    @Autowired
    private DealService dealService;

    @PostMapping("/custom-crm/validate")
    public ResponseEntity<XtremandResponse> validateCustomCrm(@RequestBody CustomCrmValidationRequestDto requestDto) {
            XtremandResponse response = integrationWrapperService.validateCustomCrmIntegration(requestDto.getPat(), requestDto.getUserId());

            HttpStatus status = HttpStatus.valueOf(response.getStatusCode());
            if (status == null) {
                    status = HttpStatus.BAD_REQUEST;
            }

            if (status.is2xxSuccessful()) {
            	Integer userId = requestDto.getUserId();
                    leadService.saveLeadCustomFormFromMcp(userId);
                    leadService.saveLeadPipelinesFromMcp(userId);
                    dealService.saveDealCustomFormFromMcp(userId);
                    dealService.saveDealPipelinesFromMcp(userId);
            }

            return new ResponseEntity<>(response, status);
    }

}
