package com.xtremand.custom.link.service;

import java.util.List;

import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;

import com.custom.link.dto.CustomLinkRequestDTO;
import com.xtremand.formbeans.XtremandResponse;
import com.xtremand.util.dto.Pageable;

public interface CustomLinkService {

	XtremandResponse save(CustomLinkRequestDTO customLinkRequestDTO, BindingResult result,
			MultipartFile dashboardBannerImage);

	XtremandResponse findAll(Pageable pageable, BindingResult result, Integer loggedInUserId, List<String> types,
			String vendorCompanyProfileName);

	XtremandResponse getById(Integer id, Integer loggedInUserId);

	XtremandResponse update(CustomLinkRequestDTO customLinkRequestDto, BindingResult result, MultipartFile dashboardBannerImage);

	XtremandResponse delete(Integer id, Integer loggedInUserId);

}
