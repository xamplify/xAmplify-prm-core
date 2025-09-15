package com.xtremand.user.bom;

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

import lombok.Data;

@Entity
@Table(name = "xt_sharelist_partner")
@Data
public class ShareListPartner {
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "xt_sharelist_partner_sequence")
	@SequenceGenerator(name = "xt_sharelist_partner_sequence", sequenceName = "xt_sharelist_partner_sequence", allocationSize = 1)
	@Column(name = "id")
	private Integer id;
	
	@ManyToOne
	@JoinColumn(name = "user_list_id" )
	private UserList userList;
	
	@ManyToOne
	@JoinColumn(name = "partnership_id")
	private Partnership partnership;
	
	@Column(name = "created_time", columnDefinition = "DATETIME")
	@Temporal(TemporalType.TIMESTAMP)
	private Date createdTime;
	
	@Column(name = "is_shared_to_company")
	private boolean sharedToCompany;

}
