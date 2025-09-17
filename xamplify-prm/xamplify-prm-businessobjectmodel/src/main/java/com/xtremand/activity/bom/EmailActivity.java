package com.xtremand.activity.bom;

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

import org.hibernate.annotations.Type;

import com.xtremand.activity.dto.EmailActivityStatusEnum;
import com.xtremand.common.bom.CompanyProfile;
import com.xtremand.user.bom.User;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "xt_email_activity")
@Getter
@Setter
public class EmailActivity {
	
	private static final String SEQUENCE = "xt_email_activity_sequence";
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SEQUENCE)
	@SequenceGenerator(name = SEQUENCE, sequenceName = SEQUENCE, allocationSize = 1)
	private Integer id;
	private String subject;
	private String body;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "recipient_user_id")
	private User recipient;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "sender_user_id")
	private User sender;
	
	@Column(name = "created_time")
	private Date createdTime;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "company_id")
	private CompanyProfile companyProfile;
	
	@Column(name = "status")
	@Type(type = "com.xtremand.activity.dto.EmailActivityStatusEnumType")
	private EmailActivityStatusEnum status;
	
	@Column(name = "opened_time")
	private Date openedTime;
	
}
