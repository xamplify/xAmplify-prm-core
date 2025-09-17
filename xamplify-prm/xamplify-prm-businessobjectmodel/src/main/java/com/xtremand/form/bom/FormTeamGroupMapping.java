package com.xtremand.form.bom;

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
@Table(name = "xt_form_team_group_mapping")
@Getter
@Setter
public class FormTeamGroupMapping implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1019922451943101667L;

	private static final String FORM_TEAM_GROUP_MAPPING_SEQUENCE = "xt_form_team_group_mapping_sequence";

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = FORM_TEAM_GROUP_MAPPING_SEQUENCE)
	@SequenceGenerator(name = FORM_TEAM_GROUP_MAPPING_SEQUENCE, sequenceName = FORM_TEAM_GROUP_MAPPING_SEQUENCE, allocationSize = 1)
	@Column(name = "id")
	private Integer id;

	@JoinColumn(name = "form_id")
	@ManyToOne
	private Form form;

	@JoinColumn(name = "team_member_group_user_mapping_id")
	@ManyToOne
	private TeamMemberGroupUserMapping teamMemberGroupUserMapping;

	@Column(name = "created_time", columnDefinition = "DATETIME")
	@Temporal(TemporalType.TIMESTAMP)
	private Date createdTime;

	@Column(name = "updated_time", columnDefinition = "DATETIME")
	@Temporal(TemporalType.TIMESTAMP)
	private Date updatedTime;

}
