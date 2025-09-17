package com.xtremand.deal.registration;

public enum CampaignDealStatusEnum {
    OPENED("OPENED"), HOLD("HOLD"),APPROVED("APPROVED"), REJECTED("REJECTED");

    protected String dealStatus;

    private CampaignDealStatusEnum(String dealStatus) {
        this.dealStatus = dealStatus;
    }

	public String getDealStatus() {
		return dealStatus;
	}


    
}
