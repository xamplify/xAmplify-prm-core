package com.xtremand.sso.bom;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Type;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "xt_saml_security")
public class SamlSecurity implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sso_details_id_seq")
	@SequenceGenerator(name = "sso_details_id_seq", sequenceName = "sso_details_id_seq", allocationSize = 1)
	@Column(name = "id")
	private Integer id;

	@Column(name = "email_id")
	private String emailId;

	@Column(name = "company_id")
	private Integer companyId;

	@Column(name = "timestamp", columnDefinition = "DATETIME")
	@Temporal(TemporalType.TIMESTAMP)
	private Date timestamp;

	@Column(name = "metadata")
	private String metadata;

	@Column(name = "metadata_filename")
	private String metadataFileName;

	@Column(name = "acs_url")
	private String acsURL;
	
	/** XNFR-579 **/
	@Column(name = "created_by_user_id")
	private Integer createdByUserId;
	
	@Column(name = "created_time")
	private Date createdTime;
	
	@Column(name = "updated_by_user_id")
	private Integer updatedByUserId;
	
	@Column(name = "updated_time")
	private Date updatedTime;
	
	@Column(name = "acs_id")
	private String acsId;
	
	@Column(name = "identity_provider_name")
	@Type(type = "com.xtremand.sso.bom.IdentityProviderNameType")
	private IdentityProviderName identityProviderName;

}
