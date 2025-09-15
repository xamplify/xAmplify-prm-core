package com.xtremand.lead.bom;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.xtremand.common.bom.XamplifyTimeStamp;
import com.xtremand.integration.bom.Integration;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name="xt_lead_fail")
@Getter @Setter
public class LeadFail extends XamplifyTimeStamp{
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "lead_fail_id_seq")
	@SequenceGenerator(name = "lead_fail_id_seq", sequenceName = "lead_fail_id_seq", allocationSize = 1)
	private Integer id;
	
	@OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="lead_id", referencedColumnName="id")
	Lead lead;
	
	@ManyToOne
	@JoinColumn(name="integration_id")
	private Integration integration;
	
	@Column(name = "external_organization_id")
	private String externalOrganizationId;
	
	@Column(name="created_by")
	Integer createdBy;
	
	@Column(name = "error_message")
	private String errorMessage;
}
