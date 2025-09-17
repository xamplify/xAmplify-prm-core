package com.xtremand.domain.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.hibernate.HibernateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.xtremand.campaign.exception.XamplifyDataAccessException;
import com.xtremand.common.bom.CompanyProfile;
import com.xtremand.common.bom.Pagination;
import com.xtremand.dao.util.GenericDAO;
import com.xtremand.domain.bom.DomainMetadata;
import com.xtremand.domain.bom.DomainModuleNameType;
import com.xtremand.domain.dao.DomainDao;
import com.xtremand.domain.dto.DomainMetadataDTO;
import com.xtremand.domain.dto.DomainRequestDTO;
import com.xtremand.domain.dto.DomainResponseDTO;
import com.xtremand.domain.service.DomainService;
import com.xtremand.exception.DuplicateEntryException;
import com.xtremand.formbeans.XtremandResponse;
import com.xtremand.team.member.group.dao.TeamMemberGroupDao;
import com.xtremand.user.dao.UserDAO;
import com.xtremand.util.BadRequestException;
import com.xtremand.util.DateUtils;
import com.xtremand.util.XamplifyUtil;
import com.xtremand.util.XamplifyUtils;
import com.xtremand.util.dto.Pageable;
import com.xtremand.util.dto.XamplifyConstants;
import com.xtremand.util.service.UtilService;
import com.xtremand.vanity.url.dto.VanityUrlDetailsDTO;

@Service
@Transactional
public class DomainServiceImpl implements DomainService {

	@Autowired
	private DomainDao domainDao;

	@Autowired
	private UserDAO userDao;

	@Autowired
	private UtilService utilService;

	@Value("${web_url}")
	private String webUrl;

	@Autowired
	private XamplifyUtil xamplifyUtil;

	@Autowired
	private GenericDAO genericDao;

	@Autowired
	private TeamMemberGroupDao teamMemberGroupDao;

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public XtremandResponse save(DomainRequestDTO domainRequestDTO, DomainModuleNameType domainModuleNameType) {
		try {
			XtremandResponse response = new XtremandResponse();
			Integer createdUserId = domainRequestDTO.getCreatedUserId();
			Integer companyId = userDao.getCompanyIdByUserId(createdUserId);
			CompanyProfile company = new CompanyProfile();
			company.setId(companyId);
			List<String> domainNames = domainRequestDTO.getDomainNames();
			List<String> availableDomains = domainDao.findAllDomainNames(companyId, domainModuleNameType);
			if (XamplifyUtils.isNotEmptyList(availableDomains)) {
				String domainName = domainNames.get(0).trim().toLowerCase();
				boolean isDuplicateDomainName = availableDomains.indexOf(domainName) > -1;
				if (isDuplicateDomainName) {
					throw new DuplicateEntryException("Already Exists");
				}
			}

			List<String> updatedDomainNames = new ArrayList<>();
			for (String domainName : domainNames) {
				String updatedDomainName = domainName.trim().toLowerCase();
				updatedDomainNames.add(updatedDomainName);
			}
			Set<String> duplicateDomainNames = XamplifyUtils.findDuplicateBySetAdd(updatedDomainNames);
			if (duplicateDomainNames.isEmpty()) {
				iterateAndSetDomainProperties(domainRequestDTO, response, createdUserId, company, updatedDomainNames,
						domainModuleNameType);
			} else {
				String message = "Please remove duplicate domain names : " + duplicateDomainNames;
				throw new DuplicateEntryException(message);
			}

			return response;
		} catch (DuplicateEntryException e) {
			throw new DuplicateEntryException(e.getMessage());
		} catch (BadRequestException e) {
			throw new BadRequestException(e.getMessage());
		} catch (HibernateException | XamplifyDataAccessException e) {
			throw new XamplifyDataAccessException(e.getMessage());
		} catch (Exception ex) {
			throw new XamplifyDataAccessException(ex.getMessage());
		}

	}

	private void iterateAndSetDomainProperties(DomainRequestDTO domainRequestDTO, XtremandResponse response,
			Integer createdUserId, CompanyProfile company, List<String> updatedDomainNames,
			DomainModuleNameType domainModuleNameType) {
		boolean isDomainAllowed = domainRequestDTO.getIsDomainAllowedToAddToSamePartnerAccount() != null
				&& domainRequestDTO.getIsDomainAllowedToAddToSamePartnerAccount();
		utilService.saveDomain(createdUserId, company, updatedDomainNames, domainModuleNameType, isDomainAllowed);
		response.setStatusCode(200);
		String message = domainRequestDTO.getDomainNames().size() > 1 ? "Domain name(s) has been added successfully"
				: "Domain name has been added successfully";
		response.setMessage(message);
	}

	@Override
	public XtremandResponse findAll(Pageable pageable, Integer loggedInUserId,
			DomainModuleNameType domainModuleNameType) {
		XtremandResponse response = new XtremandResponse();
		Pagination pagination = utilService.setPageableParameters(pageable, loggedInUserId);
		Integer companyId = userDao.getCompanyIdByUserId(loggedInUserId);
		pagination.setCompanyId(companyId);
		String toDateFilterString = pageable.getToDateFilterString();
		String fromDateFilterString = pageable.getFromDateFilterString();
		String timeZone = pageable.getTimeZone();
		if (XamplifyUtils.isValidString(toDateFilterString) && XamplifyUtils.isValidString(fromDateFilterString)
				&& XamplifyUtils.isValidString(timeZone)) {
			pagination.setFromDateFilterString(fromDateFilterString);
			pagination.setToDateFilterString(toDateFilterString);
			pagination.setTimeZone(timeZone);
			utilService.setDateFilters(pagination);
		}
		response.setData(domainDao.findAll(pagination, pageable.getSearch(), domainModuleNameType.name()));
		XamplifyUtils.addSuccessStatus(response);
		return response;
	}

	@Override
	public XtremandResponse findAllDomainNames(Integer loggedInUserId, DomainModuleNameType domainModuleNameType) {
		XtremandResponse response = new XtremandResponse();
		Integer companyId = userDao.getCompanyIdByUserId(loggedInUserId);
		List<DomainResponseDTO> domainDtos = domainDao.findAllDomainNames(companyId, domainModuleNameType.name());
		response.setData(domainDtos);
		XamplifyUtils.addSuccessStatus(response);
		return response;
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public XtremandResponse deleteDomain(Integer id, Integer loggedInUserId) {
		XtremandResponse response = new XtremandResponse();
		domainDao.delete(id);
		XamplifyUtils.addSuccessStatusWithMessage(response, "Domain has been deleted successfully");
		return response;
	}

	@Override
	public XtremandResponse getTeamMemberSignUpUrl(Integer loggedInUserId, boolean isVanityLogin, String domainName) {
		return getSignUpUrlByModuleName(loggedInUserId, isVanityLogin, domainName,
				XamplifyConstants.TEAM_MEMBER_SIGN_UP_URL_PREFIX);
	}

	private XtremandResponse getSignUpUrlByModuleName(Integer loggedInUserId, boolean isVanityLogin, String domainName,
			String urlPrefix) {
		XtremandResponse response = new XtremandResponse();
		VanityUrlDetailsDTO postDto = new VanityUrlDetailsDTO();
		postDto.setUserId(loggedInUserId);
		postDto.setVendorCompanyProfileName(domainName);
		postDto.setVanityUrlFilter(isVanityLogin);
		utilService.isVanityUrlFilterApplicable(postDto);
		String companySignUpUrl = "";
		String domainUrl = webUrl;
		if (postDto.isVendorLoggedInThroughOwnVanityUrl()) {
			domainUrl = xamplifyUtil.frameVanityURL(webUrl, domainName);
		} else {
			domainName = userDao.getCompanyProfileNameByUserId(loggedInUserId);
		}
		companySignUpUrl = domainUrl + urlPrefix + "/" + domainName;
		response.setStatusCode(200);
		response.setData(companySignUpUrl);
		return response;
	}

	@Override
	public XtremandResponse getPartnerSignUpUrl(Integer loggedInUserId, boolean isVanityLogin, String domainName) {
		return getSignUpUrlByModuleName(loggedInUserId, isVanityLogin, domainName,
				XamplifyConstants.PARTNER_SIGNUP_URL_PREFIX);
	}

	@Override
	public XtremandResponse savePartnerDomain(DomainRequestDTO domainRequestDTO) {
		return null;
	}

	@Override
	public HttpServletResponse downloadDomainCsv(Pagination pagination, HttpServletResponse response,
			DomainModuleNameType type) {
		try {
			Integer createdUserId = pagination.getUserId();
			Integer companyId = userDao.getCompanyIdByUserId(createdUserId);
			if (XamplifyUtils.isValidInteger(companyId)) {
				List<String[]> data = new ArrayList<>();
				String fileName = type.name() + "_Domain_List";
				List<DomainResponseDTO> domains = new ArrayList<>();
				data.add(new String[] { "DOMAIN NAME", "CREATED ON(PST)" });
				utilService.setDateFilters(pagination);
				domains = domainDao.getDomaisDataForCSV(companyId, pagination, type);
				for (DomainResponseDTO domain : domains) {
					data.add(new String[] { domain.getDomainName(),
							DateUtils.convertDateToString(domain.getCreatedTime()) });
				}
				return XamplifyUtils.generateCSV(fileName, response, data);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return response;
	}

	/** XNFR-780 **/
	@Override
	public Integer saveCompanyDomainMetadata(DomainMetadataDTO domainMetadataDTO) {
		Integer domainMetadaId = null;
		if (XamplifyUtils.isValidString(domainMetadataDTO.getCompanyInfoMetadata())
				&& XamplifyUtils.isValidString(domainMetadataDTO.getDomainName())) {
			DomainMetadata domainMetada = new DomainMetadata();
			domainMetada.setDomainName(domainMetadataDTO.getDomainName());
			domainMetada.setCompanyInfoMetadata(domainMetadataDTO.getCompanyInfoMetadata());
			domainMetada.setCreatedTime(new Date());
			domainMetadaId = genericDao.save(domainMetada);
		}
		return domainMetadaId;
	}

	@Override
	public XtremandResponse updatePartnerDomain(DomainRequestDTO domainRequestDto, DomainModuleNameType type) {
		XtremandResponse response = new XtremandResponse();
		if (XamplifyUtils.isValidInteger(domainRequestDto.getId())) {
			domainDao.updateDomain(domainRequestDto, type);
			XamplifyUtils.addSuccessStatus(response);
		}
		return response;
	}

}
