package com.xtremand.gdpr.setting.dto;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

@Data
public class LegalBasisSaveRequest implements Serializable{
	private static final long serialVersionUID = 1955272911302242935L;
	
	private Integer userId;
	
	private Integer companyId;
	
	private List<LegalBasisDTO> legalBasis;
}
