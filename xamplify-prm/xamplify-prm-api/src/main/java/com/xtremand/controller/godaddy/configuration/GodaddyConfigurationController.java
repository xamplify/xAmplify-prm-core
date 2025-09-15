package com.xtremand.controller.godaddy.configuration;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.xtremand.formbeans.XtremandResponse;
import com.xtremand.godaddy.configuration.service.GoDaddyConfigurationService;
import com.xtremand.godaddy.dto.GodaddyDnsRecordDetailsDto;

@RestController
@RequestMapping("/godaddy/")
public class GodaddyConfigurationController {

	  @Autowired
		private GoDaddyConfigurationService godaddyService;
	    
		@PostMapping("/dns/add")
		public ResponseEntity<XtremandResponse> addDnsRecord(@RequestBody GodaddyDnsRecordDetailsDto record) {
			return ResponseEntity.ok(godaddyService.addDnsRecordOfGodaddy(record));
		}
		
		@PostMapping("domainName/valid")
		public ResponseEntity<XtremandResponse> domainNameValidation(@RequestBody GodaddyDnsRecordDetailsDto record) {
			return ResponseEntity.ok(godaddyService.domainNameValidation(record));
		}
		
		@GetMapping("updateGodaddyConfiguration/{companyId}/{isConnected}")
		public ResponseEntity<XtremandResponse> updateGodaddyConfiguration(@PathVariable Integer companyId,@PathVariable boolean isConnected) {
			return ResponseEntity.ok(godaddyService.updateGodaddyConfiguration(companyId,isConnected));
		}

		@GetMapping("isGodaddyConfigured/{companyId}")
		public ResponseEntity<XtremandResponse> isGodaddyConfigured(@PathVariable Integer companyId) {
			return ResponseEntity.ok(godaddyService.isGodaddyConfigured(companyId));
		}
		
		@GetMapping("fetchDnsRecordByValue/{value}")
		public ResponseEntity<Boolean> fetchExistingDnsRecords(@PathVariable String value){
			return ResponseEntity.ok(godaddyService.fetchExistingDnsRecords(value));
		}
		
		@GetMapping("getDomainName/{companyId}")
		public ResponseEntity<XtremandResponse> getDomainName(@PathVariable Integer companyId ){
	       return ResponseEntity.ok(godaddyService.getDomainName(companyId));
		}
		
		@GetMapping("records")
		public ResponseEntity<XtremandResponse> getDnsRecordsOfGodaddy(){
			 return ResponseEntity.ok(godaddyService.getDnsRecordsOfGodaddy());
		}
		
		@DeleteMapping("deleteAll")
		public ResponseEntity<XtremandResponse> deleteAllDnsRecordsByTypeAnaName(){
			return ResponseEntity.ok(godaddyService.deleteAllDnsRecordsByTypeAndName());
		}

}
