package com.xtremand.form.submit.dao.hibernate;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.hibernate.FetchMode;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;
import org.hibernate.transform.Transformers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.xtremand.common.bom.Criteria;
import com.xtremand.common.bom.FindLevel;
import com.xtremand.common.bom.Pagination;
import com.xtremand.common.bom.Pagination.SORTINGORDER;
import com.xtremand.form.dao.FormDao;
import com.xtremand.form.dto.FormDataDto;
import com.xtremand.form.dto.FormLabelDTO;
import com.xtremand.form.dto.FormValue;
import com.xtremand.form.submit.bom.FormSubmit;
import com.xtremand.form.submit.bom.FormSubmitMultiChoice;
import com.xtremand.form.submit.dao.FormSubmitDao;
import com.xtremand.form.submit.dto.FormSubmitDTO;
import com.xtremand.form.submit.dto.FormSubmitFieldsValuesDTO;
import com.xtremand.formbeans.EventCheckInDTO;
import com.xtremand.formbeans.UserDTO;
import com.xtremand.landing.page.analytics.bom.GeoLocationAnalytics;
import com.xtremand.mdf.dto.MdfFormSubmittedDetailsDTO;
import com.xtremand.util.DateUtils;
import com.xtremand.util.XamplifyUtil;
import com.xtremand.util.XamplifyUtils;
import com.xtremand.util.dao.HibernateSQLQueryResultUtilDao;
import com.xtremand.util.dao.UtilDao;
import com.xtremand.util.dto.HibernateSQLQueryResultRequestDTO;
import com.xtremand.util.dto.QueryParameterDTO;
import com.xtremand.util.dto.TeamMemberFilterDTO;
import com.xtremand.util.dto.XamplifyConstants;

@Repository
@Transactional
public class HibernateFormSubmitDao implements FormSubmitDao {

	private static final String MULTI_CHOICE_QUERY_STRING = "select distinct fs.id from xt_form_submit fs,xt_form_submit_multi_choice mc,xt_form_label_choice lc where mc.form_submit_id  = fs.id  ";

	private static final String SEARCH_QUERY_STRING = " select distinct fs.id from xt_form_submit fs,xt_form_submit_single_choice ssc where fs.id=ssc.form_submit_id ";

	private static final String FS_FORM_ID = " and fs.form_id = ";

	private static final String FORM_SEARCH_DISTINCT_PREFIX = "select distinct fs.id from xt_form_submit fs,xt_form_submit_single_choice ssc,xt_form_campaign_submit c where fs.id=ssc.form_submit_id ";

	private static final String USER_ID = "userId";

	private static final String CAMPAIGN_IDS = "campaignIds";

	private static final String USER_NULL_QUERY = " and xfcs.user_id IS NULL ";

	private static final String FORM_ID_PARAM = "formId"; // Compliant

	private static final String UNION_ID = ") UNION ";

	private static final String LOWER_SEARCH_VALUE = " and LOWER(ssc.value) like LOWER(";

	private static final String LABEL_CHOICE_NAME = " and LOWER(lc.label_choice_name) like LOWER(";

	private static final String CAMPAIGN_ID = "campaignId";

	private static final String LANDING_PAGE_ID_QUERY_STRING = "  and fs.landing_page_id = ";

	private static final String ORDER_BY_ID_DESC = " order by id desc";

	private static final String PARTNERSHIP_ID = "partnershipId";

	private static final String MDF_FORM_SUBMIT_QUERY_PREFIX = " select distinct fs.id as \"id\",u.email_id as \"emailId\",c.company_name as \"companyName\",cast(r.status as text) as \"requestStatus\",r.created_time as \"createdTime\","
			+ " c.company_logo as \"companyLogo\",r.created_by as \"createdBy\",r.id as \"requestId\",r.partnership_id as \"partnershipId\", TRIM(concat(coalesce(uul.firstname,''), ' ', coalesce(uul.lastname),'')) as \"fullName\", "
			+ "TRIM(coalesce(uul.contact_company,'')) as \"partnerCompanyName\", p.status as \"partnerStatus\" ";

	private static final String MDF_FORM_SUBMIT_QUERY_TABLES = " xt_form_submit fs,xt_mdf_request r,xt_user_profile u,xt_company_profile c,xt_partnership p,xt_user_userlist uul,xt_user_list ul ";

	private static final String MDF_FORM_SUBMIT_QUERY_SUFFIX = " u.user_id =r.created_by and  u.company_id = c.company_id and fs.request_id = r.id and p.id = r.partnership_id and p.partner_id = uul.user_id and ul.user_list_id = uul.user_list_id and ul.is_default_partnerlist and ul.company_id = p.vendor_company_id  ";

	private static final String MDF_FORM_SUBMIT_QUERY_STRING = MDF_FORM_SUBMIT_QUERY_PREFIX + " from xt_form f, "
			+ MDF_FORM_SUBMIT_QUERY_TABLES + " where " + MDF_FORM_SUBMIT_QUERY_SUFFIX
			+ " and f.id = :formId and fs.form_id = f.id ";

	private static final String ORDER_BY_FORM_SUBMIT_QUERY = " order by fs.id desc";

	private static final String MDF_FORM_SUBMIT_SINGLE_CHOICE_SEARCH_QUERY_PREFIX = MDF_FORM_SUBMIT_QUERY_PREFIX
			+ " from xt_form_submit_single_choice ssc," + MDF_FORM_SUBMIT_QUERY_TABLES
			+ " where  fs.id=ssc.form_submit_id and " + MDF_FORM_SUBMIT_QUERY_SUFFIX + FS_FORM_ID;

	private static final String MDF_FORM_SUBMIT_MULTI_CHOICE_SEARCH_QUERY_PREFIX = MDF_FORM_SUBMIT_QUERY_PREFIX
			+ " from xt_form_submit_multi_choice mc,xt_form_label_choice lc," + MDF_FORM_SUBMIT_QUERY_TABLES
			+ " where  mc.form_submit_id  = fs.id and lc.id = mc.form_label_choice_id and  "
			+ MDF_FORM_SUBMIT_QUERY_SUFFIX + FS_FORM_ID;

	private static final String SUBMIT_ID = "submitId";

	private static final String PARTNERSHIP_ID_IN_QUERY_STRING = " and r.partnership_id in (:partnershipIds) ";

	private static final String PARTNERSHIP_ID_EQUALS_QUERY_STRING = " and r.partnership_id = :partnershipId ";

	private static final String OR_QUERY_STRING = ") or  ";

	@Value("${analytics.email}")
	private String analyticsEmail;

	@Value("${analytics.firstname}")
	private String analyticsFirstName;

	@Value("${analytics.lastname}")
	private String analyticsLastName;

	@Value("${analytics.companyName}")
	private String analyticsCompanyName;

	@Value("${analytics.checkIn}")
	private String checkIn;

	@Value("${analytics.checkInTime}")
	private String checkInTime;

	@Value("${default.avatar}")
	private String defaultAvatar;

	@Value("${images.folder}")
	private String imagesFolder;

	@Value("${server_url}")
	private String serverPath;

	@Value("${contactCompanyNameQuery}")
	private String contactCompanyNameQuery;

	@Value("${contactCompanyNameQueryForVendorLogin}")
	private String contactCompanyNameQueryForVendorLogin;

	@Value("${requestDetailsByFormSubmitIdQuery}")
	private String requestDetailsByFormSubmitIdQuery;

	@Autowired
	private SessionFactory sessionFactory;

	@Autowired
	private FormDao formDao;

	@Autowired
	private XamplifyUtil xamplifyUtil;

	@Autowired
	private UtilDao utilDao;

	@Autowired
	private HibernateSQLQueryResultUtilDao hibernateSQLQueryResultUtilDao;

	@Override
	public FormSubmit findByPrimaryKey(Serializable pk, FindLevel[] levels) {
		return null;
	}

	@Override
	public Collection<FormSubmit> find(List<Criteria> criterias, FindLevel[] levels) {
		return Collections.emptyList();
	}

	@Override
	public Map<String, Object> find(List<Criteria> criterias, FindLevel[] levels, Pagination pagination) {
		Map<String, Object> map = new HashMap<>();
		Session session = sessionFactory.getCurrentSession();
		Integer formId = pagination.getCompanyId();
		List<FormLabelDTO> columns = new ArrayList<>();
		if (pagination.isVendorJourney()) {
			addColumns("Partner Details", 1, columns);
		}
		columns.addAll(formDao.listFormLabelsById(formId, false, false, 0));
		Integer formColumnsLength = columns.size();
		addAdditionalColumns(pagination, columns);
		map.put("columns", columns);
		map.put("formName", pagination.getFormName());
		/** XNFR-766 **/
		map.put("isEmailFieldExists", isEmailFieldExists(formId));
		SQLQuery formSubmitQuery = null;
		formSubmitQuery = formAnalyticsQuery(pagination, session, formId);
		getTotalRecordsAndDtoList(pagination, map, session, columns, null, formSubmitQuery, formColumnsLength);
		return map;
	}

	private SQLQuery mdfFormAnalyticsQuery(Pagination pagination, Session session, Integer formId) {
		SQLQuery formSubmitQuery;
		String formSubmitQueryString;
		if (StringUtils.hasText(pagination.getSearchKey())) {
			String searchKey = "'%" + pagination.getSearchKey() + "%'";
			String lowerCloumns = " or " + " LOWER(u.email_id) like  LOWER(" + searchKey + ") or "
					+ " LOWER(c.company_name) like  LOWER(" + searchKey + OR_QUERY_STRING
					+ " LOWER(uul.firstname) like  LOWER(" + searchKey + OR_QUERY_STRING
					+ " LOWER(uul.lastname) like  LOWER(" + searchKey + OR_QUERY_STRING
					+ " LOWER(cast(r.status as text ))  like  LOWER(" + searchKey
					+ ")   or LOWER(uul.contact_company) like  LOWER(" + searchKey + ")";

			String singleChoiceSearchQueryPrefix = MDF_FORM_SUBMIT_SINGLE_CHOICE_SEARCH_QUERY_PREFIX + formId;

			String singleChoiceSearchQuerySuffix = " and ( LOWER(ssc.value) like LOWER(" + searchKey + ") "
					+ lowerCloumns;

			String multiChoiceSearchQueryStringPrefix = MDF_FORM_SUBMIT_MULTI_CHOICE_SEARCH_QUERY_PREFIX + formId;

			String multiChoiceQuerySuffix = " and  lc.id = mc.form_label_choice_id and ( LOWER(lc.label_choice_name) like LOWER("
					+ searchKey + ") " + lowerCloumns + ")";

			String singleChoiceSearchQueryString = singleChoiceSearchQueryPrefix + PARTNERSHIP_ID_EQUALS_QUERY_STRING
					+ singleChoiceSearchQuerySuffix;
			String multiChoiceSearchQueryString = multiChoiceSearchQueryStringPrefix
					+ PARTNERSHIP_ID_EQUALS_QUERY_STRING + multiChoiceQuerySuffix;

			String finalQueryString = singleChoiceSearchQueryString + UNION_ID + multiChoiceSearchQueryString;
			formSubmitQuery = session.createSQLQuery(finalQueryString);
			formSubmitQuery.setParameter(PARTNERSHIP_ID, pagination.getPartnershipId());
			return formSubmitQuery;
		} else {
			formSubmitQueryString = MDF_FORM_SUBMIT_QUERY_STRING + PARTNERSHIP_ID_EQUALS_QUERY_STRING
					+ ORDER_BY_FORM_SUBMIT_QUERY;
			formSubmitQuery = session.createSQLQuery(formSubmitQueryString);
			formSubmitQuery.setParameter(FORM_ID_PARAM, formId);
			formSubmitQuery.setParameter(PARTNERSHIP_ID, pagination.getPartnershipId());

		}
		return formSubmitQuery;
	}

	/********* XNFR-85 *************/
	private SQLQuery mdfFormAnalyticsPartnerTeamMemberGroupQuery(Pagination pagination, Session session, Integer formId,
			TeamMemberFilterDTO teamMemberFilterDTO) {
		boolean applyTeamMemberFilter = teamMemberFilterDTO.isApplyTeamMemberFilter();
		SQLQuery formSubmitQuery;
		String formSubmitQueryString;
		if (StringUtils.hasText(pagination.getSearchKey())) {
			if (applyTeamMemberFilter) {
				formSubmitQuery = getMdfSearchQuery(pagination, session, formId, applyTeamMemberFilter);
				formSubmitQuery.setParameterList("partnershipIds",
						teamMemberFilterDTO.getPartnershipIdsOrPartnerCompanyIds());
			} else {
				formSubmitQuery = getMdfSearchQuery(pagination, session, formId, applyTeamMemberFilter);
			}
		} else {
			if (applyTeamMemberFilter) {
				formSubmitQueryString = MDF_FORM_SUBMIT_QUERY_STRING + PARTNERSHIP_ID_IN_QUERY_STRING
						+ ORDER_BY_FORM_SUBMIT_QUERY;
			} else if (teamMemberFilterDTO.isEmptyFilter()) {
				formSubmitQueryString = "select id from xt_form_submit where form_id = :formId" + ORDER_BY_ID_DESC;
				formId = 0;
			} else {
				formSubmitQueryString = MDF_FORM_SUBMIT_QUERY_STRING + " " + ORDER_BY_FORM_SUBMIT_QUERY;
			}
			formSubmitQuery = session.createSQLQuery(formSubmitQueryString);
			formSubmitQuery.setParameter(FORM_ID_PARAM, formId);
			if (applyTeamMemberFilter) {
				formSubmitQuery.setParameterList("partnershipIds",
						teamMemberFilterDTO.getPartnershipIdsOrPartnerCompanyIds());

			}
		}
		return formSubmitQuery;
	}

	private SQLQuery formAnalyticsSearchQuery(Pagination pagination, Session session, Integer formId) {
		SQLQuery formSubmitQuery;
		String masterLandingQuery = pagination.getMasterLandingPageId() == null
				? " and partner_master_landing_page_id isnull "
				: " and partner_master_landing_page_id = " + pagination.getMasterLandingPageId() + " ";
		if (pagination.isMasterLandingPageAnalytics() || pagination.isVendorMarketplacePageAnalytics()) {
			masterLandingQuery = masterLandingQuery + " and fs.landing_page_id = " + pagination.getVendorLandingPageId()
					+ " ";
		}

		String searchKey = "'%" + pagination.getSearchKey() + "%'";

		String multiChoiceSearchQueryString = MULTI_CHOICE_QUERY_STRING + FS_FORM_ID + formId
				+ " and  lc.id = mc.form_label_choice_id and LOWER(lc.label_choice_name) like LOWER(" + searchKey + ")";

		String searchQueryString = SEARCH_QUERY_STRING + FS_FORM_ID + formId + masterLandingQuery + LOWER_SEARCH_VALUE
				+ searchKey + UNION_ID + multiChoiceSearchQueryString;
		formSubmitQuery = session.createSQLQuery(searchQueryString + ORDER_BY_ID_DESC);
		return formSubmitQuery;
	}

	@SuppressWarnings("unchecked")
	private void getTotalRecordsAndDtoList(Pagination pagination, Map<String, Object> map, Session session,
			List<FormLabelDTO> columns, Set<Integer> redistributedCampaignIds, SQLQuery formSubmitQuery,
			Integer formColumnsLength) {
		List<FormDataDto> formDataDtos = new ArrayList<>();
		ScrollableResults scrollableResults = formSubmitQuery.scroll();
		scrollableResults.last();
		Integer totalRecords = scrollableResults.getRowNumber() + 1;
		map.put(XamplifyUtils.TOTAL_RECORDS, totalRecords);
		XamplifyUtils.setMaxResultsForExportToExcel(pagination, formSubmitQuery, totalRecords);
		List<Integer> submitIds = new ArrayList<>();
		List<MdfFormSubmittedDetailsDTO> mdfFormSubmittedDetailsDTOs = new ArrayList<>();
		if (pagination.isMdfForm()) {
			mdfFormSubmittedDetailsDTOs.addAll(formSubmitQuery
					.setResultTransformer(Transformers.aliasToBean(MdfFormSubmittedDetailsDTO.class)).list());
		} else {
			submitIds.addAll(formSubmitQuery.list());
		}
		List<Integer> submitIdsList = submitIds.stream().filter(item -> item != null).distinct()
				.collect(Collectors.toList());
		getDtoList(pagination, map, session, formDataDtos, columns, redistributedCampaignIds, submitIdsList,
				formColumnsLength, mdfFormSubmittedDetailsDTOs);
	}

	private void getDtoList(Pagination pagination, Map<String, Object> map, Session session,
			List<FormDataDto> formDataDtos, List<FormLabelDTO> columns, Set<Integer> redistributedCampaignIds,
			List<Integer> submitIds, Integer formColumnsLength,
			List<MdfFormSubmittedDetailsDTO> mdfFormSubmittedDetailsDTOs) {
		String singleChoiceQueryString = "select label.label_order,sc.value,label.label_type,label.price_type,label.column_order,label.id from xt_form_submit_single_choice sc,xt_form_label label where sc.form_submit_id = :submitId and sc.form_label_id = label.id";
		String multiChoiceQueryString = "select l.label_order,lc.label_choice_name,lt.label_type_id,l.column_order,l.id from xt_form_submit_multi_choice mc,xt_form_label_choice lc,xt_form_label l,xt_form_label_type lt  where form_submit_id = :submitId and mc.form_label_choice_id = lc.id and l.id = lc.form_label_id and lt.id = l.label_type";
		if (pagination.isMdfForm()) {
			for (MdfFormSubmittedDetailsDTO mdfFormSubmittedDetailsDTO : mdfFormSubmittedDetailsDTOs) {
				Integer formSubmitId = mdfFormSubmittedDetailsDTO.getId();
				setFormValues(pagination, session, formDataDtos, columns, redistributedCampaignIds, formColumnsLength,
						singleChoiceQueryString, multiChoiceQueryString, formSubmitId, mdfFormSubmittedDetailsDTO);
			}
		} else {
			for (Integer formSubmitId : submitIds) {
				setFormValues(pagination, session, formDataDtos, columns, redistributedCampaignIds, formColumnsLength,
						singleChoiceQueryString, multiChoiceQueryString, formSubmitId, null);
			}
		}

		map.put("submittedData", formDataDtos);
		map.put("isTotalLeadsData", pagination.isTotalLeads());
	}

	private void setFormValues(Pagination pagination, Session session, List<FormDataDto> formDataDtos,
			List<FormLabelDTO> columns, Set<Integer> redistributedCampaignIds, Integer formColumnsLength,
			String singleChoiceQueryString, String multiChoiceQueryString, Integer formSubmitId,
			MdfFormSubmittedDetailsDTO mdfFormSubmittedDetailsDTO) {
		FormDataDto formDataDto = new FormDataDto();
		List<FormValue> formValues = new ArrayList<>();
		setMdfRequestData(formValues, pagination, mdfFormSubmittedDetailsDTO);
		setSingleChoiceData(session, formSubmitId, formValues, singleChoiceQueryString, pagination);
		setMultiChoiceData(session, formSubmitId, formValues, multiChoiceQueryString, pagination);
		EventCheckInDTO eventCheckInDTO = setEventRelatedExtraData(pagination, session, columns,
				redistributedCampaignIds, formColumnsLength, formSubmitId, formValues);
		formDataDto.setValues(formValues);
		addMissingOrderIdsData(columns, formValues, pagination.isVendorJourney());
		Collections.sort(formValues,
				Comparator.comparing(FormValue::getOrder).thenComparing(FormValue::getColumnOrder));
		formDataDto.setExpanded(false);
		if (pagination.isCheckInLeads() && !pagination.isExportToExcel()) {
			formDataDto.setCheckedInForEvent(eventCheckInDTO.isCheckedIn());
		}
		formDataDto.setFormSubmittedId(formSubmitId);
		if (pagination.isVendorJourney()) {
			this.getPartnerDetailsForVendorJourneyForm(formSubmitId, formDataDto);
		}
		if (pagination.isExportToExcel() && pagination.isVendorJourney()) {
			downloadVendorJourneyCsvData(formValues, formDataDto);
		}
		formDataDtos.add(formDataDto);
	}

	private void downloadVendorJourneyCsvData(List<FormValue> formValues, FormDataDto formDataDto) {
		StringBuilder partnerDetails = new StringBuilder();
		if (XamplifyUtils.isValidString(formDataDto.getPartnerCompanyName())) {
			partnerDetails.append(formDataDto.getPartnerCompanyName());
			partnerDetails.append(",");
		}
		if (XamplifyUtils.isValidString(formDataDto.getPartnerEmailId())) {
			partnerDetails.append(formDataDto.getPartnerEmailId());
			partnerDetails.append(",");
		}
		partnerDetails.append(DateUtils.convertDateToStringForCampaign(formDataDto.getSubmittedOn()));

		FormValue formValue = new FormValue();
		formValue.setValue(String.valueOf(partnerDetails));
		formValues.add(0, formValue);
	}

	private void setMdfRequestData(List<FormValue> formValues, Pagination pagination,
			MdfFormSubmittedDetailsDTO mdfFormSubmittedDetailsDTO) {
		if (pagination.isMdfForm() && mdfFormSubmittedDetailsDTO != null) {
			FormValue requestDetails = new FormValue();
			requestDetails.setOrder(1);
			requestDetails.setColumnOrder(1);
			requestDetails.setId(1);
			List<String> values = new ArrayList<>();
			values.add(xamplifyUtil.getCompleteImagePath(mdfFormSubmittedDetailsDTO.getCompanyLogo()));
			String companyName = XamplifyUtils.replaceNullWithEmptyString(mdfFormSubmittedDetailsDTO.getCompanyName());
			String partnerCompanyName = mdfFormSubmittedDetailsDTO.getPartnerCompanyName();
			String companyNameAndPartnerCompanyName = companyName + " (" + partnerCompanyName + ")";
			values.add(companyNameAndPartnerCompanyName);
			values.add(mdfFormSubmittedDetailsDTO.getEmailId());
			values.add(DateUtils.convertToOnlyDate(mdfFormSubmittedDetailsDTO.getCreatedTime()));
			values.add(xamplifyUtil.getMdfRequestStatusInString(mdfFormSubmittedDetailsDTO.getRequestStatus()));
			values.add(String.valueOf(mdfFormSubmittedDetailsDTO.getRequestId()));
			values.add(mdfFormSubmittedDetailsDTO.getFullName());
			values.add(mdfFormSubmittedDetailsDTO.getPartnerStatus());
			requestDetails.setValues(values);
			formValues.add(requestDetails);
		}
	}

	private EventCheckInDTO setEventRelatedExtraData(Pagination pagination, Session session, List<FormLabelDTO> columns,
			Set<Integer> redistributedCampaignIds, Integer formColumnsLength, Integer formSubmitId,
			List<FormValue> formValues) {
		setEventCampaignData(session, formSubmitId, formValues, pagination, formColumnsLength,
				redistributedCampaignIds);
		EventCheckInDTO eventCheckInDTO = new EventCheckInDTO();
		if (pagination.isCheckInLeads()) {
			eventCheckInDTO = getCheckedInDetails(formSubmitId);
		}
		if (pagination.isCheckInLeads() && pagination.isExportToExcel()) {
			addDynamicFormValues(formValues, String.valueOf(eventCheckInDTO.isCheckedIn()), columns.size() - 1);
			addDynamicFormValues(formValues, String.valueOf(eventCheckInDTO.getCheckInTimeString()), columns.size());
		}
		return eventCheckInDTO;
	}

	/**
	 * @param pagination
	 * @param columns
	 */
	private void addAdditionalColumns(Pagination pagination, List<FormLabelDTO> columns) {
		if (pagination.isTotalLeads()) {
			addColumns("Message", columns.size() + 1, columns);
			addColumns("Additional Count", columns.size() + 1, columns);
			addColumns("RSVP Time", columns.size() + 1, columns);
			addColumns("RSVP Time UTC String", columns.size() + 1, columns);
			addColumns("RSVP Type", columns.size() + 1, columns);
			addColumns(analyticsEmail, columns.size() + 1, columns);
			addColumns(analyticsFirstName, columns.size() + 1, columns);
			addColumns(analyticsLastName, columns.size() + 1, columns);
			addColumns(analyticsCompanyName, columns.size() + 1, columns);
		} else if (pagination.isCheckInLeads() && pagination.isExportToExcel()) {
			addColumns(checkIn, columns.size() + 1, columns);
			addColumns(checkInTime, columns.size() + 1, columns);
		}
	}

	private void addDynamicFormValues(List<FormValue> formValues, String value, int order) {
		FormValue checkInFormValue = new FormValue();
		if (!"null".equals(value)) {
			checkInFormValue.setValue(value);
		} else {
			checkInFormValue.setValue("");
		}
		checkInFormValue.setOrder(order);
		formValues.add(checkInFormValue);
	}

	@SuppressWarnings("unchecked")
	private void setEventCampaignData(Session session, Integer formSubmitId, List<FormValue> formValues,
			Pagination pagination, int formColumnsLength, Set<Integer> redistributedCampaignIds) {
		if (pagination.isTotalLeads()) {
			String query = "select unnest(array[message, CAST(additional_count as text), CAST(rsvp_time as text),CAST(timezone('utc', rsvp_time)as text),CAST(rsvp_type as text)]) as value from xt_campaign_event_rsvp where form_submit_id="
					+ formSubmitId;
			List<String> list = session.createSQLQuery(query).list();
			int i = formColumnsLength + 1;
			int index = 0;
			if (list.isEmpty()) {
				for (int input = 0; input <= 4; input++) {
					setEmptyValues(formValues, i);
					i++;
					index++;
				}

			} else if (list.size() == 5) {
				for (String data : list) {
					if (index <= 4) {
						setRsvpFormValues(formValues, i, index, data);
						i++;
						index++;
					}
				}
			} else if (list.size() > 5) {
				List<String> updatedList = list.subList(list.size() - 5, list.size());
				for (String data : updatedList) {
					setRsvpFormValues(formValues, i, index, data);
					i++;
					index++;
				}
			}
			addExtraData(formSubmitId, formValues, pagination, redistributedCampaignIds, i);
		}

	}

	/**
	 * @param formValues
	 * @param i
	 */
	private void setEmptyValues(List<FormValue> formValues, int i) {
		FormValue formValue = new FormValue();
		formValue.setOrder(i);
		formValue.setValue("-");
		formValues.add(formValue);
	}

	private void addExtraData(Integer formSubmitId, List<FormValue> formValues, Pagination pagination,
			Set<Integer> redistributedCampaignIds, int i) {
		int lastIndex = i - 1;
		UserDTO userDto = getVendorOrPartnerData(formSubmitId, pagination.getCampaignId(), redistributedCampaignIds);
		addPartnerOrVendorDetails(formValues, userDto.getEmailId(), lastIndex + 1);
		addPartnerOrVendorDetails(formValues, XamplifyUtils.replaceNullWithEmptyString(userDto.getFirstName()),
				lastIndex + 2);
		addPartnerOrVendorDetails(formValues, XamplifyUtils.replaceNullWithEmptyString(userDto.getLastName()),
				lastIndex + 3);
		addPartnerOrVendorDetails(formValues, userDto.getCompanyName(), lastIndex + 4);
	}

	private void setRsvpFormValues(List<FormValue> formValues, int i, int index, String data) {
		FormValue formValue = new FormValue();
		formValue.setOrder(i);
		if (index == 2) {
			Date date = DateUtils.convertStringToDate24Format(data);
			String convertedDateInString = DateUtils.convertDateToStringForCampaign(date);
			formValue.setValue(XamplifyUtils.replaceNullWithEmptyString(convertedDateInString));
		} else {
			formValue.setValue(XamplifyUtils.replaceNullWithEmptyString(data));
		}
		formValues.add(formValue);
	}

	private void addPartnerOrVendorDetails(List<FormValue> formValues, String value, int order) {
		FormValue vendorOrPartnerDetails = new FormValue();
		vendorOrPartnerDetails.setOrder(order);
		vendorOrPartnerDetails.setValue(XamplifyUtils.replaceNullWithEmptyString(value));
		formValues.add(vendorOrPartnerDetails);
	}

	private void addColumns(String column, int order, List<FormLabelDTO> columns) {
		FormLabelDTO labelDto = new FormLabelDTO();
		labelDto.setLabelName(column);
		labelDto.setOrder(order);
		labelDto.setColumnOrder(order);
		labelDto.setId(order);
		labelDto.setRequired(false);
		columns.add(labelDto);
	}

	private String setTotalAttendeesQuery(Pagination pagination, String queryString) {
		if (!pagination.isTotalAttendees()) {
			queryString += USER_NULL_QUERY;
		}
		return queryString;
	}

	private SQLQuery formAnalyticsQuery(Pagination pagination, Session session, Integer formId) {
		SQLQuery formSubmitQuery;
		String formSubmitQueryString;
		if (StringUtils.hasText(pagination.getSearchKey())) {
			formSubmitQuery = formAnalyticsSearchQuery(pagination, session, formId);
		} else {
			String masterLandingPageQuery = XamplifyUtils.isValidInteger(pagination.getMasterLandingPageId())
					? " and partner_master_landing_page_id =" + pagination.getMasterLandingPageId()
					: " and partner_master_landing_page_id isnull ";
			String vendorJourneyQuery = (pagination.isVendorJourney() || pagination.isMasterLandingPageAnalytics()
					|| pagination.isVendorMarketplacePageAnalytics() || pagination.isPartnerJourneyPage())
							? masterLandingPageQuery
							: "";
			if (pagination.isMasterLandingPageAnalytics() || pagination.isVendorMarketplacePageAnalytics()
					|| pagination.isVendorMarketplacePage()) {
				vendorJourneyQuery = vendorJourneyQuery + " and landing_page_id = "
						+ pagination.getVendorLandingPageId() + " ";
			}
			formSubmitQueryString = "select id from xt_form_submit where form_id = :formId" + vendorJourneyQuery
					+ ORDER_BY_ID_DESC;
			formSubmitQuery = session.createSQLQuery(formSubmitQueryString);
			formSubmitQuery.setParameter(FORM_ID_PARAM, formId);

		}
		return formSubmitQuery;
	}

	private void addMissingOrderIdsData(List<FormLabelDTO> columns, List<FormValue> formValues,
			boolean isVendorJourney) {
		List<Integer> columnIds = columns.stream().map(FormLabelDTO::getId).collect(Collectors.toList());
		List<Integer> submittedValueIds = formValues.stream().map(FormValue::getId).collect(Collectors.toList());
		Map<Integer, FormLabelDTO> columnsMap = columns.stream().collect(Collectors.toMap(FormLabelDTO::getId, c -> c));
		columnIds.removeAll(submittedValueIds);
		if (isVendorJourney) {
			columnIds.remove(new Integer(1));
		}
		for (Integer missedId : columnIds) {
			FormValue missedFormValue = new FormValue();
			FormLabelDTO formLabelDto = columnsMap.get(missedId);
			missedFormValue.setOrder(formLabelDto.getOrder());
			missedFormValue.setColumnOrder(formLabelDto.getColumnOrder());
			missedFormValue.setId(formLabelDto.getId());
			missedFormValue.setValue("");
			formValues.add(missedFormValue);
		}
	}

	@SuppressWarnings("unchecked")
	private void setMultiChoiceData(Session session, Integer formSubmitId, List<FormValue> formValues,
			String multiChoiceQuery, Pagination pagination) {
		List<Object[]> multiChoices = session.createSQLQuery(multiChoiceQuery).setParameter(SUBMIT_ID, formSubmitId)
				.list();
		Map<Integer, Map<Integer, FormValue>> checkBoxFormValues = new HashMap<>();
		Map<Integer, FormValue> colCheckBoxFormValues = new HashMap<>();
		for (Object[] multiChoice : multiChoices) {
			int order = (int) multiChoice[0];
			if (pagination.isMdfForm()) {
				order = order + 1;
			}
			FormValue formValue = new FormValue();
			Integer labelType = (Integer) multiChoice[2];
			String choiceValue = XamplifyUtils.replaceNullWithEmptyString((String) multiChoice[1]);
			Integer colOrder = (Integer) multiChoice[3];
			Integer id = (Integer) multiChoice[4];

			if (!labelType.equals(5) && !labelType.equals(20)) {
				formValue.setOrder(order);
				formValue.setValue(XamplifyUtils.replaceNullWithEmptyString(choiceValue));
				formValue.setColumnOrder((Integer) multiChoice[3]);
				formValue.setId((Integer) multiChoice[4]);
				formValues.add(formValue);
			} else {
				Map<Integer, FormValue> colValues = checkBoxFormValues.getOrDefault(order, new HashMap<>());
				FormValue submittedData = colValues.get(colOrder);

				if (submittedData != null) {
					// Update existing FormValue for the same colOrder
					String updatedValue = StringUtils.hasText(submittedData.getValue())
							? submittedData.getValue() + "," + choiceValue
							: choiceValue;
					submittedData.setValue(XamplifyUtils.replaceNullWithEmptyString(updatedValue));

				} else {
					FormValue newValue = new FormValue();
					newValue.setOrder(order);
					newValue.setValue(choiceValue);
					newValue.setColumnOrder(colOrder);
					newValue.setId(id);
					colValues.put(colOrder, newValue); // Add to inner map by colOrder
					checkBoxFormValues.put(order, colValues); // Update outer map by order
					colCheckBoxFormValues.put(colOrder, newValue);
				}
			}
		}
		for (Map<Integer, FormValue> colMap : checkBoxFormValues.values()) {
			for (FormValue checkBoxValue : colMap.values()) {
				if (checkBoxValue.getOrder() > 0) {
					formValues.add(checkBoxValue);
				}
			}
		}

	}

	@SuppressWarnings("unchecked")
	private void setSingleChoiceData(Session session, Integer formSubmitId, List<FormValue> formValues,
			String singleChoiceQuery, Pagination pagination) {
		List<Object[]> singleChoices = session.createSQLQuery(singleChoiceQuery).setParameter(SUBMIT_ID, formSubmitId)
				.list();
		for (Object[] singleChoice : singleChoices) {
			FormValue formValue = new FormValue();
			int order = (int) singleChoice[0];
			if (pagination.isMdfForm()) {
				order = order + 1;
			}
			formValue.setOrder(order);// Remove this line and uncomment below if
			String value = (String) singleChoice[1];
			Integer labelType = (Integer) singleChoice[2];
			String priceType = (String) singleChoice[3];
			boolean downloadable = labelType.equals(11) && value != null;
			boolean hasPriceValue = labelType.equals(12) && value != null;
			formValue.setDownloadable(downloadable);
			if (formValue.isDownloadable()) {
				addDownloadLink(formValue, value);
			} else if (hasPriceValue) {
				addPriceSymbolPrefix(formValue, value, priceType);
			} else {
				formValue.setValue(XamplifyUtils.replaceNullWithEmptyString(value));
			}
			formValue.setColumnOrder((Integer) singleChoice[4]);
			formValue.setId((Integer) singleChoice[5]);
			formValues.add(formValue);
		}
	}

	private void addDownloadLink(FormValue formValue, String value) {
		formValue.setValue(XamplifyUtils.getOrignialFileNameWithExtension(value));
		formValue.setDownloadLink(value);
	}

	private void addPriceSymbolPrefix(FormValue formValue, String value, String priceType) {
		if ("rupee".equalsIgnoreCase(priceType)) {
			formValue.setValue("₹" + " " + XamplifyUtils.replaceNullWithEmptyString(value));
		} else if ("Dollar".equalsIgnoreCase(priceType)) {
			formValue.setValue("$" + " " + XamplifyUtils.replaceNullWithEmptyString(value));
		} else if ("Yen".equalsIgnoreCase(priceType)) {
			formValue.setValue("¥" + " " + XamplifyUtils.replaceNullWithEmptyString(value));
		} else if ("Pound".equalsIgnoreCase(priceType)) {
			formValue.setValue("£" + " " + XamplifyUtils.replaceNullWithEmptyString(value));
		} else if ("Euro".equalsIgnoreCase(priceType)) {
			formValue.setValue("€" + " " + XamplifyUtils.replaceNullWithEmptyString(value));
		} else {
			formValue.setValue(XamplifyUtils.replaceNullWithEmptyString(value));
		}
	}

	@Override
	public void save(FormSubmit formSubmit) {
		Session session = sessionFactory.getCurrentSession();
		session.save(formSubmit);

	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> list(List<Criteria> criterias, Pagination pagination, Integer formId) {
		Session session = sessionFactory.getCurrentSession();
		org.hibernate.Criteria criteria = session.createCriteria(FormSubmit.class, "formSubmit");
		criteria.createAlias("formSubmit.singleChoices", "singleChoice", JoinType.LEFT_OUTER_JOIN)
				.setFetchMode("formSubmit.singleChoices", FetchMode.SELECT);
		criteria.createAlias("formSubmit.multiChoices", "multiChoice", JoinType.LEFT_OUTER_JOIN)
				.setFetchMode("formSubmit.multiChoices", FetchMode.SELECT);
		criteria.createAlias("multiChoice.formLabelChoice", "labelChoice");
		Criterion formIdCriteria = Restrictions.eq("formSubmit.form.id", formId);
		criteria.add(formIdCriteria);
		List<Criterion> criterions = generateCriteria(criterias);
		for (Criterion criterion : criterions) {
			criteria.add(criterion);
		}

		if (StringUtils.hasText(pagination.getSearchKey())) {
			Criterion singleChoiceName = Restrictions.like("singleChoice.value", "%" + pagination.getSearchKey() + "%")
					.ignoreCase();
			Criterion mulitiChoiceName = Restrictions
					.like("labelChoice.labelChoiceName", "%" + pagination.getSearchKey() + "%").ignoreCase();
			criteria.add(Restrictions.or(singleChoiceName, mulitiChoiceName));
		}

		ScrollableResults scrollableResults = criteria.scroll();
		scrollableResults.last();
		Integer totalRecords = scrollableResults.getRowNumber() + 1;
		scrollableResults.close();

		Optional<Pagination> paginationObj = Optional.ofNullable(pagination);

		Optional<Integer> maxResultsObj = Optional.ofNullable(paginationObj.get().getMaxResults());
		Optional<Integer> pageIndexObj = Optional.ofNullable(paginationObj.get().getPageIndex());
		Optional<String> sortcolumnObj = Optional.ofNullable(paginationObj.get().getSortcolumn());
		if (maxResultsObj.isPresent() && pageIndexObj.isPresent()) {
			criteria.setFirstResult((pageIndexObj.get() * maxResultsObj.get()) - maxResultsObj.get());
			criteria.setMaxResults(maxResultsObj.get());
		}

		if (sortcolumnObj.isPresent()) {
			if (SORTINGORDER.ASC == SORTINGORDER.valueOf(paginationObj.get().getSortingOrder())) {
				criteria.addOrder(Order.asc(sortcolumnObj.get()));
			} else if (SORTINGORDER.DESC == SORTINGORDER.valueOf(paginationObj.get().getSortingOrder())) {
				criteria.addOrder(Order.desc(sortcolumnObj.get()));
			}

		} else {
			criteria.addOrder(Order.desc("formSubmit.id"));
		}

		List<FormSubmit> formSubmits = criteria.list();
		Map<String, Object> resultMap = new HashMap<>();
		resultMap.put("totalRecords", totalRecords);
		resultMap.put("formSubmitData", formSubmits);
		return resultMap;
	}

	@Override
	public FormSubmit getFormSubmitById(Integer id) {
		Session session = sessionFactory.getCurrentSession();
		org.hibernate.Criteria criteria = session.createCriteria(FormSubmit.class);
		criteria.add(Restrictions.eq("id", id));
		return (FormSubmit) criteria.uniqueResult();
	}

	@SuppressWarnings("unchecked")
	@Override
	public UserDTO getVendorOrPartnerData(Integer formSubmitId, Integer campaignId,
			Set<Integer> redistributedCampaignIds) {
		UserDTO userDto = new UserDTO();
		Session session = sessionFactory.getCurrentSession();
		String campaignIdAndCustomerIdQueryString = "select xc.customer_id from xt_form_campaign_submit xfcs,xt_campaign xc where xc.campaign_id = xfcs.campaign_id and xfcs.id = (select form_campaign_submit_id from xt_form_submit where id="
				+ formSubmitId + ")";
		Integer customerId = (Integer) session.createSQLQuery(campaignIdAndCustomerIdQueryString).uniqueResult();
		Integer partnerCount = ((BigInteger) session.createSQLQuery(
				"select count(*) from xt_campaign_user_userlist xcuu where campaign_id in (:campaignIds) and user_id = :userId")
				.setParameter(USER_ID, customerId).setParameterList(CAMPAIGN_IDS, redistributedCampaignIds)
				.uniqueResult()).intValue();
		if (partnerCount > 0) {
			List<Object[]> list = session.createSQLQuery(
					"select xup.email_id,xcuu.firstname,xcuu.lastname,xcuu.companyname from xt_campaign_user_userlist xcuu,xt_user_profile xup where xup.user_id = xcuu.user_id and campaign_id in (:campaignIds) and xcuu.user_id = :userId and xup.user_id = :userId")
					.setParameter(USER_ID, customerId).setParameterList(CAMPAIGN_IDS, redistributedCampaignIds).list();
			setUserDto(userDto, list);
		} else {
			List<Object[]> userList = session.createSQLQuery(
					"select xup.email_id,xup.firstname,xup.lastname,xcp.company_name from xt_user_profile xup,xt_company_profile xcp where xup.company_id = xcp.company_id and xup.user_id  = :userId")
					.setParameter(USER_ID, customerId).list();
			setUserDto(userDto, userList);
		}
		return userDto;

	}

	private void setUserDto(UserDTO userDto, List<Object[]> userList) {
		for (Object[] partner : userList) {
			userDto.setEmailId((String) partner[0]);
			userDto.setFirstName((String) partner[1]);
			userDto.setLastName((String) partner[2]);
			userDto.setCompanyName((String) partner[3]);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public EventCheckInDTO getCheckedInDetails(Integer formSumbitId) {
		Session session = sessionFactory.getCurrentSession();
		SQLQuery maxIdQuery = session.createSQLQuery(
				"select max(id)  from  xt_event_check_in where form_submit_fk_id = :formSubmitId group by form_submit_fk_id");
		maxIdQuery.setParameter("formSubmitId", formSumbitId);
		int maxId = maxIdQuery.uniqueResult() != null ? ((Integer) maxIdQuery.uniqueResult()).intValue() : 0;
		if (maxId > 0) {
			EventCheckInDTO eventCheckInDTO = new EventCheckInDTO();
			SQLQuery checkInQuery = session
					.createSQLQuery("select id,is_checked_in,checked_in_time from  xt_event_check_in where id=:id");
			checkInQuery.setParameter("id", maxId);
			List<Object[]> checkInList = checkInQuery.list();
			for (Object[] checkInData : checkInList) {
				Integer id = (Integer) checkInData[0];
				boolean isCheckedIn = (boolean) checkInData[1];
				Date checkedInTime = (Date) checkInData[2];
				eventCheckInDTO.setId(id);
				eventCheckInDTO.setCheckedIn(isCheckedIn);
				eventCheckInDTO.setCheckInTimeString(DateUtils.convertDateToStringForCampaign(checkedInTime));
			}
			return eventCheckInDTO;
		} else {
			return new EventCheckInDTO();
		}

	}

	@SuppressWarnings("unchecked")
	@Override
	public Integer getFormSubmitIdByTrackId(Integer userId, Integer learningTrackId, Integer quizId) {
		Session session = sessionFactory.getCurrentSession();
		String hql = "select distinct id from FormSubmit  where user.id=:userId and learningTrack.id=:learningTrackId and form.id=:quizId";
		Query query = session.createQuery(hql);
		query.setParameter(USER_ID, userId);
		query.setParameter("learningTrackId", learningTrackId);
		query.setParameter("quizId", quizId);
		List<Integer> list = query.list();
		Integer formSubmitId = 0;
		if (list != null && !list.isEmpty()) {
			formSubmitId = list.get(0);
		}
		return formSubmitId;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> getSubmissionDetails(Pagination pagination) {
		Session session = sessionFactory.getCurrentSession();
		org.hibernate.Criteria criteria = session.createCriteria(GeoLocationAnalytics.class, "G");
		criteria.createAlias("G.formSubmit", "FS", JoinType.LEFT_OUTER_JOIN);
		criteria.createAlias("FS.form", "F", JoinType.LEFT_OUTER_JOIN);
		criteria.add(Restrictions.eq("F.id", pagination.getFormId()));
		if (pagination.getCampaignId() != null && pagination.getCampaignId() > 0) {
			criteria.createAlias("FS.formCampaignSubmit", "FCS", JoinType.LEFT_OUTER_JOIN);
			criteria.createAlias("FCS.campaign", "FCSC", JoinType.LEFT_OUTER_JOIN);
			Disjunction disjunction = Restrictions.disjunction();
			disjunction.add(Restrictions.eq("FCSC.id", pagination.getCampaignId()));
			disjunction.add(Restrictions.eq("FCSC.parentCampaignId", pagination.getCampaignId()));
			criteria.add(disjunction);
			if (pagination.getPartnerCompanyId() != null && pagination.getPartnerCompanyId() > 0) {
				criteria.createAlias("FCSC.user", "FCSCU", JoinType.LEFT_OUTER_JOIN);
				criteria.add(Restrictions.eq("FCSCU.companyProfile.id", pagination.getPartnerCompanyId()));
			}
		}

		if (pagination.getSearchKey() != null && !pagination.getSearchKey().isEmpty()) {
			Disjunction disjunction = Restrictions.disjunction();
			disjunction.add(Restrictions.ilike("deviceType", pagination.getSearchKey(), MatchMode.ANYWHERE));
			disjunction.add(Restrictions.ilike("os", pagination.getSearchKey(), MatchMode.ANYWHERE));
			disjunction.add(Restrictions.ilike("city", pagination.getSearchKey(), MatchMode.ANYWHERE));
			disjunction.add(Restrictions.ilike("state", pagination.getSearchKey(), MatchMode.ANYWHERE));
			disjunction.add(Restrictions.ilike("zip", pagination.getSearchKey(), MatchMode.ANYWHERE));
			disjunction.add(Restrictions.ilike("country", pagination.getSearchKey(), MatchMode.ANYWHERE));
			disjunction.add(Restrictions.ilike("ipAddress", pagination.getSearchKey(), MatchMode.ANYWHERE));
			if (pagination.getCampaignId() != null && pagination.getCampaignId() > 0) {
				criteria.createAlias("FCS.user", "FCSU", JoinType.LEFT_OUTER_JOIN);
				disjunction.add(Restrictions.ilike("FCSU.firstName", pagination.getSearchKey(), MatchMode.ANYWHERE));
				disjunction.add(Restrictions.ilike("FCSU.lastName", pagination.getSearchKey(), MatchMode.ANYWHERE));
				disjunction.add(Restrictions.ilike("FCSU.emailId", pagination.getSearchKey(), MatchMode.ANYWHERE));
			}

			criteria.add(disjunction);
		}
		criteria.addOrder(Order.desc("id"));

		ScrollableResults scrollableResults = criteria.scroll();
		scrollableResults.last();
		Integer totalRecords = scrollableResults.getRowNumber() + 1;

		criteria.setFirstResult((pagination.getPageIndex() - 1) * pagination.getMaxResults());
		criteria.setMaxResults(pagination.getMaxResults());

		List<GeoLocationAnalytics> geoList = criteria.list();
		Map<String, Object> resultMap = new HashMap<>();
		resultMap.put("totalRecords", totalRecords);
		resultMap.put("data", geoList);
		return resultMap;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<FormSubmit> getSubmissions(Integer formId, Integer campaignId, Integer partnerCompanyId) {
		Session session = sessionFactory.getCurrentSession();
		org.hibernate.Criteria criteria = session.createCriteria(FormSubmit.class, "FS");
		criteria.createAlias("FS.form", "F", JoinType.LEFT_OUTER_JOIN);
		criteria.add(Restrictions.eq("F.id", formId));
		if (campaignId != null && campaignId > 0) {
			criteria.createAlias("FS.formCampaignSubmit", "FCS", JoinType.LEFT_OUTER_JOIN);
			criteria.createAlias("FCS.campaign", "FCSC", JoinType.LEFT_OUTER_JOIN);
			Disjunction disjunction = Restrictions.disjunction();
			disjunction.add(Restrictions.eq("FCSC.id", campaignId));
			disjunction.add(Restrictions.eq("FCSC.parentCampaignId", campaignId));
			criteria.add(disjunction);
			if (partnerCompanyId != null && partnerCompanyId > 0) {
				criteria.createAlias("FCSC.user", "FCSCU", JoinType.LEFT_OUTER_JOIN);
				criteria.add(Restrictions.eq("FCSCU.companyProfile.id", partnerCompanyId));
			}
		}
		criteria.addOrder(Order.desc("id"));
		return criteria.list();
	}

	@Override
	public Map<String, Object> findMdfFormSubmittedDetails(List<Criteria> criterias, FindLevel[] levels,
			Pagination pagination) {
		Map<String, Object> map = new HashMap<>();
		Session session = sessionFactory.getCurrentSession();
		Integer formId = pagination.getCompanyId();
		List<FormLabelDTO> columns = new ArrayList<>();
		addColumns("Request Details", 1, columns);
		columns.addAll(formDao.listFormLabelsById(formId, false, true, columns.size()));
		Integer formColumnsLength = columns.size();
		addAdditionalColumns(pagination, columns);
		map.put("columns", columns);
		map.put("formName", pagination.getFormName());
		SQLQuery formSubmitQuery = null;
		formSubmitQuery = filterDetails(pagination, session, formId);
		getTotalRecordsAndDtoList(pagination, map, session, columns, new HashSet<>(), formSubmitQuery,
				formColumnsLength);
		return map;
	}

	private SQLQuery filterDetails(Pagination pagination, Session session, Integer formId) {
		SQLQuery formSubmitQuery;
		if (pagination.isPartnerView()) {
			formSubmitQuery = mdfFormAnalyticsQuery(pagination, session, formId);
		} else if (pagination.isPartnerTeamMemberGroupFilter()) {
			TeamMemberFilterDTO teamMemberFilterDTO = utilDao.applyFilterConditions(pagination.getUserId(),
					pagination.isPartnerTeamMemberGroupFilter(), true);
			formSubmitQuery = mdfFormAnalyticsPartnerTeamMemberGroupQuery(pagination, session, formId,
					teamMemberFilterDTO);

		} else {
			String formSubmitQueryString;
			if (StringUtils.hasText(pagination.getSearchKey())) {
				formSubmitQuery = getMdfSearchQuery(pagination, session, formId, false);
			} else {
				formSubmitQueryString = MDF_FORM_SUBMIT_QUERY_STRING + " " + ORDER_BY_FORM_SUBMIT_QUERY;
				formSubmitQuery = session.createSQLQuery(formSubmitQueryString);
				formSubmitQuery.setParameter(FORM_ID_PARAM, formId);
			}
		}
		return formSubmitQuery;
	}

	private SQLQuery getMdfSearchQuery(Pagination pagination, Session session, Integer formId,
			boolean applyTeamMemberFilter) {
		SQLQuery formSubmitQuery;
		String searchKey = "'%" + pagination.getSearchKey() + "%'";
		String lowerCloumns = " or " + " LOWER(u.email_id) like  LOWER(" + searchKey + ") or "
				+ " LOWER(c.company_name) like  LOWER(" + searchKey + ") or  " + " LOWER(uul.firstname) like  LOWER("
				+ searchKey + ") or  " + " LOWER(uul.lastname) like  LOWER(" + searchKey + ") or  "
				+ " LOWER(cast(r.status as text ))  like  LOWER(" + searchKey
				+ ")   or LOWER(uul.contact_company) like  LOWER(" + searchKey + ")";

		String singleChoiceSearchQueryPrefix = MDF_FORM_SUBMIT_SINGLE_CHOICE_SEARCH_QUERY_PREFIX + formId;

		String singleChoiceSearchQuerySuffix = " and ( LOWER(ssc.value) like LOWER(" + searchKey + ") " + lowerCloumns;

		String multiChoiceSearchQueryStringPrefix = MDF_FORM_SUBMIT_MULTI_CHOICE_SEARCH_QUERY_PREFIX + formId;

		String multiChoiceQuerySuffix = " and  lc.id = mc.form_label_choice_id and ( LOWER(lc.label_choice_name) like LOWER("
				+ searchKey + ") " + lowerCloumns + ")";

		String multiChoiceSearchQueryString = "";

		String singleChoiceSearchQueryString = "";

		if (applyTeamMemberFilter) {
			singleChoiceSearchQueryString = singleChoiceSearchQueryPrefix + PARTNERSHIP_ID_IN_QUERY_STRING
					+ singleChoiceSearchQuerySuffix;
			multiChoiceSearchQueryString = multiChoiceSearchQueryStringPrefix + PARTNERSHIP_ID_IN_QUERY_STRING
					+ multiChoiceQuerySuffix;
		} else {
			singleChoiceSearchQueryString = singleChoiceSearchQueryPrefix + singleChoiceSearchQuerySuffix;
			multiChoiceSearchQueryString = multiChoiceSearchQueryStringPrefix + multiChoiceQuerySuffix;
		}
		String finalQueryString = singleChoiceSearchQueryString + UNION_ID + multiChoiceSearchQueryString;
		formSubmitQuery = session.createSQLQuery(finalQueryString);
		return formSubmitQuery;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Object[]> getFormSubmitSingleChoiceData(Integer formSubmitId) {
		Session session = sessionFactory.getCurrentSession();
		String singleChoiceQueryString = "select sc.form_label_id,sc.value from xt_form_submit_single_choice sc where sc.form_submit_id = :submitId";
		return session.createSQLQuery(singleChoiceQueryString).setParameter(SUBMIT_ID, formSubmitId).list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<FormSubmitMultiChoice> getFormSubmitMultiChoiceData(Integer formSubmitId) {
		Session session = sessionFactory.getCurrentSession();
		org.hibernate.Criteria criteria = session.createCriteria(FormSubmitMultiChoice.class);
		criteria.add(Restrictions.eq("formSubmit.id", formSubmitId));
		return criteria.list();
	}

	public FormSubmit getFormSubmitByTrackId(Integer userId, Integer learningTrackId, Integer quizId) {
		Session session = sessionFactory.getCurrentSession();
		org.hibernate.Criteria criteria = session.createCriteria(FormSubmit.class);
		criteria.add(Restrictions.eq("user.id", userId));
		criteria.add(Restrictions.eq("learningTrack.id", learningTrackId));
		criteria.add(Restrictions.eq("form.id", quizId));
		return (FormSubmit) criteria.uniqueResult();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> findMasterLandingPageDetailsByFormId(Integer formId, Pagination pagination) {
		Map<String, Object> map = new HashMap<>();
		Session session = sessionFactory.getCurrentSession();
		String queryString = "";
		if (pagination.isVendorJourney()) {
			queryString = "select distinct  xfs.partner_master_landing_page_id as \"partnerMasterLandingPageId \",  "
					+ " case when xfs.partner_master_landing_page_id notnull then  xlp.name else '' end as \"masterLandingPageName\", "
					+ " case when xfs.partner_master_landing_page_id notnull then  xcp.company_name else '' end  as \"partnerCompanyName\", "
					+ " case when xfs.partner_master_landing_page_id notnull then  xup.email_id else '' end as \"partnerMailId\"  "
					+ " from xt_form_submit xfs left join xt_landing_page xlp on xlp.id = xfs.partner_master_landing_page_id  "
					+ " left join xt_user_profile xup on xup.user_id = xlp.created_user_id  "
					+ " left join xt_company_profile xcp on xup.company_id = xcp.company_id  "
					+ " where xfs.form_id  = :formId  order by xfs.partner_master_landing_page_id ";
		} else if (pagination.isPartnerJourneyPage()) {
			queryString = "select distinct  xfs.partner_master_landing_page_id as \"vendorMarketplacePageId \", "
					+ " case when xfs.partner_master_landing_page_id notnull then  xlp.name else '' end as \"marketplacePageName\",  "
					+ " case when xfs.partner_master_landing_page_id notnull then  xcp.company_name else '' end  as \"vendoeCompanyName\", "
					+ " case when xfs.partner_master_landing_page_id notnull then  xup.email_id else '' end as \"vendorMailId\","
					+ " case when xfs.partner_master_landing_page_id notnull then  xcp.company_id else null end  as \"vendoeCompanyId\" "
					+ " from xt_form_submit xfs left join xt_landing_page xlp on xlp.id = xfs.partner_master_landing_page_id   "
					+ " left join xt_user_profile xup on xup.user_id = xlp.created_user_id   "
					+ " left join xt_company_profile xcp on xup.company_id = xcp.company_id  "
					+ " where xfs.form_id  = :formId  order by xfs.partner_master_landing_page_id ";
		}
		SQLQuery query = session.createSQLQuery(queryString);
		query.setParameter("formId", formId);
		ScrollableResults scrollableResults = query.scroll();
		scrollableResults.last();
		Integer totalRecords = scrollableResults.getRowNumber() + 1;
		query.setFirstResult((pagination.getPageIndex() - 1) * pagination.getMaxResults());
		query.setMaxResults(pagination.getMaxResults());
		List<Object[]> list = query.list();
		List<FormSubmitDTO> formSubmitDTOs = new ArrayList<>();

		map.put(XamplifyConstants.TOTAL_RECORDS, totalRecords);
		if (list != null && !list.isEmpty()) {
			for (Object[] row : list) {
				FormSubmitDTO formSubmitDTO = new FormSubmitDTO();
				formSubmitDTO.setPartnerMasterLandingPageId((Integer) row[0]);
				formSubmitDTO.setMasterLandingPageName((String) row[1]);
				formSubmitDTO.setPartnerCompanyName((String) row[2]);
				formSubmitDTO.setPartnerMailId((String) row[3]);
				if (pagination.isPartnerJourneyPage()) {
					formSubmitDTO.setPartnerCompanyId((Integer) row[4]);
				}
				formSubmitDTOs.add(formSubmitDTO);
			}
		}
		map.put("formName", pagination.getFormName());
		map.put("submittedData", formSubmitDTOs);
		return map;
	}

	private void getPartnerDetailsForVendorJourneyForm(Integer formSubmitId, FormDataDto formDataDto) {
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();

		String sql = " select xfs.id as \"formSubmittedId\",  xcp.company_name as \"partnerCompanyName\", xup.email_id as \"partnerEmailId\", "
				+ " xfs.user_id as \"partnerId\" , xfs.submitted_on as \"submittedOn\"  from xt_form_submit xfs "
				+ " left join xt_user_profile xup on xup.user_id = xfs.user_id "
				+ " left join xt_company_profile xcp on xcp.company_id =xup.company_id where xfs.id =:formSubmitId";
		hibernateSQLQueryResultRequestDTO.setQueryString(sql);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO("formSubmitId", formSubmitId));

		hibernateSQLQueryResultRequestDTO.setClassInstance(FormDataDto.class);
		FormDataDto dto = (FormDataDto) hibernateSQLQueryResultUtilDao.getDto(hibernateSQLQueryResultRequestDTO,
				FormDataDto.class);
		if (dto != null) {
			formDataDto.setPartnerCompanyName(dto.getPartnerCompanyName());
			formDataDto.setPartnerEmailId(dto.getPartnerEmailId());
			formDataDto.setPartnerId(dto.getPartnerId());
			formDataDto.setSubmittedOn(dto.getSubmittedOn());
		}

	}

	public List<FormDataDto> getVendorDetailsRhoughMasterLandingPageSubmissions(Integer masterLandingPageId) {
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();

		String sql = " select distinct xfs.landing_page_id as  \"vendorLandingPageId\", xlp.name  as \"VendorLandingPageName\", "
				+ " xcp.company_name as \"vendorCompanyName\", xup.email_id as \"vendorMailId\" "
				+ " from xt_form_submit xfs left join xt_landing_page xlp on xlp.id = xfs.landing_page_id  "
				+ " left join xt_user_profile xup on xup.user_id = xlp.created_user_id  "
				+ " left join xt_company_profile xcp on xup.company_id = xcp.company_id "
				+ " where xfs.partner_master_landing_page_id  = :masterLandingPageId  order by xfs.landing_page_id ";
		hibernateSQLQueryResultRequestDTO.setQueryString(sql);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO("masterLandingPageId", masterLandingPageId));

		hibernateSQLQueryResultRequestDTO.setClassInstance(FormDataDto.class);
		return (List<FormDataDto>) hibernateSQLQueryResultUtilDao.returnDTOList(hibernateSQLQueryResultRequestDTO);

	}

	public Map<String, Object> getVendorDetailsRhoughMasterLandingPageSubmissions(Pagination pagination,
			Integer masterLandingPageId) {
		Map<String, Object> map = new HashMap<>();
		Session session = sessionFactory.getCurrentSession();

		String queryString = " select distinct xfs.landing_page_id as  \"vendorLandingPageId\", xlp.name  as \"VendorLandingPageName\", "
				+ " xcp.company_name as \"vendorCompanyName\", xup.email_id as \"vendorMailId\", "
				+ " xf.alias as \"formAlias\", xcp.company_id as \"vendorCompanyId\" "
				+ " from xt_form_submit xfs left join xt_landing_page xlp on xlp.id = xfs.landing_page_id  "
				+ " left join xt_user_profile xup on xup.user_id = xlp.created_user_id  "
				+ " left join xt_company_profile xcp on xup.company_id = xcp.company_id "
				+ " left join xt_form xf on xf.id = xfs.form_id "
				+ " where xfs.partner_master_landing_page_id  = :masterLandingPageId  order by xfs.landing_page_id ";
		SQLQuery query = session.createSQLQuery(queryString);
		query.setParameter("masterLandingPageId", masterLandingPageId);
		ScrollableResults scrollableResults = query.scroll();
		scrollableResults.last();
		Integer totalRecords = scrollableResults.getRowNumber() + 1;
		query.setFirstResult((pagination.getPageIndex() - 1) * pagination.getMaxResults());
		query.setMaxResults(pagination.getMaxResults());
		List<Object[]> list = query.list();
		List<FormSubmitDTO> formSubmitDTOs = new ArrayList<>();

		map.put(XamplifyConstants.TOTAL_RECORDS, totalRecords);
		if (list != null && !list.isEmpty()) {
			for (Object[] row : list) {
				FormSubmitDTO formSubmitDTO = new FormSubmitDTO();
				formSubmitDTO.setVendorLandingPageId((Integer) row[0]);
				formSubmitDTO.setVendorLandingPageName((String) row[1]);
				formSubmitDTO.setVendorCompanyName((String) row[2]);
				formSubmitDTO.setVendorMailId((String) row[3]);
				formSubmitDTO.setVendorFormAlias((String) row[4]);
				formSubmitDTO.setVendorCompanyId((Integer) row[5]);
				formSubmitDTOs.add(formSubmitDTO);
			}
		}
		map.put("submittedData", formSubmitDTOs);
		return map;

	}

	private SQLQuery formAnalyticsForVendorJourneyPageQuery(Pagination pagination, Session session, Integer formId) {
		SQLQuery formSubmitQuery;
		String formSubmitSingleChoiceQueryString = "";
		String formSubmitMultiChoiceQueryString = "";
		String formSubmitSingleChoiceWithoutCompanyQueryString = "";
		String formSubmitMultiChoiceWithoutCompanyQueryString = "";
		String searchFormSubmitSingleChoiceQuery = "";
		String searchFormSubmitMultiChoiceQuery = "";
		String searchFormSubmitSingleChoiceWithoutCompanyQuery = "";
		String searchFormSubmitMultiChoiceWithoutCompanyQuery = "";
		String finalQuery;
		String seachString = pagination.getSearchKey();
		String masterLandingPageQuery = XamplifyUtils.isValidInteger(pagination.getMasterLandingPageId())
				? " and partner_master_landing_page_id =" + pagination.getMasterLandingPageId()
				: " and partner_master_landing_page_id isnull ";
		formSubmitSingleChoiceQueryString = " select distinct fs.id from xt_form_submit fs\r\n"
				+ " join xt_form_submit_single_choice ssc on fs.id = ssc.form_submit_id \r\n"
				+ " join xt_company_profile xcp on xcp.company_id = fs.partner_company_id \r\n"
				+ " join xt_user_profile xup on xup.user_id  = fs.user_id \r\n"
				+ " where fs.id=ssc.form_submit_id  and fs.form_id = :formId  " + masterLandingPageQuery;
		formSubmitMultiChoiceQueryString = " union\r\n" + "  select distinct fs.id from xt_form_submit fs \r\n"
				+ " join xt_form_submit_multi_choice mc on fs.id = mc.form_submit_id \r\n"
				+ " join xt_form_label_choice lc on lc.id = mc.form_label_choice_id \r\n"
				+ " join xt_company_profile xcp on xcp.company_id = fs.partner_company_id \r\n"
				+ " join xt_user_profile xup on xup.user_id  = fs.user_id "
				+ " where  fs.form_id = :formId and  lc.id = mc.form_label_choice_id  " + masterLandingPageQuery;
		formSubmitSingleChoiceWithoutCompanyQueryString = " union SELECT DISTINCT fs.id " + " FROM xt_form_submit fs "
				+ " JOIN xt_form_submit_single_choice ssc ON fs.id = ssc.form_submit_id "
				+ " WHERE fs.form_id = :formId  " + masterLandingPageQuery;
		formSubmitMultiChoiceWithoutCompanyQueryString = " union\r\n"
				+ "  select distinct fs.id from xt_form_submit fs \r\n"
				+ " join xt_form_submit_multi_choice mc on fs.id = mc.form_submit_id \r\n"
				+ " join xt_form_label_choice lc on lc.id = mc.form_label_choice_id \r\n"
				+ " where  fs.form_id = :formId and  lc.id = mc.form_label_choice_id  " + masterLandingPageQuery;
		if (StringUtils.hasText(seachString)) {
			searchFormSubmitSingleChoiceQuery = " and (LOWER(ssc.value) like LOWER('%" + seachString
					+ "%') or LOWER(xcp.company_name) like LOWER('%" + seachString
					+ "%') or LOWER(xup.email_id) like LOWER('%" + seachString + "%')) ";
			searchFormSubmitSingleChoiceWithoutCompanyQuery = " and (LOWER(ssc.value) like LOWER('%" + seachString
					+ "%') and  (fs.partner_company_id IS NULL AND fs.user_id IS NULL) )";
			searchFormSubmitMultiChoiceQuery = " and (LOWER(lc.label_choice_name) like LOWER('%" + seachString
					+ "%') or LOWER(xcp.company_name) like LOWER('%" + seachString
					+ "%') or LOWER(xup.email_id) like LOWER('%" + seachString + "%')) ";
			searchFormSubmitMultiChoiceWithoutCompanyQuery = " and (LOWER(lc.label_choice_name) like LOWER('%"
					+ seachString + "%') and  (fs.partner_company_id IS NULL AND fs.user_id IS NULL) )";
		}
		finalQuery = formSubmitSingleChoiceQueryString + searchFormSubmitSingleChoiceQuery
				+ formSubmitSingleChoiceWithoutCompanyQueryString + searchFormSubmitSingleChoiceWithoutCompanyQuery
				+ formSubmitMultiChoiceQueryString + searchFormSubmitMultiChoiceQuery
				+ formSubmitMultiChoiceWithoutCompanyQueryString + searchFormSubmitMultiChoiceWithoutCompanyQuery;
		formSubmitQuery = session.createSQLQuery(finalQuery);
		formSubmitQuery.setParameter(FORM_ID_PARAM, formId);
		return formSubmitQuery;
	}

	private boolean isEmailFieldExists(Integer formId) {
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		String sql = "select count(*)>0 from xt_form xf join xt_form_label xfl on xfl.form_id = xf.id "
				+ " where xf.id = :formId and  xfl.label_type = 3 ";
		hibernateSQLQueryResultRequestDTO.setQueryString(sql);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO(FORM_ID_PARAM, formId));
		return hibernateSQLQueryResultUtilDao.returnBoolean(hibernateSQLQueryResultRequestDTO);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<FormSubmitFieldsValuesDTO> getFormSubmitValueDetailsById(Integer formSubmitId) {
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();

		String sql = " select distinct on (xfssc.id, xfl.label_order, xfl.column_order) xfssc.form_label_id as \"labelId\", xfssc.value as \"value\", \r\n"
				+ " xfl.label_name as \"labelName\", xfl.label_type as \"labelTypeId\" , xfl.is_required as \"required\" \r\n"
				+ " from xt_form_submit_single_choice xfssc join xt_form_label xfl on xfl.id = xfssc.form_label_id  \r\n"
				+ " where xfssc.form_submit_id = :formSubmitId order by xfl.label_order, xfl.column_order ";
		hibernateSQLQueryResultRequestDTO.setQueryString(sql);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO("formSubmitId", formSubmitId));

		hibernateSQLQueryResultRequestDTO.setClassInstance(FormSubmitFieldsValuesDTO.class);
		return (List<FormSubmitFieldsValuesDTO>) hibernateSQLQueryResultUtilDao
				.returnDTOList(hibernateSQLQueryResultRequestDTO);

	}
}
