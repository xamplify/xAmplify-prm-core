package com.xtremand.team.member.dto;

import java.io.Serializable;
import java.util.List;

import com.xtremand.user.bom.Role;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;

@Data
public class RoleDisplayDTO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4636706814405955427L;

	private boolean prm;

	private boolean prmAndPartner;

	private boolean prmTeamMember;

	private boolean prmAndPartnerTeamMember;

	private boolean partner;

	private boolean partnerTeamMember;

	private boolean user;

	private String role;

	private boolean teamMember;

	private List<Integer> roleIds;

	private boolean partnershipEstablishedWithPrmAndLoggedInAsPartner;

	@Getter(value = AccessLevel.NONE)
	private boolean partnerSuperVisor;

	@Getter(value = AccessLevel.NONE)
	private boolean prmSuperVisor;

	@Getter(value = AccessLevel.NONE)
	private boolean anyAdminOrSuperVisor;

	private boolean companyExists;

	/******* XNFR-83 **********/

	public boolean anyPartnerRole() {
		return this.prmAndPartner || this.prmAndPartnerTeamMember;

	}

	public boolean partnerOrPartnerTeamMember() {
		return this.partner || this.partnerTeamMember;
	}

	public boolean neitherPartnerNorPartnerTeamMember() {
		return !this.partner && !this.partnerTeamMember;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public boolean isPartnerSuperVisor() {
		return this.partnerTeamMember && (this.roleIds.indexOf(Role.ALL_ROLES.getRoleId())) > -1;
	}

	public boolean isPrmSuperVisor() {
		prmSuperVisor = (this.prmAndPartnerTeamMember || this.prmTeamMember)
				&& (this.roleIds.indexOf(Role.ALL_ROLES.getRoleId()) > -1
						&& this.roleIds.indexOf(Role.PRM_ROLE.getRoleId()) < 0);
		return prmSuperVisor;
	}

	public boolean hasDesignAccess() {
		return roleIds.indexOf(Role.FORM_ROLE.getRoleId()) > -1;
	}

	public boolean hasTeamAccess() {
		return isAnyAdmin() || this.partner;
	}

	public boolean isAnyAdmin() {
		return this.prm || this.prmAndPartner;
	}

	public boolean isAnyAdminOrTeamMemberOfAdmin() {
		return this.prm || this.prmTeamMember || this.prmAndPartner || this.prmAndPartnerTeamMember;
	}

	public boolean isAnyAdminOrSuperVisor() {
		return this.prm || this.prmAndPartner || this.partner || roleIds.indexOf(Role.ALL_ROLES.getRoleId()) > -1;
	}

	public boolean formAccessForPrm() {
		return this.prm || this.prmTeamMember || this.prmAndPartner || this.prmAndPartnerTeamMember;
	}

	public boolean anyAdminAndPartnerRole() {
		return this.prmAndPartner;
	}

	public boolean prmAndPartnerCompany() {
		return this.prmAndPartner || this.prmAndPartnerTeamMember;
	}

	public boolean isAnyAdminOrSuperVisorExcludingOnlyPartnerAndMarketing() {
		return (this.prm || this.prmAndPartner || roleIds.indexOf(Role.ALL_ROLES.getRoleId()) > -1)
				&& neitherPartnerNorPartnerTeamMember();
	}

	public boolean isPrmOrPrmAndPartnerCompany() {
		return this.prm || this.prmTeamMember || this.prmAndPartner || this.prmAndPartnerTeamMember;
	}

	public boolean isAdminTeamMember() {
		return this.prmTeamMember;
	}

	public boolean isAdminAndPartnerTeamMember() {
		return this.prmAndPartnerTeamMember;
	}

	public boolean isAdminTeammemberOrAdminAndPartnerTeamMember() {
		return this.prmTeamMember || this.prmAndPartnerTeamMember;

	}

	public boolean isAdminWithPartnerCompany() {
		return this.prmAndPartner;
	}

	public boolean isAdminRole() {
		return this.prm;
	}

}
