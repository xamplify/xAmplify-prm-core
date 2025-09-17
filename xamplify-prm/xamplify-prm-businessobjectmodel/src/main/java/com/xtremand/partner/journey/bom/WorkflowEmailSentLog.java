package com.xtremand.partner.journey.bom;

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

import lombok.Data;

@Entity
@Table(name="xt_workflow_email_sent_log")
@Data
public class WorkflowEmailSentLog implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator="workflow_email_email_log_id")
	@SequenceGenerator(
			name="workflow_email_email_log_id",
			sequenceName="workflow_email_email_log_id",
			allocationSize=1
			)
	@Column(name = "id")
	private Integer id;
	
	@Column(name = "user_id")  
	private Integer userId;
	
	@Column(name="workflow_id",nullable=true)
	private Integer workflowId;
	
	@Column(name = "sent_time", columnDefinition="DATETIME")
	@Temporal(TemporalType.TIMESTAMP)
	private Date sentTime;
	
	@Column(name="status_code")
	private Integer statusCode;
	
	@Column(name="learning_track_id",nullable=true)
	private Integer learningTrackId;

	
	

	

	

	
	
}
