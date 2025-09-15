package com.xtremand.integration.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Date;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.xtremand.common.bom.CompanyProfile;
import com.xtremand.dao.util.GenericDAO;
import com.xtremand.integration.bom.ExternalContactDTO;
import com.xtremand.integration.bom.Integration;
import com.xtremand.integration.bom.Integration.IntegrationType;
import com.xtremand.integration.dao.IntegrationDao;
import com.xtremand.lead.bom.Lead;
import com.xtremand.user.bom.User;

@Component
public class IntegrationServiceHelper {

	@Autowired
	IntegrationDao integrationDao;

	@Autowired
	private GenericDAO genericDAO;

	protected JSONObject convertStreamToJSONObject(InputStream inputStream) throws IOException, ParseException {
		JSONObject json = null;
		if (inputStream != null) {
			JSONParser parser = new JSONParser();
			Reader reader = new InputStreamReader(inputStream);
			if (reader != null) {
				json = (JSONObject) parser.parse(reader);
			}
		}
		return json;
	}

	protected boolean fillAdditionalFieldsInExistingLead(ExternalContactDTO existingLead, ExternalContactDTO lead) {
		boolean haveChanges = false;
		existingLead.setId(null);
		if (existingLead.getAddress() == null && lead.getAddress() != null) {
			existingLead.setAddress(lead.getAddress());
			haveChanges = true;
		}
		if (existingLead.getCity() == null && lead.getCity() != null) {
			existingLead.setCity(lead.getCity());
			haveChanges = true;
		}
		if (existingLead.getCompany() == null && lead.getCompany() != null) {
			existingLead.setCompany(lead.getCompany());
			haveChanges = true;
		}
		if (existingLead.getCountry() == null && lead.getCountry() != null) {
			existingLead.setCountry(lead.getCountry());
			haveChanges = true;
		}
		if (existingLead.getFirstName() == null && lead.getFirstName() != null) {
			existingLead.setFirstName(lead.getFirstName());
			haveChanges = true;
		}
		if (existingLead.getLastName() == null && lead.getLastName() != null) {
			existingLead.setLastName(lead.getLastName());
			haveChanges = true;
		}
		if (existingLead.getMobilePhone() == null && lead.getMobilePhone() != null) {
			existingLead.setMobilePhone(lead.getMobilePhone());
			haveChanges = true;
		}
		if (existingLead.getPostalCode() == null && lead.getPostalCode() != null) {
			existingLead.setPostalCode(lead.getPostalCode());
			haveChanges = true;
		}
		if (existingLead.getState() == null && lead.getState() != null) {
			existingLead.setState(lead.getState());
			haveChanges = true;
		}
		if (existingLead.getWebsite() == null && lead.getWebsite() != null) {
			existingLead.setWebsite(lead.getWebsite());
			haveChanges = true;
		}
		if (existingLead.getTitle() == null && lead.getTitle() != null) {
			existingLead.setTitle(lead.getTitle());
			haveChanges = true;
		}
		return haveChanges;

	}

	protected boolean fillAdditionalFieldsInExistingLead(ExternalContactDTO existingLead, Lead lead) {
		boolean haveChanges = false;
		existingLead.setId(null);

		if (existingLead.getCity() == null && lead.getCity() != null) {
			existingLead.setCity(lead.getCity());
			haveChanges = true;
		}
		if (existingLead.getCompany() == null && lead.getCompany() != null) {
			existingLead.setCompany(lead.getCompany());
			haveChanges = true;
		}
		if (existingLead.getCountry() == null && lead.getCountry() != null) {
			existingLead.setCountry(lead.getCountry());
			haveChanges = true;
		}
		if (existingLead.getFirstName() == null && lead.getFirstName() != null) {
			existingLead.setFirstName(lead.getFirstName());
			haveChanges = true;
		}
		if (existingLead.getLastName() == null && lead.getLastName() != null) {
			existingLead.setLastName(lead.getLastName());
			haveChanges = true;
		}
		if (existingLead.getMobilePhone() == null && lead.getPhone() != null) {
			existingLead.setMobilePhone(lead.getPhone());
			haveChanges = true;
		}
		if (existingLead.getPostalCode() == null && lead.getPostalCode() != null) {
			existingLead.setPostalCode(lead.getPostalCode());
			haveChanges = true;
		}
		if (existingLead.getState() == null && lead.getState() != null) {
			existingLead.setState(lead.getState());
			haveChanges = true;
		}
		if (existingLead.getWebsite() == null && lead.getWebsite() != null) {
			existingLead.setWebsite(lead.getWebsite());
			haveChanges = true;
		}
		/*
		 * if(existingLead.getTitle() == null && lead.getTitle() != null){
		 * existingLead.setTitle(lead.getTitle()); haveChanges = true; }
		 */
		return haveChanges;

	}

	// Generic method to save access token
	protected Integration saveAccessToken(User loggedInUser, JSONObject data, IntegrationType type) {
		Integration integration = null;
		if (loggedInUser != null && data != null) {
			CompanyProfile companyProfile = loggedInUser.getCompanyProfile();
			if (companyProfile != null) {
				integration = integrationDao.getUserIntegrationDetails(companyProfile.getId(), type);
				Integer integrationsCount = integrationDao
						.getTotalIntegrationsCount(loggedInUser.getCompanyProfile().getId());
				if (integration == null) {
					integration = new Integration();
					integration.setCompany(loggedInUser.getCompanyProfile());
					integration.setType(type);
					integration.setCreatedBy(loggedInUser.getUserId());
					integration.setCreatedTime(new Date());
					Date expiry = new Date();
					expiry.setTime(expiry.getTime() + ((Long) data.get("expires_in") * 1000));
					integration.setAccessToken((String) data.get("access_token"));
					integration.setRefreshToken((String) data.get("refresh_token"));
					integration.setExpiry(expiry);
					integration.setUpdatedBy(loggedInUser.getUserId());
					integration.setUpdatedTime(new Date());
					if (integrationsCount == 0) {
						integration.setActive(true);
					}
					genericDAO.save(integration);
				} else {
					Date expiry = new Date();
					expiry.setTime(expiry.getTime() + ((Long) data.get("expires_in") * 1000));
					integration.setAccessToken((String) data.get("access_token"));
					integration.setRefreshToken((String) data.get("refresh_token"));
					integration.setExpiry(expiry);
					integration.setUpdatedBy(loggedInUser.getUserId());
					integration.setUpdatedTime(new Date());
					if (integrationsCount == 1) {
						integration.setActive(true);
					}
				}
			}
		}
		return integration;
	}
}
