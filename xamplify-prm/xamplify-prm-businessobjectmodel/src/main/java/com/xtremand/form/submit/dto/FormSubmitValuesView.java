package com.xtremand.form.submit.dto;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Immutable;

import lombok.Data;

@Entity
@Table(name = "v_xt_form_submit_values")
@Immutable
@Data
public class FormSubmitValuesView {
	
	@Id
	@Column(name = "id")
	private Integer id;
	
	@Column(name = "form_id")
	private Integer formId;
	
	@Column(name = "form_name")
	private String formName;
	
	@Column(name = "alias")
	private String alias;
	
	@Column(name = "description")
	private String description;
	
	@Column(name = "customer_company_id")
	private Integer companyId;
	
	@Column(name = "created_user_id")
	private Integer createdUserId;
	
	@Column(name = "created_time")
	private Date createdTime;
	
	@Column(name = "form_label_id")
	private Integer labelId;
	
	@Column(name = "label_name")
	private String labelName;
	
	@Column(name = "value")
	private String value;
	
	@Column(name = "label_type_id")
	private Integer lableTypeId;
	
	@Column(name = "label_type")
	private String lableType;
	
	@Column(name = "form_label_choice_id")
	private String labelChoiceId;
	
	@Column(name = "form_submit_id")
	private Integer formSubmitId;
	
	@Column(name = "label_choice_name")
	private String labelChoiceName;
	
	@Column(name = "campaign_id")
	private Integer campaignId;
	
	@Column(name = "user_id")
	private Integer userId;
	
	@Column(name = "rsvp_time")
	private Date rsvpTime;
	
	@Column(name = "message")
	private String message;
	
	@Column(name = "rsvp_type")
	private String rsvpType;
	
	@Column(name = "additional_count")
	private Integer additionalCount;
	
	@Column(name = "firstname")
	private String firstName;
	
	@Column(name = "lastname")
	private String lastName;
	
	@Column(name = "user_company_name")
	private String companyName;
	
	@Column(name = "price_type")
	private String priceType;
}
