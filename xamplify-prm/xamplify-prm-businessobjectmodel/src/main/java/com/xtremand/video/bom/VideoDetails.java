package com.xtremand.video.bom;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Immutable;

import lombok.Data;

@Entity
@Table(name = "v_manage_videos")
@Immutable
@Data
public class VideoDetails {

	@Transient
	private VideoCategory category;

	@Id
	@Column(name = "video_id")
	private Integer id;

	@Column(name = "company_id")
	private Integer companyId;

	@Column(name = "video_alias")
	private String alias;

	private String title;

	@Column(name = "customer_id")
	private Integer uploadedUserId;

	@Column(name = "categories_id")
	private Integer categoryId;

	@Column(name = "category_name")
	private String categoryName;

	@Column(name = "uploaded_time")
	private Date createdTime;

	@Column(name = "video_length")
	private String videoLength;

	@Column(name = "view_by")
	private String viewBy;

	private Integer views;

	@Column(name = "image_uri")
	private String imagePath;

	@Column(name = "videouri")
	private String videoPath;

	@Column(name = "is_processed")
	private boolean isProcessed;

	@Column(name = "full_name")
	private String uploadedUserName;

	@Column(name = "company_profile_name")
	private String companyName;

	@Column(name = "gifimagepath")
	private String gifImagePath;

	@Column(name = "created_for_company")
	private Integer createdForCompany;

	private String description;

	@Transient
	private String uploadedDate;

	@Transient
	private boolean whiteLabeledAssetReceivedFromVendor;

	@Transient
	private String whiteLabeledAssetSharedByVendorCompanyName;

}
