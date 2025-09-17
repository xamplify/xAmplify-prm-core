package com.xtremand.update.email.address.service.impl;

import java.util.Date;

import org.hibernate.HibernateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

import com.xtremand.campaign.exception.XamplifyDataAccessException;
import com.xtremand.dao.util.GenericDAO;
import com.xtremand.formbeans.XtremandResponse;
import com.xtremand.superadmin.dto.ChangeEmailAddressRequestDTO;
import com.xtremand.superadmin.validator.UpdateEmailAddressValidator;
import com.xtremand.update.email.address.service.UpdateEmailAddressService;
import com.xtremand.util.XamplifyUtils;
import com.xtremand.util.bom.UpdatedEmailAddressHistory;
import com.xtremand.util.dao.HibernateSQLQueryResultUtilDao;
import com.xtremand.util.dto.HibernateSQLQueryResultRequestDTO;
import com.xtremand.util.dto.QueryParameterDTO;
import com.xtremand.util.dto.XamplifyUtilValidator;

@Service
@Transactional
public class UpdateEmailAddressServiceImpl implements UpdateEmailAddressService {

	@Autowired
	private HibernateSQLQueryResultUtilDao hibernateSQLQueryResultUtilDao;

	@Autowired
	private GenericDAO genericDao;

	@Autowired
	private UpdateEmailAddressValidator updateEmailAddressValidator;

	@Autowired
	private XamplifyUtilValidator xamplifyUtilValidator;

	@Override
	public XtremandResponse validateEmailAddressChange(ChangeEmailAddressRequestDTO changeEmailAddressRequestDTO,
			BindingResult bindingResult) {
		XtremandResponse response = new XtremandResponse();
		updateEmailAddressValidator.validate(changeEmailAddressRequestDTO, bindingResult);
		if (bindingResult.hasErrors()) {
			xamplifyUtilValidator.addErrorResponse(bindingResult, response);
		} else {
			XamplifyUtils.addSuccessStatusWithMessage(response, "Validation Passed");
		}
		return response;
	}
	
	@Override
	public XtremandResponse validateEmailAddress(String emailAddress) {
		XtremandResponse response = new XtremandResponse();
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		String queryString = "select case when count(email_id)>0 then true else false end  from xt_user_profile where LOWER(TRIM(email_id)) = :emailAddress";
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO("emailAddress", emailAddress.trim().toLowerCase()));
		boolean isEmailAddressExists = hibernateSQLQueryResultUtilDao.returnBoolean(hibernateSQLQueryResultRequestDTO);
		if (isEmailAddressExists) {
			XamplifyUtils.addSuccessStatusWithMessage(response, "Email Address Found.");
		} else {
			XamplifyUtils.addErorMessageWithStatusCode(response, "Email Address Not Found.",
					HttpStatus.NOT_FOUND.value());
		}
		return response;
	}

	@Override
	public XtremandResponse updateEmailAddress(ChangeEmailAddressRequestDTO changeEmailAddressRequestDTO) {
		try {
			XtremandResponse response = new XtremandResponse();
			String queryString = "update xt_user_profile set email_id = :updatedEmailId, user_name = :updatedEmailId where email_id = :emailId";
			updateEmailAddressInUserOrCampaignTables(changeEmailAddressRequestDTO, queryString);
			XamplifyUtils.addSuccessStatusWithMessage(response,
					"User Email Address Updated Successfully In UserProfile Table.");
			UpdatedEmailAddressHistory updatedEmailAddressHistory = new UpdatedEmailAddressHistory();
			updatedEmailAddressHistory.setOldEmailAddress(changeEmailAddressRequestDTO.getExistingEmailAddress());
			updatedEmailAddressHistory.setNewEmailAddress(changeEmailAddressRequestDTO.getUpdatedEmailAddress());
			updatedEmailAddressHistory.setUpdatedOn(new Date());
			genericDao.save(updatedEmailAddressHistory);
			return response;
		} catch (XamplifyDataAccessException xe) {
			throw new XamplifyDataAccessException(xe.getMessage(), xe);
		}
	}

	@Override
	public XtremandResponse updateCampaignEmail(ChangeEmailAddressRequestDTO changeEmailAddressRequestDTO) {
		XtremandResponse response = new XtremandResponse();
		String queryString = "update xt_campaign set email = :updatedEmailId where email  = :emailId";
		updateEmailAddressInUserOrCampaignTables(changeEmailAddressRequestDTO, queryString);
		XamplifyUtils.addSuccessStatusWithMessage(response,
				"User Email Address Updated Successfully In Campaign Module.");
		return response;
	}

	@Override
	public XtremandResponse removeAccessToken(ChangeEmailAddressRequestDTO changeEmailAddressRequestDTO) {
		XtremandResponse response = new XtremandResponse();
		String queryString = "delete from oauth_access_token where user_name = :emailId";
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO("emailId", changeEmailAddressRequestDTO.getExistingEmailAddress()));
		hibernateSQLQueryResultUtilDao.update(hibernateSQLQueryResultRequestDTO);
		XamplifyUtils.addSuccessStatusWithMessage(response, "Access Token Removed Successfully.");
		return response;
	}
	
	private void updateEmailAddressInUserOrCampaignTables(ChangeEmailAddressRequestDTO changeEmailAddressRequestDTO,
			String queryString) {
		try {
			HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
			hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(
					new QueryParameterDTO("updatedEmailId", changeEmailAddressRequestDTO.getUpdatedEmailAddress()));
			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
					.add(new QueryParameterDTO("emailId", changeEmailAddressRequestDTO.getExistingEmailAddress()));
			hibernateSQLQueryResultUtilDao.updateAndReturnCount(hibernateSQLQueryResultRequestDTO);
		} catch (HibernateException | XamplifyDataAccessException e) {
			throw new XamplifyDataAccessException("Error In updateEmailAddressInUserOrCampaignTables()", e);
		} catch (Exception ex) {
			throw new XamplifyDataAccessException("Error In updateEmailAddressInUserOrCampaignTables()", ex);
		}
	}

}
