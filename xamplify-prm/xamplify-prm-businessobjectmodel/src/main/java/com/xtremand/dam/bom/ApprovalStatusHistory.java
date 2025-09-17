package com.xtremand.dam.bom;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
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

import org.hibernate.annotations.Type;

import com.xtremand.lms.bom.LearningTrack;
import com.xtremand.user.bom.User;
import com.xtremand.util.bom.ModuleType;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "xt_approval_status_history")
@EqualsAndHashCode(callSuper = false)
@Getter
@Setter
public class ApprovalStatusHistory  implements Serializable{
	
	private static final long serialVersionUID = -1317030915287408475L;

	private static final String DAM_STATUS_HISTORY_SEQUENCE = "xt_approval_status_history_sequence";

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = DAM_STATUS_HISTORY_SEQUENCE)
	@SequenceGenerator(name = DAM_STATUS_HISTORY_SEQUENCE, sequenceName = DAM_STATUS_HISTORY_SEQUENCE, allocationSize = 1)
	@Column(name = "id", nullable = false)
	private Integer id;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "dam_id")
	private Dam dam;
	
	@Column(name = "status", nullable = false)
	@Type(type = "com.xtremand.dam.bom.ApprovalStatusTypeType")
	@Enumerated(EnumType.STRING)
	private ApprovalStatusType status;
	
	@Column( name = "comment") 
	private String comment;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "created_by", nullable = false)
	private User createdBy;

	@Column(name = "created_time", columnDefinition = "DATETIME", nullable = false)
	@Temporal(TemporalType.TIMESTAMP)
	private Date createdTime;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "learning_track_id")
	private LearningTrack learningTrack;
	
	@Column(name = "module_type", nullable = false)
	@Type(type = "com.xtremand.util.bom.ModuleTypeType")
	@Enumerated(EnumType.STRING)
	private ModuleType moduleType;

}
