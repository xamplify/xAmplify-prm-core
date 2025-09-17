package com.xtremand.partner.bom;

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

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "xt_updated_contacts_history")
public class UpdatedContactsHistory {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "updated_contacts_history_id_seq")
	@SequenceGenerator(name = "updated_contacts_history_id_seq", sequenceName = "updated_contacts_history_id_seq", allocationSize = 1)
	@Column(name = "id")
	private Integer  id;
	
	@Column(name="user_id")
	private Integer userId;
	
	@Column(name=" updated_by")
	private Integer updatedBy;
	
	@Column(name="updated_time", columnDefinition="DATETIME")
	@Temporal(TemporalType.TIMESTAMP)
	private Date updatedTime;
}
