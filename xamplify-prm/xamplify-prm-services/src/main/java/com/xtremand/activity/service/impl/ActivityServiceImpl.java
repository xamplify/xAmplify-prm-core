package com.xtremand.activity.service.impl;

import java.io.IOException;
import java.util.Date;

import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

import com.xtremand.activity.dao.ActivityDAO;
import com.xtremand.activity.service.ActivityService;
import com.xtremand.activity.validator.EmailActivityValidator;
import com.xtremand.common.bom.Pagination;
import com.xtremand.formbeans.XtremandResponse;
import com.xtremand.user.dao.UserDAO;
import com.xtremand.util.XamplifyUtils;
import com.xtremand.util.dto.Pageable;
import com.xtremand.util.dto.XamplifyUtilValidator;
import com.xtremand.util.service.UtilService;

@Service
@Transactional
public class ActivityServiceImpl implements ActivityService{
	
	private static final String INVALID_INPUT_PARAMETERS = "Invalid Input parameters.";

	@Autowired
	private UtilService utilService;
	
	@Autowired
	private XamplifyUtilValidator xamplifyUtilValidator;
	
	@Autowired
	private EmailActivityValidator validator;
	
	@Autowired
	private ActivityDAO activityDAO;
	
	@Autowired
	private UserDAO userDAO;
	
	@Value("${brandfetch.logo.endpoint}")
	private String logoEndPoint;
	
	private static final Logger logger = LoggerFactory.getLogger(ActivityServiceImpl.class);

	@Override
	public XtremandResponse fetchRecentActivities(Pageable pageable, Integer userId, Integer loggedInUserId,
			Boolean isCompanyJourney, BindingResult result) {
		XtremandResponse response = new XtremandResponse();
		validator.validateDataToFetchAllActivities(loggedInUserId, userId, result);
		if (result.hasErrors()) {
			xamplifyUtilValidator.addErrorResponse(result, response);
		} else {
			Pagination pagination = utilService.setPageableParameters(pageable, loggedInUserId);
			pagination.setContactId(userId);
			String companyProfileName = pageable.getVendorCompanyProfileName();
			if (XamplifyUtils.isValidString(companyProfileName)) {
				Integer vendorCompanyId = userDAO.getCompanyIdByProfileName(companyProfileName);
				pagination.setVendorCompanyId(vendorCompanyId);
			}
			pagination.setIsCompanyJourney(isCompanyJourney);
			response.setData(activityDAO.fetchRecentActivities(pagination));
			XamplifyUtils.addSuccessStatus(response);
		}
		return response;
	}

	@Override
	public XtremandResponse fetchLogoFromExternalSource(Integer userId) {
		XtremandResponse response = new XtremandResponse();
		if (XamplifyUtils.isValidInteger(userId)) {
			String emailId = userDAO.getEmailIdByUserId(userId);
			if(XamplifyUtils.isValidString(emailId)) {
				String domain = XamplifyUtils.getEmailDomain(emailId);
				String iconUrl = getLogoFromExternalSourceUsingDomain(domain);
				response.setData(iconUrl);
				XamplifyUtils.addSuccessStatus(response);
			} else {
				XamplifyUtils.addErorMessageWithStatusCode(response, INVALID_INPUT_PARAMETERS, 401);
			}
		} else {
			XamplifyUtils.addErorMessageWithStatusCode(response, INVALID_INPUT_PARAMETERS, 401);
		}
		return response;
	}
	
	public JSONObject getLogoFromExternalSource(String domain) {
		JSONObject json = null;
		try {
			json = fetchLogoJsonResponse(domain, json);
		} catch (JSONException e) {
			logger.error("JSONException in getLogoFromExternalSource() with Message {}, Timestamp: {}", e.getMessage(), new Date());
		} catch (IOException ioe) {
			logger.error("IOException in getLogoFromExternalSource() with Message {}, Timestamp: {}", ioe.getMessage(), new Date());
		} catch (NullPointerException npe) {
			logger.error("NullPointerException in getLogoFromExternalSource() with Message {}, Timestamp: {}", npe.getMessage(), new Date());
		} catch (Exception e) {
			logger.error("Exception in getLogoFromExternalSource() with Message {}, Timestamp: {}", e.getMessage(), new Date());
		}
		return json;
	}

	private JSONObject fetchLogoJsonResponse(String domain, JSONObject json) throws IOException {
		if (XamplifyUtils.isValidString(domain)) {
			String uri = logoEndPoint + domain;
			logger.info("framed Brandfetch URI {} at {}",uri, new Date());
			
			HttpClient httpClient = HttpClientBuilder.create().build();
			HttpResponse response = null;
			
			HttpGet httpGet = new HttpGet(uri);
			httpGet.setHeader(HttpHeaders.ACCEPT, "application/json");
			response = httpClient.execute(httpGet);		
			
			if (response != null) {
				Integer statusCode = response.getStatusLine().getStatusCode();
				String responseString = EntityUtils.toString(response.getEntity());
				if (statusCode == 200 && XamplifyUtils.isValidString(responseString)) {
					JSONArray jsonArray = new JSONArray(responseString);
		            if (jsonArray.length() > 0) {
		                json = jsonArray.getJSONObject(0);
		            }
				}
				logger.info("Response status code: {}, Response json string: {}, Timestamp: {}", statusCode,
						responseString, new Date());
			} else {
				logger.info("Received Json response from Brandfetch is equals to NULL, Timestamp: {}", new Date());
			}
		}
		return json;
	}
	
	@Override
	public String getLogoFromExternalSourceUsingDomain(String domain) {
		JSONObject jsonObject = getLogoFromExternalSource(domain);
		String imageSrc = "";
		if (jsonObject != null) {
			imageSrc = jsonObject.optString("icon", "");
		}
		return imageSrc;
	}

}
