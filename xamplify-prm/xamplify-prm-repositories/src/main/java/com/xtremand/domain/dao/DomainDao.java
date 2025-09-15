package com.xtremand.domain.dao;

import java.util.List;
import java.util.Map;

import com.xtremand.common.bom.Pagination;
import com.xtremand.domain.bom.Domain;
import com.xtremand.domain.bom.DomainModuleNameType;
import com.xtremand.domain.dto.DomainMetadataDTO;
import com.xtremand.domain.dto.DomainRequestDTO;
import com.xtremand.domain.dto.DomainResponseDTO;

public interface DomainDao {

	void saveAll(List<Domain> domains);

	Map<String, Object> findAll(Pagination pagination, String searchKey, String moduleName);

	List<String> findAllDomainNames(Integer companyId, DomainModuleNameType domainModuleNameType);

	void delete(Integer id);

	List<DomainResponseDTO> getDomaisDataForCSV(Integer companyId, Pagination pagination, DomainModuleNameType type);

	public DomainMetadataDTO getCompanyProfileMetadataByDomainName(String domainName);

	public void updateDomainMediaResourceFilePath(Integer id, String awsFilePath);

	String fetchCompanyProfileLogo(Integer id);

	public boolean checkIfDomainIsAllowedToAddToSamePartnerAccount(String domainName, Integer companyId);

	void updateDomain(DomainRequestDTO domainRequestDto, DomainModuleNameType type);

	List<DomainResponseDTO> findAllDomainNames(Integer companyId, String moduleName);
	
	public Integer getDomainIdByDomainNameAndVendorCompanyId(Integer vendorCompanyId, String domainName);

}
