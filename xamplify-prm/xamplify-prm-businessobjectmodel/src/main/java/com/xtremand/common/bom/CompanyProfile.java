package com.xtremand.common.bom;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.xtremand.form.bom.Form;
import com.xtremand.partnership.bom.Partnership;
import com.xtremand.user.bom.LegalBasis;
import com.xtremand.user.bom.UserList;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "xt_company_profile")
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class CompanyProfile implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "company_id_seq")
	@SequenceGenerator(name = "company_id_seq", sequenceName = "company_id_seq", allocationSize = 1)
	@Column(name = "company_id")
	Integer id;

	@Column(name = "company_name")
	String companyName;

	@Column(name = "company_profile_name")
	String companyProfileName;

	@Column(name = "email_id")
	String emailId;

	@Column(name = "tag_line")
	String tagLine;

	@Column(name = "facebook_link")
	String facebookLink;

	@Column(name = "google_plus_link")
	String googlePlusLink;

	@Column(name = "linked_in_link ")
	String linkedInLink;

	@Column(name = "twitter_link")
	String twitterLink;

	@Column(name = "company_logo")
	String companyLogoPath;

	@Column(name = "background_logo")
	String backgroundLogoPath;

	@Column(name = "about_us")
	String aboutUs;

	String street;

	@Column(name = "video_id")
	Integer videoId;

	@Column(name = "show_vendor_company_logo")
	boolean showVendorCompanyLogo;

	@Column(name = "favicon_logo")
	private String favIconLogoPath;

	@Column(name = "login_screen_direction")
	private String loginScreenDirection = "Center";

	@Column(name = "lead_approval_status")
	private boolean leadApprovalOrRejection;

	String phone;
	String website;
	String city;
	String state;
	String country;
	String zip;

	@Fetch(FetchMode.SUBSELECT)
	@JsonIgnore
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "partnerCompany")
	private Set<Partnership> partnershipsAsPartner = new HashSet<>();

	@Fetch(FetchMode.SUBSELECT)
	@JsonIgnore
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "vendorCompany")
	private Set<Partnership> partnershipsAsVendor = new HashSet<>();

	@Fetch(FetchMode.SUBSELECT)
	@JsonIgnore
	@OneToMany(mappedBy = "company", orphanRemoval = true, fetch = FetchType.LAZY)
	private List<UserList> userLists = new ArrayList<>();

	@Fetch(FetchMode.SUBSELECT)
	@JsonIgnore
	@OneToMany(mappedBy = "companyProfile", fetch = FetchType.LAZY)
	private List<Form> forms = new ArrayList<>();

	@Column(name = "is_email_dns_configured")
	private boolean emailDnsConfigured;

	@Fetch(FetchMode.SUBSELECT)
	@JsonIgnore
	@OneToMany(mappedBy = "company", fetch = FetchType.LAZY)
	private List<LegalBasis> legalBasisList = new ArrayList<>();

	@Column(name = "is_spf_configured")
	private boolean spfConfigured;

	@Column(name = "privacy_policy")
	private String privacyPolicy;

	@Column(name = "notify_partners")
	private boolean notifyPartners;

	@Column(name = "event_url")
	private String eventUrl;

	/**** XNFR-281 *****/
	@Column(name = "instagram_link")
	private String instagramLink;

	/**** XNFR-326 *****/
	@Column(name = "is_asset_published_email_notification")
	private boolean assetPublishedEmailNotification;

	@Column(name = "is_track_published_email_notification")
	private boolean trackPublishedEmailNotification;

	@Column(name = "is_playbook_published_email_notification")
	private boolean playbookPublishedEmailNotification;
	/**** XNFR-326 *****/

	/**** XNFR-571 *****/
	@Column(name = "is_dashboard_button_published_email_notification")
	private boolean dashboardButtonsEmailNotification;

	@Column(name = "is_dashboard_banner_published_email_notification")
	private boolean dashboardBannersEmailNotification;

	@Column(name = "is_news_and_announcements_published_email_notification")
	private boolean newsAndAnnouncementsEmailNotification;

	/*** XNFR-335 ****/
	@Column(name = "is_domain_connected")
	private boolean isDomainConnected;

	@Column(name = "godaddy_domain_name")
	private String godaddyDomainName;
	/*** XNFR-335 ***/

	@Column(name = "company_name_status")
	@org.hibernate.annotations.Type(type = "com.xtremand.company.bom.CompanyNameStatusType")
	private CompanyNameStatus companyNameStatus;

	public enum CompanyNameStatus {
		ACTIVE("active"), INACTIVE("inactive");

		protected String status;

		private CompanyNameStatus(String status) {
			this.status = status;
		}

		public String getStatus() {
			return status;
		}
	}

	@Column(name = "added_admin_company_id")
	private Integer addedAdminCompanyId;

	/****** Start XNFR-233 ****/
	@Column(name = "background_logo_style2")
	private String backgroundLogoStyle2;

	@Column(name = "login_type")
	@Enumerated(EnumType.STRING)
	private LoginStyleType loginType;

	public enum LoginStyleType {
		STYLE_ONE, STYLE_TWO
	}

	/*** Start XNFR-416 ****/
	@Column(name = "background_color_style1")
	private String backgroundColorStyle1;

	@Column(name = "background_color_style2")
	private String backgroundColorStyle2;

	@Column(name = "is_style1_background_color")
	private boolean styleOneBgColor;

	@Column(name = "is_style2_background_color")
	private boolean styleTwoBgColor;
	/*** Start XNFR-416 ****/

	/*** XBI-2016 ***/
	@Column(name = "login_form_direction_styleone")
	private String loginFormDirectionStyleOne = "Right";

	@Column(name = "sync_contacts_company_list")
	private boolean syncContactsCompanyList;

	@Column(name = "is_merge_companies_in_progress")
	private boolean mergeCompaniesInProgress;

	@Column(name = "is_sync_companies_in_progress")
	private boolean isSyncCompaniesInProgress;

	@Column(name = "support_email_id")
	private String supportEmailId;

	@Column(name = "chat_gpt_api_key")
	private String chatGptApiKey;

	@Column(name = "is_chat_gpt_integration_enabled")
	private boolean chatGptIntegrationEnabled;

	/**** XNFR-688 *****/
	@Column(name = "asset_publish_vendor_email_notification")
	private boolean assetPublishVendorEmailNotification;

	@Column(name = "track_publish_vendor_email_notification")
	private boolean trackPublishVendorEmailNotification;

	@Column(name = "play_book_vendor_email_notification")
	private boolean playbookPublishVendorEmailNotification;

	@Column(name = "dashboard_button_vendor_email_notification")
	private boolean dashboardButtonPublishVendorEmailNotification;

	@Column(name = "update_modules_from_my_profile")
	private boolean updateModulesFromMyProfile;

	/** XNFR-709 **/
	@Column(name = "custom_domain")
	private String customDomain;

	@Column(name = "partner_onboard_vendor_email_notification")
	private boolean partnerOnBoardVendorEmailNotification;

	@Column(name = "team_member_onboard_email_notification")
	private boolean teamMemberOnBoardEmailNotification;

	/** XNFR-824 **/
	@Column(name = "is_approval_required_for_assets")
	private boolean approvalRequiredForAssets;

	@Column(name = "is_approval_required_for_tracks")
	private boolean approvalRequiredForTracks;

	@Column(name = "is_approval_required_for_playbooks")
	private boolean approvalRequiredForPlaybooks;

	@Column(name = "dashboard_layout_settings")
	private boolean dashboardLayoutSettings;

	@Column(name = "campaign_contacts_limit")
	private Integer campaignContactsLimit;

}