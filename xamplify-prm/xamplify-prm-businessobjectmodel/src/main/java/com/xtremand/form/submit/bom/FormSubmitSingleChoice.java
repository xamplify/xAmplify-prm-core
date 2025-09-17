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

import org.springframework.util.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.xtremand.form.bom.FormLabel;

@Entity
@Table(name="xt_form_submit_single_choice")
public class FormSubmitSingleChoice implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6729211352290150392L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator="xt_form_submit_single_choice_sequence")
	@SequenceGenerator(
			name="xt_form_submit_single_choice_sequence",
			sequenceName="xt_form_submit_single_choice_sequence",
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
	
	
	private String value;


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


	public String getValue() {
		return value;
	}


	public void setValue(String value) {
		if(StringUtils.hasText(value)) {
			if(value.length()>1000) {
				this.value = value.trim().substring(0,999);
			}else {
				this.value = value.trim();
			}
		}else {
			this.value = value;
		}
		
	}


	public static long getSerialversionuid() {
		return serialVersionUID;
	}

}
