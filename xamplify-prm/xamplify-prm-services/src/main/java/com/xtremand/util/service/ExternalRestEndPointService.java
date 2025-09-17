package com.xtremand.util.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import javax.net.ssl.HttpsURLConnection;

import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.xtremand.exception.ExternalRestApiServiceException;
import com.xtremand.external.api.request.dto.ExternalApiRequestDTO;
import com.xtremand.external.api.request.dto.ExternalApiResponseDTO;
import com.xtremand.util.XamplifyUtil;

@Service
public class ExternalRestEndPointService {

	private static final Logger logger = LoggerFactory.getLogger(ExternalRestEndPointService.class);



	@Value("${processing.error.message}")
	private String processingErrorMessage;

	@Autowired
	private XamplifyUtil xamplifyUtil;

	public ExternalApiResponseDTO callExternalRestEndPoint(ExternalApiRequestDTO extrenalApiRequestDTO) {
		ExternalApiResponseDTO externalApiResponseDTO = new ExternalApiResponseDTO();
		InputStream inStream = null;
		HttpsURLConnection connection = null;
		String endPoint = extrenalApiRequestDTO.getEndPoint();
		try {
			URL url = new URL(endPoint);
			connection = (HttpsURLConnection) url.openConnection();
			connection.setRequestMethod(extrenalApiRequestDTO.getRequestType());
			connection.setRequestProperty("Content-Type", "application/" + "json");
			connection.setRequestProperty("Authorization", "Bearer " + extrenalApiRequestDTO.getBearerToken());
			connection.setRequestProperty("Accept", "application/json");
			connection.setRequestProperty("OpenAI-Beta", "assistants=v2");
			if ("POST".equalsIgnoreCase(extrenalApiRequestDTO.getRequestType())) {
				connection.setDoOutput(true);
				try (OutputStream os = connection.getOutputStream();
						OutputStreamWriter writer = new OutputStreamWriter(os, StandardCharsets.UTF_8)) {
					writer.write(extrenalApiRequestDTO.getRequestBodyJsonString());
					writer.flush();
				}
			}
			int statusCode = connection.getResponseCode();
			externalApiResponseDTO.setStatusCode(statusCode);
			if (statusCode == 200) {
				inStream = connection.getInputStream();
			} else {
				inStream = connection.getErrorStream();
			}
			externalApiResponseDTO.setApiResponse(xamplifyUtil.convertStreamToJSONObject(inStream));
		} catch (MalformedURLException e) {
			String errorMessage = "MalformedURLException(" + endPoint + ")";
			logger.error(errorMessage, e);
			throw new ExternalRestApiServiceException(processingErrorMessage);
		} catch (ProtocolException e) {
			String errorMessage = "ProtocolException(" + endPoint + ")";
			logger.error(errorMessage, e);
			throw new ExternalRestApiServiceException(processingErrorMessage);
		} catch (IOException e) {
			String errorMessage = "IOException(" + endPoint + ")";
			logger.error(errorMessage, e);
			throw new ExternalRestApiServiceException(processingErrorMessage);
		} catch (ParseException e) {
			String errorMessage = "ParseException(" + endPoint + ")";
			logger.error(errorMessage, e);
			throw new ExternalRestApiServiceException(processingErrorMessage);
		} finally {
			closeInputStream(inStream, endPoint);
			closeConnection(connection);
		}
		return externalApiResponseDTO;
	}

	private void closeConnection(HttpsURLConnection connection) {
		if (connection != null) {
			connection.disconnect();
		}
	}

	private void closeInputStream(InputStream inStream, String endpointUrl) {
		if (inStream != null) {
			try {
				inStream.close();
			} catch (IOException e) {
				String errorMessage = "IOException In Closing Input Stream()(" + endpointUrl + ")";
				logger.error(errorMessage, e);
				throw new ExternalRestApiServiceException(processingErrorMessage);
			}
		}
	}
}
