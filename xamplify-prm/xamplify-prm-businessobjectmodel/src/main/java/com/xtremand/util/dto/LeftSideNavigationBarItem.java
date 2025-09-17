package com.xtremand.util.dto;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.xtremand.team.member.dto.RoleDisplayDTO;

import lombok.Data;

@Data
public class LeftSideNavigationBarItem {

	private boolean partners;

	private boolean content;

	private boolean design;

	private boolean forms;

	private boolean team;

	private boolean stats;

	private boolean enableLeads;

	private boolean mdf;

	private boolean mdfAccessAsPartner;

	private boolean dam;

	private boolean damAccessAsPartner;

	private boolean onlyPartnerRole;

	private boolean partnershipEstablishedOnlyWithPrmAndLoggedInAsPartner;

	private boolean partnershipEstablishedOnlyWithPrm;

	private boolean shareLeads;

	private boolean sharedLeads;

	private boolean folders;

	private boolean opportunityDeals;

	private boolean opportunityLeadsAndDeals;

	private boolean opportunityLeadsAndDealsAccessAsPartner;

	private RoleDisplayDTO roleDisplayDto;

	private boolean lms;

	private boolean lmsAccessAsPartner;

	private boolean playbook;

	private boolean playbookAccessAsPartner;

	private Integer userId;

	private boolean accountDashboard;

	private boolean videos;

	private boolean opportunities;

	private boolean opportunitiesAccessAsPartner;

	private boolean companyProfileCreated;

	private boolean notifyPartners;

	private boolean onlyPartnerCompany;

	private boolean showAddLeadsAndDealsOptionsInDashboard;

	private Set<ModuleCustomDTO> moduleNames = new HashSet<>();

	private boolean loggedInThroughVendorVanityUrl;

	private boolean loggedInThroughOwnVanityUrl;

	private boolean loggedInThroughXamplifyUrl;

	private boolean adminOrSuperVisor;

	private boolean deletedPartner;

	private boolean loginAs;

	private boolean prmDashboard;

	private boolean admin;

	private boolean partnerAdmin;

	private boolean teamMember;

	private boolean partnerCompany;

	private boolean adminAndPartnerCompany;

	/******** XNFR-219 *****/
	private Integer companyId;

	private Integer vendorCompanyId;

	/******** XNFR-219 *****/

	/******** XNFR-224 ****/
	private boolean loginAsPartner;

	private boolean loginAsPartnerOptionEnabledForVendor;

	private Integer loginAsUserId;

	private Integer loginAsUserCompanyId;
	/******** XNFR-224 ****/
	private boolean showAddLeadOrDealButtonInMyProfileSection;

	private boolean navigateToPartnerViewSection;

	private List<ModuleCustomDTO> menuItems = new ArrayList<>();

	/**** user guide ********/
	private boolean configuration;

	/**** XNFR-326 *****/
	private boolean emailNotificationSettings;

	private boolean createWorkflow;

	/**** XNFR-583 *****/

	private boolean updateModulesFromMyProfile;

	private boolean myVendorsOptionDisplayed;

	private boolean approvalRequiredForAssets;

	private boolean approvalRequiredForTracks;

	private boolean approvalRequiredForPlaybooks;

	// XNFR-820
	private boolean approvalHub;

	/** XNFR-878 **/
	private boolean allowVendorToChangePartnerPrimaryAdmin;

	private boolean mailEnable;

	/** XNFR-979 **/
	private boolean insights;

}
