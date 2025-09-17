package com.xtremand.form.submit.bom;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.xtremand.form.bom.FormLabel;
import com.xtremand.form.bom.FormLabelChoice;


@Entity
@Table(name="xt_form_submit_multi_choice")
public class FormSubmitMultiChoice implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9017083840239104915L;
	
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator="xt_form_submit_multi_choice_sequence")
	@SequenceGenerator(
			name="xt_form_submit_multi_choice_sequence",
			sequenceName="xt_form_submit_multi_choice_sequence",
			allocationSize=1
			)
	@Column(name = "id")
	private Integer id;
	
	
	@ManyToOne
	@JsonIgnore
	@JoinColumn(name="form_submit_id")
	private FormSubmit formSubmit;
	
	@ManyToOne
	@JsonIgnore
	@JoinColumn(name="form_label_id")
	private FormLabel formLabel;
	
	
	@ManyToOne
	@JsonIgnore
	@JoinColumn(name="form_label_choice_id")
	private FormLabelChoice formLabelChoice;


	public Integer getId() {
		return id;
	}


	public void setId(Integer id) {
		this.id = id;
	}


	public FormSubmit getFormSubmit() {
		return formSubmit;
	}


	public void setFormSubmit(FormSubmit formSubmit) {
		this.formSubmit = formSubmit;
	}


	public FormLabel getFormLabel() {
		return formLabel;
	}


	public void setFormLabel(FormLabel formLabel) {
		this.formLabel = formLabel;
	}


	public FormLabelChoice getFormLabelChoice() {
		return formLabelChoice;
	}


	public void setFormLabelChoice(FormLabelChoice formLabelChoice) {
		this.formLabelChoice = formLabelChoice;
	}


	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	

}
