package com.xtremand.campaign.bom;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapsId;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Type;

import com.xtremand.common.bom.CompanyProfile;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Entity
@Table(name = "xt_module_access")
public class ModuleAccess {

	@Id
	@Column(name = "company_id")
	private Integer id;

	@OneToOne
	@JoinColumn(name = "company_id")
	@MapsId
	private CompanyProfile companyProfile;

	@Column(name = "enable_leads")
	private boolean enableLeads;
	private boolean form;
	@Column(name = "login_as_team_member")
	private boolean loginAsTeamMember;
	@Column(name = "mdf")
	private boolean mdf;
	@Column(name = "dam")
	private boolean dam;
	@Column(name = "share_leads")
	private boolean shareLeads;
	@Column(name = "lms")
	private boolean lms;
	@Column(name = "dashboard_type")
	@Type(type = "com.xtremand.campaign.bom.DashboardTypeEnumerator")
	private DashboardTypeEnum dashboardType;
	@Column(name = "play_books")
	private boolean playbooks;
	@Column(name = "exclude_users_or_domains")
	private boolean excludeUsersOrDomains;

	/*** XNFR-134 ***/
	@Column(name = "custom_skin_settings")
	private boolean customSkinSettings;

	/**** XNFR-139 *****/
	@Column(name = "max_admins")
	private Integer maxAdmins;

	/**** XNFR-224 *****/
	@Column(name = "login_as_partner")
	private boolean loginAsPartner;

	/**** XNFR-255 *****/
	@Column(name = "share_white_labeled_content")
	private boolean shareWhiteLabeledContent;

	@Transient
	@Getter(AccessLevel.NONE)
	private String dashboardTypeInString;

	public String getDashboardTypeInString() {
		return dashboardType != null ? dashboardType.name() : DashboardTypeEnum.DASHBOARD.name();
	}

	// XNFR-820
	@Column(name = "approval_hub")
	private boolean approvalHub;

	/* XNFR-878 */
	@Column(name = "allow_vendor_to_change_partner_primary_admin")
	private boolean allowVendorToChangePartnerPrimaryAdmin;

	/**** XNFR-1062 ***/
	@Column(name = "mails_enabled")
	private boolean mailsEnabled;

}
