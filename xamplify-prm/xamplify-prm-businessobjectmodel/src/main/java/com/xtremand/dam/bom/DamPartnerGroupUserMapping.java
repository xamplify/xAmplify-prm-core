package com.xtremand.dam.bom;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.xtremand.partnership.bom.Partnership;
import com.xtremand.user.bom.TeamMember;
import com.xtremand.user.bom.User;

import lombok.Data;

@Entity
@Data
@Table(name="xt_dam_partner_group_user_mapping")
public class DamPartnerGroupUserMapping implements Serializable {

	private static final long serialVersionUID = 8108299614089921387L;

	private static final String DAM_PARTNER_GROUP_USER_MAPPING_SEQUENCE = "xt_dam_partner_group_user_mapping_sequence";

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = DAM_PARTNER_GROUP_USER_MAPPING_SEQUENCE)
	@SequenceGenerator(name = DAM_PARTNER_GROUP_USER_MAPPING_SEQUENCE, sequenceName = DAM_PARTNER_GROUP_USER_MAPPING_SEQUENCE, allocationSize = 1)
	@Column(name = "id")
	private Integer id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "dam_partner_group_id", nullable = false)
	private DamPartnerGroupMapping damPartnerGroupMapping;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "partnership_id", nullable = false)
	private Partnership partnership;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "team_member_id")
	private TeamMember teamMember;

	@Column(name = "created_time", columnDefinition = "DATETIME")
	@Temporal(TemporalType.TIMESTAMP)
	private Date createdTime;

}