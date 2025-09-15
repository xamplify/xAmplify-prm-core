package com.xtremand.lms.bom;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.xtremand.common.bom.XamplifyTimeStamp;
import com.xtremand.partnership.bom.Partnership;
import com.xtremand.user.bom.TeamMember;
import com.xtremand.user.bom.User;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "xt_learning_track_visibility")
@Getter
@Setter
public class LearningTrackVisibility extends XamplifyTimeStamp {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "xt_learning_track_visibility_sequence")
	@SequenceGenerator(name = "xt_learning_track_visibility_sequence", sequenceName = "xt_learning_track_visibility_sequence", allocationSize = 1)
	private Integer id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "learning_track_id")
	private LearningTrack learningTrack;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "partnership_id")
	private Partnership partnership;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	private User user;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "team_member_id")
	private TeamMember teamMember;

	@Column(name = "is_associated_through_company")
	private boolean associatedThroughCompany;

	@OneToMany(mappedBy = "learningTrackVisibility", orphanRemoval = true, fetch = FetchType.LAZY)
	private Set<LearningTrackVisibilityGroup> groups = new HashSet<>();

	private Integer progress;

	@Column(name = "created_by")
	private Integer createdBy;

	@Column(name = "is_published", nullable = false)
	private boolean published;

	@Column(name = "published_on", columnDefinition = "DATETIME")
	@Temporal(TemporalType.TIMESTAMP)
	private Date publishedOn;

}
