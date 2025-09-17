package com.xtremand.contact.status.bom;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.xtremand.common.bom.CompanyProfile;
import com.xtremand.util.bom.CreatedAndUpdatedColumns;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "xt_contact_status")
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
public class ContactStatus extends CreatedAndUpdatedColumns implements Serializable {

	private static final long serialVersionUID = 5518405906804305875L;

	private static final String SEQUENCE = "xt_contact_status_sequence";

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SEQUENCE)
	@SequenceGenerator(name = SEQUENCE, sequenceName = SEQUENCE, allocationSize = 1)
	private Integer id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "company_id", nullable = false)
	private CompanyProfile company;

	@Column(name = "stage_name", nullable = false)
	private String stageName;

	@Column(name = "is_default")
	private boolean defaultStage;

}
