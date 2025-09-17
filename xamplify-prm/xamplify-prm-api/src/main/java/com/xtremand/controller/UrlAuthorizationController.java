package com.xtremand.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.xtremand.url.authorization.service.UrlAuthorizationService;

@RestController
@RequestMapping(value = "/authorize/")
public class UrlAuthorizationController {

	@Autowired
	private UrlAuthorizationService urlAuthorizationService;

	@GetMapping("url/modules/{moduleId}/users/{userId}/routerUrls/{routerUrl}")
	public ResponseEntity<String> authorize(@PathVariable Integer moduleId, @PathVariable("userId") Integer userId,
			@PathVariable("routerUrl") String routerUrl,
			@RequestParam(value = "subDomain", required = false) String subDomain) {
		urlAuthorizationService.authorizeByModuleId(moduleId, userId, routerUrl, subDomain);
		return ResponseEntity.ok("Authorization completed for module: " + moduleId);

	}

}
