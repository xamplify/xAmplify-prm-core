package com.xtremand.util;

import org.springframework.http.HttpHeaders;

import com.xtremand.common.bom.StatusCode;

public class HttpHeaderUtil {
	
	public static HttpHeaders getHeader(StatusCode statusCode){
		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.set(String.valueOf(statusCode.getCode()), statusCode.getMessage());
		return responseHeaders;
	}

}
