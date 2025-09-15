package com.xtremand.user.bom;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.xtremand.common.bom.CompanyProfile;
import com.xtremand.common.bom.XamplifyTimeStamp;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "xt_legal_basis")
public class LegalBasis extends XamplifyTimeStamp{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7913050169938998219L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "xt_legal_basis_sequence")
	@SequenceGenerator(name = "xt_legal_basis_sequence", sequenceName = "xt_legal_basis_sequence", allocationSize = 1)
	private Integer id;	
	
	private String name;
	
	private String description;
	
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
	private CompanyProfile company;
	
	@Column(name="created_by")
	private Integer createdBy;
	
	@Column(name="is_default")
	private boolean isDefault;
	
	@Column(name="is_select_by_default")
	private boolean isSelectByDefault;
	
	@ManyToMany(mappedBy = "legalBasis", fetch=FetchType.LAZY)	
	private List<UserUserList> userUserLists;
	
}
