package com.xtremand.dam.bom;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Type;
import org.springframework.util.StringUtils;

import com.xtremand.common.bom.CompanyProfile;
import com.xtremand.video.bom.VideoFile;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Setter;

@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "xt_dam")
public class Dam extends DamMappedSuperClass implements Serializable {

	private static final long serialVersionUID = -1317030915287408475L;

	private static final String DAM_SEQUENCE = "xt_dam_sequence";

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = DAM_SEQUENCE)
	@SequenceGenerator(name = DAM_SEQUENCE, sequenceName = DAM_SEQUENCE, allocationSize = 1)
	@Column(name = "id")
	private Integer id;

	@Column(name = "asset_name")
	@Setter(value = AccessLevel.NONE)
	private String assetName;

	@Column(name = "description")
	@Setter(value = AccessLevel.NONE)
	private String description;

	@Column(name = "asset_status")
	@Type(type = "com.xtremand.dam.bom.DamStatusEnumerator")
	@Enumerated(EnumType.STRING)
	private DamStatusEnum damStatusEnum;

	@ManyToOne
	@JoinColumn(name = "company_id")
	private CompanyProfile companyProfile;

	@Column(name = "asset_path")
	private String assetPath;

	@Column(name = "thumbnail_path")
	private String thumbnailPath;

	@Column(name = "asset_type")
	private String assetType;

	@Column(name = "is_bee_template")
	private boolean beeTemplate;

	@Column(name = "parent_id")
	private Integer parentId;

	@Column(name = "child_parent_id")
	private Integer childParentId;

	@Column(name = "published_time", columnDefinition = "DATETIME")
	@Temporal(TemporalType.TIMESTAMP)
	private Date publishedTime;

	@Column(name = "created_time", columnDefinition = "DATETIME")
	@Temporal(TemporalType.TIMESTAMP)
	private Date createdTime;

	@Column(name = "created_by")
	private Integer createdBy;

	@Column(name = "updated_time", columnDefinition = "DATETIME")
	@Temporal(TemporalType.TIMESTAMP)
	private Date updatedTime;

	@Column(name = "updated_by")
	private Integer updatedBy;

	@Column(name = "page_size")
	private String pageSize;

	@Column(name = "page_orientation")
	private String pageOrientation;

	@Column(name = "version")
	private Integer version;

	@Column(name = "image_generated_on", columnDefinition = "DATETIME")
	@Temporal(TemporalType.TIMESTAMP)
	private Date imageGeneratedOn;

	@Column(name = "is_image_generated_successfully")
	private boolean imageGeneratedSuccessfully;

	@OneToMany(mappedBy = "dam", orphanRemoval = true, fetch = FetchType.LAZY)
	private Set<DamTag> tags = new HashSet<>();

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "video_id", referencedColumnName = "id")
	private VideoFile videoFile;

	/********* XNFR-255 ***********/
	@Column(name = "is_white_labeled_asset_shared_with_partners")
	private boolean whiteLabeledAssetSharedWithPartners;

	@Column(name = "is_publishing_or_white_labeling_in_progress")
	private boolean publishingOrWhiteLabelingInProgress;

	/********* XNFR-255 ***********/

	/********* XNFR-342 ***********/
	@Column(name = "is_publishing_to_partner_list")
	private boolean publishingToPartnerList;

	@Column(name = "is_added_to_quick_links")
	private boolean addedToQuickLinks;
	
	/** XNFR-781 **/
	@Column(name = "approval_status")
	@Type(type = "com.xtremand.dam.bom.ApprovalStatusTypeType")
	@Enumerated(EnumType.STRING)
	private ApprovalStatusType approvalStatus;
	
	@Column(name = "approval_status_updated_by")
	private Integer approvalStatusUpdatedBy;
	
	@Column(name = "approval_status_updated_time", columnDefinition = "DATETIME")
	@Temporal(TemporalType.TIMESTAMP)
	private Date approvalStatusUpdatedTime;
	
	/** XNFR-833 **/
	@Column(name = "is_partner_signature_required")
	private boolean partnerSignatureRequired;
	
	@Column(name = "is_vendor_signature_required")
	private boolean vendorSignatureRequired;
	
	@Column(name = "is_vendor_signature_completed")
	private boolean vendorSignatureCompleted;
	
	/** XNFR-885 **/
	@Column(name = "approval_reference_id")
	private Integer approvalReferenceId;
	
	@Column(name = "is_vendor_signature_required_after_partner_signature")
	private boolean vendorSignatureRequiredAfterPartnerSignature;
	

	@Column(name = "open_ai_file_id")
	private String openAIFileId;

	/** XNFR-955 **/
	@Column(name = "slug")
	private String slug;
	
	/** XNFR-981 **/
	@Column(name = "ragit_file_id")
	private String ragitFileId;

	@ManyToOne
	@JoinColumn(name = "created_for_company")
	private CompanyProfile createdForCompany;

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public void setAssetName(String assetName) {
		if (StringUtils.hasText(assetName)) {
			this.assetName = assetName;
		} else {
			this.assetName = "Asset_" + System.currentTimeMillis();
		}

	}

	public void setDescription(String description) {
		if (StringUtils.hasText(description)) {
			this.description = description.trim();
		} else {
			this.description = "Deafult-Description";
		}

	}

}
