package com.xtremand.godaddy.configuration.service;

import com.xtremand.formbeans.XtremandResponse;
import com.xtremand.godaddy.dto.GodaddyDnsRecordDetailsDto;

public interface GoDaddyConfigurationService {

	XtremandResponse addDnsRecordOfGodaddy(GodaddyDnsRecordDetailsDto record);

	XtremandResponse domainNameValidation(GodaddyDnsRecordDetailsDto record);

	XtremandResponse updateGodaddyConfiguration(Integer companyId,boolean isConnected);

	XtremandResponse isGodaddyConfigured(Integer companyId);

	boolean fetchExistingDnsRecords(String value);

	XtremandResponse getDomainName(Integer companyId);
	
	XtremandResponse getDnsRecordsOfGodaddy();

	XtremandResponse deleteAllDnsRecordsByTypeAndName();

}
