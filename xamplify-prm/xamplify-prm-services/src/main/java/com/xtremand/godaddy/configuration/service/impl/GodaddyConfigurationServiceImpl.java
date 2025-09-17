package com.xtremand.godaddy.configuration.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.xtremand.formbeans.XtremandResponse;
import com.xtremand.godaddy.configuration.service.GoDaddyConfigurationService;
import com.xtremand.godaddy.dao.GoDaddyConfigurationDao;
import com.xtremand.godaddy.dto.GodaddyDnsRecordDetailsDto;

@Service
public class GodaddyConfigurationServiceImpl implements GoDaddyConfigurationService {

	private static String API_KEY = "";

	private static String API_SECRET = "";

	private static String DOMAIN_NAME = "";

	private static String TYPE = "TXT";

	private static String NAME = "@";

	private final String API_BASE_URL = "https://api.godaddy.com/v1/domains/";

	@Autowired
	private GoDaddyConfigurationDao goDaddyDao;

	@Override
	public XtremandResponse addDnsRecordOfGodaddy(GodaddyDnsRecordDetailsDto record) {
		XtremandResponse response = new XtremandResponse();
		try {
			HttpHeaders headers = godaddyHeader(API_KEY, API_SECRET);
			String API_ENDPOINT = API_BASE_URL + DOMAIN_NAME + "/records";
			String requestJson = "[{\"type\":\"" + record.getType() + "\",\"name\":\"" + record.getName()
					+ "\",\"data\":\"" + record.getData() + "\"}]";
			HttpEntity<?> requestEntity = new HttpEntity<>(requestJson, headers);
			if (!fetchExistingDnsRecords(record.getData())) {
				ResponseEntity<String> responseEntity = new RestTemplate(new HttpComponentsClientHttpRequestFactory())
						.exchange(API_ENDPOINT, HttpMethod.PATCH, requestEntity, String.class);
				response.setStatusCode(responseEntity.getStatusCodeValue());
				response.setMessage("Dns Record Added Successfully");
			} else {
				response.setStatusCode(422);
				response.setMessage("DNS Record was Dulicated.");
			}
		} catch (HttpClientErrorException e) {
			if (record.getApiKey() == null || record.getApiSecret() == null) {
				response.setStatusCode(401);
				response.setMessage("Unauthorized : Credentials must be specified");
			} else if (fetchExistingDnsRecords(record.getData())) {
				response.setStatusCode(422);
				response.setMessage("DNS Record was Dulicated.");
			} else {
				response.setStatusCode(500);
				response.setMessage("Internal Server Error");
			}
		} catch (HttpServerErrorException e) {
			response.setStatusCode(500);
			response.setMessage("Internal Server Error");
		} catch (RestClientException e) {
			response.setStatusCode(500);
			response.setMessage("Internal Server Error");
		}

		return response;
	}

	private HttpHeaders godaddyHeader(String apiKey, String apiSecret) {
		HttpHeaders headers = new HttpHeaders();
		headers.set("Authorization", "sso-key " + apiKey + ":" + apiSecret);
		headers.setContentType(MediaType.APPLICATION_JSON);
		return headers;
	}

	@Override
	public boolean fetchExistingDnsRecords(String value) {
		String DNS_GET_API = API_BASE_URL + "/" + DOMAIN_NAME + "/records/" + TYPE + "/" + NAME;
		HttpClient httpClient = HttpClients.createDefault();
		HttpGet httpGet = new HttpGet(DNS_GET_API);
		httpGet.setHeader("Authorization", "sso-key " + API_KEY + ":" + API_SECRET);
		httpGet.setHeader("Content-Type", "application/json");
		try {
			HttpResponse response = httpClient.execute(httpGet);
			String responseBody = EntityUtils.toString(response.getEntity());
			JSONArray array = new JSONArray(responseBody);
			for (int i = 0; i < array.length(); i++) {
				JSONObject existingRecord = array.getJSONObject(i);
				String existingName = existingRecord.getString("name");
				String existingType = existingRecord.getString("type");
				String existingData = existingRecord.getString("data");

				if (existingName.equals(NAME) && existingType.equals(TYPE)
						&& (existingData.equals(value) || existingData.contains("v=spf1"))) {
					return true; // Found a duplicate record
				}
			}
		} catch (Exception e) {
			return false;
		}
		return false; // No duplicate record found
	}

	@Override
	public XtremandResponse domainNameValidation(GodaddyDnsRecordDetailsDto record) {
		XtremandResponse response = new XtremandResponse();
		API_KEY = record.getApiKey();
		API_SECRET = record.getApiSecret();
		DOMAIN_NAME = record.getDomainName();
		HttpClient httpClient = HttpClients.createDefault();
		// Create an HTTP GET request to GoDaddy's API
		String API_ENDPOINT = API_BASE_URL + DOMAIN_NAME;
		HttpGet httpGet = new HttpGet(API_ENDPOINT);
		// Set API credentials in the request headers
		httpGet.setHeader("Authorization", "sso-key " + API_KEY + ":" + API_SECRET);
		try {
			// Execute the request
			HttpResponse response1 = httpClient.execute(httpGet);
			// Check the response status code
			int statusCode = response1.getStatusLine().getStatusCode();
			if (statusCode == 200) {
				response.setStatusCode(200);
				response.setMessage("Record Found");
			} else if (statusCode == 401) {
				response.setStatusCode(401);
				response.setMessage(
						"Could not authenticate. You entered an incorrect API Key or Secret Code. Please try again.");
			} else if (statusCode == 404) {
				response.setStatusCode(404);
				response.setMessage("Domain " + DOMAIN_NAME + "  Invalid");
			}
		} catch (IOException e) {
			response.setStatusCode(500);
			response.setMessage("Internal Server Error");
		}
		return response;
	}

	@Override
	public XtremandResponse updateGodaddyConfiguration(Integer companyId, boolean isConnected) {
		XtremandResponse response = new XtremandResponse();
		if (companyId != null && companyId > 0) {
			goDaddyDao.updateGodaddyConfiguration(companyId, isConnected, DOMAIN_NAME);
		}
		response.setStatusCode(200);
		return response;
	}

	@Override
	public XtremandResponse isGodaddyConfigured(Integer companyId) {
		XtremandResponse response = new XtremandResponse();
		response.setStatusCode(200);
		if (companyId != null && companyId > 0) {
			response.setData(goDaddyDao.isGodaddyConfigured(companyId));
		} else {
			response.setData(false);
		}
		return response;

	}

	@Override
	public XtremandResponse getDomainName(Integer companyId) {
		XtremandResponse response = new XtremandResponse();
		response.setStatusCode(200);
		if (companyId != null && companyId > 0) {
			response.setData(goDaddyDao.getDomainName(companyId));
		} else {
			response.setData("");
		}
		return response;
	}

	// Show Dns Records
	@Override
	public XtremandResponse getDnsRecordsOfGodaddy() {
		XtremandResponse responseResult = new XtremandResponse();
		Map<String, Object> map = new HashMap<String, Object>();
		String modifiedSPFRecord = null;
		String DNS_GET_API = API_BASE_URL + "/" + DOMAIN_NAME + "/records/" + TYPE + "/" + NAME;
		HttpClient httpClient = HttpClients.createDefault();
		HttpGet httpGet = new HttpGet(DNS_GET_API);
		httpGet.setHeader("Authorization", "sso-key " + API_KEY + ":" + API_SECRET);
		httpGet.setHeader("Content-Type", "application/json");
		List<GodaddyDnsRecordDetailsDto> dtoList = new ArrayList<GodaddyDnsRecordDetailsDto>();
		try {
			HttpResponse response = httpClient.execute(httpGet);
			String responseBody = EntityUtils.toString(response.getEntity());
			JSONArray array = new JSONArray(responseBody);
			for (int i = 0; i < array.length(); i++) {
				JSONObject existingRecord = array.getJSONObject(i);
				String existingName = existingRecord.getString("name");
				String existingType = existingRecord.getString("type");
				String existingData = existingRecord.getString("data");
				GodaddyDnsRecordDetailsDto godaddyDto = new GodaddyDnsRecordDetailsDto();
				godaddyDto.setData(existingData);
				godaddyDto.setName(existingName);
				godaddyDto.setType(existingType);
				dtoList.add(godaddyDto);
				// Find the position of "~all" in the SPF record
				String allString = "~all";
				int index = godaddyDto.getData().indexOf("~all");
				int passIndex = godaddyDto.getData().indexOf("+all");
				int failIndex = godaddyDto.getData().indexOf("-all");
				// int neutralIndex = godaddyDto.getData().indexOf("-all");
				String stringToAdd = "include:u10208008.wl009.sendgrid.net";
				int spfIndex = godaddyDto.getData().indexOf("v=spf1");
				if ((index != -1 || passIndex != -1 || failIndex != -1) && spfIndex != -1) {
					modifiedSPFRecord = getSuggestedValueMethode(modifiedSPFRecord, godaddyDto, allString, index,
							passIndex, failIndex, stringToAdd);
				}
			}
			map.put("data", dtoList);
			map.put("suggest", modifiedSPFRecord);
			responseResult.setStatusCode(200);
			responseResult.setData(map);
		} catch (Exception e) {
			responseResult.setStatusCode(400);
		}
		return responseResult;
	}

	private String getSuggestedValueMethode(String modifiedSPFRecord, GodaddyDnsRecordDetailsDto godaddyDto,
			String allString, int index, int passIndex, int failIndex, String stringToAdd) {
		if (!godaddyDto.getData().contains(stringToAdd)) {
			if (index != -1) {
				modifiedSPFRecord = godaddyDto.getData().substring(0, index).trim() + " " + stringToAdd + " "
						+ allString;
			} else if (passIndex != -1) {
				modifiedSPFRecord = godaddyDto.getData().substring(0, passIndex).trim() + " " + stringToAdd + " "
						+ allString;
			} else if (failIndex != -1) {
				modifiedSPFRecord = godaddyDto.getData().substring(0, failIndex).trim() + " " + stringToAdd + " "
						+ allString;
			}
		} else {
			if (index != -1) {
				modifiedSPFRecord = godaddyDto.getData().substring(0, index).trim() + " " + allString;
			} else if (passIndex != -1) {
				modifiedSPFRecord = godaddyDto.getData().substring(0, passIndex).trim() + " " + allString;
			} else if (failIndex != -1) {
				modifiedSPFRecord = godaddyDto.getData().substring(0, failIndex).trim() + " " + allString;
			}
		}
		return modifiedSPFRecord;
	}

	// delete dns records
	@Override
	public XtremandResponse deleteAllDnsRecordsByTypeAndName() {
		XtremandResponse response = new XtremandResponse();
		String DNS_GET_API = API_BASE_URL + "/" + DOMAIN_NAME + "/records/" + TYPE + "/" + NAME;
		HttpClient httpClient = HttpClients.createDefault();
		try {
			HttpDelete httpDelete = new HttpDelete(DNS_GET_API);
			httpDelete.addHeader("Authorization", "sso-key " + API_KEY + ":" + API_SECRET);
			HttpResponse result = httpClient.execute(httpDelete);
			int statusCode = result.getStatusLine().getStatusCode();
			if (statusCode == 204) {
				response.setStatusCode(204);
				response.setMessage("deleted All records sucessfully.");
			} else {
				response.setStatusCode(404);
				response.setMessage("The given domain is not registered, or does not have a zone file.");
			}
		} catch (IOException e) {
			response.setStatusCode(500);
			response.setMessage("Internal Server Error");
		}
		return response;
	}

}
