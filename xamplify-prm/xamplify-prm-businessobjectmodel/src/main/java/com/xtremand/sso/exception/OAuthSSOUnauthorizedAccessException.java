package com.xtremand.sso.exception;

public class OAuthSSOUnauthorizedAccessException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	private Integer statusCode;
	
	public OAuthSSOUnauthorizedAccessException() {
		super();
	}
	
	public OAuthSSOUnauthorizedAccessException(String message) {
        super(message);
    }

    public OAuthSSOUnauthorizedAccessException(String message, Throwable cause) {
        super(message, cause);
    }

    public OAuthSSOUnauthorizedAccessException(Throwable cause) {
        super(cause);
    }
    
    public OAuthSSOUnauthorizedAccessException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public OAuthSSOUnauthorizedAccessException(String message, Integer statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public Integer getStatusCode() {
        return statusCode;
    }
}
