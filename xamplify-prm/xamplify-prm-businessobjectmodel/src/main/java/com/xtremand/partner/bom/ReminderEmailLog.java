package com.xtremand.partner.bom;

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
@Table(name="xt_reminder_email_log")
public class ReminderEmailLog {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator="reminder_email_log_seq")
	@SequenceGenerator(
			name="reminder_email_log_seq",
			sequenceName="reminder_email_log_seq",
			allocationSize=1
			)
	@Column(name="id")
	private Integer id;
	
	@Column(name="vendor_id")
	private Integer vendorId;
	
	@Column(name="partner_id")
	private Integer partnerId;
	
	@Column(name="company_id")
	private Integer companyId;
	
	@Column(name="time", columnDefinition="DATETIME")
	@Temporal(TemporalType.TIMESTAMP)
	private Date time;
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public Integer getVendorId() {
		return vendorId;
	}
	public void setVendorId(Integer vendorId) {
		this.vendorId = vendorId;
	}
	public Integer getPartnerId() {
		return partnerId;
	}
	public void setPartnerId(Integer partnerId) {
		this.partnerId = partnerId;
	}
	public Date getTime() {
		return time;
	}
	public void setTime(Date time) {
		this.time = time;
	}
	public Integer getCompanyId() {
		return companyId;
	}
	public void setCompanyId(Integer companyId) {
		this.companyId = companyId;
	}	
}
