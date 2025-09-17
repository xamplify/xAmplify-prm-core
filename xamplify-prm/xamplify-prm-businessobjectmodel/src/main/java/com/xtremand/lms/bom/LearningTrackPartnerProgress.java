package com.xtremand.lms.bom;

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

import com.xtremand.common.bom.XamplifyTimeStamp;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name="xt_learning_track_partner_progress")
@Getter @Setter
public class LearningTrackPartnerProgress extends XamplifyTimeStamp{
	
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "xt_learning_track_partner_progress_sequence")
	@SequenceGenerator(name = "xt_learning_track_partner_progress_sequence", sequenceName = "xt_learning_track_partner_progress_sequence", allocationSize = 1)
	private Integer id;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name = "learning_track_visibility_id")
	private LearningTrackVisibility visibilityUser;
	
	private Integer progress;
	
	@Column(name="created_by")
	private Integer createdBy;
	
}
