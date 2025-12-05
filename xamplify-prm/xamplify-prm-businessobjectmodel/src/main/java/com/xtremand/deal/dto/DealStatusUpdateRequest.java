package com.xtremand.deal.dto;

import lombok.Data;

@Data
public class DealStatusUpdateRequest {

	private Integer dealId;
    private String pipelineStageName;

    private Integer partnerCompanyId;
    private String partnerUsername;
    private String partnerEmailId;

    private String vendorUsername;
    private String vendorEmailId;
    
}
