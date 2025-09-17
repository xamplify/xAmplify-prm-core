package com.xtremand.campaign.bom;

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
import javax.persistence.Transient;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Entity
@Table(name = "xt_download_data_info")
public class DownloadDataInfo {
	
	public enum DownloadItem {
		CAMPAIGNS_DATA("CAMPAIGNS_DATA"), LEADS_DATA("LEADS_DATA"), DEALS_DATA("DEALS_DATA"),
		MASTER_PARTNER_LIST("MASTER_PARTNER_LIST"), PARTNER_LIST("PARTNER_LIST"), CONTACT_LIST("CONTACT_LIST"),
		SHARE_LEADS("SHARE_LEADS"), SHARED_LEADS("SHARED_LEADS");

		protected String downloadItem;

		private DownloadItem(String downloadItem) {
			this.downloadItem = downloadItem;
		}

		public String getDownloadItem() {
			return downloadItem;
		}
	}
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "xt_download_data_info_sequence")
	@SequenceGenerator(name = "xt_download_data_info_sequence", sequenceName = "xt_download_data_info_sequence", allocationSize = 1)
	@Column(name = "id")
	private Integer id;

	@Column(name = "user_id")
	private Integer userId;

	@Column(name = "is_download_in_progress")
	private boolean downloadInProgress;

	@Column(name = "clicked_time", columnDefinition = "DATETIME")
	@Temporal(TemporalType.TIMESTAMP)
	private Date clickedTime;

	@Column(name = "download_completed_time", columnDefinition = "DATETIME")
	@Temporal(TemporalType.TIMESTAMP)
	private Date downloadCompletedTime;
	
	@org.hibernate.annotations.Type(type = "com.xtremand.user.bom.DownloadItemType")
	@Column(name = "type")
	private DownloadItem type;
	
	@Column(name = "amazon_url")
	private String amazonUrl;
	
	@Transient
	@Getter
	@Setter
	private String cdnAmazonUrl;
	
}
