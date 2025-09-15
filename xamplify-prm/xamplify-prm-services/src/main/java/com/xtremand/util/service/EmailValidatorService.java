package com.xtremand.util.service;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EmailValidatorService {

	private static final Logger logger = LoggerFactory.getLogger(EmailValidatorService.class);

	private static final String ERROR = "error";

	@Value("${zerobounce_api_url}")
	String zeroBounceApiUrl;

	@Value("${zerobounce_api_access_key}")
	String zeroBounceApiAccessKey;

	@Value("${mailboxlayer_api_url}")
	String mailBoxLayerApiUrl;

	@Value("${mailboxlayer_api_access_key}")
	String mailBoxLayerApiAccessKey;

	@Value("${zerobounce.bulk.validate.api}")
	String zerobounceBulkValidateApi;

	public JSONObject validate(final String email, Integer userListId, String userListName, int usersSize, int counter)
			throws ParseException, IOException {
		JSONObject jsonObject = new JSONObject();
		String targetURL = zeroBounceApiUrl + "?api_key=" + zeroBounceApiAccessKey + "&email=" + email + "&ip_address=";
		CloseableHttpClient httpClient = HttpClientBuilder.create().build();
		HttpGet getRequest = null;
		try {
			getRequest = new HttpGet(targetURL);
			HttpResponse response = httpClient.execute(getRequest);
			int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode == 200) {
				HttpEntity entity = response.getEntity();
				String responseString = EntityUtils.toString(entity);
				jsonObject = new JSONObject(responseString);
			} else {
				jsonObject = new JSONObject();
				HttpEntity entity = response.getEntity();
				String responseString = EntityUtils.toString(entity);
				jsonObject.put(ERROR, responseString);
				String errorMessage = email + " could not proceesed" + responseString;
				logger.error(errorMessage);
				return jsonObject;
			}

		} catch (Exception ex) {
			jsonObject = new JSONObject();
			jsonObject.put(ERROR, ex.getMessage());
			return jsonObject;
		} finally {
			httpClient.close();
		}
		boolean isCounterMatched = counter > 0 && usersSize > 0 && counter == usersSize;
		if (isCounterMatched) {
			String matchedCountDebugMessage = "Total " + counter + "/" + usersSize + " are successfully validated.";
			logger.debug(matchedCountDebugMessage);
		}
		return jsonObject;

	}

	public JSONObject validateEmailIdsUsingZeroBounceAPI(CloseableHttpClient httpClient, int usersSize, int counter,
			String emailId) throws IOException {
		JSONObject jsonObject = new JSONObject();
		String targetURL = zeroBounceApiUrl + "?api_key=" + zeroBounceApiAccessKey + "&email=" + emailId
				+ "&ip_address=";
		HttpGet getRequest = null;
		try {
			getRequest = new HttpGet(targetURL);
			HttpResponse response = httpClient.execute(getRequest);
			int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode == 200) {
				HttpEntity entity = response.getEntity();
				String responseString = EntityUtils.toString(entity);
				jsonObject = new JSONObject(responseString);
			} else {
				jsonObject = new JSONObject();
				HttpEntity entity = response.getEntity();
				String responseString = EntityUtils.toString(entity);
				jsonObject.put(ERROR, responseString);
				String errorMessage = emailId + " could not proceesed" + responseString;
				logger.error(errorMessage);
			}

		} catch (Exception ex) {
			jsonObject = new JSONObject();
			jsonObject.put(ERROR, ex.getMessage());
		} finally {
			closeHttpClientAndPrintDebugMessage(httpClient, usersSize, counter);
		}

		return jsonObject;
	}

	private void closeHttpClientAndPrintDebugMessage(CloseableHttpClient httpClient, int newlyAddedEmailIdsSize,
			int emailIdCounter) throws IOException {
		boolean isCounterMatched = emailIdCounter > 0 && newlyAddedEmailIdsSize > 0
				&& emailIdCounter == newlyAddedEmailIdsSize;
		if (isCounterMatched) {
			httpClient.close();
			String matchedCountDebugMessage = "Total " + emailIdCounter + "/" + newlyAddedEmailIdsSize
					+ " are successfully validated.";
			logger.debug(matchedCountDebugMessage);
		}
	}

	public JSONObject validateOld(final String email, Integer userListId, String userListName, int usersSize,
			int counter) throws IOException {
		JSONObject jsonObject = new JSONObject();
		String targetURL = zeroBounceApiUrl + "?api_key=" + zeroBounceApiAccessKey + "&email=" + email + "&ip_address=";

		HttpClient httpClient = HttpClientBuilder.create().build();
		HttpGet httpGet = null;
		try {
			httpGet = new HttpGet(targetURL);
		} catch (Exception ex) {
			jsonObject = new JSONObject();
			jsonObject.put(ERROR, ex.getMessage());
			return jsonObject;
		}
		HttpResponse response = httpClient.execute(httpGet);
		int statusCode = response.getStatusLine().getStatusCode();
		if (statusCode == 200) {
			String responseString = EntityUtils.toString(response.getEntity());
			jsonObject = new JSONObject(responseString);
		} else {
			jsonObject = new JSONObject();
			jsonObject.put("error", EntityUtils.toString(response.getEntity()));
			String errorMessage = email + " could not proceesed" + EntityUtils.toString(response.getEntity());
			logger.error(errorMessage);
			return jsonObject;
		}
		return jsonObject;
	}

	public JSONObject batchValidate(final JSONArray emailJSONArray) throws ParseException, IOException {
		logger.debug("from batchValidate() of EmailValidatorService");
		JSONObject data = null;
		if (emailJSONArray.length() > 0) {
			JSONObject requestObject = new JSONObject();
			requestObject.put("api_key", zeroBounceApiAccessKey);
			requestObject.put("email_batch", emailJSONArray);

			HttpResponse response = null;
			try {
				HttpClient httpClient = HttpClientBuilder.create().build();
				HttpPost httpPost = new HttpPost(zerobounceBulkValidateApi);
				StringEntity body = new StringEntity(requestObject.toString(1));
				body.setContentType("application/json");
				httpPost.setEntity(body);
				response = httpClient.execute(httpPost);
				if (response != null) {
					int statusCode = response.getStatusLine().getStatusCode();
					if (statusCode == 200) {
						String responseString = EntityUtils.toString(response.getEntity());
						data = new JSONObject(responseString);
					} else {
						data = new JSONObject();
						data.put(ERROR, EntityUtils.toString(response.getEntity()));
						String errorMessage = "Batch validation failed." + EntityUtils.toString(response.getEntity());
						logger.error(errorMessage);
					}
				}

			} catch (JSONException | IOException | ParseException e) {
				data = new JSONObject();
				data.put(ERROR, true);
				data.put("statusCode", "401");
				data.put("message", "Resulted Error in Zero Bounce Batch Validate");
			}

		}
		return data;
	}

}
