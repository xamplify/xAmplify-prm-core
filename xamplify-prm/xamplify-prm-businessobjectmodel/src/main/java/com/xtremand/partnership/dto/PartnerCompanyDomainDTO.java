package com.xtremand.partnership.dto;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

@Data
public class PartnerCompanyDomainDTO implements Serializable {
	
	private static final long serialVersionUID = 5690921225262811443L;
	
	private Integer id;
	private Integer partnershipId;
	private String partnerCompanyDomainName;
	private Integer createdBy;
	private Integer updatedBy;
	private Date createdTime;
	private Date updatedTime;
	private Integer companyId;
	
}
