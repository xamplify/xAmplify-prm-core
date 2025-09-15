package com.xtremand.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.xtremand.form.submit.dto.FormSubmitDTO;
import com.xtremand.form.submit.service.FormSubmitService;
import com.xtremand.formbeans.XtremandResponse;

@RestController
@RequestMapping(value="/form/submit/")
public class FormSubmitController {
	
	
	
	@Autowired
	private FormSubmitService formSubmitService;
	
	@PostMapping(value="save")
	@ResponseBody
	public ResponseEntity<XtremandResponse> save(@RequestBody FormSubmitDTO formSubmitDTO){
		return ResponseEntity.ok(formSubmitService.save(formSubmitDTO,null));
	}
	
	@PostMapping(value="uploadFile", consumes = { "multipart/form-data" })
	@ResponseBody
	public ResponseEntity<XtremandResponse> save(@RequestPart("uploadedFile") MultipartFile uploadedFile,  @RequestPart("formSubmitDTO") FormSubmitDTO formSubmitDTO){
		return ResponseEntity.ok(formSubmitService.uploadFile(uploadedFile,formSubmitDTO));
	}

	@PostMapping(value="save-lms-form")
	@ResponseBody
	public ResponseEntity<XtremandResponse> saveLmsForm(@RequestBody FormSubmitDTO formSubmitDTO){
		return ResponseEntity.ok(formSubmitService.saveLmsForm(formSubmitDTO,null));
	}
}
