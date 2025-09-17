package com.xtremand.form.bom;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.xtremand.form.submit.bom.FormSubmitMultiChoice;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name="xt_form_label_choice")
public class FormLabelChoice implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7230944494048465270L;
	
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator="xt_form_label_choice_sequence")
	@SequenceGenerator(
			name="xt_form_label_choice_sequence",
			sequenceName="xt_form_label_choice_sequence",
			allocationSize=1
			)
	@Column(name = "id")
	private Integer id;
	
	
	@ManyToOne
	@JoinColumn(name="form_label_id")
	@JsonIgnore
	private FormLabel formLabel;
	
	@Column(name="label_choice_name")
	private String labelChoiceName;
	
	
	@Column(name="label_choice_id")
	private String labelChoiceId;
	
	@Column(name="label_choice_hidden_id")
	private String labelChoiceHiddenId;
	
	@Fetch(FetchMode.SUBSELECT)
	@Setter(AccessLevel.NONE)
	@OneToMany( mappedBy = "formLabelChoice",cascade = CascadeType.ALL,fetch=FetchType.LAZY)
	private List<FormSubmitMultiChoice> formSubmitMultiChoices = new ArrayList<>();
	
	@Column(name = "is_default_column")
	private boolean defaultColumn;
	
	@ManyToMany(fetch=FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinTable(name = "xt_form_label_dependent_choices",
    joinColumns = @JoinColumn(name = "choice_id"),
    inverseJoinColumns = @JoinColumn(name = "parent_choice_id"))
    private List<FormLabelChoice> parentChoices  = new ArrayList<>();
    
    @ManyToMany(mappedBy = "parentChoices", fetch=FetchType.LAZY)    
    private List<FormLabelChoice> dependentChoices  = new ArrayList<>();
	
	
	@OneToOne( mappedBy = "formLabelChoice", orphanRemoval = true,cascade = CascadeType.ALL,fetch=FetchType.LAZY)
	private FormQuizAnswer formQuizAnswer;

	public void setFormQuizAnswer(FormQuizAnswer formQuizAnswer) {
		this.formQuizAnswer = null;

		if (formQuizAnswer != null) {
			this.formQuizAnswer = formQuizAnswer;
		}
	}
	

}
