package com.xtremand.user.bom;

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

import lombok.Data;

@Entity
@Table(name="xt_team_member_emails_history")
@Data
public class TeamMemberEmailsHistory implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 3271970313449355409L;
	
	private static final String TEAM_MEMBER_SEQUENCE = "xt_team_member_emails_history_sequence";

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = TEAM_MEMBER_SEQUENCE)
	@SequenceGenerator(name = TEAM_MEMBER_SEQUENCE, sequenceName = TEAM_MEMBER_SEQUENCE, allocationSize = 1)
	@Column(name = "id")
	private Integer id;
	
	@Column(name="team_member_id")
	private Integer teamMemberId;
	
	@Column(name="sent_by")
	private Integer sentBy;
	
	@Column(name="sent_from_team_member_section")
	private boolean sentFromTeamMemberSection;
	
	
	@Column(name = "sent_time", columnDefinition = "DATETIME")
	@Temporal(TemporalType.TIMESTAMP)
	private Date sentTime;

}
