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
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.xtremand.form.bom.FormTeamGroupMapping;
import com.xtremand.partnership.bom.PartnerTeamGroupMapping;
import com.xtremand.user.bom.TeamMember;
import com.xtremand.util.bom.XamplifyDefault;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "xt_team_member_group_user_mapping")
@EqualsAndHashCode(callSuper = false)
@Getter
@Setter
public class TeamMemberGroupUserMapping extends XamplifyDefault implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8183084652466125277L;

	private static final String TEAM_MEMBER_GROUP_USER_SEQUENCE = "xt_team_member_group_user_mapping_sequence";

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = TEAM_MEMBER_GROUP_USER_SEQUENCE)
	@SequenceGenerator(name = TEAM_MEMBER_GROUP_USER_SEQUENCE, sequenceName = TEAM_MEMBER_GROUP_USER_SEQUENCE, allocationSize = 1)
	@Column(name = "id")
	private Integer id;

	@ManyToOne
	@JoinColumn(name = "team_member_group_id")
	private TeamMemberGroup teamMemberGroup;

	@OneToOne
	@JoinColumn(name = "team_member_id", unique = true)
	private TeamMember teamMember;

	@OneToMany(mappedBy = "teamMemberGroupUserMapping", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private Set<PartnerTeamGroupMapping> partnerTeamGroupMappings = new HashSet<>();

	@OneToMany(mappedBy = "teamMemberGroupUserMapping", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private Set<FormTeamGroupMapping> formTeamGroupMappings = new HashSet<>();
}
