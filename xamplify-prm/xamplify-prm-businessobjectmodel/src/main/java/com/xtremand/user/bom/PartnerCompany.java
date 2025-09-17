package com.xtremand.user.bom;

import javax.persistence.AssociationOverride;
import javax.persistence.AssociationOverrides;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.Table;

import com.xtremand.common.bom.CompanyProfile;

@Entity
@Table(name = "xt_partner_company")
@AssociationOverrides({
@AssociationOverride(name = "partnerCompanyId.partner", 
			joinColumns = @JoinColumn(name = "partner_id")),
			@AssociationOverride(name = "partnerCompanyId.company", 
			joinColumns = @JoinColumn(name = "company_id")) })
public class PartnerCompany {
	
	@EmbeddedId
	private PartnerCompanyId partnerCompanyId = new PartnerCompanyId();
	
	@Column(name="branding_logo_uri")
	private String brandingLogoUri;

	public PartnerCompanyId getPartnerCompanyId() {
		return partnerCompanyId;
	}

	public void setPartnerCompanyId(PartnerCompanyId partnerCompanyId) {
		this.partnerCompanyId = partnerCompanyId;
	}

	public String getBrandingLogoUri() {
		return brandingLogoUri;
	}

	public void setBrandingLogoUri(String brandingLogoUri) {
		this.brandingLogoUri = brandingLogoUri;
	}
	
	public PartnerCompany() {
	}

	public PartnerCompany(User partner, CompanyProfile company, String brandingLogoUri) {
		this.partnerCompanyId.setPartner(partner);
		this.partnerCompanyId.setCompany(company);
		this.brandingLogoUri = brandingLogoUri;
	}
	public User getPartner() {
		return getPartnerCompanyId().getPartner();
	}

	public void setPartner(User partner) {
		getPartnerCompanyId().setPartner(partner);
	}

	public CompanyProfile getCompany() {
		return getPartnerCompanyId().getCompany();
	}

	public void setCompany(CompanyProfile company) {
		getPartnerCompanyId().setCompany(company);
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((partnerCompanyId == null) ? 0 : partnerCompanyId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof PartnerCompany))
			return false;
		PartnerCompany other = (PartnerCompany) obj;
		if (partnerCompanyId == null) {
			if (other.partnerCompanyId != null)
				return false;
		} else if (!partnerCompanyId.equals(other.partnerCompanyId))
			return false;
		return true;
	}
}
