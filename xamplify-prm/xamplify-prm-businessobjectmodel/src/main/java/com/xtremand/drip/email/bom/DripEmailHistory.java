package com.xtremand.drip.email.bom;

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

import com.xtremand.mail.bom.EmailTemplate;
import com.xtremand.user.bom.User;

import lombok.Data;

@Data
@Entity
@Table(name= "xt_drip_email_history")
public class DripEmailHistory {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "drip_email_id_seq")
	@SequenceGenerator(name = "drip_email_id_seq", sequenceName = "drip_email_id_seq", allocationSize = 1)
	@Column(name = "id")
	private Integer id;
	
	@Column(name = "action_id")
	private Integer actionId;
	
	//@Column(name = "user_id")
	//private int userId;
	
	@ManyToOne
	@JoinColumn(name = "user_id", referencedColumnName="user_id")
	private User userId;
	
	@ManyToOne
	@JoinColumn(name="email_template_id",referencedColumnName = "id")
	private EmailTemplate emailTemplateId;
	
	@Column(name = "sent_time", columnDefinition = "DATETIME")
	@Temporal(TemporalType.TIMESTAMP)
	private Date sentTime;
	
	@Column(name = "scheduler_time", columnDefinition = "DATETIME")
	@Temporal(TemporalType.TIMESTAMP)
	private Date schedulerTime;
	
	@Column(name="is_email_sent")
	private Boolean isEmailSent;
	
	@Column(name="email_status_message")
	private String emailStatusMessage;
		
}
