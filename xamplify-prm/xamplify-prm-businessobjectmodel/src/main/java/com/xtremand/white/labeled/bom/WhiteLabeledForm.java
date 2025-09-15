package com.xtremand.white.labeled.bom;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.xtremand.form.bom.Form;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "xt_white_labeled_forms")
@Getter
@Setter
public class WhiteLabeledForm extends WhiteLabeledContentMappedSuperClass implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7588459847965360778L;

	private static final String SEQUENCE = "xt_white_labeled_forms_sequence";

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SEQUENCE)
	@SequenceGenerator(name = SEQUENCE, sequenceName = SEQUENCE, allocationSize = 1)
	@Column(name = "id", nullable = false)
	private Integer id;

	@ManyToOne
	@JoinColumn(name = "vendor_company_form_id", nullable = true)
	private Form vendorCompanyForm;

	@OneToOne
	@JoinColumn(name = "received_white_labeled_form_id", unique = true, nullable = false)
	private Form receivedForm;

}
