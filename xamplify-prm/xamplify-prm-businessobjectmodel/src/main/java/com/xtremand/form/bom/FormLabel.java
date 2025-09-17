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
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.xtremand.form.submit.bom.FormSubmitMultiChoice;
import com.xtremand.form.submit.bom.FormSubmitSingleChoice;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;


@Entity
@Table(name="xt_form_label")
@Getter
@Setter
public class FormLabel implements Serializable,Comparable<FormLabel> {/**
	 * 
	 */
	private static final long serialVersionUID = -2516194789341635428L;
	
	
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator="xt_form_label_sequence")
	@SequenceGenerator(
			name="xt_form_label_sequence",
			sequenceName="xt_form_label_sequence",
			allocationSize=1
			)
	@Column(name = "id")
	private Integer id;
	
	
	@ManyToOne
	@JoinColumn(name="form_id")
	@JsonIgnore
	private Form form;
	
	
	@Column(name = "label_name")
	private String labelName;
	
	
	@ManyToOne
	@JoinColumn(name="label_type")
	@JsonIgnore
	private FormLabelType labelType;
	
	
	@Column(name = "label_id")
	private String labelId;
	
	
	@Column(name = "hidden_label_id")
	private String hiddenLabelId;
	
	
	@Column(name = "place_holder")
	private String placeHolder;
	
	
	@Column(name = "label_order")
	private Integer order;
	
	
	@Column(name = "is_required")
	private boolean required;
	
	@Column(name="label_length")
	private String labelLength;
	
	@Column(name="price_type")
	private String priceType;
	
	@ManyToOne
	@JoinColumn(name = "parent_label_id")
	private FormLabel parentLabelId;
	
	
	@Fetch(FetchMode.SUBSELECT)
	@OneToMany( mappedBy = "formLabel",cascade = CascadeType.ALL,fetch=FetchType.LAZY)
	private List<FormLabelChoice> formLabelChoices = new ArrayList<>();
	
	@Fetch(FetchMode.SUBSELECT)
	@Setter(AccessLevel.NONE)
	@OneToMany( mappedBy = "formLabel",cascade = CascadeType.ALL,fetch=FetchType.LAZY)
	private List<FormSubmitSingleChoice> formSubmitSingleChoices = new ArrayList<>();
	
	@Fetch(FetchMode.SUBSELECT)
	@Setter(AccessLevel.NONE)
	@OneToMany( mappedBy = "formLabel",cascade = CascadeType.ALL,fetch=FetchType.LAZY)
	private List<FormSubmitMultiChoice> formSubmitMultiChoices = new ArrayList<>();
	
	@Column(name="is_default_column")
	private boolean defaultColumn;
	
	@Column(name="description")
	private String description;
	
	@Column(name = "form_default_field_type")
	@org.hibernate.annotations.Type(type = "com.xtremand.form.bom.FormDefaultFieldType")
	private FormDefaultFieldTypeEnum formDefaultFieldType;
	
	@Column(name="display_name")
	private String displayName;
	
	@Column(name="column_order")
	private Integer columnOrder;
	
	@Column(name = "is_non_interactive")
	private boolean nonInteractive;
	
	@ManyToOne
	@JoinColumn(name="crm_original_type")
	@JsonIgnore
	private FormLabelType originalCRMType;
	
	
	@OneToOne
    @JoinColumn(name="default_choice_id")
    private FormLabelChoice defaultChoice;
	
	@Column(name="lookup_external_reference")
	private String lookUpExternalReference;
	
	@Column(name = "is_private")
	private boolean isPrivate;
	
	@Column(name = "form_lookup_default_field_type")
	@org.hibernate.annotations.Type(type = "com.xtremand.form.bom.FormLookUpDefaultFieldType")
	private FormLookUpDefaultFieldTypeEnum formLookUpDefaultFieldType;
	
	@Column(name = "is_active")
	private boolean isActive;
	
	@Column(name = "form_field_type")
	@org.hibernate.annotations.Type(type = "com.xtremand.form.bom.FormFieldType")
	private FormFieldTypeEnum formFieldType;
    
    @Transient
    private String defaultChoiceLabel;
    
    @Column(name = "is_email_notification_enabled_on_update")
    private boolean emailNotificationEnabledOnUpdate;
	

	@Override
	public int compareTo(FormLabel o) {
		if(this.getOrder()<o.getOrder()){
			return -1;
		}else if(this.getOrder() == o.getOrder()){
			return 0;
		}else{
			return 1;
		}
	}


	public static long getSerialversionuid() {
		return serialVersionUID;
	}




	public void setFormSubmitSingleChoices(List<FormSubmitSingleChoice> formSubmitSingleChoices) {
		this.formSubmitSingleChoices = formSubmitSingleChoices;
	}


	public void setFormSubmitMultiChoices(List<FormSubmitMultiChoice> formSubmitMultiChoices) {
		this.formSubmitMultiChoices = formSubmitMultiChoices;
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((formLabelChoices == null) ? 0 : formLabelChoices.hashCode());
		result = prime * result + ((formSubmitMultiChoices == null) ? 0 : formSubmitMultiChoices.hashCode());
		result = prime * result + ((formSubmitSingleChoices == null) ? 0 : formSubmitSingleChoices.hashCode());
		result = prime * result + ((hiddenLabelId == null) ? 0 : hiddenLabelId.hashCode());
		result = prime * result + ((labelId == null) ? 0 : labelId.hashCode());
		result = prime * result + ((labelName == null) ? 0 : labelName.hashCode());
		result = prime * result + ((labelType == null) ? 0 : labelType.hashCode());
		result = prime * result + ((order == null) ? 0 : order.hashCode());
		result = prime * result + ((placeHolder == null) ? 0 : placeHolder.hashCode());
		result = prime * result + (required ? 1231 : 1237);
		return result;
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		FormLabel other = (FormLabel) obj;
		if (formLabelChoices == null) {
			if (other.formLabelChoices != null) {
				return false;
			}
		} else if (!formLabelChoices.equals(other.formLabelChoices)) {
			return false;
		}
		if (formSubmitMultiChoices == null) {
			if (other.formSubmitMultiChoices != null) {
				return false;
			}
		} else if (!formSubmitMultiChoices.equals(other.formSubmitMultiChoices)) {
			return false;
		}
		if (formSubmitSingleChoices == null) {
			if (other.formSubmitSingleChoices != null) {
				return false;
			}
		} else if (!formSubmitSingleChoices.equals(other.formSubmitSingleChoices)) {
			return false;
		}
		if (hiddenLabelId == null) {
			if (other.hiddenLabelId != null) {
				return false;
			}
		} else if (!hiddenLabelId.equals(other.hiddenLabelId)) {
			return false;
		}
		if (labelId == null) {
			if (other.labelId != null) {
				return false;
			}
		} else if (!labelId.equals(other.labelId)) {
			return false;
		}
		if (labelName == null) {
			if (other.labelName != null) {
				return false;
			}
		} else if (!labelName.equals(other.labelName)) {
			return false;
		}
		if (labelType == null) {
			if (other.labelType != null) {
				return false;
			}
		} else if (!labelType.equals(other.labelType)) {
			return false;
		}
		if (order == null) {
			if (other.order != null) {
				return false;
			}
		} else if (!order.equals(other.order)) {
			return false;
		}
		if (placeHolder == null) {
			if (other.placeHolder != null) {
				return false;
			}
		} else if (!placeHolder.equals(other.placeHolder)) {
			return false;
		}
		if (required != other.required) {
			return false;
		}
		return true;
	}


	
	
	

}
