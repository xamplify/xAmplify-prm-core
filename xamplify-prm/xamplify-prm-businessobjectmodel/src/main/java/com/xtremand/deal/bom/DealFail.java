package com.xtremand.deal.bom;

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
@Table(name="xt_deal_fail")
@Getter @Setter
public class DealFail extends XamplifyTimeStamp{

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "deal_fail_id_seq")
	@SequenceGenerator(name = "deal_fail_id_seq", sequenceName = "deal_fail_id_seq", allocationSize = 1)
	private Integer id;
	
	@OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="deal_id", referencedColumnName="id")
	Deal deal;
	
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
