package com.xtremand.dashboard.buttons.bom;

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

import com.xtremand.user.bom.User;
import com.xtremand.user.bom.UserUserList;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "xt_dashboard_buttons_partner_group_mapping")
@Getter
@Setter
public class DashboardButtonsPartnerGroupMapping extends DashboardButtonsPartnershipMapping implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3143095071606102431L;

	private static final String SEQUENCE = "xt_dashboard_buttons_partner_group_mapping_sequence";

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SEQUENCE)
	@SequenceGenerator(name = SEQUENCE, sequenceName = SEQUENCE, allocationSize = 1)
	private Integer id;

	@ManyToOne
	@JoinColumn(name = "user_user_list_id")
	private UserUserList userUserList;

	@Column(name = "published_on", columnDefinition = "DATETIME")
	@Temporal(TemporalType.TIMESTAMP)
	private Date publishedOn;

	@ManyToOne
	@JoinColumn(name = "published_by")
	private User publishedBy;

}
