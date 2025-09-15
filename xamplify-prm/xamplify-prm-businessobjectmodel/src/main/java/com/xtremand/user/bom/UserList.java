package com.xtremand.user.bom;

import java.lang.reflect.InvocationTargetException;
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
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.apache.commons.beanutils.BeanUtils;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.xtremand.common.bom.CompanyProfile;
import com.xtremand.common.bom.XamplifyTimeStamp;
import com.xtremand.company.bom.Company;
import com.xtremand.form.bom.Form;
import com.xtremand.formbeans.UserDTO;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "xt_user_list")
@Getter
@Setter
public class UserList extends XamplifyTimeStamp {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static final String DEFAULT_USER_LIST = "DEFAULT_USER_LIST";

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "list_id_seq")
	@SequenceGenerator(name = "list_id_seq", sequenceName = "list_id_seq", allocationSize = 1)
	@Column(name = "user_list_id")
	Integer id;

	@Column(name = "user_list_name")
	String name;

	String alias;

	@ManyToOne
	@JoinColumn(name = "customer_id", referencedColumnName = "user_id")
	@ToString.Exclude
	private User owner;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "company_id")
	@ToString.Exclude
	private CompanyProfile company;

	public CompanyProfile getCompany() {
		return company;
	}

	public void setCompany(CompanyProfile company) {
		this.company = company;
	}

	@Column(name = "uploaded_date", columnDefinition = "DATETIME")
	@Temporal(TemporalType.TIMESTAMP)
	private Date uploadedDate;

	@org.hibernate.annotations.Type(type = "com.xtremand.user.bom.SocialNetworkType")
	@Column(name = "social_network")
	private SocialNetwork socialNetwork;

	@org.hibernate.annotations.Type(type = "com.xtremand.user.bom.ContactType")
	@Column(name = "contact_type")
	private TYPE contactType;

	@Column(name = "email_validation_ind")
	private boolean emailValidationInd;

	@Column(name = "is_synchronized_list")
	private boolean synchronisedList;

	@Column(name = "is_partner_userlist")
	private Boolean isPartnerUserList;

	@Column(name = "is_default_partnerlist")
	private boolean isDefaultPartnerList;

	@Fetch(FetchMode.SUBSELECT)
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "userList", cascade = CascadeType.ALL, orphanRemoval = true)
	@ToString.Exclude
	private Set<UserUserList> userUserLists = new HashSet<>();

	@Column(name = "external_list_id")
	Long externalListId;

	@Column(name = "is_public")
	private boolean publicList;

	@Column(name = "is_marketo_master_list")
	private boolean marketoMasterList;

	@Column(name = "synchronized_date", columnDefinition = "DATETIME")
	@Temporal(TemporalType.TIMESTAMP)
	Date synchronizedDate;

	@Column(name = "is_marketo_sync_complete")
	private boolean marketoSyncComplete;

	@Column(name = "is_deal_contact_list")
	private boolean dealContactList;

	@ManyToOne
	@JoinColumn(name = "assigned_by", referencedColumnName = "user_id")
	@ToString.Exclude
	private User assignedBy;

	@ManyToOne
	@JoinColumn(name = "assigned_company_id", referencedColumnName = "company_id")
	@ToString.Exclude
	private CompanyProfile assignedCompany;

	@Column(name = "assigned_date", columnDefinition = "DATETIME")
	@Temporal(TemporalType.TIMESTAMP)
	private Date assignedDate;

	@ManyToOne
	@JoinColumn(name = "form_id", referencedColumnName = "id")
	@ToString.Exclude
	private Form form;

	@Column(name = "module_name")
	private String moduleName;

	/**** XNFR-98 ********/
	@Column(name = "is_team_member_partner_list")
	private boolean teamMemberPartnerList;

	@OneToOne(cascade = { CascadeType.PERSIST })
	@JoinColumn(name = "team_member_id", nullable = true)
	@ToString.Exclude
	private TeamMember teamMember;

	@Column(name = "upload_in_progress")
	private boolean uploadInProgress = false;

	@Column(name = "validation_in_progress")
	private boolean validationInProgress = false;

	/**** XNFR-427 ********/
	@OneToOne(cascade = { CascadeType.PERSIST })
	@JoinColumn(name = "associated_company_id", nullable = true)
	@ToString.Exclude
	private Company associatedCompany;

	/**** XNFR-427 ********/
	@org.hibernate.annotations.Type(type = "com.xtremand.user.bom.ContactListType")
	@Column(name = "contact_list_type")
	private ContactListTypeValue contactListType;

	@Column(name = "is_master_contactlist_sync")
	private boolean isMasterContactListSync;

	@Column(name = "csv_path")
	private String csvPath;

	@Transient
	private boolean deleteTeamMemberPartnerList;

	/***** XNFR-107 ******/
	@Transient
	private List<UserUserList> teamMemberPartners;

	/**** XNFR-506 ******/
	@Transient
	private boolean signUpUsingVendorLink;

	/**** XNFR-534 ******/
	@Transient
	private boolean loginUsingSAMLSSO;

	@Transient
	private boolean loginUsingOauthSSO;

	public Form getForm() {
		return form;
	}

	public void setForm(Form form) {
		this.form = form;
	}

	public boolean isMarketoMasterList() {
		return marketoMasterList;
	}

	public void setMarketoMasterList(boolean marketoMasterList) {
		this.marketoMasterList = marketoMasterList;
	}

	public Date getSynchronizedDate() {
		return synchronizedDate;
	}

	public void setSynchronizedDate(Date synchronizedDate) {
		this.synchronizedDate = synchronizedDate;
	}

	public boolean isMarketoSyncComplete() {
		return marketoSyncComplete;
	}

	public void setMarketoSyncComplete(boolean marketoSyncComplete) {
		this.marketoSyncComplete = marketoSyncComplete;
	}

	public Long getExternalListId() {
		return externalListId;
	}

	public void setExternalListId(Long externalListId) {
		this.externalListId = externalListId;
	}

	public enum SocialNetwork {
		MANUAL("MANUAL"), SALESFORCE("SALESFORCE"), GOOGLE("GOOGLE"), ZOHO("ZOHO"), MICROSOFT("MICROSOFT"),
		MARKETO("MARKETO"), HUBSPOT("HUBSPOT"), PIPEDRIVE("PIPEDRIVE"), CONNECTWISE("CONNECTWISE"), HALOPSA("HALOPSA");

		protected String socialNetworkType;

		private SocialNetwork(String socialNetworkType) {
			this.socialNetworkType = socialNetworkType;
		}

		public String getSocialNetworkType() {
			return socialNetworkType;
		}
	}

	public static SocialNetwork getSocialNetworkEnum(String socialNetworkType) {
		switch (socialNetworkType) {
		case "MANUAL":
			return SocialNetwork.MANUAL;
		case "SALESFORCE":
			return SocialNetwork.SALESFORCE;
		case "GOOGLE":
			return SocialNetwork.GOOGLE;
		case "ZOHO":
			return SocialNetwork.ZOHO;
		case "MICROSOFT":
			return SocialNetwork.MICROSOFT;
		case "MARKETO":
			return SocialNetwork.MARKETO;
		case "HUBSPOT":
			return SocialNetwork.HUBSPOT;
		case "PIPEDRIVE":
			return SocialNetwork.PIPEDRIVE;
		case "CONNECTWISE":
			return SocialNetwork.CONNECTWISE;
		case "HALOPSA":
			return SocialNetwork.HALOPSA;

		default:
			return null;
		}
	}

	public boolean isDealContactList() {
		return dealContactList;
	}

	public void setDealContactList(boolean dealContactList) {
		this.dealContactList = dealContactList;
	}

	public enum ContactListTypeValue {
		DEFAULT_CONTACT_LIST("DEFAULT_CONTACT_LIST");

		protected String contactListTypeValue;

		private ContactListTypeValue(String contactListTypeValue) {
			this.contactListTypeValue = contactListTypeValue;
		}

		public String getContactListTypeValue() {
			return contactListTypeValue;
		}
	}

	public static TYPE getContactTypeEnum(String contactType) {
		switch (contactType.toUpperCase()) {
		case "CONTACT":
			return TYPE.CONTACT;
		case "LEAD":
			return TYPE.LEAD;
		case "LISTVIEWS":
			return TYPE.LISTVIEWS;
		case "CONTACT_LISTVIEWS":
			return TYPE.CONTACT_LISTVIEWS;
		case "LEAD_LISTVIEWS":
			return TYPE.LEAD_LISTVIEWS;
		case "LISTS":
			return TYPE.LISTS;

		default:
			return null;
		}
	}

	public enum TYPE {
		CONTACT("CONTACT"), LEAD("LEAD"), LISTVIEWS("LISTVIEWS"), CONTACT_LISTVIEWS("CONTACT_LISTVIEWS"),
		LEAD_LISTVIEWS("LEAD_LISTVIEWS"), LISTS("LISTS");

		protected String type;

		private TYPE(String type) {
			this.type = type;
		}

		public String geType() {
			return type;
		}
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public User getOwner() {
		return owner;
	}

	public void setOwner(User owner) {
		this.owner = owner;
	}

	public Date getUploadedDate() {
		return uploadedDate;
	}

	public void setUploadedDate(Date uploadedDate) {
		this.uploadedDate = uploadedDate;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		UserList other = (UserList) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	public SocialNetwork getSocialNetwork() {
		return socialNetwork;
	}

	public void setSocialNetwork(SocialNetwork socialNetwork) {
		this.socialNetwork = socialNetwork;
	}

	public TYPE getContactType() {
		return contactType;
	}

	public void setContactType(TYPE contactType) {
		this.contactType = contactType;
	}

	public static String getDefaultUserList() {
		return DEFAULT_USER_LIST;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public boolean isEmailValidationInd() {
		return emailValidationInd;
	}

	public void setEmailValidationInd(boolean emailValidationInd) {
		this.emailValidationInd = emailValidationInd;
	}

	public boolean isSynchronisedList() {
		return synchronisedList;
	}

	public void setSynchronisedList(boolean synchronisedList) {
		this.synchronisedList = synchronisedList;
	}

	public Boolean isPartnerUserList() {
		return isPartnerUserList;
	}

	public void setPartnerUserList(Boolean isPartnerUserList) {
		this.isPartnerUserList = isPartnerUserList;
	}

	public boolean isDefaultPartnerList() {
		return isDefaultPartnerList;
	}

	public void setDefaultPartnerList(boolean isDefaultPartnerList) {
		this.isDefaultPartnerList = isDefaultPartnerList;
	}

	public Set<UserUserList> getUserUserLists() {
		return userUserLists;
	}

	public void setUserUserLists(Set<UserUserList> userUserLists) {
		this.userUserLists = userUserLists;
	}

	public Set<User> getUsers() {
		Set<User> users = new HashSet<User>();
		Set<UserUserList> userUserLists = this.getUserUserLists();
		for (UserUserList userUserList : userUserLists) {
			User source = userUserList.getUser();
			User dest = new User();
			try {
				BeanUtils.copyProperties(dest, source);
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}

			dest.setDescription(userUserList.getDescription());
			dest.setCountry(userUserList.getCountry());
			dest.setCity(userUserList.getCity());
			dest.setAddress(userUserList.getAddress());
			dest.setContactCompany(userUserList.getContactCompany());
			dest.setJobTitle(userUserList.getJobTitle());
			dest.setFirstName(userUserList.getFirstName());
			dest.setLastName(userUserList.getLastName());
			dest.setMobileNumber(userUserList.getMobileNumber());
			dest.setState(userUserList.getState());
			dest.setZipCode(userUserList.getZipCode());
			dest.setVertical(userUserList.getVertical());
			dest.setRegion(userUserList.getRegion());
			dest.setPartnerType(userUserList.getPartnerType());
			dest.setCategory(userUserList.getCategory());
			dest.setAccountName(userUserList.getAccountName());
			dest.setAccountSubType(userUserList.getAccountSubType());
			dest.setAccountOwner(userUserList.getAccountOwner());
			dest.setCompanyDomain(userUserList.getCompanyDomain());
			dest.setWebsite(userUserList.getWebsite());
			dest.setTerritory(userUserList.getTerritory());
			/*
			 * List<LegalBasis> legalBasisList = userUserList.getLegalBasis(); List<Integer>
			 * legalBasisIds = new ArrayList<Integer>(); if(legalBasisList != null &&
			 * !legalBasisList.isEmpty()){ for(LegalBasis legalBasis : legalBasisList){
			 * if(legalBasis != null){ legalBasisIds.add(legalBasis.getId()); } } }
			 * dest.setLegalBasis(legalBasisIds);
			 */
			users.add(dest);
		}
		return users;
	}

	public Set<User> getUsersWithoutIntermediateTabeData() {
		Set<User> users = new HashSet<>();
		Set<UserUserList> userUserListsInfos = this.getUserUserLists();
		for (UserUserList userUserList : userUserListsInfos) {
			users.add(userUserList.getUser());
		}
		return users;
	}

	public Boolean getPublicList() {
		return publicList;
	}

	public void setPublicList(Boolean publicList) {
		this.publicList = publicList;
	}

	public Boolean getMasterContactListSync() {
		return isMasterContactListSync;
	}

	public void setMasterContactListSync(Boolean isMasterContactListSync) {
		this.isMasterContactListSync = isMasterContactListSync;
	}

	public User getAssignedBy() {
		return assignedBy;
	}

	public void setAssignedBy(User assignedBy) {
		this.assignedBy = assignedBy;
	}

	public CompanyProfile getAssignedCompany() {
		return assignedCompany;
	}

	public void setAssignedCompany(CompanyProfile assignedCompany) {
		this.assignedCompany = assignedCompany;
	}

	public Date getAssignedDate() {
		return assignedDate;
	}

	public void setAssignedDate(Date assignedDate) {
		this.assignedDate = assignedDate;
	}

	public Set<UserDTO> getUserDTOs() {
		Set<UserDTO> users = new HashSet<UserDTO>();
		Set<UserUserList> userUserLists = this.getUserUserLists();
		for (UserUserList userUserList : userUserLists) {
			User source = userUserList.getUser();
			UserDTO dest = new UserDTO();
			try {
				BeanUtils.copyProperties(dest, source);
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}

			dest.setDescription(userUserList.getDescription());
			dest.setCountry(userUserList.getCountry());
			dest.setCity(userUserList.getCity());
			dest.setAddress(userUserList.getAddress());
			dest.setContactCompany(userUserList.getContactCompany());
			dest.setJobTitle(userUserList.getJobTitle());
			dest.setFirstName(userUserList.getFirstName());
			dest.setLastName(userUserList.getLastName());
			dest.setMobileNumber(userUserList.getMobileNumber());
			dest.setState(userUserList.getState());
			dest.setZipCode(userUserList.getZipCode());
			dest.setVertical(userUserList.getVertical());
			dest.setRegion(userUserList.getRegion());
			dest.setPartnerType(userUserList.getPartnerType());
			dest.setCategory(userUserList.getCategory());
			dest.setAccountName(userUserList.getAccountName());
			dest.setAccountOwner(userUserList.getAccountOwner());
			dest.setAccountSubType(userUserList.getAccountSubType());
			dest.setCompanyDomain(userUserList.getCompanyDomain());
			dest.setWebsite(userUserList.getWebsite());
			dest.setId(source.getUserId());
			dest.setEmailId(source.getEmailId());
			/*
			 * List<LegalBasis> legalBasisList = userUserList.getLegalBasis(); List<Integer>
			 * legalBasisIds = new ArrayList<Integer>(); if(legalBasisList != null &&
			 * !legalBasisList.isEmpty()){ for(LegalBasis legalBasis : legalBasisList){
			 * if(legalBasis != null){ legalBasisIds.add(legalBasis.getId()); } } }
			 * dest.setLegalBasis(legalBasisIds);
			 */
			users.add(dest);
		}
		return users;
	}
}
