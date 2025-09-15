package com.xtremand.deal.bom;

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

import com.xtremand.common.bom.CompanyProfile;
import com.xtremand.common.bom.XamplifyTimeStamp;
import com.xtremand.lead.bom.Lead;
import com.xtremand.lead.bom.Pipeline;
import com.xtremand.lead.bom.PipelineStage;
import com.xtremand.salesforce.bom.SfCustomFieldsData;
import com.xtremand.user.bom.User;
import com.xtremand.user.bom.UserUserList;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "xt_deal")
@Getter
@Setter
public class Deal extends XamplifyTimeStamp {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "deal_id_seq")
	@SequenceGenerator(name = "deal_id_seq", sequenceName = "deal_id_seq", allocationSize = 1)
	private Integer id;

	private String title;

	@Column(name = "deal_type")
	private String dealType;

	private Double amount;

	@Column(name = "close_date")
	private Date closeDate;

	@OneToOne
	@JoinColumn(name = "associated_lead_id")
	private Lead associatedLead;

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

	@Column(name = "sf_deal_id")
	private String sfDealId;

	@Column(name = "hubspot_deal_id")
	private Long hubspotDealId;

	@Column(name = "created_by")
	Integer createdBy;

	/* XNFR-215 */
	@Column(name = "pipedrive_deal_id")
	private String pipedriveDealId;

	/* XNFR-426 */
	@Column(name = "comments")
	private String dealComment;

	@OneToMany(mappedBy = "deal", orphanRemoval = true, fetch = FetchType.LAZY)
	private List<DealStage> dealStages;

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "deal", cascade = CascadeType.ALL)
	private List<SfCustomFieldsData> sfCustomFieldsData = new ArrayList<>();

	@Column(name = "microsoft_dynamics_deal_id")
	private String microsoftDynamicsDealId;

	/* XNFR-230 */
	@OneToOne(mappedBy = "deal")
	private DealFail dealFail;

	/* XNFR-403 */
	@Column(name = "connectwise_deal_id")
	private String connectwiseDealId;

	/* XNFR-502 */
	@Column(name = "halopsa_deal_id")
	private Long haloPSADealId;

	/* XNFR-528 */
	@Column(name = "zoho_deal_id")
	private String zohoDealId;

	@Column(name = "connectwise_forecast_items_json")
	private String connectwiseForecastItemsJson;

	/* XNFR-461 */
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

	/* XNFR-502 */
	@Column(name = "halopsa_tickettype_id")
	private Long haloPSATickettypeId;

	/* XNFR-575 */

	@GeneratorType(type = ReferenceIdGenerator.class, when = GenerationTime.INSERT)
	@Column(name = "crm_reference_id")
	private String referenceId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "associated_contact_id")
	private UserUserList associatedContact;

	@Transient
	private String createdByName;

	@Transient
	private String createdByEmail;

	@Transient
	private String createdByCompanyName;

	@Transient
	private Integer forCompanyUserId;

	@Transient
	private String role;

	@Transient
	private String oldRole;

	@Transient
	private PipelineStage oldStage;

	@Transient
	private PipelineStage createdForOldStage;

	@Transient
	private PipelineStage createdByOldStage;

	// XNFR_221
	@Transient
	private boolean isUpdated;

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
	private String exceptionMessage;

}
