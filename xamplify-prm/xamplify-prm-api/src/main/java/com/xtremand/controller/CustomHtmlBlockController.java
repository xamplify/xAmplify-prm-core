package com.xtremand.controller;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.xtremand.custom.html.block.dto.CustomHtmlBlockDTO;
import com.xtremand.custom.html.block.service.CustomHtmlBlockService;
import com.xtremand.formbeans.XtremandResponse;
import com.xtremand.util.dto.Pageable;

@RestController
@RequestMapping(value = "/custom/html")
public class CustomHtmlBlockController {

	@Autowired
	private CustomHtmlBlockService customHtmlBlockService;

	@GetMapping("/paginated/userId/{loggedInUserId}")
	public ResponseEntity<XtremandResponse> findPaginatedCustomHtmls(@PathVariable Integer loggedInUserId,
			@Valid Pageable pageable) {
		return new ResponseEntity<>(customHtmlBlockService.findPaginatedCustomHtmls(pageable, loggedInUserId),
				HttpStatus.OK);
	}

	@PostMapping
	public ResponseEntity<XtremandResponse> save(@Valid @RequestBody CustomHtmlBlockDTO customHtmlBlockDto) {
		return ResponseEntity.ok(customHtmlBlockService.save(customHtmlBlockDto));
	}

	@GetMapping
	public ResponseEntity<XtremandResponse> findById(@RequestParam("id") Integer id,
			@RequestParam("loggedInUserId") Integer loggedInUserId) {
		return ResponseEntity.ok(customHtmlBlockService.findById(id, loggedInUserId));
	}

	@PutMapping
	public ResponseEntity<XtremandResponse> update(@Valid @RequestBody CustomHtmlBlockDTO customHtmlBlockDto) {
		return ResponseEntity.ok(customHtmlBlockService.update(customHtmlBlockDto));
	}

	@DeleteMapping
	public ResponseEntity<XtremandResponse> delete(@RequestParam("id") Integer id,
			@RequestParam("loggedInUserId") Integer loggedInUserId) {
		return ResponseEntity.ok(customHtmlBlockService.delete(id, loggedInUserId));
	}

	@PutMapping("/update/selection")
	public ResponseEntity<XtremandResponse> updateSelectedHtmlBlock(
			@Valid @RequestBody CustomHtmlBlockDTO customHtmlBlockDto) {
		return ResponseEntity.ok(customHtmlBlockService.updateSelectedHtmlBlock(customHtmlBlockDto));
	}

}
