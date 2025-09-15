package com.xtremand.util.bom;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import lombok.Data;

@Entity
@Table(name = "xt_updated_email_address_history")
@Data
public class UpdatedEmailAddressHistory implements Serializable {
	/**
	* 
	*/
	private static final long serialVersionUID = 1L;

	private static final String SEQUENCE = "xt_updated_email_address_sequence";

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SEQUENCE)
	@SequenceGenerator(name = SEQUENCE, sequenceName = SEQUENCE, allocationSize = 1)
	private Integer id;

	@Column(name = "old_email_address")
	private String oldEmailAddress;

	@Column(name = "new_email_address")
	private String newEmailAddress;

	@Column(name = "updated_on", columnDefinition = "DATETIME")
	@Temporal(TemporalType.TIMESTAMP)
	private Date updatedOn;

}
