package com.xtremand.user.bom;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Entity
@Data
@Table(name="v_partner_companies_and_module_access")
public class PartnerCompanyAndModuleAccessView implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8040796121867714605L;

	@Id
	@Column(name="company_id")
	private Integer companyId;
	
	@Column(name="company_profile_name")
	private String companyProfileName;
	
	@Column(name="company_name")
	private String companyName;
	
	@Column(name="is_email_dns_configured")
	private boolean emailDnsConfigured;
	
	@Column(name="exclude_users_or_domains")
	private boolean excludeUsersOrDomains;
	
	@Column(name="login_as_team_member")
	private boolean loginAsTeamMember;
	
	@Column(name="max_admins")
	private Integer maxAdmins;
	
	/** XNFR-987 **/
	@Column(name = "non_vanity_access_enabled")
	private boolean nonVanityAccessEnabled;

}
