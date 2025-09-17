package com.xtremand.user.bom;

import java.io.Serializable;

import javax.persistence.Embeddable;
import javax.persistence.ManyToOne;

import com.xtremand.common.bom.CompanyProfile;

@Embeddable
public class PartnerCompanyId implements Serializable {
	@ManyToOne
	private User partner;
	
	@ManyToOne
	private CompanyProfile company;

	public PartnerCompanyId() {
		// TODO Auto-generated constructor stub
	}

	public PartnerCompanyId(User partner, CompanyProfile company) {
		this.partner = partner;
		this.company = company;
	}

	public User getPartner() {
		return partner;
	}

	public void setPartner(User partner) {
		this.partner = partner;
	}

	public CompanyProfile getCompany() {
		return company;
	}

	public void setCompany(CompanyProfile company) {
		this.company = company;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((company == null) ? 0 : company.hashCode());
		result = prime * result + ((partner == null) ? 0 : partner.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof PartnerCompanyId))
			return false;
		PartnerCompanyId other = (PartnerCompanyId) obj;
		if (company == null) {
			if (other.company != null)
				return false;
		} else if (!company.equals(other.company))
			return false;
		if (partner == null) {
			if (other.partner != null)
				return false;
		} else if (!partner.equals(other.partner))
			return false;
		return true;
	}
}
