package com.xtremand.user.bom;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.xtremand.common.bom.CompanyProfile;
import com.xtremand.common.bom.PartnerTeamMemberViewType;
import com.xtremand.common.bom.XamplifyTimeStamp;
import com.xtremand.flexi.fields.dto.FlexiFieldRequestDTO;
import com.xtremand.form.submit.bom.FormSubmit;
import com.xtremand.partnership.bom.Partnership;
import com.xtremand.video.bom.VideoLead;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(of = { "userId", "emailId", "alias" })
@Entity
@Table(name = "xt_user_profile")
public class User extends XamplifyTimeStamp {

	private static final long serialVersionUID = 1L;

	public enum UserDefaultPage {
		// Default target url after login successful
		DASHBOARD("dashboard"), WELCOME("welcome");

		protected String defaultPage;

		private UserDefaultPage(String defaultPage) {
			this.defaultPage = defaultPage;
		}

		public String getDefaultPage() {
			return defaultPage;
		}
	}

	public enum UserStatus {
		UNAPPROVED("UnApproved"), APPROVED("APPROVE"), DECLINE("DECLINE");

		protected String status;

		private UserStatus(String status) {
			this.status = status;
		}

		public String getStatus() {
			return status;
		}

		@JsonCreator
		public static UserStatus fromString(String value) {
			for (UserStatus status : UserStatus.values()) {
				if (status.status.equalsIgnoreCase(value)) {
					return status;
				}
			}
			return null;
		}
	}

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_id_seq")
	@SequenceGenerator(name = "user_id_seq", sequenceName = "user_id_seq", allocationSize = 1)
	@Column(name = "user_id")
	private Integer userId;

	@Column(name = "user_name")
	private String userName;
	@Column(name = "email_id", unique = true)
	private String emailId;
	@Column(name = "password")
	private String password;
	@Column(name = "firstname")
	private String firstName;
	@Column(name = "lastname")
	private String lastName;

	@Column(name = "middle_name")
	private String middleName;

	@Column(name = "status")
	@org.hibernate.annotations.Type(type = "com.xtremand.user.bom.UserStatusType")
	private UserStatus userStatus;
	private String country;
	private String city;
	private String address;
	@Column(name = "headline")
	private String headLine;
	private String description;
	@Column(name = "googlelogin")
	private String googleLogin;
	@Column(name = "linkedinlogin")
	private String linkedinLogin;
	@Column(name = "twitterlogin")
	private String twitterLogin;
	@Column(name = "facebooklogin")
	private String facebookLogin;
	@Column(name = "datereg", columnDefinition = "DATETIME")
	@Temporal(TemporalType.TIMESTAMP)
	private Date dateReg;
	@Column(name = "datelastedit", columnDefinition = "DATETIME")
	@Temporal(TemporalType.TIMESTAMP)
	private Date dateLastEdit;
	@Column(name = "datelastlogin", columnDefinition = "DATETIME")
	@Temporal(TemporalType.TIMESTAMP)
	private Date dateLastLogin;
	@Column(name = "datelastnav", columnDefinition = "DATETIME")
	@Temporal(TemporalType.TIMESTAMP)
	private Date dateLastNav;

	@Column(name = "alias")
	private String alias;

	@Column(name = "google_refreshtoken")
	private String googleRefreshToken;
	@Column(name = "google_accesstoken")
	private String googleAccessToken;
	@Column(name = "salesforce_refreshtoken")
	private String salesforceRefreshToken;

	@Column(name = "zoho_refreshtoken")
	private String zohoRefreshToken;
	@Column(name = "zoho_accesstoken")
	private String zohoAccessToken;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "zoho_expiry")
	private Date zohoExpiry;

	@Column(name = "salesforce_accesstoken")
	private String salesforceAccessToken;
	@Column(name = "zoho_authtoken")
	private String zohoAuthToken;
	@Column(name = "mobile_number")
	private String mobileNumber;
	private String interests;
	private String occupation;

	@Column(name = "profile_image")
	private String profileImage;

	@Column(name = "website_url")
	private String websiteUrl;


	@OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@JoinColumn(name = "company_id", nullable = true)
	private CompanyProfile companyProfile;

	@org.hibernate.annotations.Type(type = "com.xtremand.user.bom.UserSourceType")
	private UserSource source;

	@Column(name = "is_email_valid")
	private boolean emailValid;

	@Column(name = "default_page")
	@org.hibernate.annotations.Type(type = "com.xtremand.user.bom.UserDefaultPageType")
	private UserDefaultPage userDefaultPage;

	@Column(name = "is_grid_view")
	private boolean isGridView;

	@Column(name = "zoho_username")
	private String zohoUserName;

	@Column(name = "zoho_password")
	private String zohoPassword;

	@Column(name = "contact_company")
	private String contactCompany;

	@Transient
	private Integer contactCompanyId;

	@Column(name = "job_title")
	private String jobTitle;

	@Column(name = "email_validation_ind")
	private boolean emailValidationInd;

	@Column(name = "email_category")
	private String emailCategory;

	@Transient
	private String companyName;

	@Column(name = "state")
	private String state;
	@Column(name = "zip")
	private String zipCode;
	@Transient
	private String vertical;
	@Transient
	private String region;

	@Transient
	private String partnerType;

	@Transient
	private String category;

	@ManyToMany(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	@Fetch(FetchMode.SUBSELECT)
	@JoinTable(name = "xt_user_role", joinColumns = {
			@JoinColumn(name = "user_id", nullable = false, updatable = false) }, inverseJoinColumns = {
					@JoinColumn(name = "role_id", nullable = false, updatable = false) })
	private Set<Role> roles = new HashSet<Role>(0);

	@Fetch(FetchMode.SUBSELECT)
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "teamMember", cascade = CascadeType.ALL)
	private List<TeamMember> teamMembers;

	@Fetch(FetchMode.SUBSELECT)
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "orgAdmin")
	private List<TeamMember> orgAdmins;

	@Fetch(FetchMode.SUBSELECT)
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "userCustomerId.user", cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<UserCustomer> userCustomers = new HashSet<UserCustomer>(0);

	@Fetch(FetchMode.SUBSELECT)
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "partnerCompanyId.partner", cascade = CascadeType.ALL)
	private Set<PartnerCompany> partnerCompanies = new HashSet<>();

	@Fetch(FetchMode.SUBSELECT)
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "user", cascade = CascadeType.ALL)
	private Set<UserUserList> userUserLists = new HashSet<UserUserList>(0);

	@Fetch(FetchMode.SUBSELECT)
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "user")
	private Set<VideoLead> videoLeads = new HashSet<VideoLead>();

	@Fetch(FetchMode.SUBSELECT)
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "representingPartner")
	private Set<Partnership> partnershipsAsPartner = new HashSet<Partnership>();

	@Fetch(FetchMode.SUBSELECT)
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "representingVendor")
	private Set<Partnership> partnershipsAsVendor = new HashSet<Partnership>();

	@Transient
	private List<Integer> legalBasis;

	@Transient
	private String companyProfileName;

	@Transient
	private String subjectLine;

	/*** XNFR-546 ****/
	@Transient
	private String accountName;

	@Transient
	private String accountSubType;

	@Transient
	private String territory;

	@Transient
	private String companyDomain;

	@Transient
	private String accountOwner;

	@Transient
	private String website;

	@Transient
	private String accountId;

	@Transient
	private boolean existsInPartnerList;

	@Fetch(FetchMode.SUBSELECT)
	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private Set<FormSubmit> FormSubmit = new HashSet<>();

	@Column(name = "modules_display_type")
	@org.hibernate.annotations.Type(type = "com.xtremand.user.bom.ModulesDisplayEnumType")
	private ModulesDisplayType modulesDisplayType;

	@Column(name = "preferred_language")
	private String preferredLanguage = "en";

	@Column(name = "google_profile_email")
	private String googleProfileEmail;

	@Column(name = "salesforce_profile_email")
	private String salesforceProfileEmail;

	@Column(name = "zoho_profile_email")
	private String zohoProfileEmail;

	@Column(name = "registered_time", columnDefinition = "DATETIME")
	@Temporal(TemporalType.TIMESTAMP)
	Date registeredTime;

	@Column(name = "activated_time", columnDefinition = "DATETIME")
	@Temporal(TemporalType.TIMESTAMP)
	Date activatedTime;

	@Transient
	private boolean unsubscribed;

	@Transient
	private Integer contactsLimit;

	@Transient
	private Double mdfAmount;

	@Transient
	private Integer teamMemerPkId;

	@Transient
	private boolean damTemplateDownload;

	@Transient
	private boolean notifyPartners;

	@Transient
	private boolean disableNotifyPartnersOption;

	@Transient
	private Set<Integer> selectedTeamMemberIds;
	@Transient
	private Integer teamMemberGroupId;
	@Transient
	private Integer selectedTeamMembersCount;
	@Transient
	private String selectedTeamMemberGroupName;
	@Transient
	private boolean multipleTeamMemberGroupsAssigned;

	@Column(name = "user_alias")
	private String userAlias;

	@Column(name = "zoho_api_domain")
	private String zohoApiDomain;

	@Column(name = "zoho_accounts_server")
	private String zohoAccountsServer;

	@Column(name = "is_default_page_updated")
	private boolean isDefaultPageUpdated; // XNFR-560

	@Transient
	private String fullName;

	@Fetch(FetchMode.SUBSELECT)
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "admin", cascade = CascadeType.ALL)
	private List<PartnerTeamMemberViewType> partnerTeamMemberViewType;

	@Column(name = "is_campaign_analytics_settings_enabled")
	private boolean campaignAnalyticsSettingsEnabled;

	@Transient
	private List<FlexiFieldRequestDTO> flexiFields = new ArrayList<>();

	@Column(name = "salesforce_instance_url")
	private String salesforceInstanceurl;

	/*** XNFR-812 ****/

	@Column(name = "draw_signature_image_path")
	private String drawSignatureImagePath;

	@Column(name = "typed_signature_text")
	private String typedSignatureText;

	@Column(name = "typed_signature_font")
	private String typedSignatureFont;

	@Column(name = "uploaded_signature_image_path")
	private String uploadedSignatureImagePath;

	@Transient
	private String countryCode;

	@Transient
	private Integer contactStatusId;

	/*** XNFR-812 ****/

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		User other = (User) obj;
		if (userId == null) {
			if (other.userId != null)
				return false;
		} else if (!userId.equals(other.userId))
			return false;
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((userId == null) ? 0 : userId.hashCode());
		return result;
	}

	public void initialiseCommonFields(boolean isCreate, int updatedBy) {
		super.initialiseCommonFields(isCreate, updatedBy);
		this.setDateLastEdit(new Date());
		this.setDateLastLogin(new Date());
		this.setDateLastNav(new Date());
		this.setDateReg(new Date());
	}

	public Set<UserList> getUserLists() {
		Set<UserList> userLists = new HashSet<>();
		Set<UserUserList> distinctUserUserLists = this.getUserUserLists();
		for (UserUserList userUserList : distinctUserUserLists) {
			userLists.add(userUserList.getUserList());
		}
		return userLists;
	}

	public boolean isUnApproved() {
		return UserStatus.UNAPPROVED.equals(this.userStatus);
	}

	@JsonIgnore
	public boolean isSubAdmin() {
		return this.roles.contains(Role.VIDEO_UPLOAD_ROLE);
	}

	@JsonIgnore
	public User getOrgAdminOfSubAdmin() {
		User orgAdmin = null;
		for (UserCustomer uc : this.getUserCustomers()) {
			if (uc.isOrgAdmin()) {
				orgAdmin = uc.getCustomer();
				break;
			}
		}
		return orgAdmin;
	}


}
