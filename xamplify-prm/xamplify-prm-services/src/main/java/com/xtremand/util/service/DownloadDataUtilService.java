/**
 * 
 */
package com.xtremand.util.service;

import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Method;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.xtremand.aws.CopiedFileDetails;
import com.xtremand.campaign.bom.DownloadDataInfo.DownloadItem;
import com.xtremand.campaign.exception.XamplifyDataAccessException;
import com.xtremand.common.bom.Criteria;
import com.xtremand.common.bom.Criteria.OPERATION_NAME;
import com.xtremand.common.bom.FindLevel;
import com.xtremand.common.bom.Pagination;
import com.xtremand.custom.field.dto.LeadFieldLabel;
import com.xtremand.deal.dto.DealDto;
import com.xtremand.deal.service.DealService;
import com.xtremand.flexi.fields.dao.FlexiFieldDao;
import com.xtremand.flexi.fields.dto.FlexiFieldRequestDTO;
import com.xtremand.flexi.fields.dto.FlexiFieldResponseDTO;
import com.xtremand.form.bom.FormDefaultFieldTypeEnum;
import com.xtremand.form.bom.FormTypeEnum;
import com.xtremand.form.dao.FormDao;
import com.xtremand.form.dto.FormLabelDTO;
import com.xtremand.form.dto.SelectedFieldsDTO;
import com.xtremand.formbeans.UserDTO;
import com.xtremand.formbeans.UserListDTO;
import com.xtremand.integration.bom.Integration;
import com.xtremand.integration.dao.IntegrationDao;
import com.xtremand.lead.dto.LeadDto;
import com.xtremand.lead.service.LeadService;
import com.xtremand.salesforce.bom.SfCustomFieldsData;
import com.xtremand.team.dao.TeamDao;
import com.xtremand.user.bom.Role;
import com.xtremand.user.bom.User;
import com.xtremand.user.service.UserService;
import com.xtremand.userlist.exception.UserListException;
import com.xtremand.util.DateUtils;
import com.xtremand.util.FileUtil;
import com.xtremand.util.XamplifyUtils;
import com.xtremand.util.dao.UtilDao;
import com.xtremand.vanity.url.dto.VanityUrlDetailsDTO;

import au.com.bytecode.opencsv.CSVWriter;

@Service
@Transactional
public class DownloadDataUtilService {

	private static final Logger logger = LoggerFactory.getLogger(DownloadDataUtilService.class);

	@Value("${media_base_path}")
	String mediaBasePath;

	@Value("${specialCharacters}")
	String regex;

	@Value("#{'${default.lead.report.excel.headers}'.split(',')}")
	private List<String> defaultLeadReportHeaders;

	@Value("#{'${default.lead.report.excel.label.ids}'.split(',')}")
	private List<String> defaultLeadReportFormLabelIds;

	@Value("#{'${salesforce.lead.default.cf.names}'.split(',')}")
	private List<String> salesforceLeadDefaultFields;

	@Value("#{'${default.deal.report.excel.headers}'.split(',')}")
	private List<String> defaultDealReportHeaders;

	@Autowired
	private FileUtil fileUtil;

	@Autowired
	private TeamDao teamDao;

	@Autowired
	private UserService userService;

	@Autowired
	@Lazy
	private LeadService leadService;

	@Autowired
	@Lazy
	private DealService dealService;

	@Autowired
	private FlexiFieldDao flexiFieldDao;

	@Autowired
	private IntegrationDao integrationDao;

	@Autowired
	private FormDao formDao;

        @Autowired
        private UtilService utilService;

        @Autowired
        private UtilDao utilDao;

	public static final String USERID = "userId";

	public static final String EXCLUDED = "excluded";

	public static final String INVALID = "invalid";

	public static final String UNSUBSCRIBED = "unsubscribed";

	public static final String UNDELIVERABLE = "undeliverable";

	public static final String ACTIVE = "active";

	public static final String NONACTIVE = "non-active";

	public static final String REGISTERED = "registered";

	public static final String UNREGISTERED = "unregistered";

	public static final String MASTER_PARTNER_LIST = "master_partner_list/";

	public static final String PARTNER_LIST = "partner_list/";

	public static final String SHARE_LEADS_LIST = "share_leads_list";

	public static final String SALESFORCE = "salesforce";

	public static final String HUBSPOT = "hubspot";

	public static final String CONNECTWISE = "connectwise";

	public static final String HALOPSA = "halopsa";

	public static final String ZOHO = "zoho";

	public static final String PIPEDRIVE = "pipedrive";

	public static final String XAMPLIFY = "xamplify";

	public static final String LOOKUP = "lookup";

	public static final String CURRENCY = "currency";

	public static final String NAME_DISPLAY_NAME = "Name_DisplayName";

	public static final String NAME_LABEL_ID = "Name_LabelId";

	public static final String CLOSE_DATE_DISPLAY_NAME = "CloseDate_DisplayName";

	public static final String CLOSE_DATE_LABEL_ID = "CloseDate_LabelId";

	public static final String AMOUNT_DISPLAY_NAME = "Amount_DisplayName";

	public static final String AMOUNT_LABEL_ID = "Amount_LabelId";

	public static final String LABEL_ID = "LabelId";

	public static final String DISPLAY_NAME = "DisplayName";

	public static final String APPROVE = "APPROVE";

	public CopiedFileDetails downloadLeadsData(Integer userId, Pagination pagination, DownloadItem dataType,
			String folderName) {
		CopiedFileDetails copiedFileDetails = new CopiedFileDetails();
		List<LeadDto> leads = null;
		if (XamplifyUtils.isValidInteger(userId)) {
			leads = leadService.getLeadsForCSV(pagination);
			/** XNFR-839 ****/
			Map<String, String> fieldHeaderMapping = new HashMap<>();
			if (XamplifyUtils.isNotEmptyList(pagination.getSelectedExcelFormFields())) {
				fieldHeaderMapping = pagination.getSelectedExcelFormFields().stream()
						.collect(Collectors.toMap(SelectedFieldsDTO::getDisplayName, SelectedFieldsDTO::getLabelId,
								(oldValue, newValue) -> oldValue, // Handle duplicates
								LinkedHashMap::new // Preserve order
						));
			} else {
				fieldHeaderMapping = leadService.getFieldHeaderMapping(pagination.getUserType(), userId,
						pagination.isVanityUrlFilter(), utilDao.getPrmCompanyProfileName());
			}
			/** XNFR-839 ****/
			String fileName = pagination.getFilterKey() == null ? "total-leads-data"
					: pagination.getFilterKey() + "-leads-data";
			copiedFileDetails = downloadCsvToLocalFolderForLeads(userId, fileName, folderName, fieldHeaderMapping,
					leads, pagination);
		}
		return copiedFileDetails;
	}

	public CopiedFileDetails downloadDealsData(Integer userId, Pagination pagination, DownloadItem dataType,
			String folderName) {
		CopiedFileDetails copiedFileDetails = new CopiedFileDetails();
		List<DealDto> deals = null;
		if (XamplifyUtils.isValidInteger(userId)) {
			deals = dealService.getDealsForCSV(pagination);
			/** XNFR-840 ****/
			List<SelectedFieldsDTO> excelHeaderFields = pagination.getSelectedExcelFormFields();
			Map<String, String> fieldHeaderMapping = new HashMap<>();
			if (XamplifyUtils.isNotEmptyList(excelHeaderFields)) {
				fieldHeaderMapping = excelHeaderFields.stream()
						.collect(Collectors.toMap(SelectedFieldsDTO::getDisplayName, SelectedFieldsDTO::getLabelId,
								(oldValue, newValue) -> oldValue, LinkedHashMap::new));
			} else {
				fieldHeaderMapping = dealService.getFieldHeaderMapping(pagination.getUserType(), userId);
			}
			/** XNFR-840 ****/
			String fileName = pagination.getFilterKey() == null ? "total-deals-data"
					: pagination.getFilterKey() + "-deals-data";
			copiedFileDetails = downloadCsvToLocalFolderForDeals(userId, fileName, folderName, fieldHeaderMapping,
					deals, pagination);
		}
		return copiedFileDetails;
	}

	private CopiedFileDetails setFilePath(String folderName, String fileName, Integer userId) {
		CopiedFileDetails copiedFileDetails = new CopiedFileDetails();
		String currentDate = DateUtils.getDateFormatForUploadedFiles();
		String folderPath = mediaBasePath + folderName + userId + "/" + currentDate;
		File csvDir = new File(folderPath);
		if (!csvDir.exists()) {
			csvDir.mkdirs();
		}
		String updatedFileName = fileUtil.updateOriginalFileName(fileName + ".csv").replaceAll(regex, "");
		String completeFilePath = folderPath + "/" + updatedFileName;
		copiedFileDetails.setCompleteName(completeFilePath);
		copiedFileDetails.setUpdatedFileName(updatedFileName);
		return copiedFileDetails;
	}

	private CopiedFileDetails downloadCsvToLocalFolderForDeals(Integer userId, String fileName, String folderName,
			Map<String, String> fieldHeaderMapping, List<?> data, Pagination pagination) {

		CopiedFileDetails copiedFileDetails = new CopiedFileDetails();
		boolean[] hasActiveIntegrationWrapper = { false };
		String[] activeCRMIntegrationForHeader = new String[1];

		try {
			copiedFileDetails = setFilePath(folderName, fileName, userId);
			File newFile = new File(copiedFileDetails.getCompleteName());
			try (CSVWriter writer = new CSVWriter(new FileWriter(newFile))) {

				List<String> headers = new ArrayList<>(fieldHeaderMapping.keySet());
				boolean isCheckSelectedExcelFields = XamplifyUtils
						.isNotEmptyList(pagination.getSelectedExcelFormFields());
				Map<String, String> labelIdDisplayNameMap = checkVanityAndActiveIntegrationAndFrameCustomFormLabelHeaders(
						pagination, hasActiveIntegrationWrapper, activeCRMIntegrationForHeader, headers);
				String activeCRMIntegrationValue = activeCRMIntegrationForHeader[0];

				List<String[]> csvData = new ArrayList<>();
				csvData.add(headers.toArray(new String[0]));

				for (Object dataItem : data) {
					String[] row = new String[headers.size()];
					int columnIndex = 0;

					for (String header : headers) {
						try {
							/** XNFR-840 ***/
							boolean isHeaderContains = true;
							if (isCheckSelectedExcelFields) {
								isHeaderContains = defaultDealReportHeaders.contains(header);
							}
							/*** XNFR-840 **/
							if (fieldHeaderMapping.containsKey(header) && isHeaderContains) {
								Method method = dataItem.getClass().getMethod(fieldHeaderMapping.get(header));
								String value = StringEscapeUtils.escapeCsv((String) method.invoke(dataItem));
								row[columnIndex] = value != null ? value.replaceAll("\"", "") : "";
							} else if (hasActiveIntegrationWrapper[0]) {
								frameValuesForCustomFormLabelHeadersForDeals(dataItem, row, columnIndex, header,
										activeCRMIntegrationValue, labelIdDisplayNameMap, isCheckSelectedExcelFields);
							}

						} catch (Exception e) {
							System.err.println("Error processing header: " + header + " - " + e.getMessage());
						}
						columnIndex++;
					}
					csvData.add(row);
				}
				writer.writeAll(csvData);
			}
		} catch (Exception e) {
			throw new XamplifyDataAccessException(e);
		}
		return copiedFileDetails;
	}

	private Map<String, String> checkVanityAndActiveIntegrationAndFrameCustomFormLabelHeaders(Pagination pagination,
			boolean[] hasActiveIntegrationWrapper, String[] activeCRMIntegrationForHeader, List<String> headers) {
		List<SelectedFieldsDTO> excelHeaderFields = pagination.getSelectedExcelFormFields(); // XNFR-840
		Map<String, String> labelIdDisplayNameMap = new HashMap<>();

		VanityUrlDetailsDTO vanityUrlDetailsDTO = utilService.getVanityUrlFilteredData(pagination.getUserId(),
				pagination.isVanityUrlFilter(), utilDao.getPrmCompanyProfileName());

		if (vanityUrlDetailsDTO != null && vanityUrlDetailsDTO.isVanityUrlFilter()
				&& XamplifyUtils.isValidInteger(vanityUrlDetailsDTO.getVendorCompanyId())) {

			String activeCRMIntegration = integrationDao
					.getActiveIntegrationTypeByCompanyId(vanityUrlDetailsDTO.getVendorCompanyId());

			if (activeCRMIntegration == null) {
				activeCRMIntegration = XAMPLIFY;
				headers.remove("CRM Id");
			}

			if (XamplifyUtils.isValidString(activeCRMIntegration)) {
				hasActiveIntegrationWrapper[0] = true;
				activeCRMIntegrationForHeader[0] = activeCRMIntegration;
				labelIdDisplayNameMap = frameHeadersForCustomFormLabelsByActiveCRM(headers, vanityUrlDetailsDTO,
						activeCRMIntegration, excelHeaderFields);
			}
		}

		return labelIdDisplayNameMap;
	}

	private Map<String, String> frameHeadersForCustomFormLabelsByActiveCRM(List<String> headers,
			VanityUrlDetailsDTO vanityUrlDetailsDTO, String activeCRMIntegration,
			List<SelectedFieldsDTO> excelHeaderFields) {
		List<FormLabelDTO> formLabelDtos = new ArrayList<>();
		FormTypeEnum formType = getActiveCRMFormTypeByActiveIntegration(activeCRMIntegration);
		Map<String, String> labelIdDisplayNameMap = new HashMap<>();
		String dealNameKey = getDealNameByActiveCrm(activeCRMIntegration);
		String closeDateKey = getCloseDateByActiveCrm(activeCRMIntegration);
		String amountKey = getAmountByActiveCrm(activeCRMIntegration);

		if (formType != null) {
			Integer formId = formDao.getSfCustomFormIdByCompanyIdAndFormType(vanityUrlDetailsDTO.getVendorCompanyId(),
					formType);
			if (XamplifyUtils.isValidInteger(formId) && !formType.equals(FormTypeEnum.XAMPLIFY_DEAL_CUSTOM_FORM)) {
				formLabelDtos = formDao.getFormLabelDtoByFormIdForCSV(formId);
			} else if (XamplifyUtils.isValidInteger(formId)
					&& formType.equals(FormTypeEnum.XAMPLIFY_DEAL_CUSTOM_FORM)) {
				formLabelDtos = formDao.getFormLabelDtoByFormIdForCSVXamplifyIntegration(formId);
			}
			/***** XNFR-840 ****/
			if (XamplifyUtils.isNotEmptyList(excelHeaderFields)) {
				List<FormLabelDTO> formLabelList = convertSelectedFieldsToLableTypeFields(excelHeaderFields);
				Map<String, FormLabelDTO> dtoMap = formLabelDtos.stream().collect(Collectors
						.toMap(FormLabelDTO::getLabelId, label -> label, (existing, replacement) -> existing));

				formLabelDtos = formLabelList.stream().map(label -> dtoMap.getOrDefault(label.getLabelId(), label))
						.collect(Collectors.toList());
				formLabelDtos.removeIf(
						label -> formLabelList.stream().noneMatch(dto -> dto.getLabelId().equals(label.getLabelId())));
			}
			/***** XNFR-840 ****/
			if (formLabelDtos != null && !formLabelDtos.isEmpty()) {
				if (!XamplifyUtils.isNotEmptyList(excelHeaderFields)) {
					ensureHeader(headers, "Added On (PST)", 0);
				}

				for (FormLabelDTO formLabelDto : formLabelDtos) {
					String customFieldLabel = formLabelDto.getDisplayName();
					String labelId = formLabelDto.getLabelId();

					if (activeCRMIntegration.equals(HUBSPOT) && formLabelDto.getFormLabelDefaultFieldType() != null) {
						if (formLabelDto.getFormLabelDefaultFieldType()
								.equals(FormDefaultFieldTypeEnum.DEAL_NAME.getType())) {
							dealNameKey = labelId;
						}
						if (formLabelDto.getFormLabelDefaultFieldType()
								.equals(FormDefaultFieldTypeEnum.CLOSE_DATE.getType())) {
							closeDateKey = labelId;
						}
						if (formLabelDto.getFormLabelDefaultFieldType()
								.equals(FormDefaultFieldTypeEnum.AMOUNT.getType())) {
							amountKey = labelId;
						}
					}

					frameLabelIdDisplayNameMap(labelIdDisplayNameMap, dealNameKey, closeDateKey, amountKey,
							customFieldLabel, labelId);

					if (!headers.contains(customFieldLabel) && !XamplifyUtils.isNotEmptyList(excelHeaderFields)) {
						headers.remove("Deal Title");
						headers.add(headers.indexOf("Added On (PST)") + 1, customFieldLabel);
					}
				}
			}
		}
		return labelIdDisplayNameMap;
	}

	private void frameLabelIdDisplayNameMap(Map<String, String> labelIdDisplayNameMap, String dealNameKey,
			String closeDateKey, String amountKey, String customFieldLabel, String labelId) {
		if (dealNameKey.equalsIgnoreCase(labelId)) {
			labelIdDisplayNameMap.put(NAME_LABEL_ID, labelId);
			labelIdDisplayNameMap.put(NAME_DISPLAY_NAME, customFieldLabel);
		}
		if (amountKey.equalsIgnoreCase(labelId)) {
			labelIdDisplayNameMap.put(AMOUNT_LABEL_ID, labelId);
			labelIdDisplayNameMap.put(AMOUNT_DISPLAY_NAME, customFieldLabel);
		}
		if (closeDateKey.equalsIgnoreCase(labelId)) {
			labelIdDisplayNameMap.put(CLOSE_DATE_LABEL_ID, labelId);
			labelIdDisplayNameMap.put(CLOSE_DATE_DISPLAY_NAME, customFieldLabel);
		}
	}

	private void ensureHeader(List<String> headers, String header, int position) {
		if (headers.contains(header)) {
			headers.remove(header);
		}
		headers.add(position, header);
	}

	private void frameValuesForCustomFormLabelHeadersForDeals(Object dataItem, String[] row, int columnIndex,
			String header, String activeCRMIntegration, Map<String, String> labelIdDisplayNameMap,
			boolean isCheckSelectedExcelFields) {

		String crmDealNameTitle = getDealNameByActiveCrm(activeCRMIntegration);
		String crmCloseDateTitle = getCloseDateByActiveCrm(activeCRMIntegration);
		String crmAmountTitle = getAmountByActiveCrm(activeCRMIntegration);

		if (dataItem instanceof DealDto) {
			DealDto deal = (DealDto) dataItem;

			if (activeCRMIntegration.equals(HUBSPOT)) {
				if (!crmDealNameTitle.equals(labelIdDisplayNameMap.get(NAME_LABEL_ID))
						&& header.equals(labelIdDisplayNameMap.get(NAME_DISPLAY_NAME))) {
					row[columnIndex] = StringEscapeUtils.escapeCsv(deal.getTitle() != null ? deal.getTitle() : "");
					return;
				}
				if (!crmCloseDateTitle.equals(labelIdDisplayNameMap.get(CLOSE_DATE_LABEL_ID))
						&& header.equals(labelIdDisplayNameMap.get(CLOSE_DATE_DISPLAY_NAME))) {
					row[columnIndex] = deal.getCloseDate() != null ? deal.getCloseDateString().replace("\"", "") : "";
					return;
				}
				if (!crmAmountTitle.equals(labelIdDisplayNameMap.get(AMOUNT_LABEL_ID))
						&& header.equals(labelIdDisplayNameMap.get(AMOUNT_DISPLAY_NAME))) {
					row[columnIndex] = deal.getAmount() != null
							? StringEscapeUtils.escapeCsv("$ " + deal.getAmount().toString())
							: "";
					return;
				}
			}

			if (header.equals(labelIdDisplayNameMap.get(NAME_DISPLAY_NAME))
					&& crmDealNameTitle.equals(labelIdDisplayNameMap.get(NAME_LABEL_ID))) {
				row[columnIndex] = StringEscapeUtils.escapeCsv(deal.getTitle() != null ? deal.getTitle() : "");
				return;
			}

			if (header.equals(labelIdDisplayNameMap.get(CLOSE_DATE_DISPLAY_NAME))
					&& crmCloseDateTitle.equals(labelIdDisplayNameMap.get(CLOSE_DATE_LABEL_ID))) {
				row[columnIndex] = deal.getCloseDate() != null ? deal.getCloseDateString().replace("\"", "") : "";
				return;
			}

			if (header.equals(labelIdDisplayNameMap.get(AMOUNT_DISPLAY_NAME))
					&& crmAmountTitle.equals(labelIdDisplayNameMap.get(AMOUNT_LABEL_ID))) {
				row[columnIndex] = deal.getAmount() != null
						? StringEscapeUtils.escapeCsv("$ " + deal.getAmount().toString())
						: "";
				return;
			}

			if (deal.getSfCustomFieldsData() != null) {
				for (SfCustomFieldsData sfcfData : deal.getSfCustomFieldsData()) {
					if (header.equals(sfcfData.getFormLabel().getDisplayName())) {
						if (LOOKUP.equals(sfcfData.getFormLabel().getLabelType().getLabelType())) {
							row[columnIndex] = StringEscapeUtils.escapeCsv(sfcfData.getSelectedChoiceValue());
						} else if (CURRENCY.equals(sfcfData.getFormLabel().getLabelType().getLabelType())) {
							row[columnIndex] = StringEscapeUtils.escapeCsv("$ " + sfcfData.getValue());
						} else {
							row[columnIndex] = StringEscapeUtils.escapeCsv(sfcfData.getValue());
						}
						return;
					}
				}
			}
		}
	}

	public String getAmountByActiveCrm(String activeCRMIntegration) {
		String amount = "";
		if (activeCRMIntegration != null) {
			switch (activeCRMIntegration) {
			case SALESFORCE:
				amount = "Amount";
				break;
			case HUBSPOT:
				amount = "amount";
				break;
			case CONNECTWISE:
				amount = "amount";
				break;
			case PIPEDRIVE:
				amount = "value";
				break;
			case HALOPSA:
				amount = "FOppValue";
				break;
			case ZOHO:
				amount = "Amount";
				break;
			case XAMPLIFY:
				amount = "Amount";
				break;
			default:
				amount = "";
				break;
			}
		}
		return amount;
	}

	public String getCloseDateByActiveCrm(String activeCRMIntegration) {
		String closeDate = "";
		if (activeCRMIntegration != null) {
			switch (activeCRMIntegration) {
			case SALESFORCE:
				closeDate = "CloseDate";
				break;
			case HUBSPOT:
				closeDate = "closedate";
				break;
			case CONNECTWISE:
				closeDate = "expectedCloseDate";
				break;
			case PIPEDRIVE:
				closeDate = "expected_close_date";
				break;
			case HALOPSA:
				closeDate = "FOppTargetDate";
				break;
			case ZOHO:
				closeDate = "Closing_Date";
				break;
			case XAMPLIFY:
				closeDate = "Close_Date";
				break;
			default:
				closeDate = "";
				break;
			}
		}
		return closeDate;
	}

	public String getDealNameByActiveCrm(String activeCRMIntegration) {
		String dealName = "";
		if (activeCRMIntegration != null) {
			switch (activeCRMIntegration) {
			case SALESFORCE:
				dealName = "Name";
				break;
			case HUBSPOT:
				dealName = "dealname";
				break;
			case CONNECTWISE:
				dealName = "name";
				break;
			case PIPEDRIVE:
				dealName = "title";
				break;
			case HALOPSA:
				dealName = "symptom";
				break;
			case ZOHO:
				dealName = "Account_Name";
				break;
			case XAMPLIFY:
				dealName = "Deal_Name";
				break;
			default:
				dealName = "";
				break;
			}
		}
		return dealName;
	}

	public FormTypeEnum getActiveCRMFormTypeByActiveIntegration(String activeCRMIntegration) {
		FormTypeEnum formType = null;
		if (activeCRMIntegration != null) {
			switch (activeCRMIntegration) {
			case XAMPLIFY:
				formType = FormTypeEnum.XAMPLIFY_DEAL_CUSTOM_FORM;
				break;
			default:
				formType = null;
				break;
			}
		}
		return formType;
	}

	private CopiedFileDetails downloadCsvToLocalFolderForLeads(Integer userId, String fileName, String folderName,
			Map<String, String> fieldHeaderMapping, List<?> data, Pagination pagination) {
		CopiedFileDetails copiedFileDetails = new CopiedFileDetails();
		try {
			copiedFileDetails = setFilePath(folderName, fileName, userId);
			File newFile = new File(copiedFileDetails.getCompleteName());
			FileWriter outputfile = new FileWriter(newFile);
			CSVWriter writer = new CSVWriter(outputfile);
			List<String[]> csvData = new ArrayList<>();
			List<String> headers = new ArrayList<>(fieldHeaderMapping.keySet());
			boolean[] hasSalesforceIntegration = { false };
			boolean isCheckSelectedExcelFields = XamplifyUtils.isNotEmptyList(pagination.getSelectedExcelFormFields());
			Map<String, Map<String, String>> labelIdDisplayNameMap = checkSalesforceIntegrationAndFrameCustomFormLabelHeaders(
					pagination, hasSalesforceIntegration, headers);

			csvData.add(headers.toArray(new String[0]));

			for (Object dataItem : data) {
				String[] row = new String[headers.size()];
				int columnIndex = 0;
				for (String header : headers) {
					if (fieldHeaderMapping.containsKey(header) && !isCheckSelectedExcelFields) {
						Method method = dataItem.getClass().getMethod(fieldHeaderMapping.get(header));
						String value = StringEscapeUtils.escapeCsv((String) method.invoke(dataItem));
						row[columnIndex] = value != null ? value.replaceAll("\"", "") : "";
					} else if (hasSalesforceIntegration[0]) {
						frameValuesForCustomFormLabelHeaders(dataItem, row, columnIndex, header, labelIdDisplayNameMap,
								isCheckSelectedExcelFields);
					}
					columnIndex++;
				}
				csvData.add(row);
			}

			writer.writeAll(csvData);
			writer.flush();
			writer.close();
		} catch (Exception e) {
			throw new XamplifyDataAccessException(e);
		}
		return copiedFileDetails;
	}

	private Map<String, Map<String, String>> checkSalesforceIntegrationAndFrameCustomFormLabelHeaders(
			Pagination pagination, boolean[] hasSalesforceIntegration, List<String> headers) {
		Map<String, Map<String, String>> labelIdDisplayNameMap = new HashMap<>();
		VanityUrlDetailsDTO vanityUrlDetailsDTO = utilService.getVanityUrlFilteredData(pagination.getUserId(),
				pagination.isVanityUrlFilter(), utilDao.getPrmCompanyProfileName());
		if (vanityUrlDetailsDTO != null && vanityUrlDetailsDTO.isVanityUrlFilter()
				&& XamplifyUtils.isValidInteger(vanityUrlDetailsDTO.getVendorCompanyId())) {
			Integration activeCRMIntegration = integrationDao
					.getActiveCRMIntegration(vanityUrlDetailsDTO.getVendorCompanyId());
			if (activeCRMIntegration == null) {
				hasSalesforceIntegration[0] = true;
				labelIdDisplayNameMap = frameHeadersForCustomFormLabels(headers, vanityUrlDetailsDTO, false,
						pagination);
			}
		}
		return labelIdDisplayNameMap;
	}

	private Map<String, Map<String, String>> frameHeadersForCustomFormLabels(List<String> headers,
			VanityUrlDetailsDTO vanityUrlDetailsDTO, boolean hasActiveCrmIntegration, Pagination pagination) {
		Integer formId = null;
		Map<String, Map<String, String>> labelIdDisplayNameMap = new HashMap<>();
		List<FormLabelDTO> formLabelDtos = new ArrayList<>();
		List<SelectedFieldsDTO> selectedExcelFields = pagination.getSelectedExcelFormFields();
		boolean isCheckSelectedExcelFields = XamplifyUtils.isNotEmptyList(selectedExcelFields);

		formId = formDao.getSfCustomFormIdByCompanyIdAndFormType(vanityUrlDetailsDTO.getVendorCompanyId(),
				FormTypeEnum.XAMPLIFY_LEAD_CUSTOM_FORM);
		if (!isCheckSelectedExcelFields) {
			headers.remove("CRM Id");
		}
		if (formId != null) {
			formLabelDtos = formDao.getFormLabelDtoByFormIdForCSVXamplifyIntegration(formId);
		}

		/***** XNFR-839 ****/
		if (isCheckSelectedExcelFields) {
			List<FormLabelDTO> formLabelList = convertSelectedFieldsToLableTypeFields(selectedExcelFields);
			Map<String, FormLabelDTO> dtoMap = formLabelDtos.stream().collect(
					Collectors.toMap(FormLabelDTO::getLabelId, label -> label, (existing, replacement) -> existing));

			formLabelDtos = formLabelList.stream().map(label -> dtoMap.getOrDefault(label.getLabelId(), label))
					.collect(Collectors.toList());
			formLabelDtos.removeIf(
					label -> formLabelList.stream().noneMatch(dto -> dto.getLabelId().equals(label.getLabelId())));
		}
		/***** XNFR-839 ****/
		if (formLabelDtos != null && !formLabelDtos.isEmpty()) {
			/***** XNFR-839 ****/
			boolean addedOnPresent = false;
			if (!isCheckSelectedExcelFields) {
				headers.removeAll(defaultLeadReportHeaders);
				addedOnPresent = headers.contains("Added On (PST)");
				if (addedOnPresent) {
					headers.remove("Added On (PST)");
					headers.add(0, "Added On (PST)");
				}
			}
			/***** XNFR-839 ****/
			for (FormLabelDTO formLabelDto : formLabelDtos) {
				String labelId = formLabelDto.getLabelId();
				String customFieldLabel = formLabelDto.getDisplayName();
				Map<String, String> labelDetails = new HashMap<>();

				if (XamplifyUtils.isValidString(formLabelDto.getFormLabelDefaultFieldType())
						&& FormDefaultFieldTypeEnum.XAMPLIFY_LEAD_REGISTERED_DATE.name()
								.equals(formLabelDto.getFormLabelDefaultFieldType())) {
					labelDetails.put(LABEL_ID, "xAmplify_Lead_Registered_Date_c");
				} else {
					labelDetails.put(LABEL_ID, labelId);
				}

				labelDetails.put(DISPLAY_NAME, customFieldLabel);
				if (!isCheckSelectedExcelFields) {
					if (defaultLeadReportFormLabelIds.contains(labelId)) {
						labelIdDisplayNameMap.put(labelId, labelDetails);
					}
				} else {
					labelIdDisplayNameMap.put(labelId, labelDetails);
				}
				/***** XNFR-839 ****/
				if (!headers.contains(customFieldLabel) && !isCheckSelectedExcelFields) {
					headers.add(headers.indexOf("Added On (PST)") + 1, customFieldLabel);
				} else if (!headers.contains(customFieldLabel)) {
					headers.add(customFieldLabel);
				}
				/***** XNFR-839 ****/
			}
			/***** XNFR-839 ****/
			if (!addedOnPresent && !isCheckSelectedExcelFields) {
				headers.add(0, "Added On (PST)");
			}

			/***** XNFR-839 ****/
		}
		return labelIdDisplayNameMap;
	}

	/**** XNFR-839 ******/
	private List<FormLabelDTO> convertSelectedFieldsToLableTypeFields(List<SelectedFieldsDTO> selectedExcelFields) {
		List<FormLabelDTO> formLabelDtos = new ArrayList<>();
		for (SelectedFieldsDTO dto : selectedExcelFields) {
			FormLabelDTO formLabelDTO = new FormLabelDTO();
			formLabelDTO.setDisplayName(dto.getDisplayName()); // Using setter
			formLabelDTO.setLabelId(dto.getLabelId()); // Using setter
			formLabelDtos.add(formLabelDTO);
		}
		return formLabelDtos;
	}

	/****** XNFR-839 *******/
	private void frameValuesForCustomFormLabelHeaders(Object dataItem, String[] row, int columnIndex, String header,
			Map<String, Map<String, String>> labelIdDisplayNameMap, boolean isExcelFieldsSelected) {
		String customFieldLabel = "";
		String storedLabelId = "";
		if (dataItem instanceof LeadDto) {
			LeadDto lead = (LeadDto) dataItem;
			for (Map.Entry<String, Map<String, String>> entry : labelIdDisplayNameMap.entrySet()) {
				Map<String, String> labelDetails = entry.getValue();

				customFieldLabel = labelDetails.get(DISPLAY_NAME);
				storedLabelId = labelDetails.get(LABEL_ID);
				if (isExcelFieldsSelected) {
					LeadFieldLabel fieldLabel = LeadFieldLabel.fromString(storedLabelId);
					boolean isLabelIdPresent = fieldLabel != null && fieldLabel.getLabelId().contains(storedLabelId);
					if (header.equals(customFieldLabel) && isLabelIdPresent) {
						String value = setFieldValues(lead, storedLabelId);
						/** XNFR-1022 **/
						String valueSting = StringEscapeUtils.escapeCsv(value);
						if (!XamplifyUtils.isValidString(valueSting)) {
							valueSting = "";
						}
						row[columnIndex] = valueSting.replace("\"", "");
						/** XNFR-1022 **/
						// row[columnIndex] = StringEscapeUtils.escapeCsv(value).replace("\"", "");
						// //XNFR-1022
						return;
					}
				} else {
					if (header.equals(customFieldLabel) && defaultLeadReportFormLabelIds.contains(storedLabelId)) {
						String value = setFieldValues(lead, storedLabelId);
						row[columnIndex] = StringEscapeUtils.escapeCsv(value);
						return;
					}
				}
			}
			if (lead.getSfCustomFieldsData() != null) {
				for (SfCustomFieldsData sfcfData : lead.getSfCustomFieldsData()) {
					if (header.equals(sfcfData.getFormLabel().getDisplayName())) {
						if ("lookup".equals(sfcfData.getFormLabel().getLabelType().getLabelType())) {
							row[columnIndex] = StringEscapeUtils.escapeCsv(sfcfData.getSelectedChoiceValue());
						} else if ("currency".equals(sfcfData.getFormLabel().getLabelType().getLabelType())) {
							row[columnIndex] = StringEscapeUtils.escapeCsv("$ " + sfcfData.getValue());
						} else {
							row[columnIndex] = StringEscapeUtils.escapeCsv(sfcfData.getValue());
						}
						break;
					}
				}
			}
		}
	}

	private String setFieldValues(LeadDto lead, String labelId) {
		LeadFieldLabel fieldLabel = LeadFieldLabel.fromString(labelId);
		String fieldValue = "";
		if (fieldLabel != null) {
			switch (fieldLabel) {
			case LAST_NAME:
			case LASTNAME:
				fieldValue = lead.getLastName();
				break;
			case FIRST_NAME:
			case FIRSTNAME:
				fieldValue = lead.getFirstName();
				break;
			case TITLE:
				fieldValue = lead.getTitle();
				break;
			case COMPANY:
				fieldValue = lead.getCompany();
				break;
			case STREET:
				fieldValue = lead.getStreet();
				break;
			case CITY:
				fieldValue = lead.getCity();
				break;
			case STATE:
				fieldValue = lead.getState();
				break;
			case POSTAL_CODE:
				fieldValue = lead.getPostalCode();
				break;
			case COUNTRY:
				fieldValue = lead.getCountry();
				break;
			case EMAIL:
				fieldValue = lead.getEmail();
				break;
			case WEBSITE:
				fieldValue = lead.getWebsite();
				break;
			case PHONE_NUMBER:
			case PHONE:
				fieldValue = lead.getPhone();
				if (!XamplifyUtils.isValidString(fieldValue)) {
					fieldValue = "";
				}
				break;
			case INDUSTRY:
				fieldValue = lead.getIndustry();
				if (!XamplifyUtils.isValidString(fieldValue)) {
					fieldValue = "";
				}
				break;
			case REGION:
				fieldValue = lead.getRegion();
				if (!XamplifyUtils.isValidString(fieldValue)) {
					fieldValue = "";
				}
				break;
			case LEAD_ID:
				fieldValue = lead.getReferenceId();
				if (!XamplifyUtils.isValidString(fieldValue)) {
					fieldValue = "";
				}
				break;
			case CRM_ID:
				fieldValue = lead.getCrmId();
				if (!XamplifyUtils.isValidString(fieldValue)) {
					fieldValue = "";
				}
				break;
			case CAMPAIGN_NAME:
				fieldValue = lead.getCampaignName();
				if (!XamplifyUtils.isValidString(fieldValue)) {
					fieldValue = "";
				}
				break;
			case ADDED_FOR_COMPANY_NAME:
				fieldValue = lead.getCreatedForCompanyName();
				if (!XamplifyUtils.isValidString(fieldValue)) {
					fieldValue = "";
				}
				break;
			case PARENT_CAMPAIGN_NAME:
				fieldValue = lead.getParentCampaignName();
				if (!XamplifyUtils.isValidString(fieldValue)) {
					fieldValue = "";
				}
				break;
			case ADDED_BY_COMPANY:
				fieldValue = lead.getCreatedByCompanyName();
				if (!XamplifyUtils.isValidString(fieldValue)) {
					fieldValue = "";
				}
				break;
			case ADDED_BY_NAME:
				fieldValue = lead.getCreatedByName();
				if (!XamplifyUtils.isValidString(fieldValue)) {
					fieldValue = "";
				}
				break;
			case ADDED_BY_EMAIL_ID:
				fieldValue = lead.getCreatedByEmail();
				if (!XamplifyUtils.isValidString(fieldValue)) {
					fieldValue = "";
				}
				break;
			case ADDED_ON_DATE:
				fieldValue = lead.getCreatedDateString();
				if (!XamplifyUtils.isValidString(fieldValue)) {
					fieldValue = "";
				}
				break;
			case CURRENT_STAGE_NAME:
				fieldValue = lead.getCurrentStageName();
				if (!XamplifyUtils.isValidString(fieldValue)) {
					fieldValue = "";
				}
				break;
			case XAMPLIFY_LEAD_REGISTERED_DATE:
				OffsetDateTime dateTime = OffsetDateTime.parse(lead.getCreatedTime());
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
				fieldValue = dateTime.toLocalDate().format(formatter);

				if (!XamplifyUtils.isValidString(fieldValue)) {
					fieldValue = "";
				}
				break;
			case PARTNER_TYPE:
				fieldValue = lead.getPartnerType();
				if (!XamplifyUtils.isValidString(fieldValue)) {
					fieldValue = "";
				}
				break;
			case ACCOUNT_SUBTYPE:
				fieldValue = lead.getAccountSubType();
				if (!XamplifyUtils.isValidString(fieldValue)) {
					fieldValue = "";
				}
				break;
			case ACCOUNT_OWNER:
				fieldValue = getLeadFieldValue(lead);
				break;
			default:
				break;
			}
		}
		return fieldValue;
	}

	/**** XNFR-1022 ****/
	private String getLeadFieldValue(LeadDto lead) {
		String createdByName = lead.getCreatedByName();
		String createdByEmailId = lead.getCreatedByEmail();
		if (lead.getCreatedByCompanyId().equals(lead.getCreatedForCompanyId())) {
			return XamplifyUtils.isValidString(createdByName) ? createdByName : createdByEmailId;
		} else {
			return teamDao.findTeamMemberFullNameOrEmaiIdByPartnerCompanyId(lead.getCreatedByCompanyId(),
					lead.getCreatedForCompanyId());
		}
	}

	/**** XNFR-1022 ****/
	public CopiedFileDetails editListCsv(Integer userId, List<UserDTO> userDtos, String folderName, String fileName,
			UserListDTO userListDTO) {
		String contactType = userListDTO.getContactType();
		String moduleName = userListDTO.getModuleName().trim().toLowerCase();

		CopiedFileDetails copiedFileDetails = setFilePath(folderName, fileName, userId);
		List<FlexiFieldResponseDTO> flexiFieldNames = XamplifyUtils.CONTACTS.equalsIgnoreCase(moduleName)
				? flexiFieldDao.findAll(userId)
				: null;

		if (!XamplifyUtils.isValidString(contactType)) {
			contactType = "";
		}
		try {
			File newFile = new File(copiedFileDetails.getCompleteName());
			FileWriter outputfile = new FileWriter(newFile);
			CSVWriter writer = new CSVWriter(outputfile);

			List<String[]> data = new ArrayList<>();
			data.add(getEditListCsvHeaders(contactType, flexiFieldNames, folderName, moduleName));

			for (UserDTO user : userDtos) {
				data.add(getEditListCsvValues(contactType, user, flexiFieldNames, folderName, moduleName));
			}

			writer.writeAll(data);
			writer.flush();
			writer.close();
		} catch (Exception e) {
			String debug = "error occured in partnerListCsv with userId : " + userId;
			logger.error(debug);
			throw new UserListException(e.getMessage());
		}

		return copiedFileDetails;
	}

	private String[] getEditListCsvHeaders(String contactType, List<FlexiFieldResponseDTO> flexiFieldNames,
			String folderName, String moduleName) {
		List<String> csvHeaders = new ArrayList<>();
		csvHeaders.addAll(XamplifyUtils.defaultContactCsvHeaderColumns());
		csvHeaders.add(XamplifyUtils.CONTACTS.equalsIgnoreCase(moduleName) ? "CONTACT STATUS" : "");
		switch (contactType) {
		case EXCLUDED:
			csvHeaders.add("EXCLUDED CATEGORY");
			break;
		case INVALID:
			csvHeaders.add("EMAIL CATEGORY");
			break;
		case UNSUBSCRIBED:
			csvHeaders.add("UNSUBSCRIBE REASON");
			break;
		default:
			break;
		}

		if (XamplifyUtils.isNotEmptyList(flexiFieldNames) && XamplifyUtils.CONTACTS.equalsIgnoreCase(moduleName)) {
			csvHeaders.addAll(XamplifyUtils.getFlexiFieldNames(flexiFieldNames));
		}

		if (MASTER_PARTNER_LIST.equalsIgnoreCase(folderName) || PARTNER_LIST.equalsIgnoreCase(folderName)) {
			return new String[] { "FIRSTNAME", "LASTNAME", "ACCOUNT NAME", "ACCOUNT OWNER", "ACCOUNT SUB TYPE",
					"COMPANY", "COMPANY DOMAIN", "JOBTITLE", "EMAILID", "WEBSITE", "VERTICAL", "REGION", "TERRITORY",
					"TYPE", "CATEGORY", "ADDRESS", "CITY", "STATE", "ZIP", "COUNTRY", "MOBILE NUMBER", "SIGNUP STATUS",
					"COMPANY PROFILE", "ONBOARDED ON" };
		} else {
			return csvHeaders.toArray(new String[0]);
		}
	}

	private String[] getEditListCsvValues(String contactType, UserDTO user, List<FlexiFieldResponseDTO> flexiFieldNames,
			String folderName, String moduleName) {
		List<String> csvData = new ArrayList<>();
		csvData.addAll(extractContactFieldValues(user));
		csvData.add(XamplifyUtils.CONTACTS.equalsIgnoreCase(moduleName) ? user.getContactStatus() : "");
		switch (contactType) {
		case EXCLUDED:
			csvData.add(user.getExcludedCatagory());
			break;
		case INVALID:
			csvData.add(user.getEmailCategory());
			break;
		case UNSUBSCRIBED:
			csvData.add(user.getUnsubscribedReason());
			break;
		default:
			break;
		}

		if (XamplifyUtils.isNotEmptyList(flexiFieldNames) && XamplifyUtils.CONTACTS.equalsIgnoreCase(moduleName)) {
			Map<String, String> flexiFieldValueMap = user.getFlexiFields().stream()
					.collect(Collectors.toMap(FlexiFieldRequestDTO::getFieldName, FlexiFieldRequestDTO::getFieldValue));

			flexiFieldNames.forEach(flexiFieldName -> {
				String fieldValue = flexiFieldValueMap.getOrDefault(flexiFieldName.getFieldName(), "");
				csvData.add(fieldValue);
			});
		}

		if (MASTER_PARTNER_LIST.equalsIgnoreCase(folderName) || PARTNER_LIST.equalsIgnoreCase(folderName)) {
			String signUpStatus = user.getUserStatus().equalsIgnoreCase(APPROVE) ? "Yes" : "No";
			String companyProfile = (user.getCompanyNameStatus() != null
					&& user.getCompanyNameStatus().equalsIgnoreCase(ACTIVE)) ? "Yes" : "No";
			String onboardedOn = XamplifyUtils.isValidString(user.getCreatedTime())
					? DateUtils.convertDateToString(DateUtils.convertStringToDate24Format(user.getCreatedTime()))
					: "-";
			return new String[] { user.getFirstName(), user.getLastName(), user.getAccountName(),
					user.getAccountOwner(), user.getAccountSubType(), user.getContactCompany(), user.getCompanyDomain(),
					user.getJobTitle(), user.getEmailId(), user.getWebsite(), user.getVertical(), user.getRegion(),
					user.getTerritory(), user.getPartnerType(), user.getCategory(), user.getAddress(), user.getCity(),
					user.getState(), user.getZipCode(), user.getCountry(), user.getMobileNumber(), signUpStatus,
					companyProfile, onboardedOn };
		} else {
			return csvData.toArray(new String[0]);
		}
	}

	public CopiedFileDetails userListCsv(Integer userId, List<UserDTO> userDtos, String folderName, String fileName,
			UserListDTO userListDTO) {
		String contactType = getContactType(userListDTO.getContactType());
		String moduleName = userListDTO.getModuleName().trim().toLowerCase();
		fileName = contactType + "_" + fileName;

		CopiedFileDetails copiedFileDetails = setFilePath(folderName, fileName, userId);
		List<FlexiFieldResponseDTO> flexiFieldNames = XamplifyUtils.CONTACTS.equalsIgnoreCase(moduleName)
				? flexiFieldDao.findAll(userId)
				: null;

		User loggedInUser = userService.loadUser(Arrays.asList(new Criteria(USERID, OPERATION_NAME.eq, userId)),
				new FindLevel[] { FindLevel.ROLES });

		try {
			File newFile = new File(copiedFileDetails.getCompleteName());
			FileWriter outputfile = new FileWriter(newFile);
			CSVWriter writer = new CSVWriter(outputfile);

			List<String[]> data = new ArrayList<>();
			data.add(getUserListCsvHeaders(contactType, flexiFieldNames, loggedInUser, folderName, moduleName));

			for (UserDTO user : userDtos) {
				data.add(
						getUserListCsvValues(contactType, user, flexiFieldNames, loggedInUser, folderName, moduleName));
			}

			writer.writeAll(data);
			writer.flush();
			writer.close();
		} catch (Exception e) {
			String debug = "error occured in userListCsv with userId : " + userId;
			logger.error(debug);
			throw new UserListException(e.getMessage());
		}

		return copiedFileDetails;
	}

	private String getContactType(String contactType) {
		switch (contactType) {
		case INVALID:
			return UNDELIVERABLE;
		case ACTIVE:
			return REGISTERED;
		case NONACTIVE:
			return UNREGISTERED;
		default:
			return contactType;
		}
	}

	private String[] getUserListCsvHeaders(String contactType, List<FlexiFieldResponseDTO> flexiFieldNames,
			User loggedInUser, String folderName, String moduleName) {
		List<String> csvHeaders = new ArrayList<>();
		csvHeaders.addAll("partners".equalsIgnoreCase(moduleName) ? findPartnerHeaders()
				: XamplifyUtils.defaultContactCsvHeaderColumns());
		csvHeaders.add(XamplifyUtils.CONTACTS.equalsIgnoreCase(moduleName) ? "CONTACT STATUS" : "");
		switch (contactType) {
		case EXCLUDED:
			csvHeaders.add("EXCLUDED CATEGORY");
			break;
		case UNDELIVERABLE:
			csvHeaders.add("EMAIL CATEGORY");
			break;
		case UNSUBSCRIBED:
			if (hasPrmRole(loggedInUser) || SHARE_LEADS_LIST.equalsIgnoreCase(folderName)) {
				csvHeaders.add("UNSUBSCRIBE REASON");
			} else {
				csvHeaders.add("TOTAL CAMPAIGNS");
				csvHeaders.add("ACTIVE CAMPAIGNS");
				csvHeaders.add("EMAIL OPENED");
				csvHeaders.add("CLICKED URLS");
				csvHeaders.add("UNSUBSCRIBE REASON");
			}
			break;
		default:
			if (hasPrmRole(loggedInUser) || SHARE_LEADS_LIST.equalsIgnoreCase(folderName)) {
				// No additional headers for this case
			} else {
				csvHeaders.add("TOTAL CAMPAIGNS");
				csvHeaders.add("ACTIVE CAMPAIGNS");
				csvHeaders.add("EMAIL OPENED");
				csvHeaders.add("CLICKED URLS");
			}
			break;
		}

		if (XamplifyUtils.isNotEmptyList(flexiFieldNames) && XamplifyUtils.CONTACTS.equalsIgnoreCase(moduleName)) {
			csvHeaders.addAll(XamplifyUtils.getFlexiFieldNames(flexiFieldNames));
		}

		return csvHeaders.toArray(new String[0]);
	}

	private List<String> findPartnerHeaders() {
		return Arrays.asList("FIRSTNAME", "LASTNAME", "ACCOUNT NAME", "ACCOUNT OWNER", "ACCOUNT SUB TYPE", "COMPANY",
				"COMPANY DOMAIN", "JOBTITLE", "EMAILID", "WEBSITE", "VERTICAL", "REGION", "TERRITORY", "TYPE",
				"CATEGORY", "ADDRESS", "CITY", "STATE", "ZIP", "COUNTRY", "MOBILE NUMBER");
	}

	private String[] getUserListCsvValues(String contactType, UserDTO user, List<FlexiFieldResponseDTO> flexiFieldNames,
			User loggedInUser, String folderName, String moduleName) {
		List<String> csvData = new ArrayList<>();
		csvData.addAll("partners".equalsIgnoreCase(moduleName) ? extractPartnerFieldValues(user)
				: extractContactFieldValues(user));
		csvData.add(XamplifyUtils.CONTACTS.equalsIgnoreCase(moduleName) ? user.getContactStatus() : "");
		switch (contactType) {
		case EXCLUDED:
			csvData.add(user.getExcludedCatagory());
			break;
		case UNDELIVERABLE:
			csvData.add(user.getEmailCategory());
			break;
		case UNSUBSCRIBED:
			if (hasPrmRole(loggedInUser) || SHARE_LEADS_LIST.equalsIgnoreCase(folderName)) {
				csvData.add(user.getUnsubscribedReason());
			} else {
				csvData.add(user.getTotalCampaignsCount().toString());
				csvData.add(user.getActiveCampaignsCount().toString());
				csvData.add(user.getEmailOpenedCount().toString());
				csvData.add(user.getClickedUrlsCount().toString());
				csvData.add(user.getUnsubscribedReason());
			}
			break;
		default:
			if (hasPrmRole(loggedInUser) || SHARE_LEADS_LIST.equalsIgnoreCase(folderName)) {
				// No additional values for this case
			} else {
				csvData.add(user.getTotalCampaignsCount().toString());
				csvData.add(user.getActiveCampaignsCount().toString());
				csvData.add(user.getEmailOpenedCount().toString());
				csvData.add(user.getClickedUrlsCount().toString());
			}
			break;
		}

		if (XamplifyUtils.isNotEmptyList(flexiFieldNames) && XamplifyUtils.CONTACTS.equalsIgnoreCase(moduleName)) {
			Map<String, String> flexiFieldValueMap = user.getFlexiFields().stream()
					.collect(Collectors.toMap(FlexiFieldRequestDTO::getFieldName, FlexiFieldRequestDTO::getFieldValue));

			flexiFieldNames.forEach(flexiFieldName -> {
				String fieldValue = flexiFieldValueMap.getOrDefault(flexiFieldName.getFieldName(), "");
				csvData.add(fieldValue);
			});
		}

		return csvData.toArray(new String[0]);
	}

	private List<String> extractPartnerFieldValues(UserDTO user) {
		return Arrays.asList(user.getFirstName(), user.getLastName(), user.getAccountName(), user.getAccountOwner(),
				user.getAccountSubType(), user.getContactCompany(), user.getCompanyDomain(), user.getJobTitle(),
				user.getEmailId(), user.getWebsite(), user.getVertical(), user.getRegion(), user.getTerritory(),
				user.getPartnerType(), user.getCategory(), user.getAddress(), user.getCity(), user.getState(),
				user.getZipCode(), user.getCountry(), user.getMobileNumber());
	}

	private List<String> extractContactFieldValues(UserDTO user) {
		return Arrays.asList(user.getFirstName(), user.getLastName(), user.getContactCompany(), user.getJobTitle(),
				user.getEmailId(), user.getAddress(), user.getCity(), user.getState(), user.getZipCode(),
				user.getCountry(), user.getMobileNumber());
	}

	private boolean hasPrmRole(User user) {
		return user.getRoles().stream().anyMatch(role -> role.getRoleId().equals(Role.PRM_ROLE.getRoleId()));
	}

}
