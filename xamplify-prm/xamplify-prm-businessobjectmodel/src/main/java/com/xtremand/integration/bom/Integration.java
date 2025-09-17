package com.xtremand.integration.bom;

import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.hibernate.annotations.Type;

import com.xtremand.common.bom.CompanyProfile;
import com.xtremand.common.bom.XamplifyTimeStamp;
import com.xtremand.deal.bom.DealFail;
import com.xtremand.lead.bom.LeadFail;
import com.xtremand.lead.bom.Pipeline;
import com.xtremand.lead.bom.PipelineStage;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "xt_integration")
public class Integration extends XamplifyTimeStamp {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1282557810449423644L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "integration_id_seq")
	@SequenceGenerator(name = "integration_id_seq", sequenceName = "integration_id_seq", allocationSize = 1)
	private Integer id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "company_id")
	private CompanyProfile company;

	@org.hibernate.annotations.Type(type = "com.xtremand.integration.bom.IntegrationTypeType")
	private IntegrationType type;

	@Column(name = "access_token")
	private String accessToken;

	@Column(name = "refresh_token")
	private String refreshToken;

	@Temporal(TemporalType.TIMESTAMP)
	private Date expiry;

	@Column(name = "created_by")
	private Integer createdBy;

	@Column(name = "is_sandbox")
	private boolean sandbox;

	@Column(name = "instance_url")
	private String instanceUrl;

	@Column(name = "push_leads")
	private boolean pushLeads;

	@Column(name = "external_email")
	private String externalEmail;

	@Column(name = "external_user_id")
	private String externalUserId;

	@Column(name = "external_display_name")
	private String externalDisplayName;

	@Column(name = "external_username")
	private String externalUserName;

	@Column(name = "external_organization_id")
	private String externalOrganizationId;

	@Column(name = "external_thumbnail")
	private String externalThumbnail;

	@Column(name = "web_api_instance_url")
	private String webApiInstanceUrl;

	@Column(name = "active")
	private boolean active;

	@Column(name = "client_id")
	private String clientId;

	@Column(name = "client_secret")
	private String clientSecret;

	/* XNFR-215 */
	@Column(name = "api_key")
	private String apiKey;

	@Column(name = "external_organization_name")
	private String externalOrganizationName;
	/* XNFR-215 */

	@OneToMany(mappedBy = "integration", fetch = FetchType.LAZY)
	private List<LeadFail> failedLeads;

	@OneToMany(mappedBy = "integration", fetch = FetchType.LAZY)
	private List<DealFail> failedDeals;

	/* XNFR-403 */
	@Column(name = "public_key")
	private String publicKey;

	@Column(name = "private_key")
	private String privateKey;
	/* XNFR-403 */

	/* XNFR-615 */
	@Column(name = "lead_description")
	private String leadDescription;

	@Column(name = "deal_description")
	private String dealDescription;

	@Column(name = "show_lead_pipeline")
	private boolean showLeadPipeline = true;

	@Column(name = "show_lead_pipeline_stage")
	private boolean showLeadPipelineStage = true;

	@Column(name = "show_deal_pipeline")
	private boolean showDealPipeline = true;

	@Column(name = "show_deal_pipeline_stage")
	private boolean showDealPipelineStage = true;

	@ManyToOne
	@JoinColumn(name = "lead_pipeline_id")
	private Pipeline leadPipelineId;

	@ManyToOne
	@JoinColumn(name = "lead_pipeline_stage_id")
	private PipelineStage leadPipelineStageId;

	@ManyToOne
	@JoinColumn(name = "deal_pipeline_id")
	private Pipeline dealPipelineId;

	@ManyToOne
	@JoinColumn(name = "deal_pipeline_stage_id")
	private PipelineStage dealPipelineStageId;

	@Column(name = "deal_by_partner")
	private boolean isDealByPartnerEnabled = true;

	@Column(name = "deal_by_vendor")
	private boolean isDealByVendorEnabled;

	@Column(name = "deal_by_self_lead")
	private boolean isDealBySelfLeadEnabled = true;

	@Column(name = "lead_form_column_layout")
	@Type(type = "com.xtremand.integration.bom.FormColumnLayoutTypeType")
	private FormColumnLayoutTypeEnum leadFormColumnLayout = FormColumnLayoutTypeEnum.TWO_COLUMN_LAYOUT;

	@Column(name = "deal_form_column_layout")
	@Type(type = "com.xtremand.integration.bom.FormColumnLayoutTypeType")
	private FormColumnLayoutTypeEnum dealFormColumnLayout = FormColumnLayoutTypeEnum.TWO_COLUMN_LAYOUT;

	// XNFR-681
	@Column(name = "lead_title")
	private String leadTitle = "Add a Lead";

	@Column(name = "deal_title")
	private String dealTitle = "Add a Deal";

	/*** XNFR-693 ***/
	@Column(name = "can_partner_edit_lead")
	private boolean partnerEditLead = true;

	@Column(name = "can_partner_delete_lead")
	private boolean partnerDeleteLead = true;

	/* XNFR-615 */

	@Transient
	private boolean firstIntegration;

	public enum IntegrationType {
		XAMPLIFY("xamplify");

		protected String type;

		private IntegrationType(String type) {
			this.type = type;
		}

		public String getType() {
			return type;
		}
	}

}
