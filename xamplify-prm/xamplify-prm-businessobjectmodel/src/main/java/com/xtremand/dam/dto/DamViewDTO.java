package com.xtremand.dam.dto;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Set;

import lombok.Data;

@Data
public class DamViewDTO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8104159032631423439L;

	private String assetName;

	private String description;

	private String thumbnailPath;

	private String assetPath;

	private boolean beeTemplate;

	private Date publishedTime;

	private Set<Integer> tagIds;

	private Integer categoryId;

	private boolean historyTemplate;

	/***** XNFR-255 ****/
	private boolean shareAsWhiteLabeledAsset;

	private String whiteLabeledToolTipMessage;

	private boolean disableWhiteLabelOption;

	private boolean publishedToPartnerGroups;

	private List<Integer> partnerGroupIds;

	private List<Integer> partnerIds;

	private List<Integer> partnershipIds;

	private boolean published;

	/***** XNFR-255 ****/

	/*** XNFR-434 ****/
	private String assetType;

	private boolean addedToQuickLinks;

	/*** XNFR-833 ****/
	private boolean partnerSignatureRequired;

	private boolean vendorSignatureRequired;

	private String partnerSignatureToolTipMessage;

	private boolean disablePartnerSignatureOption;

	private String vendorSignatureToolTipMessage;

	private boolean disableVendorSignatuerOption;

	private boolean vendorSignatureCompleted;

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	private String approvalStatus;

	private boolean draft;

	private boolean createdByAnyApprover;

	private String jsonBody;

	private String htmlBody;

	private boolean vendorSignatureRequiredAfterPartnerSignature;

	private String vendorSignatureRequiredAfterPartnerSignatureToolTipMessage;

	private boolean disablevendorSignatureRequiredAfterPartnerSignatureOption;

	/***** XNFR-955 ****/
	private String slug;

}
