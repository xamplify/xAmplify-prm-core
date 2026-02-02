package com.xtremand.dam.service.impl;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Hyperlink;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.s3.model.DeleteObjectsRequest.KeyVersion;
import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.xtremand.approve.dao.ApproveDAO;
import com.xtremand.approve.dto.PendingApprovalDamAndLmsDTO;
import com.xtremand.aws.AWSInputDTO;
import com.xtremand.aws.AmazonWebModel;
import com.xtremand.aws.AmazonWebService;
import com.xtremand.aws.CopiedFileDetails;
import com.xtremand.aws.FilePathAndThumbnailPath;
import com.xtremand.category.bom.CategoryModule;
import com.xtremand.category.bom.CategoryModuleEnum;
import com.xtremand.category.dao.CategoryDao;
import com.xtremand.comments.dao.CommentDao;
import com.xtremand.common.bom.CompanyProfile;
import com.xtremand.common.bom.FindLevel;
import com.xtremand.common.bom.Pagination;
import com.xtremand.dam.bom.ApprovalStatusType;
import com.xtremand.dam.bom.Dam;
import com.xtremand.dam.bom.DamAnalytics;
import com.xtremand.dam.bom.DamAnalyticsActionEnum;
import com.xtremand.dam.bom.DamPartner;
import com.xtremand.dam.bom.DamPartnerGroupMapping;
import com.xtremand.dam.bom.DamPartnerMapping;
import com.xtremand.dam.bom.DamStatusEnum;
import com.xtremand.dam.bom.DamTag;
import com.xtremand.dam.bom.PublishedAssetsView;
import com.xtremand.dam.dao.DamDao;
import com.xtremand.dam.dto.AssetPdfPreviewRequestDTO;
import com.xtremand.dam.dto.ContentPreviewDetailsDTO;
import com.xtremand.dam.dto.DamAnalyticsDTO;
import com.xtremand.dam.dto.DamAnalyticsPostDTO;
import com.xtremand.dam.dto.DamAwsDTO;
import com.xtremand.dam.dto.DamDTO;
import com.xtremand.dam.dto.DamDownloadDTO;
import com.xtremand.dam.dto.DamDownloadPostDTO;
import com.xtremand.dam.dto.DamListDTO;
import com.xtremand.dam.dto.DamPartnerPreviewDTO;
import com.xtremand.dam.dto.DamPostDTO;
import com.xtremand.dam.dto.DamPreviewDTO;
import com.xtremand.dam.dto.DamPublishGetDTO;
import com.xtremand.dam.dto.DamPublishPostDTO;
import com.xtremand.dam.dto.DamUploadPostDTO;
import com.xtremand.dam.dto.DamViewDTO;
import com.xtremand.dam.dto.PublishedContentIdAndUserListIdDetailsDTO;
import com.xtremand.dam.dto.SharedAssetDetailsViewDTO;
import com.xtremand.dam.exception.DamDataAccessException;
import com.xtremand.dam.service.DamService;
import com.xtremand.dam.validator.AssetPdfPreviewValidator;
import com.xtremand.dao.util.GenericDAO;
import com.xtremand.exception.DuplicateEntryException;
import com.xtremand.formbeans.UserDTO;
import com.xtremand.formbeans.VideoFileDTO;
import com.xtremand.formbeans.XtremandResponse;
import com.xtremand.landing.page.analytics.bom.GeoLocationAnalytics;
import com.xtremand.landing.page.analytics.bom.GeoLocationAnalyticsEnum;
import com.xtremand.landing.page.analytics.dto.GeoLocationAnalyticsDTO;
import com.xtremand.lead.dao.LeadDAO;
import com.xtremand.lms.dao.LMSDAO;
import com.xtremand.mail.service.AsyncComponent;
import com.xtremand.mail.service.MailService;
import com.xtremand.mail.service.MailService.EmailBuilder;
import com.xtremand.partnership.bom.Partnership;
import com.xtremand.partnership.dao.PartnershipDAO;
import com.xtremand.signature.dao.SignatureDao;
import com.xtremand.tag.bom.Tag;
import com.xtremand.tag.dao.TagDao;
import com.xtremand.tag.dto.TagDTO;
import com.xtremand.user.bom.Role;
import com.xtremand.user.bom.User;
import com.xtremand.user.dao.UserDAO;
import com.xtremand.user.service.UserService;
import com.xtremand.userlist.dao.UserListDAO;
import com.xtremand.util.BadRequestException;
import com.xtremand.util.DateUtils;
import com.xtremand.util.FileUtil;
import com.xtremand.util.GenerateRandomPassword;
import com.xtremand.util.PaginationUtil;
import com.xtremand.util.XamplifyUtil;
import com.xtremand.util.XamplifyUtils;
import com.xtremand.util.bom.ModuleType;
import com.xtremand.util.dao.UtilDao;
import com.xtremand.util.dto.Pageable;
import com.xtremand.util.dto.PaginatedDTO;
import com.xtremand.util.dto.ShareContentRequestDTO;
import com.xtremand.util.dto.UserDetailsUtilDTO;
import com.xtremand.util.dto.UserListAndUserId;
import com.xtremand.util.dto.XamplifyConstants;
import com.xtremand.util.dto.XamplifyUtilValidator;
import com.xtremand.util.service.UtilService;
import com.xtremand.validator.PageableValidator;
import com.xtremand.vanity.email.templates.bom.CustomDefaultEmailTemplate;
import com.xtremand.vanity.email.templates.bom.DefaultEmailTemplate;
import com.xtremand.vanity.email.templates.bom.DefaultEmailTemplateType;
import com.xtremand.vanity.url.dao.VanityURLDao;
import com.xtremand.vanity.url.dto.VanityUrlDetailsDTO;
import com.xtremand.video.bom.VideoFile;
import com.xtremand.video.bom.VideoImage;
import com.xtremand.video.dao.VideoDao;
import com.xtremand.video.formbeans.VideoFileUploadForm;
import com.xtremand.video.service.VideoService;
import com.xtremand.videoencoding.service.FFMPEGStatus;
import com.xtremand.white.labeled.dao.WhiteLabeledAssetDao;
import com.xtremand.white.labeled.dto.DamVideoDTO;

@Service
@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public class DamServiceImpl implements DamService {

	private static final String PUBLISH_ASSET = "publishAsset";

	private static final String SHARE_WHITE_LABELED_ASSET = "shareAsWhiteLabeledAsset";

	private static final String PUBLISHED_PARTNER_USER_IDS = "publishedPartnerUserIds";

	private static final String DEFAULT_PARTNER_ABOUT_US_MERGE_TAG = "{{partnerAboutUs}}";

	private static final String HOST = "http://localhost:8080/";

	private static final String XAMPLIFY_PRM = "xamplify-prm-api";
	@Autowired
	private DamDao damDao;

	@Autowired
	private UserDAO userDao;

	@Value("${amazon.dam.folder}")
	private String awsDamFolder;

	@Value("${pdfImagePreviewPath}")
	private String pdfImagePreviewPath;

	@Value("${damThumbnailPath}")
	private String damThumbnailPath;

	@Value("${content.pdf.path}")
	private String contentPdfPath;

	@Value("${content.html.path}")
	private String contentHtmlPath;

	@Value("${server_path}")
	String serverPath;

	@Value("${portrait}")
	private String portrait;

	@Value("${landscape}")
	private String landscape;

	@Value("${processingGifPath}")
	private String processingGifPath;

	@Value("${image.format.types}")
	String imageFormats;

	@Value("${specialCharacters}")
	String regex;

	@Value("${web_url}")
	String webUrl;

	@Value("${amazon.env.folder}")
	private String amazonEnvFolder;

	@Value("${amazon.images.folder}")
	private String imageFolder;

	@Value("${amazon.thumbnails.folder}")
	private String thumbnailFolder;

	@Value("${amazon.previews.folder}")
	String amazonPreviewFolder;

	@Value("${tooltip.disabled.white.label}")
	private String whiteLabeledDisabledToolTipMessage;

	@Value("${tooltip.white.label}")
	private String whiteLabeledToolTipMessage;

	/** XNFR-781 **/
	@Value("${asset.deletion.associated_with_tracks_playbooks.message}")
	private String assetDeletionAssociatedWithTracksPlaybooksMessage;

	@Value("${asset.rejection.associated_with_tracks_playbooks.message}")
	private String assetRejectionAssociatedWithTracksPlaybooksMessage;

	@Value("${asset.deletion.associated_with_tracks.message}")
	private String assetDeletionAssociatedWithTracksMessage;

	@Value("${asset.rejection.associated_with_tracks.message}")
	private String assetRejectionAssociatedWithTracksMessage;

	@Value("${asset.deletion.associated_with_playbooks.message}")
	private String assetDeletionAssociatedWithPlaybooksMessage;

	@Value("${asset.rejection.associated_with_playbooks.message}")
	private String assetRejectionAssociatedWithPlaybooksMessage;

	@Value("${dev.host}")
	String devHost;

	@Value("${prod.host}")
	String productionHost;

	@Value("${co.branding.logo}")
	String coBrandingLogo;

	@Value("${xamplify.logo}")
	String xAmplifyLogo;

	@Value("${replace.company.logo}")
	String companyLogo;

	@Autowired
	private XamplifyUtil xamplifyUtil;

	@Autowired
	private AmazonWebService amazonWebService;

	@Autowired
	private UtilService utilService;

	@Autowired
	private FileUtil fileUtil;

	@Autowired
	private AsyncComponent asyncComponent;

	@Autowired
	private TagDao tagDao;

	@Autowired
	private GenericDAO genericDAO;

	@Autowired
	private UserService userService;

	@Autowired
	private VideoService videoService;

	@Autowired
	private CommentDao commentDao;

	@Value("${upload_content_path}")
	String uploadContentPath;

	@Value("${separator}")
	String sep;

	@Autowired
	VideoDao videoDAO;

	@Autowired
	private CategoryDao categoryDao;

	@Autowired
	private UtilDao utilDao;

	@Autowired
	private UserListDAO userListDao;

	@Autowired
	private WhiteLabeledAssetDao whiteLabeledAssetDao;

	@Autowired
	private PaginationUtil paginationUtil;

	@Autowired
	private LMSDAO lmsDao;

	@Autowired
	private AssetPdfPreviewValidator assetPdfPreviewValidator;

	@Autowired
	private XamplifyUtilValidator xamplifyUtilValidator;

	@Autowired
	private PageableValidator pageableValidator;

	@Value("${image.file.types}")
	String imageFileTypes;

	@Value("${content.preview.fileTypes}")
	String contentPreviewSupportedFileFormats;

	// XNFR-833
	@Value("${amazon.access.id}")
	String amazonAccessKey;

	@Value("${amazon.base.url}")
	String amazonBaseUrl;

	@Value("${amazon.secret.key}")
	String amazonsecretKey;

	@Value("${tooltip.disabled.partner.signature.required.label}")
	private String partnerSignatureRequiredDisabledToolTipMessage;

	@Value("${tooltip.partner.signature.required.label}")
	private String partnerSignatureRequiredToolTipMessage;

	@Value("${tooltip.disabled.vendor.signature.required.label}")
	private String vendorSignatureRequiredDisabledToolTipMessage;

	@Value("${tooltip.vendor.signature.required.label}")
	private String vendorSignatureRequiredToolTipMessage;

	@Value("${tooltip.disabled.vendor.signature.required.after.partner.signature.label}")
	private String vendorSignatureRequiredAfterPartnerSignatureDisabledToolTipMessage;

	@Value("${tooltip.vendor.signature.required.after.partner.signature.label}")
	private String vendorSignatureRequiredAfterPartnerSignatureToolTipMessage;

	@Autowired
	private SignatureDao signatureDao;

	@Autowired
	private PartnershipDAO partnershipDao;

	@Autowired
	private ApproveDAO approveDao;

	@Value("${media_base_path}")
	String mediaBasePath;

	@Value("${content.preview.fileTypes.text}")
	String contentPreviewForTextView;

	@Autowired
	private LeadDAO leadDAO;
	@Autowired
	private UserDAO userDAO;
	@Autowired
	private VanityURLDao vanityURLDao;

	@Autowired
	private GenericDAO genericDao;

	@Value("${replace.company.logo}")
	private String replaceCompanyLogo;

	@Autowired
	private MailService mailService;

	@Value("${fromEmail}")
	private String fromEmail;

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public XtremandResponse save(DamPostDTO damPostDTO) {
		try {
			XtremandResponse response = new XtremandResponse();
			Integer id = damPostDTO.getId();
			Dam dam = new Dam();
			boolean saveAs = damPostDTO.isSaveAs();
			if (id != null && id > 0 && !saveAs) {
				setBeeTemplateProperties(damPostDTO.getCategoryId(), id, dam, response);
			}
			BeanUtils.copyProperties(damPostDTO, dam);
			Integer companyId = userDao.getCompanyIdByUserId(damPostDTO.getCreatedBy());
			String replacedDescription = utilService.replacedDescription(damPostDTO.getDescription());
			CompanyProfile companyProfile = new CompanyProfile();
			companyProfile.setId(companyId);
			dam.setCompanyProfile(companyProfile);
			dam.setAssetName(XamplifyUtils.removeExtraSpace(damPostDTO.getName()));
			dam.setAssetType("pdf");
			dam.setThumbnailPath(processingGifPath);
			dam.setBeeTemplate(true);
			setPageSizeAndPageOrientation(damPostDTO, dam);
			GenerateRandomPassword password = new GenerateRandomPassword();
			dam.setAlias(password.getPassword());
			dam.setCreatedTime(new Date());
			dam.setDamStatusEnum(DamStatusEnum.COMPLETED);
			dam.setAddedToQuickLinks(damPostDTO.isAddedToQuickLinks());
			dam.setDescription(replacedDescription);
			/***** XNFR-255 ****/
			boolean shareWhiteLabeledContentAccess = utilDao.hasShareWhiteLabeledContentAccessByCompanyId(companyId);
			dam.setWhiteLabeledAssetSharedWithPartners(
					shareWhiteLabeledContentAccess && damPostDTO.isShareAsWhiteLabeledAsset());
			boolean partnerGroupsSelected = damPostDTO.getPartnerGroupIds() != null
					&& !damPostDTO.getPartnerGroupIds().isEmpty();
			boolean partnerCompaniesSelected = damPostDTO.getPartnerIds() != null
					&& !damPostDTO.getPartnerIds().isEmpty();
			boolean canAssetPublished = partnerGroupsSelected || partnerCompaniesSelected;
			dam.setPublishingOrWhiteLabelingInProgress(canAssetPublished);

			/** XNFR-781 **/

			setAssetApprovalStatus(damPostDTO.getCreatedBy(), companyId, dam, damPostDTO.isDraft());

			damDao.save(dam);
			saveTags(null, damPostDTO, dam, companyId, damPostDTO.getCreatedBy());
			/****** XNFR-169 ****/
			addDamCategory(damPostDTO.getCategoryId(), damPostDTO.getCreatedBy(), dam);
			/***** XNFR-255 ****/
			damPostDTO.setCompanyId(companyId);
			damPostDTO.setDamId(dam.getId());
			/***** XNFR-255 ****/
			response.setStatusCode(200);

			boolean isPublished = XamplifyUtils.isNotEmptySet(damPostDTO.getPartnerIds())
					|| XamplifyUtils.isNotEmptySet(damPostDTO.getPartnerGroupIds());
			if (isPublished) {
				response.setMessage("PDF Created & Published Successfully");
			} else {
				response.setMessage("PDF Created Successfully");
			}

			commentDao.createApprovalStatusHistory(damPostDTO.getDamId(), damPostDTO.getCreatedBy(), ModuleType.DAM);

			return response;
		} catch (BadRequestException bre) {
			throw new BadRequestException(bre.getMessage());
		} catch (DamDataAccessException mex) {
			throw new DamDataAccessException(mex);
		} catch (Exception e) {
			throw new DamDataAccessException(e);
		}

	}

	private void setPageSizeAndPageOrientation(DamPostDTO damPostDTO, Dam dam) {
		if (StringUtils.hasText(damPostDTO.getPageSize())) {
			dam.setPageSize(damPostDTO.getPageSize());
		} else {
			dam.setPageSize("A4");
		}

		if (StringUtils.hasText(damPostDTO.getPageOrientation())) {
			dam.setPageOrientation(damPostDTO.getPageOrientation());
		} else {
			dam.setPageOrientation(portrait);
		}
	}

	private void setBeeTemplateProperties(Integer categoryId, Integer id, Dam dam, XtremandResponse response) {
		Integer categoryIdInput = categoryId;
		Integer categoryIdFromDb = damDao.getBeeTemplateCategoryIdByDamId(id);
		if (categoryIdFromDb != null && !categoryIdFromDb.equals(categoryIdInput)) {
			response.setStatusCode(400);
			throw new BadRequestException("Folder cannot be changed for history templates");
		}
		Integer parentTemplateId = null;
		Integer parentIdByChildParentId = damDao.getParentIdByChildParentId(id);
		if (parentIdByChildParentId != null) {
			parentTemplateId = parentIdByChildParentId;
			dam.setParentId(parentIdByChildParentId);
			dam.setChildParentId(id);
		} else {
			parentTemplateId = id;
			dam.setParentId(id);
		}
		/***** Get the version count of this template ******/
		Integer childTemplatesCount = damDao.getChildTemplatesCountByParentId(parentTemplateId);
		if (childTemplatesCount == 0) {
			damDao.updateVersion(id, 1);
		} else {
			Integer templateId = damDao.getIdForUpdatingVersion(parentTemplateId);
			Integer maxVersion = damDao.getMaxVersionByParentId(parentTemplateId);
			damDao.updateVersion(templateId, maxVersion + 1);
		}
	}

	@Override
	public XtremandResponse listAssets(Pagination pagination) {
		try {
			XtremandResponse response = new XtremandResponse();
			response.setStatusCode(200);
			/**** XNFR- 409 ****/
			utilService.setDateFilters(pagination);
			/**** XNFR- 409 ****/
			String companyProfileName = utilDao.getPrmCompanyProfileName();
			if (XamplifyUtils.isValidString(companyProfileName)) {
				Integer vendorCompanyId = userDao.getCompanyIdByProfileName(companyProfileName);
				pagination.setVendorCompanyId(vendorCompanyId);
			}
			response.setData(damDao.listAssets(pagination));
			return response;
		} catch (DamDataAccessException mex) {
			throw new DamDataAccessException(mex);
		} catch (Exception e) {
			throw new DamDataAccessException(e);
		}
	}

	@Override
	public XtremandResponse getById(Integer id, Integer loggedInUserId) {
		try {
			XtremandResponse response = new XtremandResponse();
			boolean isIdMatched = checkIsDamIdBelongsToLoggedInUserCompany(id, loggedInUserId);
			if (isIdMatched) {
				Dam dam = damDao.getById(id);
				if (dam != null) {
					DamListDTO dto = new DamListDTO();
					setTags(dam, null, dto);
					BeanUtils.copyProperties(dam, dto);
					/******** XNFR-169 *********/
					Integer categoryId = categoryDao.getCategoryIdByType(id, CategoryModuleEnum.DAM.name());
					dto.setCategoryId(categoryId);
					/**** XNFR-255 ****/
					dto.setWhiteLabeledAssetSharedWithPartners(dam.isWhiteLabeledAssetSharedWithPartners());
					dto.setAddedToQuickLinks(dam.isAddedToQuickLinks());
					dto.setApprovalStatus(dam.getApprovalStatus().name());
					/***** XNFR-949 *****/
					if (dam.isBeeTemplate() && !XamplifyUtils.isValidString(dam.getAssetPath())) {
						asyncComponent.uploadDesignedPdfToAws(id);
					}

					response.setStatusCode(200);
					response.setData(dto);
				} else {
					response.setStatusCode(400);
				}
			} else {
				response.setStatusCode(403);
			}
			return response;
		} catch (DamDataAccessException mex) {
			throw new DamDataAccessException(mex);
		} catch (Exception e) {
			throw new DamDataAccessException(e);
		}
	}

	@Override
	public XtremandResponse getPublishedAssetById(Integer id, Integer loggedInUserId) {
		try {
			XtremandResponse response = new XtremandResponse();
			response.setStatusCode(200);
			boolean isIdMatched = checkIsDamPartnerIdBelongsToLoggedInUserCompany(id, loggedInUserId);
			if (isIdMatched) {
				DamPublishGetDTO damPublishGetDTO = damDao.getPublishedAssetById(id);
				damPublishGetDTO.setVendorCompanyLogo(serverPath + damPublishGetDTO.getVendorCompanyLogo());
				damPublishGetDTO.setPartnerCompanyLogo(serverPath + damPublishGetDTO.getPartnerCompanyLogo());
				response.setData(damPublishGetDTO);
			} else {
				response.setStatusCode(403);
			}
			return response;
		} catch (DamDataAccessException mex) {
			throw new DamDataAccessException(mex);
		} catch (Exception e) {
			throw new DamDataAccessException(e);
		}
	}

	@Override
	public XtremandResponse listAssetsHistory(Pagination pagination) {
		try {
			XtremandResponse response = new XtremandResponse();
			response.setStatusCode(200);
			response.setData(damDao.listAssetsHistory(pagination));
			return response;
		} catch (DamDataAccessException mex) {
			throw new DamDataAccessException(mex);
		} catch (Exception e) {
			throw new DamDataAccessException(e);
		}
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public XtremandResponse publish(DamPublishPostDTO damPublishPostDTO) {
		try {
			XtremandResponse response = new XtremandResponse();
			Integer damId = damPublishPostDTO.getDamId();
			boolean isPublishedToPartnerGroups = damDao.isPublishedToPartnerGroups(damId);
			if (damPublishPostDTO.isPartnerGroupSelected()) {
				if (!isPublishedToPartnerGroups) {
					damDao.deleteDamPartnerByDamIdAndUnpublishDam(damId);
				}
				findPublishedPartnerGroupIdsAndPublishOrUnpublish(damPublishPostDTO, response, damId);
			} else {
				if (isPublishedToPartnerGroups) {
					damDao.deleteDamPartnerByDamIdAndUnpublishDam(damId);
				}
				findPublishedPartnerIdsAndPublishOrUnpublish(damPublishPostDTO, response, damId);
			}
			response.setAccess(true);
			return response;
		} catch (DamDataAccessException mex) {
			throw new DamDataAccessException(mex);
		} catch (Exception e) {
			throw new DamDataAccessException(e);
		}
	}

	private void findPublishedPartnerGroupIdsAndPublishOrUnpublish(DamPublishPostDTO damPublishPostDTO,
			XtremandResponse response, Integer damId) {
		if (damPublishPostDTO.getPartnerGroupIds().isEmpty()) {
			unPublishAsset(response, damId);
		} else {
			List<Integer> partnerGroupIds = damPublishPostDTO.getPartnerGroupIds();
			List<Integer> publishedPartnerGroupIds = damDao.findPublishedPartnerGroupIdsByDamId(damId);
			Collections.sort(partnerGroupIds);
			Collections.sort(publishedPartnerGroupIds);
			if (partnerGroupIds.equals(publishedPartnerGroupIds)) {
				response.setMessage("Nothing to publish / unpublish");
			} else {
				publishedPartnerGroupIds.removeAll(partnerGroupIds);
				damDao.findAndDeleteDamPartnerGroupMappingIds(damId, publishedPartnerGroupIds);
				removeUnselectedPartnerGroupIds(damId, partnerGroupIds);
				damPublishPostDTO.setPartnerGroupIds(partnerGroupIds);
				if (!partnerGroupIds.isEmpty()) {
					publishAndReturnMap(damPublishPostDTO, response, damId);
				} else {
					setUnpublishedResponse(response);
				}
			}
		}

	}

	private void removeUnselectedPartnerGroupIds(Integer damId, List<Integer> partnerGroupIds) {
		List<Integer> publishedPartnerGroupIds = damDao.findPublishedPartnerGroupIdsByDamId(damId);
		if (!partnerGroupIds.equals(publishedPartnerGroupIds)) {
			partnerGroupIds.removeAll(publishedPartnerGroupIds);
		}
	}

	private void findPublishedPartnerIdsAndPublishOrUnpublish(DamPublishPostDTO damPublishPostDTO,
			XtremandResponse response, Integer damId) {
		if (damPublishPostDTO.getPartnerIds().isEmpty()) {
			unPublishAsset(response, damId);
		} else {
			List<Integer> partnerIds = damPublishPostDTO.getPartnerIds();
			List<Integer> publishedPartnerIds = damDao.findPublishedPartnerIdsByDamId(damId);
			Collections.sort(partnerIds);
			Collections.sort(publishedPartnerIds);
			if (partnerIds.equals(publishedPartnerIds)) {
				response.setMessage("Nothing to publish / unpublish");
			} else {
				publishOrUnpublishToPartners(damPublishPostDTO, response, damId, partnerIds, publishedPartnerIds);
			}
		}
	}

	private void unPublishAsset(XtremandResponse response, Integer damId) {
		damDao.deleteDamPartnerByDamIdAndUnpublishDam(damId);
		response.setMessage("UnPublished Successfully.");
	}

	private void publishOrUnpublishToPartners(DamPublishPostDTO damPublishPostDTO, XtremandResponse response,
			Integer damId, List<Integer> partnerIds, List<Integer> publishedPartnerIds) {
		publishedPartnerIds.removeAll(partnerIds);
		damDao.findAndDeleteDamPartnerMappingIds(damId, publishedPartnerIds);
		removeUnselectedPartnerIds(damId, partnerIds);
		damPublishPostDTO.setPartnerIds(partnerIds);
		if (!partnerIds.isEmpty()) {
			publishAndReturnMap(damPublishPostDTO, response, damId);
		} else {
			setUnpublishedResponse(response);
		}
	}

	private void publishAndReturnMap(DamPublishPostDTO damPublishPostDTO, XtremandResponse response, Integer damId) {
		Dam dam = damDao.getById(damId);
		dam.setPublishedTime(new Date());
		damDao.publish(damPublishPostDTO, dam);
		// Do not remove this code as dam entity has a mapping table API is returning
		// empty data. --- Tejaswini
		dam.setTags(new HashSet<>());

		Map<String, Object> map = new HashMap<>();
		DamDTO damDto = getDamDTO(dam);
		map.put("dam", damDto);
		map.put("damPublishPostDTO", damPublishPostDTO);
		setResponseStatusAndMessage(damPublishPostDTO, response);
		response.setMap(map);
	}

	private void setResponseStatusAndMessage(DamPublishPostDTO damPublishPostDTO, XtremandResponse response) {
		if (damPublishPostDTO.getUpdatedPartnerIds() != null && !damPublishPostDTO.getUpdatedPartnerIds().isEmpty()) {
			response.setStatusCode(200);
		} else {
			setUnpublishedResponse(response);
		}
	}

	private void setUnpublishedResponse(XtremandResponse response) {
		response.setStatusCode(404);
		response.setMessage("Unpublished Successfully.");
	}

	private void removeUnselectedPartnerIds(Integer damId, List<Integer> partnerIds) {
		List<Integer> publishedPartnerIds = damDao.findPublishedPartnerIdsByDamId(damId);
		Collections.sort(publishedPartnerIds);
		if (!partnerIds.equals(publishedPartnerIds)) {
			partnerIds.removeAll(publishedPartnerIds);
		}
	}

	@Override
	public List<Integer> listPublishedPartneshipIdsByDamId(Integer damId) {
		try {
			return damDao.listPublishedPartnershipIdsByDamId(damId);
		} catch (DamDataAccessException mex) {
			throw new DamDataAccessException(mex);
		} catch (Exception e) {
			throw new DamDataAccessException(e);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public XtremandResponse listPublishedAssets(Pagination pagination) {
		try {
			XtremandResponse response = new XtremandResponse();
			response.setStatusCode(200);
			/**** XNFR- 409 ****/
			utilService.setDateFilters(pagination);
			/**** XNFR- 409 ****/
			utilService.setVanityUrlFilter(pagination);
			Map<String, Object> map = damDao.listPublishedAssets(pagination);
			List<PublishedAssetsView> publishedAssetsViewList = (List<PublishedAssetsView>) map.get("list");
			for (PublishedAssetsView publishedAssetsView : publishedAssetsViewList) {
				if (publishedAssetsView.getVideoId() != null) {
					String updatedThumbnailPath = serverPath + publishedAssetsView.getThumbnailPath();
					publishedAssetsView.setThumbnailPath(updatedThumbnailPath);
				}
				/*** XNFR-930 ***/
				if (XamplifyUtils.isValidString(publishedAssetsView.getAssetPath())) {
					String updatedAssetPath = xamplifyUtil
							.replaceS3WithCloudfrontViceVersa(publishedAssetsView.getAssetPath());
					publishedAssetsView.setAssetPath(updatedAssetPath);
				}
				if (XamplifyUtils.isValidString(publishedAssetsView.getThumbnailPath())) {
					String updatedThumbnailPathPath = xamplifyUtil
							.replaceS3WithCloudfrontViceVersa(publishedAssetsView.getThumbnailPath());
					publishedAssetsView.setThumbnailPath(updatedThumbnailPathPath);
				}
				/*** XNFR-930 ***/

				Integer vendorCompanyId = publishedAssetsView.getVendorCompanyId();
				Integer partnerCompanyId = publishedAssetsView.getPartnerCompanyId();
				String partnerStatus = partnershipDao
						.findPartnerShipStatusByPartnerCompanyIdAndVendorCompanyId(partnerCompanyId, vendorCompanyId);
				publishedAssetsView.setPartnerStatus(partnerStatus);
			}
			map.put("list", publishedAssetsViewList);
			response.setData(map);
			return response;
		} catch (DamDataAccessException mex) {
			throw new DamDataAccessException(mex);
		} catch (Exception e) {
			throw new DamDataAccessException(e);
		}
	}

	@Override
	public XtremandResponse updatePublishedAsset(DamPostDTO damPostDTO) {
		try {
			XtremandResponse response = new XtremandResponse();
			response.setStatusCode(200);
			damDao.updatePublishedAsset(damPostDTO);
			updateTags(null, damPostDTO);
			response.setMessage("Data updated successfully.");
			return response;
		} catch (DamDataAccessException mex) {
			throw new DamDataAccessException(mex);
		} catch (Exception e) {
			throw new DamDataAccessException(e);
		}
	}

	@Override
	public XtremandResponse listPublishedPartners(Pagination pagination) {
		try {
			XtremandResponse response = new XtremandResponse();
			response.setStatusCode(200);
			response.setData(damDao.listPublishedPartners(pagination));
			return response;
		} catch (DamDataAccessException mex) {
			throw new DamDataAccessException(mex);
		} catch (Exception e) {
			throw new DamDataAccessException(e);
		}
	}

	@Override
	public XtremandResponse listPartners(Pagination pagination) {
		try {
			XtremandResponse response = new XtremandResponse();
			response.setStatusCode(200);
			response.setData(damDao.listPartners(pagination));
			return response;
		} catch (DamDataAccessException mex) {
			throw new DamDataAccessException(mex);
		} catch (Exception e) {
			throw new DamDataAccessException(e);
		}
	}

	@Override
	public void downloadAsset(String alias, String pageSize, String pageOrientation) throws IOException {
		utilService.replaceHtmlBodyWithDynamicTags(alias, pageSize, false, pageOrientation, null);
		Integer damId = damDao.findAssetIdByAlias(alias);
		if (XamplifyUtils.isValidInteger(damId)) {
			asyncComponent.uploadDesignedPdfToAws(damId);
		}
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public void downloadPartnerAsset(String alias, GeoLocationAnalytics geoLocationAnalytics) throws IOException {
		utilService.replaceHtmlBodyWithDynamicTags(alias, null, true, null, geoLocationAnalytics.getUserId());
		Integer damPartnerId = damDao.getDamPartnerIdByAlias(alias);
		saveIntoDamAnalytics(geoLocationAnalytics.getUserId(), damPartnerId, 2, geoLocationAnalytics);
	}

	@Override
	public XtremandResponse updateDownloadOptions(DamDownloadPostDTO damDownloadPostDTO) {
		try {
			XtremandResponse response = new XtremandResponse();
			response.setStatusCode(200);
			damDao.updateDownloadOptions(damDownloadPostDTO);
			return response;
		} catch (DamDataAccessException mex) {
			throw new DamDataAccessException(mex);
		} catch (Exception e) {
			throw new DamDataAccessException(e);
		}
	}

	@Override
	public XtremandResponse getDownloadOptionsByAlias(String alias) {
		try {
			XtremandResponse response = new XtremandResponse();
			response.setStatusCode(200);
			Dam dam = damDao.getDownloadOptionsByAlias(alias);
			DamDownloadPostDTO damDownloadPostDTO = new DamDownloadPostDTO();
			BeanUtils.copyProperties(dam, damDownloadPostDTO);
			if (!StringUtils.hasText(damDownloadPostDTO.getPageSize())) {
				damDownloadPostDTO.setPageSize("A4");
			}
			response.setData(damDownloadPostDTO);
			return response;
		} catch (DamDataAccessException mex) {
			throw new DamDataAccessException(mex);
		} catch (Exception e) {
			throw new DamDataAccessException(e);
		}
	}

	private void saveTags(DamUploadPostDTO damUploadPostDTO, DamPostDTO damPostDto, Dam dam, Integer companyId,
			Integer loggedInUserId) {
		Set<Integer> tagIds = null;
		if (damUploadPostDTO != null) {
			tagIds = damUploadPostDTO.getTagIds();
		} else if (damPostDto != null) {
			tagIds = damPostDto.getTagIds();
		}
		if (tagIds != null && !tagIds.isEmpty()) {
			for (Integer tagId : tagIds) {
				if (tagId != null && tagId > 0) {
					saveTag(loggedInUserId, dam, companyId, tagId);
				}
			}
		}
	}

	private void saveTag(Integer loggedInUserId, Dam dam, Integer companyId, Integer tagId) {
		Tag tag = tagDao.getByIdAndCompanyId(tagId, companyId);
		saveTag(loggedInUserId, dam, tag);
	}

	private void saveTag(Integer loggedInUserId, Dam dam, Tag tag) {
		if (tag != null) {
			DamTag damTag = new DamTag();
			damTag.setCreatedBy(loggedInUserId);
			damTag.setUpdatedBy(loggedInUserId);
			damTag.setDam(dam);
			damTag.setTag(tag);
			damTag.initialiseCommonFields(true, loggedInUserId);
			genericDAO.save(damTag);
		}
	}

	private void copyFilesAndUploadAsynchronously(MultipartFile uploadedFile, DamUploadPostDTO damUploadPostDTO,
			MultipartFile thumbnailFile, Integer companyId, String fileType, DamAwsDTO damAwsDTO) throws IOException {
		CopiedFileDetails copiedAwsFileDetails = new CopiedFileDetails();
		String filePathSuffix = imageFolder + companyId;
		String thumbnailPathPrefix = thumbnailFolder + companyId;
		getCopiedAwsFileDetails(uploadedFile, damUploadPostDTO, damAwsDTO, copiedAwsFileDetails, filePathSuffix);
		if (thumbnailFile != null) {
			CopiedFileDetails thumbnailFileAwsDetails = new CopiedFileDetails();
			amazonWebService.copyFileToXampifyServerAndGenerateThumbnail(thumbnailFile,
					damUploadPostDTO.getLoggedInUserId(), thumbnailPathPrefix,
					copiedAwsFileDetails.getUpdatedFileName(), thumbnailFileAwsDetails, true);
			String thumbnailName = thumbnailFileAwsDetails.getCompleteThumbnailName();
			if (thumbnailName != null) {
				thumbnailName = thumbnailName.substring(0, thumbnailName.lastIndexOf('.')) + "."
						+ (thumbnailName.substring(thumbnailName.lastIndexOf('.') + 1)).toLowerCase();
				thumbnailFileAwsDetails.setCompleteThumbnailName(thumbnailName);
			}
			damAwsDTO.setCompleteThumbnailFileName(thumbnailName);
			damAwsDTO.setCopiedThumbnailFilePath(thumbnailFileAwsDetails.getCopiedImageFilePath());
		} else {
			String fileName = damUploadPostDTO.isCloudContent() ? damUploadPostDTO.getFileName()
					: uploadedFile.getOriginalFilename();
			amazonWebService.generateThumbnail(fileName, thumbnailPathPrefix, fileType, copiedAwsFileDetails);
			String thumbnailName = copiedAwsFileDetails.getCompleteThumbnailName();
			if (thumbnailName != null) {
				thumbnailName = thumbnailName.substring(0, thumbnailName.lastIndexOf('.')) + "."
						+ (thumbnailName.substring(thumbnailName.lastIndexOf('.') + 1)).toLowerCase();
				copiedAwsFileDetails.setCompleteThumbnailName(thumbnailName);
			}
			damAwsDTO.setCompleteThumbnailFileName(thumbnailName);
			damAwsDTO.setCopiedThumbnailFilePath(copiedAwsFileDetails.getThumbnailFilePath());
//			sharedAssetPath(uploadedFile, damUploadPostDTO, damAwsDTO);
		}
	}

	private void getCopiedAwsFileDetails(MultipartFile uploadedFile, DamUploadPostDTO damUploadPostDTO,
			DamAwsDTO damAwsDTO, CopiedFileDetails copiedAwsFileDetails, String filePathSuffix) throws IOException {
		String fileName = damUploadPostDTO.isCloudContent() ? damUploadPostDTO.getFileName()
				: uploadedFile.getOriginalFilename();
		if (damUploadPostDTO.isCloudContent()) {
			amazonWebService.copyCloudFileToXamplifyServer(damUploadPostDTO, damUploadPostDTO.getLoggedInUserId(),
					filePathSuffix, fileName, copiedAwsFileDetails, false);
		} else {
			amazonWebService.copyFileToXamplifyServer(uploadedFile, damUploadPostDTO.getLoggedInUserId(),
					filePathSuffix, fileName, copiedAwsFileDetails, false);
		}
		String assetFileName = copiedAwsFileDetails.getCompleteName();
		assetFileName = assetFileName.substring(0, assetFileName.lastIndexOf('.')) + "."
				+ (assetFileName.substring(assetFileName.lastIndexOf('.') + 1)).toLowerCase();
		copiedAwsFileDetails.setCompleteName(assetFileName);
		damAwsDTO.setCompleteAssetFileName(assetFileName);
		damAwsDTO.setCopiedAssetFilePath(copiedAwsFileDetails.getCopiedImageFilePath());
		damUploadPostDTO.setCompleteAssetFileName(assetFileName);
		damUploadPostDTO.setCopiedAssetFilePath(copiedAwsFileDetails.getCopiedImageFilePath());
	}

	private void saveIntoDam(Dam dam) {
		try {
			damDao.save(dam);
		} catch (DamDataAccessException mex) {
			throw new DamDataAccessException(mex);
		} catch (Exception e) {
			throw new DamDataAccessException(e);
		}
	}

	@Override
	public XtremandResponse update(DamUploadPostDTO damUploadPostDTO, MultipartFile thumbnailFile) {
		try {
			XtremandResponse response = new XtremandResponse();
			if (thumbnailFile != null) {
				Integer companyId = damDao.getCompanyIdById(damUploadPostDTO.getId());
				Integer loggedInUserId = damUploadPostDTO.getLoggedInUserId();
				Integer id = damUploadPostDTO.getId();
				String filePathSuffix = awsDamFolder + '/' + companyId + '/' + id;
				String fileType = fileUtil.getFileExtension(thumbnailFile);
				AWSInputDTO awsInputDTO = new AWSInputDTO();
				awsInputDTO.setOriginalFie(thumbnailFile);
				awsInputDTO.setUserId(loggedInUserId);
				awsInputDTO.setFilePathSuffix(filePathSuffix);
				awsInputDTO.setFileType(fileType);
				awsInputDTO.setThumbnailFile(thumbnailFile);
				FilePathAndThumbnailPath filePathAndThumbnailPath = amazonWebService
						.uploadFileToAwsAndGetPath(awsInputDTO);
				String thumbnailPath = filePathAndThumbnailPath.getThumbnailPath();
				String updatedThumbnailPath = thumbnailPath != null && StringUtils.hasText(thumbnailPath)
						? thumbnailPath
						: xamplifyUtil.getThumbnailPathByFileType(fileType);
				damUploadPostDTO.setThumbnailPath(updatedThumbnailPath);
			}
			damDao.update(damUploadPostDTO);
			updateTags(damUploadPostDTO, null);
			response.setStatusCode(200);
			return response;
		} catch (DuplicateEntryException mex) {
			throw new DuplicateEntryException(mex.getMessage());
		} catch (DamDataAccessException mex) {
			throw new DamDataAccessException(mex);
		} catch (Exception e) {
			throw new DamDataAccessException(e);
		}
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public XtremandResponse updateAssetDetailsAndReplaceAsset(DamUploadPostDTO damUploadPostDTO,
			MultipartFile thumbnailFile, MultipartFile uploadedFile,
			PendingApprovalDamAndLmsDTO pendingApprovalDamAndLmsDTO) {
		try {
			XtremandResponse response = new XtremandResponse();
			Integer id = damUploadPostDTO.getId();
			Dam dam = damDao.getById(id);
			boolean isPublished = damDao.isAssetPublished(id);
			boolean isHistoryTemplate = dam.getParentId() != null && dam.getParentId() > 0;
			Integer companyId = damDao.getCompanyIdById(damUploadPostDTO.getId());
			/** XNFR-884 **/
			processApprovalStatusUpdate(damUploadPostDTO, dam, companyId);
			if (isHistoryTemplate) {
				Integer categoryIdInput = damUploadPostDTO.getCategoryId();
				Integer parentTemplateCategoryId = damDao.getParentTemplateCategoryIdByHistoryTemplateId(id);
				if (parentTemplateCategoryId != null && !parentTemplateCategoryId.equals(categoryIdInput)) {
					throw new BadRequestException("Folder cannot be changed for history templates");
				} else if (parentTemplateCategoryId != null) {
					updateDamDetails(damUploadPostDTO, thumbnailFile, dam, companyId);
				}
			} else {
				updateDamDetails(damUploadPostDTO, thumbnailFile, dam, companyId);
			}
			if (dam.isBeeTemplate()) {
				dam.setHtmlBody(damUploadPostDTO.getHtmlBody());
				dam.setJsonBody(damUploadPostDTO.getJsonBody());
			}
			/********* XNFR-255 **********/
			publishOrWhiteLabelAsset(damUploadPostDTO, response, id);
			/********* XNFR-255 **********/

			/*** XNFR-434 ****/
			copyReplaceAssets(damUploadPostDTO, thumbnailFile, uploadedFile, response, companyId, dam);
			/*** XNFR-434 ****/

			/*** XNFR-833 ****/
//			updateAssePathForEnabledVendorSignature(damUploadPostDTO, thumbnailFile, uploadedFile, response, companyId, dam);
			/*** XNFR-833 ****/

			if (!damUploadPostDTO.isDraft()
					&& (damUploadPostDTO.isSendForApproval() || damUploadPostDTO.isSendForReApproval())) {
				buildPendingApprovalDamForEmailNotification(damUploadPostDTO, dam.getAssetType(), companyId, dam,
						pendingApprovalDamAndLmsDTO);
			}

			if (damUploadPostDTO.isSendForApproval() && !damUploadPostDTO.isDraft()) {
				commentDao.createApprovalStatusHistory(id, damUploadPostDTO.getLoggedInUserId(), ModuleType.DAM,
						damUploadPostDTO.getApprovalStatusInString());
			}

			response.setStatusCode(200);
			/********* XNFR-342 *****/
			boolean isPartnerListOrPartnerGroupSelected = XamplifyUtils.isNotEmptySet(damUploadPostDTO.getPartnerIds())
					|| XamplifyUtils.isNotEmptySet(damUploadPostDTO.getPartnerGroupIds());
			if (isPartnerListOrPartnerGroupSelected && !isPublished) {
				response.setMessage("Updated & Published Successfully");
			} else {
				response.setMessage("Updated Successfully");
			}

			/********* XNFR-342 *****/
			return response;
		} catch (BadRequestException mex) {
			throw new BadRequestException(mex.getMessage());
		} catch (DuplicateEntryException mex) {
			throw new DuplicateEntryException(mex.getMessage());
		} catch (DamDataAccessException mex) {
			throw new DamDataAccessException(mex);
		} catch (Exception e) {
			throw new DamDataAccessException(e);
		}
	}

	/********* XNFR-255 **********/
	private void publishOrWhiteLabelAsset(DamUploadPostDTO damUploadPostDTO, XtremandResponse response, Integer id) {
		Map<String, Object> map = new HashMap<>();
		map.put(PUBLISH_ASSET, false);
		map.put(SHARE_WHITE_LABELED_ASSET, false);
		Set<Integer> partnerIds = damUploadPostDTO.getPartnerIds();
		Set<Integer> partnerGroupIds = damUploadPostDTO.getPartnerGroupIds();
		/*** Added On 03/08/2023 ****/
		if (partnerGroupIds != null && partnerIds != null && partnerIds.isEmpty() && !partnerGroupIds.isEmpty()) {
			damUploadPostDTO.setPartnerGroupSelected(true);
		}
		/*** Added On 03/08/2023 ****/
		boolean isPartnerGroupSelected = damUploadPostDTO.isPartnerGroupSelected();
		boolean isShareAsWhiteLabeledAsset = damUploadPostDTO.isShareAsWhiteLabeledAsset();

		findUpdatedPartnerCompanyIdsForPublishing(isPartnerGroupSelected, id, map, partnerIds, partnerGroupIds);

		findUpdatedPartnerCompanyIdsForWhiteLabelingTheAsset(id, map, partnerIds, partnerGroupIds,
				isPartnerGroupSelected, isShareAsWhiteLabeledAsset);
		response.setMap(map);
		boolean publishAsset = (boolean) map.get(PUBLISH_ASSET);
		boolean shareAsWhiteLabeledAsset = (boolean) map.get(SHARE_WHITE_LABELED_ASSET);
		if (publishAsset || shareAsWhiteLabeledAsset) {
			damDao.updatePublishingOrWhiteLabeledStatus(id);
		}
	}

	/********* XNFR-255 **********/
	private void findUpdatedPartnerCompanyIdsForPublishing(boolean isPartnerGroupSelected, Integer id,
			Map<String, Object> map, Set<Integer> partnerIds, Set<Integer> partnerGroupIds) {
		boolean isPublishedToPartnerGroups = damDao.isPublishedToPartnerGroups(id);
		if (isPartnerGroupSelected) {
			findUpdatedPartnerCompanyIdsByPartnerGroup(id, map, partnerGroupIds, isPublishedToPartnerGroups);
		} else {
			findUpdatedPartnerCompanyIdsByPartnerCompanies(id, map, partnerIds, isPublishedToPartnerGroups);
		}
	}

	/********* XNFR-255 **********/
	private void findUpdatedPartnerCompanyIdsByPartnerCompanies(Integer id, Map<String, Object> map,
			Set<Integer> partnerIds, boolean isPublishedToPartnerGroups) {
		boolean isPartnerIdsEmpty = partnerIds == null || partnerIds.isEmpty();
		if (isPublishedToPartnerGroups || isPartnerIdsEmpty) {
			damDao.deleteDamPartnerByDamIdAndUnpublishDam(id);
		}
		if (!isPartnerIdsEmpty) {
			List<Integer> partnerIdsArrayList = XamplifyUtils.convertSetToList(partnerIds);
			List<Integer> publishedPartnerIds = damDao.findPublishedPartnerIdsByDamId(id);
			/***** XBI-1829 ****/
			List<Integer> publishedPartnerIdsForMap = new ArrayList<>();
			publishedPartnerIdsForMap.addAll(publishedPartnerIds);
			map.put(PUBLISHED_PARTNER_USER_IDS, publishedPartnerIdsForMap);
			/***** XBI-1829 ****/
			Collections.sort(partnerIdsArrayList);
			Collections.sort(publishedPartnerIds);
			if (partnerIdsArrayList.equals(publishedPartnerIds)) {
				map.put(PUBLISH_ASSET, false);
			} else {
				publishedPartnerIds.removeAll(partnerIdsArrayList);
				damDao.findAndDeleteDamPartnerMappingIds(id, publishedPartnerIds);
//				damDao.findAndDeleteUnMappedRowsFromDamPartnerOrDamPartnerGroupMapping(isPublishedToPartnerGroups);
				removeUnselectedPartnerIds(id, partnerIdsArrayList);
				List<Integer> partnerCompanyIds = userDao
						.findCompanyIdsByUserIds(XamplifyUtils.convertListToSetElements(partnerIdsArrayList));
				map.put(PUBLISH_ASSET, true);
				map.put("partnerIds", partnerIdsArrayList);
				map.put("partnerCompanyIds", partnerCompanyIds);
				map.put("publishingToPartnersInsidePartnerList", false);
			}
		}
	}

	/********* XNFR-255 **********/
	private void findUpdatedPartnerCompanyIdsByPartnerGroup(Integer id, Map<String, Object> map,
			Set<Integer> partnerGroupIds, boolean isPublishedToPartnerGroups) {
		boolean isGroupIdsEmpty = partnerGroupIds == null || partnerGroupIds.isEmpty();
		if (!isPublishedToPartnerGroups || isGroupIdsEmpty) {
			damDao.deleteDamPartnerByDamIdAndUnpublishDam(id);
		}
		if (!isGroupIdsEmpty) {
			List<Integer> publishedPartnerGroupIds = damDao.findPublishedPartnerGroupIdsByDamId(id);
			List<Integer> partnerGroupIdsArrayList = XamplifyUtils.convertSetToList(partnerGroupIds);
			Collections.sort(partnerGroupIdsArrayList);
			Collections.sort(publishedPartnerGroupIds);
			if (partnerGroupIdsArrayList.equals(publishedPartnerGroupIds)) {
				comparePublishedPartnerIds(id, map, partnerGroupIdsArrayList);
			} else {
				map.put(PUBLISH_ASSET, true);
				publishedPartnerGroupIds.removeAll(partnerGroupIdsArrayList);
				damDao.findAndDeleteDamPartnerGroupMappingIds(id, publishedPartnerGroupIds);
//				damDao.findAndDeleteUnMappedRowsFromDamPartnerOrDamPartnerGroupMapping(isPublishedToPartnerGroups);
				removeUnselectedPartnerGroupIds(id, partnerGroupIdsArrayList);
				map.put("partnerGroupIds", partnerGroupIdsArrayList);
				/***** XBI-1829 ****/
				List<Integer> publishedPartnerUserIds = damDao.findPublishedPartnerUserIdsByDamId(id);
				map.put(PUBLISHED_PARTNER_USER_IDS, publishedPartnerUserIds);
				/***** XBI-1829 ****/

			}
		}
	}

	private void comparePublishedPartnerIds(Integer id, Map<String, Object> map,
			List<Integer> partnerGroupIdsArrayList) {
		/****** XNFR-342 *****/
		if (XamplifyUtils.isNotEmptyList(partnerGroupIdsArrayList) && partnerGroupIdsArrayList.size() == 1) {
			List<Integer> publishedUserListUserIds = damDao.findPublishedPartnerUserIdsByDamId(id);
			List<Integer> userIdsByUserListId = userListDao.findUserIdsByUserListId(partnerGroupIdsArrayList.get(0));
			Collections.sort(publishedUserListUserIds);
			Collections.sort(userIdsByUserListId);
			boolean isUniqueArrayList = XamplifyUtils.isUniqueArrayLists(publishedUserListUserIds, userIdsByUserListId);
			if (isUniqueArrayList) {
				map.put(PUBLISH_ASSET, false);
			} else {
				userIdsByUserListId.removeAll(publishedUserListUserIds);
				map.put(PUBLISH_ASSET, true);
				map.put("partnerGroupIds", partnerGroupIdsArrayList);
				map.put(PUBLISHED_PARTNER_USER_IDS, userIdsByUserListId);
				map.put("publishingToPartnersInsidePartnerList", true);
			}

		} else {
			map.put(PUBLISH_ASSET, false);
		}
	}

	/********* XNFR-255 **********/
	private void findUpdatedPartnerCompanyIdsForWhiteLabelingTheAsset(Integer id, Map<String, Object> map,
			Set<Integer> partnerIds, Set<Integer> partnerGroupIds, boolean isPartnerGroupSelected,
			boolean shareAsWhiteLabeledAsset) {
		boolean isPartnerGroupOrPartnerCompanySelected = XamplifyUtils
				.isPartnerGroupOrPartnerCompanySelected(partnerGroupIds, partnerIds);
		if (isPartnerGroupOrPartnerCompanySelected && shareAsWhiteLabeledAsset) {
			List<Integer> partnerCompanyIds = new ArrayList<>();
			if (isPartnerGroupSelected) {
				partnerCompanyIds.addAll(userListDao.findPartnerCompanyIdsByUserListIds(partnerGroupIds));
			} else {
				partnerCompanyIds.addAll(userDao.findCompanyIdsByUserIds(partnerIds));
			}
			List<Integer> onlyPartnerCompanyIds = utilDao.filterOnlyPartnerCompanyIds(partnerCompanyIds);
			partnerCompanyIds.removeAll(onlyPartnerCompanyIds);
			Collections.sort(partnerCompanyIds);

			List<Integer> assetSharedWithPartnerCompanyIds = whiteLabeledAssetDao
					.findPartnerCompanyIdsByVendorAssetId(id);
			Collections.sort(assetSharedWithPartnerCompanyIds);

			if (partnerCompanyIds.equals(assetSharedWithPartnerCompanyIds)) {
				map.put(SHARE_WHITE_LABELED_ASSET, false);
			} else {
				partnerCompanyIds.removeAll(assetSharedWithPartnerCompanyIds);
				map.put(SHARE_WHITE_LABELED_ASSET, true);
			}

		}
	}

	private void updateDamDetails(DamUploadPostDTO damUploadPostDTO, MultipartFile thumbnailFile, Dam dam,
			Integer companyId) throws IOException {
		boolean isInputThumbnailFileIsImageFile = false;
		String fileType = null;
		if (thumbnailFile != null) {
			String inputThumbnailFileType = fileUtil.getFileExtension(thumbnailFile);
			isInputThumbnailFileIsImageFile = inputThumbnailFileType != null
					&& StringUtils.hasText(inputThumbnailFileType)
					&& xamplifyUtil.convertStringToArrayListWithCommaSeperator(imageFormats)
							.indexOf(inputThumbnailFileType) > -1
					&& !"gif".equals(inputThumbnailFileType);
			fileType = fileUtil.getFileExtension(thumbnailFile);
		}
		boolean isUploadedAssetIsImageFile = false;
		String uploadedAssetName = null;
		String uploadedAssetFileType = null;
		if (XamplifyUtils.isValidString(dam.getAssetPath())) {
			String uploadedAssetPath = dam.getAssetPath();
			uploadedAssetName = uploadedAssetPath.substring(uploadedAssetPath.lastIndexOf("/") + 1);
			uploadedAssetFileType = fileUtil.getFileExtension(uploadedAssetName);
			isUploadedAssetIsImageFile = uploadedAssetFileType != null && StringUtils.hasText(uploadedAssetFileType)
					&& xamplifyUtil.convertStringToArrayListWithCommaSeperator(imageFormats)
							.indexOf(uploadedAssetFileType) > -1
					&& !"gif".equals(uploadedAssetFileType);
		}

		if (thumbnailFile != null && isInputThumbnailFileIsImageFile && !dam.isBeeTemplate()) {
			AWSInputDTO awsInputDTO = setAWSInputDTOAndDeleteThumbnail(damUploadPostDTO, thumbnailFile, dam, fileType,
					companyId);

			setFilePathAndThumbnailPath(damUploadPostDTO, fileType, isUploadedAssetIsImageFile, uploadedAssetFileType,
					awsInputDTO);
		} else if (thumbnailFile != null && isInputThumbnailFileIsImageFile && dam.isBeeTemplate()) {
			uploadThumbnailToAWS(damUploadPostDTO, thumbnailFile, dam, fileType, companyId);
		} else {
			damUploadPostDTO.setThumbnailPath(null);
		}
		/******* XNFR-255 *****/
		damUploadPostDTO.setDescription(utilService.replacedDescription(damUploadPostDTO.getDescription()));
		updateAssetDetails(damUploadPostDTO, dam);
		saveDamTagsForExistingAsset(damUploadPostDTO.getTagIds(), damUploadPostDTO.getLoggedInUserId(), dam);
		/********** XNFR-169 *******************/
		Integer categoryId = damUploadPostDTO.getCategoryId();
		Integer damId = damUploadPostDTO.getId();
		Integer loggedInUserId = damUploadPostDTO.getLoggedInUserId();
		if (XamplifyUtils.isValidInteger(categoryId)) {
			CategoryModule categoryModule = damDao.findCategoryByDamId(damId);
			if (categoryModule != null) {
				categoryModule.setCategoryId(categoryId);
				categoryModule.setUpdatedUserId(loggedInUserId);
				categoryModule.setUpdatedTime(new Date());
			}
			categoryDao.updateHistoryTemplatesCategoryId(categoryId, damId, loggedInUserId);
		}
		/********** XNFR-169 *******************/
	}

	private void updateAssetDetails(DamUploadPostDTO damUploadPostDTO, Dam dam) {
		dam.setAssetName(damUploadPostDTO.getAssetName());
		dam.setDescription(damUploadPostDTO.getDescription());
		dam.setUpdatedBy(damUploadPostDTO.getLoggedInUserId());
		dam.setUpdatedTime(new Date());
		dam.setWhiteLabeledAssetSharedWithPartners(damUploadPostDTO.isShareAsWhiteLabeledAsset());
		dam.setAddedToQuickLinks(damUploadPostDTO.isAddedToQuickLinks());
		dam.setPartnerSignatureRequired(damUploadPostDTO.isPartnerSignatureRequired());
		dam.setVendorSignatureRequired(damUploadPostDTO.isVendorSignatureRequired());
		dam.setVendorSignatureRequiredAfterPartnerSignature(
				damUploadPostDTO.isVendorSignatureRequiredAfterPartnerSignature());
		dam.setSlug(damUploadPostDTO.getSlug());
		if (XamplifyUtils.isValidString(damUploadPostDTO.getThumbnailPath())) {
			dam.setThumbnailPath(damUploadPostDTO.getThumbnailPath());
		}
		if (damUploadPostDTO.isUpdateApprovalStatus()
				&& XamplifyUtils.isValidString(damUploadPostDTO.getApprovalStatusInString())) {
			dam.setApprovalStatus(approveDao.getApprovalStatusByString(damUploadPostDTO.getApprovalStatusInString()));
			dam.setApprovalStatusUpdatedBy(damUploadPostDTO.getApprovalStatusUpdatedby());
			dam.setApprovalStatusUpdatedTime(new Date());
		}
	}

	private void copyReplaceAssets(DamUploadPostDTO damUploadPostDTO, MultipartFile thumbnailFile,
			MultipartFile uploadedFile, XtremandResponse response, Integer companyId, Dam dam) throws IOException {
		if ((uploadedFile != null || (damUploadPostDTO.getDownloadLink() != null
				&& StringUtils.hasText(damUploadPostDTO.getDownloadLink())))
				&& XamplifyUtils.isValidInteger(damUploadPostDTO.getId())) {
			dam.setDamStatusEnum(DamStatusEnum.PROCESSING);
			dam.setAssetName(XamplifyUtils.removeExtraSpace(damUploadPostDTO.getAssetName()));
			dam.setDescription(damUploadPostDTO.getDescription());
			dam.setUpdatedBy(damUploadPostDTO.getLoggedInUserId());
			dam.setUpdatedTime(new Date());
			dam.setWhiteLabeledAssetSharedWithPartners(damUploadPostDTO.isShareAsWhiteLabeledAsset());
			dam.setVendorSignatureRequired(damUploadPostDTO.isVendorSignatureRequired());
			dam.setPartnerSignatureRequired(damUploadPostDTO.isPartnerSignatureRequired());
			dam.setVendorSignatureRequiredAfterPartnerSignature(
					damUploadPostDTO.isVendorSignatureRequiredAfterPartnerSignature());
			dam.setVendorSignatureCompleted(false);
			if (StringUtils.hasText(damUploadPostDTO.getThumbnailPath())) {
				dam.setThumbnailPath(damUploadPostDTO.getThumbnailPath());
			}
			String uploadedFileType = null;
			if (damUploadPostDTO.isCloudContent()) {
				uploadedFileType = fileUtil.getFileExtension(damUploadPostDTO.getFileName());
			} else {
				uploadedFileType = fileUtil.getFileExtension(uploadedFile);
			}
			DamAwsDTO damAwsDTO = new DamAwsDTO();
			damAwsDTO.setDamId(damUploadPostDTO.getId());
			damAwsDTO.setFileType(uploadedFileType);
			damAwsDTO.setUserId(damUploadPostDTO.getLoggedInUserId());
			damAwsDTO.setAssetName(XamplifyUtils.removeExtraSpace(damUploadPostDTO.getAssetName()));
			damUploadPostDTO.setReplaceAsset(true);
			damAwsDTO.setReplaceAsset(true);
			/**** Move the asset to xampify server path ***********/

			copyFilesAndUploadAsynchronously(uploadedFile, damUploadPostDTO, thumbnailFile, companyId, uploadedFileType,
					damAwsDTO);
			Map<String, Object> map = response.getMap();
			map.put("dto", damAwsDTO);
			setIsVideoFileParameter(map, false);
		}
	}

	private void updateAssePathForEnabledVendorSignature(DamUploadPostDTO damUploadPostDTO, MultipartFile thumbnailFile,
			MultipartFile uploadedFile, XtremandResponse response, Integer companyId, Dam dam) {
		if (damUploadPostDTO.isVendorSignatureRequired() && !damUploadPostDTO.isVendorSignatureCompleted()
				&& XamplifyUtils.isValidString(damUploadPostDTO.getSelectedSignatureImagePath()) && !dam.isBeeTemplate()
				&& !damUploadPostDTO.isReplaceAsset()) {
			Path filePathToDelete = null;
			Integer loggedInUserId = damUploadPostDTO.getLoggedInUserId();
			String companyName = userDao.getCompanyNameByUserId(loggedInUserId);
			User user = userDao.getFirstNameLastNameAndJobTitleByUserId(loggedInUserId);
			String userAlias = userDao.getAliasByUserId(loggedInUserId);
			String signaturePath = fileUtil.createSignatureFolderPath(userAlias);
			String selectedSignatureImagePath = damUploadPostDTO.getSelectedSignatureImagePath();
			String originalFilename = selectedSignatureImagePath
					.substring(selectedSignatureImagePath.lastIndexOf("/") + 1);
			String completeSignaturePath = signaturePath + sep + originalFilename;
			if (XamplifyUtils.isValidString(damUploadPostDTO.getAssetPath())) {
				updateVendorAssetPathAndDeleteLocalFIle(damUploadPostDTO, response, dam, filePathToDelete,
						loggedInUserId, companyName, user, selectedSignatureImagePath, completeSignaturePath);
			}
		}
	}

	private void updateVendorAssetPathAndDeleteLocalFIle(DamUploadPostDTO damUploadPostDTO, XtremandResponse response,
			Dam dam, Path filePathToDelete, Integer loggedInUserId, String companyName, User user,
			String selectedSignatureImagePath, String completeSignaturePath) {
		String pdfUrl = damUploadPostDTO.getAssetPath();
		/*** XNFR-930 ***/
		String updatedPdfUrl = xamplifyUtil.replaceS3WithCloudfrontViceVersa(pdfUrl);
		/**** XNFR-930 ***/
		File originalPdfFile = downloadFileFromS3(updatedPdfUrl);
		try (PDDocument document = PDDocument.load(originalPdfFile)) {
			if (document.isEncrypted()) {
				document.setAllSecurityToBeRemoved(true);
			}
			if (XamplifyUtils.isValidString(selectedSignatureImagePath)) {
				File signatureImageFile = new File(completeSignaturePath);
				addVendorSignature(document, signatureImageFile, user, companyName,
						damUploadPostDTO.getGeoLocationDetails());
			}

			String modifiedPdfFileName = "modified_" + UUID.randomUUID().toString() + ".pdf";
			String modifiedPdfFilePath = fileUtil.getPathForModifiedSharedAssetPdfPath("shared-asset",
					modifiedPdfFileName, loggedInUserId);
			try (FileOutputStream fos = new FileOutputStream(modifiedPdfFilePath)) {
				document.save(fos);
			}
			String uniqueAssetFileName = damUploadPostDTO.getAssetPath().replace("https://s3.amazonaws.com/xamplify/",
					"");
			xamplifyUtil.saveFileToAws(xamplifyUtil.getAmazonClient(), uniqueAssetFileName, modifiedPdfFilePath, null);
			filePathToDelete = Paths.get(modifiedPdfFilePath);
			response.setFilePath(amazonBaseUrl + xamplifyUtil.getBucketName() + '/' + uniqueAssetFileName);
			String updatedSharedAssetPath = amazonBaseUrl + xamplifyUtil.getBucketName() + '/' + uniqueAssetFileName;
			damDao.updateAssetPathForVendor(updatedSharedAssetPath, dam.getId());
			dam.setAssetPath(updatedSharedAssetPath);
		} catch (IOException e) {
			e.printStackTrace();
		}
		deleteLocalFilePath(filePathToDelete);
	}

	private void deleteLocalFilePath(Path filePathToDelete) {
		if (filePathToDelete != null) {
			try {
				Files.delete(filePathToDelete);
				String debugMessage = filePathToDelete + " deleted successfully";
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private AWSInputDTO setAWSInputDTOAndDeleteThumbnail(DamUploadPostDTO damUploadPostDTO, MultipartFile thumbnailFile,
			Dam dam, String fileType, Integer companyId) {
		Integer loggedInUserId = damUploadPostDTO.getLoggedInUserId();
		String filePathSuffix = thumbnailFolder + companyId;
		AWSInputDTO awsInputDTO = new AWSInputDTO();
		awsInputDTO.setOriginalFie(thumbnailFile);
		awsInputDTO.setUserId(loggedInUserId);
		awsInputDTO.setFilePathSuffix(filePathSuffix);
		awsInputDTO.setFileType(fileType);
		awsInputDTO.setThumbnailFile(thumbnailFile);
		awsInputDTO.setCompanyId(companyId);
		deleteThumbnailFromAWSByKey(dam);
		return awsInputDTO;
	}

	private void setFilePathAndThumbnailPath(DamUploadPostDTO damUploadPostDTO, String fileType,
			boolean isUploadedAssetIsImageFile, String uploadedAssetFileType, AWSInputDTO awsInputDTO) {
		FilePathAndThumbnailPath filePathAndThumbnailPath = null;
		if (isUploadedAssetIsImageFile) {
			filePathAndThumbnailPath = setFilePathAndThumbnailPath(fileType, uploadedAssetFileType, awsInputDTO);
		} else {
			filePathAndThumbnailPath = amazonWebService.uploadFileToAwsAndGetPathNew(awsInputDTO);
		}
		if (filePathAndThumbnailPath.getThumbnailPath() != null) {
			damUploadPostDTO.setThumbnailPath(filePathAndThumbnailPath.getThumbnailPath());
		}
	}

	private FilePathAndThumbnailPath setFilePathAndThumbnailPath(String fileType, String uploadedAssetFileType,
			AWSInputDTO awsInputDTO) {
		FilePathAndThumbnailPath filePathAndThumbnailPath;
		if (uploadedAssetFileType.equalsIgnoreCase(fileType)) {
			filePathAndThumbnailPath = amazonWebService.uploadFileToAwsAndGetPathNew(awsInputDTO);
		} else {
			filePathAndThumbnailPath = amazonWebService.uploadFileToAwsAndGetPathNew(awsInputDTO);
		}
		return filePathAndThumbnailPath;
	}

	private void uploadThumbnailToAWS(DamUploadPostDTO damUploadPostDTO, MultipartFile thumbnailFile, Dam dam,
			String fileType, Integer companyId) throws IOException {
		String uploadedThumbnailPath = dam.getThumbnailPath();
		String uploadedThumbnailName = uploadedThumbnailPath.substring(uploadedThumbnailPath.lastIndexOf("/") + 1);
		List<KeyVersion> keys = new ArrayList<>();
		if (uploadedThumbnailName.contains("_thumb.png")) {
			String key1 = amazonEnvFolder + thumbnailFolder + companyId + "/" + uploadedThumbnailName;
			String assetName = uploadedThumbnailName.replace("_thumb.png", "");
			String key2 = amazonEnvFolder + imageFolder + companyId + "/" + assetName;
			keys.add(new KeyVersion(key1));
			keys.add(new KeyVersion(key2));
		} else if (uploadedThumbnailPath.contains("previews")) {
			String key = amazonEnvFolder + amazonPreviewFolder + companyId + xamplifyUtil.getSubFolderPath("dam") + "/"
					+ uploadedThumbnailName;
			keys.add(new KeyVersion(key));
		}
		amazonWebService.deleteAssetOrThumbnail(keys);
		DamAwsDTO damAwsDTO = new DamAwsDTO();
		copyFilesAndUploadAsynchronously(thumbnailFile, damUploadPostDTO, null, companyId, fileType, damAwsDTO);
		FilePathAndThumbnailPath filePathAndThumbnailPath = amazonWebService.uploadAssetAndThumbnail(damAwsDTO);
		damUploadPostDTO.setThumbnailPath(filePathAndThumbnailPath.getThumbnailPath());
	}

	private void deleteThumbnailFromAWSByKey(Dam dam) {
		if (!dam.isBeeTemplate()) {
			String uploadedThumbnailPath = dam.getThumbnailPath();
			String uploadedThumbnailName = uploadedThumbnailPath.substring(uploadedThumbnailPath.lastIndexOf("/") + 1);
			AmazonWebModel amazonWebModel = new AmazonWebModel();
			List<String> awsFileKeys = new ArrayList<>();
			awsFileKeys.add(uploadedThumbnailName);
			amazonWebModel.setAwsFileKeys(awsFileKeys);
			amazonWebService.deleteThumbnail(amazonWebModel);
		}
	}

	private void updateTags(DamUploadPostDTO damUploadPostDTO, DamPostDTO damPostDto) {
		Integer damId = 0;
		Set<Integer> newIds = null;
		Integer loggedInUserId = null;
		if (damUploadPostDTO != null) {
			damId = damUploadPostDTO.getId();
			newIds = damUploadPostDTO.getTagIds();
			loggedInUserId = damUploadPostDTO.getLoggedInUserId();
		} else if (damPostDto != null) {
			damId = damPostDto.getId();
			newIds = damPostDto.getTagIds();
			loggedInUserId = damPostDto.getCreatedBy();
		}
		Dam existingDam = damDao.getById(damId);
		if (existingDam != null) {
			saveDamTagsForExistingAsset(newIds, loggedInUserId, existingDam);
		}
	}

	private void saveDamTagsForExistingAsset(Set<Integer> newIds, Integer loggedInUserId, Dam existingDam) {
		Integer companyId = 0;
		if (existingDam.getCompanyProfile() != null && existingDam.getCompanyProfile().getId() > 0) {
			companyId = existingDam.getCompanyProfile().getId();
		}
		Set<DamTag> damTags = existingDam.getTags();
		Set<Integer> existingTagIds = new LinkedHashSet<>();
		iterateTagsandRemove(newIds, damTags, existingTagIds);
		if (newIds != null && !newIds.isEmpty()) {
			newIds.removeAll(existingTagIds);
			for (Integer newId : newIds) {
				Tag tag = tagDao.getByIdAndCompanyId(newId, companyId);
				saveTag(loggedInUserId, existingDam, tag);
			}
		}
	}

	private void iterateTagsandRemove(Set<Integer> newIds, Set<DamTag> damTags, Set<Integer> existingTagIds) {
		for (DamTag damTag : damTags) {
			Integer tagId = damTag.getTag().getId();
			existingTagIds.add(tagId);
			if (newIds == null || newIds.isEmpty() || !newIds.contains(tagId)) {
				genericDAO.remove(damTag);
			}
		}
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public String getAssetPathByAlias(GeoLocationAnalytics geoLocationAnalytics, String alias, Integer id,
			boolean isPartnerContent, OAuth2Authentication auth, Integer userId) {
		try {
			Dam dam = genericDAO.get(Dam.class, id);
			String downloadUrl = "";
			if (isPartnerContent) {
				downloadUrl = setParentContentDownloadUrl(geoLocationAnalytics, alias, isPartnerContent, auth, dam);
			} else {
				downloadUrl = setDownloadUrl(alias, isPartnerContent, auth, userId, dam);
			}
			return downloadUrl;
		} catch (DamDataAccessException mex) {
			throw new DamDataAccessException(mex);
		} catch (Exception e) {
			throw new DamDataAccessException(e);
		}
	}

	private String setDownloadUrl(String alias, boolean isPartnerContent, OAuth2Authentication auth, Integer userId,
			Dam dam) {
		String downloadUrl;
		if (hasDownloadAssetAccess(dam.getId(), userId)) {
			if (dam.getVideoFile() != null && dam.getVideoFile().getId() != null) {
				downloadUrl = getVideoAssetPath(auth, dam);
			} else {
				downloadUrl = damDao.getAssetPathByAlias(alias, isPartnerContent);
			}
		} else {
			downloadUrl = webUrl + "404";
		}
		return downloadUrl;
	}

	private String setParentContentDownloadUrl(GeoLocationAnalytics geoLocationAnalytics, String alias,
			boolean isPartnerContent, OAuth2Authentication auth, Dam dam) {
		String downloadUrl;
		if (dam.getVideoFile() != null && dam.getVideoFile().getId() != null) {
			downloadUrl = getVideoAssetPath(auth, dam);
		} else {
			downloadUrl = damDao.getAssetPathByAlias(alias, isPartnerContent);
		}
		Integer damPartnerId = damDao.getDamPartnerIdByAlias(alias);
		saveIntoDamAnalytics(geoLocationAnalytics.getUserId(), damPartnerId, 2, geoLocationAnalytics);
		return downloadUrl;
	}

	public String getVideoAssetPath(OAuth2Authentication auth, Dam dam) {
		final OAuth2AuthenticationDetails details = (OAuth2AuthenticationDetails) auth.getDetails();
		return videoService.getVideoPath(dam.getVideoFile().getId()) + "?access_token=" + details.getTokenValue();
	}

	@Override
	public XtremandResponse delete(DamUploadPostDTO dto) {
		try {
			XtremandResponse response = new XtremandResponse();
			Integer damId = dto.getId();
			Dam dam = genericDAO.get(Dam.class, damId);
			String assetType = dam.getAssetType();
			boolean isVideoFile = fileUtil.isVideoFileByType(assetType);
			Integer loggedInUserId = dto.getLoggedInUserId();
			if (isAssociatedWithLMS(dto.getId())) {
				returnTrackOrPlayBookNamesAssociatedWithDamId(response, damId, true);
			} else {
				if (isVideoFile) {
					response = videoService.statusChange(dam.getVideoFile().getId(), loggedInUserId);
				} else {
					damDao.delete(damId);
					response.setStatusCode(200);
					response.setAccess(true);
					response.setMessage("Data Deleted Successfully.");
				}
			}
			return response;
		} catch (DamDataAccessException mex) {
			throw new DamDataAccessException(mex);
		} catch (Exception e) {
			throw new DamDataAccessException(e);
		}
	}

	@Override
	public void returnTrackOrPlayBookNamesAssociatedWithDamId(XtremandResponse response, Integer damId,
			boolean isDeleteAction) {
		response.setStatusCode(401);
		List<String> trackNames = lmsDao.findTrackNamesByAssetId(damId);
		List<String> playBookNames = lmsDao.findPlayBookNamesByAssetId(damId);
		if (XamplifyUtils.isNotEmptyList(trackNames) && XamplifyUtils.isNotEmptyList(playBookNames)) {
			trackNames.addAll(playBookNames);
			String message = isDeleteAction ? assetDeletionAssociatedWithTracksPlaybooksMessage
					: assetRejectionAssociatedWithTracksPlaybooksMessage;
			response.setMessage(message);
			response.setData(trackNames);
		} else {
			if (XamplifyUtils.isNotEmptyList(trackNames)) {
				String message = isDeleteAction ? assetDeletionAssociatedWithTracksMessage
						: assetRejectionAssociatedWithTracksMessage;
				response.setMessage(message);
				response.setData(trackNames);
			} else {
				String message = isDeleteAction ? assetDeletionAssociatedWithPlaybooksMessage
						: assetRejectionAssociatedWithPlaybooksMessage;
				response.setMessage(message);
				response.setData(playBookNames);
			}
		}
		response.setAccess(true);
	}

	private boolean isAssociatedWithLMS(Integer damId) {
		return damDao.isAssociatedWithLMS(damId, false);
	}

	@Override
	public XtremandResponse getSharedAssetDetailsById(Integer id, Integer loggedInUserId) {
		try {
			XtremandResponse response = new XtremandResponse();
			response.setAccess(true);
			boolean isIdMatched = checkIsDamPartnerIdBelongsToLoggedInUserCompany(id, loggedInUserId);
			if (isIdMatched) {
				SharedAssetDetailsViewDTO dto = damDao.getSharedAssetDetailsById(id);
				if (dto != null) {
					response.setStatusCode(200);
					dto.setPublishedTimeInUTCString(DateUtils.getUtcString(dto.getPublishedTime()));
					if (dto.getVideoId() != null) {
						dto.setThumbnailPath(serverPath + dto.getThumbnailPath());
					}
					getEncodedSharedAssetpath(dto);
					/***** XNFR-949 *****/
					DamUploadPostDTO damUploadPostDTO = damDao.findDamByPartnerId(id);
					if (damUploadPostDTO.isBeeTemplate() && damUploadPostDTO.isPartnerSignatureRequired()
							&& !XamplifyUtils.isValidString(damUploadPostDTO.getAssetPath())) {
						asyncComponent.uploadDesignedPdfToAws(damUploadPostDTO.getId());
					}

					response.setData(dto);
				} else {
					response.setStatusCode(404);
				}
				return response;
			} else {
				throw new AccessDeniedException(XamplifyConstants.USER_NOT_AUTHORIZED_TO_ACCESS_PAGE);
			}
		} catch (AccessDeniedException ae) {
			throw new AccessDeniedException(ae.getMessage());
		} catch (DamDataAccessException mex) {
			throw new DamDataAccessException(mex);
		} catch (Exception e) {
			throw new DamDataAccessException(e);
		}
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public XtremandResponse saveDamAnalytics(DamAnalyticsPostDTO damAnalyticsPostDTO) {
		try {
			XtremandResponse response = new XtremandResponse();
			GeoLocationAnalytics geoLocationAnalytics = new GeoLocationAnalytics();
			geoLocationAnalytics.setUserId(damAnalyticsPostDTO.getLoggedInUserId());
			GeoLocationAnalyticsDTO geoLocationAnalyticsDTO = damAnalyticsPostDTO.getGeoLocationDetails();
			geoLocationAnalytics.setDeviceType(geoLocationAnalyticsDTO.getDeviceType());
			geoLocationAnalytics.setOs(geoLocationAnalyticsDTO.getOs());
			geoLocationAnalytics.setCity(geoLocationAnalyticsDTO.getCity());
			geoLocationAnalytics.setCountry(geoLocationAnalyticsDTO.getCountry());
			geoLocationAnalytics.setIsp(geoLocationAnalyticsDTO.getIsp());
			geoLocationAnalytics.setIpAddress(geoLocationAnalyticsDTO.getIpAddress());
			geoLocationAnalytics.setState(geoLocationAnalyticsDTO.getState());
			geoLocationAnalytics.setZip(geoLocationAnalyticsDTO.getZip());
			geoLocationAnalytics.setLatitude(geoLocationAnalyticsDTO.getLatitude());
			geoLocationAnalytics.setLongitude(geoLocationAnalyticsDTO.getLongitude());
			geoLocationAnalytics.setCountryCode(geoLocationAnalyticsDTO.getCountryCode());
			geoLocationAnalytics.setTimezone(geoLocationAnalyticsDTO.getTimezone());
			saveIntoDamAnalytics(damAnalyticsPostDTO.getLoggedInUserId(), damAnalyticsPostDTO.getDamPartnerId(),
					damAnalyticsPostDTO.getActionType(), geoLocationAnalytics);
			response.setStatusCode(200);
			response.setAccess(true);
			return response;
		} catch (DamDataAccessException mex) {
			throw new DamDataAccessException(mex);
		} catch (Exception e) {
			throw new DamDataAccessException(e);
		}
	}

	private void saveIntoDamAnalytics(Integer loggedInUserId, Integer damPartnerId, Integer actionId,
			GeoLocationAnalytics geoLocationAnalytics) {
		DamAnalytics damAnalytics = new DamAnalytics();
		damAnalytics.setActionTime(new Date());
		if (actionId.equals(1)) {
			damAnalytics.setDamAnalyticsActionEnum(DamAnalyticsActionEnum.VIEW);
		} else if (actionId.equals(2)) {
			damAnalytics.setDamAnalyticsActionEnum(DamAnalyticsActionEnum.DOWNLOAD);
		}
		if (damDao.isPublishedToPartnerGroupsByDamPartnerId(damPartnerId)) {
			damAnalytics.setActionPerformedBy(loggedInUserId);
			DamPartner damPartner = new DamPartner();
			damPartner.setId(damPartnerId);
			damAnalytics.setDamPartner(damPartner);
		} else {
			DamPartnerMapping damPartnerMapping = damDao.findByPartnerIdAndDamPartnerId(loggedInUserId, damPartnerId);
			damAnalytics.setDamPartnerMapping(damPartnerMapping);
		}

		damDao.save(damAnalytics);
		geoLocationAnalytics.setDamAnalytics(damAnalytics);
		geoLocationAnalytics.setAnalyticsType(GeoLocationAnalyticsEnum.DAM);
		damDao.save(geoLocationAnalytics);

	}

	@Override
	public XtremandResponse listDamAnalytics(Pagination pagination) {
		try {
			XtremandResponse response = new XtremandResponse();
			response.setStatusCode(200);
			response.setAccess(true);
			response.setData(damDao.listDamAnalytics(pagination));
			return response;
		} catch (DamDataAccessException mex) {
			throw new DamDataAccessException(mex);
		} catch (Exception e) {
			throw new DamDataAccessException(e);
		}
	}

	@Override
	public XtremandResponse showPartnerDetailsWithAnalyticsCount(Integer damPartnerId, Integer partnerId) {
		try {
			XtremandResponse response = new XtremandResponse();
			response.setStatusCode(200);
			Map<String, Object> map = new HashMap<>();
			map.put("tilesInfo", damDao.findPartnerDetailsAndViewAndDownloadsCount(damPartnerId, partnerId));
			map.put("assetName", damDao.getAssetNameByDamPartnerId(damPartnerId));
			/***** XNFR-949 *****/
			DamUploadPostDTO damUploadPostDTO = damDao.findDamByPartnerId(damPartnerId);
			if (damUploadPostDTO.isBeeTemplate() && damUploadPostDTO.isPartnerSignatureRequired()
					&& !XamplifyUtils.isValidString(damUploadPostDTO.getAssetPath())) {
				asyncComponent.uploadDesignedPdfToAws(damUploadPostDTO.getId());
			}

			response.setData(map);
			return response;
		} catch (DamDataAccessException mex) {
			throw new DamDataAccessException(mex);
		} catch (Exception e) {
			throw new DamDataAccessException(e);
		}
	}

	@Override
	public XtremandResponse listPublishedPartnersAnalytics(Pagination pagination) {
		try {
			XtremandResponse response = new XtremandResponse();
			response.setStatusCode(200);
			response.setData(damDao.listPublishedPartnersAnalytics(pagination));
			return response;
		} catch (DamDataAccessException mex) {
			throw new DamDataAccessException(mex);
		} catch (Exception e) {
			throw new DamDataAccessException(e);
		}
	}

	@Override
	public XtremandResponse getAssetDetailsById(Integer id, Integer userId) {
		try {
			XtremandResponse response = new XtremandResponse();
			response.setStatusCode(200);
			boolean isIdMatched = checkIsDamIdBelongsToLoggedInUserCompany(id, userId);
			if (isIdMatched) {
				DamViewDTO damViewDto = new DamViewDTO();
				setDamViewDtoDetials(id, damViewDto);
				response.setData(damViewDto);
				return response;
			} else {
				throw new AccessDeniedException(XamplifyConstants.USER_NOT_AUTHORIZED_TO_ACCESS_PAGE);
			}

		} catch (DamDataAccessException mex) {
			throw new DamDataAccessException(mex);
		} catch (AccessDeniedException e) {
			throw new AccessDeniedException(e.getMessage());
		} catch (Exception e) {
			throw new DamDataAccessException(e);
		}
	}

	private void setDamViewDtoDetials(Integer id, DamViewDTO damViewDto) throws UnsupportedEncodingException {
		Object[] row = damDao.getAssetValuesById(id);
		damViewDto.setAssetName((String) row[0]);
		damViewDto.setDescription((String) row[1]);
		damViewDto.setThumbnailPath((String) row[2]);
		damViewDto.setAssetPath((String) row[3]);
		damViewDto.setBeeTemplate((boolean) row[4]);
		damViewDto.setPublishedTime((Date) row[5]);
		setTagIds(damViewDto, row);
		/******** XNFR-169 *********/
		Integer categoryId = categoryDao.getCategoryIdByType(id, CategoryModuleEnum.DAM.name());
		damViewDto.setCategoryId(categoryId);
		/******** XNFR-169 *********/
		boolean historyTemplate = (boolean) row[7];
		damViewDto.setHistoryTemplate(historyTemplate);
		/**** XNFR-255 ****/
		setWhiteLabeledProperties(damViewDto, row);
		/*** Added On 02/08/2023 ****/
		boolean isPublishedToPartnerGroups = damDao.isPublishedToPartnerGroups(id);
		damViewDto.setPublishedToPartnerGroups(isPublishedToPartnerGroups);
		setPartnershipIdsProperty(id, damViewDto);
		setPartnerIdsProperty(id, damViewDto);
		setPartnerGroupIdsProperty(id, damViewDto);
		/*** Added On 02/08/2023 ****/
		damViewDto.setPublished(damDao.isAssetPublished(id));
		/** XNFR-434 **/
		damViewDto.setAssetType((String) row[9]);
		/** XNFR-434 **/
		damViewDto.setAddedToQuickLinks((boolean) row[10]);
		/** XNFR-833 **/
		setPartnerSignatureRequired(damViewDto, row);
		setVendorSignatureRequired(damViewDto, row);
		damViewDto.setVendorSignatureCompleted((boolean) row[13]);
		setVendorSignatureRequiredAfterPartnerSignatureCompleted(damViewDto, row);
		/** XNFR-689 ****/

		/** XNFR-884 **/
		damViewDto.setApprovalStatus((String) row[14]);
		boolean createdByAnyApprover = approveDao.checkIsAssetApproverByTeamMemberId((Integer) row[15]);
		damViewDto.setCreatedByAnyApprover(createdByAnyApprover);
		damViewDto.setJsonBody((String) row[16]);
		damViewDto.setHtmlBody((String) row[17]);
		damViewDto.setSlug((String) row[19]);
		if (XamplifyUtils.isValidString(damViewDto.getAssetPath())
				&& XamplifyUtils.isValidString(damViewDto.getAssetType()) && damViewDto.getAssetType().equals("pdf")) {
			String encodedUrl = URLEncoder.encode(damViewDto.getAssetPath(), StandardCharsets.UTF_8.name());
			String baseUrl;
			if (xamplifyUtil.isDev()) {
				baseUrl = HOST;
			} else if (xamplifyUtil.isQA()) {
				baseUrl = devHost;
			} else if (xamplifyUtil.isProduction()) {
				baseUrl = productionHost;
			} else {
				baseUrl = HOST;
			}
			String proxyUrl = baseUrl + XAMPLIFY_PRM + "/api/pdf/proxy?pdfUrl=" + encodedUrl;
			damViewDto.setAssetPath(proxyUrl);
		}
		/***** XNFR-949 *****/
		if (damViewDto.isBeeTemplate() && !XamplifyUtils.isValidString(damViewDto.getAssetPath())) {
			asyncComponent.uploadDesignedPdfToAws(id);
		}
	}

	private boolean checkIsDamIdBelongsToLoggedInUserCompany(Integer id, Integer userId) {
		Integer companyId = userDao.getCompanyIdByUserId(userId);
		return damDao.isIdMatchedByCompanyIdAndId(companyId, id);
	}

	private boolean checkIsDamPartnerIdBelongsToLoggedInUserCompany(Integer damPartnerId, Integer userId) {
		Integer companyId = userDao.getCompanyIdByUserId(userId);
		return damDao.isIdMatchedByCompanyIdAndDamPartnerId(companyId, damPartnerId);
	}

	private void setPartnershipIdsProperty(Integer id, DamViewDTO damViewDto) {
		List<Integer> partnershipIds = damDao.findPartnershipIdsForPartnerCompaniesOptionByDamId(id);
		if (partnershipIds != null && !partnershipIds.isEmpty()) {
			damViewDto.setPartnershipIds(partnershipIds);
		} else {
			damViewDto.setPartnershipIds(new ArrayList<>());
		}
	}

	private void setPartnerIdsProperty(Integer id, DamViewDTO damViewDto) {
		List<Integer> partnerIds = damDao.findPublishedPartnerIdsByDamId(id);
		if (partnerIds != null && !partnerIds.isEmpty()) {
			damViewDto.setPartnerIds(partnerIds);
		} else {
			damViewDto.setPartnerIds(new ArrayList<>());
		}
	}

	private void setPartnerGroupIdsProperty(Integer id, DamViewDTO damViewDto) {
		List<Integer> publishedPartnerGroupIds = damDao.findPublishedPartnerGroupIdsByDamId(id);
		if (publishedPartnerGroupIds != null && !publishedPartnerGroupIds.isEmpty()) {
			damViewDto.setPartnerGroupIds(publishedPartnerGroupIds);
		} else {
			damViewDto.setPartnerGroupIds(new ArrayList<>());
		}
	}

	private void setWhiteLabeledProperties(DamViewDTO damViewDto, Object[] row) {
		damViewDto.setShareAsWhiteLabeledAsset((boolean) row[8]);
		if (damViewDto.isShareAsWhiteLabeledAsset()) {
			damViewDto.setDisableWhiteLabelOption(true);
			damViewDto.setWhiteLabeledToolTipMessage(whiteLabeledDisabledToolTipMessage);
		} else {
			damViewDto.setWhiteLabeledToolTipMessage(whiteLabeledToolTipMessage);
		}
	}

	// XNFR-833
	private void setPartnerSignatureRequired(DamViewDTO damViewDto, Object[] row) {
		damViewDto.setPartnerSignatureRequired((boolean) row[11]);
		if (damViewDto.isPartnerSignatureRequired()) {
			damViewDto.setDisablePartnerSignatureOption(true);
			damViewDto.setPartnerSignatureToolTipMessage(partnerSignatureRequiredDisabledToolTipMessage);
		} else {
			damViewDto.setPartnerSignatureToolTipMessage(partnerSignatureRequiredToolTipMessage);
		}
	}

	private void setVendorSignatureRequired(DamViewDTO damViewDto, Object[] row) {
		damViewDto.setVendorSignatureRequired((boolean) row[12]);
		if (damViewDto.isVendorSignatureRequired()) {
			damViewDto.setDisableVendorSignatuerOption(true);
			damViewDto.setVendorSignatureToolTipMessage(vendorSignatureRequiredDisabledToolTipMessage);
		} else {
			damViewDto.setVendorSignatureToolTipMessage(vendorSignatureRequiredToolTipMessage);
		}
	}

	private void setVendorSignatureRequiredAfterPartnerSignatureCompleted(DamViewDTO damViewDto, Object[] row) {
		damViewDto.setVendorSignatureRequiredAfterPartnerSignature((boolean) row[18]);
		if (damViewDto.isVendorSignatureRequiredAfterPartnerSignature()) {
			damViewDto.setDisablevendorSignatureRequiredAfterPartnerSignatureOption(true);
			damViewDto.setVendorSignatureRequiredAfterPartnerSignatureToolTipMessage(
					vendorSignatureRequiredAfterPartnerSignatureDisabledToolTipMessage);
		} else {
			damViewDto.setVendorSignatureRequiredAfterPartnerSignatureToolTipMessage(
					vendorSignatureRequiredAfterPartnerSignatureToolTipMessage);
		}
	}

	private void setTagIds(DamViewDTO damViewDto, Object[] row) {
		String tagIdStr = (String) row[6];
		Set<Integer> tagIds = new LinkedHashSet<>();
		List<String> tagIdStrList = new ArrayList<>();
		if (tagIdStr != null) {
			tagIdStrList.addAll(Arrays.asList(tagIdStr.split(",")));
			for (String tagId : tagIdStrList) {
				tagIds.add(Integer.parseInt(tagId));
			}
		}
		damViewDto.setTagIds(tagIds);
	}

	private void setTags(Dam dam, DamViewDTO damViewDto, DamListDTO damListDto) {
		Set<Integer> tagIds = new LinkedHashSet<>();
		if (dam.getTags() != null && !dam.getTags().isEmpty()) {
			for (DamTag damTag : dam.getTags()) {
				if (damTag != null && damTag.getTag() != null) {
					TagDTO tagDto = new TagDTO();
					BeanUtils.copyProperties(damTag.getTag(), tagDto);
					tagIds.add(tagDto.getId());
				}
			}
		}
		if (damListDto != null) {
			damListDto.setTagIds(tagIds);
		} else if (damViewDto != null) {
			damViewDto.setTagIds(tagIds);
		}
	}

	@Override
	public XtremandResponse deletePartner(Integer id) {
		try {
			XtremandResponse response = new XtremandResponse();
			response.setStatusCode(200);
			damDao.deletePartner(id);
			return response;
		} catch (DamDataAccessException mex) {
			throw new DamDataAccessException(mex);
		} catch (Exception e) {
			throw new DamDataAccessException(e);
		}
	}

	@Override
	public void uploadAsset(DamAwsDTO damAwsDTO, DamUploadPostDTO damUploadPostDTO) {
		asyncComponent.uploadAsset(damAwsDTO, damUploadPostDTO);
	}

	@Override
	public XtremandResponse previewAssetById(Integer id) {
		try {
			XtremandResponse response = new XtremandResponse();
			response.setStatusCode(200);
			getHtmlBodyAndReplaceCompanyLogoAndWebsiteUrl(id, response);
			return response;
		} catch (DamDataAccessException mex) {
			throw new DamDataAccessException(mex);
		} catch (Exception e) {
			throw new DamDataAccessException(e);
		}
	}

	private void getHtmlBodyAndReplaceCompanyLogoAndWebsiteUrl(Integer id, XtremandResponse response) {
		DamPreviewDTO damPreviewDTO = damDao.previewAssetById(id);
		if (damPreviewDTO.isBeeTemplate()) {
			String updatedHtmlBody = "";
			String htmlBody = damPreviewDTO.getHtmlBody();
			User createdUser = userDao.findByPrimaryKey(damPreviewDTO.getCreatedBy(),
					new FindLevel[] { FindLevel.COMPANY_PROFILE });
			updatedHtmlBody = utilService.replaceSenderMergeTagsAndPartnerAboutUsMergeTag(htmlBody, createdUser, null,
					true);
			updatedHtmlBody = xamplifyUtil.replaceCompanyWebsiteUrl(updatedHtmlBody,
					createdUser.getCompanyProfile().getWebsite());
			damPreviewDTO.setHtmlBody(updatedHtmlBody);
		}
		response.setData(damPreviewDTO);
	}

	private void getHtmlBodyAndReplaceCompanyLogoAndWebsiteUrlAndPartnerLogo(
			AssetPdfPreviewRequestDTO assetPdfPreviewRequestDTO, XtremandResponse response) {
		Integer id = assetPdfPreviewRequestDTO.getId();
		DamPreviewDTO damPreviewDTO = damDao.previewAssetById(id);
		if (damPreviewDTO.isBeeTemplate()) {
			String updatedHtmlBody = "";
			String htmlBody = damPreviewDTO.getHtmlBody();
			Integer createdUserId = damPreviewDTO.getCreatedBy();
			User createdUser = userDao.findByPrimaryKey(createdUserId, new FindLevel[] { FindLevel.COMPANY_PROFILE });
			Integer createdByCompanyId = createdUser.getCompanyProfile().getId();
			updatedHtmlBody = utilService.replaceSenderMergeTagsAndPartnerAboutUsMergeTag(htmlBody, createdUser, null,
					true);
			Integer loggedInUserIdCompanyId = userDao.getCompanyIdByUserId(assetPdfPreviewRequestDTO.getUserId());
			boolean isPartnerView = XamplifyUtils.isValidInteger(createdByCompanyId)
					&& XamplifyUtils.isValidInteger(loggedInUserIdCompanyId)
					&& !loggedInUserIdCompanyId.equals(createdByCompanyId);
			if (isPartnerView) {
				String partnerCompanyLogoPath = userDao.getCompanyLogoPath(loggedInUserIdCompanyId);
				String vendorCompanyLogoPath = createdUser.getCompanyProfile().getCompanyLogoPath();
				String partnerCompanyWebSiteUrl = userDao.getCompanyWebSiteUrlByCompanyId(loggedInUserIdCompanyId);
				updatedHtmlBody = xamplifyUtil.replaceCompanyLogoAndCoBrandedLogo(htmlBody, partnerCompanyLogoPath,
						vendorCompanyLogoPath, partnerCompanyWebSiteUrl);
			}
			updatedHtmlBody = xamplifyUtil.replaceCompanyWebsiteUrl(updatedHtmlBody,
					createdUser.getCompanyProfile().getWebsite());
			damPreviewDTO.setHtmlBody(updatedHtmlBody);
		}
		response.setData(damPreviewDTO);
	}

	@Override
	public XtremandResponse checkVendorAccessForDamPartnerAnalytics(Integer damId, Integer partnerId) {
		XtremandResponse response = new XtremandResponse();
		response.setStatusCode(damDao.checkVendorAccessForDamPartnerAnalytics(damId, partnerId) ? 200 : 404);
		return response;
	}

	@Override
	public XtremandResponse checkDamPartnerId(Integer damPartnerId, Integer loggedInUserId) {
		XtremandResponse response = new XtremandResponse();
		boolean isIdMatched = checkIsDamPartnerIdBelongsToLoggedInUserCompany(damPartnerId, loggedInUserId);
		if (isIdMatched) {
			response.setStatusCode(200);
		} else {
			throw new AccessDeniedException(XamplifyConstants.USER_NOT_AUTHORIZED_TO_ACCESS_PAGE);
		}
		return response;
	}

	private boolean hasDownloadAssetAccess(Integer damId, Integer userId) {
		List<Integer> partnerIds = damDao.getUsersByDamAlias(damId, userId);
		return partnerIds.indexOf(userId) > -1;
	}

	@Override
	public XtremandResponse findPublishedPartnershipIds(Integer damId) {
		XtremandResponse response = new XtremandResponse();
		List<Integer> partnershipIds = damDao.findPartnershipIdsForPartnerCompaniesOptionByDamId(damId);
		if (partnershipIds != null && !partnershipIds.isEmpty()) {
			response.setData(partnershipIds);
		} else {
			response.setData(new ArrayList<Integer>());
		}
		return response;
	}

	@Override
	public XtremandResponse findPublishedPartnerIdsByDamId(Integer damId) {
		XtremandResponse response = new XtremandResponse();
		List<Integer> partnerIds = damDao.findPublishedPartnerIdsByDamId(damId);
		if (partnerIds != null && !partnerIds.isEmpty()) {
			response.setData(partnerIds);
		} else {
			response.setData(new ArrayList<Integer>());
		}
		Set<Integer> publishedPartnershipIds = new HashSet<>();
		publishedPartnershipIds.addAll(damDao.findPublishedPartnershipIds(damId));
		Map<String, Object> map = new HashMap<>();
		map.put("partnershipIds", publishedPartnershipIds);
		response.setMap(map);
		return response;
	}

	@Override
	public XtremandResponse insertIntoDamPartnerMapping() {
		XtremandResponse response = new XtremandResponse();
		damDao.insertIntoDamPartnerMapping();
		response.setMessage("Success");
		return response;
	}

	@Override
	public void findAndDeleteUnMappedRowsFromDamPartnerOrDamPartnerGroupMapping(boolean isPartnerGroupSelected) {
		try {
			damDao.findAndDeleteUnMappedRowsFromDamPartnerOrDamPartnerGroupMapping(isPartnerGroupSelected);
		} catch (DamDataAccessException mex) {
			throw new DamDataAccessException(mex);
		} catch (Exception e) {
			throw new DamDataAccessException(e);
		}
	}

	@Override
	public XtremandResponse findPublishedPartnerGroupIdsByDamId(Integer damId) {
		XtremandResponse response = new XtremandResponse();
		List<Integer> publishedPartnerGroupIds = damDao.findPublishedPartnerGroupIdsByDamId(damId);
		if (publishedPartnerGroupIds != null && !publishedPartnerGroupIds.isEmpty()) {
			response.setData(publishedPartnerGroupIds);
		} else {
			response.setData(new ArrayList<Integer>());
		}
		return response;
	}

	@Override
	public XtremandResponse isPublishedToPartnerGroups(Integer damId) {
		XtremandResponse response = new XtremandResponse();
		response.setData(damDao.isPublishedToPartnerGroups(damId));
		return response;
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public XtremandResponse insertVideoIdsIntoDam() {
		XtremandResponse response = new XtremandResponse();
		List<VideoFile> videosList = genericDAO.load(VideoFile.class);
		for (VideoFile videoFile : videosList) {
			Dam dam = new Dam();
			User videoCustomer = videoFile.getCustomer();
			CompanyProfile companyProfile = userService.getCompanyProfileByUser(videoCustomer.getUserId());
			dam.setCompanyProfile(companyProfile);
			Integer companyId = companyProfile.getId();
			String assetName = "";
			String videoTitle = videoFile.getTitle();
			videoTitle = videoTitle.replaceAll("'", "''");
			Integer videoTitleCount = videoDAO.getVideoTitleCount(videoTitle, companyId);
			VideoFile.TYPE viewByType = videoFile.getViewBy();
			long milliSeconds = System.nanoTime();
			if (viewByType == VideoFile.TYPE.DRAFT) {
				assetName = videoTitle + "_" + milliSeconds;
				videoFile.setTitle(assetName);
			} else {
				if (videoTitleCount > 1) {
					assetName = videoTitle + "_" + milliSeconds;
					videoFile.setTitle(assetName);
				} else {
					assetName = videoTitle;
				}
			}
			String fileType = videoFile.getUri().substring(videoFile.getUri().lastIndexOf('.') + 1);
			dam.setAssetType(fileType);
			dam.setAssetName(XamplifyUtils.removeExtraSpace(assetName));
			GenerateRandomPassword password = new GenerateRandomPassword();
			dam.setAlias(password.getPassword());
			dam.setCreatedBy(videoCustomer.getUserId());
			dam.setCreatedTime(videoFile.getCreatedTime());
			dam.setDamStatusEnum(DamStatusEnum.COMPLETED);
			dam.setVideoFile(videoFile);
			genericDAO.save(dam);
			VideoImage videoImage = videoFile.getVideoImage();
			if (videoImage != null) {
				boolean isVideoImage = videoFile.getImageUri().equalsIgnoreCase(videoImage.getImage1())
						|| videoFile.getImageUri().equalsIgnoreCase(videoImage.getImage2())
						|| videoFile.getImageUri().equalsIgnoreCase(videoImage.getImage3());
				if (!isVideoImage) {
					videoFile.setCustomThumbnailUploaded(true);
				}
			}
		}
		response.setStatusCode(200);
		response.setMessage("data inserted successfully.");
		return response;
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public XtremandResponse uploadContent(MultipartFile uploadedFile, DamUploadPostDTO damUploadPostDTO,
			MultipartFile thumbnailFile) {
		XtremandResponse response = new XtremandResponse();
		Integer companyId = userDao.getCompanyIdByUserId(damUploadPostDTO.getLoggedInUserId());
		damUploadPostDTO.setCompanyId(companyId);
		checkDuplicateFieldForAsset(damUploadPostDTO, response, companyId, damUploadPostDTO.isBeeTemplate(),
				damUploadPostDTO.isSaveAs());
		if (response.getStatusCode() != 401) {
			boolean isVideoFile = fileUtil.isVideoFile(uploadedFile.getOriginalFilename());
			if (isVideoFile) {
				response = uploadVideo(uploadedFile, damUploadPostDTO, response);
				response.setMessage("Video Uploaded Successfully");
				/**** XNFR-434 *****/
				Integer videoId = damUploadPostDTO.getVideoId();
				updateVideoAssetStatus(videoId);
				/**** XNFR-434 *****/

			} else {
				response = upload(uploadedFile, damUploadPostDTO, thumbnailFile, response);
				setUploadedSuccessfullyMessage(damUploadPostDTO, response);
				/**** XNFR-434 *****/
				Integer damId = damUploadPostDTO.getId();
				updateAssetStatus(damId);
				/**** XNFR-434 *****/
			}

		}
		return response;
	}

	private void checkDuplicateFieldForAsset(DamUploadPostDTO damUploadPostDTO, XtremandResponse response,
			Integer companyId, boolean isBeeTemplate, boolean isSaveAs) {
		Map<String, Object> map = new HashMap<>();
		if (!isBeeTemplate || isSaveAs) {
			response = isDuplicateAssetName(damUploadPostDTO.getAssetName(), companyId, response,
					damUploadPostDTO.isSendForReApproval());
			if (response.getStatusCode() == 401) {
				map.put("duplicateName", true);
			}
		}
		boolean slugExist = damDao.isAssetExistBySlugAndCompany(damUploadPostDTO.getSlug(), companyId);
		if (slugExist) {
			response.setStatusCode(401);
			map.put("duplicateSlug", true);
		}
		response.setMap(map);
	}

	/**** XNFR-434 *****/
	private void updateAssetStatus(Integer damId) {
		if (XamplifyUtils.isValidInteger(damId)) {
			damDao.updateStatusToProgessByDamId(damId);
		}
	}

	/**** XNFR-434 *****/
	private void updateVideoAssetStatus(Integer videoId) {
		if (XamplifyUtils.isValidInteger(videoId)) {
			damDao.updateStatusToProgessByVideoId(videoId);
		}
	}

	public List<String> getTags(DamUploadPostDTO damUploadPostDTO, Integer companyId) {
		Set<Integer> tagIds = damUploadPostDTO.getTagIds();
		List<String> tags = new ArrayList<>();
		if (tagIds != null && !tagIds.isEmpty()) {
			for (Integer tagId : tagIds) {
				if (tagId != null && tagId > 0) {
					Tag tag = tagDao.getByIdAndCompanyId(tagId, companyId);
					tags.add(tag.getTagName());
				}
			}
		}
		return tags;
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public XtremandResponse uploadCloudContent(DamUploadPostDTO damUploadPostDTO, MultipartFile thumbnailFile) {
		XtremandResponse response = new XtremandResponse();
		Integer companyId = userDao.getCompanyIdByUserId(damUploadPostDTO.getLoggedInUserId());
		response = isDuplicateAssetName(damUploadPostDTO.getAssetName(), companyId, response,
				damUploadPostDTO.isSendForReApproval());
		boolean isVideoFile = fileUtil.isVideoFile(damUploadPostDTO.getFileName());
		if (response.getStatusCode() != 401) {
			if (isVideoFile) {
				response = uploadVideo(null, damUploadPostDTO, response);
				/*** XNFR-434 ***/
				updateVideoAssetStatus(damUploadPostDTO.getVideoId());
				/*** XNFR-434 ***/
			} else {
				response = upload(null, damUploadPostDTO, thumbnailFile, response);
				/*** XNFR-434 ***/
				updateAssetStatus(damUploadPostDTO.getId());
				/*** XNFR-434 ***/
			}
		}
		return response;
	}

	@Override
	public XtremandResponse upload(MultipartFile uploadedFile, DamUploadPostDTO damUploadPostDTO,
			MultipartFile thumbnailFile, XtremandResponse response) {
		try {
			Integer loggedInUserId = damUploadPostDTO.getLoggedInUserId();
			Integer companyId = userDao.getCompanyIdByUserId(loggedInUserId);
			if (XamplifyUtils.isValidInteger(loggedInUserId) && XamplifyUtils.isValidInteger(companyId)
					&& !damUploadPostDTO.isCloudContent()) {
				fileUtil.validateFileSize(uploadedFile, response, "DAM");
				if (response.getStatusCode() != 400) {
					response = upload(uploadedFile, damUploadPostDTO, thumbnailFile, companyId, response);
				}
			} else if (damUploadPostDTO.isCloudContent()) {
				response = upload(uploadedFile, damUploadPostDTO, thumbnailFile, companyId, response);
			} else {
				response.setStatusCode(404);
				response.setMessage("Invalid Input");
			}

			/** XNFR-781 **/
			Map<String, Object> map = response.getMap();
			if (map != null) {
				DamAwsDTO damAwsDTO = (DamAwsDTO) map.get("dto");
				if (damAwsDTO != null) {
					commentDao.createApprovalStatusHistory(damAwsDTO.getDamId(), loggedInUserId, ModuleType.DAM);
				}
			}
			return response;
		} catch (DamDataAccessException mex) {
			throw new DamDataAccessException(mex);
		} catch (BadRequestException e) {
			throw new BadRequestException(e);
		} catch (Exception e) {
			throw new DamDataAccessException(e);
		}
	}

	public XtremandResponse upload(MultipartFile uploadedFile, DamUploadPostDTO damUploadPostDTO,
			MultipartFile thumbnailFile, Integer companyId, XtremandResponse response) throws IOException {
		String fileType = null;
		if (damUploadPostDTO.isCloudContent()) {
			fileType = fileUtil.getFileExtension(damUploadPostDTO.getFileName());
		} else {
			fileType = fileUtil.getFileExtension(uploadedFile);
		}
		Dam dam = new Dam();
		CompanyProfile companyProfile = new CompanyProfile();
		companyProfile.setId(companyId);
		dam.setCompanyProfile(companyProfile);
		dam.setDamStatusEnum(DamStatusEnum.PROCESSING);
		dam.setAssetType(fileType);
		dam.setAssetName(XamplifyUtils.removeExtraSpace(damUploadPostDTO.getAssetName()));
		dam.setSlug(XamplifyUtils.removeExtraSpace(damUploadPostDTO.getSlug()));
		dam.setCreatedBy(damUploadPostDTO.getLoggedInUserId());
		dam.setDescription(utilService.replacedDescription(damUploadPostDTO.getDescription()));
		if (damUploadPostDTO.isSendForReApproval() && fileType.equalsIgnoreCase("pdf")
				&& XamplifyUtils.isValidString(damUploadPostDTO.getThumbnailPath())) {
			dam.setThumbnailPath(damUploadPostDTO.getThumbnailPath());
		} else {
			dam.setThumbnailPath(processingGifPath);
		}
		GenerateRandomPassword password = new GenerateRandomPassword();
		dam.setAlias(password.getPassword());
		dam.setCreatedTime(new Date());
		/**** XNFR-255 ***/
		setWhiteLabelOption(damUploadPostDTO, companyId, dam);
		dam.setAddedToQuickLinks(damUploadPostDTO.isAddedToQuickLinks());
		if (damUploadPostDTO.isBeeTemplate()) {
			dam.setBeeTemplate(damUploadPostDTO.isBeeTemplate());
			dam.setHtmlBody(damUploadPostDTO.getHtmlBody());
			dam.setJsonBody(damUploadPostDTO.getJsonBody());
		}
		boolean saveAs = damUploadPostDTO.isSaveAs();
		if (XamplifyUtils.isValidInteger(damUploadPostDTO.getId()) && !saveAs) {
			setBeeTemplateProperties(damUploadPostDTO.getCategoryId(), damUploadPostDTO.getId(), dam, response);
			dam.setDamStatusEnum(DamStatusEnum.COMPLETED);
		}

		/** XNFR-781 **/

		setAssetApprovalStatus(damUploadPostDTO.getLoggedInUserId(), companyId, dam, damUploadPostDTO.isDraft());

		/** XNFR-833 **/
		dam.setPartnerSignatureRequired(damUploadPostDTO.isPartnerSignatureRequired());
		dam.setVendorSignatureRequired(damUploadPostDTO.isVendorSignatureRequired());
		dam.setVendorSignatureRequiredAfterPartnerSignature(
				damUploadPostDTO.isVendorSignatureRequiredAfterPartnerSignature());

		/** XNFR-885 **/
		if (damUploadPostDTO.isSendForReApproval()
				&& XamplifyUtils.isValidInteger(damUploadPostDTO.getApprovalReferenceId())) {
			dam.setApprovalReferenceId(damUploadPostDTO.getApprovalReferenceId());
		}
		/**** XNFR-255 ***/
		saveIntoDam(dam);
		saveTags(damUploadPostDTO, null, dam, companyId, damUploadPostDTO.getLoggedInUserId());
		DamAwsDTO damAwsDTO = new DamAwsDTO();
		damAwsDTO.setDamId(dam.getId());
		damAwsDTO.setFileType(fileType);
		damAwsDTO.setUserId(damUploadPostDTO.getLoggedInUserId());
		damAwsDTO.setAssetName(XamplifyUtils.removeExtraSpace(damUploadPostDTO.getAssetName()));
		/***** XNFR-169 *****/
		addDamCategory(damUploadPostDTO.getCategoryId(), damUploadPostDTO.getLoggedInUserId(), dam);

		copyFilesAndUploadAsynchronously(uploadedFile, damUploadPostDTO, thumbnailFile, companyId, fileType, damAwsDTO);
		/**** Move the asset to xampify server path ***********/
		Map<String, Object> map = new HashMap<>();
		map.put("dto", damAwsDTO);

		PendingApprovalDamAndLmsDTO pendingApprovalDamAndLmsDTO = new PendingApprovalDamAndLmsDTO();
		if (!damUploadPostDTO.isDraft()
				&& (damUploadPostDTO.isSendForApproval() || damUploadPostDTO.isSendForReApproval())) {
			buildPendingApprovalDamForEmailNotification(damUploadPostDTO, fileType, companyId, dam,
					pendingApprovalDamAndLmsDTO);
			map.put("pendingApprovalDamAndLmsDTO", pendingApprovalDamAndLmsDTO);
		}

		setIsVideoFileParameter(map, false);
		response.setStatusCode(200);
		response.setMap(map);
		return response;
	}

	private void buildPendingApprovalDamForEmailNotification(DamUploadPostDTO damUploadPostDTO, String fileType,
			Integer companyId, Dam dam, PendingApprovalDamAndLmsDTO pendingApprovalDamAndLmsDTO) {
		pendingApprovalDamAndLmsDTO.setCreatedById(damUploadPostDTO.getLoggedInUserId());
		pendingApprovalDamAndLmsDTO.setStatus(dam.getApprovalStatus().name());
		pendingApprovalDamAndLmsDTO.setName(dam.getAssetName());
		pendingApprovalDamAndLmsDTO.setCreatedTime(dam.getCreatedTime());
		pendingApprovalDamAndLmsDTO.setAssetType(fileType);
		pendingApprovalDamAndLmsDTO.setModuleType(ModuleType.DAM.name());
		pendingApprovalDamAndLmsDTO.setCompanyId(companyId);
	}

	/***** XNFR-169 *****/
	private void addDamCategory(Integer categoryId, Integer loggedInUserId, Dam dam) {
		CategoryModule categoryModule = new CategoryModule();
		Integer companyId = userDao.getCompanyIdByUserId(loggedInUserId);
		if (categoryId == null || categoryId == 0) {
			categoryId = categoryDao.getDefaultCategoryIdByCompanyId(companyId);
		}
		categoryModule.setCategoryId(categoryId);
		categoryModule.setCategoryModuleEnum(CategoryModuleEnum.DAM);
		categoryModule.setCreatedTime(new Date());
		categoryModule.setCreatedUserId(loggedInUserId);
		categoryModule.setCompanyId(companyId);
		categoryModule.setDamId(dam.getId());
		categoryDao.saveCategoryModule(categoryModule);

	}

	public XtremandResponse uploadVideo(MultipartFile uploadedFile, DamUploadPostDTO damUploadPostDTO,
			XtremandResponse response) {
		XtremandResponse saveVideoResponse = null;
		Integer userId = damUploadPostDTO.getLoggedInUserId();
		String companyProfileName = damUploadPostDTO.getCompanyProfileName();
		if (damUploadPostDTO.isCloudContent()) {
			saveVideoResponse = videoService.saveVideo(damUploadPostDTO.getDownloadLink(),
					damUploadPostDTO.getFileName(), damUploadPostDTO.getOauthToken(),
					damUploadPostDTO.getLoggedInUserId());
		} else if (damUploadPostDTO.getSource() != null && !damUploadPostDTO.getSource().isEmpty()
				&& damUploadPostDTO.getSource().equalsIgnoreCase("webcam")) {
			saveVideoResponse = videoService.saveRecordedVideo(uploadedFile, userId);
		} else {
			saveVideoResponse = videoService.saveVideo(uploadedFile, damUploadPostDTO.getLoggedInUserId());
		}
		String videoFilePath = (String) saveVideoResponse.getData();
		Map<String, Object> map = new HashMap<>();
		setIsVideoFileParameter(map, true);
		map.put("assetPath", videoFilePath);
		response.setMap(map);
		response.setAccess(true);
		response.setStatusCode(200);
		return response;
	}

	private void setIsVideoFileParameter(Map<String, Object> map, boolean isVideoFile) {
		map.put("isVideoFile", isVideoFile);
	}

	public XtremandResponse processVideo(DamUploadPostDTO damUploadPostDTO, MultipartFile thumbnailFile,
			PendingApprovalDamAndLmsDTO pendingApprovalDamAndLmsDTO) {
		XtremandResponse response = new XtremandResponse();
		Integer companyId = userDao.getCompanyIdByUserId(damUploadPostDTO.getLoggedInUserId());
		String videoFilePath = damUploadPostDTO.getAssetPath().split("~")[0];
		List<String> tags = getTags(damUploadPostDTO, companyId);
		/**** XNFR-434 ****/
		Integer videoId = damUploadPostDTO.getVideoId();
		boolean isReplaceAsset = XamplifyUtils.isValidInteger(videoId) && !damUploadPostDTO.isSendForReApproval();
		damUploadPostDTO.setReplaceAsset(isReplaceAsset);
		if (isReplaceAsset) {
			String videoUri = videoDAO.getVideoPath(videoId);
			damUploadPostDTO.setVideoUri(videoUri);
		}

		Dam dam = new Dam();
		if (damUploadPostDTO.isSendForReApproval()) {
			DamUploadPostDTO videoAssetDetails = damDao.getVideoAssetDetailsForReApproval(videoId);
			dam.setApprovalReferenceId(videoAssetDetails.getId());
			String assetName = XamplifyUtils.removeExtraSpace(videoAssetDetails.getAssetName());
			dam.setAssetName(assetName);
			damUploadPostDTO.setAssetName(assetName);
			damUploadPostDTO.setDescription(videoAssetDetails.getDescription());
			damUploadPostDTO.setApprovalReferenceId(videoAssetDetails.getApprovalReferenceId());
			tags = damUploadPostDTO.getTags();
		} else {
			dam.setAssetName(XamplifyUtils.removeExtraSpace(damUploadPostDTO.getAssetName()));
		}
		dam.setSlug(damUploadPostDTO.getSlug());
		Map<String, Object> videoReultMap = videoService.processVideo1(videoFilePath, damUploadPostDTO, thumbnailFile,
				tags);
		VideoFile videoFile = (VideoFile) videoReultMap.get("videoFile");
		/*** XNFR-434 ****/
		if (isReplaceAsset) {
			dam = setReplaceAssetProperties(damUploadPostDTO, videoId);
		} else {
			if (damUploadPostDTO.isSendForReApproval()) {
				DamUploadPostDTO videoAssetDetails = damDao.getVideoAssetDetailsForReApproval(videoId);
				dam.setApprovalReferenceId(videoAssetDetails.getId());
				dam.setAssetName(XamplifyUtils.removeExtraSpace(videoAssetDetails.getAssetName()));
				dam.setDescription(videoAssetDetails.getDescription());
			} else {
				dam.setAssetName(XamplifyUtils.removeExtraSpace(damUploadPostDTO.getAssetName()));
				dam.setDescription(damUploadPostDTO.getDescription());
			}
			CompanyProfile companyProfile = new CompanyProfile();
			companyProfile.setId(companyId);
			CompanyProfile existingCompanyProfile = userDao.getCompanyProfileNameAndCompanyNameByCompanyId(companyId);
			companyProfile.setCompanyProfileName(existingCompanyProfile.getCompanyProfileName());
			companyProfile.setCompanyName(existingCompanyProfile.getCompanyName());
			dam.setCompanyProfile(companyProfile);
			dam.setDamStatusEnum(DamStatusEnum.PROCESSING);
			String fileType = videoFile.getUri().substring(videoFile.getUri().lastIndexOf('.') + 1);
			dam.setAssetType(fileType);
			GenerateRandomPassword password = new GenerateRandomPassword();
			dam.setAlias(password.getPassword());
			dam.setCreatedBy(damUploadPostDTO.getLoggedInUserId());
			dam.setCreatedTime(videoFile.getCreatedTime());
			dam.setUpdatedBy(videoFile.getUpdatedBy());
			dam.setUpdatedTime(videoFile.getUpdatedTime());
			dam.setVideoFile(videoFile);
			/** XNFR-781 **/

			setAssetApprovalStatus(damUploadPostDTO.getLoggedInUserId(), companyId, dam, damUploadPostDTO.isDraft());
			/**** XNFR-255 ***/
			setWhiteLabelOption(damUploadPostDTO, companyId, dam);
			/**** XNFR-255 ***/
			dam.setAddedToQuickLinks(damUploadPostDTO.isAddedToQuickLinks());
			saveIntoDam(dam);
			/********* XNFR-169 **********/
			addDamCategory(damUploadPostDTO.getCategoryId(), damUploadPostDTO.getLoggedInUserId(), dam);

			commentDao.createApprovalStatusHistory(dam.getId(), damUploadPostDTO.getLoggedInUserId(), ModuleType.DAM);

			if (!damUploadPostDTO.isDraft()
					&& (damUploadPostDTO.isSendForApproval() || damUploadPostDTO.isSendForReApproval())) {
				buildPendingApprovalDamForEmailNotification(damUploadPostDTO, fileType, companyId, dam,
						pendingApprovalDamAndLmsDTO);
			}
		}
		String finalPath = (String) videoReultMap.get("finalPath");
		int currentBitRate = (int) videoReultMap.get("currentBitRate");
		User user = (User) videoReultMap.get("loggedInUser");
		asyncComponent.processVideo(finalPath, currentBitRate, new FFMPEGStatus(), videoFile, user, dam,
				damUploadPostDTO);

		response.setAccess(true);
		response.setStatusCode(200);
		/****** XNFR-342 *****/
		setUploadedSuccessfullyMessage(damUploadPostDTO, response);
		/****** XNFR-342 *****/
		return response;

	}

	/*** XNFR-434 ****/
	private Dam setReplaceAssetProperties(DamUploadPostDTO damUploadPostDTO, Integer videoId) {
		Dam dam = damDao.getByVideoId(videoId);
		Integer damId = dam.getId();
		damDao.updateStatusToProgessByDamId(damId);
		List<Integer> partnerGroupIds = new ArrayList<>();
		partnerGroupIds.addAll(damDao.findPublishedPartnerGroupIdsByDamId(damId));
		Set<Integer> distinctPartnerGroupIds = XamplifyUtils.convertListToSetElements(partnerGroupIds);
		damUploadPostDTO.setPartnerGroupIds(distinctPartnerGroupIds);
		List<Integer> partnerIds = new ArrayList<>();
		partnerIds.addAll(damDao.findPublishedPartnerIdsByDamId(damId));
		Set<Integer> distinctPartnerIds = XamplifyUtils.convertListToSetElements(partnerIds);
		damUploadPostDTO.setPartnerIds(distinctPartnerIds);
		boolean partnerGroupsSelected = damUploadPostDTO.getPartnerGroupIds() != null
				&& !damUploadPostDTO.getPartnerGroupIds().isEmpty();
		boolean partnerCompaniesSelected = damUploadPostDTO.getPartnerIds() != null
				&& !damUploadPostDTO.getPartnerIds().isEmpty();
		boolean canAssetPublished = partnerGroupsSelected || partnerCompaniesSelected;
		dam.setPublishingOrWhiteLabelingInProgress(canAssetPublished);
		damUploadPostDTO.setShareAsWhiteLabeledAsset(dam.isWhiteLabeledAssetSharedWithPartners());
		damUploadPostDTO.setPartnerGroupSelected(partnerGroupsSelected);
		return dam;
	}

	private void setUploadedSuccessfullyMessage(DamUploadPostDTO damUploadPostDTO, XtremandResponse response) {
		boolean isPartnerListOrPartnerGroupSelected = XamplifyUtils.isNotEmptySet(damUploadPostDTO.getPartnerIds())
				|| XamplifyUtils.isNotEmptySet(damUploadPostDTO.getPartnerGroupIds());
		if (isPartnerListOrPartnerGroupSelected) {
			response.setMessage("Uploaded & Published Successfully");
		} else {
			response.setMessage("Uploaded Successfully");
		}
	}

	private void setWhiteLabelOption(DamUploadPostDTO damUploadPostDTO, Integer companyId, Dam dam) {
		boolean shareWhiteLabeledContentAccess = utilDao.hasShareWhiteLabeledContentAccessByCompanyId(companyId);
		dam.setWhiteLabeledAssetSharedWithPartners(
				shareWhiteLabeledContentAccess && damUploadPostDTO.isShareAsWhiteLabeledAsset());
		boolean partnerGroupsSelected = damUploadPostDTO.getPartnerGroupIds() != null
				&& !damUploadPostDTO.getPartnerGroupIds().isEmpty();
		boolean partnerCompaniesSelected = damUploadPostDTO.getPartnerIds() != null
				&& !damUploadPostDTO.getPartnerIds().isEmpty();
		boolean canAssetPublished = partnerGroupsSelected || partnerCompaniesSelected;
		dam.setPublishingOrWhiteLabelingInProgress(canAssetPublished);
	}

	public XtremandResponse isDuplicateAssetName(String assetName, Integer companyId, XtremandResponse response,
			boolean isReApprovalVersion) {
		boolean isDuplicateAssetName = damDao.isDuplicateAssetName(assetName, companyId);
		if (isDuplicateAssetName && !isReApprovalVersion) {
			response.setStatusCode(401);
			response.setData("Asset Name already exists");
		}
		return response;
	}

	public XtremandResponse isDuplicateAssetName(String assetName, Integer companyId, Integer damId,
			XtremandResponse response) {
		boolean isDuplicateAssetName = damDao.isDuplicateAssetName(assetName, companyId, damId);
		if (isDuplicateAssetName) {
			response.setStatusCode(401);
			response.setData("Asset Name already exists");
			response.setAccess(true);
		}
		return response;
	}

	@Override
	public XtremandResponse updateVideo(VideoFileUploadForm videoFileUploadForm, Integer loggedInUserId,
			PendingApprovalDamAndLmsDTO pendingApprovalDamAndLmsDTO) {
		XtremandResponse response = new XtremandResponse();
		Integer companyId = userDao.getCompanyIdByUserId(loggedInUserId);

		Integer approvalReferenceId = damDao.getApprovalReferenceIdByDamId(videoFileUploadForm.getDamId());
		if (!XamplifyUtils.isValidInteger(approvalReferenceId)) {
			response = isDuplicateAssetName(videoFileUploadForm.getTitle(), companyId, videoFileUploadForm.getDamId(),
					response);
		}
		if (response.getStatusCode() != 401) {
			boolean isAssetPublished = damDao.isAssetPublished(videoFileUploadForm.getDamId());
			/*** Added On 03/08/2023 ****/
			Set<Integer> partnerIds = videoFileUploadForm.getPartnerIds();
			Set<Integer> partnerGroupIds = videoFileUploadForm.getPartnerGroupIds();
			if (partnerIds.isEmpty() && partnerGroupIds != null && !partnerGroupIds.isEmpty()) {
				videoFileUploadForm.setPartnerGroupSelected(true);
			}
			/*** Added On 03/08/2023 ****/
			VideoFile videoFile = videoService.updateVideo(videoFileUploadForm, loggedInUserId);
			VideoFileDTO videoFileDTO = videoService.getVideoFileDTO(videoFile, loggedInUserId);

			String currentApprovalStatusInString = damDao.getApprovalStatusById(videoFileUploadForm.getDamId());
			boolean canChangeApprovalStatus = checkCanChangeTheApprovalStatusFromRejectedOrDraft(videoFileUploadForm,
					currentApprovalStatusInString);

			String approvalStatusInString = canChangeApprovalStatus
					? determineApprovalStatus(loggedInUserId, companyId, videoFileUploadForm.isDraft()).name()
					: "";

			damDao.updateVideoAssetDetails(videoFileUploadForm, videoFile.getUpdatedTime(), loggedInUserId,
					canChangeApprovalStatus, approvalStatusInString);

			/**** XNFR-255 ****/

			/********** XNFR-169 *******************/
			Integer folderId = videoFileUploadForm.getFolderId();
			if (folderId != null && folderId > 0) {
				categoryDao.updateCategoryIdByType(videoFileUploadForm.getDamId(), folderId, loggedInUserId,
						CategoryModuleEnum.DAM.name());
			}
			/********** XNFR-169 *******************/
			response.setStatusCode(200);
			/**** XNFR-342 ****/
			/********* XNFR-342 *****/
			boolean isPartnerListOrPartnerGroupSelected = XamplifyUtils
					.isNotEmptySet(videoFileUploadForm.getPartnerIds())
					|| XamplifyUtils.isNotEmptySet(videoFileUploadForm.getPartnerGroupIds());
			if (isPartnerListOrPartnerGroupSelected && !isAssetPublished) {
				response.setMessage("Updated & Published Successfully");
			} else {
				response.setMessage("Updated Successfully");
			}
			/********* XNFR-342 *****/
			response.setData(videoFileDTO);
			response.setAccess(videoFileDTO.isAccess());
			/********* XNFR-255 **********/
			DamUploadPostDTO damUploadPostDTO = new DamUploadPostDTO();
			damUploadPostDTO.setPartnerGroupIds(videoFileUploadForm.getPartnerGroupIds());
			damUploadPostDTO.setPartnerIds(videoFileUploadForm.getPartnerIds());
			damUploadPostDTO.setPartnerGroupSelected(videoFileUploadForm.isPartnerGroupSelected());
			damUploadPostDTO.setShareAsWhiteLabeledAsset(videoFileUploadForm.isShareAsWhiteLabeledAsset());
			publishOrWhiteLabelAsset(damUploadPostDTO, response, videoFileUploadForm.getDamId());
			videoFileUploadForm.setDamUploadPostDTO(damUploadPostDTO);
			/********* XNFR-255 **********/

			if (videoFileUploadForm.isSendForApproval() && !videoFileUploadForm.isDraft()) {
				pendingApprovalDamAndLmsDTO.setCreatedById(loggedInUserId);
				pendingApprovalDamAndLmsDTO.setStatus(currentApprovalStatusInString);
				pendingApprovalDamAndLmsDTO.setName(videoFileUploadForm.getTitle());
				pendingApprovalDamAndLmsDTO.setCreatedTime(videoFile.getCreatedTime());
				pendingApprovalDamAndLmsDTO.setAssetType("mp4");
				pendingApprovalDamAndLmsDTO.setModuleType(ModuleType.DAM.name());
				pendingApprovalDamAndLmsDTO.setCompanyId(companyId);
			}

			return response;
		}
		return response;
	}

	private boolean checkCanChangeTheApprovalStatusFromRejectedOrDraft(VideoFileUploadForm videoFileUploadForm,
			String currentApprovalStatusInString) {
		return XamplifyUtils.isValidString(currentApprovalStatusInString) && ((!videoFileUploadForm.isDraft()
				&& (currentApprovalStatusInString.equals(ApprovalStatusType.DRAFT.name())
						|| currentApprovalStatusInString.equals(ApprovalStatusType.REJECTED.name())))
				|| (videoFileUploadForm.isDraft()
						&& currentApprovalStatusInString.equals(ApprovalStatusType.REJECTED.name())));
	}

	@Override
	public DamDTO getDamDTO(Dam dam) {
		DamDTO damDTO = new DamDTO();
		BeanUtils.copyProperties(dam, damDTO);
		damDTO.setStatus(dam.getDamStatusEnum().name());
		damDTO.setVideoFileId(dam.getVideoFile() != null ? dam.getVideoFile().getId() : null);
		return damDTO;
	}

	@Override
	public XtremandResponse insertTags() {
		XtremandResponse response = new XtremandResponse();
		try {
			List<Object[]> list = damDao.getVideoTagsDetails();
			for (Object[] row : list) {
				Integer companyId = (Integer) row[0];
				String tagsString = (String) row[1];
				Integer userId = (Integer) row[2];
				Date date = (Date) row[3];
				List<String> tags = Arrays.asList(tagsString.split(","));
				List<String> tagsList = new ArrayList<>();
				for (String t : tags) {
					tagsList.add(t.trim());
				}
				User user = genericDAO.get(User.class, userId);
				CompanyProfile companyProfile = new CompanyProfile();
				companyProfile.setId(companyId);
				tagsList.removeAll(Collections.singletonList(null));
				TreeSet<String> seen = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
				tagsList.removeIf(s -> !seen.add(s));
				tagsList = tagsList.stream().map(String::trim).collect(Collectors.toList());
				List<String> damTags = damDao.getDamTags(companyId);
				if (damTags != null && !damTags.isEmpty()) {
					damTags.removeAll(Collections.singletonList(null));
					damTags = damTags.stream().map(String::trim).collect(Collectors.toList());
					tagsList.removeAll(damTags);
				}
				for (String tagValue : tagsList) {
					boolean isTagExists = damDao.isTagExists(tagValue.trim().toLowerCase(), companyId);
					if (!isTagExists) {
						Tag tag = new Tag();
						tag.setCompanyProfile(companyProfile);
						tag.setTagName(tagValue.trim());
						tag.setCreatedBy(user);
						tag.setCreatedTime(date);
						tag.setUpdatedBy(user);
						tag.setUpdatedTime(date);
						genericDAO.save(tag);
					}
				}
			}
		} catch (Exception e) {
			throw new DamDataAccessException(e);
		}
		response.setStatusCode(200);
		response.setData("tags inserted successfully");
		return response;
	}

	@Override
	public XtremandResponse findFileTypes(Integer companyId, Integer categoryId) {
		XtremandResponse response = new XtremandResponse();
		response.setStatusCode(200);
		response.setData(damDao.findFileTypes(companyId, categoryId));
		return response;
	}

	@Override
	public XtremandResponse findFileTypesForPartnerView(VanityUrlDetailsDTO vanityUrlDetailsDTO, Integer categoryId) {
		XtremandResponse response = new XtremandResponse();
		List<String> fileTypes = new ArrayList<>();
		boolean isVanityUrlFilter = vanityUrlDetailsDTO.isVanityUrlFilter();
		utilService.isVanityUrlFilterApplicable(vanityUrlDetailsDTO);
		if (isVanityUrlFilter) {
			Integer vendorCompanyId = vanityUrlDetailsDTO.getVendorCompanyId();
			Integer partnerCompanyId = vanityUrlDetailsDTO.getLoggedInUserCompanyId();
			fileTypes.addAll(damDao.findFileTypesForSharedAssetsByVendorCompanyIdAndPartnerCompanyId(vendorCompanyId,
					partnerCompanyId, categoryId));
		} else {
			Integer partnerCompanyId = userDao.getCompanyIdByUserId(vanityUrlDetailsDTO.getUserId());
			fileTypes.addAll(damDao.findFileTypesForSharedAssetsByPartnerCompanyId(partnerCompanyId, categoryId));
		}
		response.setStatusCode(200);
		response.setData(fileTypes);
		return response;
	}

	@Override
	public void downloadPdf(String size, String orientation, String htmlBody, Integer loggedInUserId, String title)
			throws IOException {
		DamDownloadDTO damDownloadDTO = new DamDownloadDTO();
		damDownloadDTO.setHtmlBody(htmlBody);
		damDownloadDTO.setPageOrientation(orientation);
		damDownloadDTO.setPageSize(size);
		if (StringUtils.hasText(title)) {
			damDownloadDTO.setFileName(title);
		} else {
			damDownloadDTO.setFileName("Preview");
		}
		damDownloadDTO.setCreatedBy(loggedInUserId);
		utilService.setTagsAndDownloadPDF(damDownloadDTO, "1234", size, false, orientation, null);
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public XtremandResponse changeAsParentAsset(Integer assetId, Integer loggedInUserId) {
		try {
			XtremandResponse response = new XtremandResponse();
			boolean isParentTemplate = damDao.isParentTemplate(assetId);
			if (isParentTemplate) {
				throw new BadRequestException("Parent template cannot be switched.");
			} else {
				Dam dam = damDao.getAssetDetailsById(assetId);
				Integer parentId = dam.getParentId();
				boolean isDamUsedInTracks = damDao.isAssociatedWithLMS(parentId, false);
				if (isDamUsedInTracks) {
					throw new BadRequestException(
							"Asset can not be switched as it is associated with one or more Tracks/Playbooks");
				} else {
					damDao.changeAsParentAsset(assetId, loggedInUserId, dam);
					/***** XNFR-949 *****/
					if (dam.isBeeTemplate() && !XamplifyUtils.isValidString(dam.getAssetPath())) {
						asyncComponent.uploadDesignedPdfToAws(assetId);
					}
					response.setStatusCode(200);
					response.setMessage("Template switched successfully");
					return response;
				}

			}
		} catch (BadRequestException bre) {
			throw new BadRequestException(bre.getMessage());
		} catch (DamDataAccessException mex) {
			throw new DamDataAccessException(mex);
		} catch (Exception e) {
			throw new DamDataAccessException(e);
		}

	}

	@Override
	public XtremandResponse findAllUnPublishedAndFilteredPublishedAssets(Pageable pageable, BindingResult result,
			Integer loggedInUserId, Integer userListId, Integer userListUserId) {
		XtremandResponse response = new XtremandResponse();
		Integer companyId = userDao.getCompanyIdByUserId(loggedInUserId);
		List<Integer> damIds = new ArrayList<>();
		addPublishedDamIds(userListId, userListUserId, damIds);
		List<Integer> unPublishedIds = damDao.findUnPublishedAndApprovedAssetIdsByCompanyId(companyId);
		damIds.addAll(unPublishedIds);
		Pagination pagination = utilService.setPageableParameters(pageable, loggedInUserId);
		pagination.setIds(damIds);
		pagination.setCompanyId(companyId);
		if (XamplifyUtils.isNotEmptyList(damIds)) {
			response.setData(damDao.findAllUnPublishedAndFilteredPublishedAssets(pagination, pageable.getSearch()));
		} else {
			response.setData(paginationUtil.returnEmptyList(new HashMap<>(), new ArrayList<>()));
		}
		XamplifyUtils.addSuccessStatus(response);
		return response;
	}

	private void addPublishedDamIds(Integer userListId, Integer userListUserId, List<Integer> damIds) {
		List<PublishedContentIdAndUserListIdDetailsDTO> publishedAssetDetails = damDao
				.findAllPublishedAssetsByUserListId(userListId);
		if (userListId != null && userListId > 0) {
			List<Integer> userListIds = new ArrayList<>();
			userListIds.add(userListId);
			List<UserListAndUserId> userListAndUserIdDtos = userListDao.findUserIdsAndUserListIds(userListIds);
			List<Integer> deactivatedPartners = utilDao
					.findDeactivedPartnersByCompanyId(userListAndUserIdDtos.get(0).getCompanyId());
			userListAndUserIdDtos.removeIf(user -> deactivatedPartners.contains(user.getUserId()));
			Integer usersCount = userListAndUserIdDtos.size();
			for (PublishedContentIdAndUserListIdDetailsDTO publishedContentIdAndUserListIdDetailsDTO : publishedAssetDetails) {
				List<Integer> publishedUserListUserIds = publishedContentIdAndUserListIdDetailsDTO.getUserIds();
				boolean isAssetPublishedWithPartnerList = publishedUserListUserIds != null
						&& !publishedUserListUserIds.isEmpty() && usersCount.equals(publishedUserListUserIds.size());
				Integer publishedDamId = publishedContentIdAndUserListIdDetailsDTO.getId();
				boolean isUnPublishedAsset = publishedUserListUserIds != null && !publishedUserListUserIds.isEmpty()
						&& publishedUserListUserIds.indexOf(userListUserId) < 0;
				if (isUnPublishedAsset && !isAssetPublishedWithPartnerList) {
					damIds.add(publishedDamId);
				}
			}
		} else {
			if (XamplifyUtils.isNotEmptyList(publishedAssetDetails)) {
				damIds.addAll(publishedAssetDetails.stream().map(PublishedContentIdAndUserListIdDetailsDTO::getId)
						.collect(Collectors.toList()));
			}
		}
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public XtremandResponse updateDamStatus(ShareContentRequestDTO shareContentRequestDTO) {
		Set<Integer> damIds = shareContentRequestDTO.getDamIds();
		if (XamplifyUtils.isNotEmptySet(damIds)) {
			damDao.updateIsPublishedToPartnerListByIds(damIds, true);
		}
		XtremandResponse response = new XtremandResponse();
		response.setStatusCode(200);
		response.setAccess(true);
		response.setMessage("Asset(s) Shared Successfully.");
		return response;
	}

	@Override
	public XtremandResponse findPublishedPartnerIds(Integer userListId, Integer damId) {
		XtremandResponse response = new XtremandResponse();
		response.setStatusCode(200);
		response.setAccess(true);
		response.setData(damDao.findPublishedPartnerIdsByUserListIdAndDamId(userListId, damId));
		Set<Integer> publishedPartnershipIds = new HashSet<>();
		publishedPartnershipIds.addAll(damDao.findPublishedPartnershipIds(damId));
		Map<String, Object> map = new HashMap<>();
		map.put("partnershipIds", publishedPartnershipIds);
		response.setMap(map);
		return response;
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public XtremandResponse updatePDFData(DamPostDTO damPostDTO) {
		try {
			XtremandResponse response = new XtremandResponse();
			response.setStatusCode(200);
			response.setAccess(true);
			Integer id = damPostDTO.getId();
			if (damDao.isPublished(id)) {
				throw new BadRequestException("We cannot update this pdf as it is already published.");
			} else {
				Dam dam = damDao.getById(id);
				dam.setUpdatedBy(damPostDTO.getLoggedInUserId());
				dam.setUpdatedTime(new Date());
				dam.setHtmlBody(damPostDTO.getHtmlBody());
				dam.setJsonBody(damPostDTO.getJsonBody());
				return response;
			}
		} catch (BadRequestException bre) {
			throw new BadRequestException(bre.getMessage());
		} catch (DamDataAccessException mex) {
			throw new DamDataAccessException(mex);
		} catch (Exception e) {
			throw new DamDataAccessException(e);
		}

	}

	/****** XBI-2133 ******/
	@Override
	public XtremandResponse fetchWhiteLabeledContentSharedByVendorCompanies(Integer companyId) {
		XtremandResponse response = new XtremandResponse();
		response.setStatusCode(200);
		response.setData(damDao.fetchWhiteLabeledContentSharedByVendorCompanies(companyId));
		return response;
	}

	@Override
	public XtremandResponse findSharedAssetsByCompaniesForPartnerView(VanityUrlDetailsDTO vanityUrlDetailsDTO) {
		XtremandResponse response = new XtremandResponse();
		utilService.isVanityUrlFilterApplicable(vanityUrlDetailsDTO);
		response.setStatusCode(200);
		response.setData(damDao.findSharedAssetsByCompaniesForPartnerView(vanityUrlDetailsDTO));
		return response;
	}

	/***** XNFR-501 ****/
	@Override
	public XtremandResponse preview(AssetPdfPreviewRequestDTO assetPdfPreviewRequestDTO, BindingResult result) {
		XtremandResponse response = new XtremandResponse();
		assetPdfPreviewValidator.validate(assetPdfPreviewRequestDTO, result);
		if (result.hasErrors()) {
			xamplifyUtilValidator.addErrorResponse(result, response);
		} else {
			if (assetPdfPreviewRequestDTO.isTrackOrPlayBookPdfPreview()) {
				getHtmlBodyAndReplaceCompanyLogoAndWebsiteUrlAndPartnerLogo(assetPdfPreviewRequestDTO, response);
			} else {
				Integer damId = assetPdfPreviewRequestDTO.getId();
				getHtmlBodyAndReplaceCompanyLogoAndWebsiteUrl(damId, response);
				if (XamplifyUtils.isValidInteger(damId)) {
					asyncComponent.uploadDesignedPdfToAws(damId);
				}
			}
			XamplifyUtils.addSuccessStatus(response);
		}
		return response;

	}

	@Override
	public XtremandResponse findContentDetails(Integer id) {
		XtremandResponse response = new XtremandResponse();
		ContentPreviewDetailsDTO contentPreviewDetailsDTO = damDao.findContentDetails(id);
		if (contentPreviewDetailsDTO != null) {
			frameContentPreviewDetailsDTO(response, contentPreviewDetailsDTO);
		} else {
			contentNotFound(response);
		}
		return response;
	}

	private void frameContentPreviewDetailsDTO(XtremandResponse response,
			ContentPreviewDetailsDTO contentPreviewDetailsDTO) {
		String filePath = contentPreviewDetailsDTO.getFilePath();
		String videoFilePath = contentPreviewDetailsDTO.getVideoUri();
		if (StringUtils.hasText(filePath) || StringUtils.hasText(videoFilePath)) {
			List<String> availableImageFileTypes = fileUtil.getArrayList(imageFileTypes);
			List<String> supportedFileTypes = fileUtil.getArrayList(contentPreviewSupportedFileFormats);
			String fileType = contentPreviewDetailsDTO.getFileType();
			boolean isImageFile = availableImageFileTypes.indexOf(fileType) > -1;
			boolean isSupportedFile = supportedFileTypes.indexOf(fileType) > -1;
			if (isImageFile || isSupportedFile) {
				contentPreviewDetailsDTO.setImageFile(isImageFile);
				response.setStatusCode(200);
				if (StringUtils.hasText(videoFilePath)) {
					contentPreviewDetailsDTO.setFilePath(contentPreviewDetailsDTO.getVideoUri());
				}
				response.setData(contentPreviewDetailsDTO);
			} else {
				response.setStatusCode(1000);
				response.setMessage("This file cannot be previewed.");
			}

		} else {
			contentNotFound(response);
		}
	}

	public XtremandResponse findContentDetailsForPartner(Integer id, Integer damPartnerId) {
		XtremandResponse response = new XtremandResponse();
		ContentPreviewDetailsDTO contentPreviewDetailsDTO = damDao.findContentDetailsForPartner(id, damPartnerId);
		if (contentPreviewDetailsDTO != null) {
			frameContentPreviewDetailsDTO(response, contentPreviewDetailsDTO);
		} else {
			contentNotFound(response);
		}
		return response;
	}

	private void contentNotFound(XtremandResponse response) {
		response.setStatusCode(404);
		response.setMessage("No Content Found");
	}

	@Override
	public XtremandResponse findAssetAnalytics(Pagination pagination) {
		try {
			XtremandResponse response = new XtremandResponse();
			response.setStatusCode(200);
			response.setData(damDao.findAssetAnalytics(pagination));
			return response;
		} catch (DamDataAccessException mex) {
			throw new DamDataAccessException(mex);
		} catch (Exception e) {
			throw new DamDataAccessException(e);
		}
	}

	/**** XNFR-543 ****/
	@Override
	public XtremandResponse findAllPartners(Pageable pageable, Integer damId, Integer userId, BindingResult result) {
		XtremandResponse response = new XtremandResponse();
		boolean isDamIdMatched = checkIsDamIdBelongsToLoggedInUserCompany(damId, userId);
		if (isDamIdMatched) {
			Map<String, Object> map = new HashMap<>();
			map.put("isPublished", damDao.isAssetPublished(damId));
			map.put("isVideoFile", damDao.isVideoFile(damId));
			String sortColumnString = "viewCount,downloadCount,companyName";
			pageableValidator.validatePagableParameters(pageable, result, sortColumnString);
			if (result.hasErrors()) {
				xamplifyUtilValidator.addErrorResponse(result, response);
			} else {
				Pagination pagination = utilService.setPageableParameters(pageable, userId);
				pagination.setId(damId);
				pagination.setUserId(userId);
				pagination.setPartnerTeamMemberGroupFilter(pageable.isFilterPartners());
				pagination.setPartnerSignatureType(pageable.getPartnerSignatureType());
				PaginatedDTO paginatedDTO = damDao.findAllPartnersByPartnerCompanies(pagination, pageable.getSearch());
				addProxyPathForPdfSharedPath(paginatedDTO);
				XamplifyUtils.addPaginatedDTO(response, paginatedDTO);
				asyncComponent.uploadDesignedPdfToAws(damId);
			}
			response.setMap(map);
			return response;
		} else {
			throw new AccessDeniedException(XamplifyConstants.USER_NOT_AUTHORIZED_TO_ACCESS_PAGE);
		}

	}

	@Override
	public HttpServletResponse findAllPartnersByDamId(Pageable pageable, Integer damId, Integer userId,
			HttpServletResponse response) {
		boolean isDamIdMatched = checkIsDamIdBelongsToLoggedInUserCompany(damId, userId);
		if (isDamIdMatched) {
			String fileName = "Dam-Analytics-Report";
			Pagination pagination = utilService.setPageableParameters(pageable, userId);
			pagination.setId(damId);
			pagination.setUserId(userId);
			pagination.setExcludeLimit(true);
			pagination.setPartnerTeamMemberGroupFilter(pageable.isFilterPartners());
			pagination.setPartnerSignatureType(pageable.getPartnerSignatureType());
			PaginatedDTO paginatedDTO = damDao.findAllPartnersByPartnerCompanies(pagination, pageable.getSearch());
			PaginatedDTO paginatedDTOForSheet2 = damDao.findAllPartnersByPartnerCompaniesDetails(pagination,
					pageable.getSearch());
			@SuppressWarnings("unchecked")
			List<DamAnalyticsDTO> damAnalyticsList = (List<DamAnalyticsDTO>) paginatedDTO.getList();
			@SuppressWarnings("unchecked")
			List<DamAnalyticsDTO> damAnalticsDetailedList = (List<DamAnalyticsDTO>) paginatedDTOForSheet2.getList();
			return frameDamAnalyticsCSVData(response, fileName, damAnalyticsList, damAnalticsDetailedList, pagination);
		} else {
			throw new AccessDeniedException(XamplifyConstants.USER_NOT_AUTHORIZED_TO_ACCESS_PAGE);
		}

	}

	private HttpServletResponse frameDamAnalyticsCSVData(HttpServletResponse response, String fileName,
			List<DamAnalyticsDTO> damAnalyticsList, List<DamAnalyticsDTO> damAnalticsDetailedList,
			Pagination pagination) {
		try {
			XSSFWorkbook workbook = createWorkbookForDamAnalyticsData(damAnalyticsList, damAnalticsDetailedList,
					pagination);
			response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
			response.setHeader("Content-Disposition", "attachment; filename=" + fileName + ".xlsx");
			workbook.write(response.getOutputStream());
			workbook.close();
			return response;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return response;
	}

	private XSSFWorkbook createWorkbookForDamAnalyticsData(List<DamAnalyticsDTO> damAnalyticsList,
			List<DamAnalyticsDTO> damAnalticsDetailedList, Pagination pagination) {
		List<String[]> row = new ArrayList<>();
		List<String> headerList = new ArrayList<>();

		headerList.add("PARTNER COMPANY");
		headerList.add("ASSET NAME");
		headerList.add("ASSET TYPE");
		headerList.add("CREATED BY");
		headerList.add("VIEW COUNT");
		headerList.add("DOWNLOAD COUNT");
		headerList.add("PUBLISHED ON(PST)");

		row.add(headerList.toArray(new String[0]));
		for (DamAnalyticsDTO dam : damAnalyticsList) {
			String publishedDate = checkIfDateIsNull(dam.getPublishedOn());
			List<String> dataList = new ArrayList<>();
			dataList.add(dam.getContactCompany());
			dataList.add(dam.getAssetName());
			dataList.add(dam.getAssetType());
			dataList.add(dam.getCreatedBy());
			dataList.add(dam.getViewCount() != null ? dam.getViewCount().toString() : "0");
			dataList.add(dam.getDownloadCount() != null ? dam.getDownloadCount().toString() : "0");
			dataList.add(publishedDate);
			row.add(dataList.toArray(new String[0]));
		}

		XSSFWorkbook workbook = new XSSFWorkbook();
		Sheet sheet = workbook.createSheet("Dam Analytics Details");
		CreationHelper createHelper = workbook.getCreationHelper();

		CellStyle hyperlinkStyle = workbook.createCellStyle();
		Font linkFont = workbook.createFont();
		linkFont.setUnderline(Font.U_SINGLE);
		linkFont.setColor(IndexedColors.BLUE.getIndex());
		hyperlinkStyle.setFont(linkFont);

		Row headerRow = sheet.createRow(0);
		for (int i = 0; i < row.get(0).length; i++) {
			headerRow.createCell(i).setCellValue(row.get(0)[i]);
		}

		sheet.setAutoFilter(new CellRangeAddress(0, 0, 0, headerRow.getLastCellNum() - 1));

		int viewCountColumnIndex = -1;
		int downloadCountColumnIndex = -1;
		for (int i = 0; i < row.get(0).length; i++) {
			if ("VIEW COUNT".equalsIgnoreCase(row.get(0)[i])) {
				viewCountColumnIndex = i;
			}
			if ("DOWNLOAD COUNT".equalsIgnoreCase(row.get(0)[i])) {
				downloadCountColumnIndex = i;
			}
		}

		for (int i = 1; i < row.size(); i++) {
			Row dataRow = sheet.createRow(i);
			String[] data = row.get(i);

			for (int j = 0; j < data.length; j++) {
				Cell cell = dataRow.createCell(j);
				if (j == viewCountColumnIndex) {
					cell.setCellValue(data[j]);
					Hyperlink link = createHelper.createHyperlink(HyperlinkType.DOCUMENT);
					link.setAddress("'Dam Partner Analytics Details'!A1");
					cell.setHyperlink(link);
					cell.setCellStyle(hyperlinkStyle);
				} else if (j == downloadCountColumnIndex) {
					cell.setCellValue(data[j]);
					Hyperlink link = createHelper.createHyperlink(HyperlinkType.DOCUMENT);
					link.setAddress("'Dam Partner Analytics Details'!A1");
					cell.setHyperlink(link);
					cell.setCellStyle(hyperlinkStyle);
				} else {
					cell.setCellValue(data[j]);
				}
			}
		}

		Sheet viewDetailSheet = workbook.createSheet("Dam Partner Analytics Details");
		Row drillHeader = viewDetailSheet.createRow(0);
		drillHeader.createCell(0).setCellValue("ASSET NAME");
		drillHeader.createCell(1).setCellValue("ASSET TYPE");
		drillHeader.createCell(2).setCellValue("CREATED BY");
		drillHeader.createCell(3).setCellValue("PARTNER COMPANY NAME");
		drillHeader.createCell(4).setCellValue("PARTNER EMAIL ID (Including partner team members)");
		drillHeader.createCell(5).setCellValue("PARTNER NAME (Including partner team members)");
		drillHeader.createCell(6).setCellValue("VIEW COUNT");
		drillHeader.createCell(7).setCellValue("DOWNLOAD COUNT");
		drillHeader.createCell(8).setCellValue("PUBLISHED ON(PST)");

		viewDetailSheet.setAutoFilter(new CellRangeAddress(0, 0, 0, 8));

		int drillRowNum = 1;
		for (DamAnalyticsDTO dam : damAnalticsDetailedList) {
			Row r = viewDetailSheet.createRow(drillRowNum++);
			r.createCell(0).setCellValue(dam.getAssetName());
			r.createCell(1).setCellValue(dam.getAssetType());
			r.createCell(2).setCellValue(dam.getCreatedBy());
			r.createCell(3).setCellValue(dam.getCompanyName());
			r.createCell(4).setCellValue(dam.getEmailId());
			r.createCell(5).setCellValue(dam.getPartnerName());
			r.createCell(6).setCellValue(dam.getViewCount() != null ? dam.getViewCount().toString() : "0");
			r.createCell(7).setCellValue(dam.getDownloadCount() != null ? dam.getDownloadCount().toString() : "0");
			r.createCell(8).setCellValue(checkIfDateIsNull(dam.getPublishedOn()));
		}
		return workbook;
	}

	private String checkIfDateIsNull(Date date) {
		String dateToString = "";
		if (date != null) {
			dateToString = DateUtils.convertDateToString(date);
		}
		return dateToString;
	}

	private void addProxyPathForPdfSharedPath(PaginatedDTO paginatedDTO) {
		List<DamAnalyticsDTO> damList = (List<DamAnalyticsDTO>) paginatedDTO.getList();
		for (DamAnalyticsDTO damDto : damList) {
			String sharedAssetPath = damDto.getSharedAssetPath();
			if (XamplifyUtils.isValidString(sharedAssetPath)) {
				String baseUrl;
				if (xamplifyUtil.isDev()) {
					baseUrl = HOST;
				} else if (xamplifyUtil.isQA()) {
					baseUrl = devHost;
				} else if (xamplifyUtil.isProduction()) {
					baseUrl = productionHost;
				} else {
					baseUrl = HOST;
				}
				String proxyUrl = baseUrl + XAMPLIFY_PRM + "/api/pdf/proxy?pdfUrl=";
				damDto.setAssetProxyPath(proxyUrl);
			}
		}
	}

	@Override
	public XtremandResponse findAllPartnerCompanyUsers(Pageable pageable, Integer damPartnerId, boolean isExportToExcel,
			BindingResult result) {
		XtremandResponse response = new XtremandResponse();
		String sortColumnString = "viewCount,downloadCount,companyName,emailId,firstName,lastName";
		pageableValidator.validatePagableParameters(pageable, result, sortColumnString);
		if (result.hasErrors() && !isExportToExcel) {
			xamplifyUtilValidator.addErrorResponse(result, response);
		} else {
			Pagination pagination = utilService.setPageableParameters(pageable, null);
			pagination.setId(damPartnerId);
			PaginatedDTO paginatedDTO = damDao.findAllPartnerCompanyUsersByDamPartnerId(pagination,
					pageable.getSearch());
			XamplifyUtils.addPaginatedDTO(response, paginatedDTO);
		}
		return response;
	}

	@Override
	public XtremandResponse findContentDetailsForPartnerView(Integer damPartnerId, Integer loggedInPartnerId) {
		DamPartnerPreviewDTO damPreviewDTO = damDao.findHtmlBodyByDamPartnerId(damPartnerId);
		XtremandResponse response = new XtremandResponse();
		Map<String, Object> map = new HashMap<>();
		if (damPreviewDTO != null) {
			Integer id = damPreviewDTO.getId();
			boolean isBeePdf = damPreviewDTO.isBeeTemplate()
					&& !XamplifyUtils.isValidString(damPreviewDTO.getAssestPath());
			map.put("isBeePdf", isBeePdf);
			if (isBeePdf) {
				beeTemplateViewChanges(loggedInPartnerId, damPreviewDTO, response);
			} else {
				response = findContentDetailsForPartner(id, damPartnerId);
			}
			response.setMap(map);
		} else {
			response = XamplifyUtils.set404ErrorResponse();
		}
		return response;
	}

	private void beeTemplateViewChanges(Integer loggedInPartnerId, DamPartnerPreviewDTO damPreviewDTO,
			XtremandResponse response) {
		String htmlBody = damPreviewDTO.getHtmlBody();
		String updatedHtmlBody = "";
		User receiver = userDao.findByPrimaryKey(loggedInPartnerId, new FindLevel[] { FindLevel.COMPANY_PROFILE });
		User createdUser = userDao.findByPrimaryKey(damPreviewDTO.getCreatedBy(),
				new FindLevel[] { FindLevel.COMPANY_PROFILE });
		CompanyProfile vendorCompanyProfile = createdUser.getCompanyProfile();
		String companyLogo = vendorCompanyProfile != null ? vendorCompanyProfile.getCompanyLogoPath() : "";
		updatedHtmlBody = utilService.replaceSenderMergeTagsAndPartnerAboutUsMergeTag(htmlBody, receiver, null, true);
		CompanyProfile partnerCompany = receiver.getCompanyProfile();
		String partnerCompanyWebSiteUrl = partnerCompany != null ? partnerCompany.getWebsite() : "";
		String partnerCompanyLogo = partnerCompany != null ? partnerCompany.getCompanyLogoPath() : "";
		updatedHtmlBody = xamplifyUtil.replaceCompanyLogoAndCoBrandedLogo(updatedHtmlBody, partnerCompanyLogo,
				companyLogo, partnerCompanyWebSiteUrl);
		updatedHtmlBody = updatedHtmlBody.replace(DEFAULT_PARTNER_ABOUT_US_MERGE_TAG,
				receiver.getCompanyProfile().getAboutUs());

		updatedHtmlBody = utilService.replaceCompanyWebSiteUrl(updatedHtmlBody, createdUser);
		updatedHtmlBody = utilService.replaceReceiverMergeTags(receiver, updatedHtmlBody);
		response.setData(updatedHtmlBody);
		XamplifyUtils.addSuccessStatus(response);
	}

	private VanityUrlDetailsDTO createVanityUrlDetailsDTO(String subDomain, Integer loggedInUserId) {
		VanityUrlDetailsDTO vanityUrlDetailsDTO = new VanityUrlDetailsDTO();
		vanityUrlDetailsDTO.setVanityUrlFilter(true);
		vanityUrlDetailsDTO.setVendorCompanyProfileName(subDomain);
		vanityUrlDetailsDTO.setUserId(loggedInUserId);
		return vanityUrlDetailsDTO;
	}

	public XtremandResponse authorizeDamUrlAccess(String subDomain, Integer loggedInUserId, String routerUrl,
			Integer id) {
		XtremandResponse response = new XtremandResponse();
		boolean isUrlCanBeAccessedByPartner = "modules".equalsIgnoreCase(routerUrl)
				|| "shared".equalsIgnoreCase(routerUrl);
		if (StringUtils.hasText(subDomain)) {
			VanityUrlDetailsDTO vanityUrlDetailsDTO = createVanityUrlDetailsDTO(subDomain, loggedInUserId);
			utilService.isVanityUrlFilterApplicable(vanityUrlDetailsDTO);
			if (vanityUrlDetailsDTO.isVendorLoggedInThroughOwnVanityUrl()) {
				boolean isDamModuleEnabledBySuperAdmin = utilDao.hasDamAccessByUserId(loggedInUserId);
				List<Integer> roleIds = userDao.getRoleIdsByUserId(loggedInUserId);
				boolean isAnyVendorAdmin = Role.isAnyVendorCompanyAdmin(roleIds);
				boolean damAccess;
				boolean isDamRole = roleIds.contains(Role.DAM.getRoleId());

				damAccess = isAnyVendorAdmin || isDamRole;
				if (isDamModuleEnabledBySuperAdmin && damAccess) {
					XamplifyUtils.addSuccessStatus(response);
				} else {
					throw new AccessDeniedException(XamplifyConstants.USER_NOT_AUTHORIZED_TO_ACCESS_PAGE);
				}

			} else if (vanityUrlDetailsDTO.isPartnerLoggedInThroughVanityUrl()) {
				if (isUrlCanBeAccessedByPartner) {
					Integer companyId = userDao.getCompanyIdByProfileName(subDomain);
					boolean isDamModuleEnabledBySuperAdmin = utilDao.hasDamAccessByCompanyId(companyId);
					List<Integer> roleIds = userDao.getRoleIdsByUserId(loggedInUserId);
					boolean isPartnerAdmin = Role.isPartnerAdmin(roleIds);
					boolean isDamRole = roleIds.contains(Role.DAM.getRoleId());
					boolean isAssetSharedToPartner = damDao
							.isAssetSharedToPartnerCompanyByPartnerIdAndVendorCompany(loggedInUserId, companyId);
					boolean damAccessAsPartner = isPartnerAdmin || isDamRole || isAssetSharedToPartner;
					if (isDamModuleEnabledBySuperAdmin && damAccessAsPartner) {
						XamplifyUtils.addSuccessStatus(response);
					} else {
						throw new AccessDeniedException(XamplifyConstants.USER_NOT_AUTHORIZED_TO_ACCESS_PAGE);
					}
				} else {
					throw new AccessDeniedException(XamplifyConstants.USER_NOT_AUTHORIZED_TO_ACCESS_PAGE);
				}

			}

		} else {
			boolean isDamModuleEnabledBySuperAdmin = utilDao.hasDamAccessByUserId(loggedInUserId);
			List<Integer> roleIds = userDao.getRoleIdsByUserId(loggedInUserId);
			boolean isAnyVendorAdmin = Role.isAnyVendorCompanyAdmin(roleIds);
			boolean damAccess;
			boolean isAssetSharedToPartner = false;
			boolean isDamRole = roleIds.contains(Role.DAM.getRoleId());

			if (isUrlCanBeAccessedByPartner) {
				isAssetSharedToPartner = damDao.isAssetSharedToPartnerCompanyByPartnerId(loggedInUserId);
			}

			damAccess = isAnyVendorAdmin || isDamRole || isAssetSharedToPartner;
			if (isDamModuleEnabledBySuperAdmin && damAccess) {
				XamplifyUtils.addSuccessStatus(response);
			} else {
				throw new AccessDeniedException(XamplifyConstants.USER_NOT_AUTHORIZED_TO_ACCESS_PAGE);
			}
		}

		return response;
	}

	@Override
	public XtremandResponse validateDamId(Integer damId, Integer loggedInUserId) {
		XtremandResponse response = new XtremandResponse();
		boolean isDamIdMatched = checkIsDamIdBelongsToLoggedInUserCompany(damId, loggedInUserId);
		if (isDamIdMatched) {
			XamplifyUtils.addSuccessStatus(response);
		} else {
			throw new AccessDeniedException(XamplifyConstants.USER_NOT_AUTHORIZED_TO_ACCESS_PAGE);
		}
		return response;
	}

	@Override
	public XtremandResponse validateVideoId(Integer videoId, Integer loggedInUserId) {
		boolean isVideoIdMatched = true;
		XtremandResponse response = new XtremandResponse();
		isVideoIdMatched = isVideoIdMatched(videoId, loggedInUserId);
		if (isVideoIdMatched) {
			XamplifyUtils.addSuccessStatus(response);
		} else {
			throw new AccessDeniedException(XamplifyConstants.USER_NOT_AUTHORIZED_TO_ACCESS_PAGE);
		}
		return response;
	}

	private boolean isVideoIdMatched(Integer videoId, Integer loggedInUserId) {
		Integer companyId = userDao.getCompanyIdByUserId(loggedInUserId);
		return damDao.isVideoIdMatchedByCompanyIdAndVideoId(companyId, videoId);
	}

	@Override
	public XtremandResponse publishAssetToPartnerCompany(Integer userListId, Integer partnerUserId, Integer damId,
			Integer loggedInUserId) {
		XtremandResponse response = new XtremandResponse();
		Integer vendorCompanyId = userListDao.getCompanyIdByUserListId(userListId);
		Integer partnershipId = partnershipDao.getPartnershipIdByPartnerCompanyUserId(partnerUserId, vendorCompanyId);
		if (XamplifyUtils.isValidInteger(partnershipId)) {
			if (damDao.isAssetPublishedToPartner(damId, userListId, partnerUserId)) {
				return buildErrorResponse(400, "Asset has already been published.");
			}
			createDamPartnerWithGroupMappingAndShareAsset(userListId, partnerUserId, damId, loggedInUserId,
					partnershipId);
			response.setStatusCode(200);
			response.setMessage("Published successfully.");
		} else {
			return buildErrorResponse(400, "Partnership is not established.");
		}

		return response;
	}

	private XtremandResponse buildErrorResponse(int statusCode, String message) {
		return XamplifyUtils.buildErrorResponse(statusCode, message);
	}

	private void createDamPartnerWithGroupMappingAndShareAsset(Integer userListId, Integer partnerUserId, Integer damId,
			Integer loggedInUserId, Integer partnershipId) {
		Dam dam = damDao.getById(damId);
		Partnership partnership = new Partnership();
		partnership.setId(partnershipId);

		Integer damPartnerId = damDao.findDamPartnerIdByDamIdAndPartnershipId(damId, partnershipId);
		DamPartner damPartner = new DamPartner();
		if (XamplifyUtils.isValidInteger(damPartnerId)) {
			damPartner.setId(damPartnerId);
		} else {
			damPartner.setDam(dam);
			damPartner.setAlias(XamplifyUtils.generateAlias());
			damPartner.setHtmlBody(dam.getHtmlBody());
			damPartner.setJsonBody(dam.getJsonBody());
			damPartner.setPartnerGroupSelected(true);
			damPartner.setPartnership(partnership);
			damPartner.setPublishedBy(loggedInUserId);
			damPartner.setPublishedTime(new Date());
			genericDAO.save(damPartner);
		}

		DamPartnerGroupMapping groupMapping = new DamPartnerGroupMapping();
		groupMapping.setDamPartner(damPartner);
		groupMapping.setUserId(partnerUserId);
		groupMapping.setUserListId(userListId);
		groupMapping.setCreatedTime(new Date());
		genericDAO.save(groupMapping);

		utilService.insertIntoDamPartnerGroupUserMapping(partnershipId, partnerUserId, groupMapping.getId());

		DamUploadPostDTO damUploadPostDTO = new DamUploadPostDTO();
		damUploadPostDTO.setLoggedInUserId(loggedInUserId);
		damUploadPostDTO.setDamId(dam.getId());
		damUploadPostDTO.setShareAsWhiteLabeledAsset(dam.isWhiteLabeledAssetSharedWithPartners());
		damUploadPostDTO.setAssetName(XamplifyUtils.removeExtraSpace(dam.getAssetName()));
		VideoFile videoFile = dam.getVideoFile();
		if (videoFile != null) {
			DamVideoDTO damVideoDTO = damDao.findDamAndVideoDetailsByVideoId(videoFile.getId());
			damUploadPostDTO.setDamVideoDTO(damVideoDTO);
		}

		damDao.handleWhiteLabeledAssetSharing(damUploadPostDTO, null, dam, partnershipId);
	}

	@Override
	public XtremandResponse addPartnerGroup(Integer userListId, Integer partnershipId, Integer damId,
			Integer loggedInUserId) {
		XtremandResponse response = new XtremandResponse();
		Integer damPartnerId = damDao.getDamPartnerIdByDamIdAndPartnershipId(damId, partnershipId);
		Integer partnerUserId = partnershipDao.getPartnerIdByPartnershipId(partnershipId);
		DamPartner damPartner = new DamPartner();
		damPartner.setId(damPartnerId);
		DamPartnerGroupMapping damPartnerGroupMapping = new DamPartnerGroupMapping();
		damPartnerGroupMapping.setDamPartner(damPartner);
		damPartnerGroupMapping.setUserId(partnerUserId);
		damPartnerGroupMapping.setUserListId(userListId);
		damPartnerGroupMapping.setCreatedTime(new Date());
		genericDAO.save(damPartnerGroupMapping);
		XamplifyUtils.addSuccessStatusWithMessage(response, "Group Added Successfully.");
		return response;
	}

	/** XNFR-781 start **/
	private void setAssetApprovalStatus(Integer loggedInUserId, Integer companyId, Dam dam, boolean isDraft) {
		if (XamplifyUtils.isValidInteger(loggedInUserId) && XamplifyUtils.isValidInteger(companyId) && dam != null) {
			ApprovalStatusType approvalStatus = determineApprovalStatus(loggedInUserId, companyId, isDraft);
			dam.setApprovalStatus(approvalStatus);
			dam.setApprovalStatusUpdatedBy(loggedInUserId);
			dam.setApprovalStatusUpdatedTime(new Date());
		}
	}

	private ApprovalStatusType determineApprovalStatus(Integer loggedInUserId, Integer companyId, boolean isDraft) {
		if (isDraft) {
			return ApprovalStatusType.DRAFT;
		}

		boolean isApprovalRequiredForAssets = userDao.checkIfAssetApprovalRequiredByCompanyId(companyId);
		if (!isApprovalRequiredForAssets) {
			return ApprovalStatusType.APPROVED;
		}

		boolean isApprovalPrivilegeManager = approveDao.isApprovalPrivilegeManager(loggedInUserId);
		boolean isAssetApprover = approveDao.checkIsAssetApproverByTeamMemberIdAndCompanyId(loggedInUserId, companyId);

		return (isApprovalPrivilegeManager || isAssetApprover) ? ApprovalStatusType.APPROVED
				: ApprovalStatusType.CREATED;
	}

	@Override
	public XtremandResponse uploadPartnerSignature(SharedAssetDetailsViewDTO sharedAssetDetailsViewDTO,
			MultipartFile uploadedFile) {
		XtremandResponse response = new XtremandResponse();
		Path filePathToDelete = null;
		Integer loggedInUserId = sharedAssetDetailsViewDTO.getLoggedInUserId();
		if (XamplifyUtils.isValidInteger(loggedInUserId)) {
			String companyName = userDao.getCompanyNameByUserId(loggedInUserId);
			User user = userDao.getFirstNameLastNameAndJobTitleByUserId(loggedInUserId);
			String userAlias = userDao.getAliasByUserId(loggedInUserId);
			String signaturePath = fileUtil.createSignatureFolderPath(userAlias);
			String selectedSignatureImagePath = sharedAssetDetailsViewDTO.getSelectedSignaturePath();
			String originalFilename = selectedSignatureImagePath
					.substring(selectedSignatureImagePath.lastIndexOf("/") + 1);
			String completeSignaturePath = signaturePath + sep + originalFilename;
			if (XamplifyUtils.isValidString(sharedAssetDetailsViewDTO.getSharedAssetPath())) {
				String pdfUrl = sharedAssetDetailsViewDTO.getSharedAssetPath();
				File originalPdfFile = fileUtil.convertMultipartFileToFile(uploadedFile);
				if (originalPdfFile != null) {
					updateSharedAssetPathAndDeleteLocalFile(sharedAssetDetailsViewDTO, response, filePathToDelete,
							loggedInUserId, companyName, user, selectedSignatureImagePath, completeSignaturePath,
							originalPdfFile);
				}
			}
		}
		return response;
	}

	private void updateSharedAssetPathAndDeleteLocalFile(SharedAssetDetailsViewDTO sharedAssetDetailsViewDTO,
			XtremandResponse response, Path filePathToDelete, Integer loggedInUserId, String companyName, User user,
			String selectedSignatureImagePath, String completeSignaturePath, File originalPdfFile) {
		try (PDDocument document = PDDocument.load(originalPdfFile)) {
			if (document.isEncrypted()) {
				document.setAllSecurityToBeRemoved(true);
			}
//			if (XtremandUtils.isValidString(selectedSignatureImagePath)) {
//				File signatureImageFile = new File(completeSignaturePath);
//				addPartnerSignatureByVendorSIgnatureCompleted(sharedAssetDetailsViewDTO, companyName, user, document,
//						signatureImageFile);
//			}
			filePathToDelete = updateSharedAssetPathForPartner(sharedAssetDetailsViewDTO, response, loggedInUserId,
					document, completeSignaturePath);
		} catch (IOException e) {
			e.printStackTrace();
		}
		deleteLocalFilePath(filePathToDelete);
	}

	private Path updateSharedAssetPathForPartner(SharedAssetDetailsViewDTO sharedAssetDetailsViewDTO,
			XtremandResponse response, Integer loggedInUserId, PDDocument document, String assetName)
			throws IOException {
		Path filePathToDelete;
		String modifiedPdfFileName = "modified_" + UUID.randomUUID().toString() + ".pdf";
		String modifiedPdfFilePath = fileUtil.getPathForModifiedSharedAssetPdfPath("shared-asset", modifiedPdfFileName,
				loggedInUserId);
		try (FileOutputStream fos = new FileOutputStream(modifiedPdfFilePath)) {
			document.save(fos);
		}
		String uniqueAssetFileName = xamplifyUtil.generateUniqueFileName(modifiedPdfFilePath,
				sharedAssetDetailsViewDTO.getDamId());
		xamplifyUtil.saveFileToAws(xamplifyUtil.getAmazonClient(), uniqueAssetFileName, modifiedPdfFilePath, null);
		filePathToDelete = Paths.get(modifiedPdfFilePath);
		response.setFilePath(amazonBaseUrl + xamplifyUtil.getBucketName() + '/' + uniqueAssetFileName);
		String updatedSharedAssetPath = amazonBaseUrl + xamplifyUtil.getBucketName() + '/' + uniqueAssetFileName;
		damDao.updateSharedAssetPathForPartner(updatedSharedAssetPath, sharedAssetDetailsViewDTO.getId());
		String body = "";
		String subject = "";
		DamDTO damDto = damDao.getVendorAndPartnerIdAssets(sharedAssetDetailsViewDTO.getDamId());
		List<UserDetailsUtilDTO> adminAndSupervisorsDetailsList = userDao
				.fetchAdminsAndSupervisorsByPartnerIdAndVendorCompanyId(damDto.getCompanyId(), damDto.getCreatedBy());

		damDao.updateSharedAssetPathForPartner(updatedSharedAssetPath, sharedAssetDetailsViewDTO.getId());
		UserDTO user = userDao
				.getFullNameAndEmailIdAndCompanyNameByUserId(sharedAssetDetailsViewDTO.getLoggedInUserId());
		boolean hasVanityAccess = utilDao.hasVanityAccessByCompanyId(damDto.getCompanyId());
		CompanyProfile cp = new CompanyProfile();
		cp.setId(damDto.getCompanyId());
		if (hasVanityAccess) {
			DefaultEmailTemplate defaultTemplate = vanityURLDao
					.getVanityDefaultEmailTemplateByType(DefaultEmailTemplateType.PARTNER_PDF_SIGNATURE_COMPLETED);
			if (defaultTemplate != null) {
				CustomDefaultEmailTemplate customTemplate = vanityURLDao
						.getVanityETByDefVanityETIdAndCompanyId(defaultTemplate.getId(), cp);
				if (customTemplate != null) {
					subject = customTemplate.getSubject();
					body = genericDao.get(CustomDefaultEmailTemplate.class, customTemplate.getId()).getHtmlBody();
				} else {
					subject = defaultTemplate.getSubject();
					body = genericDAO.get(DefaultEmailTemplate.class, defaultTemplate.getId()).getHtmlBody();

				}
			}
		} else {
			DefaultEmailTemplate defaultTemplate = vanityURLDao
					.getVanityDefaultEmailTemplateByType(DefaultEmailTemplateType.PARTNER_PDF_SIGNATURE_COMPLETED);
			if (defaultTemplate != null) {
				subject = defaultTemplate.getSubject();
				body = defaultTemplate.getHtmlBody();
			}
		}

		for (UserDetailsUtilDTO userDetails : adminAndSupervisorsDetailsList) {
			String htmlBody = body;
			if (hasVanityAccess && StringUtils.hasText(user.getCompanyProfileName())) {
				htmlBody = htmlBody.replace("{{vendorFullName}}", userDetails.getFullName());
				String updatedCompanyLogoPath = serverPath + (user.getCompanyLogo());
				htmlBody = htmlBody.replace(coBrandingLogo, updatedCompanyLogoPath);
				htmlBody = htmlBody.replace("<Vanity_Company_Logo>", updatedCompanyLogoPath);
				htmlBody = replaceAssetLink(assetName, htmlBody, sharedAssetDetailsViewDTO, damDto, user, userDetails);

				htmlBody = htmlBody.replace("{{partnerName}}", user.getFullName());
				htmlBody = htmlBody.replace("{{partnerCompanyName}}", user.getCompanyName());
				htmlBody = htmlBody.replaceAll("<Vanity_Company_Logo_Href>", user.getWebsite());
			} else {
				htmlBody = htmlBody.replace("{{vendorFullName}}", userDetails.getFullName());
				htmlBody = replaceAssetLink(assetName, htmlBody, sharedAssetDetailsViewDTO, damDto, user, userDetails);
				htmlBody = htmlBody.replace("{{partnerName}}", user.getFullName());
				htmlBody = htmlBody.replace("{{partnerCompanyName}}", user.getCompanyName());
				htmlBody = htmlBody.replace(coBrandingLogo, "xAmplifyLogo");
				htmlBody = htmlBody.replace("<Vanity_Company_Logo_Href>", webUrl + "/login");
			}
			mailService.sendMail(new EmailBuilder().from(user.getEmailId()).senderName(user.getCompanyName())
					.to(userDetails.getEmailId()).subject(subject).body(htmlBody).build());
		}
		return filePathToDelete;
	}

	private void addPartnerSignatureByVendorSIgnatureCompleted(SharedAssetDetailsViewDTO sharedAssetDetailsViewDTO,
			String companyName, User user, PDDocument document, File signatureImageFile) throws IOException {
		if (sharedAssetDetailsViewDTO.isVendorSignatureCompleted()) {
			addPartnerSignatureToLastPage(document, signatureImageFile,
					sharedAssetDetailsViewDTO.getGeoLocationDetails(), companyName, user);
		} else {
			addPartnerSignatureToNewPage(document, signatureImageFile,
					sharedAssetDetailsViewDTO.getGeoLocationDetails(), companyName, user);
		}
	}

	private File downloadFileFromS3(String fileUrl) {
		try {
			URL url = new URL(fileUrl);
			File tempFile = new File(System.getProperty("java.io.tmpdir"),
					"tempFisle_" + UUID.randomUUID().toString() + ".pdf");
			FileUtils.copyURLToFile(url, tempFile);
			return tempFile;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	private void addVendorSignature(PDDocument document, File vendorSignatureImage, User user, String companyName,
			GeoLocationAnalyticsDTO geoLocationAnalyticsDTO) throws IOException {
		PDImageXObject vendorImage = PDImageXObject.createFromFile(vendorSignatureImage.getAbsolutePath(), document);
		PDPage vendorPage = new PDPage(PDRectangle.A4);
		document.addPage(vendorPage);
		PDPageContentStream contentStream = new PDPageContentStream(document, vendorPage);

		float labelFontSize = 9;
		contentStream.setFont(PDType1Font.HELVETICA_BOLD, labelFontSize);

		PDType1Font normalFont = PDType1Font.HELVETICA;
		float valueFontSize = 9;

		float yPosition = 720;
		float leftMargin = 20;
		float labelXPosition = leftMargin;

		float vendorImageWidth = 100;
		float vendorImageHeight = 50;

		contentStream.drawImage(vendorImage, leftMargin, yPosition, vendorImageWidth, vendorImageHeight);

		drawRectangleAroundImage(contentStream, leftMargin, yPosition, vendorImageWidth, vendorImageHeight);

		addUserFirstNameAndLastNameTextToPdf(user, contentStream, normalFont, valueFontSize, yPosition, labelXPosition);

		addCompanyNameToThePdf(companyName, contentStream, labelFontSize, normalFont, valueFontSize, yPosition,
				labelXPosition);

		addTitleToPdf(user, contentStream, labelFontSize, normalFont, valueFontSize, yPosition, labelXPosition);

		addGeolocationTextToPdf(geoLocationAnalyticsDTO, contentStream, labelFontSize, normalFont, valueFontSize,
				yPosition, labelXPosition);

		String latLongText = addLatitudeAndLongitureTestToPdf(geoLocationAnalyticsDTO);

		frameLatitudeLongitudeTextForPdf(contentStream, labelFontSize, normalFont, valueFontSize, yPosition,
				labelXPosition, latLongText);

		addCurrentDateToPdf(contentStream, labelFontSize, normalFont, valueFontSize, yPosition, labelXPosition);

		contentStream.close();
	}

	private void addPartnerSignatureToLastPage(PDDocument document, File partnerSignatureImage,
			GeoLocationAnalyticsDTO geoLocationAnalyticsDTO, String companyName, User user) throws IOException {
		PDImageXObject partnerImage = PDImageXObject.createFromFile(partnerSignatureImage.getAbsolutePath(), document);
		PDPage lastPage = document.getPage(document.getNumberOfPages() - 1);
		PDPageContentStream contentStream = new PDPageContentStream(document, lastPage,
				PDPageContentStream.AppendMode.APPEND, true);

		float labelFontSize = 9;
		contentStream.setFont(PDType1Font.HELVETICA_BOLD, labelFontSize);

		PDType1Font normalFont = PDType1Font.HELVETICA;
		float valueFontSize = 9;

		float yPosition = 720;
		float rightMargin = 380;

		float partnerImageWidth = 100;
		float partnerImageHeight = 50;

		contentStream.drawImage(partnerImage, rightMargin, yPosition, partnerImageWidth, partnerImageHeight);

		float labelXPosition = rightMargin;

		drawRectangleAroundImage(contentStream, rightMargin, yPosition, partnerImageWidth, partnerImageHeight);

		addUserFirstNameAndLastNameTextToPdf(user, contentStream, normalFont, valueFontSize, yPosition, labelXPosition);

		addCompanyNameToThePdf(companyName, contentStream, labelFontSize, normalFont, valueFontSize, yPosition,
				labelXPosition);

		addTitleToPdf(user, contentStream, labelFontSize, normalFont, valueFontSize, yPosition, labelXPosition);

		addGeolocationTextToPdf(geoLocationAnalyticsDTO, contentStream, labelFontSize, normalFont, valueFontSize,
				yPosition, labelXPosition);

		String latLongText = addLatitudeAndLongitureTestToPdf(geoLocationAnalyticsDTO);

		frameLatitudeLongitudeTextForPdf(contentStream, labelFontSize, normalFont, valueFontSize, yPosition,
				labelXPosition, latLongText);

		addCurrentDateToPdf(contentStream, labelFontSize, normalFont, valueFontSize, yPosition, labelXPosition);

		contentStream.close();
	}

	private void addPartnerSignatureToNewPage(PDDocument document, File partnerSignatureImage,
			GeoLocationAnalyticsDTO geoLocationAnalyticsDTO, String companyName, User user) throws IOException {
		PDImageXObject partnerImage = PDImageXObject.createFromFile(partnerSignatureImage.getAbsolutePath(), document);
		PDPage partnerPage = new PDPage(PDRectangle.A4);
		document.addPage(partnerPage);

		PDPageContentStream contentStream = new PDPageContentStream(document, partnerPage);

		float labelFontSize = 9;
		contentStream.setFont(PDType1Font.HELVETICA_BOLD, labelFontSize);

		PDType1Font normalFont = PDType1Font.HELVETICA;
		float valueFontSize = 9;

		float yPosition = 720;
		float rightMargin = 380;

		float partnerImageWidth = 100;
		float partnerImageHeight = 50;

		contentStream.drawImage(partnerImage, rightMargin, yPosition, partnerImageWidth, partnerImageHeight);

		float labelXPosition = rightMargin;

		drawRectangleAroundImage(contentStream, rightMargin, yPosition, partnerImageWidth, partnerImageHeight);

		addUserFirstNameAndLastNameTextToPdf(user, contentStream, normalFont, valueFontSize, yPosition, labelXPosition);

		addCompanyNameToThePdf(companyName, contentStream, labelFontSize, normalFont, valueFontSize, yPosition,
				labelXPosition);

		addTitleToPdf(user, contentStream, labelFontSize, normalFont, valueFontSize, yPosition, labelXPosition);

		addGeolocationTextToPdf(geoLocationAnalyticsDTO, contentStream, labelFontSize, normalFont, valueFontSize,
				yPosition, labelXPosition);

		String latLongText = addLatitudeAndLongitureTestToPdf(geoLocationAnalyticsDTO);

		frameLatitudeLongitudeTextForPdf(contentStream, labelFontSize, normalFont, valueFontSize, yPosition,
				labelXPosition, latLongText);

		addCurrentDateToPdf(contentStream, labelFontSize, normalFont, valueFontSize, yPosition, labelXPosition);

		contentStream.close();
	}

	private void addUserFirstNameAndLastNameTextToPdf(User user, PDPageContentStream contentStream,
			PDType1Font normalFont, float valueFontSize, float yPosition, float labelXPosition) throws IOException {
		String nameToShow = "--";

		if (user.getFirstName() != null && !user.getFirstName().isEmpty() && user.getLastName() != null
				&& !user.getLastName().isEmpty()) {
			nameToShow = user.getFirstName() + " " + user.getLastName();
		} else if (user.getFirstName() != null && !user.getFirstName().isEmpty()) {
			nameToShow = user.getFirstName();
		} else if (user.getLastName() != null && !user.getLastName().isEmpty()) {
			nameToShow = user.getLastName();
		}
		if (nameToShow.length() > 40) {
			nameToShow = nameToShow.substring(0, 40);
		}
		contentStream.beginText();
		contentStream.newLineAtOffset(labelXPosition, yPosition - 20);
		contentStream.showText("Name: ");
		contentStream.setFont(normalFont, valueFontSize);
		contentStream.showText(nameToShow);
		contentStream.endText();
	}

	private void addCompanyNameToThePdf(String companyName, PDPageContentStream contentStream, float labelFontSize,
			PDType1Font normalFont, float valueFontSize, float yPosition, float labelXPosition) throws IOException {
		if (companyName.length() > 35) {
			companyName = companyName.substring(0, 35);
		}
		contentStream.beginText();
		contentStream.newLineAtOffset(labelXPosition, yPosition - 32);
		contentStream.setFont(PDType1Font.HELVETICA_BOLD, labelFontSize);
		contentStream.showText("Company Name: ");
		contentStream.setFont(normalFont, valueFontSize);
		contentStream.showText(companyName);
		contentStream.endText();
	}

	private void addTitleToPdf(User user, PDPageContentStream contentStream, float labelFontSize,
			PDType1Font normalFont, float valueFontSize, float yPosition, float labelXPosition) throws IOException {
		String title = "--";
		if (XamplifyUtils.isValidString(user.getOccupation())) {
			title = user.getOccupation();
		}
		contentStream.beginText();
		contentStream.newLineAtOffset(labelXPosition, yPosition - 44);
		contentStream.setFont(PDType1Font.HELVETICA_BOLD, labelFontSize);
		contentStream.showText("Job Title: ");
		contentStream.setFont(normalFont, valueFontSize);
		contentStream.showText(title);
		contentStream.endText();
	}

	private void addGeolocationTextToPdf(GeoLocationAnalyticsDTO geoLocationAnalyticsDTO,
			PDPageContentStream contentStream, float labelFontSize, PDType1Font normalFont, float valueFontSize,
			float yPosition, float labelXPosition) throws IOException {
		String geoLocationText = "";
		if (geoLocationAnalyticsDTO != null) {
			StringBuilder geoLocation = new StringBuilder();

			if (geoLocationAnalyticsDTO.getCity() != null) {
				geoLocation.append(geoLocationAnalyticsDTO.getCity());
			}
			if (geoLocationAnalyticsDTO.getState() != null) {
				geoLocation.append(", ");
				geoLocation.append(geoLocationAnalyticsDTO.getState());
			}
			if (geoLocationAnalyticsDTO.getCountry() != null) {
				geoLocation.append(", ");
				geoLocation.append(geoLocationAnalyticsDTO.getCountry());
			}

			geoLocationText += geoLocation.toString();
		}
		contentStream.beginText();
		contentStream.newLineAtOffset(labelXPosition, yPosition - 56);
		contentStream.setFont(PDType1Font.HELVETICA_BOLD, labelFontSize);
		contentStream.showText("Geo-location: ");
		contentStream.setFont(normalFont, valueFontSize);
		contentStream.showText(geoLocationText);
		contentStream.endText();
	}

	private String addLatitudeAndLongitureTestToPdf(GeoLocationAnalyticsDTO geoLocationAnalyticsDTO) {
		String latLongText = "";
		if (geoLocationAnalyticsDTO != null) {
			if (geoLocationAnalyticsDTO.getLatitude() != null) {
				latLongText += geoLocationAnalyticsDTO.getLatitude();
			}
			if (geoLocationAnalyticsDTO.getLongitude() != null) {
				if (!latLongText.isEmpty()) {
					latLongText += ", ";
				}
				latLongText += geoLocationAnalyticsDTO.getLongitude();
			}
		}
		return latLongText;
	}

	private void frameLatitudeLongitudeTextForPdf(PDPageContentStream contentStream, float labelFontSize,
			PDType1Font normalFont, float valueFontSize, float yPosition, float labelXPosition, String latLongText)
			throws IOException {
		if (!latLongText.isEmpty()) {
			contentStream.beginText();
			contentStream.newLineAtOffset(labelXPosition, yPosition - 68);
			contentStream.setFont(PDType1Font.HELVETICA_BOLD, labelFontSize);
			contentStream.showText("Latitude/Longitude: ");
			contentStream.setFont(normalFont, valueFontSize);
			contentStream.showText(latLongText);
			contentStream.endText();
		}
	}

	private void addCurrentDateToPdf(PDPageContentStream contentStream, float labelFontSize, PDType1Font normalFont,
			float valueFontSize, float yPosition, float labelXPosition) throws IOException {
		String dateText = new SimpleDateFormat("dd/MM/yyyy").format(new Date());
		contentStream.beginText();
		contentStream.newLineAtOffset(labelXPosition, yPosition - 80);
		contentStream.setFont(PDType1Font.HELVETICA_BOLD, labelFontSize);
		contentStream.showText("Date: ");
		contentStream.setFont(normalFont, valueFontSize);
		contentStream.showText(dateText);
		contentStream.endText();
	}

	private void drawRectangleAroundImage(PDPageContentStream contentStream, float imageXPosition, float imageYPosition,
			float imageWidth, float imageHeight) throws IOException {
		contentStream.setLineWidth(1);
		contentStream.setStrokingColor(Color.BLACK);
		contentStream.moveTo(imageXPosition, imageYPosition);
		contentStream.lineTo(imageXPosition + imageWidth, imageYPosition);
		contentStream.lineTo(imageXPosition + imageWidth, imageYPosition + imageHeight);
		contentStream.lineTo(imageXPosition, imageYPosition + imageHeight);
		contentStream.closePath();
		contentStream.stroke();
	}

	public MultipartFile convertToHtmlAndPushToAws(String htmlBody) {
		String pdfFileName = "output.pdf";
		String folderPath = mediaBasePath + "shared-asset26/" + System.currentTimeMillis();
		String completeFileName = folderPath + "/" + pdfFileName;

		try {
			// Ensure directory exists
			Path pdfDirPath = Paths.get(folderPath);
			Files.createDirectories(pdfDirPath);

			// Create a temporary HTML file
			File htmlSource = File.createTempFile("tempHtml", ".html");
			htmlSource.deleteOnExit();
			Files.write(htmlSource.toPath(), htmlBody.getBytes(StandardCharsets.UTF_8));

			// Create the PDF destination file
			File pdfDest = new File(completeFileName);

			// Convert HTML to PDF
			try (InputStream stream = new FileInputStream(htmlSource);
					PdfWriter writer = new PdfWriter(pdfDest);
					PdfDocument pdf = new PdfDocument(writer)) {

				ConverterProperties converterProperties = new ConverterProperties();
				HtmlConverter.convertToPdf(stream, pdf, converterProperties);
			}

			// Ensure PDF file exists
			if (!pdfDest.exists() || pdfDest.length() == 0) {
				System.err.println("PDF generation failed: File does not exist or is empty.");
				return null;
			}

			// Convert PDF File to MultipartFile using MockMultipartFile
			try (InputStream pdfInputStream = new FileInputStream(pdfDest)) {
				return new MockMultipartFile("file", pdfFileName, "application/pdf", pdfInputStream);
			}

		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
//	public byte[] convertToHtmlAndPushToAws(String htmlBody) {
//	    if (htmlBody == null || htmlBody.trim().isEmpty()) {
//	        throw new IllegalArgumentException("HTML content cannot be null or empty");
//	    }
//
//	    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//	    try {
//	        HtmlConverter.convertToPdf(htmlBody, outputStream);
//	        byte[] pdfBytes = outputStream.toByteArray();
//
//	        // Debugging: Write to a file and check if it's valid
//	        Files.write(Paths.get("debug-output.pdf"), pdfBytes);
//	        System.out.println("PDF successfully written to debug-output.pdf");
//
//	        return pdfBytes;
//	    } catch (Exception e) {
//	        e.printStackTrace();
//	        throw new RuntimeException("Failed to convert HTML to PDF", e);
//	    }
//	}

	@Override
	public XtremandResponse uploadVendorSignatureForPartner(SharedAssetDetailsViewDTO sharedAssetDetailsViewDTO,
			MultipartFile uploadedFile) {
		XtremandResponse response = new XtremandResponse();
		Path filePathToDelete = null;
		Integer loggedInUserId = sharedAssetDetailsViewDTO.getLoggedInUserId();
		if (XamplifyUtils.isValidInteger(loggedInUserId)) {
			if (uploadedFile != null) {
				File originalPdfFile = fileUtil.convertMultipartFileToFile(uploadedFile);
				if (originalPdfFile != null) {
					updateSharedAssetPathAndDeleteLocalFileForvendor(sharedAssetDetailsViewDTO, response,
							filePathToDelete, loggedInUserId, originalPdfFile);
				}
			}
		}
		return response;
	}

	private void updateSharedAssetPathAndDeleteLocalFileForvendor(SharedAssetDetailsViewDTO sharedAssetDetailsViewDTO,
			XtremandResponse response, Path filePathToDelete, Integer loggedInUserId, File originalPdfFile) {
		try (PDDocument document = PDDocument.load(originalPdfFile)) {
			if (document.isEncrypted()) {
				document.setAllSecurityToBeRemoved(true);
			}
			filePathToDelete = updateSharedAssetPathForPartnerAndAddVendorSignatureToPdf(sharedAssetDetailsViewDTO,
					response, loggedInUserId, document);
		} catch (IOException e) {
			e.printStackTrace();
		}
		deleteLocalFilePath(filePathToDelete);
	}

	private Path updateSharedAssetPathForPartnerAndAddVendorSignatureToPdf(
			SharedAssetDetailsViewDTO sharedAssetDetailsViewDTO, XtremandResponse response, Integer loggedInUserId,
			PDDocument document) throws FileNotFoundException, IOException {
		Path filePathToDelete;
		String modifiedPdfFileName = "modified_" + UUID.randomUUID().toString() + ".pdf";
		String modifiedPdfFilePath = fileUtil.getPathForModifiedSharedAssetPdfPath("shared-asset", modifiedPdfFileName,
				loggedInUserId);
		try (FileOutputStream fos = new FileOutputStream(modifiedPdfFilePath)) {
			document.save(fos);
		}
		String uniqueAssetFileName = xamplifyUtil.generateUniqueFileName(modifiedPdfFilePath,
				sharedAssetDetailsViewDTO.getDamId());
		xamplifyUtil.saveFileToAws(xamplifyUtil.getAmazonClient(), uniqueAssetFileName, modifiedPdfFilePath, null);
		filePathToDelete = Paths.get(modifiedPdfFilePath);
		response.setFilePath(amazonBaseUrl + xamplifyUtil.getBucketName() + '/' + uniqueAssetFileName);
		String updatedSharedAssetPath = amazonBaseUrl + xamplifyUtil.getBucketName() + '/' + uniqueAssetFileName;
		damDao.updateSharedAssetPathForPartnerAndVendorSignatureCompleted(updatedSharedAssetPath,
				sharedAssetDetailsViewDTO.getId());
		return filePathToDelete;
	}

	@Override
	public XtremandResponse getSharedAssetDetailsByIdForVendor(Integer id) {
		try {
			XtremandResponse response = new XtremandResponse();
			response.setAccess(true);
			if (XamplifyUtils.isValidInteger(id)) {
				SharedAssetDetailsViewDTO dto = damDao.getSharedAssetDetailsById(id);
				if (dto != null) {
					response.setStatusCode(200);
					getEncodedSharedAssetpath(dto);
					response.setData(dto);
				} else {
					response.setStatusCode(404);
				}
				return response;
			} else {
				throw new AccessDeniedException(XamplifyConstants.USER_NOT_AUTHORIZED_TO_ACCESS_PAGE);
			}
		} catch (AccessDeniedException ae) {
			throw new AccessDeniedException(ae.getMessage());
		} catch (DamDataAccessException mex) {
			throw new DamDataAccessException(mex);
		} catch (Exception e) {
			throw new DamDataAccessException(e);
		}
	}

	private void getEncodedSharedAssetpath(SharedAssetDetailsViewDTO dto) throws UnsupportedEncodingException {
		List<String> availableImageFileTypes = fileUtil.getArrayList(imageFileTypes);
		List<String> availabletextFileTypes = fileUtil.getArrayList(contentPreviewForTextView);
		List<String> availableContentPreviewTypes = fileUtil.getArrayList(contentPreviewSupportedFileFormats);
		dto.setTextFileType(availabletextFileTypes.contains(dto.getAssetType()));
		dto.setImageFileType(availableImageFileTypes.contains(dto.getAssetType()));
		dto.setContentPreviewType(availableContentPreviewTypes.contains(dto.getAssetType()));
		/*** XNFR-930 ***/
		if (XamplifyUtils.isValidString(dto.getAssetPath())) {
			String updatedAssetPath = xamplifyUtil.replaceS3WithCloudfrontViceVersa(dto.getAssetPath());
			dto.setAssetPath(updatedAssetPath);
		}
		if (XamplifyUtils.isValidString(dto.getThumbnailPath())) {
			String updatedThumbnailPathPath = xamplifyUtil.replaceS3WithCloudfrontViceVersa(dto.getThumbnailPath());
			dto.setThumbnailPath(updatedThumbnailPathPath);
		}
		if (XamplifyUtils.isValidString(dto.getSharedAssetPath())) {
			String updatedSharedAssetPath = xamplifyUtil.replaceS3WithCloudfrontViceVersa(dto.getSharedAssetPath());
			dto.setSharedAssetPath(updatedSharedAssetPath);
		}
		/*** XNFR-930 ***/
		if (XamplifyUtils.isValidString(dto.getSharedAssetPath())) {
			String encodedUrl = URLEncoder.encode(dto.getSharedAssetPath(), StandardCharsets.UTF_8.name());
			String baseUrl;
			if (xamplifyUtil.isDev()) {
				baseUrl = HOST;
			} else if (xamplifyUtil.isQA()) {
				baseUrl = devHost;
			} else if (xamplifyUtil.isProduction()) {
				baseUrl = productionHost;
			} else {
				baseUrl = HOST;
			}
			String proxyUrl = baseUrl + XAMPLIFY_PRM + "/api/pdf/proxy?pdfUrl=" + encodedUrl;
			dto.setSharedAssetPath(proxyUrl);
		}
		if (XamplifyUtils.isValidString(dto.getAssetPath())
				&& (dto.getAssetType().equals("pdf") || availableImageFileTypes.contains(dto.getAssetType())
						|| availabletextFileTypes.contains(dto.getAssetType()) || dto.getAssetType().equals("csv")
						|| dto.getAssetType().equals("mp3"))) {
			String encodedUrl = URLEncoder.encode(dto.getAssetPath(), StandardCharsets.UTF_8.name());
			String baseUrl;
			if (xamplifyUtil.isDev()) {
				baseUrl = HOST;
			} else if (xamplifyUtil.isQA()) {
				baseUrl = devHost;
			} else if (xamplifyUtil.isProduction()) {
				baseUrl = productionHost;
			} else {
				baseUrl = HOST;
			}
			String proxyUrl = baseUrl + XAMPLIFY_PRM + "/api/pdf/proxy?pdfUrl=" + encodedUrl;
			dto.setAssetPath(proxyUrl);
		}
	}

	@Override
	public XtremandResponse getIsVendorSignatureRequiredAfterPartnerSignatureCompleted(Integer id) {
		XtremandResponse response = new XtremandResponse();
		response.setStatusCode(200);
		response.setData(damDao.isVendorSignatureRequiredAfterPartnerSignatureCompleted(id));
		return response;
	}

	@Override
	public XtremandResponse getIsPartnerSignatureRequiredAndGetPartnerSignatureCount(Integer id) {
		XtremandResponse response = new XtremandResponse();
		boolean isParterSignatureRequired = damDao.isPartnerSignatureRequired(id);
		if (isParterSignatureRequired) {
			response.setData(damDao.getPartnerSignatureCount(id));
			response.setStatusCode(200);
		}
		return response;
	}

	public XtremandResponse getPartnersByDamIdAndCompanyIds(List<Integer> companyIds, Integer damId) {
		XtremandResponse response = new XtremandResponse();
		response.setStatusCode(200);

		if (!XamplifyUtils.isNotEmptyList(companyIds)) {
			response.setData(Collections.emptyList());
			return response;
		}
		boolean isPartnerGroupSelected = damDao.isPublishedToPartnerGroups(damId);
		List<UserDTO> partners = userDao.getUsersByDamIdAndPartnerCompanyIds(damId, companyIds, isPartnerGroupSelected);

		partners.stream().forEach(partner -> {
			partner.setDamPartnerIds(Arrays.asList(partner.getDamPartnerId()));
			partner.setEmailId(partner.getEmailId());
			if ((partner.getFirstName() != null && !partner.getFirstName().isEmpty())
					&& (partner.getLastName() != null && !partner.getLastName().isEmpty())) {
				partner.setFullName(partner.getFirstName() + "  " + partner.getLastName());
			} else if (partner.getFirstName() != null && !partner.getFirstName().isEmpty()) {
				partner.setFullName(partner.getFirstName());
			} else {
				partner.setFullName(partner.getLastName());
			}
		});
		response.setData(partners);
		return response;
	}

	private void processApprovalStatusUpdate(DamUploadPostDTO damUploadPostDTO, Dam dam, Integer companyId) {
		if (XamplifyUtils.isValidInteger(damUploadPostDTO.getLoggedInUserId()) && ((!damUploadPostDTO.isDraft()
				&& (ApprovalStatusType.DRAFT.equals(dam.getApprovalStatus())
						|| ApprovalStatusType.REJECTED.equals(dam.getApprovalStatus())))
				|| (damUploadPostDTO.isDraft() && ApprovalStatusType.REJECTED.equals(dam.getApprovalStatus())))) {
			damUploadPostDTO.setUpdateApprovalStatus(true);
			ApprovalStatusType approvalStatus = determineApprovalStatus(damUploadPostDTO.getLoggedInUserId(), companyId,
					damUploadPostDTO.isDraft());
			damUploadPostDTO.setApprovalStatusInString(approvalStatus.name());
			damUploadPostDTO.setApprovalStatusUpdatedby(damUploadPostDTO.getLoggedInUserId());
			dam.setApprovalStatus(approvalStatus);
			dam.setApprovalStatusUpdatedBy(damUploadPostDTO.getLoggedInUserId());
			dam.setApprovalStatusUpdatedTime(new Date());
		}
	}

	private String replaceAssetLink(String assetName, String body, SharedAssetDetailsViewDTO sharedAssetDetailsViewDTO,
			DamDTO damDto, UserDTO user, UserDetailsUtilDTO userDetails) {
		String finalLinks = assetName;
		String assetNames = sharedAssetDetailsViewDTO.getAssetName();
		Integer damId = sharedAssetDetailsViewDTO.getDamId();

		if (XamplifyUtils.isValidString(assetNames) && damId != null) {
			String vanityURLDomain = utilDao.hasVanityAccessByUserId(damDto.getCreatedBy())
					? xamplifyUtil.frameVanityURL(webUrl, userDetails.getCompanyName())
					: webUrl;
			String encodedDamId = Base64.getUrlEncoder()
					.encodeToString(damId.toString().getBytes(StandardCharsets.UTF_8));
			StringBuilder assetLinks = new StringBuilder();
			String[] assetList = assetNames.split(",");
			for (int i = 0; i < assetList.length; i++) {
				String dam = assetList[i].trim();
				String assetUrl = vanityURLDomain + "home/dam/partner-companies/" + encodedDamId + "/" + dam;
				assetLinks.append("<a href=\"").append(assetUrl).append("\" target=\"_blank\">").append(dam)
						.append("</a>");

				if (i < assetList.length - 1) {
					assetLinks.append(", ");
				}
			}

			finalLinks = assetLinks.toString();
		}
		String mergeTag = "{{assetName}}";
		return body.replace(mergeTag, finalLinks);
	}

	@Override
	public XtremandResponse getPartnerSignatureCountDetails(Integer id) {
		XtremandResponse response = new XtremandResponse();
		response.setData(damDao.getPartnerSignatureCount(id));
		response.setStatusCode(200);
		return response;
	}

	/** XNFR-955 **/
	public XtremandResponse validateSlug(String slug, Integer companyId) {
		XtremandResponse response = new XtremandResponse();
		boolean slugExist = damDao.isAssetExistBySlugAndCompany(slug, companyId);
		response.setData(!slugExist);
		response.setStatusCode(200);
		return response;
	}

	public XtremandResponse getAssetDetailBySlug1(String slug, Integer companyId, Integer loggedInUserId) {
		XtremandResponse response = new XtremandResponse();
		response.setStatusCode(200);
		response.setAccess(true);
		Integer damId = damDao.getDamIdBySlugAndCopmanyId(slug, companyId);
		Map<String, Object> map = new HashMap<>();

		if (damId != null) {
			if (isLoggedinUserHasDamAccess(loggedInUserId)) {
				boolean isDamBelongToLoggedinUserCompany = checkIsDamIdBelongsToLoggedInUserCompany(damId,
						loggedInUserId);
				if (isDamBelongToLoggedinUserCompany) {
					DamListDTO assetDetails = damDao.findAssetDetailsBySlugAndCompanyId(slug, companyId);
					map.put("isVendor", true);
					response.setMap(map);
					if (assetDetails != null) {
						response.setData(assetDetails);
					} else {
						response.setStatusCode(404);
					}
				} else {
					response.setStatusCode(403);
				}
			} else {
				map.put("isVendor", false);
				Integer partnershipId = partnershipDao.getPartnershipIdByPartnerCompanyUserId(loggedInUserId,
						companyId);
				Integer damPartnerId = damDao.getDamPartnerIdByDamIdAndPartnershipId(damId, partnershipId);

				if (damPartnerId != null && isLoggedinPartnerHasDamAccess(loggedInUserId, companyId)) {
					boolean isPublishedToPartnerGroup = damDao.isPublishedToPartnerGroupsByDamPartnerId(damPartnerId);
					boolean isAssetSharedToLoggedInUser = isPublishedToPartnerGroup;
					if (!isPublishedToPartnerGroup) {
						List<Integer> partnerIds = damDao.findPublishedPartnerIdsByDamIdAndPartnershipId(damId,
								partnershipId);
						if (XamplifyUtils.isNotEmptyList(partnerIds)) {
							isAssetSharedToLoggedInUser = partnerIds.stream().anyMatch(id -> id.equals(loggedInUserId));
						}
					}
					if (isAssetSharedToLoggedInUser) {
						map.put("damPartnerId", damPartnerId);
						response = getSharedAssetDetailsById(damPartnerId, loggedInUserId);
						response.setMap(map);
					} else {
						response.setStatusCode(403);
					}
				} else {
					response.setStatusCode(403);
				}
			}
		} else {
			response.setStatusCode(404);
		}
		return response;
	}

	public XtremandResponse getAssetDetailBySlug(String slug, Integer companyId, Integer loggedInUserId) {
		XtremandResponse response = new XtremandResponse();
		response.setStatusCode(200);
		response.setAccess(true);

		Integer damId = damDao.getDamIdBySlugAndCopmanyId(slug, companyId);
		if (damId == null) {
			response.setStatusCode(404);
			return response;
		}
		boolean isDamBelongToLoggedinUserCompany = checkIsDamIdBelongsToLoggedInUserCompany(damId, loggedInUserId);
		if (isDamBelongToLoggedinUserCompany) {
			return handleVendorAccess(slug, companyId, loggedInUserId, response);
		} else {
			return handlePartnerAccess(companyId, loggedInUserId, damId, response);
		}
	}

	private XtremandResponse handleVendorAccess(String slug, Integer companyId, Integer loggedInUserId,
			XtremandResponse response) {
		Map<String, Object> map = new HashMap<>();
		map.put("isVendor", true);
		response.setMap(map);

		if (!isLoggedinUserHasDamAccess(loggedInUserId)) {
			response.setStatusCode(403);
			return response;
		}

		DamListDTO assetDetails = damDao.findAssetDetailsBySlugAndCompanyId(slug, companyId);
		if (assetDetails != null) {
			response.setData(assetDetails);
		} else {
			response.setStatusCode(404);
		}

		return response;
	}

	private XtremandResponse handlePartnerAccess(Integer companyId, Integer loggedInUserId, Integer damId,
			XtremandResponse response) {
		Map<String, Object> map = new HashMap<>();
		map.put("isVendor", false);
		response.setMap(map);

		Integer partnershipId = partnershipDao.getPartnershipIdByPartnerCompanyUserId(loggedInUserId, companyId);
		Integer damPartnerId = damDao.getDamPartnerIdByDamIdAndPartnershipId(damId, partnershipId);

		if (damPartnerId == null || !isLoggedinPartnerHasDamAccess(loggedInUserId, companyId)
				|| !isAssetSharedWithUser(damId, damPartnerId, partnershipId, loggedInUserId)) {
			response.setStatusCode(403);
			return response;
		}

		map.put("damPartnerId", damPartnerId);
		XtremandResponse sharedResponse = getSharedAssetDetailsById(damPartnerId, loggedInUserId);
		sharedResponse.setMap(map);
		return sharedResponse;
	}

	private boolean isAssetSharedWithUser(Integer damId, Integer damPartnerId, Integer partnershipId, Integer userId) {
		if (damDao.isPublishedToPartnerGroupsByDamPartnerId(damPartnerId)) {
			return true;
		}

		List<Integer> partnerIds = damDao.findPublishedPartnerIdsByDamIdAndPartnershipId(damId, partnershipId);
		return XamplifyUtils.isNotEmptyList(partnerIds) && partnerIds.contains(userId);
	}

	private boolean isLoggedinUserHasDamAccess(Integer loggedInUserId) {
		boolean isDamModuleEnabledBySuperAdmin = utilDao.hasDamAccessByUserId(loggedInUserId);
		List<Integer> roleIds = userDao.getRoleIdsByUserId(loggedInUserId);
		boolean isAnyVendorAdmin = Role.isAnyVendorCompanyAdmin(roleIds);
		boolean damAccess;
		boolean isDamRole = roleIds.contains(Role.DAM.getRoleId());

		damAccess = isAnyVendorAdmin || isDamRole;

		return isDamModuleEnabledBySuperAdmin && damAccess;
	}

	private boolean isLoggedinPartnerHasDamAccess(Integer loggedInUserId, Integer vendorCompanyId) {
		boolean isDamModuleEnabledBySuperAdmin = utilDao.hasDamAccessByCompanyId(vendorCompanyId);
		List<Integer> roleIds = userDao.getRoleIdsByUserId(loggedInUserId);
		boolean isPartnerAdmin = Role.isPartnerAdmin(roleIds);
		boolean isDamRole = roleIds.contains(Role.DAM.getRoleId());
		boolean isAssetSharedToPartner = damDao.isAssetSharedToPartnerCompanyByPartnerIdAndVendorCompany(loggedInUserId,
				vendorCompanyId);
		boolean damAccessAsPartner = isPartnerAdmin || isDamRole || isAssetSharedToPartner;
		return isDamModuleEnabledBySuperAdmin && damAccessAsPartner;
	}

	@Override
	public XtremandResponse damDetailsByDamId(Integer contentId, String contentType) {
		XtremandResponse response = new XtremandResponse();

		if ("PLAYBOOK".equalsIgnoreCase(contentType)) {
			response.setData(damDao.playbookDetailsByPlaybookId(contentId));
		} else if ("TRACK".equalsIgnoreCase(contentType)) {
			response.setData(damDao.trackDetailsByTrackId(contentId));
		} else {
			response.setData(damDao.damDetailsByDamId(contentId));
		}
		response.setStatusCode(200);
		return response;
	}

}
