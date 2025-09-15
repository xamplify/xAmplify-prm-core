package com.xtremand.high.level.analytics.service.impl;

import java.lang.reflect.Field;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.ConditionalFormattingRule;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.DataBarFormatting;
import org.apache.poi.ss.usermodel.ExtendedColor;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Hyperlink;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.SheetConditionalFormatting;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xddf.usermodel.PresetColor;
import org.apache.poi.xddf.usermodel.XDDFColor;
import org.apache.poi.xddf.usermodel.XDDFShapeProperties;
import org.apache.poi.xddf.usermodel.XDDFSolidFillProperties;
import org.apache.poi.xddf.usermodel.chart.AxisPosition;
import org.apache.poi.xddf.usermodel.chart.BarDirection;
import org.apache.poi.xddf.usermodel.chart.ChartTypes;
import org.apache.poi.xddf.usermodel.chart.LegendPosition;
import org.apache.poi.xddf.usermodel.chart.XDDFBar3DChartData;
import org.apache.poi.xddf.usermodel.chart.XDDFBar3DChartData.Series;
import org.apache.poi.xddf.usermodel.chart.XDDFCategoryAxis;
import org.apache.poi.xddf.usermodel.chart.XDDFChartData;
import org.apache.poi.xddf.usermodel.chart.XDDFChartLegend;
import org.apache.poi.xddf.usermodel.chart.XDDFDataSource;
import org.apache.poi.xddf.usermodel.chart.XDDFDataSourcesFactory;
import org.apache.poi.xddf.usermodel.chart.XDDFNumericalDataSource;
import org.apache.poi.xddf.usermodel.chart.XDDFValueAxis;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFChart;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFDataBarFormatting;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFHyperlink;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.xtremand.campaign.exception.XamplifyDataAccessException;
import com.xtremand.dao.util.GenericDAO;
import com.xtremand.exception.DuplicateEntryException;
import com.xtremand.formbeans.XtremandResponse;
import com.xtremand.high.level.analytics.dao.hibernate.HibernateHighLevelAnalyticsDao;
import com.xtremand.high.level.analytics.dto.HighLevelAnalyticsDetailReportDTO;
import com.xtremand.high.level.analytics.service.HighLevelAnalyticsService;
import com.xtremand.highlevel.analytics.bom.DownloadModule;
import com.xtremand.highlevel.analytics.bom.DownloadRequest;
import com.xtremand.highlevel.analytics.bom.DownloadStatus;
import com.xtremand.highlevel.analytics.dto.HighLevelAnalyticsActivePartnersDto;
import com.xtremand.highlevel.analytics.dto.HighLevelAnalyticsDto;
import com.xtremand.highlevel.analytics.dto.HighLevelAnalyticsInactivePartnersDto;
import com.xtremand.highlevel.analytics.dto.HighLevelAnalyticsOnboardPartnersDto;
import com.xtremand.highlevel.analytics.dto.HighLevelAnalyticsPartnerAndTeamMemberDto;
import com.xtremand.highlevel.analytics.dto.HighLevelAnalyticsShareLeadsDto;
import com.xtremand.mail.service.AsyncComponent;
import com.xtremand.module.dao.ModuleDao;
import com.xtremand.module.service.ModuleService;
import com.xtremand.team.dao.TeamDao;
import com.xtremand.team.member.dto.RoleDisplayDTO;
import com.xtremand.user.bom.User;
import com.xtremand.user.dao.UserDAO;
import com.xtremand.util.dao.UtilDao;
import com.xtremand.util.dto.AccessDetailsDTO;
import com.xtremand.util.dto.DownloadRequestPostDTO;
import com.xtremand.util.dto.LeftSideNavigationBarItem;
import com.xtremand.util.dto.NumberFormatterString;
import com.xtremand.util.dto.TeamMemberFilterDTO;
import com.xtremand.util.service.UtilService;
import com.xtremand.vanity.url.dto.VanityUrlDetailsDTO;

@Service
@Transactional
public class HighLevelAnalyticsServiceImpl implements HighLevelAnalyticsService {

	@Autowired
	private HibernateHighLevelAnalyticsDao highLevelAnalyticsDao;

	@Autowired
	private UtilService utilService;

	@Autowired
	private ModuleService moduleService;

	@Autowired
	private ModuleDao moduleDao;

	@Autowired
	private UtilDao utilDao;

	@Autowired
	private GenericDAO genericDao;

	@Autowired
	@Lazy
	private AsyncComponent asyncComponent;

	@Autowired
	private UserDAO userDao;

	@Autowired
	private TeamDao teamDao;

	@Value("${high.total.module}")
	private String total;

	@Value("${onboard.module}")
	private String onBoard;

	@Value("${social.accounts.color}")
	private String shareLeadsAndOnBoardPartnersColor;

	@Value("${partners.icon}")
	private String partnersIcon;

	@Value("${partners.color}")
	private String partnersColor;

	@Value("${campaigns.module}")
	private String campaigns;

	@Value("${campaigns.icon}")
	private String campaignIcon;

	@Value("${campaigns.color}")
	private String campaignColor;

	@Value("${contacts.module}")
	private String contacts;

	@Value("${contacts.icon}")
	private String contactsIcon;

	@Value("${team.members.color}")
	private String contactsColor;

	@Value("${high.launched.module}")
	private String launched;

	@Value("${high.redistributed.module}")
	private String redisributed;

	@Value("${contacts.color}")
	private String redistributeCampaignColor;

	@Value("${uploaded.videos.color}")
	private String activePartnerColor;

	@Value("${active.module}")
	private String active;

	@Value("${inactive.module}")
	private String inActive;

	@Value("${users.module}")
	private String users;

	@Value("${share.leads.module}")
	private String shareLeads;

	@Value("${share.leads.icon}")
	private String shareLeadsIcon;

	@Value("${team.members.icon}")
	private String totalUsersIcon;

	@Value("${dashboard.access.denied}")
	private String accessDeniedClassName;

	/***** High Level Analytics ************/
	@Override
	public XtremandResponse getActiveAndInActivePartnersForDonutChart(VanityUrlDetailsDTO vanityUrlDetailsDto) {
		return highLevelAnalyticsDao.getActiveAndInActivePartnersForDonutChart(vanityUrlDetailsDto);
	}

	private HighLevelAnalyticsDetailReportDTO setModuleAnalyticsDetails(Integer count, String module, boolean hasAccess,
			String faIcon, String color, Integer moduleId, String description) {
		HighLevelAnalyticsDetailReportDTO dto = new HighLevelAnalyticsDetailReportDTO();
		Double countConvertToDouble = new Double(count);
		String convertConut = NumberFormatterString.formatValueInTrillionsOrBillions(countConvertToDouble);
		dto.setModuleId(moduleId);
		dto.setModuleName(module);
		dto.setCount(convertConut);
		if (count != 0) {
			dto.setHasAccess(hasAccess);
		} else {
			dto.setHasAccess(false);
		}
		if (hasAccess) {
			dto.setColor(color);
		} else {
			dto.setColor(color + " " + accessDeniedClassName);
		}
		dto.setFaIcon(faIcon);
		dto.setDescription(description);

		return dto;
	}

	@Override
	public XtremandResponse downloadAnalytics(VanityUrlDetailsDTO vanityUrlDetailsDto, Integer id) {
		XtremandResponse response = new XtremandResponse();
		try {
			XSSFWorkbook workBook = new XSSFWorkbook();
			String sheetName = "Over View";
			XSSFSheet sheet = workBook.createSheet(sheetName);
			sheet.setColumnWidth(0, 21000);
			Integer userId = vanityUrlDetailsDto.getUserId();

			RoleDisplayDTO roleDisplayDTO = utilService.getRoleDetailsByUserId(userId);
			AccessDetailsDTO accessDetailsDTO = utilService.getAccessDetails(userId);

			String customName = moduleDao.findPartnersModuleCustomNameByUserId(userId);
			String companyName = userDao.getCompanyNameByUserId(userId);
			String totalUsers = generateTotalUsersDetailReportSheet(vanityUrlDetailsDto, workBook, userId,
					accessDetailsDTO);

			String totalPartnersDetails = generateTotalPartnersDetailReportSheet(vanityUrlDetailsDto, roleDisplayDTO,
					workBook, userId, customName, accessDetailsDTO);
			String onboardPartners = generateOnboardPartnersSheet(vanityUrlDetailsDto, workBook, roleDisplayDTO, userId,
					customName, accessDetailsDTO);

			String activePartners = generateActivePartnersSheet(vanityUrlDetailsDto, workBook, roleDisplayDTO, userId,
					accessDetailsDTO);

			String inactivePartners = generateInactivePartnersSheet(vanityUrlDetailsDto, workBook, roleDisplayDTO,
					userId, customName, accessDetailsDTO);

			CreationHelper createHelper = workBook.getCreationHelper();
			CellStyle hlink_style = workBook.createCellStyle();
			Font hlink_font = workBook.createFont();
			hlink_font.setUnderline(Font.U_SINGLE);

			hlink_font.setColor(IndexedColors.BLUE.getIndex());
			hlink_style.setFont(hlink_font);
			hlink_style.setAlignment(HorizontalAlignment.CENTER);

			CellStyle totalContactsCountAlignment = workBook.createCellStyle();
			totalContactsCountAlignment.setAlignment(HorizontalAlignment.CENTER);

			Row row = sheet.createRow(0);

			generateTotaluserHyperLink(vanityUrlDetailsDto, workBook, sheet, totalUsers, createHelper, hlink_style, row,
					companyName, accessDetailsDTO, totalContactsCountAlignment);

			createEmptyRow(sheet);

			createTotalPartnerAndTotalContactHyperLink(vanityUrlDetailsDto, workBook, sheet, roleDisplayDTO,
					totalPartnersDetails, createHelper, hlink_style, totalContactsCountAlignment, companyName, userId,
					accessDetailsDTO);

			createEmptyRow(sheet);

			createEmptyRow(sheet);

			createOnBoardAndActiveAndInActivePartnerHyperLink(vanityUrlDetailsDto, sheet, onboardPartners,
					activePartners, inactivePartners, roleDisplayDTO, createHelper, hlink_style, accessDetailsDTO,
					totalContactsCountAlignment);

			createEmptyRow(sheet);

			generatePieChart(vanityUrlDetailsDto, sheet, roleDisplayDTO, customName);

			generateShareLeadsForSecondDrill(vanityUrlDetailsDto, workBook, roleDisplayDTO, userId, hlink_style,
					createHelper);

			generateShareLeadsForThirdDrill(vanityUrlDetailsDto, workBook, roleDisplayDTO, userId, hlink_style,
					createHelper);

			response.setData(workBook);
		} catch (Exception e) {
			highLevelAnalyticsDao.updateDownloadRequestStatus(id, DownloadStatus.FAILED);
		}
		return response;
	}

	private void generatePieChart(VanityUrlDetailsDTO vanityUrlDetailsDto, XSSFSheet sheet,
			RoleDisplayDTO roleDisplayDTO, String customName) {
		boolean isPrm = roleDisplayDTO.isPrm() || roleDisplayDTO.isPrmTeamMember() || roleDisplayDTO.isPrmAndPartner()
				|| roleDisplayDTO.isPrmAndPartnerTeamMember();
		if (!(isPrm)) {
			XSSFDrawing piedrawing = sheet.createDrawingPatriarch();
			XSSFClientAnchor pieanchor = piedrawing.createAnchor(0, 0, 0, 0, 3, 15, 13, 27);
			XSSFChart piechart = piedrawing.createChart(pieanchor);
			piechart.setTitleText("Active Vs InActive " + customName);
			piechart.setTitleOverlay(false);

			XDDFChartLegend pielegend = piechart.getOrAddLegend();
			pielegend.setPosition(LegendPosition.BOTTOM);
			XDDFDataSource<String> cat = XDDFDataSourcesFactory.fromArray(new String[] { "Active", "Inactive" });
			XDDFNumericalDataSource<Integer> values = XDDFDataSourcesFactory.fromArray(new Integer[] {
					highLevelAnalyticsDao.getHighLevelAnalyticsDetailReportsForActivePartners(vanityUrlDetailsDto),
					highLevelAnalyticsDao.getHighLevelAnalyticsDetailReportsForInActivePartners(vanityUrlDetailsDto) });

			XDDFChartData piedata = piechart.createData(ChartTypes.PIE, null, null);
			piedata.setVaryColors(true);
			XDDFChartData.Series pieSeries = piedata.addSeries(cat, values);
			pieSeries.setShowLeaderLines(true);
			int datalabels = 0;
			piechart.getCTChart().getPlotArea().getPieChartArray(0).getSerArray(datalabels).getDLbls().addNewShowVal()
					.setVal(true);
			piechart.getCTChart().getPlotArea().getPieChartArray(0).getSerArray(datalabels).getDLbls()
					.addNewShowLegendKey().setVal(false);
			piechart.getCTChart().getPlotArea().getPieChartArray(0).getSerArray(datalabels).getDLbls()
					.addNewShowCatName().setVal(false);
			piechart.getCTChart().getPlotArea().getPieChartArray(0).getSerArray(datalabels).getDLbls()
					.addNewShowSerName().setVal(false);
			piechart.plot(piedata);
		}
	}

	private void createOnBoardAndActiveAndInActivePartnerHyperLink(VanityUrlDetailsDTO vanityUrlDetailsDto,
			XSSFSheet sheet, String onboardPartners, String activePartners, String inactivePartners,
			RoleDisplayDTO roleDisplayDTO, CreationHelper createHelper, CellStyle hlink_style,
			AccessDetailsDTO accessDetailsDTO, CellStyle totalContactsCountAlignment) {
		String name = moduleDao.findPartnersModuleCustomNameByUserId(vanityUrlDetailsDto.getUserId());
		boolean isPrm = roleDisplayDTO.isPrm() || roleDisplayDTO.isPrmTeamMember() || roleDisplayDTO.isPrmAndPartner()
				|| roleDisplayDTO.isPrmAndPartnerTeamMember();
		Row row;
		Cell cell;

		row = sheet.createRow(17);
		cell = row.createCell(0);
		cell.setCellValue("On Board " + name + " (with CompanyProfile)");
		cell = row.createCell(1);
		cell.setCellValue(
				highLevelAnalyticsDao.getHighLevelAnalyticsDetailReportsForOnBoardPartners(vanityUrlDetailsDto));
		if (accessDetailsDTO.isPartnerAccess()) {
			Hyperlink link7 = createHelper.createHyperlink(HyperlinkType.DOCUMENT);
			link7.setAddress("'" + onboardPartners + "'!A1");
			cell.setHyperlink(link7);
			cell.setCellStyle(hlink_style);
		} else {
			cell.setCellStyle(totalContactsCountAlignment);
		}

		createEmptyRow(sheet);

		if (!(isPrm)) {
			row = sheet.createRow(19);
			cell = row.createCell(0);
			cell.setCellValue("Active " + name + " ( " + name
					+ "  Who Redistributed atleast one Campaign and with Company profile)");
			cell = row.createCell(1);
			cell.setCellValue(
					highLevelAnalyticsDao.getHighLevelAnalyticsDetailReportsForActivePartners(vanityUrlDetailsDto));
			cell.setCellStyle(totalContactsCountAlignment);

			createEmptyRow(sheet);

			if (!(isPrm)) {
				row = sheet.createRow(21);
				cell = row.createCell(0);
				cell.setCellValue("Inactive " + name + " ( " + name
						+ "  Who does not Redistribute Campaign and without CompanyProfile)");
				cell = row.createCell(1);
				cell.setCellValue(highLevelAnalyticsDao
						.getHighLevelAnalyticsDetailReportsForInActivePartners(vanityUrlDetailsDto));
				cell.setCellStyle(totalContactsCountAlignment);
			}
		}

	}

	public XtremandResponse getHighLevelAnalyticsDetailReportsForTiles(VanityUrlDetailsDTO vanityUrlDetailsDTO) {
		Integer loggedInUserId = vanityUrlDetailsDTO.getUserId();
		List<HighLevelAnalyticsDetailReportDTO> dtos = new ArrayList<>();
		RoleDisplayDTO roleDisplayDTO = utilService.getRoleDetailsByUserId(loggedInUserId);
		AccessDetailsDTO accessDetailsDTO = utilService.getAccessDetails(loggedInUserId);
		boolean applyFilter = vanityUrlDetailsDTO.isApplyFilter();
		TeamMemberFilterDTO teamMemberFilterDTO = utilDao.applyFilterConditions(loggedInUserId, applyFilter, false);
		setTotalPartnerTile(vanityUrlDetailsDTO, dtos, roleDisplayDTO, accessDetailsDTO);
		setOnBoardPartnersCount(vanityUrlDetailsDTO, dtos, roleDisplayDTO, accessDetailsDTO);
		setActivePartnersCount(vanityUrlDetailsDTO, dtos, roleDisplayDTO, accessDetailsDTO);
		setInActivePartnersCount(vanityUrlDetailsDTO, dtos, roleDisplayDTO, accessDetailsDTO);
		setShareLeadsCount(vanityUrlDetailsDTO, dtos, accessDetailsDTO);
		setTotalUsersCount(vanityUrlDetailsDTO, dtos, accessDetailsDTO);
		XtremandResponse response = new XtremandResponse();
		response.setStatusCode(200);
		response.setData(dtos);

		return response;
	}

	private void setTotalUsersCount(VanityUrlDetailsDTO vanityUrlDetailsDTO,
			List<HighLevelAnalyticsDetailReportDTO> dtos, AccessDetailsDTO accessDetailsDTO) {
		String description = total + users + " (Admin+Team Members) which includes all the users of the platform";
		Integer count = highLevelAnalyticsDao.getHighLevelAnalyticsDetailReportsForTotalUsersTile(vanityUrlDetailsDTO);
		dtos.add(setModuleAnalyticsDetails(count, total + users, accessDetailsDTO.isTeamMemberAccess(), totalUsersIcon,
				contactsColor, 9, description));
	}

	private void createTotalPartnerAndTotalContactHyperLink(VanityUrlDetailsDTO vanityUrlDetailsDto,
			XSSFWorkbook workBook, XSSFSheet sheet, RoleDisplayDTO roleDisplayDTO, String totalPartnersDetails,
			CreationHelper createHelper, CellStyle hlink_style, CellStyle totalContactsCountAlignment,
			String companyName, Integer userId, AccessDetailsDTO accessDetailsDTO) {
		String name = moduleDao.findPartnersModuleCustomNameByUserId(vanityUrlDetailsDto.getUserId());
		boolean isPrm = roleDisplayDTO.isPrm() || roleDisplayDTO.isPrmTeamMember() || roleDisplayDTO.isPrmAndPartner()
				|| roleDisplayDTO.isPrmAndPartnerTeamMember();
		Row row;
		Cell cell;
		row = sheet.createRow(4);
		cell = row.createCell(0);
		CellStyle cellStyle1 = workBook.createCellStyle();
		Font bluefont1 = workBook.createFont();
		bluefont1.setColor(IndexedColors.DARK_BLUE.getIndex());
		bluefont1.setBold(true);
		cellStyle1.setFont(bluefont1);
		cell.setCellStyle(cellStyle1);
		cell.setCellValue("Total " + name + " For " + companyName);

		row = sheet.createRow(5);
		cell = row.createCell(0);
		cell.setCellValue(name + " Which Includes (#" + name + "-"
				+ highLevelAnalyticsDao.getPartnerCountInExcel(vanityUrlDetailsDto) + " and " + "#Team Members-"
				+ highLevelAnalyticsDao.getTeamMemberCountInExcel(vanityUrlDetailsDto) + ")");
		cell = row.createCell(1);
		cell.setCellValue(
				highLevelAnalyticsDao.getHighLevelAnalyticsDetailReportsForTotalPartners(vanityUrlDetailsDto));
		if (accessDetailsDTO.isPartnerAccess()) {
			Hyperlink link2 = createHelper.createHyperlink(HyperlinkType.DOCUMENT);
			link2.setAddress("'" + totalPartnersDetails + "'!A1");
			cell.setHyperlink(link2);
			cell.setCellStyle(hlink_style);
		} else {
			cell.setCellStyle(totalContactsCountAlignment);
		}

		createEmptyRow(sheet);

		boolean access = utilDao.hasShareLeadsAccessByUserId(vanityUrlDetailsDto.getUserId());
		LeftSideNavigationBarItem leftSideNavigationBarItem = moduleService.findLeftMenuItems(vanityUrlDetailsDto);
		boolean isShareLeadsAccess = leftSideNavigationBarItem.isShareLeads();

		if (!(isPrm)) {
			row = sheet.createRow(9);
			cell = row.createCell(0);
			cell.setCellValue("Total Share Leads");
			cell = row.createCell(1);
			cell.setCellValue(
					highLevelAnalyticsDao.getHighLevelAnalyticsDetailReportsForShareLeadsTile(vanityUrlDetailsDto));
			if (access && isShareLeadsAccess) {
				Hyperlink link4 = createHelper.createHyperlink(HyperlinkType.DOCUMENT);
				String list = generateShareLeads(vanityUrlDetailsDto, workBook, roleDisplayDTO, userId, hlink_style,
						createHelper);
				link4.setAddress("'" + list + "'!A1");
				cell.setHyperlink(link4);
				cell.setCellStyle(hlink_style);
			} else {

				cell.setCellStyle(totalContactsCountAlignment);
			}
		}
	}

	private void generateTotaluserHyperLink(VanityUrlDetailsDTO vanityUrlDetailsDto, XSSFWorkbook workBook,
			XSSFSheet sheet, String totalUsers, CreationHelper createHelper, CellStyle hlink_style, Row row,
			String companyName, AccessDetailsDTO accessDetailsDTO, CellStyle totalContactsCountAlignment) {
		CellStyle cellStyle = workBook.createCellStyle();
		cellStyle.setAlignment(HorizontalAlignment.CENTER);
		Font bluefont = workBook.createFont();
		bluefont.setBold(true);
		bluefont.setFontHeightInPoints((short) 15);
		bluefont.setColor(IndexedColors.DARK_BLUE.getIndex());
		cellStyle.setFont(bluefont);
		Cell cell = row.createCell(0);
		cell.setCellValue(companyName + " Analysis");
		cell.setCellStyle(cellStyle);

		createEmptyRowWithCellStyle(sheet, cellStyle);

		row = sheet.createRow(2);
		cell = row.createCell(0);
		cell.setCellValue("Total Users Which Includes all the users of Platform With " + companyName);
		cell = row.createCell(1);
		cell.setCellValue(
				highLevelAnalyticsDao.getHighLevelAnalyticsDetailReportsForTotalUsersTile(vanityUrlDetailsDto));
		if (accessDetailsDTO.isTeamMemberAccess()) {
			XSSFHyperlink link = (XSSFHyperlink) createHelper.createHyperlink(HyperlinkType.DOCUMENT);
			link.setAddress("'" + totalUsers + "'!A1");
			cell.setHyperlink(link);
			cell.setCellStyle(hlink_style);
		} else {
			cell.setCellStyle(totalContactsCountAlignment);
		}

	}

	private String generateInactivePartnersSheet(VanityUrlDetailsDTO vanityUrlDetailsDto, XSSFWorkbook workBook,
			RoleDisplayDTO roleDisplayDTO, Integer userId, String name, AccessDetailsDTO accessDetailsDTO) {
		String inactivePartners = "Inactive Partner Detail Report";
		try {
			List<HighLevelAnalyticsInactivePartnersDto> inactivePartnersList = highLevelAnalyticsDao
					.getInactivePartners(userId, vanityUrlDetailsDto);

			XSSFSheet inactivePartnersSheet = workBook.createSheet(inactivePartners);
			inactivePartnersSheet.setColumnWidth(0, 8000);
			CellStyle inactivePartnersCellStyle = workBook.createCellStyle();
			inactivePartnersCellStyle.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
			inactivePartnersCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
			inactivePartnersCellStyle.setAlignment(HorizontalAlignment.CENTER);
			Font inactivePartnersFont = workBook.createFont();
			inactivePartnersFont.setColor(IndexedColors.BLACK.getIndex());
			inactivePartnersFont.setBold(true);
			inactivePartnersCellStyle.setFont(inactivePartnersFont);

			Row inactivePartnersRow = inactivePartnersSheet.createRow((short) 0);

			Cell inactivePartnersCell = inactivePartnersRow.createCell((short) 0);
			inactivePartnersSheet.setColumnWidth(1, 8000);
			inactivePartnersCell.setCellValue("Partner Company Name");
			inactivePartnersCell.setCellStyle(inactivePartnersCellStyle);

			inactivePartnersCell = inactivePartnersRow.createCell((short) 1);
			inactivePartnersSheet.setColumnWidth(2, 8000);
			inactivePartnersCell.setCellValue("Email ID");
			inactivePartnersCell.setCellStyle(inactivePartnersCellStyle);

			inactivePartnersCell = inactivePartnersRow.createCell((short) 2);
			inactivePartnersSheet.setColumnWidth(3, 8000);
			inactivePartnersCell.setCellValue("First Name");
			inactivePartnersCell.setCellStyle(inactivePartnersCellStyle);

			inactivePartnersCell = inactivePartnersRow.createCell((short) 3);
			inactivePartnersSheet.setColumnWidth(4, 8000);
			inactivePartnersCell.setCellValue("Last Name");
			inactivePartnersCell.setCellStyle(inactivePartnersCellStyle);

			int inactivePartnersRowNum = 1;
			for (HighLevelAnalyticsInactivePartnersDto inactivePartnersData : inactivePartnersList) {
				inactivePartnersRow = inactivePartnersSheet.createRow((short) inactivePartnersRowNum++);
				inactivePartnersCell = inactivePartnersRow.createCell((short) 0);
				inactivePartnersCell.setCellValue(inactivePartnersData.getCompanyName());

				inactivePartnersCell = inactivePartnersRow.createCell((short) 1);
				inactivePartnersCell.setCellValue(inactivePartnersData.getEmailId());

				inactivePartnersCell = inactivePartnersRow.createCell((short) 2);
				inactivePartnersCell.setCellValue(inactivePartnersData.getFirstName());

				inactivePartnersCell = inactivePartnersRow.createCell((short) 3);
				inactivePartnersCell.setCellValue(inactivePartnersData.getLastName());

			}
			return inactivePartners;

		} catch (Exception e) {
			return inactivePartners;
		}
	}

	private String generateActivePartnersSheet(VanityUrlDetailsDTO vanityUrlDetailsDto, XSSFWorkbook workBook,
			RoleDisplayDTO roleDisplayDTO, Integer userId, AccessDetailsDTO accessDetailsDTO) {
		String activePartners = "Active Partner Detail Report";

		try {
			List<HighLevelAnalyticsActivePartnersDto> activePartnersList = highLevelAnalyticsDao
					.getActivePartners(userId, vanityUrlDetailsDto);

			XSSFSheet activePartnersSheet = workBook.createSheet(activePartners);
			activePartnersSheet.setColumnWidth(0, 8000);
			CellStyle activePartnersCellStyle = workBook.createCellStyle();
			activePartnersCellStyle.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
			activePartnersCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
			activePartnersCellStyle.setAlignment(HorizontalAlignment.CENTER);
			Font activePartnersFont = workBook.createFont();
			activePartnersFont.setColor(IndexedColors.BLACK.getIndex());
			activePartnersFont.setBold(true);
			activePartnersCellStyle.setFont(activePartnersFont);
			Row activePartnersRow = activePartnersSheet.createRow((short) 0);

			Cell activePartnersCell = activePartnersRow.createCell((short) 0);
			activePartnersSheet.setColumnWidth(1, 8000);
			activePartnersCell.setCellValue("Partner Company Name");
			activePartnersCell.setCellStyle(activePartnersCellStyle);

			activePartnersCell = activePartnersRow.createCell((short) 1);
			activePartnersSheet.setColumnWidth(2, 8000);
			activePartnersCell.setCellValue("Email ID");
			activePartnersCell.setCellStyle(activePartnersCellStyle);

			activePartnersCell = activePartnersRow.createCell((short) 2);
			activePartnersSheet.setColumnWidth(3, 8000);
			activePartnersCell.setCellValue("Recent Campaign Name");
			activePartnersCell.setCellStyle(activePartnersCellStyle);

			activePartnersCell = activePartnersRow.createCell((short) 3);
			activePartnersSheet.setColumnWidth(4, 8000);
			activePartnersCell.setCellValue("Recent Campaign Launched Time");
			activePartnersCell.setCellStyle(activePartnersCellStyle);

			activePartnersCell = activePartnersRow.createCell((short) 4);
			activePartnersSheet.setColumnWidth(5, 8000);
			activePartnersCell.setCellValue("First Campaign Name");
			activePartnersCell.setCellStyle(activePartnersCellStyle);

			activePartnersCell = activePartnersRow.createCell((short) 5);
			activePartnersSheet.setColumnWidth(6, 8000);
			activePartnersCell.setCellValue("First Campaign Launched Time");
			activePartnersCell.setCellStyle(activePartnersCellStyle);

			activePartnersCell = activePartnersRow.createCell((short) 6);
			activePartnersSheet.setColumnWidth(7, 8000);
			activePartnersCell.setCellValue("Redistributed Campaign");
			activePartnersCell.setCellStyle(activePartnersCellStyle);

			CellStyle activePartnersAlignment = workBook.createCellStyle();
			activePartnersAlignment.setAlignment(HorizontalAlignment.CENTER);
			int activePartnersRowNum = 1;
			for (HighLevelAnalyticsActivePartnersDto activePartnersData : activePartnersList) {
				activePartnersRow = activePartnersSheet.createRow((short) activePartnersRowNum++);
				activePartnersCell = activePartnersRow.createCell((short) 0);
				activePartnersCell.setCellValue(activePartnersData.getPartnerCompanyName());

				activePartnersCell = activePartnersRow.createCell((short) 1);
				activePartnersCell.setCellValue(activePartnersData.getEmailId());

				activePartnersCell = activePartnersRow.createCell((short) 2);
				activePartnersCell.setCellValue(activePartnersData.getRecentCampaignName());

				SimpleDateFormat activePartnersDate = new SimpleDateFormat("yyyy/dd/MM hh:mm:ss");
				String stringActivePartnersDate = activePartnersDate
						.format(activePartnersData.getRecentCampaignLaunchedTime());
				activePartnersCell = activePartnersRow.createCell((short) 3);
				activePartnersCell.setCellValue(stringActivePartnersDate);
				activePartnersCell.setCellStyle(activePartnersAlignment);

				activePartnersCell = activePartnersRow.createCell((short) 4);
				activePartnersCell.setCellValue(activePartnersData.getFirstCampaignName());

				SimpleDateFormat activePartnersFirstDate = new SimpleDateFormat("yyyy/dd/MM hh:mm:ss");
				String stringActivePartnersFirstDate = activePartnersFirstDate
						.format(activePartnersData.getFirstCampaignlaunchedTime());
				activePartnersCell = activePartnersRow.createCell((short) 5);
				activePartnersCell.setCellValue(stringActivePartnersFirstDate);
				activePartnersCell.setCellStyle(activePartnersAlignment);

				BigInteger redistributedCampBigInteger = activePartnersData.getRedistributedCampaignId();
				int redistributedCampaignBigInteger = redistributedCampBigInteger.intValue();
				activePartnersCell = activePartnersRow.createCell((short) 6);
				activePartnersCell.setCellValue(redistributedCampaignBigInteger);
				activePartnersCell.setCellStyle(activePartnersAlignment);
			}

			XSSFDrawing activeDrawing = activePartnersSheet.createDrawingPatriarch();
			ClientAnchor activeAnchorSheet = activeDrawing.createAnchor(0, 0, 0, 0, 9, 0, 17, 16);

			XSSFChart activeChart = activeDrawing.createChart(activeAnchorSheet);
			activeChart.setTitleText("Top 10 Active Partners-Campaigns Count");
			activeChart.setTitleOverlay(false);

			XDDFChartLegend activeLegendSheet = activeChart.getOrAddLegend();
			activeLegendSheet.setPosition(LegendPosition.TOP_RIGHT);

			XDDFCategoryAxis activeBottomAxisSheet = activeChart.createCategoryAxis(AxisPosition.BOTTOM);
			activeBottomAxisSheet.setVisible(true);
			XDDFValueAxis activeLeftAxisSheet = activeChart.createValueAxis(AxisPosition.LEFT);
			activeLeftAxisSheet.setVisible(false);

			XDDFDataSource<String> names = XDDFDataSourcesFactory.fromStringCellRange(activePartnersSheet,
					new CellRangeAddress(1, 10, 0, 0));

			XDDFNumericalDataSource<Double> twovalue = XDDFDataSourcesFactory.fromNumericCellRange(activePartnersSheet,
					new CellRangeAddress(1, 10, 6, 6));

			XDDFChartData activeDataSheet = activeChart.createData(ChartTypes.BAR, activeBottomAxisSheet,
					activeLeftAxisSheet);
			XDDFBar3DChartData.Series activeSeries2Sheet = (Series) activeDataSheet.addSeries(names, twovalue);
			activeSeries2Sheet.setTitle("", null);
			int seriesnr = 0;
			activeSeries2Sheet.setShowLeaderLines(true);
			activeChart.getCTChart().getPlotArea().getBar3DChartArray(0).getSerArray(seriesnr).getDLbls()
					.addNewShowVal().setVal(true);
			activeChart.getCTChart().getPlotArea().getBar3DChartArray(0).getSerArray(seriesnr).getDLbls()
					.addNewShowLegendKey().setVal(false);
			activeChart.getCTChart().getPlotArea().getBar3DChartArray(0).getSerArray(seriesnr).getDLbls()
					.addNewShowCatName().setVal(false);
			activeChart.getCTChart().getPlotArea().getBar3DChartArray(0).getSerArray(seriesnr).getDLbls()
					.addNewShowSerName().setVal(false);

			activeDataSheet.setVaryColors(true);
			activeChart.plot(activeDataSheet);

			XDDFBar3DChartData activeBarSheet = (XDDFBar3DChartData) activeDataSheet;
			activeBarSheet.setBarDirection(BarDirection.COL);
			solidFillSeries(activeDataSheet, 0, PresetColor.CORNFLOWER_BLUE);
			return activePartners;
		} catch (Exception e) {
			return activePartners;
		}

	}

	private String generateOnboardPartnersSheet(VanityUrlDetailsDTO vanityUrlDetailsDto, XSSFWorkbook workBook,
			RoleDisplayDTO roleDisplayDTO, Integer userId, String name, AccessDetailsDTO accessDetailsDTO) {
		String onboardPartners = "Onboard Partners Details";

		try {
			List<HighLevelAnalyticsOnboardPartnersDto> onBoardList = highLevelAnalyticsDao.getOnboardPartners(userId,
					vanityUrlDetailsDto);

			XSSFSheet onboardPartnersSheet = workBook.createSheet(onboardPartners);
			onboardPartnersSheet.setColumnWidth(0, 8000);
			CellStyle onboardPartnersCellStyle = workBook.createCellStyle();
			onboardPartnersCellStyle.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
			onboardPartnersCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
			onboardPartnersCellStyle.setAlignment(HorizontalAlignment.CENTER);
			Font onboardPartnersFont = workBook.createFont();
			onboardPartnersFont.setColor(IndexedColors.BLACK.getIndex());
			onboardPartnersFont.setBold(true);
			onboardPartnersCellStyle.setFont(onboardPartnersFont);
			Row onboardPartnersRow = onboardPartnersSheet.createRow((short) 0);

			Cell onboardPartnersCell = onboardPartnersRow.createCell((short) 0);
			onboardPartnersSheet.setColumnWidth(1, 8000);
			onboardPartnersCell.setCellValue("Partner Company Name");
			onboardPartnersCell.setCellStyle(onboardPartnersCellStyle);

			onboardPartnersCell = onboardPartnersRow.createCell((short) 1);
			onboardPartnersSheet.setColumnWidth(2, 8000);
			onboardPartnersCell.setCellValue("Email ID");
			onboardPartnersCell.setCellStyle(onboardPartnersCellStyle);

			onboardPartnersCell = onboardPartnersRow.createCell((short) 2);
			onboardPartnersSheet.setColumnWidth(3, 8000);
			onboardPartnersCell.setCellValue("First Name");
			onboardPartnersCell.setCellStyle(onboardPartnersCellStyle);

			onboardPartnersCell = onboardPartnersRow.createCell((short) 3);
			onboardPartnersSheet.setColumnWidth(4, 8000);
			onboardPartnersCell.setCellValue("Last Name");
			onboardPartnersCell.setCellStyle(onboardPartnersCellStyle);

			int onboardPartnerRowNum = 1;
			for (HighLevelAnalyticsOnboardPartnersDto onBoardPartnersData : onBoardList) {
				onboardPartnersRow = onboardPartnersSheet.createRow((short) onboardPartnerRowNum++);
				onboardPartnersCell = onboardPartnersRow.createCell((short) 0);
				onboardPartnersCell.setCellValue(onBoardPartnersData.getCompanyName());

				onboardPartnersCell = onboardPartnersRow.createCell((short) 1);
				onboardPartnersCell.setCellValue(onBoardPartnersData.getEmail());

				onboardPartnersCell = onboardPartnersRow.createCell((short) 2);
				onboardPartnersCell.setCellValue(onBoardPartnersData.getFirstname());

				onboardPartnersCell = onboardPartnersRow.createCell((short) 3);
				onboardPartnersCell.setCellValue(onBoardPartnersData.getLastname());

			}
			return onboardPartners;
		} catch (Exception e) {
			return onboardPartners;
		}
	}

	private String generateTotalPartnersDetailReportSheet(VanityUrlDetailsDTO vanityUrlDetailsDto,
			RoleDisplayDTO roleDisplayDTO, XSSFWorkbook workBook, Integer userId, String customName,
			AccessDetailsDTO accessDetailsDTO) {
		String totalPartnersDetails = "Total Partners Detail Report";

		try {
			List<HighLevelAnalyticsPartnerAndTeamMemberDto> partnerAndTeamMemberList = highLevelAnalyticsDao
					.getHighLevelAnalyticsPartnerWhichIncludesPartnerAndTeamMember(userId, vanityUrlDetailsDto);

			XSSFSheet totalPartnersSheet = workBook.createSheet(totalPartnersDetails);
			totalPartnersSheet.setColumnWidth(0, 8000);
			CellStyle totalPartnersCellStyle = workBook.createCellStyle();
			totalPartnersCellStyle.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
			totalPartnersCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
			totalPartnersCellStyle.setAlignment(HorizontalAlignment.CENTER);
			Font totalPartnersFont = workBook.createFont();
			totalPartnersFont.setColor(IndexedColors.BLACK.getIndex());
			totalPartnersFont.setBold(true);
			totalPartnersCellStyle.setFont(totalPartnersFont);
			Row totalPartnersRow = totalPartnersSheet.createRow((short) 0);

			Cell totalPartnersCell = totalPartnersRow.createCell((short) 0);
			totalPartnersSheet.setColumnWidth(1, 8000);
			totalPartnersCell.setCellValue("Partner Company Name");
			totalPartnersCell.setCellStyle(totalPartnersCellStyle);

			totalPartnersCell = totalPartnersRow.createCell((short) 1);
			totalPartnersSheet.setColumnWidth(2, 8000);
			totalPartnersCell.setCellValue("Email Id");
			totalPartnersCell.setCellStyle(totalPartnersCellStyle);

			totalPartnersCell = totalPartnersRow.createCell((short) 2);
			totalPartnersSheet.setColumnWidth(3, 8000);
			totalPartnersCell.setCellValue("First Name");
			totalPartnersCell.setCellStyle(totalPartnersCellStyle);

			totalPartnersCell = totalPartnersRow.createCell((short) 3);
			totalPartnersSheet.setColumnWidth(4, 8000);
			totalPartnersCell.setCellValue("Last Name");
			totalPartnersCell.setCellStyle(totalPartnersCellStyle);

			totalPartnersCell = totalPartnersRow.createCell((short) 4);
			totalPartnersSheet.setColumnWidth(5, 8000);
			totalPartnersCell.setCellValue("Description");
			totalPartnersCell.setCellStyle(totalPartnersCellStyle);

			int partnerAndTeamMemberRowNum = 1;
			for (HighLevelAnalyticsPartnerAndTeamMemberDto partnerAndTeamMemberDataList : partnerAndTeamMemberList) {
				totalPartnersRow = totalPartnersSheet.createRow((short) partnerAndTeamMemberRowNum++);
				totalPartnersCell = totalPartnersRow.createCell((short) 0);
				totalPartnersCell.setCellValue(partnerAndTeamMemberDataList.getPartnerCompanyName());

				totalPartnersCell = totalPartnersRow.createCell((short) 1);
				totalPartnersCell.setCellValue(partnerAndTeamMemberDataList.getEmailId());

				totalPartnersCell = totalPartnersRow.createCell((short) 2);
				totalPartnersCell.setCellValue(partnerAndTeamMemberDataList.getFirstName());

				totalPartnersCell = totalPartnersRow.createCell((short) 3);
				totalPartnersCell.setCellValue(partnerAndTeamMemberDataList.getLastName());

				totalPartnersCell = totalPartnersRow.createCell((short) 4);
				totalPartnersCell.setCellValue(partnerAndTeamMemberDataList.getDescription());
			}
			return totalPartnersDetails;
		} catch (Exception e) {
			return totalPartnersDetails;
		}
	}

	private String generateTotalUsersDetailReportSheet(VanityUrlDetailsDTO vanityUrlDetailsDto, XSSFWorkbook workBook,
			Integer userId, AccessDetailsDTO accessDetailsDTO) {
		String totalUsers = "Total Users Detail Report";
		if (accessDetailsDTO.isTeamMemberAccess()) {
			try {

				List<HighLevelAnalyticsDto> totalUsersList = highLevelAnalyticsDao
						.getHighLevelAnalyticsTotalUsersDetailReport(userId, vanityUrlDetailsDto);

				XSSFSheet totalUsersSheet = workBook.createSheet(totalUsers);
				totalUsersSheet.setColumnWidth(0, 8000);
				CellStyle totalUsersCellStyle = workBook.createCellStyle();
				totalUsersCellStyle.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
				totalUsersCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
				totalUsersCellStyle.setAlignment(HorizontalAlignment.CENTER);
				Font font = workBook.createFont();
				font.setColor(IndexedColors.BLACK.getIndex());
				font.setBold(true);
				totalUsersCellStyle.setFont(font);
				Row totalUsersRow = totalUsersSheet.createRow((short) 0);

				Cell totalUsersCell = totalUsersRow.createCell((short) 0);
				totalUsersSheet.setColumnWidth(1, 8000);
				totalUsersCell.setCellValue("Email Id");
				totalUsersCell.setCellStyle(totalUsersCellStyle);

				totalUsersCell = totalUsersRow.createCell((short) 1);
				totalUsersSheet.setColumnWidth(2, 8000);
				totalUsersCell.setCellValue("First Name");
				totalUsersCell.setCellStyle(totalUsersCellStyle);

				totalUsersCell = totalUsersRow.createCell((short) 2);
				totalUsersSheet.setColumnWidth(3, 8000);
				totalUsersCell.setCellValue("Last Name");
				totalUsersCell.setCellStyle(totalUsersCellStyle);

				totalUsersCell = totalUsersRow.createCell((short) 3);
				totalUsersSheet.setColumnWidth(4, 8000);
				totalUsersCell.setCellValue("Date reg");
				totalUsersCell.setCellStyle(totalUsersCellStyle);

				totalUsersCell = totalUsersRow.createCell((short) 4);
				totalUsersSheet.setColumnWidth(5, 8000);
				totalUsersCell.setCellValue("Date Last Login");
				totalUsersCell.setCellStyle(totalUsersCellStyle);

				CellStyle totalUsersAlignment = workBook.createCellStyle();
				totalUsersAlignment.setAlignment(HorizontalAlignment.CENTER);
				int totalUsersRowNum = 1;
				for (HighLevelAnalyticsDto totalUsersDataList : totalUsersList) {
					totalUsersRow = totalUsersSheet.createRow((short) totalUsersRowNum++);
					totalUsersCell = totalUsersRow.createCell((short) 0);
					totalUsersCell.setCellValue(totalUsersDataList.getEmailId());

					totalUsersCell = totalUsersRow.createCell((short) 1);
					totalUsersCell.setCellValue(totalUsersDataList.getFirstName());

					totalUsersCell = totalUsersRow.createCell((short) 2);
					totalUsersCell.setCellValue(totalUsersDataList.getLastName());

					SimpleDateFormat totalUsersDateLogin = new SimpleDateFormat("yyyy/dd/MM hh:mm:ss");
					String dateRegString = totalUsersDateLogin.format(totalUsersDataList.getDateReg());
					totalUsersCell = totalUsersRow.createCell((short) 3);
					totalUsersCell.setCellValue(dateRegString);
					totalUsersCell.setCellStyle(totalUsersAlignment);

					SimpleDateFormat totalUsersLastLogin = new SimpleDateFormat("yyyy/dd/MM hh:mm:ss");
					String dateLastLogin = totalUsersLastLogin.format(totalUsersDataList.getDateLastLogin());
					totalUsersCell = totalUsersRow.createCell((short) 4);
					totalUsersCell.setCellValue(dateLastLogin);
					totalUsersCell.setCellStyle(totalUsersAlignment);
				}

				return totalUsers;

			} catch (Exception e) {
				return totalUsers;
			}
		}
		return totalUsers;
	}

	private void createEmptyRow(XSSFSheet sheet) {
		XSSFRow row = sheet.createRow(3);
		XSSFCell cell = row.createCell(0);
		cell.setCellValue(" ");

	}

	private void createEmptyRowWithCellStyle(XSSFSheet sheet, CellStyle cellStyle) {
		XSSFRow row = sheet.createRow(1);
		XSSFCell cell = row.createCell(0);
		cell.setCellStyle(cellStyle);
		cell.setCellValue(" ");
	}

//colors for stacked bar chart,active,through,redistributed campaigns excel sheets
	private static void solidFillSeries(XDDFChartData data, int index, PresetColor color) {
		XDDFSolidFillProperties fill = new XDDFSolidFillProperties(XDDFColor.from(color));
		XDDFChartData.Series series = data.getSeries(index);
		XDDFShapeProperties properties = series.getShapeProperties();
		if (properties == null) {
			properties = new XDDFShapeProperties();
		}
		properties.setFillProperties(fill);
		series.setShapeProperties(properties);
	}

	// for data bars in through campaign excel sheet
	public static void applyDataBars(SheetConditionalFormatting sheetCF, int c, ExtendedColor color) throws Exception {
		CellRangeAddress[] regions = { new CellRangeAddress(1, c, 4, 4) };
		ConditionalFormattingRule rule = sheetCF.createConditionalFormattingRule(color);
		DataBarFormatting dbf = rule.getDataBarFormatting();

		dbf.setWidthMin(0);
		dbf.setWidthMax(100);
		if (dbf instanceof XSSFDataBarFormatting) {
			Field _databar = XSSFDataBarFormatting.class.getDeclaredField("_databar");
			_databar.setAccessible(true);
			org.openxmlformats.schemas.spreadsheetml.x2006.main.CTDataBar ctDataBar = (org.openxmlformats.schemas.spreadsheetml.x2006.main.CTDataBar) _databar
					.get(dbf);
			ctDataBar.setMinLength(0);
			ctDataBar.setMaxLength(100);
		}

		sheetCF.addConditionalFormatting(regions, rule);
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public XtremandResponse saveDownloadRequest(DownloadRequestPostDTO downloadRequestPostDTO) {
		try {
			XtremandResponse response = new XtremandResponse();
			Integer userId = downloadRequestPostDTO.getUserId();
			setPropertiesAndSaveRequest(response, userId);
			return response;
		} catch (DuplicateEntryException e) {
			throw new DuplicateEntryException(e.getMessage());
		} catch (XamplifyDataAccessException e) {
			throw new XamplifyDataAccessException(e);
		} catch (Exception e) {
			throw new XamplifyDataAccessException(e);
		}
	}

	private void setPropertiesAndSaveRequest(XtremandResponse response, Integer userId) {
		DownloadRequest downloadRequest = new DownloadRequest();
		downloadRequest.setDownloadModule(DownloadModule.HIGH_LEVEL_DASHBOARD_ANALYTICS);
		downloadRequest.setDownloadStatus(DownloadStatus.REQUESTED);
		User user = userDao.getUser(userId);
		downloadRequest.setRequestedBy(user);
		downloadRequest.setRequestedOn(new Date());
		downloadRequest.setUpdatedOn(new Date());
		genericDao.save(downloadRequest);
		response.setStatusCode(200);
		response.setMessage("We are processing your download request.We will send an email once it is completed.");
		response.setData(downloadRequest.getId());
	}

	private void setShareLeadsCount(VanityUrlDetailsDTO vanityUrlDetailsDTO,
			List<HighLevelAnalyticsDetailReportDTO> dtos, AccessDetailsDTO accessDetailsDTO) {

		boolean access = utilDao.hasShareLeadsAccessByUserId(vanityUrlDetailsDTO.getUserId());
		LeftSideNavigationBarItem leftSideNavigationBarItem = moduleService.findLeftMenuItems(vanityUrlDetailsDTO);
		boolean isShareLeadsAccess = leftSideNavigationBarItem.isShareLeads();

		if (access) {
			Integer count = highLevelAnalyticsDao
					.getHighLevelAnalyticsDetailReportsForShareLeadsTile(vanityUrlDetailsDTO);
			String description = shareLeads + " generated by their own company";
			dtos.add(setModuleAnalyticsDetails(count, shareLeads, isShareLeadsAccess, shareLeadsIcon,
					shareLeadsAndOnBoardPartnersColor, 7, description));
		}
	}

	private void setInActivePartnersCount(VanityUrlDetailsDTO vanityUrlDetailsDTO,
			List<HighLevelAnalyticsDetailReportDTO> dtos, RoleDisplayDTO roleDisplayDTO,
			AccessDetailsDTO accessDetailsDTO) {
		boolean isPrm = roleDisplayDTO.isPrm() || roleDisplayDTO.isPrmTeamMember() || roleDisplayDTO.isPrmAndPartner()
				|| roleDisplayDTO.isPrmAndPartnerTeamMember();
		String customName = moduleDao.findPartnersModuleCustomNameByUserId(vanityUrlDetailsDTO.getUserId());

		String description = customName
				+ " who didn’t redistributes  at least one campaign  and does not registered with company profile";
		Integer count = highLevelAnalyticsDao
				.getHighLevelAnalyticsDetailReportsForInActivePartners(vanityUrlDetailsDTO);
		dtos.add(setModuleAnalyticsDetails(count, inActive + customName + " Companies", false, partnersIcon,
				contactsColor, 4, description));

	}

	private void setActivePartnersCount(VanityUrlDetailsDTO vanityUrlDetailsDTO,
			List<HighLevelAnalyticsDetailReportDTO> dtos, RoleDisplayDTO roleDisplayDTO,
			AccessDetailsDTO accessDetailsDTO) {
		boolean isPrm = roleDisplayDTO.isPrm() || roleDisplayDTO.isPrmTeamMember() || roleDisplayDTO.isPrmAndPartner()
				|| roleDisplayDTO.isPrmAndPartnerTeamMember();
		String customName = moduleDao.findPartnersModuleCustomNameByUserId(vanityUrlDetailsDTO.getUserId());
		String description = customName + " who redistribute atleast one campaign and with a company profile";
		Integer count = highLevelAnalyticsDao.getHighLevelAnalyticsDetailReportsForActivePartners(vanityUrlDetailsDTO);
		dtos.add(setModuleAnalyticsDetails(count, active + customName + " Companies", false, partnersIcon,
				activePartnerColor, 3, description));

	}

	private void setOnBoardPartnersCount(VanityUrlDetailsDTO vanityUrlDetailsDTO,
			List<HighLevelAnalyticsDetailReportDTO> dtos, RoleDisplayDTO roleDisplayDTO,
			AccessDetailsDTO accessDetailsDTO) {
		String customName = moduleDao.findPartnersModuleCustomNameByUserId(vanityUrlDetailsDTO.getUserId());

		String description = customName + " with Company profile";
		Integer count = highLevelAnalyticsDao.getHighLevelAnalyticsDetailReportsForOnBoardPartners(vanityUrlDetailsDTO);
		dtos.add(setModuleAnalyticsDetails(count, onBoard + customName + " Companies",
				accessDetailsDTO.isPartnerAccess(), partnersIcon, shareLeadsAndOnBoardPartnersColor, 2, description));

	}

	private void setTotalPartnerTile(VanityUrlDetailsDTO vanityUrlDetailsDTO,
			List<HighLevelAnalyticsDetailReportDTO> dtos, RoleDisplayDTO roleDisplayDTO,
			AccessDetailsDTO accessDetailsDTO) {
		String customName = moduleDao.findPartnersModuleCustomNameByUserId(vanityUrlDetailsDTO.getUserId());

		String description = total + " Onboarded " + customName + " Companies(which includes " + customName
				+ " and  Respective " + customName + " Team Members)";
		Integer count = highLevelAnalyticsDao.getHighLevelAnalyticsDetailReportsForTotalPartners(vanityUrlDetailsDTO);
		dtos.add(setModuleAnalyticsDetails(count, total + customName + " Companies", accessDetailsDTO.isPartnerAccess(),
				partnersIcon, partnersColor, 1, description));

	}

	@Override
	public void processHighLevelAnalyticsFailedRequests() {
		List<DownloadRequest> list = highLevelAnalyticsDao.findFailedRequests();
		if (!list.isEmpty() && list != null) {
			startProcessOfFailed(list);
		}
	}

	public void startProcessOfFailed(List<DownloadRequest> lists) {
		for (DownloadRequest request : lists) {
			DownloadRequest updateRequest = highLevelAnalyticsDao.findById(request.getId());
			if (updateRequest.getDownloadStatus() == DownloadStatus.FAILED) {
				Integer userID = updateRequest.getRequestedBy().getUserId();
				VanityUrlDetailsDTO vanityUrlDetailsDTO = new VanityUrlDetailsDTO();
				vanityUrlDetailsDTO.setUserId(userID);
				boolean isTeamMember = teamDao.isTeamMember(userID);
				vanityUrlDetailsDTO.setApplyFilter(isTeamMember);
				asyncComponent.generateHighLevelAnalyticsExcelAndSendEmailNotification(updateRequest.getId(),
						vanityUrlDetailsDTO);
			}

		}
	}

	private String generateShareLeadsForThirdDrill(VanityUrlDetailsDTO vanityUrlDetailsDto, XSSFWorkbook workBook,
			RoleDisplayDTO roleDisplayDTO, Integer userId, CellStyle hlink_style, CreationHelper createHelper) {
		boolean access = utilDao.hasShareLeadsAccessByUserId(vanityUrlDetailsDto.getUserId());
		LeftSideNavigationBarItem leftSideNavigationBarItem = moduleService.findLeftMenuItems(vanityUrlDetailsDto);
		boolean isShareLeadsAccess = leftSideNavigationBarItem.isShareLeads();
		String thirdDrill = "Share Leads Report-3";

		if (access && isShareLeadsAccess) {
			try {
				List<HighLevelAnalyticsShareLeadsDto> thirdShareLeadsList = highLevelAnalyticsDao
						.getShareLeadsForThirdDrill(vanityUrlDetailsDto);

				XSSFSheet shareLeadsSheetForThirdDrill = workBook.createSheet(thirdDrill);
				shareLeadsSheetForThirdDrill.setColumnWidth(0, 8000);
				CellStyle shareLeadsSheetForThirdDrillCellStyle = workBook.createCellStyle();
				shareLeadsSheetForThirdDrillCellStyle.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
				shareLeadsSheetForThirdDrillCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
				shareLeadsSheetForThirdDrillCellStyle.setAlignment(HorizontalAlignment.CENTER);
				Font font = workBook.createFont();
				font.setColor(IndexedColors.BLACK.getIndex());
				font.setBold(true);
				shareLeadsSheetForThirdDrillCellStyle.setFont(font);
				Row shareLeadsForThirdDrillRow = shareLeadsSheetForThirdDrill.createRow((short) 0);

				Cell shareLeadsForThirdDrillCell = shareLeadsForThirdDrillRow.createCell((short) 0);
				shareLeadsSheetForThirdDrill.setColumnWidth(1, 8000);
				shareLeadsForThirdDrillCell.setCellValue("Partner Company Name");
				shareLeadsForThirdDrillCell.setCellStyle(shareLeadsSheetForThirdDrillCellStyle);

				shareLeadsForThirdDrillCell = shareLeadsForThirdDrillRow.createCell((short) 1);
				shareLeadsSheetForThirdDrill.setColumnWidth(2, 8000);
				shareLeadsForThirdDrillCell.setCellValue("User List Name");
				shareLeadsForThirdDrillCell.setCellStyle(shareLeadsSheetForThirdDrillCellStyle);

				shareLeadsForThirdDrillCell = shareLeadsForThirdDrillRow.createCell((short) 2);
				shareLeadsSheetForThirdDrill.setColumnWidth(3, 8000);
				shareLeadsForThirdDrillCell.setCellValue("Leads Email Id");
				shareLeadsForThirdDrillCell.setCellStyle(shareLeadsSheetForThirdDrillCellStyle);

				int thirdShareleadsRowNum = 1;
				CellStyle shareLeadsAlignment = workBook.createCellStyle();
				shareLeadsAlignment.setAlignment(HorizontalAlignment.CENTER);
				for (HighLevelAnalyticsShareLeadsDto shareLeadsDataList : thirdShareLeadsList) {
					shareLeadsForThirdDrillRow = shareLeadsSheetForThirdDrill
							.createRow((short) thirdShareleadsRowNum++);
					shareLeadsForThirdDrillCell = shareLeadsForThirdDrillRow.createCell((short) 0);
					shareLeadsForThirdDrillCell.setCellValue(shareLeadsDataList.getPartnerCompanyName());
					shareLeadsForThirdDrillCell.setCellStyle(shareLeadsAlignment);

					shareLeadsForThirdDrillCell = shareLeadsForThirdDrillRow.createCell((short) 1);
					shareLeadsForThirdDrillCell.setCellValue(shareLeadsDataList.getUserListName());
					shareLeadsForThirdDrillCell.setCellStyle(shareLeadsAlignment);

					shareLeadsForThirdDrillCell = shareLeadsForThirdDrillRow.createCell((short) 2);
					shareLeadsForThirdDrillCell.setCellValue(shareLeadsDataList.getLeadEmailId());
					shareLeadsForThirdDrillCell.setCellStyle(shareLeadsAlignment);

				}
				return thirdDrill;
			} catch (Exception e) {
				return thirdDrill;
			}
		}
		return thirdDrill;
	}

	private String generateShareLeadsForSecondDrill(VanityUrlDetailsDTO vanityUrlDetailsDto, XSSFWorkbook workBook,
			RoleDisplayDTO roleDisplayDTO, Integer userId, CellStyle hlink_style, CreationHelper createHelper) {
		boolean access = utilDao.hasShareLeadsAccessByUserId(vanityUrlDetailsDto.getUserId());
		LeftSideNavigationBarItem leftSideNavigationBarItem = moduleService.findLeftMenuItems(vanityUrlDetailsDto);
		boolean isShareLeadsAccess = leftSideNavigationBarItem.isShareLeads();
		String secondDrill = "Share Leads Report-2";
		String thirdDrill = "Share Leads Report-3";

		if (access && isShareLeadsAccess) {
			try {
				List<HighLevelAnalyticsShareLeadsDto> secondShareLeadsList = highLevelAnalyticsDao
						.getShareLeadsForSecondDrill(vanityUrlDetailsDto);

				XSSFSheet shareLeadsSheetForSecondDrill = workBook.createSheet(secondDrill);
				shareLeadsSheetForSecondDrill.setColumnWidth(0, 8000);
				CellStyle shareLeadsSheetForSecondDrillCellStyle = workBook.createCellStyle();
				shareLeadsSheetForSecondDrillCellStyle.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
				shareLeadsSheetForSecondDrillCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
				shareLeadsSheetForSecondDrillCellStyle.setAlignment(HorizontalAlignment.CENTER);
				Font font = workBook.createFont();
				font.setColor(IndexedColors.BLACK.getIndex());
				font.setBold(true);
				shareLeadsSheetForSecondDrillCellStyle.setFont(font);
				Row shareLeadsForSecondDrillRow = shareLeadsSheetForSecondDrill.createRow((short) 0);

				Cell shareLeadsForSecondDrillCell = shareLeadsForSecondDrillRow.createCell((short) 0);
				shareLeadsSheetForSecondDrill.setColumnWidth(1, 8000);
				shareLeadsForSecondDrillCell.setCellValue("Partner Company Name");
				shareLeadsForSecondDrillCell.setCellStyle(shareLeadsSheetForSecondDrillCellStyle);

				shareLeadsForSecondDrillCell = shareLeadsForSecondDrillRow.createCell((short) 1);
				shareLeadsSheetForSecondDrill.setColumnWidth(2, 8000);
				shareLeadsForSecondDrillCell.setCellValue("User List Name");
				shareLeadsForSecondDrillCell.setCellStyle(shareLeadsSheetForSecondDrillCellStyle);

				shareLeadsForSecondDrillCell = shareLeadsForSecondDrillRow.createCell((short) 2);
				shareLeadsSheetForSecondDrill.setColumnWidth(3, 8000);
				shareLeadsForSecondDrillCell.setCellValue("Share Leads");
				shareLeadsForSecondDrillCell.setCellStyle(shareLeadsSheetForSecondDrillCellStyle);

				int secondShareleadsRowNum = 1;
				CellStyle shareLeadsAlignment = workBook.createCellStyle();
				shareLeadsAlignment.setAlignment(HorizontalAlignment.CENTER);
				for (HighLevelAnalyticsShareLeadsDto shareLeadsDataList : secondShareLeadsList) {
					shareLeadsForSecondDrillRow = shareLeadsSheetForSecondDrill
							.createRow((short) secondShareleadsRowNum++);
					shareLeadsForSecondDrillCell = shareLeadsForSecondDrillRow.createCell((short) 0);
					shareLeadsForSecondDrillCell.setCellValue(shareLeadsDataList.getPartnerCompanyName());
					shareLeadsForSecondDrillCell.setCellStyle(shareLeadsAlignment);

					shareLeadsForSecondDrillCell = shareLeadsForSecondDrillRow.createCell((short) 1);
					shareLeadsForSecondDrillCell.setCellValue(shareLeadsDataList.getUserListName());
					shareLeadsForSecondDrillCell.setCellStyle(shareLeadsAlignment);

					shareLeadsForSecondDrillCell = shareLeadsForSecondDrillRow.createCell((short) 2);
					shareLeadsForSecondDrillCell.setCellValue(shareLeadsDataList.getNumberOfShareLeads());
					shareLeadsForSecondDrillCell.setCellStyle(shareLeadsAlignment);

					Hyperlink leadsListLink = createHelper.createHyperlink(HyperlinkType.DOCUMENT);
					leadsListLink.setAddress("'" + thirdDrill + "'!A1");
					shareLeadsForSecondDrillCell.setHyperlink(leadsListLink);
					shareLeadsForSecondDrillCell.setCellStyle(hlink_style);

				}

				return secondDrill;
			} catch (Exception e) {
				return secondDrill;
			}
		}
		return secondDrill;
	}

	private String generateShareLeads(VanityUrlDetailsDTO vanityUrlDetailsDto, XSSFWorkbook workBook,
			RoleDisplayDTO roleDisplayDTO, Integer userId, CellStyle hlink_style, CreationHelper createHelper) {
		String secondDrill = "Share Leads Report-2";
		String list = "Share Leads Report";
		try {
			List<HighLevelAnalyticsShareLeadsDto> shareLeadsList = highLevelAnalyticsDao
					.getShareLeads(vanityUrlDetailsDto);

			XSSFSheet shareLeadsSheet = workBook.createSheet(list);
			shareLeadsSheet.setColumnWidth(0, 8000);
			CellStyle shareLeadsCellStyle = workBook.createCellStyle();
			shareLeadsCellStyle.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
			shareLeadsCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
			shareLeadsCellStyle.setAlignment(HorizontalAlignment.CENTER);
			Font font = workBook.createFont();
			font.setColor(IndexedColors.BLACK.getIndex());
			font.setBold(true);
			shareLeadsCellStyle.setFont(font);
			Row shareLeadsRow = shareLeadsSheet.createRow((short) 0);

			Cell shareLeadsCell = shareLeadsRow.createCell((short) 0);
			shareLeadsSheet.setColumnWidth(1, 8000);
			shareLeadsCell.setCellValue("Partner Company Name");
			shareLeadsCell.setCellStyle(shareLeadsCellStyle);

			shareLeadsCell = shareLeadsRow.createCell((short) 1);
			shareLeadsSheet.setColumnWidth(2, 8000);
			shareLeadsCell.setCellValue("Leads List");
			shareLeadsCell.setCellStyle(shareLeadsCellStyle);

			int shareleadsRowNum = 1;
			CellStyle shareLeadsAlignment = workBook.createCellStyle();
			shareLeadsAlignment.setAlignment(HorizontalAlignment.CENTER);
			for (HighLevelAnalyticsShareLeadsDto shareLeadsDataList : shareLeadsList) {
				shareLeadsRow = shareLeadsSheet.createRow((short) shareleadsRowNum++);
				shareLeadsCell = shareLeadsRow.createCell((short) 0);
				shareLeadsCell.setCellValue(shareLeadsDataList.getPartnerCompanyName());
				shareLeadsCell.setCellStyle(shareLeadsAlignment);

				BigInteger bigInteger = shareLeadsDataList.getShareLeadsList();
				int toInteger = bigInteger.intValue();
				shareLeadsCell = shareLeadsRow.createCell((short) 1);
				shareLeadsCell.setCellValue(toInteger);
				shareLeadsCell.setCellStyle(shareLeadsAlignment);

				Hyperlink leadsListLink = createHelper.createHyperlink(HyperlinkType.DOCUMENT);
				leadsListLink.setAddress("'" + secondDrill + "'!A1");
				shareLeadsCell.setHyperlink(leadsListLink);
				shareLeadsCell.setCellStyle(hlink_style);

			}
			return list;

		} catch (Exception e) {
			return list;
		}
	}

}