package com.xtremand.domain.service;

import javax.servlet.http.HttpServletResponse;

import com.xtremand.common.bom.Pagination;
import com.xtremand.domain.bom.DomainModuleNameType;
import com.xtremand.domain.dto.DomainMetadataDTO;
import com.xtremand.domain.dto.DomainRequestDTO;
import com.xtremand.formbeans.XtremandResponse;
import com.xtremand.util.dto.Pageable;

public interface DomainService {

	XtremandResponse save(DomainRequestDTO domainRequestDTO, DomainModuleNameType domainModuleNameType);

	XtremandResponse savePartnerDomain(DomainRequestDTO domainRequestDTO);

	XtremandResponse findAll(Pageable pageable, Integer loggedInUserId, DomainModuleNameType domainModuleNameType);

	XtremandResponse deleteDomain(Integer id, Integer loggedInUserId);

	XtremandResponse getTeamMemberSignUpUrl(Integer loggedInUserId, boolean isVanityLogin, String domainName);

	XtremandResponse getPartnerSignUpUrl(Integer loggedInUserId, boolean isVanityLogin, String domainName);

	public HttpServletResponse downloadDomainCsv(Pagination pagination, HttpServletResponse response,
			DomainModuleNameType type);

	public Integer saveCompanyDomainMetadata(DomainMetadataDTO domainMetadataDTO);

	XtremandResponse updatePartnerDomain(DomainRequestDTO domainRequestDto, DomainModuleNameType partner);

	XtremandResponse findAllDomainNames(Integer loggedInUserId, DomainModuleNameType domainModuleNameType);

}
