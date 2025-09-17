package com.xtremand.dam.bom;

import java.io.Serializable;
import java.math.BigInteger;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;

@MappedSuperclass
@Data
public class DamAnalyticsMappedSuperClass implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 753930051190635952L;

	@Id
	@Column(name = "id")
	private Integer id;
	
	@Column(name = "dam_id")
	private Integer damId;
	
	@Column(name="dam_partner_id")
	private Integer damPartnerId;
	
	
	@Column(name="user_id")
	private Integer userId;
	
	@Column(name="partner_id")
	@JsonIgnore
	private Integer partnerId;
	
	@Column(name="partnership_id")
	private Integer partnershipId;
	
	
	@Column(name="email_id")
	private String emailId;
	
	@Column(name="first_name")
	private String firstName;
	
	@Column(name="last_name")
	private String lastName;
	
	@Column(name="full_name")
	private String fullName;
	
	@Column(name="contact_company")
	private String contactCompany;
	
	@Column(name="view_count")
	private BigInteger viewCount;
	
	@Column(name="download_count")
	private BigInteger downloadCount;
	

}
