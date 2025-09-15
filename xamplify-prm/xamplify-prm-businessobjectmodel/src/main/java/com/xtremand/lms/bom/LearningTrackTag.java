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
import com.xtremand.tag.bom.Tag;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name="xt_learning_track_tag")
@Getter @Setter
public class LearningTrackTag extends XamplifyTimeStamp {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "xt_learning_track_tag_sequence")
	@SequenceGenerator(name = "xt_learning_track_tag_sequence", sequenceName = "xt_learning_track_tag_sequence", allocationSize = 1)
	private Integer id;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name = "learning_track_id") 
	private LearningTrack learningTrack;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name = "tag_id") 
	private Tag tag;
	
	@Column(name="created_by")
	private Integer createdBy;
}
