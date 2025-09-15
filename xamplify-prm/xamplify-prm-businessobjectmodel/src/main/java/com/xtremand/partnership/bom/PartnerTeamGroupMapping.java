package com.xtremand.partnership.bom;

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

import com.xtremand.team.member.group.bom.TeamMemberGroupUserMapping;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name="xt_partner_team_group_mapping")
@Getter
@Setter
public class PartnerTeamGroupMapping implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1882887379078561570L;
	
	
	private static final String PARTNER_TEAM_GROUP_MAPPING_SEQUENCE = "xt_partner_team_group_mapping_sequence";
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = PARTNER_TEAM_GROUP_MAPPING_SEQUENCE)
	@SequenceGenerator(name = PARTNER_TEAM_GROUP_MAPPING_SEQUENCE, sequenceName = PARTNER_TEAM_GROUP_MAPPING_SEQUENCE, allocationSize = 1)
	@Column(name = "id")
	private Integer id;
	
	@JoinColumn(name = "partnership_id")
	@ManyToOne
	private Partnership partnership;
	
	@JoinColumn(name = "team_member_group_user_mapping_id")
	@ManyToOne
	private TeamMemberGroupUserMapping teamMemberGroupUserMapping;
	
	@Column(name="created_time",columnDefinition="DATETIME")
	@Temporal(TemporalType.TIMESTAMP)
	private Date createdTime;
	
	@Column(name="updated_time",columnDefinition="DATETIME")
	@Temporal(TemporalType.TIMESTAMP)
	private Date updatedTime;

}
