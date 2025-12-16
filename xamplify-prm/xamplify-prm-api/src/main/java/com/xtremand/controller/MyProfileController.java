package com.xtremand.controller;

import java.io.IOException;

import org.json.JSONObject;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.xtremand.deal.service.DealService;
import com.xtremand.formbeans.XtremandResponse;
import com.xtremand.integration.bom.Integration.IntegrationType;
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
            } else {
            	String dataString = (String) response.getData();
            	JSONObject data = new JSONObject(dataString);
            	if (data.has("error") && data.has("error_description")) {
            		response.setMessage(data.getString("error_description"));
            	}
            }

            return new ResponseEntity<>(response, HttpStatus.OK);
    }
    
    @GetMapping("/custom-crm/active/{userId}")
    public ResponseEntity<XtremandResponse> getCustomCrmDetails(@PathVariable Integer userId) {
            XtremandResponse response = new XtremandResponse();
			try {
				response = integrationWrapperService.getActiveCRMDetails(userId);
				return new ResponseEntity<>(response, HttpStatus.OK);
			} catch (IOException | ParseException e) {
				e.printStackTrace();
			}
			return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);

    }
    
    @GetMapping("/unlink/{userId}")
    public ResponseEntity<XtremandResponse> unlinkXamplifyIntegration(@PathVariable Integer userId) {
    	XtremandResponse response;
    	response = integrationWrapperService.unlinkCRM(IntegrationType.CUSTOM_CRM.name(), userId);
    	HttpStatus status = HttpStatus.valueOf(response.getStatusCode());
        if (status == null) {
                status = HttpStatus.BAD_REQUEST;
        }
    	return new ResponseEntity<>(response, status);
    }

}
