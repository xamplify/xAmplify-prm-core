package com.xtremand.user.bom;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Immutable;

@Entity
@Immutable
@Table(name = "xt_role")
@Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
public class Role {

	/************* Admin Roles ********************/
	public static final Role USER_ROLE = new Role(3, "ROLE_USER");
	public static final Role COMPANY_PARTNER = new Role(12, "ROLE_COMPANY_PARTNER");
	public static final Role PRM_ROLE = new Role(20, "ROLE_PRM");

	/***************** Team Member Roles *******************/
	public static final Role VIDEO_UPLOAD_ROLE = new Role(4, "ROLE_VIDEO_UPLOAD");
	public static final Role STATS_ROLE = new Role(8, "ROLE_STATS");
	public static final Role ALL_ROLES = new Role(9, "ROLE_ALL");
	public static final Role PARTNERS = new Role(11, "ROLE_PARTNERS");
	public static final Role OPPORTUNITY = new Role(14, "ROLE_OPPORTUNITY");// Dynamic Role
	public static final Role MDF = new Role(21, "ROLE_MDF");// Dynamic Role
	public static final Role DAM = new Role(22, "ROLE_DAM");// Dynamic Role
	public static final Role LEARNING_TRACK = new Role(23, "ROLE_LEARNING_TRACK");// Dynamic Role
	public static final Role PLAY_BOOK = new Role(24, "ROLE_PLAY_BOOK");// Dynamic Role
	public static final Role SHARE_LEADS = new Role(25, "ROLE_SHARE_LEADS");// Dynamic Role

	/************* Deprecated Roles *************/
	public static final Role FORM_ROLE = new Role(15, "ROLE_FORM");

	@Id
	@GeneratedValue
	@Column(name = "role_id")
	private Integer roleId;

	@Column(name = "role")
	private String roleName;

	private String description;

	private Role() {

	}

	private Role(int roleId, String roleName) {
		this.roleId = roleId;
		this.roleName = roleName;
	}

	public Integer getRoleId() {
		return roleId;
	}

	public void setRoleId(Integer roleId) {
		this.roleId = roleId;
	}

	public String getRoleName() {
		return roleName;
	}

	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public int hashCode() {
		return roleId;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Role other = (Role) obj;
		if (roleId == null) {
			if (other.roleId != null)
				return false;
		} else if (!roleId.equals(other.roleId))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Role [roleId=" + roleId + ", roleName=" + roleName + ", description=" + description + "]";
	}

	public static String getAllAdminRolesInString() {
		return String.valueOf(Role.PRM_ROLE.getRoleId());

	}

	public static String getAllAdminRolesAndPartnerRoleInString() {
		return getAllAdminRolesInString() + "," + Role.COMPANY_PARTNER.getRoleId();
	}

	public static boolean hasAnyAdminRole(Set<Role> roles) {
		return roles.contains(Role.PRM_ROLE);
	}

	public static boolean isOnlyPartnerCompanyByRoleNames(List<String> roleNames) {
		return roleNames != null && !roleNames.isEmpty() && roleNames.indexOf(Role.COMPANY_PARTNER.getRoleName()) > -1
				&& roleNames.indexOf(Role.PRM_ROLE.getRoleName()) < 0;
	}

	public static boolean isOnlyPartnerCompanyByRoleIds(List<Integer> roleIds) {
		return roleIds != null && !roleIds.isEmpty() && roleIds.indexOf(Role.COMPANY_PARTNER.getRoleId()) > -1
				&& roleIds.indexOf(Role.PRM_ROLE.getRoleId()) < 0;
	}

	public static List<Integer> getAllAdminRoleIds() {
		List<Integer> roleIds = new ArrayList<>();
		roleIds.add(Role.PRM_ROLE.getRoleId());
		roleIds.add(Role.COMPANY_PARTNER.getRoleId());
		return roleIds;
	}

	public static boolean isAnyAdmin(List<Integer> roleIds) {
		boolean isPrm = roleIds.indexOf(Role.PRM_ROLE.getRoleId()) > -1;
		boolean isPartner = roleIds.indexOf(Role.COMPANY_PARTNER.getRoleId()) > -1;
		return isPrm || isPartner;
	}

	public static boolean isPartnerAdmin(List<Integer> roleIds) {
		return roleIds.indexOf(Role.COMPANY_PARTNER.getRoleId()) > -1;
	}

	public static boolean isAnyAdminAndPartnerCompany(List<Integer> roleIds) {
		boolean isPartner = roleIds.indexOf(Role.COMPANY_PARTNER.getRoleId()) > -1;
		boolean isPrmAndPartner = roleIds.indexOf(Role.PRM_ROLE.getRoleId()) > -1 && isPartner;
		return isPrmAndPartner;
	}

	public static boolean isAnyVendorCompanyAdmin(List<Integer> roleIds) {
		boolean isPrm = roleIds.indexOf(Role.PRM_ROLE.getRoleId()) > -1;
		return isPrm;
	}

}