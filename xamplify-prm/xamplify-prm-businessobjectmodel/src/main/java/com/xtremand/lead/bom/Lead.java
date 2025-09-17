package com.xtremand.lead.bom;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.GenerationTime;
import org.hibernate.annotations.GeneratorType;
import org.hibernate.annotations.Type;

import com.xtremand.common.bom.CompanyProfile;
import com.xtremand.common.bom.XamplifyTimeStamp;
import com.xtremand.deal.bom.Deal;
import com.xtremand.form.submit.bom.FormSubmit;
import com.xtremand.salesforce.bom.SfCustomFieldsData;
import com.xtremand.user.bom.User;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "xt_lead")
@Getter
@Setter
public class Lead extends XamplifyTimeStamp {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "lead_id_seq")
	@SequenceGenerator(name = "lead_id_seq", sequenceName = "lead_id_seq", allocationSize = 1)
	private Integer id;

	@Column(name = "first_name")
	private String firstName;

	@Column(name = "last_name")
	private String lastName;

	private String company;
	private String email;
	private String phone;
	private String website;
	private String street;
	private String city;
	private String state;
	private String country;
	private String title;
	private String industry;
	private String region;

	@Column(name = "postal_code")
	private String postalCode;


	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "associated_user_id")
	private User associatedUser;

	@ManyToOne
	@JoinColumn(name = "created_by_company_id")
	private CompanyProfile createdByCompany;

	@ManyToOne
	@JoinColumn(name = "created_for_company_id")
	private CompanyProfile createdForCompany;

	@ManyToOne
	@JoinColumn(name = "pipeline_id")
	private Pipeline pipeline;

	@ManyToOne
	@JoinColumn(name = "pipeline_stage_id")
	private PipelineStage currentStage;

	@Column(name = "created_by")
	Integer createdBy;

	@OneToMany(mappedBy = "lead", orphanRemoval = true, fetch = FetchType.LAZY)
	private List<LeadStage> leadStages;

	@Column(name = "sf_lead_id")
	private String sfLeadId;

	@OneToOne(mappedBy = "associatedLead", fetch = FetchType.LAZY)
	private Deal associatedDeal;

	@Column(name = "microsoft_dynamics_lead_id")
	private String microsoftDynamicsLeadId;

	@Column(name = "hubspot_lead_id")
	private Long hubspotLeadId;

	/* XNFR-215 */
	@Column(name = "pipedrive_lead_id")
	private String pipedriveLeadId;

	/* XNFR-230 */
	@OneToOne(mappedBy = "lead")
	private LeadFail leadFail;

	/* XNFR-403 */
	@Column(name = "connectwise_lead_id")
	private Long connectwiseLeadId;

	/* XNFR-403 */
	@Column(name = "connectwise_lead_company_id")
	private Long connectwiseLeadCompanyId;

	/****** XNFR-426 ***/
	@Column(name = "approval_status")
	@Type(type = "com.xtremand.lead.bom.LeadApprovalStatusType")
	private LeadApprovalStatusEnum leadApprovalStatusType;

	@Column(name = "approval_status_updated_time")
	private Date approvalStatusUpdatedTime;

	@Column(name = "approval_status_comment")
	private String approvalStatusComment;
	/***********/

	/**** XNFR-503 ****/
	@Column(name = "halopsa_lead_id")
	private Long halopsaLeadId;

	@Column(name = "halopsa_tickettype_id")
	private Long halopsaTicketTypeId;
	
	/* XNFR-528 */
	@Column(name="zoho_lead_id")
	private String zohoLeadId;
	
	/* XNFR-521 */
	@ManyToOne
	@JoinColumn(name = "created_by_pipeline_id")
	private Pipeline createdByPipeline;

	@ManyToOne
	@JoinColumn(name = "created_by_pipeline_stage_id")
	private PipelineStage createdByPipelineStage;

	@ManyToOne
	@JoinColumn(name = "created_for_pipeline_id")
	private Pipeline createdForPipeline;

	@ManyToOne
	@JoinColumn(name = "created_for_pipeline_stage_id")
	private PipelineStage createdForPipelineStage;

	//XNFR-615
	@OneToMany(fetch = FetchType.LAZY, mappedBy="lead", cascade=CascadeType.ALL)
	private List<SfCustomFieldsData> sfCustomFieldsData = new ArrayList<>();
	
	@GeneratorType(type = LeadReferenceIdGenerator.class, when = GenerationTime.INSERT)
    @Column(name = "crm_reference_id")
    private String referenceId;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "form_submit_id")
	private FormSubmit formSubmit;

	@Transient
	private boolean pushToMarketo;

	@Transient
	private boolean pushToHubspot;

	@Transient
	private boolean pushToSalesforce;

	@Transient
	private String createdByName;

	@Transient
	private String createdByEmail;

	@Transient
	private String createdByCompanyName;

	@Transient
	private Integer forCompanyUserId;

	@Transient
	private boolean pushToMicrosoftDynamics;

	@Transient
	private Long externalContactId;

	// XNFR_221
	@Transient
	private boolean isUpdated;

	// XNFR-426
	@Transient
	private String leadComment;

	// XNFR-463
	@Transient
	private boolean fromAutoSync;

	// XNFR-461
	@Transient
	private Integer pushToCRMUserId;

	@Transient
	private PipelineStage pushToCRMStage;

	@Transient
	private PipelineStage pushToCRMOldStage;
	
	@Transient
	private boolean isSFAccountIdExist;

}
