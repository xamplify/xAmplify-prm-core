package com.xtremand.update.email.address.service;

import org.springframework.validation.BindingResult;

import com.xtremand.formbeans.XtremandResponse;
import com.xtremand.superadmin.dto.ChangeEmailAddressRequestDTO;

public interface UpdateEmailAddressService {

	XtremandResponse validateEmailAddressChange(ChangeEmailAddressRequestDTO changeEmailAddressRequestDTO,
			BindingResult bindingResult);

	XtremandResponse updateEmailAddress(ChangeEmailAddressRequestDTO changeEmailAddressRequestDTO);

	XtremandResponse updateCampaignEmail(ChangeEmailAddressRequestDTO changeEmailAddressRequestDTO);

	XtremandResponse removeAccessToken(ChangeEmailAddressRequestDTO changeEmailAddressRequestDTO);

	XtremandResponse validateEmailAddress(String emailAddress);

}
