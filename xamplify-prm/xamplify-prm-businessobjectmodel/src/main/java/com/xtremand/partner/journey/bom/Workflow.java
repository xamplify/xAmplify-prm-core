package com.xtremand.partner.journey.bom;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
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

import com.xtremand.campaign.bom.WorkflowsStatusEnum;
import com.xtremand.common.bom.CompanyProfile;
import com.xtremand.common.bom.XamplifyTimeStamp;
import com.xtremand.lms.bom.LearningTrack;
import com.xtremand.mail.bom.EmailTemplate;
import com.xtremand.user.bom.User;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "xt_workflow")
@Getter
@Setter
public class Workflow extends XamplifyTimeStamp implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4793435140561980237L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "workflow_id_seq")
	@SequenceGenerator(name = "workflow_id_seq", sequenceName = "workflow_id_seq", allocationSize = 1)
	private Integer id;

	private String title;

	@Column(name = "filter_query_json")
	private String filterQueryJson;

	@ManyToOne
	@JoinColumn(name = "company_id")
	private CompanyProfile company;

	@ManyToOne
	@JoinColumn(name = "trigger_subject_id")
	private TriggerComponent triggerSubject;

	@ManyToOne
	@JoinColumn(name = "trigger_action_id")
	private TriggerComponent triggerAction;

	@ManyToOne
	@JoinColumn(name = "trigger_time_phrase_id")
	private TriggerComponent triggerTimePhrase;

	@Column(name = "notification_subject")
	private String notificationSubject;

	@Column(name = "pre_header")
	private String preHeader;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "from_email_user_id")
	private User fromEmailUser;

	@Column(name = "notification_message")
	private String notificationMessage;

	@ManyToOne
	@JoinColumn(name = "notification_template_id")
	private EmailTemplate template;

	@Column(name = "status")
	@org.hibernate.annotations.Type(type = "com.xtremand.campaign.bom.WorkflowsEnumType")
	private WorkflowsStatusEnum status;

	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "workflow")
	private Set<WorkflowUserlist> userlists = new HashSet<WorkflowUserlist>();

	@Column(name = "custom_trigger_days")
	private Integer customTriggerDays;

	@Column(name = "created_by")
	private Integer createdBy;

	@Column(name = "is_scheduler_in_progress")
	private boolean schedulerInProgress;

	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "workflow")
	private Set<WorkflowPartner> partners = new HashSet<WorkflowPartner>();
	
	@Column(name = "is_partner_group_selected")
	private boolean partnerGroupSelected;

	@ManyToOne
	@JoinColumn(name = "learning_track_id")
	private LearningTrack learningTrack;
	
}
