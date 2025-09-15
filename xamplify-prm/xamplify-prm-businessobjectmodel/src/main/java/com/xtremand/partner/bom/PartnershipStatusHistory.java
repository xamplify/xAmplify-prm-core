package com.xtremand.partner.bom;

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

import com.xtremand.partnership.bom.Partnership;
import com.xtremand.partnership.bom.Partnership.PartnershipStatus;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "xt_partnership_status_history")
public class PartnershipStatusHistory {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "partnership_status_history_id_seq")
	@SequenceGenerator(name = "partnership_status_history_id_seq", sequenceName = "partnership_status_history_id_seq", allocationSize = 1)
	@Column(name = "id")
	private Integer  id;
	
	@Column(name="status")
	@org.hibernate.annotations.Type(type = "com.xtremand.partner.bom.PartnershipStatusType")
	private PartnershipStatus status;
	
	@Column(name="created_by")
	Integer createdBy;
	
	@Column(name="created_time", columnDefinition="DATETIME")
	@Temporal(TemporalType.TIMESTAMP)
	Date createdTime;
	
	@ManyToOne
	@JoinColumn(name="partnership_id")
    private Partnership partnership;

}
