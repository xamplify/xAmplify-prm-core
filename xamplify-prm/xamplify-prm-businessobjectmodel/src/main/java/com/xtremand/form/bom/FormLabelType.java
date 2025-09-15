package com.xtremand.form.bom;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import lombok.Data;

@Entity
@Table(name="xt_form_label_type")
@Data
public class FormLabelType implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6297230910775813424L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator="xt_form_label_type_sequence")
	@SequenceGenerator(
			name="xt_form_label_type_sequence",
			sequenceName="xt_form_label_type_sequence",
			allocationSize=1
			)
	@Column(name = "id")
	private Integer id;
	
	@Column(name="label_type_id")
	private Integer labelTypeId;
	
	@Column(name="label_type")
	private String labelType;
	
	@OneToMany(mappedBy="labelType")
	private List<FormLabel> formLabels;

}
