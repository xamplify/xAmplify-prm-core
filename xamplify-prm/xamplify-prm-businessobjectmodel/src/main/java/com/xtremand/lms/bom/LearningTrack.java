package com.xtremand.lms.bom;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
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
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.hibernate.annotations.Type;

import com.xtremand.category.bom.CategoryModule;
import com.xtremand.common.bom.CompanyProfile;
import com.xtremand.common.bom.XamplifyTimeStamp;
import com.xtremand.dam.bom.ApprovalStatusType;
import com.xtremand.form.bom.Form;
import com.xtremand.form.submit.bom.FormSubmit;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "xt_learning_track")
@Getter
@Setter
public class LearningTrack extends XamplifyTimeStamp {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "xt_learning_track_sequence")
	@SequenceGenerator(name = "xt_learning_track_sequence", sequenceName = "xt_learning_track_sequence", allocationSize = 1)
	private Integer id;

	@ManyToOne
	@JoinColumn(name = "company_id")
	private CompanyProfile company;

	private String title;
	private String description;
	private String slug;

	@Column(name = "featured_image")
	private String featuredImage;

	@Column(name = "is_published")
	private boolean published;

	@Column(name = "published_time", columnDefinition = "DATETIME")
	@Temporal(TemporalType.TIMESTAMP)
	Date publishedTime;

	@ManyToOne
	@JoinColumn(name = "quiz_id")
	private Form quiz;

	@Column(name = "is_follow_asset_sequence")
	private boolean followAssetSequence;

	@OneToMany(mappedBy = "learningTrack", orphanRemoval = true, fetch = FetchType.LAZY)
	private Set<LearningTrackContent> contents = new HashSet<>();

	@OneToMany(mappedBy = "learningTrack", orphanRemoval = true, fetch = FetchType.LAZY)
	private Set<LearningTrackVisibility> visibilityUsers = new HashSet<>();

	@OneToMany(mappedBy = "learningTrack", orphanRemoval = true, fetch = FetchType.LAZY)
	private Set<LearningTrackTag> tags = new HashSet<>();

	@Column(name = "created_by")
	private Integer createdBy;

	@OneToMany(mappedBy = "learningTrack", fetch = FetchType.LAZY)
	private Set<FormSubmit> formSubmit = new HashSet<>();

	@org.hibernate.annotations.Type(type = "com.xtremand.lms.bom.LearningTrackTypeType")
	private LearningTrackType type;

	@OneToOne(mappedBy = "learningTrack", cascade = CascadeType.ALL)
	private CategoryModule categoryModule;

	/********* XNFR-327 ***********/
	@Column(name = "is_white_labeled_track_or_playbook_shared_with_partners")
	private boolean whiteLabeledTrackOrPlaybookSharedWithPartners;

	@Column(name = "is_publishing_or_white_labeling_in_progress")
	private boolean publishingOrWhiteLabelingInProgress;

	/********* XNFR-327 ***********/

	/********* XNFR-342 ***********/
	@Column(name = "is_publishing_to_partner_list")
	private boolean publishingToPartnerList;

	@Column(name = "is_track_updated_email_notification")
	private boolean trackUpdatedEmailNotification;

	@Transient
	private boolean loadVisibilityUsersFromDb;

	@Transient
	private List<Integer> existingVisibilityUserIds = new ArrayList<>();

	@Transient
	private List<Integer> progressedVisibilityUserIds = new ArrayList<>();

	@Transient
	private boolean publishedTrackUpated;
	
	@Column(name="is_added_to_quick_links")
	private boolean addedToQuickLinks;
	
	@Column(name="is_group_by_assets")
	private boolean groupByAssets;
	
	/** XNFR-824 **/
	@Column(name = "approval_status")
	@Type(type = "com.xtremand.dam.bom.ApprovalStatusTypeType")
	@Enumerated(EnumType.STRING)
	private ApprovalStatusType approvalStatus;
	
	@Column(name = "approval_status_updated_by")
	private Integer approvalStatusUpdatedBy;
	
	@Column(name = "approval_status_updated_time", columnDefinition = "DATETIME")
	@Temporal(TemporalType.TIMESTAMP)
	private Date approvalStatusUpdatedTime;
	
	@Column(name = "expire_date", columnDefinition = "DATETIME")
	@Temporal(TemporalType.TIMESTAMP)
	private Date expireDate; 	

	@Transient
	@Getter
	@Setter
	private String cdnFeaturedImage;
}
