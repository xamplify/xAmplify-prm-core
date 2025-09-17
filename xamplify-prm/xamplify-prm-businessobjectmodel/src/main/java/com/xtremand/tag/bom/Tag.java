package com.xtremand.tag.bom;

import java.io.Serializable;
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

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.xtremand.common.bom.CompanyProfile;
import com.xtremand.user.bom.User;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name="xt_tag")
@EqualsAndHashCode(callSuper = false)
public class Tag implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -9062789549778036798L;
	
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator="xt_tag_sequence")
	@SequenceGenerator(
			name="xt_tag_sequence",
			sequenceName="xt_tag_sequence",
			allocationSize=1
			)
	@Column(name = "id")
	private Integer id;
	
	
	@Column(name="tag_name")
	private String tagName;
	
	@Column(name = "created_time", columnDefinition = "DATETIME")
	@CreationTimestamp
	private Date createdTime;
	
	@ManyToOne
	@JoinColumn(name = "created_by")
	private User createdBy;
	
	@Column(name = "updated_time", columnDefinition = "DATETIME")
	@UpdateTimestamp
	private Date updatedTime;
	
	@ManyToOne
	@JoinColumn(name = "updated_by")
	private User updatedBy;
	
	@ManyToOne
	@JoinColumn(name="company_id")
	private CompanyProfile companyProfile;
	
}

