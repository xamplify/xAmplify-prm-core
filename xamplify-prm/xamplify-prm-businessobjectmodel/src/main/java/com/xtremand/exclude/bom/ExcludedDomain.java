package com.xtremand.exclude.bom;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import com.xtremand.user.bom.User;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "xt_excluded_domain")
public class ExcludedDomain {	
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "excluded_domain_id_seq")
	@SequenceGenerator(name = "excluded_domain_id_seq", sequenceName = "excluded_domain_id_seq", allocationSize = 1)
	@Column(name = "id")
	private Integer id;
	
	@Column(name = "company_id")
	private Integer companyId;
	
	@Column(name = "domain_name")
	private String domainName;
	
	@ManyToOne
	@JoinColumn(name="created_by", referencedColumnName="user_id")
	private User createdUser;
	
	@Column(name = "created_time", columnDefinition="DATETIME")
	@Temporal(TemporalType.TIMESTAMP)
	private Date createdTime;	
	
	@Transient
	private String utcTimeString;

}
