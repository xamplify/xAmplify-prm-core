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
import com.xtremand.dam.bom.Dam;
import com.xtremand.form.bom.Form;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name="xt_learning_track_content")
@Getter @Setter
public class LearningTrackContent extends XamplifyTimeStamp{
	/**
	 * 
	 */
	private static final long serialVersionUID = -6309608004023780525L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "xt_learning_track_content_sequence")
	@SequenceGenerator(name = "xt_learning_track_content_sequence", sequenceName = "xt_learning_track_content_sequence", allocationSize = 1)
	private Integer id;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name = "learning_track_id") 
	private LearningTrack learningTrack;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name = "dam_id") 
	private Dam dam;
	
	@Column(name = "display_index")
	private Integer displayIndex;
	
	@Column(name="created_by")
	private Integer createdBy;
	
	@ManyToOne
	@JoinColumn(name = "quiz_id")
	private Form quiz;
}
