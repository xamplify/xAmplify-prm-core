package com.xtremand.formbeans;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class UserListDTO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6886312217181705398L;
	private Integer id;
	private String name;
	private String uploadedBy;
	private Integer uploadedUserId;
	private Integer noOfContacts;
	private Date createdTime;
	private String createdDate;
	private Integer activeUsersCount;
	private Integer inActiveUsersCount;
	private String socialNetwork;
	private String contactType;
	private String alias;
	private boolean synchronisedList;
	private Boolean isPartnerUserList;
	private boolean isDefaultPartnerList;
	private boolean emailValidationInd;
	private String companyName;
	private boolean invalidList;
	private Boolean publicList;
	private Boolean marketoMasterList;
	private Boolean marketoSyncComplete;
	private String assignedTo;
	private boolean assignedLeadsList;
	private boolean assignedToPartner;
	private Date sharedDate;
	private String assignedDate;
	private boolean sharedLeads;
	private boolean vanityUrlFilter;
	private String vendorCompanyProfileName;
	private String partnerCompanyName;
	private Integer companyId;
	private Long externalListId;
	private String moduleName;
	private Integer associatedCompanyId;
	/**** XNFR-98 ******/
	private boolean teamMemberPartnerList;
	private boolean uploadInProgress = false;
	private boolean validationInProgress = false;
	/**** XNFR-252 ***/
	@Getter
	@Setter
	private Integer loginAsUserId;

	/**** XNFR-427 ***/
	private boolean companyList;

	/**** XNFR-427 ***/
	private boolean isDefaultContactList;

	private boolean isSyncInProgress;

	private String csvPath;
	
	private boolean editList = false;
	
	private boolean isDownload;
	
	/**XNFR-553**/
	@Getter
	@Setter
	private boolean isFormList;
	
	@Getter
	@Setter
	private boolean isMasterContactListSync;
	/**XNFR-553**/
	
	private Integer sourceUserListId;
	private boolean copyList = false;
	
	public void setIsDownload(Boolean isDownload) {
		this.isDownload = isDownload;
	}
	public Boolean getIsDownload() {
		return isDownload;
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

	public String getUploadedBy() {
		return uploadedBy;
	}

	public void setUploadedBy(String uploadedBy) {
		this.uploadedBy = uploadedBy;
	}

	public Integer getNoOfContacts() {
		return noOfContacts;
	}

	public void setNoOfContacts(Integer noOfContacts) {
		this.noOfContacts = noOfContacts;
	}

	public String getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(String createdDate) {
		this.createdDate = createdDate;
	}

	public Integer getActiveUsersCount() {
		return activeUsersCount;
	}

	public void setActiveUsersCount(Integer activeUsersCount) {
		this.activeUsersCount = activeUsersCount;
	}

	public Integer getInActiveUsersCount() {
		return inActiveUsersCount;
	}

	public void setInActiveUsersCount(Integer inActiveUsersCount) {
		this.inActiveUsersCount = inActiveUsersCount;
	}

	public String getSocialNetwork() {
		return socialNetwork;
	}

	public void setSocialNetwork(String socialNetwork) {
		this.socialNetwork = socialNetwork;
	}

	public String getContactType() {
		return contactType;
	}

	public void setContactType(String contactType) {
		this.contactType = contactType;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public boolean isSynchronisedList() {
		return synchronisedList;
	}

	public void setSynchronisedList(boolean synchronisedList) {
		this.synchronisedList = synchronisedList;
	}

	public Integer getUploadedUserId() {
		return uploadedUserId;
	}

	public void setUploadedUserId(Integer uploadedUserId) {
		this.uploadedUserId = uploadedUserId;
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

	public boolean isEmailValidationInd() {
		return emailValidationInd;
	}

	public void setEmailValidationInd(boolean emailValidationInd) {
		this.emailValidationInd = emailValidationInd;
	}

	public String getCompanyName() {
		return companyName;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}

	public Boolean getIsPartnerUserList() {
		return isPartnerUserList;
	}

	public void setIsPartnerUserList(Boolean isPartnerUserList) {
		this.isPartnerUserList = isPartnerUserList;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public boolean isInvalidList() {
		return invalidList;
	}

	public void setInvalidList(boolean invalidList) {
		this.invalidList = invalidList;
	}

	public Boolean getPublicList() {
		return publicList;
	}

	public void setPublicList(Boolean publicList) {
		this.publicList = publicList;
	}

	/**** XNFR-427 ***/

	public boolean isDefaultContactList() {
		return isDefaultContactList;
	}

	public void setDefaultContactList(boolean isDefaultContactList) {
		this.isDefaultContactList = isDefaultContactList;
	}

	public boolean isSyncInProgress() {
		return isDefaultContactList;
	}

	public void setSyncInProgress(boolean isSyncInProgress) {
		this.isSyncInProgress = isSyncInProgress;
	}

}
