package com.xtremand.partner.journey.bom;

public enum TriggerActionEnum {
	activated("activated"),
	signed_up("signed_up"),
	redistributed_campaign("redistributed_campaign"),
	created_company_profile("created_company_profile"),
	created_lead("created_lead"),
	created_deal("created_deal"),
	converted_lead("converted_lead"),
	closed_deal("closed_deal"),
	added_team_member("added_team_member"),
	added_contact("added_contact"),
	completed_track("completed_track"),
	completed_playbook("completed_playbook"),
	viewed_track("viewed_track"),
	viewed_playbook("viewed_playbook"),
	viewed_pages("viewed_pages"),
	requested_mdf("requested_mdf"),
	redistributed_sharelead("redistributed_sharelead");

	protected String type;
	TriggerActionEnum(String type) {
		this.type = type;
	}

	public String getType() {
		return type;
	}
}
