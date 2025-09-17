
package com.xtremand.formbeans;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.persistence.Column;

import org.hibernate.annotations.Type;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.xtremand.campaign.bom.ModuleAccessDTO;
import com.xtremand.company.bom.CompanySource;
import com.xtremand.company.dto.CompanyProfileDTO;
import com.xtremand.flexi.fields.dto.FlexiFieldRequestDTO;
import com.xtremand.social.formbeans.MyMergeTagsInfo;
import com.xtremand.user.bom.ModulesDisplayType;
import com.xtremand.user.bom.Role;
import com.xtremand.util.dto.ModuleCustomDTO;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@ToString(of = { "emailId", "id", "firstName", "lastName" })
public class UserDTO implements Serializable {
	private static final long serialVersionUID = 1L;
	private String address;
	private String alias;
	private String category;
	private String city;
	private Integer companyId;
	private String companyLogo;
	private String companyName;
	private Integer contactCompanyId;
	private String contactCompany;
	private String country;
	private String createdTime;
	private String dateLastEdit;
	private String dateLastLogin;
	private String dateLastNav;
	private String dateReg;
	private String description;
	private String emailId;
	@JsonIgnore
	private MultipartFile file;
	private String firstName;
	private String middleName;
	private String fullName;
	private boolean hasCompany;
	private boolean hasPassword;
	private Integer id;
	private String interests;
	private boolean isSignedUp;
	private String jobTitle;
	private String lastName;
	private String mobileNumber;
	private String occupation;
	private String partnerType;
	private String password;
	private String profileId;
	private String profileImagePath;
	private String region;
	private Integer roleId;
	private Set<Role> roles;
	private String state;
	private Integer updatedBy;
	private String updatedTime;
	private String userDefaultPage;
	private ArrayList<Integer> userListIds = new ArrayList<>();
	private String userName;
	private String userStatus;
	private boolean vendorSignUp;
	private String vertical;
	private String websiteUrl;
	private String zipCode;
	private String emailCategory;
	private ModuleAccessDTO moduleAccessDto;
	private String unsubscribedReason;
	private boolean teamMember;
	private String source;
	private List<Integer> legalBasis = new ArrayList<>();
	private String flexiFieldsJson;
	@Type(type = "json")
	@Column(columnDefinition = "jsonb")
	private List<FlexiFieldRequestDTO> flexiFields = new ArrayList<>();
	private boolean validEmail;
	private ModulesDisplayType modulesDisplayType;
	private String companyProfileName;
	private String preferredLanguage;
	private boolean unsubscribed;
	private String companyFavIconPath;
	private Integer contactsLimit;
	private Double mdfAmount;
	private String mdfAmountInString;
	private boolean enableVanityURL;
	private String roleName;
	private boolean allBoundSource;
	private boolean defaultMdfFormAvaible;
	private String message;
	private String fullNameOrEmailId;
	private BigInteger totalCampaignsCount;
	private BigInteger activeCampaignsCount;
	private BigInteger emailOpenedCount;
	private BigInteger clickedUrlsCount;
	private BigInteger viewsCount;
	private Integer userListId;
	private Integer learningTrackProgress;
	private Integer leadsCount;
	private Integer dealsCount;
	private Integer learningTrackScore;
	private Integer learningTrackMaxScore;
	private boolean hasLearningTrackQuiz = false;
	private String quizSubmittedTime;
	private Integer quizId;
	private Integer learningTrackId;
	private boolean notifyPartners;
	private Integer teamMemberGroupId;
	private Set<Integer> selectedTeamMemberIds;
	private Integer selectedTeamMembersCount;
	private Integer partnershipId;
	private String selectedTeamMemberGroupName;
	private boolean multipleTeamMemberGroupsAssigned;
	private String quizName;
	private boolean loginAsPartnerOptionEnabledForVendor;
	private boolean loginAsPartner;
	private MyMergeTagsInfo mergeTagInfo;

	private boolean secondAdmin;

	private CompanyProfileDTO companyProfileDTO;
	private Integer partnerCompanyId;
	private String partnerCompanyName;
	private String companyNameStatus;
	private String displayContactCompany;
	private String excludedCatagory;

	// XNFR-427
	private boolean createContactCompanyIfDoesNotExist;
	private CompanySource contactCompanySource;

	/*** XNFR-506 ***/
	private boolean signUpUsingVendorLink;

	private Integer userUserListId;

	/*** XNFR-546 ***/
	private String accountName;
	private String accountSubType;
	private String territory;
	private String companyDomain;
	private String accountOwner;
	private String website;

	private boolean isDefaultPageUpdated; // XNFR-560
	/*** XNFR-534 ***/
	private boolean loginUsingSAMLSSO;
	private boolean loginUsingOauthSSO;

	public String viewType;

	public BigInteger rowNumber;

	// XNFR-697
	private String accountId;

	private boolean sendNoSalesforceAccountNotificationToVendor;

	private boolean existingUser;

	private String salesforceInstanceUrl;

	// XNFR-766
	private Integer userId;

	private boolean partnerFilter;

	private List<Integer> damPartnerIds;

	private boolean emailValidationInd;

	private Set<ModuleCustomDTO> defaultModules;

	// XNFR-923
	private Integer damPartnerId;

	private String countryCode;

	private String contactStatus;

	private Integer contactStatusId;

	private String partnerStatus;
	
	private Integer userStatusCode;
	
	private String companyAddress;
	
	private String companyEmail;
	
	private String companyMobile;
	
	private String instagramUrl;
	
	private String twitterUrl;
	
	private String googlePlusLink;
	
	private String facebookLink;
	
	private String linkedInLink;
	
	private String eventUrl;
	
	private String aboutUs;
	
	private String privacyPolicy;
	
	private String partnerAboutUs;
	
	private String unsubscribeLink;
	
	private String senderCompanyAddress;
	
	private String senderCompanyContactNumber;

	private boolean dealContactList;

	private boolean partnerUserList;
	
	//XNFR-1021
	private String deactivatedOn;

	private boolean marketingModulesAccessToVendor;

	private boolean marketingModulesAccessToPartner;

	private Set<ModuleCustomDTO> marketingModules;

	private String trackName;
	private String folder;
	private Date publishedOn;
	private String createdBy;
	private String partnerName;
	private Integer progress;
	private String score;
	
	private String accountOwnerEmailId;
	private String accountOwnerAccountId; 
}