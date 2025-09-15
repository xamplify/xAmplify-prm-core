package com.xtremand.team.member.group.bom;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.xtremand.category.bom.XamplifyDefaultColumn;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "xt_team_member_group")
@EqualsAndHashCode(callSuper = false)
@Getter
@Setter
public class TeamMemberGroup extends XamplifyDefaultColumn implements Serializable {

	/**
	* 
	*/
	private static final long serialVersionUID = -2794824518363705184L;

	private static final String TEAM_MEMBER_GROUP_SEQUENCE = "xt_team_member_group_sequence";

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = TEAM_MEMBER_GROUP_SEQUENCE)
	@SequenceGenerator(name = TEAM_MEMBER_GROUP_SEQUENCE, sequenceName = TEAM_MEMBER_GROUP_SEQUENCE, allocationSize = 1)
	@Column(name = "id")
	private Integer id;

	@Column(name = "name")
	private String name;

	@Column(name = "is_default")
	private boolean defaultGroup;

	@OneToMany(mappedBy = "teamMemberGroup", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	private Set<TeamMemberGroupRoleMapping> teamMemberGroupRoleMappings = new HashSet<>();

	@Column(name = "is_default_sso_group")
	private boolean defaultSsoGroup;

	@Column(name = "alias")
	private String alias;

	/*** XNFR-1066 ***/
	@Column(name = "is_marketing_modules_enabled")
	private boolean marketingModulesAccessToTeamMemberGroup;

	@Transient
	private Integer teamMembersCount;

	@Transient
	private String signUpUrl;

}
