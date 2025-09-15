package com.xtremand.external.api.request.dto;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExternalApiRequestDTO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String endPoint;

	private String bearerToken;

	private String requestType;

	private Object requestBodyClassInstance;
	
	private String requestBodyJsonString;

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

//	public String getRequestBodyJsonString() {
//		String jsonString = "";
//		try {
//			if (requestBodyClassInstance != null) {
//				ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
//				jsonString = ow.writeValueAsString(requestBodyClassInstance);
//			} else {
//				jsonString = "";
//			}
//		} catch (JsonProcessingException e) {
//			e.printStackTrace();
//		}
//		return jsonString;
//
//	}

}
