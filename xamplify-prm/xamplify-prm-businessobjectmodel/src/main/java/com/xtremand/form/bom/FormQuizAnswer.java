package com.xtremand.form.bom;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name="xt_form_quiz_answers")
public class FormQuizAnswer implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4964767318425848719L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator="xt_form_quiz_answers_sequence")
	@SequenceGenerator(
			name="xt_form_quiz_answers_sequence",
			sequenceName="xt_form_quiz_answers_sequence",
			allocationSize=1
			)
	@Column(name = "id")
	private Integer id;
	
	@OneToOne
	@JsonIgnore
	@JoinColumn(name="form_label_choice_id")
	private FormLabelChoice formLabelChoice;

	public FormQuizAnswer() {
		super();
	}

	public FormQuizAnswer(FormLabelChoice formLabelChoice) {
		super();
		this.formLabelChoice = formLabelChoice;
	}
	
	
}
