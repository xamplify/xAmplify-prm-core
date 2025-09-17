package com.xtremand.dashboard.analytics.views.bom;

import java.io.Serializable;
import java.math.BigInteger;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import lombok.Data;

@Entity(name = "dashboard_module_analytics_view")
@Data
@Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
public class DashboardModuleAnalyticsView implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6302760858231190241L;

	@Id
	@Column(name = "company_id")
	private Integer companyId;

	private BigInteger campaigns;

	@Column(name = "partner_analytics")
	private BigInteger partnerAnalytics;

	private BigInteger contacts;

	@Column(name = "email_templates")
	private BigInteger emailTemplates;

	@Column(name = "uploaded_video")
	private BigInteger uploadedVideos;

	@Column(name = "social_accounts")
	private BigInteger socialAccounts;

	@Column(name = "team_members")
	private BigInteger teamMembers;

	private BigInteger vendors;

	@Transient
	private boolean contactsVisibility;

}
