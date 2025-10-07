package com.xtremand.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.net.ssl.HttpsURLConnection;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.DocumentFont;
import com.itextpdf.text.pdf.PdfArray;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfDictionary;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfString;
import com.itextpdf.text.pdf.parser.ImageRenderInfo;
import com.itextpdf.text.pdf.parser.PdfReaderContentParser;
import com.itextpdf.text.pdf.parser.RenderListener;
import com.itextpdf.text.pdf.parser.TextRenderInfo;
import com.itextpdf.text.pdf.parser.Vector;
import com.xtremand.aws.FilePathAndThumbnailPath;
import com.xtremand.campaign.bom.ModuleAccess;
import com.xtremand.campaign.bom.ModuleAccessDTO;
import com.xtremand.common.bom.CompanyProfile;
import com.xtremand.common.bom.Pagination;
import com.xtremand.common.bom.Pagination.SORTINGORDER;
import com.xtremand.dam.bom.Dam;
import com.xtremand.dam.bom.DamPartner;
import com.xtremand.dam.bom.DamStatusEnum;
import com.xtremand.dam.dto.DamUploadPostDTO;
import com.xtremand.formbeans.UserDTO;
import com.xtremand.mdf.bom.MdfAmountType;
import com.xtremand.mdf.bom.MdfWorkFlowStepType;
import com.xtremand.mdf.dto.MdfRequestViewDTO;
import com.xtremand.team.member.dto.TeamMemberModuleDTO;
import com.xtremand.user.bom.Role;
import com.xtremand.user.bom.User;
import com.xtremand.util.dto.XamplifyConstants;
import com.xtremand.vanity.url.dto.UrlDTO;
import com.xtremand.video.bom.CallAction;
import com.xtremand.video.bom.VideoCategory;
import com.xtremand.video.bom.VideoControl;
import com.xtremand.video.bom.VideoFile;
import com.xtremand.video.bom.VideoFile.TYPE;
import com.xtremand.video.bom.VideoFile.VideoStatus;
import com.xtremand.video.bom.VideoImage;
import com.xtremand.video.bom.VideoTag;
import com.xtremand.white.labeled.dto.DamVideoDTO;

@Component
public class XamplifyUtil {

	private static final String DUMMY_CO_BRANDING_TAG = "href=\"https://dummycobrandingurl.com\"";

	private static final String DEFAULT_VIDEO_THUMBNAIL_TAG = "href=\"https://dummyurl.com\"";

	private static final String JAVA_SCRIPT_VOID = XamplifyConstants.JAVA_SCRIPT_VOID;

	private static final String ALL = "All";

	private static final String ALL_FA_ICON = "fa fa-check-circle";

	private static final String VIDEO = "Video";

	private static final String VIDEO_ICON = "fa fa-file-video-o";

	private static final String STATS = "Stats";

	private static final String STATS_ICON = "fa fa-line-chart";

	private static final String PARTNERS_ICON = "fa fa-handshake-o";

	private static final String SHARE_LEADS_OR_SHARED_LEADS_ICON = "fa fa-id-badge";

	private static final String MDF = "MDF";

	private static final String MDF_ICON = "fa fa-dollar";

	private static final String DAM = "DAM";

	private static final String DAM_ICON = "fa fa-file-pdf-o";

	private static final String LEARNING_TRACK = "Track Builder";

	private static final String LEARNING_TRACK_ICON = "fa fa-graduation-cap";

	private static final String OPPORTUNITIES = "Opportunities";

	private static final String OPPORTUNITIES_ICON = "fa fa-money";

	private static final String PLAY_BOOK = "Play Book";

	private static final String PLAY_BOOK_ICON = "fa fa-book";

	private static final String COLOR_API_BASE = "https://xxxxx";

	@Value("${newRequest.open}")
	private String newRequest;

	@Value("${inProgress}")
	private String inProgress;

	@Value("${preApproved}")
	private String preApproved;

	@Value("${reimbursement.issued}")
	private String reimburseMentIssued;

	@Value("${reimbursement.declined}")
	private String reimburseMentDeclined;

	@Value("${request.expired}")
	private String requestExpired;

	@Value("${request.declined}")
	private String requestDeclined;

	@Value("${default.avatar}")
	private String defaultAvatar;

	@Value("${images.folder}")
	private String imagesFolder;

	@Value("${server_url}")
	private String serverUrl;

	@Value("${web_url}")
	String webUrl;

	@Value("${server_path}")
	String serverPath;

	@Value("${company.logo.url}")
	private String companyLogoDefaultUrl;

	@Value("${dev.cobranding.image}")
	String devCoBrandingImage;

	@Value("${prod.cobranding.image}")
	String prodCoBrandingImage;

	@Value("${release.cobranding.image}")
	String releaseCoBrandingImage;

	@Value("${parntershipCreatedOnUtilSortQuery}")
	private String parntershipCreatedOnUtilSortQuery;

	@Value("${partnerCompanyNameUtilSortQuery}")
	private String partnerCompanyNameUtilSortQuery;

	@Value("${partnerEmailIdUtilSortQuery}")
	private String partnerEmailIdUtilSortQuery;

	@Value("${partnerFistNameUtilSortQuery}")
	private String partnerFistNameUtilSortQuery;

	@Value("${partnerLastNameUtilSortQuery}")
	private String partnerLastNameUtilSortQuery;

	@Value("${csv.content.path}")
	private String csvThumbnailPath;

	@Value("${cvs.content.path}")
	private String cvsThumbnailPath;

	@Value("${default.content.path}")
	private String defaultThumbnailPath;

	@Value("${gif.content.path}")
	private String gifThumbnailPath;

	@Value("${html.content.path}")
	private String htmlThumbnailPath;

	@Value("${doc.content.path}")
	private String docThumbnailPath;

	@Value("${pdfs.content.path}")
	private String pdfThumbnailPath;

	@Value("${ppt.content.path}")
	private String pptThumbnailPath;

	@Value("${pct.content.path}")
	private String pctThumbnailPath;

	@Value("${pptx.content.path}")
	private String pptxThumbnailPath;

	@Value("${txt.content.path}")
	private String txtThumbnailPath;

	@Value("${xls.content.path}")
	private String xlsThumbnailPath;

	@Value("${xlsm.content.path}")
	private String xlsmThumbnailPath;

	@Value("${xml.content.path}")
	private String xmlThumbnailPath;

	@Value("${zip.content.path}")
	private String zipThumbnailPath;

	@Value("${documents.content.path}")
	private String documentsThumbnailPath;

	@Value("${xps.content.path}")
	private String xpsThumbnailPath;

	@Value("${rtf.content.path}")
	private String rtfThumbnailPath;

	@Value("${odt.content.path}")
	private String odtThumbnailPath;

	@Value("${wps.content.path}")
	private String wpsThumbnailPath;

	@Value("${htm.content.path}")
	private String htmThumbnailPath;

	@Value("${mht.content.path}")
	private String mhtThumbnailPath;

	@Value("${log.content.path}")
	private String logThumbnailPath;

	@Value("${mp3.content.path}")
	private String mp3ThumbnailPath;

	@Value("${mhtml.content.path}")
	private String mhtmlThumbnailPath;

	@Value("${rar.content.path}")
	private String rarThumbnailPath;

	@Value("${apk.content.path}")
	private String apkThumbnailPath;

	@Value("${viewsCountOrderByQuery}")
	private String viewsCountOrderByQuery;

	@Value("${downloadCountOrderByQuery}")
	private String downloadCountOrderByQuery;

	@Value("${amazon.email.templates.folder}")
	String amazonEmailTemplatesFolder;

	@Value("${amazon.white.labeled.email.templates.folder}")
	private String amazonWhiteLabeledEmailTemplatesFolder;

	@Value("${amazon.white.labeled.landing.pages.folder}")
	private String amazonWhiteLabeledLandingPagesFolder;

	@Value("${amazon.custom.email.templates.folder}")
	private String amazonCustomEmailTemplatesFolder;

	@Value("${amazon.landing.pages.folder}")
	String amazonLandingPagesFolder;

	@Value("${amazon.dam.bee.thumbnail.folder}")
	String amazonDamBeeThumbnailFolder;

	@Value("${amazon.campaign.email.templates.folder}")
	String amazonCampaignEmailTemplatesFolder;

	@Value("${amazon.previews.folder}")
	String amazonPreviewFolder;

	@Value("${partner.email.template}")
	String partnerEmailTemplate;

	@Value("${devImagesHost}")
	private String devImagesHost;

	@Value("${dev.host}")
	String qaHost;

	@Value("${prod.host}")
	String productionHost;

	@Value("${spring.profiles.active}")
	private String profiles;

	@Value("${amazon.high.level.analytics.folder}")
	private String amazonHighLevelAnalyticsFolder;

	@Value("${domain}")
	private String domain;

	@Value("${bee.message.key}")
	String beeKey;

	@Value("${DEFAULT_DASHBOARD_BANNER_IMAGE_PATH}")
	private String defaultDashboardBannerImagePath;

	@Value("${DEFAULT_HELP_IMAGE_PATH}")
	private String deafultHelpImagePath;

	@Value("${help.description}")
	private String helpDescription;

	@Value("${rsvp.yes}")
	String rsvpYes;

	@Value("${event.utm.public}")
	String eventUtmCode;

	@Value("${zero.bounce.api.enabled}")
	private String isZeroBounceApiEnabled;

	// XNFR-833
	@Value("${amazon.access.id}")
	String amazonAccessKey;

	@Value("${amazon.base.url}")
	String amazonBaseUrl;

	@Value("${amazon.secret.key}")
	String amazonsecretKey;

	@Value("${amazon.env.folder}")
	String amazonEnvFolder;

	@Autowired
	private Environment environment;

	@Value("${media_base_path}")
	String mediaBasePath;

	@Value("${server_path}")
	public String server_path;

	// XNFR-833 END

	/***** XNFR-930 ***/
	@Value("${amazon.cloudfront.url}")
	String amazonClodFrontUrl;

	@Value("${amazon.cloudfront.enabled}")
	boolean amazonClodFrontEnabled;

	/***** XNFR-930 ***/

	public List<String> convertStringToArrayListWithCommaSeperator(String string) {
		return new ArrayList<>(Arrays.asList(string.toLowerCase().replace(" ", "").trim().split(",")));
	}

	public String getThumbnailPathByFileType(String fileType) {
		String thumbnailPath = "";
		switch (fileType.toLowerCase()) {
		case "csv":
			thumbnailPath = csvThumbnailPath;
			break;

		case "cvs":
			thumbnailPath = cvsThumbnailPath;
			break;

		case "gif":
			thumbnailPath = gifThumbnailPath;
			break;

		case "html":
			thumbnailPath = htmlThumbnailPath;
			break;

		case "doc":
			thumbnailPath = docThumbnailPath;
			break;

		case "pdf":
			thumbnailPath = pdfThumbnailPath;
			break;

		case "ppt":
			thumbnailPath = pptThumbnailPath;
			break;

		case "pct":
			thumbnailPath = pctThumbnailPath;
			break;

		case "pptx":
			thumbnailPath = pptxThumbnailPath;
			break;

		case "txt":
			thumbnailPath = txtThumbnailPath;
			break;

		case "xls":
			thumbnailPath = xlsThumbnailPath;
			break;

		case "xlsx":
			thumbnailPath = xlsmThumbnailPath;
			break;

		case "xlsm":
			thumbnailPath = xlsmThumbnailPath;
			break;

		case "xml":
			thumbnailPath = xmlThumbnailPath;
			break;

		case "zip":
			thumbnailPath = zipThumbnailPath;
			break;

		case "docx":
			thumbnailPath = documentsThumbnailPath;
			break;

		case "docm":
			thumbnailPath = documentsThumbnailPath;
			break;

		case "dotm":
			thumbnailPath = documentsThumbnailPath;
			break;

		case "dotx":
			thumbnailPath = documentsThumbnailPath;
			break;

		case "dot":
			thumbnailPath = documentsThumbnailPath;
			break;

		case "xps":
			thumbnailPath = xpsThumbnailPath;
			break;

		case "rtf":
			thumbnailPath = rtfThumbnailPath;
			break;

		case "odt":
			thumbnailPath = odtThumbnailPath;
			break;

		case "wps":
			thumbnailPath = wpsThumbnailPath;
			break;

		case "htm":
			thumbnailPath = htmThumbnailPath;
			break;

		case "mht":
			thumbnailPath = mhtThumbnailPath;
			break;

		case "log":
			thumbnailPath = logThumbnailPath;
			break;

		case "mp3":
			thumbnailPath = mp3ThumbnailPath;
			break;

		case "mhtml":
			thumbnailPath = mhtmlThumbnailPath;
			break;

		case "rar":
			thumbnailPath = rarThumbnailPath;
			break;

		case "apk":
			thumbnailPath = apkThumbnailPath;
			break;

		default:
			thumbnailPath = defaultThumbnailPath;
			break;
		}
		return thumbnailPath;
	}

	public String getMdfRequestStatusInString(String status) {
		if (MdfWorkFlowStepType.NEW_REQUEST.name().equals(status)) {
			status = newRequest;
		} else if (MdfWorkFlowStepType.IN_PROGRESS.name().equals(status)) {
			status = inProgress;
		} else if (MdfWorkFlowStepType.PRE_APPROVED.name().equals(status)) {
			status = preApproved;
		} else if (MdfWorkFlowStepType.REIMBURSEMENT_ISSUED.name().equals(status)) {
			status = reimburseMentIssued;
		} else if (MdfWorkFlowStepType.REIMBURSEMENT_DECLINED.name().equals(status)) {
			status = reimburseMentDeclined;
		} else if (MdfWorkFlowStepType.REQUEST_EXPIRED.name().equals(status)) {
			status = requestExpired;
		} else if (MdfWorkFlowStepType.REQUEST_DECLINED.name().equals(status)) {
			status = requestDeclined;
		}
		return status;
	}

	public void getMdfRequestStatusInStringAndNumber(String status, MdfRequestViewDTO mdfRequestViewDTO) {
		Integer statusInInteger = 0;
		String statusInString = "";
		if (MdfWorkFlowStepType.NEW_REQUEST.name().equals(status)) {
			statusInString = newRequest;
			statusInInteger = 1;
		} else if (MdfWorkFlowStepType.IN_PROGRESS.name().equals(status)) {
			statusInString = inProgress;
			statusInInteger = 2;
		} else if (MdfWorkFlowStepType.PRE_APPROVED.name().equals(status)) {
			statusInString = preApproved;
			statusInInteger = 3;
		} else if (MdfWorkFlowStepType.REIMBURSEMENT_ISSUED.name().equals(status)) {
			statusInString = reimburseMentIssued;
			statusInInteger = 4;
		} else if (MdfWorkFlowStepType.REIMBURSEMENT_DECLINED.name().equals(status)) {
			statusInString = reimburseMentDeclined;
			statusInInteger = 5;
		} else if (MdfWorkFlowStepType.REQUEST_EXPIRED.name().equals(status)) {
			statusInString = requestExpired;
			statusInInteger = 6;
		} else if (MdfWorkFlowStepType.REQUEST_DECLINED.name().equals(status)) {
			statusInString = requestDeclined;
			statusInInteger = 7;
		}
		mdfRequestViewDTO.setMdfWorkFlowStepTypeInString(statusInString);
		mdfRequestViewDTO.setStatusInInteger(statusInInteger);

	}

	public String getCompleteImagePath(String picturePath) {
		if (StringUtils.hasText(picturePath)) {
			if (picturePath.startsWith("http") || picturePath.startsWith("https")) {
				return picturePath.replace("http:", "https:");
			} else {
				String prefixUrl = "";
				if (serverUrl.indexOf("localhost") > -1) {
					prefixUrl = getImagesPrefixPath() + picturePath;
				} else {
					prefixUrl = serverUrl + imagesFolder + picturePath;
				}
				return prefixUrl;
			}
		} else {
			return defaultAvatar;
		}
	}

	public String getMdfAmountTypeInString(String mdfAmountType) {
		if (MdfAmountType.FUND_ADDED.name().equals(mdfAmountType)) {
			return "Added";
		} else if (MdfAmountType.FUND_REMOVED.name().equals(mdfAmountType)) {
			return "Removed";
		} else {
			return "";
		}
	}

	public Set<String> getHrefAndIframeLinks(String content) {
		if (StringUtils.hasText(content)) {
			Document doc = Jsoup.parse(content);
			Elements links = doc.select("a[href]");
			Elements iframes = doc.select("iframe");
			Set<String> ahrefTags = links.stream().map(element -> element.attr("href")).collect(Collectors.toSet());
			Set<String> iframeSources = iframes.stream().map(iframe -> iframe.attr("src")).collect(Collectors.toSet());
			Set<String> allTags = new HashSet<>();
			allTags.addAll(ahrefTags);
			allTags.addAll(iframeSources);
			allTags.remove(null);
			allTags.remove("");
			return allTags;
		} else {
			return new HashSet<>();
		}
	}

	public UrlDTO getUrlDetails(String vanityUrlDomain, boolean vendorJourney, boolean isMasterLandingPage,
			boolean partnerJourneyPage, boolean isVendorMarketplacePage, Integer masterLandingPageId,
			Integer vendorMarketplacePageId) {
		UrlDTO urlDTO = new UrlDTO();
		String baseUrl = "";
		if (vendorJourney) {
			if (masterLandingPageId != null) {
				baseUrl = webUrl + "mlvjf/" + masterLandingPageId + "/";
			} else {
				baseUrl = webUrl + "vjf/";
			}
		} else if (isMasterLandingPage) {
			baseUrl = webUrl + "mlpf/";
		} else if (partnerJourneyPage) {
			if (vendorMarketplacePageId != null) {
				baseUrl = webUrl + "vmppjf/" + vendorMarketplacePageId + "/";
			} else {
				baseUrl = webUrl + "pjpf/";
			}
		} else if (isVendorMarketplacePage) {
			baseUrl = webUrl + "vmpf/";
		} else {
			baseUrl = webUrl + "f/";
		}
		urlDTO.setVanityUrl(frameVanityURL(baseUrl, vanityUrlDomain));
		urlDTO.setUrl(baseUrl);
		return urlDTO;
	}

	public String frameVanityURL(String baseClientURL, String companyProfileName) {
		return baseClientURL;
	}

	public String replaceCompanyLogo(String htmlBody, String companyLogo) {
		String body = htmlBody;
		body = body.replaceAll(companyLogoDefaultUrl, serverPath + XamplifyUtils.escapeDollarSequece(companyLogo));
		return body;
	}

	public String replaceCompanyLogoAndCoBrandedLogo(String htmlBody, String partnerCompanyLogoPath,
			String vendorCompanyLogoPath, String partnerCompanyWebSiteUrl) {
		String updatedTemplateBody = replaceCompanyLogo(htmlBody, vendorCompanyLogoPath);
		String updatedPartnerCompanyLogoPath = serverPath + XamplifyUtils.escapeDollarSequece(partnerCompanyLogoPath);
		updatedTemplateBody = replaceCoBrandingLogoImagePath(updatedPartnerCompanyLogoPath, updatedTemplateBody,
				partnerCompanyWebSiteUrl);
		return updatedTemplateBody;
	}

	public String replaceCoBrandingLogoImagePath(String updatedPartnerCompanyLogoPath, String updatedTemplateBody,
			String companyWebsite) {
		if (StringUtils.hasText(updatedPartnerCompanyLogoPath)) {
			updatedTemplateBody = updatedTemplateBody.replaceAll(devCoBrandingImage, updatedPartnerCompanyLogoPath);
			updatedTemplateBody = updatedTemplateBody.replaceAll(prodCoBrandingImage, updatedPartnerCompanyLogoPath);
			updatedTemplateBody = updatedTemplateBody.replaceAll(releaseCoBrandingImage, updatedPartnerCompanyLogoPath);
			boolean isValidCompanyWebSiteUrl = StringUtils.hasText(companyWebsite) && companyWebsite != null;
			companyWebsite = addHttpOrHttpsProtocolAsPrefix(companyWebsite);
			String updatedCompanyWebsite = "href=\"" + companyWebsite + "\"";
			String coBrandingUrl = isValidCompanyWebSiteUrl ? updatedCompanyWebsite : JAVA_SCRIPT_VOID;
			updatedTemplateBody = updatedTemplateBody.replaceAll(DUMMY_CO_BRANDING_TAG, coBrandingUrl);
		}
		return updatedTemplateBody;
	}

	public String getSelectedSortOptionForPartners(Pagination pagination) {
		String sortOptionQueryString = "";
		if (StringUtils.hasText(pagination.getSortcolumn())) {
			if ("firstName".equals(pagination.getSortcolumn())) {
				sortOptionQueryString += partnerFistNameUtilSortQuery + pagination.getSortingOrder();
				sortOptionQueryString = setNullConditionsForAscOrDesc(pagination, sortOptionQueryString);
			} else if ("lastName".equals(pagination.getSortcolumn())) {
				sortOptionQueryString += partnerLastNameUtilSortQuery + pagination.getSortingOrder();
				sortOptionQueryString = setNullConditionsForAscOrDesc(pagination, sortOptionQueryString);
			} else if ("emailId".equals(pagination.getSortcolumn())) {
				sortOptionQueryString += partnerEmailIdUtilSortQuery + pagination.getSortingOrder();
			} else if ("companyName".equals(pagination.getSortcolumn())) {
				sortOptionQueryString += partnerCompanyNameUtilSortQuery + pagination.getSortingOrder();
			} else if ("createdTime".equals(pagination.getSortcolumn())) {
				sortOptionQueryString += parntershipCreatedOnUtilSortQuery + pagination.getSortingOrder();
			}
		} else {
			sortOptionQueryString += parntershipCreatedOnUtilSortQuery + " desc";
		}
		return sortOptionQueryString;
	}

	public String getSelectedSortOptionForPublishedPartnersAnalytics(Pagination pagination) {
		String sortOptionQueryString = "";
		if (StringUtils.hasText(pagination.getSortcolumn())) {
			if ("firstName".equals(pagination.getSortcolumn())) {
				sortOptionQueryString += partnerFistNameUtilSortQuery + pagination.getSortingOrder();
				sortOptionQueryString = setNullConditionsForAscOrDesc(pagination, sortOptionQueryString);
			} else if ("lastName".equals(pagination.getSortcolumn())) {
				sortOptionQueryString += partnerLastNameUtilSortQuery + pagination.getSortingOrder();
				sortOptionQueryString = setNullConditionsForAscOrDesc(pagination, sortOptionQueryString);
			} else if ("emailId".equals(pagination.getSortcolumn())) {
				sortOptionQueryString += partnerEmailIdUtilSortQuery + pagination.getSortingOrder();
			} else if ("companyName".equals(pagination.getSortcolumn())) {
				sortOptionQueryString += partnerCompanyNameUtilSortQuery + pagination.getSortingOrder();
			} else if ("createdTime".equals(pagination.getSortcolumn())) {
				sortOptionQueryString += parntershipCreatedOnUtilSortQuery + pagination.getSortingOrder();
			} else if ("views".equals(pagination.getSortcolumn())) {
				sortOptionQueryString += viewsCountOrderByQuery + pagination.getSortingOrder();
			} else if ("downloads".equals(pagination.getSortcolumn())) {
				sortOptionQueryString += downloadCountOrderByQuery + pagination.getSortingOrder();
			}
		} else {
			sortOptionQueryString += parntershipCreatedOnUtilSortQuery + " desc";
		}
		return sortOptionQueryString;
	}

	public String setNullConditionsForAscOrDesc(Pagination pagination, String sortOptionQueryString) {
		if (SORTINGORDER.ASC.name().equals(pagination.getSortingOrder())) {
			sortOptionQueryString += " nulls first";
		} else {
			sortOptionQueryString += " nulls last";
		}
		return sortOptionQueryString;
	}

	public String getSubFolderPath(String type) {
		String subFolderPath = "";
		if (StringUtils.hasText(type)) {
			if ("email_template".equals(type)) {
				subFolderPath = amazonEmailTemplatesFolder;
			} else if ("landing_page".equals(type)) {
				subFolderPath = amazonLandingPagesFolder;
			} else if ("campaign_email_template".equals(type) || partnerEmailTemplate.equals(type)) {
				subFolderPath = amazonCampaignEmailTemplatesFolder;
			} else if ("dam".equals(type)) {
				subFolderPath = amazonDamBeeThumbnailFolder;
			} else if ("whiteLabeledEmailTemplates".equals(type)) {
				subFolderPath = amazonWhiteLabeledEmailTemplatesFolder;
			} else if ("whiteLabeledLandingPages".equals(type)) {
				subFolderPath = amazonWhiteLabeledLandingPagesFolder;
			} else if ("custom_email_templates".equals(type)) {
				subFolderPath = amazonCustomEmailTemplatesFolder;
			}
		}
		return subFolderPath;
	}

	public String getImagesPrefixPath() {
		String imagesPrefixHost = "";
		if (isDev()) {
			imagesPrefixHost = devImagesHost;
		} else if (isQA()) {
			imagesPrefixHost = qaHost + imagesFolder;
		} else if (isProduction()) {
			imagesPrefixHost = productionHost + imagesFolder;
		}
		return imagesPrefixHost;
	}

	public boolean isDev() {
		return "dev".equals(profiles);
	}

	public boolean isQA() {
		return "qa".equals(profiles);
	}

	public boolean isRelease() {
		return "release".equals(profiles);
	}

	public boolean isProduction() {
		return "production".equals(profiles);
	}

	public boolean isStage() {
		return "stage".equals(profiles);
	}

	/*** XNFR-127 ******/
	public void addItemsToArrayList(List<List<Object>> lists, String moduleName, Object count) {
		List<Object> list = new ArrayList<>();
		list.add(moduleName);
		list.add(count);
		lists.add(list);
	}

	public void addAllModule(List<TeamMemberModuleDTO> modules, List<Integer> assignedModuleIds) {
		modules.add(new TeamMemberModuleDTO(ALL, ALL_FA_ICON, "", Role.ALL_ROLES.getRoleId(),
				assignedModuleIds.indexOf(Role.ALL_ROLES.getRoleId()) > -1));
	}

	public void addOpportunityModule(List<TeamMemberModuleDTO> modules, List<Integer> assignedModuleIds) {
		modules.add(new TeamMemberModuleDTO(OPPORTUNITIES, OPPORTUNITIES_ICON, "Create leads/deals",
				Role.OPPORTUNITY.getRoleId(), assignedModuleIds.indexOf(Role.OPPORTUNITY.getRoleId()) > -1));
	}

	public void addShareLeadsModule(List<TeamMemberModuleDTO> modules, List<Integer> assignedModuleIds,
			String moduleName, String message) {
		modules.add(new TeamMemberModuleDTO(moduleName, SHARE_LEADS_OR_SHARED_LEADS_ICON, message,
				Role.SHARE_LEADS.getRoleId(), assignedModuleIds.indexOf(Role.SHARE_LEADS.getRoleId()) > -1));
	}

	public void addStatsModule(List<TeamMemberModuleDTO> modules, List<Integer> assignedModuleIds) {
		modules.add(new TeamMemberModuleDTO(STATS, STATS_ICON, "View all statistics", Role.STATS_ROLE.getRoleId(),
				assignedModuleIds.indexOf(Role.STATS_ROLE.getRoleId()) > -1));
	}

	public void addVideosModule(List<TeamMemberModuleDTO> modules, List<Integer> assignedModuleIds) {
		modules.add(new TeamMemberModuleDTO(VIDEO, VIDEO_ICON, "", Role.VIDEO_UPLOAD_ROLE.getRoleId(),
				assignedModuleIds.indexOf(Role.VIDEO_UPLOAD_ROLE.getRoleId()) > -1));
	}

	public void addPartnersModule(List<TeamMemberModuleDTO> modules, List<Integer> assignedModuleIds,
			String partnerCustomModuleName) {
		modules.add(new TeamMemberModuleDTO(partnerCustomModuleName, PARTNERS_ICON, "", Role.PARTNERS.getRoleId(),
				assignedModuleIds.indexOf(Role.PARTNERS.getRoleId()) > -1));
	}

	public void addPlayBookModule(List<TeamMemberModuleDTO> modules, List<Integer> assignedModuleIds) {
		modules.add(new TeamMemberModuleDTO(PLAY_BOOK, PLAY_BOOK_ICON, "Create/Manage play books",
				Role.PLAY_BOOK.getRoleId(), assignedModuleIds.indexOf(Role.PLAY_BOOK.getRoleId()) > -1));
	}

	public void addLmsModule(List<TeamMemberModuleDTO> modules, List<Integer> assignedModuleIds) {
		modules.add(new TeamMemberModuleDTO(LEARNING_TRACK, LEARNING_TRACK_ICON, "Create/Manage learning tracks",
				Role.LEARNING_TRACK.getRoleId(), assignedModuleIds.indexOf(Role.LEARNING_TRACK.getRoleId()) > -1));
	}

	public void addDamModule(List<TeamMemberModuleDTO> modules, List<Integer> assignedModuleIds) {
		modules.add(new TeamMemberModuleDTO(DAM, DAM_ICON, "Create/Manage assets", Role.DAM.getRoleId(),
				assignedModuleIds.indexOf(Role.DAM.getRoleId()) > -1));
	}

	public void addMdfModule(List<TeamMemberModuleDTO> modules, List<Integer> assignedModuleIds,
			String mdfDescription) {
		modules.add(new TeamMemberModuleDTO(MDF, MDF_ICON, mdfDescription, Role.MDF.getRoleId(),
				assignedModuleIds.indexOf(Role.MDF.getRoleId()) > -1));
	}

	/**** XNFR-83 ******/
	public String getVaniryOrXamplifyDomainUrl(String companyProfileName) {
		return companyProfileName != null && StringUtils.hasText(companyProfileName)
				? "https://" + companyProfileName + "." + domain
				: webUrl;
	}

	/**** XNFR-83 ******/
	public String getProfilePicturePrefixPath(String profileImage) {
		String updatedPath = "";
		if (profileImage != null) {
			if (profileImage.startsWith("http:")) {
				updatedPath = profileImage.replace("http:", "https:");
			} else if (profileImage.startsWith("images/")) {
				updatedPath = getImagesPrefixPath();
			}
		}
		return updatedPath;
	}

	public HttpsURLConnection callBeeRestApi(String htmlBody) {
		HttpsURLConnection connection = null;
		try {
			connection = establishBeeConnection();
			Map<String, Object> request = new HashMap<>();
			request.put("html", htmlBody);
			request.put("width", 512);
			request.put("height", 1000);
			request.put("file_type", "png");

			ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
			String requestBody = ow.writeValueAsString(request);
			OutputStreamWriter wr = new OutputStreamWriter(connection.getOutputStream());
			wr.write(requestBody);
			wr.flush();
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return connection;

	}

	public HttpsURLConnection generateImageByHtmlBody(String htmlBody) {
		HttpsURLConnection connection = null;
		try {
			connection = establishBeeConnection();
			Map<String, Object> request = new HashMap<>();
			request.put("html", htmlBody);
			request.put("file_type", "png");

			ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
			String requestBody = ow.writeValueAsString(request);
			OutputStreamWriter wr = new OutputStreamWriter(connection.getOutputStream());
			wr.write(requestBody);
			wr.flush();
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return connection;

	}

	public HttpsURLConnection establishBeeConnection() {
		String endpoint = "https://api.getbee.io/v1/message/image";
		URL url = null;
		HttpsURLConnection connection = null;
		try {
			url = new URL(endpoint);
			connection = (HttpsURLConnection) url.openConnection();
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-type", "application/json");
			connection.setDoOutput(true);
			connection.setRequestProperty("Authorization", "Bearer " + beeKey);
			connection.connect();
		} catch (MalformedURLException e) {
			System.out.println("Internet is not connected");
		} catch (ProtocolException e) {
			System.out.println("Internet is not connected");
		} catch (IOException e) {
			System.out.println("Internet is not connected");
		}
		return connection;
	}

	/******** XNFR-255 *********/
	public VideoFile setWhiteLabeledVideoFileProperties(DamVideoDTO damVideoDTO, User partner) {
		VideoFile videoFile = new VideoFile();
		BeanUtils.copyProperties(damVideoDTO, videoFile);
		VideoCategory videoCategory = new VideoCategory();
		videoCategory.setId(108);
		videoFile.setCategory(videoCategory);
		videoFile.setTitle(damVideoDTO.getAssetName());
		videoFile.setUri(damVideoDTO.getVideoUri());
		videoFile.setCustomer(partner);
		videoFile.setCreatedTime(new Date());
		videoFile.setUpdatedTime(new Date());
		videoFile.setUpdatedBy(partner.getUserId());
		videoFile.setAlias(String.valueOf(UUID.randomUUID()));
		videoFile.setVideoID(damVideoDTO.getVideoIdAsString());
		videoFile.setVideoStatus(VideoStatus.APPROVED);
		videoFile.setViewBy(TYPE.PRIVATE);
		videoFile.setVideoSize(damVideoDTO.getVideoSize().doubleValue());
		videoFile.setBitrate(damVideoDTO.getBitRate().doubleValue());
		videoFile.setViews(0);

		VideoImage videoImage = new VideoImage(damVideoDTO.getImage1(), damVideoDTO.getImage2(),
				damVideoDTO.getImage3(), damVideoDTO.getGif1(), damVideoDTO.getGif2(), damVideoDTO.getGif3());
		videoFile.setVideoImage(videoImage);
		videoImage.setVideoFile(videoFile);

		VideoControl videoControl = new VideoControl(damVideoDTO.getPlayerColor(),
				damVideoDTO.isEnableVideoController(), damVideoDTO.getControllerColor(), damVideoDTO.isAllowSharing(),
				damVideoDTO.isEnableSettings(), damVideoDTO.isAllowFullScreen(), damVideoDTO.isAllowComments(),
				damVideoDTO.isAllowLikes(), damVideoDTO.isEnableCasting(), damVideoDTO.isAllowEmbed(),
				damVideoDTO.getTransparency(), damVideoDTO.is360Video(), damVideoDTO.isDefaultSetting(),
				damVideoDTO.getBrandingLogoUri(), damVideoDTO.getBrandingLogoDescUri(),
				damVideoDTO.isEnableVideoCobrandingLogo());
		videoFile.setVideoControl(videoControl);
		videoControl.setVideoFile(videoFile);
		CallAction callAction = new CallAction(damVideoDTO.isCallToActionName(), damVideoDTO.isSkip(),
				damVideoDTO.getUpperText(), damVideoDTO.getLowerText(), damVideoDTO.isStartOfVideo(),
				damVideoDTO.isEndOfVideo(), damVideoDTO.isCallAction());
		videoFile.setCallAction(callAction);
		callAction.setVideoFile(videoFile);
		List<String> tags = damVideoDTO.getTags();
		if (tags != null && !tags.isEmpty()) {
			for (String tag : tags) {
				videoFile.getVideoTags().add(new VideoTag(videoFile.getId(), tag));
			}
		}
		return videoFile;
	}

	/******** XNFR-255 *********/
	public Dam setWhiteLabeledDamProperties(DamVideoDTO damVideoDTO, Integer partnerCompanyId, Integer partnerId,
			VideoFile videoFile) {
		Dam dam = new Dam();
		dam.setAssetName(videoFile.getTitle());
		dam.setSlug(videoFile.getTitle().replaceAll(" ", "_"));
		CompanyProfile partnerCompany = new CompanyProfile();
		partnerCompany.setId(partnerCompanyId);
		dam.setCompanyProfile(partnerCompany);
		dam.setDescription(damVideoDTO.getDescription());
		dam.setAssetType(damVideoDTO.getAssetType());
		dam.setAlias(XamplifyUtils.generateAlias());
		dam.setCreatedTime(new Date());
		dam.setCreatedBy(partnerId);
		dam.setUpdatedTime(new Date());
		dam.setUpdatedBy(partnerId);
		dam.setVideoFile(videoFile);
		String assetStatus = damVideoDTO.getAssetStatus();
		if (DamStatusEnum.COMPLETED.name().equals(assetStatus)) {
			dam.setDamStatusEnum(DamStatusEnum.COMPLETED);
		} else if (DamStatusEnum.PROCESSING.name().equals(assetStatus)) {
			dam.setDamStatusEnum(DamStatusEnum.PROCESSING);
		} else if (DamStatusEnum.FAILED.name().equals(assetStatus)) {
			dam.setDamStatusEnum(DamStatusEnum.FAILED);
		}
		return dam;
	}

	public String getDefaultDashboardBannerImagePath() {
		return defaultDashboardBannerImagePath;
	}

	public String getDefaultHelpImagePath() {
		return deafultHelpImagePath;
	}

	public String getHelpDescription() {
		return helpDescription;
	}

	public String replaceDefaultCompanyLogoWithCompanyLogo(String companyLogo, String htmlBody,
			String companyWebsiteUrl) {
		htmlBody = htmlBody.replaceAll(companyLogoDefaultUrl, companyLogo);
		htmlBody = replaceCompanyWebsiteUrl(htmlBody, companyWebsiteUrl);
		htmlBody = htmlBody.replaceAll(DEFAULT_VIDEO_THUMBNAIL_TAG, JAVA_SCRIPT_VOID);
		return htmlBody;
	}

	public String replaceCompanyWebsiteUrl(String htmlBody, String companyWebsiteUrl) {
		if (StringUtils.hasText(companyWebsiteUrl)) {
			String updatedCompanyWebsiteUrl = addHttpOrHttpsProtocolAsPrefix(companyWebsiteUrl);
			htmlBody = htmlBody.replace("VENDOR_COMPANY_WEBSITE_URL", updatedCompanyWebsiteUrl);
		}
		return htmlBody;
	}

	public String addHttpOrHttpsProtocolAsPrefix(String companyWebsiteUrl) {
		if (StringUtils.hasText(companyWebsiteUrl)) {
			boolean isLinkStartsWithHttpProtocol = companyWebsiteUrl.startsWith("https://")
					|| companyWebsiteUrl.startsWith("http://");
			return isLinkStartsWithHttpProtocol ? companyWebsiteUrl : "//" + companyWebsiteUrl;
		} else {
			return companyWebsiteUrl;
		}

	}

	public JSONObject convertStreamToJSONObject(InputStream inputStream) throws IOException, ParseException {
		JSONParser parser = new JSONParser();
		Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
		return (JSONObject) parser.parse(reader);
	}

	public Class<?> mapJsonResponseToDTO(JSONObject jsonObject, Class<?> clazz) throws IOException {
		if (jsonObject != null && clazz != null) {
			byte[] jsonData = jsonObject.toString().getBytes();
			ObjectMapper mapper = new ObjectMapper();
			return (Class<?>) mapper.readValue(jsonData, clazz);
		} else {
			return null;
		}

	}

	public String replaceHtmlUrlsWithPrefix(String content) {
		if (StringUtils.hasText(content)) {
			Document doc = Jsoup.parse(content);
			Elements links = doc.select("a[href]");
			for (Element link : links) {
				String href = link.attr("href");
				if (!href.startsWith("http://") && !href.startsWith("https://")) {
					link.attr("href", "//" + href);
				}
			}
			return doc.toString();
		} else {
			return content;
		}
	}

	public boolean checkIsZeroBounceApiEnabled() {
		return "true".equals(isZeroBounceApiEnabled);
	}

	public ModuleAccessDTO copyModuleAccessToModuleAccessDTO(ModuleAccess moduleAccess) {
		ModuleAccessDTO moduleAccessDTO = new ModuleAccessDTO();
		BeanUtils.copyProperties(moduleAccess, moduleAccessDTO);
		moduleAccessDTO.setLeads(moduleAccess.isEnableLeads());
		moduleAccessDTO.setFormBuilder(moduleAccess.isForm());
		return moduleAccessDTO;
	}

	// XNFR-833
	public FilePathAndThumbnailPath uploadAssetAndThumbnail(DamUploadPostDTO damUploadPostDTO) {
		AmazonS3 amazonClient = getAmazonClient();
		FilePathAndThumbnailPath filePathAndThumbnailPath = new FilePathAndThumbnailPath();
		String originalAssetFilePath = damUploadPostDTO.getCopiedAssetFilePath();
		String uniqueAssetFileName = generateUniqueFileName(damUploadPostDTO.getCompleteAssetFileName(),
				damUploadPostDTO.getDamId());
		saveFileToAws(amazonClient, uniqueAssetFileName, originalAssetFilePath, null);
		filePathAndThumbnailPath.setFilePath(amazonBaseUrl + getBucketName() + '/' + uniqueAssetFileName);
		return filePathAndThumbnailPath;
	}

	public String generateUniqueFileName(String originalFilePath, Integer damID) {
		String originalFileName = originalFilePath.substring(originalFilePath.lastIndexOf(File.separator) + 1);
		String uniqueId = UUID.randomUUID().toString();
		String extension = originalFileName.substring(originalFileName.lastIndexOf("."));
		return amazonEnvFolder + "shared-asset/" + damID + "/" + uniqueId + extension;
	}

	public AmazonS3 getAmazonClient() {
		BasicAWSCredentials creds = new BasicAWSCredentials(amazonAccessKey, amazonsecretKey);
		return AmazonS3Client.builder().withRegion("us-east-1").withCredentials(new AWSStaticCredentialsProvider(creds))
				.build();
	}

	public void saveFileToAws(AmazonS3 s3Client, String completeFileName, String uploadedFilePath, String fileType) {
		pushToAWS(s3Client, completeFileName, uploadedFilePath, fileType);
	}

	public void pushToAWS(AmazonS3 s3Client, String completeFileName, String uploadedFilePath, String fileType) {
		PutObjectRequest request = new PutObjectRequest(getBucketName(), completeFileName, new File(uploadedFilePath));
		request.setCannedAcl(CannedAccessControlList.PublicRead);
		ObjectMetadata metadata = new ObjectMetadata();
		metadata.setContentType(XamplifyUtils.isValidString(fileType) ? fileType : "plain/text");
		metadata.addUserMetadata("x-amz-meta-title", "someTitle");
		request.setMetadata(metadata);
		s3Client.putObject(request);
	}

	public String getBucketName() {
		return environment.getProperty("amazon.bucket.name");
	}

	public void downloadAssestPath(String awsPath, String localPath) {
		try {
			URL url = new URL(awsPath);
			HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
			httpURLConnection.setRequestMethod("GET");
			httpURLConnection.setConnectTimeout(5000);
			httpURLConnection.setReadTimeout(5000);

			Path path = Paths.get(localPath);
			Files.createDirectories(path.getParent());
			try (InputStream inputStream = httpURLConnection.getInputStream()) {
				Files.copy(inputStream, path, StandardCopyOption.REPLACE_EXISTING);
			}
		} catch (Exception e) {
			String message = "Error while downloading assest in local";
			e.printStackTrace();
		}
	}

	public void shareAssestToPartner(Integer damId, DamPartner damPartner, DamUploadPostDTO damUploadPostDTO,
			String assestPath) {
		if (XamplifyUtils.isValidString(assestPath)) {
			String filePath = mediaBasePath + "shared-assest/" + System.currentTimeMillis() + "/assests-path.pdf";

			try {
				downloadAssestPath(assestPath, filePath);
				File file = new File(filePath);
				damUploadPostDTO.setCopiedAssetFilePath(file.getPath());
				damUploadPostDTO.setDamId(damId);
				damUploadPostDTO.setCompleteAssetFileName(assestPath.replace("https://s3.amazonaws.com/xamplify/", ""));
				FilePathAndThumbnailPath filePaths = uploadAssetAndThumbnail(damUploadPostDTO);
				damPartner.setSharedAssetPath(filePaths.getFilePath());
				if (file.exists()) {
					Files.delete(file.toPath());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	// XNFR-833 -END

	public HttpsURLConnection establishConnectionForHtmlToImage() {
		String endpoint = "https://imageconverter.xamplify.co/convert_html_to_image";
		URL url = null;
		HttpsURLConnection connection = null;

		try {
			url = new URL(endpoint);
			connection = (HttpsURLConnection) url.openConnection();
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
			connection.setDoOutput(true);
			connection.connect();
		} catch (MalformedURLException e) {
			System.out.println("Internet is not connected");
		} catch (ProtocolException e) {
			System.out.println("Internet is not connected");
		} catch (IOException e) {
			System.out.println("Internet is not connected");
		}
		return connection;
	}

	public HttpsURLConnection callHtmlToImageRestApi(String htmlBody) {
		HttpsURLConnection connection = null;
		try {
			connection = establishConnectionForHtmlToImage();
			String normalized = Normalizer.normalize(htmlBody, Normalizer.Form.NFD);

			// Remove non-ASCII characters
			String sanitized = normalized.replaceAll("[^\\x00-\\x7F]", "");
			Map<String, Object> request = new HashMap<>();
			request.put("html", sanitized);
			ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
			String requestBody = ow.writeValueAsString(request);
			OutputStreamWriter wr = new OutputStreamWriter(connection.getOutputStream(), StandardCharsets.UTF_8);

			wr.write(requestBody);
			wr.flush();
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return connection;

	}

	public String replaceS3WithCloudfrontViceVersa(String input) {
		String s3BaseUrl = amazonBaseUrl + getBucketName();
		if (!input.contains(s3BaseUrl)) {
			return input;
		}
		String tagetUrl = amazonClodFrontEnabled ? amazonClodFrontUrl : s3BaseUrl;
		return input.replace(s3BaseUrl, tagetUrl);
	}

	public void replaceAllMergeTagsForDam(String pdfPath, UserDTO vendor, UserDTO partner, String outputPath) {
		try {
			if (outputPath == null || outputPath.isEmpty()) {
				outputPath = pdfPath.replace(".pdf", "_merged.pdf");
			}

			PdfReader reader = new PdfReader(pdfPath);
			PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(outputPath));
			Map<String, String> mergeTags = buildMergeTags(vendor, partner);

			BaseFont fallbackFont = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.EMBEDDED);

			String partnerLogoPlaceholder = "";
			String partnerLogoPath = mergeTags.get(partnerLogoPlaceholder);

			for (int pageNum = 1; pageNum <= reader.getNumberOfPages(); pageNum++) {
				PdfContentByte canvas = stamper.getOverContent(pageNum);

				if (partnerLogoPath != null) {
					PdfDictionary pageDict = reader.getPageN(pageNum);
					PdfArray annots = pageDict.getAsArray(PdfName.ANNOTS);
					if (annots != null) {
						for (int i = annots.size() - 1; i >= 0; i--) {
							PdfDictionary annot = annots.getAsDict(i);
							if (PdfName.LINK.equals(annot.getAsName(PdfName.SUBTYPE))) {
								PdfDictionary action = annot.getAsDict(PdfName.A);
								if (action != null && PdfName.URI.equals(action.getAsName(PdfName.S))) {
									PdfString uri = action.getAsString(PdfName.URI);
									if (uri != null && uri.toUnicodeString().equals("https://dummycobrandingurl.com")) {
										PdfArray rect = annot.getAsArray(PdfName.RECT);
										float llx = rect.getAsNumber(0).floatValue();
										float lly = rect.getAsNumber(1).floatValue();
										float urx = rect.getAsNumber(2).floatValue();
										float ury = rect.getAsNumber(3).floatValue();
										float width = urx - llx;
										float height = ury - lly;

										canvas.saveState();
										canvas.setColorFill(BaseColor.WHITE);
										canvas.rectangle(llx, lly, width, height);
										canvas.fill();
										canvas.restoreState();

										Image partnerLogo = Image.getInstance(partnerLogoPath);
										partnerLogo.scaleAbsolute(width, height);
										partnerLogo.setAbsolutePosition(llx, lly);
										canvas.addImage(partnerLogo);

										annots.remove(i);
									}
								}
							}
						}
					}
				}

				TextReconstructionRenderListener listener = new TextReconstructionRenderListener();

				PdfReaderContentParser parser = new PdfReaderContentParser(reader);
				parser.processContent(pageNum, listener);

				List<ReconstructedLine> lines = listener.getLines();

				for (ReconstructedLine line : lines) {
					String originalLine = line.text;

					String replacedLine = replaceTags(originalLine, mergeTags);

					if (!originalLine.equals(replacedLine)) {
						BaseFont baseFont = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI,
								BaseFont.EMBEDDED);
						float fontSize = line.fontSize;

						float eraseWidth = baseFont.getWidthPoint(originalLine, fontSize);
						float ascent = baseFont.getAscentPoint(originalLine, fontSize);
						float descent = baseFont.getDescentPoint(originalLine, fontSize);
						float eraseHeight = ascent - descent;
						float lowerY = line.y + descent;

						float rightPadding = 30;

						canvas.saveState();
						canvas.setColorFill(BaseColor.WHITE);
						canvas.rectangle(line.x - 1, lowerY - 1, eraseWidth + rightPadding, eraseHeight + 2);
						canvas.fill();
						canvas.restoreState();

						canvas.beginText();
						canvas.setFontAndSize(baseFont, fontSize);
						canvas.setColorFill(BaseColor.BLACK);
						canvas.setTextMatrix(line.x, line.y);
						canvas.showText(replacedLine);
						canvas.endText();
					}

				}

			}

			stamper.setFormFlattening(false);
			stamper.close();
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static String replaceTags(String input, Map<String, String> mergeTags) {
		String replaced = input;
		for (Map.Entry<String, String> entry : mergeTags.entrySet()) {
			replaced = replaced.replace(entry.getKey(), entry.getValue());
		}
		replaced = replaced.replaceAll("\\{\\{[^}]*}}", "");
		return replaced;
	}

	private Map<String, String> buildMergeTags(UserDTO vendor, UserDTO partner) {
		Map<String, String> mergeTags = new HashMap<>();

		mergeTags.put("{{partnerCompanyName}}", safe(partner.getCompanyName(), "-"));
		mergeTags.put("{{partnerCompanyEmailId}}", safe(partner.getEmailId(), "-"));
		mergeTags.put("{{partnerCompanyContactNumber}}", safe(partner.getCompanyMobile(), "-"));
		mergeTags.put("{{partnerCompanyUrl}}", safe(partner.getWebsiteUrl(), "-"));
		mergeTags.put("{{partnerCompanyAddress}}", safe(cleanAddress(partner.getCompanyAddress()), "-"));
		mergeTags.put("{{partnerCompanyAboutUs}}", safe(partner.getPartnerAboutUs(), "-"));

		if (partner.getCompanyLogo() != null && !partner.getCompanyLogo().trim().isEmpty()) {
			String partnerLogoPath = server_path + XamplifyUtils.escapeDollarSequece(partner.getCompanyLogo());
			mergeTags.put("", safe(partnerLogoPath, "-"));
		}

		mergeTags.put("{{senderFirstName}}", safe(vendor.getFirstName(), "There"));
		mergeTags.put("{{senderMiddleName}}", safe(vendor.getMiddleName(), "There"));
		mergeTags.put("{{senderLastName}}", safe(vendor.getLastName(), "There"));
		mergeTags.put("{{senderFullName}}", safe(vendor.getFullName(), "There"));
		mergeTags.put("{{senderJobTitle}}", safe(vendor.getJobTitle(), "-"));
		mergeTags.put("{{senderEmailId}}", safe(vendor.getEmailId(), "-"));
		mergeTags.put("{{senderContactNumber}}", safe(vendor.getMobileNumber(), "-"));
		mergeTags.put("{{senderCompany}}", safe(vendor.getCompanyName(), "-"));
		mergeTags.put("{{senderCompanyUrl}}", safe(vendor.getWebsiteUrl(), "-"));
		mergeTags.put("{{senderCompanyInstagramUrl}}", safe(vendor.getInstagramUrl(), "-"));
		mergeTags.put("{{senderCompanyTwitterUrl}}", safe(vendor.getTwitterUrl(), "-"));
		mergeTags.put("{{senderCompanyGoogleUrl}}", safe(vendor.getGooglePlusLink(), "-"));
		mergeTags.put("{{senderCompanyFacebookUrl}}", safe(vendor.getFacebookLink(), "-"));
		mergeTags.put("{{senderCompanyLinkedinUrl}}", safe(vendor.getLinkedInLink(), "-"));
		mergeTags.put("{{senderCompanyAddress}}", safe(cleanAddress(vendor.getAddress()), "-"));
		mergeTags.put("{{senderEventUrl}}", safe(vendor.getEventUrl(), "-"));
		mergeTags.put("{{senderAboutUs}}", safe(vendor.getAboutUs(), "-"));
		mergeTags.put("{{senderPrivacyPolicy}}", safe(vendor.getPrivacyPolicy(), "-"));
		mergeTags.put("{{senderCompanyContactNumber}}", safe(vendor.getMobileNumber(), "-"));

		String companyLogoPath = server_path + XamplifyUtils.escapeDollarSequece(vendor.getCompanyLogo());

		mergeTags.put("", safe(companyLogoPath, "-"));
		return mergeTags;
	}

	private static String safe(String val, String defaultVal) {
		return (val != null && !val.trim().isEmpty()) ? val.trim() : defaultVal;
	}

	private String cleanAddress(String fullAddress) {
		return Arrays.stream(fullAddress.split(",")).map(String::trim).filter(part -> !part.isEmpty())
				.collect(Collectors.joining(", "));
	}

	private static class ReconstructedLine {
		String text;
		float x, y, width, height;
		float fontSize;
		DocumentFont font;

		ReconstructedLine(String text, float x, float y, float width, float height, float fontSize, DocumentFont font) {
			this.text = text;
			this.x = x;
			this.y = y;
			this.width = width;
			this.height = height;
			this.fontSize = fontSize;
			this.font = font;
		}
	}

	private static class TextReconstructionRenderListener implements RenderListener {
		private final List<ReconstructedLine> lines = new ArrayList<>();
		private final StringBuilder currentLine = new StringBuilder();
		private float x = 0, y = 0, width = 0, height = 0, fontSize = 0;
		private DocumentFont font;

		@Override
		public void beginTextBlock() {
			currentLine.setLength(0);
			width = 0;
		}

		@Override
		public void renderText(TextRenderInfo renderInfo) {
			String text = renderInfo.getText();
			if (text == null || text.trim().isEmpty())
				return;

			Vector start = renderInfo.getBaseline().getStartPoint();
			float chunkX = start.get(Vector.I1);
			float chunkY = start.get(Vector.I2);

			float chunkFontSize = renderInfo.getAscentLine().getStartPoint().get(Vector.I2)
					- renderInfo.getDescentLine().getStartPoint().get(Vector.I2);
			DocumentFont chunkFont = renderInfo.getFont();
			float chunkWidth = chunkFont.getWidthPoint(text, chunkFontSize);

			if (currentLine.length() > 0 && Math.abs(chunkY - y) > 1.0f) {
				lines.add(new ReconstructedLine(currentLine.toString(), x, y, width, height, fontSize, font));
				currentLine.setLength(0);
				width = 0;
			}

			if (currentLine.length() == 0) {
				x = chunkX;
				y = chunkY;
				font = chunkFont;
				fontSize = chunkFontSize;
			}

			currentLine.append(text);
			width += chunkWidth;
			height = fontSize;
		}

		@Override
		public void endTextBlock() {
			if (currentLine.length() > 0) {
				lines.add(new ReconstructedLine(currentLine.toString(), x, y, width, height, fontSize, font));
				currentLine.setLength(0);
				width = 0;
			}
		}

		@Override
		public void renderImage(ImageRenderInfo renderInfo) {
		}

		public List<ReconstructedLine> getLines() {
			return lines;
		}
	}

	/*** XNFR-1013 - START ****/
	public String getColorPaletteJson(String domain) throws IOException {
		String endpoint = COLOR_API_BASE + URLEncoder.encode(domain, StandardCharsets.UTF_8.toString());
		HttpURLConnection connection = (HttpURLConnection) new URL(endpoint).openConnection();
		connection.setRequestMethod("GET");
		connection.setRequestProperty("Accept", "application/json");

		try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
			return reader.lines().collect(Collectors.joining());
		}
	}

	public Optional<Map<String, String>> getColorPalette(String domain) {
		try {
			String json = getColorPaletteJson(domain);
			Map<String, String> colorMap = new ObjectMapper().readValue(json, new TypeReference<Map<String, String>>() {
			});
			return Optional.of(colorMap);
		} catch (Exception e) {
			System.err.println("Failed to fetch or parse color palette: " + e.getMessage());
			return Optional.empty();
		}
	}

	/*** XNFR-1013 - END ****/
	
	public String getLoginUrl() {
		return webUrl+"/login";
	}

	public Optional<Map<String, String>> getColorPaletteForDomain(String domain) {
		try {
			String json = getColorPaletteJson(domain);
			Map<String, String> colorMap = new ObjectMapper().readValue(json, new TypeReference<Map<String, String>>() {
			});
			return Optional.of(colorMap);
		} catch (Exception e) {
			System.err.println("Failed to fetch or parse color palette: " + e.getMessage());
			return Optional.empty();
		}
	}


}
