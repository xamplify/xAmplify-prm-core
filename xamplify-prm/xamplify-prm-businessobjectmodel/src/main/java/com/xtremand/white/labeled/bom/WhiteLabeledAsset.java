package com.xtremand.white.labeled.bom;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.xtremand.dam.bom.Dam;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "xt_white_labeled_assets")
@Getter
@Setter
public class WhiteLabeledAsset extends WhiteLabeledContentMappedSuperClass implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3286918563601680046L;

	private static final String SEQUENCE = "xt_white_labeled_assets_sequence";

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SEQUENCE)
	@SequenceGenerator(name = SEQUENCE, sequenceName = SEQUENCE, allocationSize = 1)
	@Column(name = "id", nullable = false)
	private Integer id;

	@ManyToOne
	@JoinColumn(name = "vendor_company_asset_id")
	private Dam vendorCompanyAsset;

	@OneToOne
	@JoinColumn(name = "received_white_labeled_asset_id", unique = true)
	private Dam receivedAsset;

}
