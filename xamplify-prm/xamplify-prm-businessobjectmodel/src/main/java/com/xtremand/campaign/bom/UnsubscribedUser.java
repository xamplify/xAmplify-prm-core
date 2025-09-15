package com.xtremand.campaign.bom;

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

@Entity
@Table(name="xt_unsubscribed_user")
public class UnsubscribedUser {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator="unsubscribed_user_id_seq")
	@SequenceGenerator(
			name="unsubscribed_user_id_seq",
			sequenceName="unsubscribed_user_id_seq",
			allocationSize=1
			)
	private Integer id;

	@Column(name = "user_id")
	private Integer userId;

	@Column(name = "customer_company_id")
	private Integer customerCompanyId;


	@Column(name = "unsubscribed_time", columnDefinition="DATETIME")
	@Temporal(TemporalType.TIMESTAMP)
	private Date unsubscribedTime;
	
	
	@Column(name = "customer_enabled_time", columnDefinition="DATETIME")
	@Temporal(TemporalType.TIMESTAMP)
	private Date customerEnabledTime;
	
	@Column(name = "is_customer_enabled")
	private boolean customerEnabled;
	
	@Column(name = "reason")
	private String reason;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public Integer getCustomerCompanyId() {
		return customerCompanyId;
	}

	public void setCustomerCompanyId(Integer customerCompanyId) {
		this.customerCompanyId = customerCompanyId;
	}

	public Date getUnsubscribedTime() {
		return unsubscribedTime;
	}

	public void setUnsubscribedTime(Date unsubscribedTime) {
		this.unsubscribedTime = unsubscribedTime;
	}

	public Date getCustomerEnabledTime() {
		return customerEnabledTime;
	}

	public void setCustomerEnabledTime(Date customerEnabledTime) {
		this.customerEnabledTime = customerEnabledTime;
	}

	public boolean isCustomerEnabled() {
		return customerEnabled;
	}

	public void setCustomerEnabled(boolean customerEnabled) {
		this.customerEnabled = customerEnabled;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}
}
