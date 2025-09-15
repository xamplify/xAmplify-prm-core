package com.xtremand.dashboard.buttons.dto;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import com.xtremand.util.dto.CreatedTimeConverter;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class DashboardButtonsDTO extends CreatedTimeConverter implements Serializable {

	private static final long serialVersionUID = 1L;

	private Integer id;
	private Integer vendorId;
	private String companyProfileName;
	private String buttonTitle;
	private String buttonSubTitle;
	private String buttonDescription;
	private String buttonLink;
	private String buttonIcon;
	private boolean openInNewTab;
	private String openInNewTabTarget = "";
	/**** XNFR-571 *****/
	private boolean partnerGroupSelected;
	private Set<Integer> partnerGroupIds;
	private Set<Integer> partnerIds;
	private Set<Integer> partnershipIds;
	private boolean publishingInProgress;
	private boolean published;
	private boolean partnerGroupIdsMatched;
	private boolean partnerIdsMatched;
	private boolean publishingFromEditSection;
	private Set<Integer> publishedPartnerGroupIds;
	private Set<Integer> publishedPartnerIds;
	private Set<Integer> publishedPartnershipIds;
	private Integer orderId;
	private List<Integer> publishedPartnerUserIds;
	/**** XNFR-571 *****/
	/**** XNFR-880 *****/
	private String alternateUrl;
	private List<DashboardAlternateUrlDTO> alternateUrls;
	

}
