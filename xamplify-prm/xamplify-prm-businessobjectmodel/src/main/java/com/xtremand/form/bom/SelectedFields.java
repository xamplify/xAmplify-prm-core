package com.xtremand.form.bom;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.xtremand.integration.bom.Integration;
import com.xtremand.util.bom.XamplifyDefaultFieldsEntity;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "xt_selected_fields")
@Getter
@Setter
public class SelectedFields extends XamplifyDefaultFieldsEntity {

	private static final String SEQUENCE = "xt_selected_fields_seq";

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SEQUENCE)
	@SequenceGenerator(name = SEQUENCE, sequenceName = SEQUENCE, allocationSize = 1)
	@Column(name = "id")
	private Integer id;

	@Column(name = "label_name")
	private String labelName;

	@Column(name = "display_name")
	private String displayName;

	@Column(name = "label_id")
	private String labelId;

	@ManyToOne
	@JoinColumn(name = "form_id")
	@JsonIgnore
	private Form form;

	@ManyToOne
	@JoinColumn(name = "integration_id")
	private Integration integration;
	
	@Column(name="column_order")
	private Integer columnOrder;
	
	@Column(name = "is_selected_column")
	private boolean selectedColumn;
	
	@Column(name = "my_preferences_enabled")
	private boolean myPreferencesEnabled;
	
	@Column(name = "is_default_column")
	private boolean defaultColumn;
	
	
	/****** XNFR-426 ***/
	@Column(name = "opportunity_type", columnDefinition = "opportunity_type")
	@Type(type = "com.xtremand.form.bom.OpportunityType")
	private OpportunityTypeEnum opportunityType;

}
