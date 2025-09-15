package com.xtremand.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.signature.dto.SignatureDTO;
import com.xtremand.campaign.exception.XamplifyDataAccessException;
import com.xtremand.formbeans.XtremandResponse;
import com.xtremand.signature.service.SignatureService;

@RequestMapping(value = "/signature/")
@RestController
public class SignatureController {

	@Autowired
	private SignatureService signatureService;

	/*** XNFR-812 ****/
	@PostMapping("/saveTypedSignature")
	public ResponseEntity<XtremandResponse> saveTypedSignature(@RequestBody SignatureDTO signatureRequestDTO) {
		return ResponseEntity.ok(signatureService.saveTypedSignature(signatureRequestDTO));
	}

	@GetMapping("/getExistingSignatures/{loggedInUserId}")
	public ResponseEntity<XtremandResponse> getExistingSignatures(@PathVariable Integer loggedInUserId) {
		try {
			return ResponseEntity.ok(signatureService.getExistingSignatures(loggedInUserId));
		} catch (IOException e) {
			throw new XamplifyDataAccessException(e);
		}
	}

	@PostMapping("/saveDrawSignature")
	public ResponseEntity<XtremandResponse> saveDrawSignature(@RequestBody SignatureDTO signatureDTO) {
		return ResponseEntity.ok(signatureService.saveDrawSignature(signatureDTO));
	}

	@PostMapping("/saveUploadedSignature")
	public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file,
			@RequestParam("userId") Integer userId) {
		try {
			String result = signatureService.uploadFile(file, userId);
			return ResponseEntity.ok(result);
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			return ResponseEntity.status(500).body("Failed to upload file.");
		}
	}

	@PutMapping("/removeExistingSignature")
	public ResponseEntity<XtremandResponse> removeExistingSignature(@RequestBody SignatureDTO signatureDto) {
		return ResponseEntity.ok(signatureService.removeExistingSignature(signatureDto));
	}

}
