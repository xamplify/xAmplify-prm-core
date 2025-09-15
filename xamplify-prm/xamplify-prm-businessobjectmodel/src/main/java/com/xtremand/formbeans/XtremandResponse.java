package com.xtremand.formbeans;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class XtremandResponse implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private int statusCode;
	
	private String message;
	
	private Object data;
	
        private Map<String, Object> map;

        private List<ErrorResponse> errorResponses;

       private Map<String, String> errors;
	
	private boolean access;	
	
	private String filePath;
	
	private String captchaSiteKey;
	

	public XtremandResponse() {
		super();
	}

	public int getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public Map<String, Object> getMap() {
		return map;
	}

	public void setMap(Map<String, Object> map) {
		this.map = map;
	}

        public List<ErrorResponse> getErrorResponses() {
                return errorResponses;
        }

        public void setErrorResponses(List<ErrorResponse> errorResponses) {
                this.errorResponses = errorResponses;
        }

       public Map<String, String> getErrors() {
               return errors;
       }

       public void setErrors(Map<String, String> errors) {
               this.errors = errors;
       }

	public boolean isAccess() {
		return access;
	}

	public void setAccess(boolean access) {
		this.access = access;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public String getCaptchaSiteKey() {
		return captchaSiteKey;
	}

	public void setCaptchaSiteKey(String captchaSiteKey) {
		this.captchaSiteKey = captchaSiteKey;
	}

	
	
}
