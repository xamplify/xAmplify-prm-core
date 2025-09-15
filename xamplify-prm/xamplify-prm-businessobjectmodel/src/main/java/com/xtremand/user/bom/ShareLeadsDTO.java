package com.xtremand.user.bom;

import java.math.BigInteger;
import java.util.Date;
import java.util.Set;

import com.xtremand.util.bom.DateUtils;

import lombok.Data;

@Data
public class ShareLeadsDTO {
	
	private Integer userId;
	private Integer userListId;
	private Set<Integer> partnerIds;
	private String listName;
	
	//******START: XNFR-316
	private Date createdTime;
	private String createdTimeInUTC;
	private Date assignedDate;
	private String assignedDateInUTC;
	private String emailId;
	private BigInteger shareLeadCount;	
	private String firstName;
	private String lastName;
	private String mobileNumber;
	private DateUtils dateUtils = new DateUtils();
	private String companyName;
	private Integer companyId;
	private String status;
	private String vendorCompany;
	private String vendorTeamMemberEmailId;
	private String fullName;
	
	public void setCreatedTime(Date createdTime) {
		this.createdTime = createdTime;
		if (createdTime != null) {
			setCreatedTimeInUTC(dateUtils.getUTCString(createdTime));
		}
	}
	
	public void setAssignedDate(Date assignedDate) {
		this.assignedDate = assignedDate;
		if (assignedDate != null) {
			setAssignedDateInUTC(dateUtils.getUTCString(assignedDate));
		}
	}
	//******START: XNFR-316
}
