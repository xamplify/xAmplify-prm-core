package com.xtremand.domain.dao.hibernate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.xtremand.campaign.exception.XamplifyDataAccessException;
import com.xtremand.common.bom.Pagination;
import com.xtremand.domain.bom.Domain;
import com.xtremand.domain.bom.DomainModuleNameType;
import com.xtremand.domain.dao.DomainDao;
import com.xtremand.domain.dto.DomainMetadataDTO;
import com.xtremand.domain.dto.DomainRequestDTO;
import com.xtremand.domain.dto.DomainResponseDTO;
import com.xtremand.exception.DuplicateEntryException;
import com.xtremand.util.PaginationUtil;
import com.xtremand.util.XamplifyUtils;
import com.xtremand.util.dao.HibernateSQLQueryResultUtilDao;
import com.xtremand.util.dto.HibernateSQLQueryResultRequestDTO;
import com.xtremand.util.dto.QueryParameterDTO;
import com.xtremand.util.dto.SortColumnDTO;

@Repository
@Transactional
public class HibernateDomainDao implements DomainDao {

	private static final String MODULE_NAME = "moduleName";

	private static final String COMPANY_ID = "companyId";

	@Value("${createdTime.property.name}")
	private String createdTimePropertyName;

	@Autowired
	private HibernateSQLQueryResultUtilDao hibernateSQLQueryResultUtilDao;

	@Autowired
	private SessionFactory sessionFactory;

	@Autowired
	private PaginationUtil paginationUtil;

	@Override
	public void saveAll(List<Domain> domains) {
		try {
			Session session = sessionFactory.getCurrentSession();
			for (int i = 0; i < domains.size(); i++) {
				session.save(domains.get(i));
				if (i % 30 == 0) {
					session.flush();
					session.clear();
				}
			}
		} catch (ConstraintViolationException con) {
			throw new DuplicateEntryException("Already Exists");
		} catch (HibernateException | XamplifyDataAccessException e) {
			throw new XamplifyDataAccessException(e.getMessage());
		} catch (Exception ex) {
			throw new XamplifyDataAccessException(ex.getMessage());
		}

	}

	@Override
	public Map<String, Object> findAll(Pagination pagination, String searchKey, String moduleName) {
		Map<String, Object> map = new HashMap<>();
		boolean hasSearchKey = searchKey != null && StringUtils.hasText(searchKey);
		StringBuilder findAllDomainNamesQueryString = new StringBuilder();
		String sortQueryString = addSortColumns(pagination);
		String queryString = "select d.id as \"id\", d.domain_name as \"domainName\",d.created_time as \"createdTime\","
				+ "d.is_domain_allowed_to_add_to_same_partner_account as \"isDomainAllowedToAddToSamePartnerAccount\", "
				+ "d.is_domain_deactivated as \"domainDeactivated\", "
				+ "CAST(CASE WHEN d.deactivated_on IS NOT NULL THEN d.deactivated_on END AS TEXT) as \"deactivatedOn\" "
				+ "from xt_allowed_domain d \n"
				+ "where d.company_id = :companyId and cast(d.module_name as text) = :moduleName \t";
		String searchWorkflowsQueryString = " and (LOWER(d.domain_name) like LOWER('%searchKey%'))";
		findAllDomainNamesQueryString.append(queryString);
		if (pagination.getFromDateFilter() != null && pagination.getToDateFilter() != null) {
			String filterQueryString = " and  d.created_time  between  TO_TIMESTAMP('" + pagination.getFromDateFilter()
					+ "', 'Dy Mon DD HH24:MI:SS ZZZ YYYY') and TO_TIMESTAMP('" + pagination.getToDateFilter()
					+ "', 'Dy Mon DD HH24:MI:SS ZZZ YYYY') ";
			findAllDomainNamesQueryString.append(filterQueryString);
		}
		if (hasSearchKey) {
			searchKey = XamplifyUtils.escapeSingleQuotesForSearchQuery(searchKey);
			searchKey = XamplifyUtils.addBackSlashToSpecialCharacters(searchKey);
			findAllDomainNamesQueryString.append(searchWorkflowsQueryString.replace("searchKey", searchKey))
					.append(sortQueryString);
		} else {
			findAllDomainNamesQueryString.append(sortQueryString);
		}
		Session session = sessionFactory.getCurrentSession();
		SQLQuery query = session.createSQLQuery(String.valueOf(findAllDomainNamesQueryString));
		query.setParameter(COMPANY_ID, pagination.getCompanyId());
		query.setParameter(MODULE_NAME, moduleName);
		return paginationUtil.setScrollableAndGetList(pagination, map, query, DomainResponseDTO.class);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<DomainResponseDTO> findAllDomainNames(Integer companyId, String moduleName) {
		String queryString = "SELECT d.domain_name as \"domainName\", d.is_domain_deactivated as \"domainDeactivated\" \n"
				+ "FROM xt_allowed_domain d WHERE d.company_id = :companyId AND cast(d.module_name as text) = :moduleName";
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO(COMPANY_ID, companyId));
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO(MODULE_NAME, moduleName));
		return (List<DomainResponseDTO>) hibernateSQLQueryResultUtilDao.getListDto(hibernateSQLQueryResultRequestDTO,
				DomainResponseDTO.class);
	}

	private String addSortColumns(Pagination pagination) {
		List<SortColumnDTO> sortColumnDTOs = new ArrayList<>();
		SortColumnDTO createdTimeSortOption = new SortColumnDTO(createdTimePropertyName, "d.created_time", true, true,
				false);
		sortColumnDTOs.add(createdTimeSortOption);
		return paginationUtil.generateSortQuery(pagination, sortColumnDTOs, "desc");
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<String> findAllDomainNames(Integer companyId, DomainModuleNameType domainModuleNameType) {
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(
				"select lower(trim(domain_name)) from xt_allowed_domain where company_id = :companyId and cast(module_name as text) = :moduleName");
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO(COMPANY_ID, companyId));
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO(MODULE_NAME, domainModuleNameType.name()));
		return (List<String>) hibernateSQLQueryResultUtilDao.returnList(hibernateSQLQueryResultRequestDTO);
	}

	@Override
	public void delete(Integer id) {
		Session session = sessionFactory.getCurrentSession();
		String hqlString = "delete from Domain where id=:id";
		Query query = session.createQuery(hqlString);
		query.setParameter("id", id);
		query.executeUpdate();

	}

	@SuppressWarnings("unchecked")
	@Override
	public List<DomainResponseDTO> getDomaisDataForCSV(Integer companyId, Pagination pagination,
			DomainModuleNameType domainModuleNameType) {
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		String searchKey = pagination.getSearchKey();
		String finalQueryString = "select d.id as \"id\", d.domain_name as \"domainName\",d.created_time as \"createdTime\" from xt_allowed_domain d"
				+ " where d.company_id = :companyId and cast(d.module_name as text) = :moduleName \t";
		if (pagination.getFromDateFilter() != null && pagination.getToDateFilter() != null) {
			String filterQueryString = " and  d.created_time  between  TO_TIMESTAMP('" + pagination.getFromDateFilter()
					+ "', 'Dy Mon DD HH24:MI:SS ZZZ YYYY') and TO_TIMESTAMP('" + pagination.getToDateFilter()
					+ "', 'Dy Mon DD HH24:MI:SS ZZZ YYYY') ";
			finalQueryString += filterQueryString;
		}

		if (XamplifyUtils.isValidString(searchKey)) {
			String searchQueryString = " and (LOWER(d.domain_name) like LOWER('%searchKey%'))";
			finalQueryString += searchQueryString.replace("searchKey", searchKey);
		}

		String sortQuery = " order by d.created_time desc ";
		finalQueryString += sortQuery;
		hibernateSQLQueryResultRequestDTO.setQueryString(finalQueryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO(COMPANY_ID, companyId));
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO(MODULE_NAME, domainModuleNameType.name()));
		return (List<DomainResponseDTO>) hibernateSQLQueryResultUtilDao.getListDto(hibernateSQLQueryResultRequestDTO,
				DomainResponseDTO.class);
	}

	/** XNFR-780 start **/
	@Override
	public DomainMetadataDTO getCompanyProfileMetadataByDomainName(String domainName) {
		if (XamplifyUtils.isValidString(domainName)) {
			String queryString = "select id as \"id\", company_info_metadata as \"companyInfoMetadata\","
					+ "created_time as \"createdTime\" from xt_domain_metadata where domain_name = :domainName";
			HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
			hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
					.add(new QueryParameterDTO("domainName", domainName));
			return (DomainMetadataDTO) hibernateSQLQueryResultUtilDao.getDto(hibernateSQLQueryResultRequestDTO,
					DomainMetadataDTO.class);
		} else {
			return new DomainMetadataDTO();
		}
	}

	@Override
	public void updateDomainMediaResourceFilePath(Integer id, String awsFilePath) {
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		String queryString = "update xt_domain_media_resource set file_path = :awsFilePath, temporary_file_path = null where id = :id";
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("id", id));
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO("awsFilePath", awsFilePath));
		hibernateSQLQueryResultUtilDao.update(hibernateSQLQueryResultRequestDTO);
	}

	/** XNFR-780 end **/

	@Override
	@SuppressWarnings("unchecked")
	public String fetchCompanyProfileLogo(Integer id) {
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		String queryString = "select file_path as \"path\" from xt_domain_media_resource where domain_metadata_id = :id";
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("id", id));
		List<String> logos = (List<String>) hibernateSQLQueryResultUtilDao
				.returnList(hibernateSQLQueryResultRequestDTO);
		if (XamplifyUtils.isNotEmptyList(logos)) {
			return logos.get(0);
		} else {
			return null;
		}
	}

	@Override
	public boolean checkIfDomainIsAllowedToAddToSamePartnerAccount(String domainName, Integer companyId) {
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		String sqlQuery = "select is_domain_allowed_to_add_to_same_partner_account from xt_allowed_domain where company_id = :companyId and "
				+ "domain_name = :domainName and cast(module_name as text) = 'PARTNER' ";
		hibernateSQLQueryResultRequestDTO.setQueryString(sqlQuery);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO(COMPANY_ID, companyId));
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("domainName", domainName));
		return (boolean) hibernateSQLQueryResultUtilDao.getUniqueResult(hibernateSQLQueryResultRequestDTO);
	}

	@Override
	public void updateDomain(DomainRequestDTO domainRequestDto, DomainModuleNameType domainModuleNameType) {
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		String queryString = "update xt_allowed_domain set is_domain_allowed_to_add_to_same_partner_account = :isDomainAllowedToAdd "
				+ " where id = :id and cast(module_name as text) = :moduleName ";
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO("id", domainRequestDto.getId()));
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("isDomainAllowedToAdd",
				domainRequestDto.getIsDomainAllowedToAddToSamePartnerAccount()));
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO(MODULE_NAME, domainModuleNameType.name()));
		hibernateSQLQueryResultUtilDao.update(hibernateSQLQueryResultRequestDTO);
	}

	@Override
	public Integer getDomainIdByDomainNameAndVendorCompanyId(Integer vendorCompanyId, String domainName) {
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		String sqlString = "select id from xt_allowed_domain xad where company_id = :vendorCompanyId and domain_name =:domainName and \"module_name\" ='PARTNER' ";
		hibernateSQLQueryResultRequestDTO.setQueryString(sqlString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO("vendorCompanyId", vendorCompanyId));
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("domainName", domainName));

		return (Integer) hibernateSQLQueryResultUtilDao.getUniqueResult(hibernateSQLQueryResultRequestDTO);
	}

}
