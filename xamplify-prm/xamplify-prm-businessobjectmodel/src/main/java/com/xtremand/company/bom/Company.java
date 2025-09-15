package com.xtremand.company.bom;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.xtremand.common.bom.CompanyProfile;
import com.xtremand.common.bom.XamplifyTimeStamp;
import com.xtremand.user.bom.UserList;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "xt_company")
@Getter
@Setter
public class Company extends XamplifyTimeStamp {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "company_pk_id_seq")
	@SequenceGenerator(name = "company_pk_id_seq", sequenceName = "company_pk_id_seq", allocationSize = 1)
	private Integer id;

	private String name;
	private String address;
	private String city;
	private String state;
	private String country;
	private String zip;
	private String phone;
	private String fax;
	private String website;
	private String email;

	@Column(name = "linkedin_url")
	private String linkedinURL;

	@Column(name = "facebook_url")
	private String facebookURL;

	@Column(name = "twitter_url")
	private String twitterURL;

	@org.hibernate.annotations.Type(type = "com.xtremand.company.bom.CompanySourceType")
	private CompanySource source;

	@ManyToOne
	@JoinColumn(name = "company_id")
	private CompanyProfile companyProfile;

	@Column(name = "created_by")
	Integer createdBy;

	@OneToOne(mappedBy = "associatedCompany")
	private UserList contactList;

	@Column(name = "country_code")
	private String countryCode;

}
