package com.xtremand.partnership.bom;

import java.util.Date;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.xtremand.common.bom.CompanyProfile;
import com.xtremand.common.bom.ModuleCustom;
import com.xtremand.common.bom.PartnerTeamMemberViewType;
import com.xtremand.common.bom.XamplifyTimeStamp;
import com.xtremand.lms.bom.LearningTrackVisibility;
import com.xtremand.partner.bom.PartnershipStatusHistory;
import com.xtremand.user.bom.User;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "xt_partnership")
public class Partnership extends XamplifyTimeStamp {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "partnership_id_seq")
	@SequenceGenerator(name = "partnership_id_seq", sequenceName = "partnership_id_seq", allocationSize = 1)
	@Column(name = "id")
	private Integer id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "partner_id", referencedColumnName = "user_id")
	private User representingPartner;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "partner_company_id", referencedColumnName = "company_id")
	private CompanyProfile partnerCompany;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "vendor_id", referencedColumnName = "user_id")
	private User representingVendor;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "vendor_company_id", referencedColumnName = "company_id")
	private CompanyProfile vendorCompany;

	@Column(name = "created_by")
	Integer createdBy;

	@Column(name = "source")
	@org.hibernate.annotations.Type(type = "com.xtremand.partner.bom.PartnershipSourceType")
	private PartnershipSource source;

	@Column(name = "status")
	@org.hibernate.annotations.Type(type = "com.xtremand.partner.bom.PartnershipStatusType")
	private PartnershipStatus status;

	@OneToMany(mappedBy = "partnership", orphanRemoval = true, fetch = FetchType.LAZY)
	private Set<PartnershipStatusHistory> partnershipStatusHistory;

	@Column(name = "contacts_limit")
	private Integer contactsLimit;

	@OneToMany(mappedBy = "partnership", orphanRemoval = true, fetch = FetchType.LAZY)
	private Set<LearningTrackVisibility> learningTracks;

	@OneToMany(mappedBy = "partnership", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private Set<ModuleCustom> moduleCustom;

	@Column(name = "notify_partners")
	private boolean notifyPartners;

	@OneToMany(mappedBy = "partnership", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private Set<PartnerTeamGroupMapping> partnerTeamGroupMappings;

	public enum PartnershipSource {
		ONBOARD("onboard"), VENDOR_INVITATION("invitation"), SIGN_UP_LINK("SIGN_UP_LINK"), SAML_SSO("SAML_SSO"), OAUTH_SSO("OAUTH_SSO");

		protected String source;

		private PartnershipSource(String source) {
			this.source = source;
		}

		public String getSource() {
			return source;
		}
	}

	public enum PartnershipStatus {
    
		INVITED("invited"), APPROVED("approved"), DECLINED("declined"), SUSPENDED("suspended"), DEACTIVATED("deactivated");


		protected String status;

		private PartnershipStatus(String status) {
			this.status = status;
		}

		public String getStatus() {
			return status;
		}
	}

	/**** XNFR-224 ****/
	@Column(name = "is_login_as_partner_option_enabled_for_vendor")
	private boolean loginAsPartnerOptionEnabledForVendor;

	@Column(name = "is_login_as_partner_email_notification_enabled")
	private boolean loginAsPartnerEmailNotificationEnabled;

	/**** XNFR-224 ****/
	@OneToMany(mappedBy = "partnership", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private Set<PartnerTeamMemberViewType> partnerTeamMemberViewType;
	
	/**** XNFR-699 ****/
	@Column(name = "partner_salesforce_account_id")
	private String partnerSalesforceAccountId;

	@Column(name = "partner_salesforce_account_name")
	private String partnerSalesforceAccountName;
	
	@Column(name = "deactivated_on", columnDefinition = "DATETIME")
	@Temporal(TemporalType.TIMESTAMP)
	Date deactivatedOn;

	/*** XNFR-1066 ***/
	@Column(name = "is_marketing_modules_enabled")
	private boolean marketingModulesEnabled;

}