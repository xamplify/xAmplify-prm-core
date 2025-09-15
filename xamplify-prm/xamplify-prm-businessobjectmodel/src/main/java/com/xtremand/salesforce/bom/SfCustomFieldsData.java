package com.xtremand.salesforce.bom;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.xtremand.deal.bom.Deal;
import com.xtremand.form.bom.FormLabel;
import com.xtremand.lead.bom.Lead;

import lombok.Data;

@Data
@Entity
@Table(name = "xt_sf_cf_data")
public class SfCustomFieldsData implements Serializable {

	/**
	* 
	*/
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sf_cf_data_id_seq")
	@SequenceGenerator(name = "sf_cf_data_id_seq", sequenceName = "sf_cf_data_id_seq", allocationSize = 1)
	@Column(name = "id")
	private Integer id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "sf_cf_label_id", referencedColumnName = "id")
	private FormLabel formLabel;

	@Column(name = "value")
	private String value;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "deal_id")
	Deal deal;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "lead_id")
	Lead lead;

	@Column(name = "sf_cf_selected_choice_value")
	private String selectedChoiceValue;
}
