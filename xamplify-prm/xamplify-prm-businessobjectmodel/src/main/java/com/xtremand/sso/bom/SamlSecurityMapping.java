package com.xtremand.sso.bom;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "xt_saml_security_mapping")
public class SamlSecurityMapping {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sso_mapping_id_seq")
	@SequenceGenerator(name = "sso_mapping_id_seq", sequenceName = "sso_mapping_id_seq", allocationSize = 1)
	@Column(name = "id")
	private Integer id;

	@Column(name = "sp_company_id")
	private Integer spCompanyId;

	@Column(name = "idp_company_id")
	private Integer idpCompanyId;

	@Column(name = "saml_security_id")
	private Integer samlSecurityId;

	@Column(name = "user_id")
	private Integer userId;

}
