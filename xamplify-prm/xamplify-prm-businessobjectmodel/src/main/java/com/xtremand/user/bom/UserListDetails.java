package com.xtremand.user.bom;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Immutable;

import lombok.Data;

@Entity
@Table(name="v_manage_contacts_partners")
@Immutable
@Data
public class UserListDetails {

	@Id
	@Column(name="user_list_id")
	private Integer id;

	@Column(name="user_list_name")
	private String name;

	@Column(name="company_id")
	private Integer companyId;

	@Column(name="company_name")
	private String companyName;
	
	@Column(name="alias")
	private String alias;
	
	@Column(name="created_time")
	private Date createdTime;

	@Column(name="uploaded_by")
	private String uploadedBy;
	
	@Column(name="created_by")
	private Integer uploadedUserId;
	
	@Column(name="no_of_users")
	private Integer noOfContacts;	
	
	@Column(name="contact_type")
	private String contactType;	
	
	@Column(name="is_public")
	private Boolean publicList ;
	
	@Column(name="is_partner_userlist")
	private Boolean isPartnerUserList;
	
	@Column(name="email_validation_ind")
	private boolean emailValidationInd;
	
	@Column(name="social_network")
	private String socialNetwork;
	
	@Column(name="is_synchronized_list")
	private boolean synchronisedList;
	
	@Column(name="is_default_partnerlist")
	private boolean isDefaultPartnerList;	
	
	@Column(name="is_marketo_master_list")
	private Boolean marketoMasterList;
	
	@Column(name="is_marketo_sync_complete")
	private Boolean marketoSyncComplete;
	
	@Column(name="is_form_list")
	private Boolean formList;
	
	@Column(name="module_name")
	private String moduleName;
	
	@Column(name="upload_in_progress")
	private boolean uploadInProgress;
	
	@Column(name="validation_in_progress")
	private boolean validationInProgress;
	
	@Transient
	private boolean assignedLeadsList;
	
	@Transient
	private String createdDate;
	
	@Transient
	private boolean teamMemberPartnerList;
	
	@Column(name="is_company_list")
	private Boolean companyList;

	@Column(name="associated_company")
	private String associatedCompany;	
	
	@Column(name="associated_company_id")
	private Integer associatedCompanyId;
	
	@Column(name="is_default_contactlist")
	private Boolean isDefaultContactList;
	
	@Column(name = "is_master_contactlist_sync")
	private boolean isMasterContactListSync;
	
	@Column(name="shared_company_name")
	private String sharedCompanyName;

}
