package com.xtremand.activity.bom;

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

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "xt_email_recipient")
@Getter
@Setter
public class EmailRecipient {
	
	private static final String SEQUENCE = "xt_email_recipient_sequence";
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SEQUENCE)
	@SequenceGenerator(name = SEQUENCE, sequenceName = SEQUENCE, allocationSize = 1)
	private Integer id;
	
	@Column(name = "email_id")
	private String emailId;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "email_activity_id")
	private EmailActivity emailActivity;
	
	@Column(name = "email_recipient_type")
	@Type(type = "com.xtremand.activity.bom.EmailRecipientEnumType")
	private EmailRecipientEnum emailRecipientEnum;
}
