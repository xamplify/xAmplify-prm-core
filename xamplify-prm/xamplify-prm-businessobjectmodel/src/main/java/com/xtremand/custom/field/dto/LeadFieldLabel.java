package com.xtremand.custom.field.dto;

public enum LeadFieldLabel {
	LAST_NAME("Last_Name"), FIRST_NAME("First_Name"), TITLE("Title"), COMPANY("Company"), STREET("Address"),
	CITY("City"), STATE("State"), POSTAL_CODE("Postal_Code"), COUNTRY("Country"), EMAIL("Email"), WEBSITE("Website"),
	PHONE_NUMBER("Phone_Number"), INDUSTRY("Industry"), REGION("Region"), DEAL_NAME("Deal_Name"), AMOUNT("Amount"), CLOSE_DATE("Close_Date"),
	CRM_ID("crm_id_c_xamp"),LEAD_ID("lead_id_c_xamp"),CAMPAIGN_NAME("campaign_name_c_xamp"),ADDED_FOR_COMPANY_NAME("added_for_company_c_xamp"),
	PARENT_CAMPAIGN_NAME("parent_campaign_name_c_xamp"),ADDED_BY_COMPANY("added_by_company_c_xamp"),ADDED_BY_NAME("added_by_name_c_xamp"),
	ADDED_BY_EMAIL_ID("added_by_email_id_c_xamp"),ADDED_ON_DATE("added_on_date_string_c_xamp"),CURRENT_STAGE_NAME("current_stage_name_c_xamp")
	,LASTNAME("LastName"),FIRSTNAME("FirstName"),PHONE("Phone"),XAMPLIFY_LEAD_REGISTERED_DATE("xAmplify_Lead_Registered_Date_c"),ACCOUNT_OWNER("Account_Owner"),
	PARTNER_TYPE("Partner_Type"),ACCOUNT_SUBTYPE("Account_Sub_Type");

	private final String labelId;

	LeadFieldLabel(String labelId) {
		this.labelId = labelId;
	}

	public String getLabelId() {
		return labelId;
	}

	public static LeadFieldLabel fromString(String labelId) {
		for (LeadFieldLabel label : values()) {
			if (label.labelId.equals(labelId)) {
				return label;
			}
		}
		return null;
	}
}
