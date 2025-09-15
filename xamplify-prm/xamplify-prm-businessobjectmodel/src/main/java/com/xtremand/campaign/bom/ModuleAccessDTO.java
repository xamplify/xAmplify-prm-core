package com.xtremand.campaign.bom;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

@Data
public class ModuleAccessDTO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private boolean id;
	private boolean leads;
	private boolean formBuilder;
	private Integer companyId;
	private Integer userId;
	private boolean loginAsTeamMember;
	private boolean vanityUrlDomain;
	private boolean mdf;
	private boolean dam;
	private boolean shareLeads;
	private Integer roleId;
	private boolean lms;
	private DashboardTypeEnum dashboardType;
	private String dashboardTypeInString;
	private boolean playbooks;
	private boolean excludeUsersOrDomains;
	/*** XNFR-134 ***/
	private boolean customSkinSettings;
	/*** XNFR-139 ****/
	private Integer maxAdmins;
	/*** XNFR-224 ****/
	private boolean loginAsPartner;
	private boolean shareWhiteLabeledContent;
	/**** Upgrading Account ****/
	private Integer teamMemberGroupId;
	private List<Integer> teamMemberGroupRoleIds;
	private boolean createWorkflow;
	/**** XNFR-595 *****/
	private boolean paymentOverDue;
	/**** XNFR-669 *****/
	/**** XNFR-712 *****/
	private boolean approvals;
	// XNFR-820
	private boolean approvalHub;
	/* XNFR-832 */
	private boolean unlockMdfFundingEnabled;

	/* XNFR-878 */
	private boolean allowVendorToChangePartnerPrimaryAdmin;

	/** XNFR-979 **/
	private boolean insights;

	/** XNFR-987 **/
	private boolean nonVanityAccessEnabled;

	/** XNFR-1062 **/
	private boolean mailsEnabled;

}
