package com.xtremand.flexi.fields.bom;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

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
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.xtremand.common.bom.CompanyProfile;
import com.xtremand.util.bom.CreatedAndUpdatedColumns;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "xt_flexi_fields")
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
public class FlexiField extends CreatedAndUpdatedColumns implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5518405906804305875L;

	private static final String SEQUENCE = "xt_flexi_fields_sequence";

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SEQUENCE)
	@SequenceGenerator(name = SEQUENCE, sequenceName = SEQUENCE, allocationSize = 1)
	private Integer id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "company_id", nullable = false)
	private CompanyProfile company;

	@Column(name = "field_name", nullable = false)
	private String fieldName;
	
	@Fetch(FetchMode.SUBSELECT)
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "flexiField", cascade = CascadeType.ALL)
	private Set<UserListFlexiField> userListFlexiFields = new HashSet<>();

}
