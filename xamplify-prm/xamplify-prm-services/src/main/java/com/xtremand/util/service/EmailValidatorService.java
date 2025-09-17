package com.xtremand.util.service;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
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

}
