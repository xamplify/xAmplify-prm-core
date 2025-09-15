package com.xtremand.util;

import java.util.Collections;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.xtremand.common.bom.StatusCode;
import com.xtremand.formbeans.XtremandResponse;

public class ResponseUtil {
	@SuppressWarnings({ "rawtypes" })
	public static ResponseEntity getResponse(HttpStatus status, int statuscode, Object body){
		StatusCode statusCode = StatusCode.getstatuscode(statuscode);
		if(statusCode!=null){
			body = body!=null?body:Collections.singletonMap("message", statusCode.getMessage());
			return ResponseEntity.status(status).headers(HttpHeaderUtil.getHeader(statusCode)).body(body);
		}else{
			throw new ResponseUtilException("Status Code Not Found For "+statuscode);
		}
		
	}
	
	
	public static XtremandResponse getAccessDeniedResponse(XtremandResponse response){
		return setResponse(403, "You do not have permission to view this.", response);
	}
	
	public static XtremandResponse getPageNotFoundResponse(XtremandResponse response){
		return setResponse(404, "No Data Found", response);
	}
	
	public static XtremandResponse setResponse(int statusCode,String message,XtremandResponse response){
		response.setStatusCode(statusCode);
		response.setMessage(message);
		return response;
	}
	
}
