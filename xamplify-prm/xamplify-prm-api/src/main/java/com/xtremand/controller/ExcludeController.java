package com.xtremand.controller;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.xtremand.common.bom.Pagination;
import com.xtremand.formbeans.UserDTO;
import com.xtremand.formbeans.XtremandResponse;
import com.xtremand.user.service.UserService;

@RestController
@RequestMapping("/exclude")
public class ExcludeController {
	
	private  static final  Logger logger = LoggerFactory.getLogger(ExcludeController.class);
	
	@Autowired
	UserService userService;	
	
	@PostMapping("save-users/{userId}")
	@ResponseBody
	public ResponseEntity<XtremandResponse> saveExcludedUsers(@RequestBody Set<UserDTO> excludedUsers, @PathVariable Integer userId) {
		return ResponseEntity.ok(userService.saveExcludedUsers(excludedUsers, userId));
	}
	
	@PostMapping("list-users/{userId}")
	@ResponseBody
	public ResponseEntity<XtremandResponse>  listExcludedUsers(@PathVariable Integer userId, @RequestBody Pagination pagination){
		return ResponseEntity.ok(userService.listExcludedUsers(userId, pagination));
	}
	
	@GetMapping("delete-user/{excludedUserId}/{userId}")
	@ResponseBody
	public ResponseEntity<XtremandResponse> deleteExcludedUser(@PathVariable Integer excludedUserId, @PathVariable Integer userId) {
		return ResponseEntity.ok(userService.deleteExcludedUser(userId,  excludedUserId));
	}
	
	@PostMapping("save-domains/{userId}")
	@ResponseBody
	public ResponseEntity<XtremandResponse> saveExcludedDomains(@RequestBody Set<String> domainNames, @PathVariable Integer userId) {
		return ResponseEntity.ok(userService.saveExcludedDomains(domainNames, userId));
	}
	
	@PostMapping("list-domains/{userId}")
	@ResponseBody
	public ResponseEntity<XtremandResponse>  listExcludedDomains(@PathVariable Integer userId, @RequestBody Pagination pagination){
		return ResponseEntity.ok(userService.listExcludedDomains(userId, pagination));
	}
	
	@GetMapping("delete-domain/{domain}/{userId}")
	@ResponseBody
	public ResponseEntity<XtremandResponse> deleteExcludedDomain(@PathVariable String domain, @PathVariable Integer userId) {
		return ResponseEntity.ok(userService.deleteExcludedDomain(userId,  domain));
	}
	
	@PostMapping("validate-users/{userId}")
	@ResponseBody
	public ResponseEntity<XtremandResponse> validateExcludedUsers(@RequestBody Set<UserDTO> excludedUsers, @PathVariable Integer userId) {
		return ResponseEntity.ok(userService.validateExcludedUsersExist(excludedUsers, userId));
	}

	@PostMapping("validate-domains/{userId}")
	@ResponseBody
	public ResponseEntity<XtremandResponse> validateExcludedDomains(@RequestBody Set<String> excludedDomains, @PathVariable Integer userId) {
		return ResponseEntity.ok(userService.validateExcludedDomainsExist(excludedDomains, userId));
	}

}
