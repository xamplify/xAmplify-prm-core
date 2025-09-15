package com.xtremand.user.bom;

import java.io.Serializable;
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
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.xtremand.company.bom.Company;
import com.xtremand.contact.status.bom.ContactStatus;
import com.xtremand.flexi.fields.bom.UserListFlexiField;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "xt_user_userlist")
@Getter
@Setter
public class UserUserList implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4604349043810076726L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "xt_user_userlist_sequence")
	@SequenceGenerator(name = "xt_user_userlist_sequence", sequenceName = "xt_user_userlist_sequence", allocationSize = 1)
	private Integer id;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "user_id")
	@ToString.Exclude
	private User user;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_list_id")
	@ToString.Exclude
	private UserList userList;

	private String description;
	private String country;
	private String city;
	private String address;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "contact_company_id", referencedColumnName = "id")
	@ToString.Exclude
	private Company company;

	@Column(name = "contact_company")
	private String contactCompany;

	@Column(name = "job_title")
	private String jobTitle;

	@Column(name = "firstname")
	private String firstName;

	@Column(name = "lastname")
	private String lastName;

	@Column(name = "mobile_number")
	private String mobileNumber;

	@Column(name = "state")
	private String state;

	@Column(name = "zip")
	private String zipCode;

	private String vertical;
	private String region;
	@Column(name = "partner_type")
	private String partnerType;
	private String category;

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "xt_user_legal_basis", joinColumns = {
			@JoinColumn(name = "user_userlist_id") }, inverseJoinColumns = @JoinColumn(name = "legal_basis_id"))
	@ToString.Exclude
	private List<LegalBasis> legalBasis;

	/*** XNFR_546 ****/

	@Column(name = "account_name ")
	private String accountName;

	@Column(name = "account_sub_type ")
	private String accountSubType;

	@Column(name = "territory")
	private String territory;

	@Column(name = "company_domain")
	private String companyDomain;

	@Column(name = "account_owner")
	private String accountOwner;

	@Column(name = "website")
	private String website;

	@Fetch(FetchMode.SUBSELECT)
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "userUserList", cascade = CascadeType.ALL)
	@ToString.Exclude
	private Set<UserListFlexiField> userListFlexiFields = new HashSet<>();

	@Transient
	private Long externalContactId;

	@Column(name = "country_code")
	private String countryCode;

	@Column(name = "contact_status_id")
	private Integer contactStatusId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "contact_status_id", insertable = false, updatable = false)
	private ContactStatus contactStatus;

}
