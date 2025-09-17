package com.xtremand.formbeans;

import java.util.List;

import lombok.Data;

@Data
public class VendorInvitationDTO {
	public List<String> emailIds;
	public String subject;
	public String message;
	public String vanityURL;
	private Integer teamMemberGroupId;
}
