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
import com.xtremand.user.bom.UserList;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name="xt_learning_track_visibility_group")
@Getter @Setter
public class LearningTrackVisibilityGroup extends XamplifyTimeStamp{
	/**
	 * 
	 */
	private static final long serialVersionUID = 9035286833443045507L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "xt_learning_track_visibility_group_sequence")
	@SequenceGenerator(name = "xt_learning_track_visibility_group_sequence", sequenceName = "xt_learning_track_visibility_group_sequence", allocationSize = 1)
	private Integer id;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name = "visibility_id") 
	private LearningTrackVisibility learningTrackVisibility;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name = "user_list_id") 
	private UserList userList;
	
	@Column(name="created_by")
	private Integer createdBy;
}
