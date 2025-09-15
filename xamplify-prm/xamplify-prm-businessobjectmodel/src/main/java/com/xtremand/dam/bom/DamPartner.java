package com.xtremand.dam.bom;

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
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import com.xtremand.partnership.bom.Partnership;

import lombok.Data;
import lombok.EqualsAndHashCode;


@Entity
@Table(name = "xt_dam_partner")
@Data
@EqualsAndHashCode(callSuper = false)
public class DamPartner extends DamMappedSuperClass implements Serializable {/**
	 * 
	 */
	private static final long serialVersionUID = 8325419022746721606L;
	
	private static final String DAM_PARTNER_SEQUENCE = "xt_dam_partner_sequence";

	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = DAM_PARTNER_SEQUENCE)
	@SequenceGenerator(name = DAM_PARTNER_SEQUENCE, sequenceName = DAM_PARTNER_SEQUENCE, allocationSize = 1)
	@Column(name = "id")
	private Integer id;
	
	@ManyToOne
	@JoinColumn(name = "dam_id")
	private Dam dam;
	
	@ManyToOne
	@JoinColumn(name = "partnership_id")
	private Partnership partnership;
	
	@Column(name="is_partner_group_selected")
	private boolean partnerGroupSelected;
	
	@Column(name = "published_time", columnDefinition = "DATETIME")
	@Temporal(TemporalType.TIMESTAMP)
	private Date publishedTime;
	
	@Column(name = "published_by")
	private Integer publishedBy;
	
	@Column(name="updated_by")
	private Integer updatedBy;
	
	@Column(name = "updated_time", columnDefinition = "DATETIME")
	@Temporal(TemporalType.TIMESTAMP)
	private Date updatedTime;
	
	@Transient
	private String alias;
	
	@Column(name="shared_asset_path")
	private String sharedAssetPath;
	
	@Column(name="is_partner_signature_completed")
	private boolean partnerSignatureCompleted;
	
	@Column(name="is_vendor_signature_completed")
	private boolean vendorSignatureCompleted;
	

}
