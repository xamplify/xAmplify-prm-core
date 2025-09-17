package com.xtremand.integration.bom;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.xtremand.company.bom.CompanySource;

import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class ExternalContactDTO implements Serializable{
	
	private Long id;
	private Integer userId;
	private String firstName;
	private String lastName;
	private String email;
	private String createdAt;
	private String updatedAt;
	private String country, city, state, postalCode, address, company, title, mobilePhone;
	private String website;
	private List<Integer> legalBasis = new ArrayList<>();
	
	//XNFR-427
	private CompanySource source;
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((email == null) ? 0 : email.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ExternalContactDTO other = (ExternalContactDTO) obj;
		if (email == null) {
			if (other.email != null)
				return false;
		} else if (!email.equals(other.email))
			return false;
		return true;
	}
	
}
