package com.xtremand.controller.analytics;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.xtremand.analytics.service.VendorAnalyticsService;
import com.xtremand.common.bom.Pagination;
import com.xtremand.formbeans.XtremandResponse;
import com.xtremand.vanity.url.dto.VanityUrlDetailsDTO;

@RestController
@RequestMapping("/vendor")
public class VendorAnalyticsController {

	@Autowired
	private VendorAnalyticsService vendorAnalyticsService;

	@PostMapping(value = "/details")
	public ResponseEntity<Map<String, Object>> getVendorDetails(@RequestParam Integer partnerId,
			@RequestBody Pagination pagination) {
		Map<String, Object> resultMap = vendorAnalyticsService.findAllVendors(pagination, partnerId);
		return ResponseEntity.status(HttpStatus.OK).body(resultMap);
	}

	@PostMapping(value = "/info")
	public ResponseEntity<Map<String, Object>> getVendors(@RequestBody Pagination pagination) {
		Map<String, Object> resultMap = vendorAnalyticsService.getVendors(pagination);
		return ResponseEntity.status(HttpStatus.OK).body(resultMap);
	}

	@PostMapping(value = "/count")
	public ResponseEntity<XtremandResponse> getVendorCount(@RequestBody VanityUrlDetailsDTO vanityUrlDetailsDTO) {
		XtremandResponse response = vendorAnalyticsService.getVendorCount(vanityUrlDetailsDTO);
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}
}
