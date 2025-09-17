package com.xtremand.mdf.dto;

import java.io.Serializable;

import com.xtremand.mdf.bom.MdfWorkFlowStepType;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

@Data
public class MdfRequestViewDTO implements Serializable {/**
	 * 
	 */
	private static final long serialVersionUID = 310073753815194187L;
	
	
	private Integer id;
	
	@Setter(value = AccessLevel.NONE)
	private Double allocationAmount;
	
	private String allocationDateInString;
	
	private String allocationExpirationDateInString;
	
	@Setter(value = AccessLevel.NONE)
	private Double reimbursementAmount;
	
	private String description;
	
	private Integer statusInInteger;
	
	private String mdfWorkFlowStepTypeInString;
	
	private MdfWorkFlowStepType mdfWorkFlowStepType;
	
	private Integer createdBy;
	
	private String requestCreatedDateInString;
	
	private String title;
	
	@Setter(value = AccessLevel.NONE)
	private Double requestAmount;
	
	private String eventDateInString;
	
	private Integer loggedInUserId;
	
	private Double sumOfAllocationAmount;
	
	private Double sumOfReimbursementAmount;
	
	@Setter(value = AccessLevel.NONE)
	private Integer partnershipId;

	
	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public void setAllocationAmount(Double allocationAmount) {
		if(allocationAmount==null) {
			this.allocationAmount = Double.valueOf(0);
		}else {
			this.allocationAmount = allocationAmount;
		}
	}

	public void setRequestAmount(Double requestAmount) {
		if(requestAmount==null) {
			this.requestAmount = Double.valueOf(0);
		}else {
			this.requestAmount = requestAmount;
		}
	}

	public void setReimbursementAmount(Double reimbursementAmount) {
		if(reimbursementAmount==null) {
			this.reimbursementAmount = Double.valueOf(0);
		}else {
			this.reimbursementAmount = reimbursementAmount;
		}
	}

	public void setPartnershipId(Integer partnershipId) {
		if(partnershipId!=null) {
			this.partnershipId = partnershipId;
		}else {
			this.partnershipId = 0;
		}
		
	}

	
	

}
