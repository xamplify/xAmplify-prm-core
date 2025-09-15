package com.xtremand.dashboard.buttons.bom;

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

import com.xtremand.common.bom.CompanyProfile;
import com.xtremand.user.bom.User;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "xt_dashboard_buttons")
public class DashboardButton implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "dashboard_buttons_id_seq")
	@SequenceGenerator(name = "dashboard_buttons_id_seq", sequenceName = "dashboard_buttons_id_seq", allocationSize = 1)
	@Column(name = "id")
	private Integer id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "vendor_id", referencedColumnName = "user_id")
	private User user;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "vendor_company_id", referencedColumnName = "company_id")
	private CompanyProfile companyProfile;

	@Column(name = "title")
	private String buttonTitle;

	@Column(name = "sub_title")
	private String buttonSubTitle;

	@Column(name = "description")
	private String buttonDescription;

	@Column(name = "link")
	private String buttonLink;

	@Column(name = "icon")
	private String buttonIcon;

	@Column(name = "new_tab")
	private boolean openInNewTab;

	@Column(name = "timestamp", columnDefinition = "DATETIME")
	@Temporal(TemporalType.TIMESTAMP)
	private Date timestamp;

	/**** XNFR-571 *****/
	@Column(name = "is_publishing_in_progress")
	private boolean publishingInProgress;

	@Column(name = "order_id")
	private Integer order;
	
	@Column(name = "updated_time", columnDefinition = "DATETIME")
	@Temporal(TemporalType.TIMESTAMP)
	private Date updatedTime;
	
	@Column(name = "alternate_url")
	private String alternateUrl;

}
