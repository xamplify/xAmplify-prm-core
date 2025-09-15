package com.xtremand.external.api.request.dto;

import java.io.IOException;
import java.io.Serializable;

import org.json.simple.JSONObject;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Data;

@Data
public class ExternalApiResponseDTO implements Serializable {
	/**
	* 
	*/
	private static final long serialVersionUID = -4300821472419405605L;

	private JSONObject apiResponse;

	private int statusCode;

	@JsonIgnore
	private Class<?> successClassInstance;

	@JsonIgnore
	private Class<?> errorClassInstance;

	public Class<?> mapJsonResponseToDTO(JSONObject apiResponse, int statusCode) throws IOException {
		boolean isValidApiResponse = apiResponse != null;
		Class<?> clazz;
		if (statusCode == 200) {
			clazz = successClassInstance;
		} else {
			clazz = errorClassInstance;
		}
		if (isValidApiResponse && clazz != null) {
			byte[] jsonData = apiResponse.toString().getBytes();
			ObjectMapper mapper = new ObjectMapper();
			return (Class<?>) mapper.readValue(jsonData, clazz);
		} else {
			return null;
		}

	}

}
