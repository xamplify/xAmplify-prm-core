package com.xtremand.vanity.email.templates.bom;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name="xt_default_email_templates")
public class DefaultEmailTemplate implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "default_email_templates_sequence")
	@SequenceGenerator(name = "default_email_templates_sequence", sequenceName = "default_email_templates_sequence", allocationSize = 1)
	@Column(name="id")
	private Integer id;
	
	@Column(name="name")
	private String name;
	
	@Column(name="subject")
	private String subject;

	@Column(name="json_body")
	private String jsonBody;

	@Column(name="html_body")
	private String htmlBody;

	@Column(name="image_path")
	private String imagePath;

	@Column(name = "type")
	@org.hibernate.annotations.Type(type = "com.xtremand.vanity.email.templates.bom.DefaultEmailTemplateTypeEnum")
	private DefaultEmailTemplateType type;
	

}
