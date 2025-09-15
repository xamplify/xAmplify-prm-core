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
@Table(name = "xt_learning_track_content_partner_activity")
@Getter
@Setter
public class LearningTrackPartnerActivity extends XamplifyTimeStamp {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "xt_learning_track_content_partner_activity_sequence")
	@SequenceGenerator(name = "xt_learning_track_content_partner_activity_sequence", sequenceName = "xt_learning_track_content_partner_activity_sequence", allocationSize = 1)
	private Integer id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "learning_track_content_id")
	private LearningTrackContent learningTrackContent;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "learning_track_visibility_id")
	private LearningTrackVisibility visibilityUser;

	private String status;

	@org.hibernate.annotations.Type(type = "com.xtremand.lms.bom.PartnerActivityTypeType")
	private PartnerActivityType type;

	@Column(name = "created_by")
	private Integer createdBy;

}
