package com.xtremand.vanity.email.templates.bom;

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

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "xt_vendor_email_sent_log")
public class VendorEmailSentLog implements Serializable{

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "vendor_email_sent_log_seq")
	@SequenceGenerator(name = "vendor_email_sent_log_seq", sequenceName = "vendor_email_sent_log_seq", allocationSize = 1)
	@Column(name = "id")
	private Integer id;

	@Column(name = "vendor_id")
	private Integer vendorId;

	@Column(name = "partner_id")
	private Integer partnerId;

	@Column(name = "partner_company_id")
	private Integer partnerCompanyId;

	@Column(name = "vendor_company_id")
	private Integer vendorCompanyId;

	@Column(name = "sent_on", columnDefinition = "DATETIME")
	@Temporal(TemporalType.TIMESTAMP)
	private Date sentOn;

	@Column(name = "email_type")
	@org.hibernate.annotations.Type(type = "com.xtremand.vanity.email.templates.bom.DefaultEmailTemplateTypeEnum")
	private DefaultEmailTemplateType type;

}
