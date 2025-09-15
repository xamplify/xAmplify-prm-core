package com.xtremand.lead.bom;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;


@Entity
@Table(name = "xt_lead_fields_settings")
@Getter
@Setter
public class LeadCustomField {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "xt_lead_fields_settings_sequence")
	@SequenceGenerator(name = "xt_lead_fields_settings_sequence", sequenceName = "xt_lead_fields_settings_sequence", allocationSize = 1)
	private Integer id;
	
	@ManyToOne
	@JoinColumn(name = "lead_field_id")
	private LeadField leadFieldId;
	
	@Column(name = "label_name")
	private String labelName;
	
	@Column(name = "company_id")
	private Integer companyId;
	
	
	
	@Column(name = "label_id")
	private String labelId;
	
	@Column(name = "placeholder")
	private String placeholder;
	
	@Column(name = "display_name")
	private String displayName;
	
	@Column(name = "created_by")
	private Integer createdBy;
	
	@Column(name = "updated_by")
	private Integer updatedBy;

	@Column(name = "created_time")
	private Date createdTime;
	
	@Column(name = "updated_time")
	private Date updatedTime;
	
	//XNFR-602
	@Column(name = "display_index")
	private Integer displayIndex;
	
}
