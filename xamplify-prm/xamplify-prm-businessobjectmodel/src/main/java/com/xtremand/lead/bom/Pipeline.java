package com.xtremand.lead.bom;

import java.util.List;

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
import javax.persistence.Transient;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.xtremand.common.bom.CompanyProfile;
import com.xtremand.common.bom.XamplifyTimeStamp;
import com.xtremand.deal.bom.Deal;
import com.xtremand.integration.bom.Integration.IntegrationType;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "xt_pipeline")
@Getter
@Setter
public class Pipeline extends XamplifyTimeStamp {
	/**
	 * 
	 */
	private static final long serialVersionUID = 793127316258897237L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pipeline_id_seq")
	@SequenceGenerator(name = "pipeline_id_seq", sequenceName = "pipeline_id_seq", allocationSize = 1)
	private Integer id;

	@ManyToOne
	@JoinColumn(name = "company_id")
	private CompanyProfile company;

	private String name;

	@org.hibernate.annotations.Type(type = "com.xtremand.lead.bom.PipelineTypeType")
	private PipelineType type;

	@Fetch(FetchMode.SUBSELECT)
	@OneToMany(mappedBy = "pipeline", orphanRemoval = true, fetch = FetchType.LAZY)
	private List<PipelineStage> stages;

	@Column(name = "created_by")
	Integer createdBy;

	@Fetch(FetchMode.SUBSELECT)
	@OneToMany(mappedBy = "pipeline", fetch = FetchType.LAZY)
	private List<Lead> leads;

	@Fetch(FetchMode.SUBSELECT)
	@OneToMany(mappedBy = "pipeline", fetch = FetchType.LAZY)
	private List<Deal> deals;

	@Column(name = "is_private")
	private boolean isPrivate = false;

	@Column(name = "is_salesforce_pipeline")
	private boolean isSalesforcePipeline = false;

	@Column(name = "is_default")
	private boolean isDefault = false;

	@org.hibernate.annotations.Type(type = "com.xtremand.integration.bom.IntegrationTypeType")
	@Column(name = "integration_type")
	private IntegrationType integrationType = IntegrationType.XAMPLIFY;

	@Column(name = "external_pipeline_id")
	private String externalPipelineId;

	@Transient
	private PipelineStage selectedStage;

}
