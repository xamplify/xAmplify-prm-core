package com.xtremand.team.member.group.bom;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import lombok.Getter;
import lombok.Setter;


@Entity
@Table(name = "xt_team_member_group_role_mapping")
@Getter
@Setter
public class TeamMemberGroupRoleMapping implements Serializable {/**
	 * 
	 */
	private static final long serialVersionUID = 50078749250444010L;
	
	private static final String TEAM_MEMBER_GROUP_SEQUENCE_MAPPING = "xt_team_member_group_role_mapping_sequence";

	
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = TEAM_MEMBER_GROUP_SEQUENCE_MAPPING)
	@SequenceGenerator(name = TEAM_MEMBER_GROUP_SEQUENCE_MAPPING, sequenceName = TEAM_MEMBER_GROUP_SEQUENCE_MAPPING, allocationSize = 1)
	@Column(name = "id")
	private Integer id;
	
	
	@ManyToOne
	@JoinColumn(name="team_member_group_id")
	private TeamMemberGroup teamMemberGroup;
	
	@Column(name="role_id")
	private Integer roleId;
	
	@Column(name="created_time",columnDefinition="DATETIME")
	@Temporal(TemporalType.TIMESTAMP)
	private Date createdTime;



}
