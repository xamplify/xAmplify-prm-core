package com.xtremand.mdf.dao.hibernate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.HibernateException;
import org.hibernate.SQLQuery;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.hibernate.transform.Transformers;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import com.xtremand.common.bom.Pagination;
import com.xtremand.common.bom.Pagination.SORTINGORDER;
import com.xtremand.formbeans.UserDTO;
import com.xtremand.mdf.bom.MdfDetails;
import com.xtremand.mdf.bom.MdfRequest;
import com.xtremand.mdf.bom.MdfRequestHistory;
import com.xtremand.mdf.dao.MdfDao;
import com.xtremand.mdf.dto.MdfAmountTilesDTO;
import com.xtremand.mdf.dto.MdfDetailsTimeLineDTO;
import com.xtremand.mdf.dto.MdfParnterDTO;
import com.xtremand.mdf.dto.MdfRequestCommentDTO;
import com.xtremand.mdf.dto.MdfRequestDocumentDTO;
import com.xtremand.mdf.dto.MdfRequestTilesDTO;
import com.xtremand.mdf.dto.MdfRequestTimeLineDTO;
import com.xtremand.mdf.dto.MdfRequestVendorDTO;
import com.xtremand.mdf.dto.MdfUserDTO;
import com.xtremand.mdf.dto.VendorMdfAmountTilesDTO;
import com.xtremand.mdf.dto.VendorMdfRequestTilesDTO;
import com.xtremand.mdf.exception.MdfDataAccessException;
import com.xtremand.user.bom.Role;
import com.xtremand.user.dao.UserDAO;
import com.xtremand.util.DateUtils;
import com.xtremand.util.XamplifyUtil;
import com.xtremand.util.XamplifyUtils;
import com.xtremand.util.dao.UtilDao;
import com.xtremand.util.dto.TeamMemberFilterDTO;

@Repository
public class HibernateMdfDao implements MdfDao {

	private static final String VENDOR_COMPANY_ID = "vendorCompanyId";

	private static final String PARTNER_COMPANY_ID = "partnerCompanyId";

	private static final String ADMIN_ROLES = "( ur.role_id =" + Role.PRM_ROLE.getRoleId();

	private static final String MDF_DETAILS_ID = "mdfDetailsId";

	private static final String TOTAL_RECORDS = "totalRecords";

	private static final String REQUEST_ID = "requestId";

	@Value("${default.avatar}")
	private String defaultAvatar;

	@Value("${images.folder}")
	private String imagesFolder;

	@Value("${server_url}")
	private String serverPath;

	@Value("${vendorMdfAmountTilesInfoQuery}")
	private String vendorMdfAmountTilesInfoQuery;

	@Value("${vendorMdfAmountTilesInfoWithPartnerFilterQuery}")
	private String vendorMdfAmountTilesInfoWithPartnerFilterQuery;

	@Value("${vendorMdfRequestTilesQuery}")
	private String vendorMdfRequestTilesQuery;

	@Value("${listParntersQuery}")
	private String listPartnersQuery;

	@Value("${mdfPartnershipFilterQuery}")
	private String mdfPartnershipFilterQuery;

	@Value("${getMdfPartnerDetailsByPartnershipId}")
	private String getMdfPartnerDetailsByPartnershipId;

	@Value("${getMdfPartnerDetailsByMdfDetailsId}")
	private String getMdfPartnerDetailsByMdfDetailsId;

	@Value("${listParntersSearchQuery}")
	private String listPartnersSearchQuery;

	@Value("${parntershipCreatedOnSortQuery}")
	private String parntershipCreatedOnSortQuery;

	@Value("${partnerCompanyNameSortQuery}")
	private String partnerCompanyNameSortQuery;

	@Value("${partnerEmailIdSortQuery}")
	private String partnerEmailIdSortQuery;

	@Value("${partnerFistNameSortQuery}")
	private String partnerFistNameSortQuery;

	@Value("${partnerLastNameSortQuery}")
	private String partnerLastNameSortQuery;

	@Value("${partnerMdfBalancesQuery}")
	private String partnerMdfBalancesQuery;

	/******* MDF Requests **************/
	@Value("${mdfRequestPartnerTilesQueryWithOutVanityFilter}")
	private String mdfRequestPartnerTilesQueryWithOutVanityFilter;

	@Value("${mdfRequestPartnerTilesQueryWithVanityFilter}")
	private String mdfRequestPartnerTilesQueryWithVanityFilter;

	@Value("${listMdfAccessVendorsQuery}")
	private String listMdfAccessVendorsQuery;

	@Value("${listMdfAccessVendorCompanyIdParameter}")
	private String listMdfAccessVendorCompanyIdParameter;

	@Value("${listMdfAccessVendorsCreatedTimeSortQuery}")
	private String listMdfAccessVendorsCreatedTimeSortQuery;

	@Value("${listMdfAccessVendorsRequestsCountSortQuery}")
	private String listMdfAccessVendorsRequestsCountSortQuery;

	@Value("${listMdfAccessVendorsCompanyNameSortQuery}")
	private String listMdfAccessVendorsCompanyNameSortQuery;

	@Value("${listMdfAccessVendorsSearchQuery}")
	private String listMdfAccessVendorsSearchQuery;

	@Value("${listMdfAccessVendorsGroupByQuery}")
	private String listMdfAccessVendorsGroupByQuery;

	@Value("${mdfRequestFormDetailsQuery}")
	private String mdfRequestFormDetailsQuery;

	@Value("${mdfRequestOwnerNameAndCompanyQuery}")
	private String mdfRequestOwnerNameAndCompanyQuery;

	@Value("${mdfDetailsTimeLineHistoryQuery}")
	private String mdfDetailsTimeLineHistoryQuery;

	@Value("${mdfRequestTimeLineHistoryQuery}")
	private String mdfRequestTimeLineHistoryQuery;

	@Value("${mdfRequestDocumentsQuery}")
	private String mdfRequestDocumentsQuery;

	@Value("${mdfRequestCommentsQuery}")
	private String mdfRequestCommentsQuery;

	@Value("${partnerDetailsFromPartnershipQuery}")
	private String partnerDetailsFromPartnershipQuery;

	@Value("${contactCompanyNameQueryForVendorLogin}")
	private String contactCompanyNameQueryForVendorLogin;

	@Value("${listAllRequestTitlesByPartnerShipIdQuery}")
	private String listAllRequestTitlesByPartnerShipIdQuery;

	@Value("${sumOfAllocationAmountByPartnershipIdQuery}")
	private String sumOfAllocationAmountByPartnershipIdQuery;

	@Autowired
	private SessionFactory sessionFactory;

	@Autowired
	private UserDAO userDao;

	@Autowired
	private XamplifyUtil xamplifyUtil;

	@Autowired
	private UtilDao utilDao;

	@Override
	public void save(Object clazz) {
		try {
			sessionFactory.getCurrentSession().save(clazz);
		} catch (HibernateException | MdfDataAccessException e) {
			throw new MdfDataAccessException(e);
		} catch (Exception ex) {
			throw new MdfDataAccessException(ex);
		}
	}

	@Override
	public VendorMdfAmountTilesDTO getVendorMdfAmountTilesInfo(Integer vendorCompanyId, Integer loggedInUserId,
			boolean applyFilter) {
		try {
			if (vendorCompanyId != null && vendorCompanyId > 0) {
				String queryString = "";
				/************ XNFR-85 ********************/
				TeamMemberFilterDTO teamMemberFilterDTO = utilDao.applyFilterConditions(loggedInUserId, applyFilter,
						true);
				boolean applyPartnershipIdsFilter = teamMemberFilterDTO.isApplyTeamMemberFilter();
				if (teamMemberFilterDTO.isEmptyFilter()) {
					new VendorMdfAmountTilesDTO();
					return VendorMdfAmountTilesDTO.setDefaultData();
				} else {
					if (applyPartnershipIdsFilter) {
						queryString = vendorMdfAmountTilesInfoWithPartnerFilterQuery;
					} else {
						queryString = vendorMdfAmountTilesInfoQuery;
					}
					SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(queryString);
					query.setParameter(VENDOR_COMPANY_ID, vendorCompanyId);
					utilDao.applyPartnershipIdsParameterList(applyPartnershipIdsFilter,
							teamMemberFilterDTO.getPartnershipIdsOrPartnerCompanyIds(), query);
					return (VendorMdfAmountTilesDTO) query
							.setResultTransformer(Transformers.aliasToBean(VendorMdfAmountTilesDTO.class))
							.uniqueResult();
				}

			} else {
				new VendorMdfAmountTilesDTO();
				return VendorMdfAmountTilesDTO.setDefaultData();
			}
		} catch (HibernateException | MdfDataAccessException e) {
			throw new MdfDataAccessException(e);
		} catch (Exception ex) {
			throw new MdfDataAccessException(ex);
		}

	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> listPartners(Pagination pagination) {
		try {
			HashMap<String, Object> map = new HashMap<>();
			String finalQueryString = "";
			String sortQueryString = getSelectedSortOptionForPartners(pagination);
			String searchKey = pagination.getSearchKey();
			boolean hasSearchKey = StringUtils.hasText(searchKey);
			String findPartnersQuery = listPartnersQuery;
			/************* XNFR-85 **************/
			TeamMemberFilterDTO teamMemberFilterDTO = utilDao.applyFilterConditions(pagination.getUserId(),
					pagination.isPartnerTeamMemberGroupFilter(), true);
			boolean applyPartnershipIdsFilter = teamMemberFilterDTO.isApplyTeamMemberFilter();
			if (teamMemberFilterDTO.isEmptyFilter()) {
				map.put(TOTAL_RECORDS, 0);
				map.put("partners", new ArrayList<MdfParnterDTO>());
				return map;
			} else {
				if (applyPartnershipIdsFilter) {
					findPartnersQuery = findPartnersQuery + " " + mdfPartnershipFilterQuery;
				}
			}
			if (hasSearchKey) {
				finalQueryString = findPartnersQuery + " " + listPartnersSearchQuery.replace("searchKey", searchKey)
						+ " " + sortQueryString;
			} else {
				finalQueryString = findPartnersQuery + " " + sortQueryString;
			}
			SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(finalQueryString);
			query.setParameter(VENDOR_COMPANY_ID, pagination.getVendorCompanyId());
			utilDao.applyPartnershipIdsParameterList(applyPartnershipIdsFilter,
					teamMemberFilterDTO.getPartnershipIdsOrPartnerCompanyIds(), query);
			ScrollableResults scrollableResults = query.scroll();
			scrollableResults.last();
			Integer totalRecords = scrollableResults.getRowNumber() + 1;
			query.setFirstResult((pagination.getPageIndex() - 1) * pagination.getMaxResults());
			query.setMaxResults(pagination.getMaxResults());
			List<MdfParnterDTO> parnterDTOs = query.setResultTransformer(Transformers.aliasToBean(MdfParnterDTO.class))
					.list();
			map.put(TOTAL_RECORDS, totalRecords);
			map.put("partners", parnterDTOs);
			return map;
		} catch (HibernateException | MdfDataAccessException e) {
			throw new MdfDataAccessException(e);
		} catch (Exception ex) {
			throw new MdfDataAccessException(ex);
		}
	}

	@Override
	public MdfAmountTilesDTO getPartnerMdfAmountTilesInfo(Integer vendorCompanyId, Integer partnerCompanyId) {
		try {
			if (vendorCompanyId != null && vendorCompanyId > 0 && partnerCompanyId != null && partnerCompanyId > 0) {
				SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(partnerMdfBalancesQuery);
				query.setParameter(VENDOR_COMPANY_ID, vendorCompanyId);
				query.setParameter(PARTNER_COMPANY_ID, partnerCompanyId);
				return (MdfAmountTilesDTO) query.setResultTransformer(Transformers.aliasToBean(MdfAmountTilesDTO.class))
						.uniqueResult();
			} else {
				new MdfAmountTilesDTO();
				return MdfAmountTilesDTO.setDefaultData();
			}
		} catch (HibernateException | MdfDataAccessException e) {
			throw new MdfDataAccessException(e);
		} catch (Exception ex) {
			throw new MdfDataAccessException(ex);
		}

	}

	@Override
	public MdfDetails getMdfDetailsByPartnershipId(Integer partnershipId) {
		try {
			Session session = sessionFactory.getCurrentSession();
			org.hibernate.Criteria criteria = session.createCriteria(MdfDetails.class);
			criteria.add(Restrictions.eq("partnership.id", partnershipId));
			return (MdfDetails) criteria.uniqueResult();
		} catch (HibernateException | MdfDataAccessException e) {
			throw new MdfDataAccessException(e);
		} catch (Exception ex) {
			throw new MdfDataAccessException(ex);
		}
	}

	@Override
	public MdfRequestTilesDTO getMdfRequestsPartnerTilesForXamplifyLogin(Integer partnerCompanyId) {
		try {
			if (partnerCompanyId != null && partnerCompanyId > 0) {
				SQLQuery query = sessionFactory.getCurrentSession()
						.createSQLQuery(mdfRequestPartnerTilesQueryWithOutVanityFilter);
				query.setParameter(PARTNER_COMPANY_ID, partnerCompanyId);
				return (MdfRequestTilesDTO) query
						.setResultTransformer(Transformers.aliasToBean(MdfRequestTilesDTO.class)).uniqueResult();
			} else {
				new MdfRequestTilesDTO();
				return MdfRequestTilesDTO.setDefaultData();
			}
		} catch (HibernateException | MdfDataAccessException e) {
			throw new MdfDataAccessException(e);
		} catch (Exception ex) {
			throw new MdfDataAccessException(ex);
		}

	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> listVendorsAndRequestsCountByPartnerCompanyId(Pagination pagination) {
		try {
			HashMap<String, Object> map = new HashMap<>();
			String finalQueryString = "";
			String sortQueryString = getSelectedSortOptionForVendors(pagination);
			String searchKey = pagination.getSearchKey();
			boolean hasSearchKey = StringUtils.hasText(searchKey);
			/*** XNFR-252 *****/
			Integer loginAsUserId = pagination.getLoginAsUserId();
			boolean isLoginAsPartner = XamplifyUtils.isLoginAsPartner(loginAsUserId);
			if (pagination.isVanityUrlFilter() || isLoginAsPartner) {
				finalQueryString = listMdfAccessVendorsQuery + listMdfAccessVendorCompanyIdParameter
						+ listMdfAccessVendorsGroupByQuery + sortQueryString;
			} else {
				if (hasSearchKey) {
					finalQueryString = listMdfAccessVendorsQuery
							+ listMdfAccessVendorsSearchQuery.replace("searchKey", searchKey)
							+ listMdfAccessVendorsGroupByQuery + sortQueryString;
				} else {
					finalQueryString = listMdfAccessVendorsQuery + listMdfAccessVendorsGroupByQuery + sortQueryString;
				}
			}
			SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(finalQueryString);
			query.setParameter(PARTNER_COMPANY_ID, pagination.getPartnerCompanyId());
			if (pagination.isVanityUrlFilter()) {
				query.setParameter(VENDOR_COMPANY_ID,
						userDao.getCompanyIdByProfileName(utilDao.getPrmCompanyProfileName()));
			} else if (isLoginAsPartner) {
				query.setParameter(VENDOR_COMPANY_ID, userDao.getCompanyIdByUserId(loginAsUserId));
			}
			ScrollableResults scrollableResults = query.scroll();
			scrollableResults.last();
			Integer totalRecords = scrollableResults.getRowNumber() + 1;
			query.setFirstResult((pagination.getPageIndex() - 1) * pagination.getMaxResults());
			query.setMaxResults(pagination.getMaxResults());
			List<MdfRequestVendorDTO> vendors = query
					.setResultTransformer(Transformers.aliasToBean(MdfRequestVendorDTO.class)).list();
			List<MdfRequestVendorDTO> updatedVendors = new ArrayList<>();
			for (MdfRequestVendorDTO mdfRequestVendorDTO : vendors) {
				String companyLogoPath = mdfRequestVendorDTO.getCompanyLogoPath();
				mdfRequestVendorDTO.setCompanyLogoPath(getCompleteImagePath(companyLogoPath));
				updatedVendors.add(mdfRequestVendorDTO);
			}
			map.put(TOTAL_RECORDS, totalRecords);
			map.put("vendors", updatedVendors);
			return map;
		} catch (HibernateException | MdfDataAccessException e) {
			throw new MdfDataAccessException(e);
		} catch (Exception ex) {
			throw new MdfDataAccessException(ex);
		}
	}

	private String getCompleteImagePath(String companyLogoPath) {
		if (org.springframework.util.StringUtils.hasText(companyLogoPath)) {
			if (serverPath.indexOf("localhost") > -1) {
				return xamplifyUtil.getImagesPrefixPath() + companyLogoPath;
			} else {
				return serverPath + imagesFolder + companyLogoPath;
			}
		} else {
			return defaultAvatar;
		}
	}

	private String getSelectedSortOptionForPartners(Pagination pagination) {
		String sortOptionQueryString = "";
		if (StringUtils.hasText(pagination.getSortcolumn())) {
			if ("firstName".equals(pagination.getSortcolumn())) {
				sortOptionQueryString += partnerFistNameSortQuery + pagination.getSortingOrder();
				sortOptionQueryString = setNullConditionsForAscOrDesc(pagination, sortOptionQueryString);
			} else if ("lastName".equals(pagination.getSortcolumn())) {
				sortOptionQueryString += partnerLastNameSortQuery + pagination.getSortingOrder();
				sortOptionQueryString = setNullConditionsForAscOrDesc(pagination, sortOptionQueryString);
			} else if ("emailId".equals(pagination.getSortcolumn())) {
				sortOptionQueryString += partnerEmailIdSortQuery + pagination.getSortingOrder();
			} else if ("contactCompany".equals(pagination.getSortcolumn())) {
				sortOptionQueryString += partnerCompanyNameSortQuery + pagination.getSortingOrder();
			} else if ("createdTime".equals(pagination.getSortcolumn())) {
				sortOptionQueryString += parntershipCreatedOnSortQuery + pagination.getSortingOrder();
			}
		} else {
			sortOptionQueryString += parntershipCreatedOnSortQuery + " desc";
		}
		return sortOptionQueryString;
	}

	private String getSelectedSortOptionForVendors(Pagination pagination) {
		String sortOptionQueryString = "";
		if (StringUtils.hasText(pagination.getSortcolumn())) {
			if ("companyName".equals(pagination.getSortcolumn())) {
				sortOptionQueryString += listMdfAccessVendorsCompanyNameSortQuery + pagination.getSortingOrder();
				sortOptionQueryString = setNullConditionsForAscOrDesc(pagination, sortOptionQueryString);
			} else if ("count".equals(pagination.getSortcolumn())) {
				sortOptionQueryString += listMdfAccessVendorsRequestsCountSortQuery + pagination.getSortingOrder();
				sortOptionQueryString = setNullConditionsForAscOrDesc(pagination, sortOptionQueryString);
			} else if ("createdTime".equals(pagination.getSortcolumn())) {
				sortOptionQueryString += listMdfAccessVendorsCreatedTimeSortQuery + pagination.getSortingOrder();
			}
		} else {
			sortOptionQueryString += listMdfAccessVendorsCreatedTimeSortQuery + " desc";
		}
		return sortOptionQueryString;
	}

	private String setNullConditionsForAscOrDesc(Pagination pagination, String sortOptionQueryString) {
		if (SORTINGORDER.ASC.name().equals(pagination.getSortingOrder())) {
			sortOptionQueryString += " nulls first";
		} else {
			sortOptionQueryString += " nulls last";
		}
		return sortOptionQueryString;
	}

	@Override
	public void save(MdfRequest mdfRequest) {
		try {
			sessionFactory.getCurrentSession().save(mdfRequest);
		} catch (HibernateException | MdfDataAccessException e) {
			throw new MdfDataAccessException(e);
		} catch (Exception ex) {
			throw new MdfDataAccessException(ex);
		}
	}

	@Override
	public void save(MdfRequestHistory mdfRequestHistory) {
		try {
			sessionFactory.getCurrentSession().save(mdfRequestHistory);
		} catch (HibernateException | MdfDataAccessException e) {
			throw new MdfDataAccessException(e);
		} catch (Exception ex) {
			throw new MdfDataAccessException(ex);
		}
	}

	@Override
	public MdfRequestTilesDTO getMdfRequestsPartnerTilesForVanityLogin(Integer partnerCompanyId,
			Integer vendorCompanyId) {
		try {
			if (partnerCompanyId != null && partnerCompanyId > 0 && vendorCompanyId != null && vendorCompanyId > 0) {
				SQLQuery query = sessionFactory.getCurrentSession()
						.createSQLQuery(mdfRequestPartnerTilesQueryWithVanityFilter);
				query.setParameter(PARTNER_COMPANY_ID, partnerCompanyId);
				query.setParameter(VENDOR_COMPANY_ID, vendorCompanyId);
				return (MdfRequestTilesDTO) query
						.setResultTransformer(Transformers.aliasToBean(MdfRequestTilesDTO.class)).uniqueResult();
			} else {
				new MdfRequestTilesDTO();
				return MdfRequestTilesDTO.setDefaultData();
			}
		} catch (HibernateException | MdfDataAccessException e) {
			throw new MdfDataAccessException(e);
		} catch (Exception ex) {
			throw new MdfDataAccessException(ex);
		}

	}

	@Override
	public VendorMdfRequestTilesDTO getMdfRequestTilesInfoForVendors(Integer vendorCompanyId, Integer loggedInUserId,
			boolean addFilter) {
		try {
			if (vendorCompanyId != null && vendorCompanyId > 0) {
				String queryString = "";
				TeamMemberFilterDTO teamMemberFilterDTO = utilDao.applyFilterConditions(loggedInUserId, addFilter,
						true);
				boolean applyPartnershipFilter = teamMemberFilterDTO.isApplyTeamMemberFilter();
				if (teamMemberFilterDTO.isEmptyFilter()) {
					new VendorMdfRequestTilesDTO();
					return VendorMdfRequestTilesDTO.setDefaultData();
				} else {
					if (applyPartnershipFilter) {
						queryString = vendorMdfRequestTilesQuery + "  and xp.id in (:partnershipIds)";
					} else {
						queryString = vendorMdfRequestTilesQuery;
					}
					SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(queryString);
					query.setParameter(VENDOR_COMPANY_ID, vendorCompanyId);
					utilDao.applyPartnershipIdsParameterList(applyPartnershipFilter,
							teamMemberFilterDTO.getPartnershipIdsOrPartnerCompanyIds(), query);
					return (VendorMdfRequestTilesDTO) query
							.setResultTransformer(Transformers.aliasToBean(VendorMdfRequestTilesDTO.class))
							.uniqueResult();
				}

			} else {
				new VendorMdfRequestTilesDTO();
				return VendorMdfRequestTilesDTO.setDefaultData();
			}
		} catch (HibernateException | MdfDataAccessException e) {
			throw new MdfDataAccessException(e);
		} catch (Exception ex) {
			throw new MdfDataAccessException(ex);
		}

	}

	@Override
	public MdfRequest getMdfRequestById(Integer id) {
		try {
			Session session = sessionFactory.getCurrentSession();
			org.hibernate.Criteria criteria = session.createCriteria(MdfRequest.class);
			criteria.add(Restrictions.eq("id", id));
			return (MdfRequest) criteria.uniqueResult();
		} catch (HibernateException | MdfDataAccessException e) {
			throw new MdfDataAccessException(e);
		} catch (Exception ex) {
			throw new MdfDataAccessException(ex);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<String> listTitleAndEventDateAndRequestAmountByRequestId(Integer requestId) {
		try {
			Session session = sessionFactory.getCurrentSession();
			return session.createSQLQuery(mdfRequestFormDetailsQuery).setParameter(REQUEST_ID, requestId).list();
		} catch (HibernateException | MdfDataAccessException e) {
			throw new MdfDataAccessException(e);
		} catch (Exception ex) {
			throw new MdfDataAccessException(ex);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean validateRequestId(Integer requestId, Integer loggedInUserCompanyId) {
		try {
			Session session = sessionFactory.getCurrentSession();
			String sqlString = "select p.vendor_company_id,p.partner_company_id from xt_partnership p,xt_mdf_request r "
					+ " where p.id = r.partnership_id and r.id = :requestId";
			List<Object[]> list = session.createSQLQuery(sqlString).setParameter(REQUEST_ID, requestId).list();
			if (!list.isEmpty()) {
				Integer vendorCompanyId = (Integer) list.get(0)[0];
				Integer partnerCompanyId = (Integer) list.get(0)[1];
				return loggedInUserCompanyId.equals(vendorCompanyId) || loggedInUserCompanyId.equals(partnerCompanyId);
			} else {
				return false;
			}
		} catch (HibernateException | MdfDataAccessException e) {
			throw new MdfDataAccessException(e);
		} catch (Exception ex) {
			throw new MdfDataAccessException(ex);
		}
	}

	@Override
	public MdfUserDTO getMdfVendorDetails(Integer vendorCompanyId) {
		try {
			if (vendorCompanyId != null && vendorCompanyId > 0) {
				String queryString = "select distinct u.email_id as \"emailId\" ,u.firstname ||' '|| u.lastname ||' '|| u.middle_name as \"fullName\","
						+ "	c.company_name as \"companyName\",c.website as \"website\",u.mobile_number as \"phoneNumber\",u.profile_image as \"profilePicturePath\" from  xt_company_profile c,xt_user_profile u,xt_user_role ur where c.company_id = :vendorCompanyId"
						+ "	 and u.company_id = c.company_id and ur.user_id = u.user_id and " + ADMIN_ROLES + ") and "
						+ " u.user_id not in (select team_member_id from xt_team_member where company_id = :vendorCompanyId)";
				SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(queryString);
				query.setParameter(VENDOR_COMPANY_ID, vendorCompanyId);
				return (MdfUserDTO) query.setResultTransformer(Transformers.aliasToBean(MdfUserDTO.class))
						.uniqueResult();
			} else {
				return new MdfUserDTO();
			}
		} catch (HibernateException | MdfDataAccessException e) {
			throw new MdfDataAccessException(e);
		} catch (Exception ex) {
			throw new MdfDataAccessException(ex);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Object[]> getMdfOwnerNameAndContactCompany(Integer vendorCompanyId, List<Integer> userIds) {
		try {
			Session session = sessionFactory.getCurrentSession();
			return session.createSQLQuery(mdfRequestOwnerNameAndCompanyQuery)
					.setParameter(VENDOR_COMPANY_ID, vendorCompanyId).setParameterList("userIds", userIds).list();
		} catch (HibernateException | MdfDataAccessException e) {
			throw new MdfDataAccessException(e);
		} catch (Exception ex) {
			throw new MdfDataAccessException(ex);
		}
	}

	@Override
	public MdfUserDTO getMdfRequestOwnerDetails(Integer userId) {
		try {
			if (userId != null && userId > 0) {
				String queryString = "select email_id as \"emailId\",  firstname ||' '|| lastname ||' '|| middle_name as \"fullName\", profile_image as \"profilePicturePath\" from  xt_user_profile "
						+ "	where user_id=:userId";
				SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(queryString);
				query.setParameter("userId", userId);
				return (MdfUserDTO) query.setResultTransformer(Transformers.aliasToBean(MdfUserDTO.class))
						.uniqueResult();
			} else {
				return new MdfUserDTO();
			}
		} catch (HibernateException | MdfDataAccessException e) {
			throw new MdfDataAccessException(e);
		} catch (Exception ex) {
			throw new MdfDataAccessException(ex);
		}
	}

	@Override
	public MdfUserDTO getPartnerManagerDetails(Integer companyId) {
		try {
			if (companyId != null && companyId > 0) {
				String queryString = "select distinct u.user_id as \"userId\", u.email_id as \"emailId\" ,u.firstname ||' '|| u.lastname ||' '|| u.middle_name as \"fullName\","
						+ " u.profile_image as \"profilePicturePath\",u.occupation as \"title\",u.mobile_number as \"phoneNumber\" from xt_user_profile u,xt_user_role ur where u.user_id = ur.user_id "
						+ "	 and " + ADMIN_ROLES + " or  ur.role_id=" + Role.COMPANY_PARTNER.getRoleId()
						+ ") and u.company_id=:companyId and "
						+ " u.user_id not in (select team_member_id from xt_team_member where company_id = :companyId)";
				SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(queryString);
				query.setParameter("companyId", companyId);
				return (MdfUserDTO) query.setResultTransformer(Transformers.aliasToBean(MdfUserDTO.class))
						.uniqueResult();
			} else {
				return new MdfUserDTO();
			}
		} catch (HibernateException | MdfDataAccessException e) {
			throw new MdfDataAccessException(e);
		} catch (Exception ex) {
			throw new MdfDataAccessException(ex);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Object[]> getVendorCompanyIdAndPartnerCompanyIdByRequestId(Integer requestId) {
		try {
			if (requestId != null && requestId > 0) {
				String queryString = "select p.vendor_company_id,p.partner_company_id from xt_partnership p,xt_mdf_request r where r.partnership_id = p.id and r.id = :requestId";
				SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(queryString);
				query.setParameter(REQUEST_ID, requestId);
				return query.list();
			} else {
				return null;
			}
		} catch (HibernateException | MdfDataAccessException e) {
			throw new MdfDataAccessException(e);
		} catch (Exception ex) {
			throw new MdfDataAccessException(ex);
		}
	}

	@Override
	public Double getSumOfAllocationAmountByRequestId(Integer requestId) {
		try {
			if (requestId != null && requestId > 0) {
				String queryString = "select sum(allocation_amount) from xt_mdf_request_history where request_id = :requestId";
				SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(queryString);
				query.setParameter(REQUEST_ID, requestId);
				Double sum = (Double) query.uniqueResult();
				return sum != null ? sum : Double.valueOf(0);
			} else {
				return Double.valueOf(0);
			}
		} catch (HibernateException | MdfDataAccessException e) {
			throw new MdfDataAccessException(e);
		} catch (Exception ex) {
			throw new MdfDataAccessException(ex);
		}
	}

	@Override
	public Double getSumOfReimbursementAmountByRequestId(Integer requestId) {
		try {
			if (requestId != null && requestId > 0) {
				String queryString = "select sum(reimburse_amount) from xt_mdf_request_history where request_id = :requestId";
				SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(queryString);
				query.setParameter(REQUEST_ID, requestId);
				Double sum = (Double) query.uniqueResult();
				return sum != null ? sum : Double.valueOf(0);
			} else {
				return Double.valueOf(0);
			}
		} catch (HibernateException | MdfDataAccessException e) {
			throw new MdfDataAccessException(e);
		} catch (Exception ex) {
			throw new MdfDataAccessException(ex);
		}
	}

	@Override
	public MdfParnterDTO getPartnerAndMdfAmountDetails(Integer partnershipId) {
		try {
			if (partnershipId != null && partnershipId > 0) {
				SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(getMdfPartnerDetailsByPartnershipId);
				query.setParameter("partnershipId", partnershipId);
				return (MdfParnterDTO) query.setResultTransformer(Transformers.aliasToBean(MdfParnterDTO.class))
						.uniqueResult();
			} else {
				MdfParnterDTO defaultMdfParnterDTO = new MdfParnterDTO();
				defaultMdfParnterDTO.setPartnershipId(0);
				return defaultMdfParnterDTO;
			}
		} catch (HibernateException | MdfDataAccessException e) {
			throw new MdfDataAccessException(e);
		} catch (Exception ex) {
			throw new MdfDataAccessException(ex);
		}

	}

	@Override
	public boolean validateMdfDetailsId(Integer mdfDetailsId, Integer loggedInUserCompanyId) {
		try {
			String sqlString = "select case when count(*)>0 then true else false end  from xt_mdf_details where company_id = :companyId and id = :mdfDetailsId";
			SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(sqlString);
			query.setParameter("companyId", loggedInUserCompanyId);
			query.setParameter(MDF_DETAILS_ID, mdfDetailsId);
			return (boolean) query.uniqueResult();
		} catch (HibernateException | MdfDataAccessException e) {
			throw new MdfDataAccessException(e);
		} catch (Exception ex) {
			throw new MdfDataAccessException(ex);
		}
	}

	@Override
	public MdfParnterDTO getPartnerAndMdfAmountDetailsByMdfDetailsId(Integer mdfDetailsId) {
		try {
			if (mdfDetailsId != null && mdfDetailsId > 0) {
				SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(getMdfPartnerDetailsByMdfDetailsId);
				query.setParameter(MDF_DETAILS_ID, mdfDetailsId);
				return (MdfParnterDTO) query.setResultTransformer(Transformers.aliasToBean(MdfParnterDTO.class))
						.uniqueResult();
			} else {
				MdfParnterDTO defaultMdfParnterDTO = new MdfParnterDTO();
				defaultMdfParnterDTO.setPartnershipId(0);
				return defaultMdfParnterDTO;
			}
		} catch (HibernateException | MdfDataAccessException e) {
			throw new MdfDataAccessException(e);
		} catch (Exception ex) {
			throw new MdfDataAccessException(ex);
		}

	}

	@SuppressWarnings("unchecked")
	@Override
	public List<MdfDetailsTimeLineDTO> listMdfDetailsTimeLineHistory(Integer mdfDetailsId) {
		try {
			SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(mdfDetailsTimeLineHistoryQuery);
			query.setParameter(MDF_DETAILS_ID, mdfDetailsId);
			List<MdfDetailsTimeLineDTO> timeLineHistory = query
					.setResultTransformer(Transformers.aliasToBean(MdfDetailsTimeLineDTO.class)).list();
			if (timeLineHistory != null && !timeLineHistory.isEmpty()) {
				return timeLineHistory;
			} else {
				return new ArrayList<>();
			}
		} catch (HibernateException | MdfDataAccessException e) {
			throw new MdfDataAccessException(e);
		} catch (Exception ex) {
			throw new MdfDataAccessException(ex);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<MdfRequestTimeLineDTO> listMdfRequestTimeLineHistory(Integer requestId) {
		try {
			SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(mdfRequestTimeLineHistoryQuery);
			query.setParameter(REQUEST_ID, requestId);
			List<MdfRequestTimeLineDTO> timeLineHistory = query
					.setResultTransformer(Transformers.aliasToBean(MdfRequestTimeLineDTO.class)).list();
			if (timeLineHistory != null && !timeLineHistory.isEmpty()) {
				return timeLineHistory;
			} else {
				return new ArrayList<>();
			}
		} catch (HibernateException | MdfDataAccessException e) {
			throw new MdfDataAccessException(e);
		} catch (Exception ex) {
			throw new MdfDataAccessException(ex);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> listRequestDocuments(Pagination pagination) {
		try {
			HashMap<String, Object> map = new HashMap<>();
			SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(mdfRequestDocumentsQuery);
			query.setParameter(REQUEST_ID, pagination.getCategoryId());
			ScrollableResults scrollableResults = query.scroll();
			scrollableResults.last();
			Integer totalRecords = scrollableResults.getRowNumber() + 1;
			query.setFirstResult((pagination.getPageIndex() - 1) * pagination.getMaxResults());
			query.setMaxResults(pagination.getMaxResults());
			List<MdfRequestDocumentDTO> documents = query
					.setResultTransformer(Transformers.aliasToBean(MdfRequestDocumentDTO.class)).list();
			List<MdfRequestDocumentDTO> updatedDocuments = new ArrayList<>();
			for (MdfRequestDocumentDTO document : documents) {
				MdfRequestDocumentDTO updatedDocumentDto = new MdfRequestDocumentDTO();
				BeanUtils.copyProperties(document, updatedDocumentDto);
				updatedDocumentDto
						.setProfilePicturePath(xamplifyUtil.getCompleteImagePath(document.getProfilePicturePath()));
				updatedDocumentDto.setUploadedTimeInUTCString(DateUtils.getUtcString(document.getUploadedTime()));
				updatedDocuments.add(updatedDocumentDto);
			}
			map.put(TOTAL_RECORDS, totalRecords);
			map.put("documents", updatedDocuments);
			return map;
		} catch (HibernateException | MdfDataAccessException e) {
			throw new MdfDataAccessException(e);
		} catch (Exception ex) {
			throw new MdfDataAccessException(ex);
		}
	}

	@Override
	public String getMdfDocumentAwsFilePathByAlias(String alias) {
		try {
			SQLQuery query = sessionFactory.getCurrentSession()
					.createSQLQuery("select file_path from xt_mdf_request_documents where file_path_alias=:alias");
			query.setParameter("alias", alias);
			return query.uniqueResult() != null ? (String) query.uniqueResult() : "";
		} catch (HibernateException | MdfDataAccessException e) {
			throw new MdfDataAccessException(e);
		} catch (Exception ex) {
			throw new MdfDataAccessException(ex);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<MdfRequestCommentDTO> listMdfRequestComments(Integer requestId) {
		try {
			SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(mdfRequestCommentsQuery);
			query.setParameter(REQUEST_ID, requestId);
			return query.setResultTransformer(Transformers.aliasToBean(MdfRequestCommentDTO.class)).list();
		} catch (HibernateException | MdfDataAccessException e) {
			throw new MdfDataAccessException(e);
		} catch (Exception ex) {
			throw new MdfDataAccessException(ex);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<UserDTO> listPartnerDetailsFromUserList(Integer partnershipId, Integer userId) {
		try {
			if (partnershipId != null && userId != null && partnershipId > 0 && userId > 0) {
				SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(partnerDetailsFromPartnershipQuery);
				query.setParameter("partnershipId", partnershipId);
				query.setParameter("userId", userId);
				List<UserDTO> users = query.setResultTransformer(Transformers.aliasToBean(UserDTO.class)).list();
				if (users != null && !users.isEmpty()) {
					return users;
				} else {
					return new ArrayList<>();
				}
			} else {
				return new ArrayList<>();
			}

		} catch (HibernateException | MdfDataAccessException e) {
			throw new MdfDataAccessException(e);
		} catch (Exception ex) {
			throw new MdfDataAccessException(ex);
		}
	}

	@Override
	public String getPartnerCompanyNameByVendorCompanyId(Integer vendorCompanyId, Integer requestCreatedBy) {
		try {
			Session session = sessionFactory.getCurrentSession();
			return (String) session.createSQLQuery(contactCompanyNameQueryForVendorLogin)
					.setParameter(VENDOR_COMPANY_ID, vendorCompanyId).setParameter("createdBy", requestCreatedBy)
					.uniqueResult();
		} catch (HibernateException | MdfDataAccessException e) {
			throw new MdfDataAccessException(e);
		} catch (Exception ex) {
			throw new MdfDataAccessException(ex);
		}
	}

	@Override
	public String getPartnerDisplayNameForMdfRequest(Integer partnershipId, Integer requestCreatedBy) {
		try {
			List<UserDTO> userDtos = listPartnerDetailsFromUserList(partnershipId, requestCreatedBy);
			if (userDtos != null && !userDtos.isEmpty()) {
				UserDTO userDto = userDtos.get(0);
				String fullName = userDto.getFullName();
				if (fullName != null && StringUtils.hasText(fullName.trim())) {
					return fullName;
				} else {
					return "";
				}
			} else {
				return userDao.getDisplayName(requestCreatedBy);
			}
		} catch (HibernateException | MdfDataAccessException e) {
			throw new MdfDataAccessException(e);
		} catch (Exception ex) {
			throw new MdfDataAccessException(ex);
		}
	}

	@Override
	public String getPartnerDisplayNameForMdfEmailNotification(Integer partnershipId, Integer requestCreatedBy) {
		try {
			List<UserDTO> userDtos = listPartnerDetailsFromUserList(partnershipId, requestCreatedBy);
			if (userDtos != null && !userDtos.isEmpty()) {
				UserDTO userDto = userDtos.get(0);
				String fullName = userDto.getFullName();
				if (fullName != null && StringUtils.hasText(fullName.trim())) {
					return fullName;
				} else {
					return userDao.getEmailIdByUserId(requestCreatedBy);
				}
			} else {
				return userDao.getDisplayName(requestCreatedBy);
			}
		} catch (HibernateException | MdfDataAccessException e) {
			throw new MdfDataAccessException(e);
		} catch (Exception ex) {
			throw new MdfDataAccessException(ex);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<String> listAllRequestTitlesByPartnershipId(Integer partnershipId) {
		try {
			Session session = sessionFactory.getCurrentSession();
			return session.createSQLQuery(listAllRequestTitlesByPartnerShipIdQuery)
					.setParameter("partnershipId", partnershipId).list();
		} catch (HibernateException | MdfDataAccessException e) {
			throw new MdfDataAccessException(e);
		} catch (Exception ex) {
			throw new MdfDataAccessException(ex);
		}
	}

	@Override
	public Double getSumOfAllocationAmountByPartnershipId(Integer partnershipId) {
		try {
			Session session = sessionFactory.getCurrentSession();
			return (Double) session.createSQLQuery(sumOfAllocationAmountByPartnershipIdQuery)
					.setParameter("partnershipId", partnershipId).uniqueResult();
		} catch (HibernateException | MdfDataAccessException e) {
			throw new MdfDataAccessException(e);
		} catch (Exception ex) {
			throw new MdfDataAccessException(ex);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<String> getUserEmailIdsForDownloadingDocuments(String alias) {
		try {
			Session session = sessionFactory.getCurrentSession();
			String sqlQuery = " select LOWER(TRIM(u.email_id)) from xt_mdf_request mr,xt_mdf_request_documents mrd,xt_partnership p,xt_user_profile u "
					+ " where mr.id = mrd.request_id and mrd.file_path_alias = :alias and p.id = mr.partnership_id "
					+ " and (u.company_id = p.partner_company_id or u.company_id = p.vendor_company_id  or u.user_id = 1)";
			return session.createSQLQuery(sqlQuery).setParameter("alias", alias).list();
		} catch (HibernateException | MdfDataAccessException e) {
			throw new MdfDataAccessException(e);
		} catch (Exception ex) {
			throw new MdfDataAccessException(ex);
		}
	}

}
