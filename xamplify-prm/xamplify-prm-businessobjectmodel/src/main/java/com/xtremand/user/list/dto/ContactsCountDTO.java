package com.xtremand.user.list.dto;

import lombok.Data;

@Data
public class ContactsCountDTO {
	private Integer allCounts;
	private Integer activeCount;
	private Integer inActiveCount;
	private Integer inValidCount;
	private Integer unSubscribedCount;
	private Integer excludedCount;
	private Integer validCount;
	private Integer deactivatedCount;
}
