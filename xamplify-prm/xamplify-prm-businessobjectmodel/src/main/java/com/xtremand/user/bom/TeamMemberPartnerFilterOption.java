package com.xtremand.user.bom;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "xt_team_member_partner_filter_options")
@Getter
@Setter
public class TeamMemberPartnerFilterOption implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3002079180024262973L;
	
	private static final String SEQUENCE = "xt_team_member_partner_filter_options_sequence";
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SEQUENCE)
	@SequenceGenerator(name = SEQUENCE, sequenceName = SEQUENCE, allocationSize = 1)
	@Column(name = "id")
	private Integer id;
	
	@Column(name = "filter_type")
	@org.hibernate.annotations.Type(type = "com.xtremand.user.bom.TeamMemberPartnerFilterEnumType")
	private TeamMemberPartnerFilterType teamMemberPartnerFilterType;
	
	@OneToOne
	@JoinColumn(name = "team_member_id", unique = true)
	private TeamMember teamMember;
	
	@Column(name = "created_time", columnDefinition = "DATETIME")
	@Temporal(TemporalType.TIMESTAMP)
	private Date createdTime;

	@Column(name = "updated_time", columnDefinition = "DATETIME", nullable = true)
	@Temporal(TemporalType.TIMESTAMP)
	private Date updatedTime;


	
	
	
	
	
	


}
