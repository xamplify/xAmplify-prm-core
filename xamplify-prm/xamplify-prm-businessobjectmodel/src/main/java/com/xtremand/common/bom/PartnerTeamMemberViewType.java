package com.xtremand.common.bom;

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

import com.xtremand.partnership.bom.Partnership;
import com.xtremand.user.bom.ModulesDisplayType;
import com.xtremand.user.bom.TeamMember;
import com.xtremand.user.bom.User;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "xt_partner_team_member_view_type")
public class PartnerTeamMemberViewType implements Serializable {

	private static final long serialVersionUID = 1L;

	private static final String SEQUENCE = "xt_partner_team_member_view_type_sequence";

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SEQUENCE)
	@SequenceGenerator(name = SEQUENCE, sequenceName = SEQUENCE, allocationSize = 1)
	private Integer id;

	@ManyToOne
	@JoinColumn(name = "admin_id")
	private User admin;

	@ManyToOne
	@JoinColumn(name = "partnership_id")
	private Partnership partnership;

	@ManyToOne
	@JoinColumn(name = "team_member_id")
	private TeamMember teamMember;

	@Column(name = "vendor_view_type")
	@org.hibernate.annotations.Type(type = "com.xtremand.user.bom.ModulesDisplayEnumType")
	private ModulesDisplayType vendorViewType;

	@Column(name = "is_view_updated")
	private Boolean viewUpdated;

	@Column(name = "created_time")
	@Temporal(TemporalType.TIMESTAMP)
	private Date createdTime;

	@Column(name = "updated_time")
	@Temporal(TemporalType.TIMESTAMP)
	private Date updatedTime;

}
