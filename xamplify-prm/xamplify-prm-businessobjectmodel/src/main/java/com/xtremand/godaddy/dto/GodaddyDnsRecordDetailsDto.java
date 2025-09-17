package com.xtremand.godaddy.dto;

import org.apache.http.ssl.TrustStrategy;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GodaddyDnsRecordDetailsDto {
	public static final TrustStrategy INSTANCE = null;
	
	private String type;
	
	private String name;
	
	private String data;
	
	private String domainName;
	
	private String apiKey;
	
	private String apiSecret;
}
