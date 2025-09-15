package com.xtremand.partner.journey.bom;

public enum TriggerTimePhraseEnum {
	in_the_past_day(1), 
	in_the_past_7_days(7),
	in_the_past_14_days(14),
	in_the_past_28_days(28),
	in_the_past_30_days(30),
	in_the_past_60_days(60),
	in_the_past_90_days(90),
	in_the_past_year(365),
	custom(0);

	protected int value;
	TriggerTimePhraseEnum(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}
}
