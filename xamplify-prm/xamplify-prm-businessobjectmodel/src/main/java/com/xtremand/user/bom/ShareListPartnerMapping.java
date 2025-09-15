package com.xtremand.user.bom;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import lombok.Data;

@Entity
@Table(name = "xt_sharelist_partner_mapping")
@Data
public class ShareListPartnerMapping {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "xt_sharelist_partner_mapping_sequence")
	@SequenceGenerator(name = "xt_sharelist_partner_mapping_sequence", sequenceName = "xt_sharelist_partner_mapping_sequence", allocationSize = 1)
	@Column(name = "id")
	private Integer id;
	
	@ManyToOne
	@JoinColumn(name = "sharelist_partner_id")
	private ShareListPartner shareListPartner;
	
	
	@Column(name = "partner_id")
	private Integer partnerId;
}
