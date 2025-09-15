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

import com.xtremand.common.bom.CompanyProfile;
import com.xtremand.user.bom.User;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "xt_task_activity")
@Getter
@Setter
public class TaskActivity {
	
private static final String SEQUENCE = "xt_task_activity_sequence";
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SEQUENCE)
	@SequenceGenerator(name = SEQUENCE, sequenceName = SEQUENCE, allocationSize = 1)
	private Integer id;
	
	@Column(name = "name")
	private String name;
	
	@Column(name = "description")
	private String description;
	
	@Column(name = "due_date")
	private Date dueDate;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "status")
	private TaskActivityStatus status;
	
	@Column(name = "created_time")
	private Date createdTime;
	
	@Column(name = "updated_time")
	private Date updatedTime;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "assigned_to")
	private User assignedTo;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "assigned_by")
	private User assignedBy;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "contact_id")
	private User contact;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "updated_by")
	private User updatedBy;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "company_id")
	private CompanyProfile company;
	
	@Column(name = "task_type")
	@Type(type = "com.xtremand.activity.bom.TaskActivityTypeType")
	private TaskActivityTypeEnum taskType;
	
	@Column(name = "priority")
	@Type(type = "com.xtremand.activity.bom.TaskActivityPriorityType")
	private TaskActivityPriorityEnum priority;
	
	@Column(name = "is_alert_email_sent")
	private Boolean isAlertEmailSent;
	
	@Column(name = "remainder")
	private String remainder;
	
	@Column(name = "remainder_type")
	@Type(type = "com.xtremand.activity.bom.TaskActivityRemainderType")
	private TaskActivityRemainderEnum remainderType;

}
