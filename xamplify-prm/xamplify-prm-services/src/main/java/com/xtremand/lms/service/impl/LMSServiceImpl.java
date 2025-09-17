package com.xtremand.lms.service.impl;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;

import com.xtremand.approve.dao.ApproveDAO;
import com.xtremand.aws.AmazonWebService;
import com.xtremand.category.bom.Category;
import com.xtremand.category.bom.CategoryModule;
import com.xtremand.category.bom.CategoryModuleEnum;
import com.xtremand.category.dao.CategoryDao;
import com.xtremand.category.dto.CategoryDTO;
import com.xtremand.comments.dao.CommentDao;
import com.xtremand.common.bom.CompanyProfile;
import com.xtremand.common.bom.Criteria;
import com.xtremand.common.bom.Criteria.OPERATION_NAME;
import com.xtremand.common.bom.FindLevel;
import com.xtremand.common.bom.Pagination;
import com.xtremand.company.dao.CompanyProfileDao;
import com.xtremand.content.service.ContentService;
import com.xtremand.dam.bom.ApprovalStatusType;
import com.xtremand.dam.bom.Dam;
import com.xtremand.dam.dao.DamDao;
import com.xtremand.dam.dto.DamDownloadDTO;
import com.xtremand.dam.dto.DamListDTO;
import com.xtremand.dam.dto.PublishedContentIdAndUserListIdDetailsDTO;
import com.xtremand.dao.util.GenericDAO;
import com.xtremand.form.bom.Form;
import com.xtremand.form.dao.FormDao;
import com.xtremand.form.dto.FormDTO;
import com.xtremand.form.submit.bom.FormSubmit;
import com.xtremand.form.submit.dao.FormSubmitDao;
import com.xtremand.form.submit.dto.FormSubmitAnswerDTO;
import com.xtremand.form.submit.service.FormSubmitService;
import com.xtremand.formbeans.UserDTO;
import com.xtremand.formbeans.XtremandResponse;
import com.xtremand.lms.bom.LearningTrack;
import com.xtremand.lms.bom.LearningTrackContent;
import com.xtremand.lms.bom.LearningTrackPartnerActivity;
import com.xtremand.lms.bom.LearningTrackTag;
import com.xtremand.lms.bom.LearningTrackType;
import com.xtremand.lms.bom.LearningTrackVisibility;
import com.xtremand.lms.bom.LearningTrackVisibilityGroup;
import com.xtremand.lms.bom.PartnerActivityType;
import com.xtremand.lms.dao.LMSDAO;
import com.xtremand.lms.dto.LearningTrackContentDto;
import com.xtremand.lms.dto.LearningTrackContentResponseDTO;
import com.xtremand.lms.dto.LearningTrackDto;
import com.xtremand.lms.dto.LearningTrackPartnerActivityDto;
import com.xtremand.lms.dto.PlaybookAssetResponseDTO;
import com.xtremand.lms.dto.PlaybookContentCategoryListDTO;
import com.xtremand.lms.dto.PreviewPlaybookResponseDTO;
import com.xtremand.lms.dto.ShareLearningTrackResponseDTO;
import com.xtremand.lms.service.LMSService;
import com.xtremand.mail.service.AsyncComponent;
import com.xtremand.partnership.bom.Partnership;
import com.xtremand.partnership.bom.PartnershipDTO;
import com.xtremand.partnership.dao.PartnershipDAO;
import com.xtremand.tag.bom.Tag;
import com.xtremand.tag.dto.TagDTO;
import com.xtremand.team.dao.TeamDao;
import com.xtremand.user.bom.Role;
import com.xtremand.user.bom.User;
import com.xtremand.user.bom.UserList;
import com.xtremand.user.dao.UserDAO;
import com.xtremand.user.service.UserService;
import com.xtremand.userlist.dao.UserListDAO;
import com.xtremand.util.DateUtils;
import com.xtremand.util.FileUtil;
import com.xtremand.util.PaginationUtil;
import com.xtremand.util.ResponseUtilException;
import com.xtremand.util.XamplifyUtil;
import com.xtremand.util.XamplifyUtils;
import com.xtremand.util.bom.ModuleType;
import com.xtremand.util.dao.HibernateSQLQueryResultUtilDao;
import com.xtremand.util.dao.UtilDao;
import com.xtremand.util.dto.CompanyDTO;
import com.xtremand.util.dto.HibernateSQLQueryResultRequestDTO;
import com.xtremand.util.dto.Pageable;
import com.xtremand.util.dto.ShareContentRequestDTO;
import com.xtremand.util.dto.UserListAndUserId;
import com.xtremand.util.service.UtilService;
import com.xtremand.vanity.url.dto.VanityUrlDetailsDTO;
import com.xtremand.video.bom.VideoFile;

@Service("LmsService")
@Transactional
public class LMSServiceImpl implements LMSService {
	private static final Logger logger = LoggerFactory.getLogger(LMSServiceImpl.class);
	private static final String UNAUTHORIZED = "UnAuthorized";
	private static final String SUCCESS = "Success";
	private static final String FAILED = "Failed";
	private static final String INVALID_INPUT = "Invalid Input";
	private static final String PARTNER = "Partner";
	private static final String RECORD_NOT_FOUND = "Record Not Found";

	private static final Set<String> FINISHED_STATUS_LIST = new HashSet<>(
			Arrays.asList("VIEWED", "DOWNLOADED", "SUBMITTED"));

	private static final String HOST = "http://localhost:8080/";
	private static final String XAMPLIFY_PRM = "xamplify-prm-api";

	@Value("${dev.host}")
	String devHost;

	@Value("${prod.host}")
	String productionHost;

	@Value("${image.file.types}")
	String imageFileTypes;

	@Value("${content.preview.fileTypes}")
	String contentPreviewSupportedFileFormats;

	@Value("${content.preview.fileTypes.text}")
	String contentPreviewForTextView;

	@Autowired
	private UserService userService;

	@Autowired
	private UserDAO userDao;

	@Autowired
	private GenericDAO genericDAO;

	@Autowired
	private LMSDAO lmsDAO;

	@Autowired
	private UtilService utilService;

	@Autowired
	private PartnershipDAO partnershipDAO;

	@Autowired
	private CategoryDao categoryDao;

	@Autowired
	AmazonWebService amazonWebService;

	@Autowired
	private FormDao formDao;

	@Autowired
	private DamDao damDao;

	@Autowired
	@Lazy
	private AsyncComponent asyncComponent;

	@Autowired
	private FormSubmitDao formSubmitDao;

	@Autowired
	private TeamDao teamDao;

	@Autowired
	private FormSubmitService formSubmitService;

	@Value("${server_path}")
	String serverPath;

	@Autowired
	private PaginationUtil paginationUtil;

	@Autowired
	private UtilDao utilDao;

	@Autowired
	private UserListDAO userListDao;

	@Autowired
	private HibernateSQLQueryResultUtilDao hibernateSQLQueryResultUtilDao;

	@Autowired
	private CompanyProfileDao companyProfileDao;

	@Autowired
	private ContentService contentService;

	@Autowired
	private CommentDao commentDao;

	@Autowired
	private ApproveDAO approveDao;

	@Autowired
	private XamplifyUtil xamplifyUtil;

	@Autowired
	private FileUtil fileUtil;

	@Override
	public XtremandResponse save(MultipartFile featuredImage, LearningTrackDto learningTrackDto) {
		XtremandResponse response = new XtremandResponse();
		String responseMessage = FAILED;
		Integer responseStatusCode = 400;
		if (validateSaveLearningTrackRequest(learningTrackDto)) {
			Integer loggedInUserId = learningTrackDto.getUserId();
			CompanyProfile loggedInCompany = userService.getCompanyProfileByUser(loggedInUserId);
			if (loggedInCompany != null && canCreateLearningTrack(loggedInUserId)) {
				if (learningTrackDto.getType() == null) {
					learningTrackDto.setType(LearningTrackType.TRACK);
				}
				LearningTrack exLearningTrack = lmsDAO.getLearningTrackByTitle(learningTrackDto.getTitle(),
						loggedInCompany.getId(), learningTrackDto.getType());
				if (exLearningTrack == null) {
					exLearningTrack = lmsDAO.getLearningTrackBySlug(learningTrackDto.getSlug(), loggedInCompany.getId(),
							learningTrackDto.getType());
					if (exLearningTrack == null) {
						LearningTrack learningTrack = createLearningTrack(learningTrackDto, loggedInCompany);
						String filePath = uploadFeaturedImage(featuredImage, learningTrack.getId(),
								loggedInCompany.getId());
						if (!StringUtils.isBlank(filePath)) {
							learningTrack.setFeaturedImage(filePath);
						}
						response.setData(getLearningTrackDto(learningTrack, loggedInUserId));

						responseMessage = SUCCESS;
						responseStatusCode = 200;
					} else {
						responseMessage = "Duplicate Entry for Slug ";
						responseStatusCode = 409;
					}
				} else {
					responseMessage = "Duplicate Entry for Title ";
					responseStatusCode = 409;
				}
			} else {
				responseMessage = UNAUTHORIZED;
				responseStatusCode = 401;
			}
		} else {
			responseMessage = INVALID_INPUT;
			responseStatusCode = 500;
		}
		response.setMessage(responseMessage);
		response.setStatusCode(responseStatusCode);
		return response;
	}

	private String uploadFeaturedImage(MultipartFile featuredImage, Integer learningTrackId, Integer companyId) {
		String filePath = "";
		if (featuredImage != null) {
			File tempFile;
			try {
				tempFile = File.createTempFile("lms_img_temp", null);
				FileUtils.copyInputStreamToFile(featuredImage.getInputStream(), tempFile);
				String fileName = featuredImage.getOriginalFilename();
				String extension = fileName.substring(fileName.lastIndexOf('.') + 1);
				filePath = amazonWebService.uploadLMSFeaturedImage(tempFile.getAbsolutePath(), companyId,
						learningTrackId + "." + extension);
			} catch (IOException e) {
				logger.error("Error while uploading featured image:" + e.getMessage());
			}
		}
		return filePath;
	}

	private LearningTrack createLearningTrack(LearningTrackDto learningTrackDto, CompanyProfile loggedInCompany) {
		LearningTrack learningTrack = new LearningTrack();
		learningTrack.setTitle(XamplifyUtils.removeExtraSpace(learningTrackDto.getTitle()));
		learningTrack.setDescription(utilService.replacedDescription(learningTrackDto.getDescription()));
		learningTrack.setCompany(loggedInCompany);
		learningTrack.setSlug(learningTrackDto.getSlug());
		learningTrack.setPublished(learningTrackDto.isPublished());
		if (learningTrackDto.isPublished()) {
			learningTrack.setPublishedTime(new Date());
		}

		learningTrack.setType(learningTrackDto.getType());
		if (LearningTrackType.TRACK == learningTrackDto.getType()) {
			learningTrack.setFollowAssetSequence(learningTrackDto.isFollowAssetSequence());
		} else {
			learningTrack.setFollowAssetSequence(false);
		}

		learningTrack.setCreatedBy(learningTrackDto.getUserId());
		learningTrack.initialiseCommonFields(true, learningTrackDto.getUserId());
		/**** XNFR-327 *****/
		learningTrack.setPublishingOrWhiteLabelingInProgress(true);
		/**** XNFR-327 *****/
		learningTrack.setAddedToQuickLinks(learningTrackDto.isAddedToQuickLinks());
		learningTrack.setGroupByAssets(learningTrackDto.isGroupByAssets());
		/**** XNFR-897 ****/
		learningTrack.setExpireDate(convertStringToDate(learningTrackDto.getExpireDate()));
		/** XNFR-824 **/
		setLearningTrackApprovalStatus(learningTrackDto.getUserId(), loggedInCompany.getId(), learningTrack);

		genericDAO.save(learningTrack);
		saveContent(learningTrackDto, learningTrack);
		saveTags(learningTrackDto, learningTrack);
		saveCategory(learningTrackDto, learningTrack);
		/**** XNFR-327 *****/
		learningTrackDto.setId(learningTrack.getId());

		/** XNFR-824 **/
		ModuleType moduleType = LearningTrackType.TRACK.equals(learningTrackDto.getType()) ? ModuleType.TRACK
				: ModuleType.PLAYBOOK;
		commentDao.createApprovalStatusHistory(learningTrack.getId(), learningTrackDto.getUserId(), moduleType);

		return learningTrack;
	}

	private Date convertStringToDate(String dateString) {
		if (!XamplifyUtils.isValidString(dateString)) {
			return null; // Return null or handle it as needed
		}
		SimpleDateFormat inputFormat = new SimpleDateFormat("MM/dd/yyyy h:mm a");
		Date date = null;
		try {
			date = inputFormat.parse(dateString);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return date;
	}

	private void saveCategory(LearningTrackDto learningTrackDto, LearningTrack learningTrack) {
		saveCategory(learningTrackDto.getCategoryId(), learningTrackDto.getUserId(), learningTrack,
				learningTrackDto.getType());
	}

	private void saveCategory(Integer categoryId, Integer loggedInUserId, LearningTrack learningTrack,
			LearningTrackType learningTrackType) {
		if (categoryId != null && categoryId > 0) {
			Category category = genericDAO.get(Category.class, categoryId);
			if (category != null) {
				CategoryModule categoryModule = new CategoryModule();
				categoryModule.setCategoryId(category.getId());
				categoryModule.setCompanyId(category.getCompanyId());
				categoryModule.setLearningTrack(learningTrack);
				if (LearningTrackType.TRACK.equals(learningTrackType)) {
					categoryModule.setCategoryModuleEnum(CategoryModuleEnum.LEARNING_TRACK);
				} else {
					categoryModule.setCategoryModuleEnum(CategoryModuleEnum.PLAY_BOOK);
				}
				categoryModule.setCreatedUserId(loggedInUserId);
				categoryModule.setUpdatedUserId(loggedInUserId);
				categoryModule.setCreatedTime(new Date());
				categoryModule.setUpdatedTime(new Date());
				genericDAO.save(categoryModule);
			}
		}
	}

	private void saveTags(LearningTrackDto learningTrackDto, LearningTrack learningTrack) {
		if (learningTrackDto.getTagIds() != null && !learningTrackDto.getTagIds().isEmpty()) {
			for (Integer tagId : learningTrackDto.getTagIds()) {
				if (tagId != null && tagId > 0) {
					saveTag(learningTrackDto.getUserId(), learningTrack, tagId);
				}
			}
		}
	}

	private void saveTag(Integer loggedInUserId, LearningTrack learningTrack, Integer tagId) {
		Tag tag = genericDAO.get(Tag.class, tagId);
		saveTag(loggedInUserId, learningTrack, tag);
	}

	private void saveTag(Integer loggedInUserId, LearningTrack learningTrack, Tag tag) {
		if (tag != null) {
			LearningTrackTag ltTag = new LearningTrackTag();
			ltTag.setLearningTrack(learningTrack);
			ltTag.setTag(tag);
			ltTag.setCreatedBy(loggedInUserId);
			ltTag.initialiseCommonFields(true, loggedInUserId);
			genericDAO.save(ltTag);
		}
	}

	private void saveVisibilityGroup(UserList userList, LearningTrackVisibility visibilityCompany, Integer userId) {
		LearningTrackVisibilityGroup visibilityGroup = new LearningTrackVisibilityGroup();
		visibilityGroup.setUserList(userList);
		visibilityGroup.setLearningTrackVisibility(visibilityCompany);
		visibilityGroup.setCreatedBy(userId);
		visibilityGroup.initialiseCommonFields(true, userId);
		genericDAO.save(visibilityGroup);
	}

	private LearningTrackVisibility saveVisibilityUser(Integer loggedInUserId, Partnership partnerShip,
			LearningTrack learningTrack, User user, UserList userList, boolean isAssociatedThroughCompany) {
		LearningTrackVisibility visibilityUser = null;
		if (user != null && partnerShip != null && learningTrack != null) {
			visibilityUser = new LearningTrackVisibility();
			visibilityUser.setPartnership(partnerShip);
			visibilityUser.setLearningTrack(learningTrack);
			visibilityUser.setUser(user);
			if (user.getTeamMembers() != null && !user.getTeamMembers().isEmpty()) {
				visibilityUser.setTeamMember(user.getTeamMembers().get(0));
			}
			visibilityUser.setAssociatedThroughCompany(isAssociatedThroughCompany);
			visibilityUser.setCreatedBy(loggedInUserId);
			visibilityUser.initialiseCommonFields(true, loggedInUserId);
			genericDAO.save(visibilityUser);

			if (userList != null) {
				saveVisibilityGroup(userList, visibilityUser, loggedInUserId);
			}
		}
		return visibilityUser;
	}

	private void saveContent(LearningTrackDto learningTrackDto, LearningTrack learningTrack) {
		/*
		 * if (learningTrackDto.getContentIds() != null &&
		 * !learningTrackDto.getContentIds().isEmpty()) { int index = 1; for (Integer
		 * contentId : learningTrackDto.getContentIds()) { if (contentId != null &&
		 * contentId > 0) { saveContent(learningTrack, contentId,
		 * learningTrackDto.getUserId(), index++); } } }
		 */

		if (learningTrackDto.getContentAndQuizData() != null && !learningTrackDto.getContentAndQuizData().isEmpty()) {
			int index = 1;
			for (Entry<Integer, LearningTrackContentDto> entry : learningTrackDto.getContentAndQuizData().entrySet()) {
				LearningTrackContentDto learningTrackContentDto = entry.getValue();
				if (learningTrackContentDto != null && learningTrackContentDto.getId() != null
						&& learningTrackContentDto.getId() > 0) {
					saveContent(learningTrack, learningTrackContentDto.getId(), learningTrackDto.getUserId(), index++,
							learningTrackContentDto.isTypeQuizId());
				}
			}
		}
	}

	private void saveContent(LearningTrack learningTrack, Integer contentId, Integer loggedInUserId, int displayIndex,
			Boolean isQuizId) {
		Dam dam = null;
		Form form = null;
		if (Boolean.TRUE.equals(isQuizId)) {
			form = formDao.getById(contentId);
		} else {
			dam = damDao.getById(contentId);
		}
		saveContent(learningTrack, dam, form, loggedInUserId, displayIndex);
	}

	private void saveContent(LearningTrack learningTrack, Dam dam, Form form, Integer loggedInUserId,
			int displayIndex) {
		if (dam != null || form != null) {
			LearningTrackContent content = new LearningTrackContent();
			if (dam != null && form == null) {
				content.setDam(dam);
			} else if (form != null && dam == null) {
				content.setQuiz(form);
			}
			content.setLearningTrack(learningTrack);
			content.setDisplayIndex(displayIndex);
			content.setCreatedBy(loggedInUserId);
			content.initialiseCommonFields(true, loggedInUserId);
			genericDAO.save(content);
			learningTrack.getContents().add(content);
		}
	}

	private boolean canCreateLearningTrack(Integer userId) {
		boolean isTeamMember = teamDao.isTeamMember(userId);
		if (isTeamMember) {
			Integer superiorId = teamDao.getOrgAdminIdByTeamMemberId(userId);
			List<Integer> roles = userDao.getRoleIdsByUserId(superiorId);
			return hasLearningTrackAccess(roles);
		} else {
			List<Integer> roles = userDao.getRoleIdsByUserId(userId);
			return hasLearningTrackAccess(roles);
		}
	}

	private boolean hasLearningTrackAccess(List<Integer> roles) {
		return roles.contains(Role.PRM_ROLE.getRoleId());
	}

	private boolean validateSaveLearningTrackRequest(LearningTrackDto learningTrackDto) {
		boolean valid = false;
		if (learningTrackDto != null && !StringUtils.isBlank(learningTrackDto.getTitle())
				&& !StringUtils.isBlank(learningTrackDto.getDescription())
				&& !StringUtils.isBlank(learningTrackDto.getSlug()) && learningTrackDto.getUserId() != null
				&& learningTrackDto.getUserId() > 0 && learningTrackDto.getContentAndQuizData() != null
				&& learningTrackDto.getCategoryId() != null && learningTrackDto.getCategoryId() > 0
				&& !learningTrackDto.getContentAndQuizData().isEmpty()
				&& ((learningTrackDto.getUserIds() != null && !learningTrackDto.getUserIds().isEmpty())
						|| (learningTrackDto.getGroupIds() != null && !learningTrackDto.getGroupIds().isEmpty()))) {
			valid = true;
		}
		return valid;
	}

	@SuppressWarnings("unchecked")
	@Override
	public XtremandResponse getLearningTracksForVendor(Pagination pagination) {
		XtremandResponse response = new XtremandResponse();
		String responseMessage = FAILED;
		Integer responseStatusCode = 400;
		Map<String, Object> resultMap = new HashMap<>();
		if (pagination != null && pagination.getUserId() != null && pagination.getUserId() > 0) {
			Integer companyId = userService.getCompanyIdByUserId(pagination.getUserId());
			if (companyId != null) {
				pagination.setCompanyId(companyId);
				String companyType = utilService.getCompanyType(companyId);
				if (companyType != null && !companyType.equals(PARTNER)) {
					if (pagination.getLmsType() == null) {
						pagination.setLmsType(LearningTrackType.TRACK);
					}
					resultMap = lmsDAO.getLearningTracksForVendor(pagination);
					if (resultMap != null && !resultMap.isEmpty() && resultMap.get("data") != null) {
						List<LearningTrack> learningTracks = (List<LearningTrack>) resultMap.get("data");
						/***** XNFR-930 ***/
						learningTracks.forEach(item -> {
							if (XamplifyUtils.isValidString(item.getFeaturedImage())) {
								String updatedFeaturedImagePath = xamplifyUtil
										.replaceS3WithCloudfrontViceVersa(item.getFeaturedImage());
								item.setCdnFeaturedImage(updatedFeaturedImagePath);
							}
						});
						/***** XNFR-930 ***/
						resultMap.put("data", getlearningTracksDtoList(learningTracks, pagination.getUserId(),
								pagination.getLmsType()));
						response.setData(resultMap);
					}
					responseMessage = SUCCESS;
					responseStatusCode = 200;
				} else {
					responseMessage = UNAUTHORIZED;
					responseStatusCode = 403;
				}
			} else {
				responseMessage = UNAUTHORIZED;
				responseStatusCode = 401;
			}
		} else {
			responseMessage = INVALID_INPUT;
			responseStatusCode = 500;
		}
		response.setMessage(responseMessage);
		response.setStatusCode(responseStatusCode);
		return response;
	}

	private List<LearningTrackDto> getlearningTracksDtoList(List<LearningTrack> learningTracks, Integer loggedInUserId,
			LearningTrackType learningTrackType) {
		List<LearningTrackDto> learningTrackDtoList = new ArrayList<>();
		if (learningTracks != null && !learningTracks.isEmpty()) {
			/** XNFR-821 **/
			String moduleType = learningTrackType == LearningTrackType.TRACK ? ModuleType.TRACK.name()
					: ModuleType.PLAYBOOK.name();
			Integer companyId = userDao.getCompanyIdByUserId(loggedInUserId);
			List<Integer> listAllApprovers = approveDao.findAllApproversByModuleTypeAndCompanyId(companyId, moduleType);

			for (LearningTrack learningTrack : learningTracks) {
				LearningTrackDto learningTrackDto = getLearningTrackDto(learningTrack, loggedInUserId);
				if (learningTrackDto != null) {
					if (listAllApprovers.contains(learningTrackDto.getCreatedById())) {
						learningTrackDto.setCreatedByAnyApprovalManagerOrApprover(true);
					}
					learningTrackDtoList.add(learningTrackDto);
				}
			}
		}
		return learningTrackDtoList;
	}

	private LearningTrackDto getLearningTrackDto(LearningTrack learningTrack, Integer loggedInUserId) {
		LearningTrackDto learningTrackDto = null;
		if (learningTrack != null) {
			learningTrackDto = new LearningTrackDto();
			learningTrackDto.setId(learningTrack.getId());
			learningTrackDto.setDescription(utilService.replacedDescription(learningTrack.getDescription()));
			learningTrackDto.setFeaturedImage(learningTrack.getFeaturedImage());
			learningTrackDto.setPublished(learningTrack.isPublished());
			learningTrackDto.setCreatedByCompanyId(learningTrack.getCompany().getId());
			learningTrackDto.setSlug(learningTrack.getSlug());
			learningTrackDto.setTitle(XamplifyUtils.removeExtraSpace(learningTrack.getTitle()));
			Boolean canUpdate = canUpdateLearningTrack(learningTrack, loggedInUserId);
			learningTrackDto.setCanUpdate(canUpdate);
			learningTrackDto.setCanDelete(canUpdate);
			learningTrackDto.setCdnFeaturedImage(learningTrack.getCdnFeaturedImage());
			Boolean canPublish = false;
			if (Boolean.TRUE.equals(canUpdate && learningTrack.getVisibilityUsers() != null)
					&& !learningTrack.getVisibilityUsers().isEmpty()) {
				canPublish = true;
			}
			learningTrackDto.setCanPublish(canPublish);
			learningTrackDto.setFollowAssetSequence(learningTrack.isFollowAssetSequence());
			learningTrackDto.setCreatedTime(DateUtils.getUtcString(learningTrack.getCreatedTime()));
			User createdUser = userService.loadUser(
					Arrays.asList(new Criteria("userId", OPERATION_NAME.eq, learningTrack.getCreatedBy())),
					new FindLevel[] { FindLevel.COMPANY_PROFILE });
			learningTrackDto.setCreatedByName(getCustomerFullName(createdUser));

			setCategory(learningTrack, learningTrackDto);
			if (LearningTrackType.TRACK == learningTrack.getType()
					|| LearningTrackType.PLAYBOOK == learningTrack.getType()) {
				LearningTrackVisibility visibilityUser = lmsDAO.getVisibilityUser(loggedInUserId,
						learningTrackDto.getId());
				if (visibilityUser != null) {
					learningTrackDto.setProgress(visibilityUser.getProgress());
				}
			}

			if (learningTrack.getQuiz() != null) {
				FormSubmit formSubmit = formDao.getLearningTrackFormSubmission(learningTrack.getId(), loggedInUserId);
				if (formSubmit != null) {
					learningTrackDto.setScore(formSubmit.getScore());
					learningTrackDto.setMaxScore(formSubmit.getForm().getMaxScore());
				}
			}
			setTags(learningTrack, learningTrackDto);

			if (LearningTrackType.PLAYBOOK == learningTrack.getType()) {
				Integer damCount = lmsDAO.getDameCountByLearningId(learningTrack.getId());
				if (damCount <= 0) {
					learningTrackDto.setHasDamContent(false);
				}
			}
			/**** XNFR-327 ****/
			learningTrackDto
					.setPublishingOrWhiteLabelingInProgress(learningTrack.isPublishingOrWhiteLabelingInProgress());
			learningTrackDto.setPublishingToPartnerList(learningTrack.isPublishingToPartnerList());
			/**** XNFR-327 ****/
			/*** XNFR-523 ****/
			learningTrackDto.setTrackUpdatedEmailNotification(learningTrack.isTrackUpdatedEmailNotification());
			learningTrackDto.setAddedToQuickLinks(learningTrack.isAddedToQuickLinks());
			learningTrackDto.setGroupByAssets(learningTrack.isGroupByAssets());

			/** XNFR-824 **/
			learningTrackDto.setCreatedById(learningTrack.getCreatedBy());
			learningTrackDto.setApprovalStatus(learningTrack.getApprovalStatus().name());
			learningTrackDto.setApprovalStatusUpdatedBy(learningTrack.getApprovalStatusUpdatedBy());
			learningTrackDto.setApprovalStatusUpdatedTimeInString(learningTrack.getApprovalStatusUpdatedTime());
			/*** XNFR-897 **/
			if (learningTrack.getExpireDate() != null) {
				Date expireDate = learningTrack.getExpireDate(); // Assuming learningTrack is available
				Instant instant = expireDate.toInstant();
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy h:mm a")
						.withZone(ZoneId.systemDefault());
				String formattedDate = formatter.format(instant);
				learningTrackDto.setExpireDate(formattedDate);
			}
		}

		return learningTrackDto;
	}

	private LearningTrackDto getLearningTrackDtoInDetail(LearningTrack learningTrack, Integer loggedInUserId,
			boolean isPreview) {
		LearningTrackDto learningTrackDto = getLearningTrackDto(learningTrack, loggedInUserId);
		if (learningTrackDto != null) {
			setContents(learningTrack, learningTrackDto, loggedInUserId);
			setVisibility(learningTrack, learningTrackDto);
		}
		return learningTrackDto;
	}

	/*
	 * private void setQuiz(LearningTrack learningTrack, LearningTrackDto
	 * learningTrackDto) { if (learningTrack.getQuiz() != null) { FormDTO quiz = new
	 * FormDTO(); BeanUtils.copyProperties(learningTrack.getQuiz(), quiz);
	 * quiz.setName(learningTrack.getQuiz().getFormName());
	 * learningTrackDto.setQuiz(quiz); } }
	 */

	private void setCategory(LearningTrack learningTrack, LearningTrackDto learningTrackDto) {
		CategoryModule categoryModule = categoryDao.getCategoryModuleByLearningTrackId(learningTrack.getId());
		if (categoryModule != null && categoryModule.getCategoryId() != null) {
			Category category = genericDAO.get(Category.class, categoryModule.getCategoryId());
			if (category != null) {
				CategoryDTO categoryDTO = new CategoryDTO();
				BeanUtils.copyProperties(category, categoryDTO);
				learningTrackDto.setCategory(categoryDTO);
			}
		}
	}

	private void setTags(LearningTrack learningTrack, LearningTrackDto learningTrackDto) {
		List<TagDTO> tagList = new ArrayList<>();
		List<String> tagNames = new ArrayList<>();
		if (learningTrack.getTags() != null && !learningTrack.getTags().isEmpty()) {
			for (LearningTrackTag tag : learningTrack.getTags()) {
				if (tag != null && tag.getTag() != null) {
					TagDTO tagDto = new TagDTO();
					BeanUtils.copyProperties(tag.getTag(), tagDto);
					tagList.add(tagDto);
					tagNames.add(tagDto.getTagName());
				}
			}
		}
		learningTrackDto.setTags(tagList);
		learningTrackDto.setTagNames(tagNames);
	}

	private void setVisibility(LearningTrack learningTrack, LearningTrackDto learningTrackDto) {
		Set<Integer> visibilityUserIds = new HashSet<>();
		Set<Integer> partnerShipIds = new HashSet<>();
		Set<Integer> groupIds = new HashSet<>();

		List<Object[]> rows = lmsDAO.getVisibilityByLearningTrackId(learningTrack.getId());
		for (Object[] row : rows) {
			if (row[0] != null)
				visibilityUserIds.add(((Number) row[0]).intValue());
			if (row[1] != null)
				partnerShipIds.add(((Number) row[1]).intValue());
			if (row[2] != null)
				groupIds.add(((Number) row[2]).intValue());
		}

		learningTrackDto.setUserIds(visibilityUserIds);
		learningTrackDto.setPartnershipIds(partnerShipIds);
		learningTrackDto.setGroupIds(groupIds);
	}

	private String getCustomerFullName(User user) {
		return utilService.getFullName(user);
	}

	private void setContents(LearningTrack learningTrack, LearningTrackDto learningTrackDto, Integer loggedInUserId) {
		boolean enableQuiz = true;
		List<LearningTrackContentDto> contentList = new ArrayList<>();
		List<LearningTrackContent> contents = lmsDAO.getContentsInOrder(learningTrack.getId());
		if (contents != null && !contents.isEmpty()) {
			for (LearningTrackContent content : contents) {
				if (content != null) {
					LearningTrackContentDto contentDto = new LearningTrackContentDto();
					if (content.getDam() != null) {
						DamListDTO damDto = new DamListDTO();
						BeanUtils.copyProperties(content.getDam(), damDto);
						VideoFile videoFile = content.getDam().getVideoFile();
						if (videoFile != null) {
							damDto.setThumbnailPath(serverPath + videoFile.getImageUri());
							damDto.setAssetPath(serverPath + videoFile.getUri());
							damDto.setVideoId(videoFile.getId());
							damDto.setDescription(videoFile.getDescription());
						}
						String categoryName = lmsDAO.getCategoryNameByDamId(content.getDam(), content.getQuiz());
						if (XamplifyUtils.isValidString(categoryName)) {
							damDto.setCategoryName(categoryName);
						}
						contentDto.setOpened(false);
						if (LearningTrackType.TRACK == learningTrack.getType()
								|| LearningTrackType.PLAYBOOK == learningTrack.getType()) {
							List<LearningTrackPartnerActivity> partnerActivities = lmsDAO
									.getPartnerActivity(content.getId(), loggedInUserId);
							if (partnerActivities != null && !partnerActivities.isEmpty()) {
								for (LearningTrackPartnerActivity partnerActivity : partnerActivities) {
									if (partnerActivity != null) {
										if (FINISHED_STATUS_LIST.contains(partnerActivity.getType().name())) {
											contentDto.setFinished(true);
											break;
										} else if ("OPENED".equals(partnerActivity.getType().name())) {
											contentDto.setOpened(true);
										}
									}
								}
								enableQuiz = enableQuiz && contentDto.isFinished();
							} else {
								enableQuiz = false;
							}
						} else {
							enableQuiz = false;
						}
						/**** XNFR-501 ***/
						damDto.setLearningTrackContentMappingId(content.getId());
						damDto.setDisplayTime(content.getDam().getCreatedTime());
						UserDTO damCreatedByName = userDao.getEmailIdAndDisplayName(content.getDam().getCreatedBy());
						damDto.setFullName(damCreatedByName.getFullNameOrEmailId());
						damDto.setCategoryName(damDto.getCategoryName());
						contentDto.setDam(damDto);
						contentDto.setId(damDto.getId());
						contentDto.setTypeQuizId(false);
						addProxyToPDFAssetPath(damDto);
					} else if (content.getQuiz() != null) {
						FormDTO quiz = new FormDTO();
						BeanUtils.copyProperties(content.getQuiz(), quiz);
						quiz.setName(content.getQuiz().getFormName());
						List<LearningTrackPartnerActivity> partnerActivities = lmsDAO
								.getPartnerActivity(content.getId(), loggedInUserId);
						if (partnerActivities != null && !partnerActivities.isEmpty() && partnerActivities.size() > 0) {
							FormSubmit formSubmit = formDao.getLearningTrackFormSubmissionByFormID(
									learningTrack.getId(), loggedInUserId, content.getQuiz().getId());
							if (formSubmit != null) {
								contentDto.setFinished(true);
							} else {
								contentDto.setFinished(false);
							}
						} else {
							contentDto.setFinished(false);
						}
						String categoryName = lmsDAO.getCategoryNameByDamId(content.getDam(), content.getQuiz());
						if (XamplifyUtils.isValidString(categoryName)) {
							quiz.setCategoryName(categoryName);
						}
						quiz.setSelected(true);
						contentDto.setQuiz(quiz);
						contentDto.setId(quiz.getId());
						contentDto.setTypeQuizId(true);
						UserDTO quizCreatedByName = userDao
								.getEmailIdAndDisplayName(content.getQuiz().getCreatedUserId());
						quiz.setCreatedName(quizCreatedByName.getFullNameOrEmailId());
						quiz.setCreatedDateString(content.getQuiz().getCreatedTime().toString());
					}
					contentList.add(contentDto);
				}
			}
		}
		learningTrackDto.setContents(contentList);

	}

	private Boolean canUpdateLearningTrack(LearningTrack learningTrack, Integer loggedInUserId) {
		Boolean canUpdate = false;
		if (learningTrack != null && loggedInUserId != null) {
			List<Integer> superiorIds = partnershipDAO.getSuperiorIds(learningTrack.getCompany().getId());
			if (superiorIds.contains(loggedInUserId)
					|| learningTrack.getCreatedBy().intValue() == loggedInUserId.intValue()) {
				canUpdate = true;
			}
		}
		return canUpdate;
	}

	@Override
	public XtremandResponse getPartnerList(Integer loggedInUserId) {
		XtremandResponse response = new XtremandResponse();
		String responseMessage = FAILED;
		Integer responseStatusCode = 400;
		if (loggedInUserId != null && loggedInUserId > 0) {
			CompanyProfile loggedInCompany = userService.getCompanyProfileByUser(loggedInUserId);
			if (loggedInCompany != null) {
				String companyType = utilService.getCompanyType(loggedInCompany.getId());
				if (companyType != null && !companyType.equals(PARTNER)) {
					List<Partnership> partnerships = partnershipDAO
							.getApprovedPartnershipsByVendorCompany(loggedInCompany);
					if (partnerships != null && !partnerships.isEmpty()) {
						List<PartnershipDTO> partnershipDtolist = new ArrayList<>();
						for (Partnership partnership : partnerships) {
							if (partnership != null && partnership.getPartnerCompany() != null) {
								PartnershipDTO partnershipDto = new PartnershipDTO();
								partnershipDto.setId(partnership.getId());
								partnershipDto.setPartnerCompanyName(partnership.getPartnerCompany().getCompanyName());
								partnershipDto.setPartnerCompanyId(partnership.getPartnerCompany().getId());
								partnershipDtolist.add(partnershipDto);
							}
						}
						response.setData(partnershipDtolist);
						responseMessage = SUCCESS;
						responseStatusCode = 200;
					}
				}
			} else {
				responseMessage = UNAUTHORIZED;
				responseStatusCode = 401;
			}
		} else {
			responseMessage = INVALID_INPUT;
			responseStatusCode = 500;
		}
		response.setMessage(responseMessage);
		response.setStatusCode(responseStatusCode);
		return response;
	}

	@Override
	public XtremandResponse validateSlug(LearningTrackDto learningTrackDto) {
		XtremandResponse response = new XtremandResponse();
		String responseMessage = FAILED;
		Integer responseStatusCode = 400;
		boolean isValid = false;
		if (learningTrackDto != null && learningTrackDto.getUserId() != null && learningTrackDto.getUserId() > 0
				&& StringUtils.isNotBlank(learningTrackDto.getSlug())) {
			CompanyProfile loggedInCompany = userService.getCompanyProfileByUser(learningTrackDto.getUserId());
			if (loggedInCompany != null) {
				String companyType = utilService.getCompanyType(loggedInCompany.getId());
				if (companyType != null && !companyType.equals(PARTNER)) {
					if (learningTrackDto.getType() == null) {
						learningTrackDto.setType(LearningTrackType.TRACK);
					}
					LearningTrack learningTrack = lmsDAO.getLearningTrackBySlug(learningTrackDto.getSlug(),
							loggedInCompany.getId(), learningTrackDto.getType());
					if (learningTrack == null) {
						isValid = true;
					}
					responseMessage = SUCCESS;
					responseStatusCode = 200;
				} else {
					responseMessage = UNAUTHORIZED;
					responseStatusCode = 401;
				}
			} else {
				responseMessage = UNAUTHORIZED;
				responseStatusCode = 401;
			}
		} else {
			responseMessage = INVALID_INPUT;
			responseStatusCode = 500;
		}
		response.setData(isValid);
		response.setMessage(responseMessage);
		response.setStatusCode(responseStatusCode);
		return response;
	}

	@SuppressWarnings("unchecked")
	@Override
	public XtremandResponse getLearningTracksForPartner(Pagination pagination) {
		XtremandResponse response = new XtremandResponse();
		String responseMessage = FAILED;
		Integer responseStatusCode = 400;
		if (pagination != null && pagination.getUserId() != null && pagination.getUserId() > 0) {
			Integer companyId = userService.getCompanyIdByUserId(pagination.getUserId());
			if (companyId != null) {
				pagination.setCompanyId(companyId);
				String companyType = utilService.getCompanyType(companyId);
				VanityUrlDetailsDTO vanityUrlDetailsDTO = utilService.getVanityUrlFilteredData(pagination.getUserId(),
						pagination.isVanityUrlFilter(), utilDao.getPrmCompanyProfileName());
				boolean loginAsPartner = XamplifyUtils.isLoginAsPartner(pagination.getLoginAsUserId());
				if (vanityUrlDetailsDTO.isPartnerLoggedInThroughVanityUrl()) {
					pagination.setVendorCompanyId(vanityUrlDetailsDTO.getVendorCompanyId());
				} else if (loginAsPartner) {
					pagination.setVendorCompanyId(userDao.getCompanyIdByUserId(pagination.getLoginAsUserId()));
				}
				if (companyType != null && companyType.contains(PARTNER)) {
					if (pagination.getLmsType() == null) {
						pagination.setLmsType(LearningTrackType.TRACK);
					}
					pagination.setPartnerView(true);
					Map<String, Object> resultMap = lmsDAO.getLearningTracksForPartner(pagination);
					if (resultMap != null && !resultMap.isEmpty() && resultMap.get("data") != null) {
						List<LearningTrackVisibility> visibilityCompanies = (List<LearningTrackVisibility>) resultMap
								.get("data");
						List<LearningTrackDto> learningTracks = new ArrayList<>();

						for (LearningTrackVisibility learningTrackVisibility : visibilityCompanies) {
							if (learningTrackVisibility != null) {
								LearningTrack learningTrack = learningTrackVisibility.getLearningTrack();
								if (learningTrack != null) {
									LearningTrackDto learningTrackDto = getLearningTrackDto(learningTrack,
											pagination.getUserId());
									// XBI-2797
									if (pagination.isPartnerView()) {
										learningTrackDto.setCreatedTime(
												DateUtils.getUtcString(learningTrackVisibility.getPublishedOn()));
										String partnerStatus = partnershipDAO
												.findPartnerShipStatusByPartnerCompanyIdAndVendorCompanyId(companyId,
														learningTrack.getCompany().getId());
										learningTrackDto.setPartnerStatus(partnerStatus);
									}

									learningTracks.add(learningTrackDto);
								}
							}
						}
						resultMap.put("data", learningTracks);
						response.setData(resultMap);
					}
					responseMessage = SUCCESS;
					responseStatusCode = 200;
				} else {
					responseMessage = UNAUTHORIZED;
					responseStatusCode = 403;
				}
			} else {
				responseMessage = UNAUTHORIZED;
				responseStatusCode = 401;
			}
		} else {
			responseMessage = INVALID_INPUT;
			responseStatusCode = 500;
		}
		response.setMessage(responseMessage);
		response.setStatusCode(responseStatusCode);
		return response;
	}

	@Override
	public XtremandResponse publishLearningTrack(boolean publish, Integer learningTrackId, Integer loggedInUserId) {
		XtremandResponse response = new XtremandResponse();
		if (XamplifyUtils.isValidInteger(loggedInUserId) && XamplifyUtils.isValidInteger(learningTrackId)) {
			CompanyProfile loggedInCompany = userService.getCompanyProfileByUser(loggedInUserId);
			if (loggedInCompany != null) {
				String companyType = utilService.getCompanyType(loggedInCompany.getId());
				if (companyType != null && !companyType.equals(PARTNER)) {
					publishLearningTrack(publish, learningTrackId, loggedInUserId, response);
				} else {
					add401Status(response);
				}
			} else {
				add401Status(response);
			}
		} else {
			response.setMessage(INVALID_INPUT);
			response.setStatusCode(500);
		}
		return response;
	}

	private void publishLearningTrack(boolean publish, Integer learningTrackId, Integer loggedInUserId,
			XtremandResponse response) {
		LearningTrack learningTrack = genericDAO.get(LearningTrack.class, learningTrackId);
		if (Boolean.TRUE.equals(canUpdateLearningTrack(learningTrack, loggedInUserId)) && (!publish
				|| (learningTrack.getVisibilityUsers() != null && !learningTrack.getVisibilityUsers().isEmpty()))) {
			learningTrack.setPublished(publish);
			learningTrack.setPublishedTime(new Date());
			deleteExistingPartnerActivities(learningTrack);
			if (learningTrack.isPublished()) {
				/******* XNFR-342 ****/
				List<Integer> uniqueUserIds = updatePublishValues(learningTrack);
				/******* XNFR-342 ****/
				if (uniqueUserIds.size() == learningTrack.getVisibilityUsers().size()) {
					learningTrack.setPublished(false);
					response.setStatusCode(402);
				} else {
					asyncComponent.sendLMSPublishedNotification(learningTrack.getId(), loggedInUserId);
					response.setStatusCode(200);
				}
			} else {
				response.setStatusCode(200);
			}
			response.setMessage(SUCCESS);
		} else {
			add401Status(response);
		}
	}

	private void add401Status(XtremandResponse response) {
		response.setMessage(UNAUTHORIZED);
		response.setStatusCode(401);
	}

	/******* XNFR-342 ****/
	private List<Integer> updatePublishValues(LearningTrack learningTrack) {
		List<Integer> uniqueUserIds = new ArrayList<>();
		List<Integer> deactivatedPartners = utilDao
				.findDeactivedPartnersByCompanyId(learningTrack.getCompany().getId());
		for (LearningTrackVisibility visibilityUser : learningTrack.getVisibilityUsers()) {
			Integer userId = visibilityUser.getUser().getUserId();
			if (deactivatedPartners.contains(userId)) {
				uniqueUserIds.add(userId);
			} else {
				visibilityUser.setPublished(true);
				visibilityUser.setPublishedOn(new Date());
			}
		}

		return uniqueUserIds;
	}

	/******* XNFR-342 ****/

	private void deleteExistingPartnerActivities(LearningTrack learningTrack) {
		if (!learningTrack.isPublished() && learningTrack.getVisibilityUsers() != null
				&& !learningTrack.getVisibilityUsers().isEmpty()) {
			Set<Integer> visibilityIds = new LinkedHashSet<>();
			for (LearningTrackVisibility visibilityUser : learningTrack.getVisibilityUsers()) {
				/******** XNFR-342 *****/
				visibilityUser.setPublished(false);
				visibilityUser.setPublishedOn(null);
				/******** XNFR-342 *****/
				visibilityIds.add(visibilityUser.getId());
				visibilityUser.setProgress(null);
				formDao.deleteLearningTrackQuizSubmissionsByUserId(learningTrack.getId(),
						visibilityUser.getUser().getUserId());
			}
			if (visibilityIds != null && !visibilityIds.isEmpty() && visibilityIds.size() > 0) {
				lmsDAO.clearPartnersActivity(visibilityIds);
			}
		}
	}

	@Override
	public XtremandResponse getBySlug(LearningTrackType type, Integer companyId, String slug, Integer loggedInUserId) {
		XtremandResponse response = new XtremandResponse();
		String responseMessage = FAILED;
		Integer responseStatusCode = 400;
		if (loggedInUserId != null && loggedInUserId > 0 && StringUtils.isNotBlank(slug)) {
			CompanyProfile loggedInCompany = userService.getCompanyProfileByUser(loggedInUserId);
			if (loggedInCompany != null) {
				LearningTrack learningTrack = lmsDAO.getLearningTrackBySlug(slug, companyId, type);
				if (canViewLearningTrack(learningTrack, loggedInUserId, loggedInCompany)) {
					response.setData(getLearningTrackDtoInDetail(learningTrack, loggedInUserId, false));
					responseMessage = SUCCESS;
					responseStatusCode = 200;
				} else {
					responseMessage = UNAUTHORIZED;
					responseStatusCode = 403;
				}
			} else {
				responseMessage = UNAUTHORIZED;
				responseStatusCode = 401;
			}
		} else {
			responseMessage = INVALID_INPUT;
			responseStatusCode = 500;
		}
		response.setMessage(responseMessage);
		response.setStatusCode(responseStatusCode);
		return response;
	}

	private boolean canViewLearningTrack(LearningTrack learningTrack, Integer loggedInUserId,
			CompanyProfile loggedInCompany) {
		Boolean canView = false;
		if (learningTrack != null && loggedInUserId != null && loggedInCompany != null) {
			if (loggedInCompany.getId() == learningTrack.getCompany().getId()
					|| checkVisibility(learningTrack, loggedInCompany.getId(), loggedInUserId)) {
				canView = true;
			}
		}
		return canView;
	}

	private boolean canViewAnalytics(LearningTrack learningTrack, Integer loggedInUserId,
			CompanyProfile loggedInCompany) {
		Boolean canView = false;
		if (learningTrack != null && loggedInUserId != null && loggedInCompany != null) {
			if (loggedInCompany.getId() == learningTrack.getCompany().getId()) {
				canView = true;
			}
		}
		return canView;
	}

	public boolean checkVisibility(LearningTrack learningTrack, Integer loggedInCompanyId, Integer loggedInUserId) {
		if (learningTrack.isPublished()) {
			Partnership partnership = partnershipDAO.checkPartnership(learningTrack.getCompany().getId(),
					loggedInCompanyId);
			if (partnership != null) {
				LearningTrackVisibility visibilityUser = lmsDAO.getVisibilityUser(loggedInUserId, partnership.getId(),
						learningTrack.getId());
				if (visibilityUser != null) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public XtremandResponse edit(MultipartFile featuredImage, LearningTrackDto learningTrackDto) {
		XtremandResponse response = new XtremandResponse();
		String responseMessage = FAILED;
		Integer responseStatusCode = 400;
		if (validateEditLearningTrackRequest(learningTrackDto)) {
			Integer loggedInUserId = learningTrackDto.getUserId();
			Integer learningTrackId = learningTrackDto.getId();
			LearningTrack learningTrack = genericDAO.get(LearningTrack.class, learningTrackId);
			/**** XNFR-523 ********/
			setUpdatedTrackData(learningTrackDto, learningTrackId, learningTrack);
			/**** XNFR-523 ********/
			CompanyProfile loggedInCompany = userService.getCompanyProfileByUser(loggedInUserId);
			if (loggedInCompany != null && canUpdateLearningTrack(learningTrack, loggedInUserId)) {
				LearningTrack exLearningTrack = lmsDAO.getLearningTrackByTitle(learningTrackDto.getTitle(),
						loggedInCompany.getId(), learningTrack.getType());
				if (exLearningTrack == null || exLearningTrack.getId() == learningTrackDto.getId()) {
					exLearningTrack = lmsDAO.getLearningTrackBySlug(learningTrackDto.getSlug(), loggedInCompany.getId(),
							learningTrack.getType());
					if (exLearningTrack == null || exLearningTrack.getId() == learningTrackDto.getId()) {
						updateLearningTrack(learningTrackDto, learningTrack, loggedInCompany, featuredImage);
						responseMessage = SUCCESS;
						responseStatusCode = 200;
					} else {
						responseMessage = "Duplicate Entry for Slug ";
						responseStatusCode = 409;
					}
				} else {
					responseMessage = "Duplicate Entry for Title ";
					responseStatusCode = 409;
				}

			} else {
				responseMessage = UNAUTHORIZED;
				responseStatusCode = 401;
			}
		} else {
			responseMessage = INVALID_INPUT;
			responseStatusCode = 500;
		}
		response.setMessage(responseMessage);
		response.setStatusCode(responseStatusCode);
		return response;
	}

	/**** XNFR-523 ********/
	private void setUpdatedTrackData(LearningTrackDto learningTrackDto, Integer learningTrackId,
			LearningTrack learningTrack) {
		if (learningTrack.isPublished()) {

			boolean isTitleUpdated = isTitleUpdated(learningTrackDto, learningTrack);

			boolean isDescriptionUpdated = isDescriptionUpdated(learningTrackDto, learningTrack);

			boolean isFollowSequenceOptionUpdated = learningTrackDto.isFollowAssetSequence() != learningTrack
					.isFollowAssetSequence();

			boolean isContentUpdated = isContentUpdated(learningTrackDto, learningTrackId);

			boolean isQuizUpdated = isQuizUpdated(learningTrackDto, learningTrackId);

			boolean assetSequenceUpdated = isAssetSequenceUpdated(learningTrackDto, learningTrackId);

			boolean isPublishedTrackUpdated = isTitleUpdated || isContentUpdated || isDescriptionUpdated
					|| isQuizUpdated || isFollowSequenceOptionUpdated || assetSequenceUpdated;
			learningTrackDto.setPublishedTrackUpdated(isPublishedTrackUpdated);
			if (isPublishedTrackUpdated) {
				List<Integer> progressedVisibilityUserIds = lmsDAO
						.findProgressedVisibilityUserIdsByTrackOrPlayBookId(learningTrackId);
				learningTrackDto.setProgressedVisibilityUserIds(progressedVisibilityUserIds);

			}
			List<Integer> existingVisibilityUserIds = lmsDAO.findVisibilityUserIdsByTrackOrPlayBookId(learningTrackId);
			learningTrackDto.setPreviouslySelectedVisibilityUserIds(existingVisibilityUserIds);
		}
	}

	private boolean isAssetSequenceUpdated(LearningTrackDto learningTrackDto, Integer learningTrackId) {
		List<LearningTrackContentResponseDTO> existingContents = lmsDAO
				.findLearningTrackContentsByTrackOrPlayBookId(learningTrackId);

		List<LearningTrackContentResponseDTO> updatedContentsDto = new ArrayList<>();
		Map<Integer, LearningTrackContentDto> newContentAndQuizData = learningTrackDto.getContentAndQuizData();
		boolean isValidContentAndQuizData = newContentAndQuizData != null && !newContentAndQuizData.isEmpty();
		if (isValidContentAndQuizData) {
			for (Entry<Integer, LearningTrackContentDto> entry : newContentAndQuizData.entrySet()) {
				Integer index = entry.getKey();
				if (index != null) {
					index = index + 1;
				}
				Integer damOrQuizId = entry.getValue().getId();
				Integer quizId = null;
				Integer damId = null;
				if (entry.getValue().isTypeQuizId()) {
					quizId = damOrQuizId;
				} else {
					damId = damOrQuizId;
				}
				LearningTrackContentResponseDTO learningTrackContentResponseDTO = new LearningTrackContentResponseDTO();
				learningTrackContentResponseDTO.setDamId(damId);
				learningTrackContentResponseDTO.setDisplayIndex(index);
				learningTrackContentResponseDTO.setQuizId(quizId);
				updatedContentsDto.add(learningTrackContentResponseDTO);
			}
		}
		return !existingContents.equals(updatedContentsDto);
	}

	private boolean isTitleUpdated(LearningTrackDto learningTrackDto, LearningTrack learningTrack) {
		String updatedTitle = learningTrackDto.getTitle();
		String existingTitle = learningTrack.getTitle();
		if (org.springframework.util.StringUtils.hasText(updatedTitle)) {
			updatedTitle = updatedTitle.trim();
		}
		if (org.springframework.util.StringUtils.hasText(existingTitle)) {
			existingTitle = existingTitle.trim();
		}
		return !updatedTitle.equalsIgnoreCase(existingTitle);
	}

	private boolean isQuizUpdated(LearningTrackDto learningTrackDto, Integer learningTrackId) {
		List<Integer> previouslySelectedQuizIds = lmsDAO.findQuizIdsByTrackOrPlayBookId(learningTrackId);
		List<Integer> updatedQuizIds = new ArrayList<>();
		List<LearningTrackContentDto> learningTrackContentDtos = learningTrackDto.getContents();
		if (XamplifyUtils.isNotEmptyList(learningTrackContentDtos)) {
			updatedQuizIds.addAll(learningTrackContentDtos.stream().map(LearningTrackContentDto::getQuiz)
					.filter(quiz -> quiz != null && XamplifyUtils.isValidInteger(quiz.getId())).map(FormDTO::getId)
					.collect(Collectors.toList()));
			updatedQuizIds.removeAll(Collections.singleton(null));
			Collections.sort(updatedQuizIds);
		}
		return !previouslySelectedQuizIds.equals(updatedQuizIds);
	}

	private boolean isContentUpdated(LearningTrackDto learningTrackDto, Integer learningTrackId) {
		List<Integer> previouslySelectedContentIds = sortPreviouslySelectedContentIds(learningTrackId);
		List<Integer> updatedContentIds = sortUpdatedContentIds(learningTrackDto);
		return !previouslySelectedContentIds.equals(updatedContentIds);
	}

	private boolean isDescriptionUpdated(LearningTrackDto learningTrackDto, LearningTrack learningTrack) {
		String existingDescription = learningTrack.getDescription();
		String updatedDescription = learningTrackDto.getDescription();
		if (org.springframework.util.StringUtils.hasText(existingDescription)) {
			existingDescription = existingDescription.trim();
		}
		if (org.springframework.util.StringUtils.hasText(updatedDescription)) {
			updatedDescription = updatedDescription.trim();
		}
		return !existingDescription.equalsIgnoreCase(updatedDescription);
	}

	private List<Integer> sortPreviouslySelectedContentIds(Integer learningTrackId) {
		List<Integer> previouslySelectedContentIds = lmsDAO.findContentIdsByTrackOrPlayBookId(learningTrackId);
		if (XamplifyUtils.isNotEmptyList(previouslySelectedContentIds)) {
			previouslySelectedContentIds.removeAll(Collections.singleton(null));
			Collections.sort(previouslySelectedContentIds);
		}
		return previouslySelectedContentIds;
	}

	private List<Integer> sortUpdatedContentIds(LearningTrackDto learningTrackDto) {
		List<Integer> updatedContentIds = new ArrayList<>();
		List<LearningTrackContentDto> learningTrackContentDtos = learningTrackDto.getContents();
		if (XamplifyUtils.isNotEmptyList(learningTrackContentDtos)) {
			updatedContentIds.addAll(
					learningTrackContentDtos.stream().map(LearningTrackContentDto::getId).collect(Collectors.toList()));
			updatedContentIds.removeAll(Collections.singleton(null));
			Collections.sort(updatedContentIds);
		}
		return updatedContentIds;
	}

	private boolean validateEditLearningTrackRequest(LearningTrackDto learningTrackDto) {
		boolean valid = false;
		if (learningTrackDto != null && learningTrackDto.getId() != null && learningTrackDto.getId() > 0
				&& !StringUtils.isBlank(learningTrackDto.getTitle())
				&& !StringUtils.isBlank(learningTrackDto.getDescription())
				&& !StringUtils.isBlank(learningTrackDto.getSlug()) && learningTrackDto.getUserId() != null
				&& learningTrackDto.getUserId() > 0 && learningTrackDto.getCategoryId() != null
				&& learningTrackDto.getCategoryId() > 0 && learningTrackDto.getContentAndQuizData() != null
				&& !learningTrackDto.getContentAndQuizData().isEmpty()
				&& ((learningTrackDto.getUserIds() != null && !learningTrackDto.getUserIds().isEmpty())
						|| (learningTrackDto.getGroupIds() != null && !learningTrackDto.getGroupIds().isEmpty()))) {
			valid = true;
		}
		return valid;
	}

	private void updateLearningTrack(LearningTrackDto learningTrackDto, LearningTrack learningTrack,
			CompanyProfile loggedInCompany, MultipartFile featuredImage) {
		if (learningTrack != null) {
			boolean isTrackModule = LearningTrackType.TRACK.equals(learningTrackDto.getType());
			boolean isPlayBookModule = LearningTrackType.PLAYBOOK.equals(learningTrackDto.getType());
			boolean sendPublishNotification = false;
			learningTrack.setTitle(XamplifyUtils.removeExtraSpace(learningTrackDto.getTitle()));
			learningTrack.setDescription(learningTrackDto.getDescription());
			learningTrack.setSlug(learningTrackDto.getSlug());
			if (learningTrack.isPublished() != learningTrackDto.isPublished()) {
				if (!learningTrack.isPublished()) {
					sendPublishNotification = true;
				}
				learningTrack.setPublished(learningTrackDto.isPublished());
				learningTrack.setPublishedTime(new Date());
			}
			learningTrack.setQuiz(null);
			learningTrack.setFollowAssetSequence(false);
			if (isTrackModule) {
				learningTrack.setFollowAssetSequence(learningTrackDto.isFollowAssetSequence());
			}

			learningTrack.initialiseCommonFields(false, learningTrackDto.getUserId());

			updateFeaturedImage(featuredImage, learningTrackDto, learningTrack, loggedInCompany);
			updateContent(learningTrackDto, learningTrack);
			updateTags(learningTrackDto, learningTrack);
			updateCategory(learningTrackDto, learningTrack);
			/******** XNFR-327 *****/
			learningTrackDto.setSendPublishNotification(sendPublishNotification);
			learningTrack.setPublishingOrWhiteLabelingInProgress(true);
			/******** XNFR-327 *****/
			learningTrack.setAddedToQuickLinks(learningTrackDto.isAddedToQuickLinks());
			learningTrack.setGroupByAssets(learningTrackDto.isGroupByAssets());
			/**** XNFR-897 ****/
			learningTrack.setExpireDate(convertStringToDate(learningTrackDto.getExpireDate()));
			/*** XNFR-523 ****/
			setTrackUpdatedEmailNoticationOption(learningTrackDto, learningTrack, isTrackModule, isPlayBookModule);
		}
	}

	/*** XNFR-523 ****/
	private void setTrackUpdatedEmailNoticationOption(LearningTrackDto learningTrackDto, LearningTrack learningTrack,
			boolean isTrackModule, boolean isPlayBookModule) {
		if (isTrackModule) {
			boolean isTrackPublishedEmailNotification = companyProfileDao
					.isTrackPublishedEmailNotificationByUserId(learningTrackDto.getUserId());
			enableOrDisableTrackUpdatedEmailNotificationOption(learningTrackDto, learningTrack,
					isTrackPublishedEmailNotification);
		} else if (isPlayBookModule) {
			boolean isPlayBookPublishedEmailNotification = companyProfileDao
					.isPlaybookPublishedEmailNotificationByUserId(learningTrackDto.getUserId());
			enableOrDisableTrackUpdatedEmailNotificationOption(learningTrackDto, learningTrack,
					isPlayBookPublishedEmailNotification);
		}
	}

	/*** XNFR-523 ****/
	private void enableOrDisableTrackUpdatedEmailNotificationOption(LearningTrackDto learningTrackDto,
			LearningTrack learningTrack, boolean isTrackOrPlayBookEmailNotificationEnabled) {
		if (!isTrackOrPlayBookEmailNotificationEnabled) {
			learningTrack.setTrackUpdatedEmailNotification(false);
		} else {
			learningTrack.setTrackUpdatedEmailNotification(learningTrackDto.isTrackUpdatedEmailNotification());
		}
	}

	private void updateCategory(LearningTrackDto learningTrackDto, LearningTrack learningTrack) {
		if (learningTrackDto.getCategoryId() != null && learningTrackDto.getCategoryId() > 0) {
			CategoryModule categoryModule = categoryDao.getCategoryModuleByLearningTrackId(learningTrack.getId());
			if (categoryModule != null) {
				if (categoryModule.getCategoryId() != null
						&& categoryModule.getCategoryId() != learningTrackDto.getCategoryId()) {
					Category newCategory = genericDAO.get(Category.class, learningTrackDto.getCategoryId());
					if (newCategory != null) {
						categoryModule.setCategoryId(newCategory.getId());
						categoryModule.setUpdatedTime(new Date());
						categoryModule.setUpdatedUserId(learningTrackDto.getUserId());
					}
				}
			} else {
				saveCategory(learningTrackDto, learningTrack);
			}
		}
	}

	private void updateFeaturedImage(MultipartFile featuredImage, LearningTrackDto learningTrackDto,
			LearningTrack learningTrack, CompanyProfile loggedInCompany) {
		if (featuredImage != null && featuredImage.getSize() > 0) {
			String filePath = uploadFeaturedImage(featuredImage, learningTrack.getId(), loggedInCompany.getId());
			if (!StringUtils.isBlank(filePath)) {
				learningTrack.setFeaturedImage(filePath);
			}
		} else if (learningTrackDto.isRemoveFeaturedImage()) {
			String filePath = learningTrack.getFeaturedImage();
			if (!StringUtils.isBlank(filePath)) {
				String fileName = filePath.substring(filePath.lastIndexOf('/') + 1);
				amazonWebService.deleteFeaturedImage(loggedInCompany.getId(), fileName);
				learningTrack.setFeaturedImage(null);
			}
		}
	}

	private void updateTags(LearningTrackDto learningTrackDto, LearningTrack learningTrack) {
		Set<Integer> newIds = learningTrackDto.getTagIds();
		Set<Integer> existingIds = new HashSet<>();
		if (learningTrackDto.getTagIds() != null) {
			for (LearningTrackTag tag : learningTrack.getTags()) {
				if (tag != null) {
					existingIds.add(tag.getTag().getId());
					if (newIds != null && !newIds.contains(tag.getTag().getId())) {
						genericDAO.remove(tag);
					}
				}
			}
			newIds.removeAll(existingIds);
			for (Integer newId : newIds) {
				if (newId != null && newId > 0) {
					saveTag(learningTrackDto.getUserId(), learningTrack, newId);
				}
			}
		}

	}

	private void updateContent(LearningTrackDto learningTrackDto, LearningTrack learningTrack) {
		List<LearningTrackContent> existingContents = new ArrayList<>();
		List<Integer> existingContentIds = new ArrayList<>();
		existingContents = sortContentsAndAddIdsToExistingContents(learningTrack, existingContents, existingContentIds);

		List<Integer> newContentIds = new ArrayList<>();
		Map<Integer, LearningTrackContentDto> newContentAndQuizData = learningTrackDto.getContentAndQuizData();
		Map<Integer, Integer> newContentData = new LinkedHashMap<>();
		Map<Integer, Integer> newQuizData = new LinkedHashMap<>();

		addNewContentOrQuizData(newContentIds, newContentAndQuizData, newContentData, newQuizData);

		removeAndUpdateContentData(learningTrackDto, learningTrack, existingContents, existingContentIds, newContentIds,
				newContentAndQuizData, newContentData, newQuizData);
	}

	private void removeAndUpdateContentData(LearningTrackDto learningTrackDto, LearningTrack learningTrack,
			List<LearningTrackContent> existingContents, List<Integer> existingContentIds, List<Integer> newContentIds,
			Map<Integer, LearningTrackContentDto> newContentAndQuizData, Map<Integer, Integer> newContentData,
			Map<Integer, Integer> newQuizData) {
		if (!newContentIds.equals(existingContentIds)) {
			lmsDAO.clearDisplayIndex(learningTrackDto.getId());
			List<Integer> newList = new ArrayList<>(newContentIds);
			List<Integer> oldList = new ArrayList<>(existingContentIds);
			Collections.sort(newList);
			Collections.sort(oldList);
			List<Integer> clonedList = new ArrayList<>(newContentIds);
			for (LearningTrackContent existingContent : existingContents) {
				if (existingContent != null
						&& ((existingContent.getDam() != null && existingContent.getDam().getId() != null)
								|| (existingContent.getQuiz() != null && existingContent.getQuiz().getId() != null))) {
					Integer contentId = null;
					boolean isExistingContentQuiz = false;
					if (existingContent.getDam() != null) {
						contentId = existingContent.getDam().getId();
					} else if (existingContent.getQuiz() != null) {
						contentId = existingContent.getQuiz().getId();
						isExistingContentQuiz = true;
					}
					if (newContentIds.contains(contentId)) {
						existingContent.setDisplayIndex(newContentIds.indexOf(contentId) + 1);
						existingContent.initialiseCommonFields(false, learningTrackDto.getUserId());
						if (contentId != null) {
							if (isExistingContentQuiz) {
								newQuizData.entrySet().removeIf(
										entry -> (existingContent.getQuiz().getId().equals(entry.getValue())));
							} else {
								newContentData.entrySet()
										.removeIf(entry -> (existingContent.getDam().getId().equals(entry.getValue())));
							}
						}
					} else {
						if (existingContent.getQuiz() != null && existingContent.getQuiz().getId() > 0) {
							formDao.deleteLearningTrackQuizSubmissionsByFormId(learningTrackDto.getId(),
									existingContent.getQuiz().getId());
						}
						genericDAO.remove(existingContent);
					}
				}
			}
			clonedList.removeAll(existingContentIds);
			for (Entry<Integer, Integer> newQuiz : newQuizData.entrySet()) {
				saveContent(learningTrack, newQuiz.getValue(), learningTrackDto.getUserId(), newQuiz.getKey() + 1,
						true);
			}
			for (Entry<Integer, Integer> newContent : newContentData.entrySet()) {
				saveContent(learningTrack, newContent.getValue(), learningTrackDto.getUserId(), newContent.getKey() + 1,
						false);
			}

		}
	}

	private void addNewContentOrQuizData(List<Integer> newContentIds,
			Map<Integer, LearningTrackContentDto> newContentAndQuizData, Map<Integer, Integer> newContentData,
			Map<Integer, Integer> newQuizData) {
		if (newContentAndQuizData != null && !newContentAndQuizData.isEmpty()) {
			for (Entry<Integer, LearningTrackContentDto> entry : newContentAndQuizData.entrySet()) {
				if (entry != null && entry.getValue() != null) {
					LearningTrackContentDto newContent = entry.getValue();
					if (newContent.isTypeQuizId()) {
						newContentIds.add(newContent.getId());
						newQuizData.put(entry.getKey(), newContent.getId());
					} else {
						newContentIds.add(newContent.getId());
						newContentData.put(entry.getKey(), newContent.getId());
					}
				}
			}
		}
	}

	private List<LearningTrackContent> sortContentsAndAddIdsToExistingContents(LearningTrack learningTrack,
			List<LearningTrackContent> existingContents, List<Integer> existingContentIds) {
		if (learningTrack.getContents() != null && !learningTrack.getContents().isEmpty()) {
			existingContents = (learningTrack.getContents()).stream()
					.sorted(Comparator.comparing(LearningTrackContent::getDisplayIndex)).collect(Collectors.toList());
			if (existingContents != null && !existingContents.isEmpty()) {
				for (LearningTrackContent existingContent : existingContents) {
					if (existingContent != null) {
						if (existingContent.getDam() != null) {
							existingContentIds.add(existingContent.getDam().getId());
						} else if (existingContent.getQuiz() != null) {
							existingContentIds.add(existingContent.getQuiz().getId());
						}
					}
				}
			}
		}
		return existingContents;
	}

	@Override
	public XtremandResponse delete(LearningTrackDto learningTrackDto) {
		XtremandResponse response = new XtremandResponse();
		String responseMessage = FAILED;
		Integer responseStatusCode = 400;
		if (learningTrackDto != null && learningTrackDto.getId() != null && learningTrackDto.getId() > 0
				&& learningTrackDto.getUserId() != null && learningTrackDto.getUserId() > 0) {
			Integer loggedInUserId = learningTrackDto.getUserId();
			LearningTrack learningTrack = genericDAO.get(LearningTrack.class, learningTrackDto.getId());
			if (Boolean.TRUE.equals(canUpdateLearningTrack(learningTrack, loggedInUserId))) {
				genericDAO.remove(learningTrack);
				responseMessage = SUCCESS;
				responseStatusCode = 200;
			} else {
				responseMessage = UNAUTHORIZED;
				responseStatusCode = 401;
			}
		} else {
			responseMessage = INVALID_INPUT;
			responseStatusCode = 500;
		}
		response.setMessage(responseMessage);
		response.setStatusCode(responseStatusCode);
		return response;
	}

	@Override
	public XtremandResponse updatePartnerProgress(LearningTrackDto learningTrackDto) {
		XtremandResponse response = new XtremandResponse();
		String responseMessage = FAILED;
		Integer responseStatusCode = 400;
		if (learningTrackDto != null && learningTrackDto.getId() != null && learningTrackDto.getId() > 0
				&& learningTrackDto.getUserId() != null && learningTrackDto.getUserId() > 0
				&& learningTrackDto.getContentId() != null && learningTrackDto.getContentId() > 0) {
			Integer loggedInUserId = learningTrackDto.getUserId();
			User user = userService.loadUser(Arrays.asList(new Criteria("userId", OPERATION_NAME.eq, loggedInUserId)),
					new FindLevel[] { FindLevel.COMPANY_PROFILE });
			LearningTrack learningTrack = genericDAO.get(LearningTrack.class, learningTrackDto.getId());
			CompanyProfile loggedInCompany = user.getCompanyProfile();
			if (loggedInCompany != null && learningTrack.isPublished()) {
				Partnership partnership = partnershipDAO.checkPartnership(learningTrack.getCompany().getId(),
						loggedInCompany.getId());
				if (partnership != null) {
					LearningTrackVisibility visibilityUser = lmsDAO.getVisibilityUser(loggedInUserId,
							partnership.getId(), learningTrack.getId());
					LearningTrackContent content = null;
					if (learningTrackDto.isTypeQuizId()) {
						content = lmsDAO.getContentByQuizID(learningTrackDto.getId(), learningTrackDto.getContentId());
					} else if (!learningTrackDto.isTypeQuizId()) {
						content = lmsDAO.getContentByDamID(learningTrackDto.getId(), learningTrackDto.getContentId());
					}
					if (visibilityUser != null && content != null) {
						updatePartnerProgress(content, visibilityUser, learningTrackDto.getStatus(),
								learningTrackDto.isTypeQuizId());
						responseMessage = SUCCESS;
						responseStatusCode = 200;
					}
				}
			} else {
				responseMessage = UNAUTHORIZED;
				responseStatusCode = 401;
			}
		} else {
			responseMessage = INVALID_INPUT;
			responseStatusCode = 500;
		}
		response.setMessage(responseMessage);
		response.setStatusCode(responseStatusCode);
		return response;
	}

	private void updatePartnerProgress(LearningTrackContent content, LearningTrackVisibility visibilityUser,
			PartnerActivityType partnerActivityType, boolean typeQuizId) {
		if (content != null && visibilityUser != null) {
			savePartnerActivity(content, visibilityUser, partnerActivityType);
			LearningTrack learningTrack = content.getLearningTrack();
			if (FINISHED_STATUS_LIST.contains(partnerActivityType.name())) {
				Integer totalContentCount = learningTrack.getContents().size();
				calculateAndSavePartnerProgress(learningTrack, visibilityUser, totalContentCount);
			}
		}
	}

	private void calculateAndSavePartnerProgress(LearningTrack learningTrack, LearningTrackVisibility visibilityUser,
			Integer totalContentCount) {
		List<Integer> finishedContentIds = lmsDAO.getPartnerFinishedContent(learningTrack.getId(),
				visibilityUser.getId());
		Integer finishedCount = 0;
		if (finishedContentIds != null && !finishedContentIds.isEmpty()) {
			finishedCount = finishedContentIds.size();
		}
		Integer progress = null;
		if (totalContentCount != null && totalContentCount > 0 && finishedCount != null && finishedCount > 0
				&& finishedCount <= totalContentCount) {
			if (finishedCount == totalContentCount) {
				progress = 100;
			} else {
				float totalcountByhundred = (float) 100 / totalContentCount;
				progress = (int) (totalcountByhundred * finishedCount);
			}
		}
		visibilityUser.setProgress(progress);
	}

	private void savePartnerActivity(LearningTrackContent content, LearningTrackVisibility visibilityUser,
			PartnerActivityType partnerActivityType) {
		LearningTrackPartnerActivity partnerActivity = new LearningTrackPartnerActivity();
		partnerActivity.setLearningTrackContent(content);
		partnerActivity.setVisibilityUser(visibilityUser);
		partnerActivity.setType(partnerActivityType);
		partnerActivity.setCreatedBy(visibilityUser.getUser().getUserId());
		partnerActivity.initialiseCommonFields(true, visibilityUser.getUser().getUserId());
		genericDAO.save(partnerActivity);
		genericDAO.flushCurrentSession();
	}

	@Override
	public XtremandResponse getAnalytics(Pagination pagination) {
		XtremandResponse response = new XtremandResponse();
		String responseMessage = FAILED;
		Integer responseStatusCode = 400;
		if (pagination != null && pagination.getUserId() != null && pagination.getUserId() > 0
				&& pagination.getLearningTrackId() != null && pagination.getLearningTrackId() > 0) {
			Integer loggedInUserId = pagination.getUserId();
			CompanyProfile loggedInCompany = userService.getCompanyProfileByUser(loggedInUserId);
			if (loggedInCompany != null) {
				LearningTrack learningTrack = genericDAO.get(LearningTrack.class, pagination.getLearningTrackId());
				if (canViewAnalytics(learningTrack, loggedInUserId, loggedInCompany)) {
					response.setData(lmsDAO.getAnalytics(pagination));
					responseMessage = SUCCESS;
					responseStatusCode = 200;
				} else {
					responseMessage = UNAUTHORIZED;
					responseStatusCode = 403;
				}
			} else {
				responseMessage = UNAUTHORIZED;
				responseStatusCode = 401;
			}
		} else {
			responseMessage = INVALID_INPUT;
			responseStatusCode = 500;
		}
		response.setMessage(responseMessage);
		response.setStatusCode(responseStatusCode);
		return response;
	}

	@Override
	public HttpServletResponse downloadTrackAnalytics(Pageable pageable, Integer userId, Integer learningTrackId,
			String lmsType, HttpServletResponse response) {
		Pagination pagination = new Pagination();
		pagination.setUserId(userId);
		pagination.setLearningTrackId(learningTrackId);
		String sort = pageable.getSort();
		if (sort != null && org.springframework.util.StringUtils.hasText(sort)) {
			String sortColumn = sort.split(",")[0];
			String sortOrder = sort.split(",")[1];
			pagination.setSortcolumn(sortColumn);
			pagination.setSortingOrder(sortOrder);
		}
		pagination.setSearchKey(pageable.getSearch());
		boolean isTeamMemberPartnerFilter = pageable.isFilterPartners();
		pagination.setPartnerTeamMemberGroupFilter(isTeamMemberPartnerFilter);
		String fileName = "";
		if ("TRACK".equalsIgnoreCase(lmsType)) {
			pagination.setLmsType(LearningTrackType.TRACK);
			fileName = "Track Interaction Details";
		} else {
			pagination.setLmsType(LearningTrackType.PLAYBOOK);
			fileName = "Playbook Interaction Details";
		}
		if (pagination != null && pagination.getUserId() != null && pagination.getUserId() > 0
				&& pagination.getLearningTrackId() != null && pagination.getLearningTrackId() > 0) {
			Integer loggedInUserId = pagination.getUserId();
			CompanyProfile loggedInCompany = userService.getCompanyProfileByUser(loggedInUserId);
			if (loggedInCompany != null) {
				LearningTrack learningTrack = genericDAO.get(LearningTrack.class, pagination.getLearningTrackId());
				if (canViewAnalytics(learningTrack, loggedInUserId, loggedInCompany)) {
					Map<String, Object> analytics = lmsDAO.downloadTrackAnalytics(pagination);
					@SuppressWarnings("unchecked")
					List<CompanyDTO> companyDTOList = (List<CompanyDTO>) analytics.get("data");
					List<Integer> companyIds = new ArrayList<>();

					for (CompanyDTO company : companyDTOList) {
						if (company != null && company.getId() != null) {
							companyIds.add(company.getId());
						}
					}

					pagination.setCompanyIds(companyIds);
					Map<String, Object> detailedAnalytics = lmsDAO.downloadDetailedTrackAnalytics(pagination);
					@SuppressWarnings("unchecked")
					List<CompanyDTO> companyDetailedDTOList = (List<CompanyDTO>) detailedAnalytics.get("data");
					return frameTrackAnalyticsCSVData(response, fileName, lmsType, companyDTOList,
							companyDetailedDTOList, pagination);
				}
			}
		}
		return null;
	}

	private HttpServletResponse frameTrackAnalyticsCSVData(HttpServletResponse response, String fileName,
			String lmsType, List<CompanyDTO> companyDTOList, List<CompanyDTO> companyDetailedDTOList,
			Pagination pagination) {
		try {
			XSSFWorkbook workbook = createWorkbookForTrackAnalyticsData(lmsType, companyDTOList, companyDetailedDTOList,
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

	private XSSFWorkbook createWorkbookForTrackAnalyticsData(String lmsType, List<CompanyDTO> companyDTOList,
			List<CompanyDTO> companyDetailedDTOList, Pagination pagination) {
		List<String[]> row = new ArrayList<>();
		List<String> headerList = new ArrayList<>();

		headerList.add("PARTNER COMPANY");
		if ("TRACK".equalsIgnoreCase(lmsType)) {
			headerList.add("TRACK NAME");
		} else {
			headerList.add("PLAYBOOK NAME");
		}
		headerList.add("CREATED BY");
		headerList.add("VIEW COUNT");
		headerList.add("PUBLISHED ON(PST)");

		row.add(headerList.toArray(new String[0]));
		for (CompanyDTO track : companyDTOList) {
			String publishedDate = checkIfDateIsNull(track.getPublishedOn());
			List<String> dataList = new ArrayList<>();
			dataList.add(track.getCompanyName());
			dataList.add(track.getTrackName());
			dataList.add(track.getCreatedBy());
			dataList.add(track.getViewCount() != null ? track.getViewCount().toString() : "0");
			dataList.add(publishedDate);
			row.add(dataList.toArray(new String[0]));
		}

		XSSFWorkbook workbook = new XSSFWorkbook();
		Sheet sheet;
		if ("TRACK".equalsIgnoreCase(lmsType)) {
			sheet = workbook.createSheet("Track Interaction Details");
		} else {
			sheet = workbook.createSheet("Playbook Interaction Details");
		}
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
		for (int i = 0; i < row.get(0).length; i++) {
			if ("VIEW COUNT".equalsIgnoreCase(row.get(0)[i])) {
				viewCountColumnIndex = i;
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
					link.setAddress("'All Partner Interaction Details'!A1");
					cell.setHyperlink(link);
					cell.setCellStyle(hyperlinkStyle);
				} else {
					cell.setCellValue(data[j]);
				}
			}
		}
		Sheet viewDetailSheet = workbook.createSheet("All Partner Interaction Details");
		Row drillHeader = viewDetailSheet.createRow(0);
		if ("TRACK".equalsIgnoreCase(lmsType)) {
			drillHeader.createCell(0).setCellValue("TRACK NAME");
		} else {
			drillHeader.createCell(0).setCellValue("PLAYBOOK NAME");
		}
		drillHeader.createCell(1).setCellValue("CREATED BY");
		drillHeader.createCell(2).setCellValue("PARTNER COMPANY NAME");
		drillHeader.createCell(3).setCellValue("PARTNER EMAIL ID");
		drillHeader.createCell(4).setCellValue("PARTNER NAME");
		drillHeader.createCell(5).setCellValue("PROGRESS");
		if ("TRACK".equalsIgnoreCase(lmsType)) {
			drillHeader.createCell(6).setCellValue("QUIZ SCORE");
			drillHeader.createCell(7).setCellValue("PUBLISHED ON(PST)");
		} else {
			drillHeader.createCell(6).setCellValue("PUBLISHED ON(PST)");
		}
		viewDetailSheet.setAutoFilter(new CellRangeAddress(0, 0, 0, 6));
		int drillRowNum = 1;
		for (CompanyDTO track : companyDetailedDTOList) {
			Row r = viewDetailSheet.createRow(drillRowNum++);
			r.createCell(0).setCellValue(track.getTrackName());
			r.createCell(1).setCellValue(track.getCreatedBy());
			r.createCell(2).setCellValue(track.getCompanyName());
			r.createCell(3).setCellValue(track.getEmailId());
			r.createCell(4)
					.setCellValue(track.getPartnerName() != null && !track.getPartnerName().trim().isEmpty()
							? track.getPartnerName()
							: "–");
			r.createCell(5).setCellValue(track.getProgress() != null ? track.getProgress().toString() + "%" : "0%");
			String cellValue = "";
			if ("TRACK".equalsIgnoreCase(lmsType)) {
				String quizName = track.getFormName();
				Integer progress = track.getProgress();
				String score = track.getScore();
				Date submittedOn = track.getSubmittedOn();
				if (score != null && quizName != null && !quizName.trim().isEmpty() && progress != null
						&& submittedOn != null) {
					String displayQuizName = quizName.length() > 35 ? quizName.substring(0, 35) + "..." : quizName;
					String formattedDate = new SimpleDateFormat("MMM d, yyyy, h:mm a").format(submittedOn);
					cellValue = displayQuizName + " | " + score + " | Submitted On: " + formattedDate;
				} else {
					cellValue = score != null ? score : "";
				}
				r.createCell(6).setCellValue(cellValue);
				r.createCell(7).setCellValue(checkIfDateIsNull(track.getPublishedOn()));
			} else {
				r.createCell(6).setCellValue(checkIfDateIsNull(track.getPublishedOn()));
			}
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

	@SuppressWarnings("unchecked")
	@Override
	public XtremandResponse getPartnerAnalytics(Pagination pagination) {
		XtremandResponse response = new XtremandResponse();
		String responseMessage = FAILED;
		Integer responseStatusCode = 400;
		if (pagination != null && pagination.getUserId() != null && pagination.getUserId() > 0
				&& pagination.getLearningTrackId() != null && pagination.getLearningTrackId() > 0
				&& pagination.getPartnerCompanyId() != null && pagination.getPartnerCompanyId() > 0) {
			Integer loggedInUserId = pagination.getUserId();
			CompanyProfile loggedInCompany = userService.getCompanyProfileByUser(loggedInUserId);
			if (loggedInCompany != null) {
				LearningTrack learningTrack = genericDAO.get(LearningTrack.class, pagination.getLearningTrackId());
				if (canViewAnalytics(learningTrack, loggedInUserId, loggedInCompany)) {
					Partnership partnership = partnershipDAO.checkPartnership(learningTrack.getCompany().getId(),
							pagination.getPartnerCompanyId());
					Map<String, Object> resultMap = lmsDAO.getPartnerAnalytics(pagination);
					List<UserDTO> users = (List<UserDTO>) resultMap.get("data");
					if (LearningTrackType.TRACK == learningTrack.getType() && users != null && !users.isEmpty()) {
						Set<Integer> quizIds = new LinkedHashSet<>();
						if (learningTrack.getContents() != null && !learningTrack.getContents().isEmpty()) {
							quizIds = learningTrack.getContents().stream()
									.filter(learningTrackContent -> learningTrackContent.getQuiz() != null)
									.map(learningTrackContent -> learningTrackContent.getQuiz().getId())
									.collect(Collectors.toSet());
						}
						boolean hasLearningTrackQuiz = false;
						if (quizIds != null && !quizIds.isEmpty()) {
							hasLearningTrackQuiz = true;
						}
						iterateUsersAndSetProperties(learningTrack, partnership, users, hasLearningTrackQuiz);
					}
					resultMap.put("data", users);
					response.setData(resultMap);
					responseMessage = SUCCESS;
					responseStatusCode = 200;
				} else {
					responseMessage = UNAUTHORIZED;
					responseStatusCode = 403;
				}
			} else {
				responseMessage = UNAUTHORIZED;
				responseStatusCode = 401;
			}
		} else {
			responseMessage = INVALID_INPUT;
			responseStatusCode = 500;
		}
		response.setMessage(responseMessage);
		response.setStatusCode(responseStatusCode);
		return response;
	}

	private void iterateUsersAndSetProperties(LearningTrack learningTrack, Partnership partnership, List<UserDTO> users,
			boolean hasLearningTrackQuiz) {
		for (UserDTO user : users) {
			user.setHasLearningTrackQuiz(hasLearningTrackQuiz);
			if (partnership != null) {
				Integer recentlysubmittedContentId = lmsDAO.getRecentlyFinishedQuizId(learningTrack.getId(),
						user.getId(), partnership.getId());
				if (recentlysubmittedContentId != null) {
					FormSubmit formSubmit = formDao.getLearningTrackFormSubmissionByFormID(learningTrack.getId(),
							user.getId(), recentlysubmittedContentId);
					if (formSubmit != null) {
						user.setLearningTrackScore(formSubmit.getScore());
						user.setLearningTrackMaxScore(formSubmit.getForm().getMaxScore());
						user.setQuizSubmittedTime(DateUtils.getUtcString(formSubmit.getSubmittedOn()));
						user.setQuizId(formSubmit.getForm().getId());
						user.setLearningTrackId(learningTrack.getId());
						user.setQuizName(formSubmit.getForm().getFormName());
					}
				}
			}
		}
	}

	@Override
	public HttpServletResponse downloadPartnerTrackAnalytics(Pageable pageable, Integer userId, Integer learningTrackId,
			Integer partnerCompanyId, String lmsType, HttpServletResponse response) {
		Pagination pagination = new Pagination();
		pagination.setUserId(userId);
		pagination.setLearningTrackId(learningTrackId);
		String sort = pageable.getSort();
		if (sort != null && org.springframework.util.StringUtils.hasText(sort)) {
			String sortColumn = sort.split(",")[0];
			String sortOrder = sort.split(",")[1];
			pagination.setSortcolumn(sortColumn);
			pagination.setSortingOrder(sortOrder);
		}
		pagination.setSearchKey(pageable.getSearch());
		pagination.setPartnerCompanyId(partnerCompanyId);
		boolean isTeamMemberPartnerFilter = pageable.isFilterPartners();
		pagination.setPartnerTeamMemberGroupFilter(isTeamMemberPartnerFilter);
		String fileName = "All Partner Interactions";
		if (pagination != null && pagination.getUserId() != null && pagination.getUserId() > 0
				&& pagination.getLearningTrackId() != null && pagination.getLearningTrackId() > 0
				&& pagination.getPartnerCompanyId() != null && pagination.getPartnerCompanyId() > 0) {
			Integer loggedInUserId = pagination.getUserId();
			CompanyProfile loggedInCompany = userService.getCompanyProfileByUser(loggedInUserId);
			if (loggedInCompany != null) {
				LearningTrack learningTrack = genericDAO.get(LearningTrack.class, pagination.getLearningTrackId());
				if (canViewAnalytics(learningTrack, loggedInUserId, loggedInCompany)) {
					Partnership partnership = partnershipDAO.checkPartnership(learningTrack.getCompany().getId(),
							pagination.getPartnerCompanyId());
					Map<String, Object> resultMap = lmsDAO.getPartnerTrackAnalyticsForDownload(pagination);
					List<UserDTO> users = (List<UserDTO>) resultMap.get("data");
					if (LearningTrackType.TRACK == learningTrack.getType() && users != null && !users.isEmpty()) {
						Set<Integer> quizIds = new LinkedHashSet<>();
						if (learningTrack.getContents() != null && !learningTrack.getContents().isEmpty()) {
							quizIds = learningTrack.getContents().stream()
									.filter(learningTrackContent -> learningTrackContent.getQuiz() != null)
									.map(learningTrackContent -> learningTrackContent.getQuiz().getId())
									.collect(Collectors.toSet());
						}
						boolean hasLearningTrackQuiz = false;
						if (quizIds != null && !quizIds.isEmpty()) {
							hasLearningTrackQuiz = true;
						}
						iterateUsersAndSetProperties(learningTrack, partnership, users, hasLearningTrackQuiz);
					}
					resultMap.put("data", users);
					@SuppressWarnings("unchecked")
					List<UserDTO> userDTOList = (List<UserDTO>) resultMap.get("data");
					return frameTrackPartnerAnalyticsCSVData(response, fileName, lmsType, userDTOList, pagination);
				}
			}
		}
		return null;

	}

	private HttpServletResponse frameTrackPartnerAnalyticsCSVData(HttpServletResponse response, String fileName,
			String lmsType, List<UserDTO> userDTOList, Pagination pagination) {
		try {
			List<String[]> row = new ArrayList<>();
			List<String> headerList = new ArrayList<>();
			if ("TRACK".equalsIgnoreCase(lmsType)) {
				headerList.add("TRACK NAME");
			} else {
				headerList.add("PLAYBOOK NAME");
			}
			headerList.add("CREATED BY");
			headerList.add("PARTNER COMPANY NAME");
			headerList.add("PARTNER NAME");
			headerList.add("PARTNER EMAIL ID");
			headerList.add("PROGRESS");
			if ("TRACK".equalsIgnoreCase(lmsType)) {
				headerList.add("QUIZ SCORE");
			}
			headerList.add("PUBLISHED ON(PST)");
			row.add(headerList.toArray(new String[0]));
			for (UserDTO track : userDTOList) {
				String publishedDate = checkIfDateIsNull(track.getPublishedOn());
				List<String> dataList = new ArrayList<>();
				dataList.add(track.getTrackName());
				dataList.add(track.getCreatedBy());
				dataList.add(track.getCompanyName());
				dataList.add(track.getPartnerName());
				dataList.add(track.getEmailId());
				dataList.add(track.getProgress() != null ? track.getProgress().toString() + "%" : "0%");
				if ("TRACK".equalsIgnoreCase(lmsType)) {
					if (track.isHasLearningTrackQuiz()) {
						if (track.getLearningTrackScore() != null && track.getLearningTrackMaxScore() != null) {
							String quizName = track.getQuizName() != null ? track.getQuizName() : "";
							if (quizName.length() > 35) {
								quizName = quizName.substring(0, 35) + "...";
							}
							String submittedTime = track.getQuizSubmittedTime() != null ? track.getQuizSubmittedTime()
									: "";
							String scoreDisplay = quizName + " | " + track.getLearningTrackScore() + " out of "
									+ track.getLearningTrackMaxScore();
							if (!submittedTime.isEmpty()) {
								scoreDisplay += " | Submitted On: "
										+ DateUtils.formatUtcIsoStringToReadable(submittedTime);
							}
							dataList.add(scoreDisplay);
						} else {
							dataList.add("Not Yet Finished");
						}
					} else {
						dataList.add("Quiz Not Available");
					}
				}
				dataList.add(publishedDate);
				row.add(dataList.toArray(new String[0]));
			}
			return XamplifyUtils.generateCSV(fileName, response, row);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return response;
	}

	public static String checkIfFullNameIsNull(String firstName, String lastName) {
		StringBuilder fullName = new StringBuilder();

		if (firstName != null && !firstName.trim().isEmpty()) {
			fullName.append(firstName.trim());
		}

		if (lastName != null && !lastName.trim().isEmpty()) {
			if (fullName.length() > 0) {
				fullName.append(" ");
			}
			fullName.append(lastName.trim());
		}

		return fullName.toString();
	}

	@Override
	public XtremandResponse getById(Integer learningTrackId, Integer loggedInUserId) {
		XtremandResponse response = new XtremandResponse();
		String responseMessage = FAILED;
		Integer responseStatusCode = 400;
		if (loggedInUserId != null && loggedInUserId > 0 && learningTrackId != null && learningTrackId > 0) {
			CompanyProfile loggedInCompany = userService.getCompanyProfileByUser(loggedInUserId);
			if (loggedInCompany != null) {
				LearningTrack learningTrack = genericDAO.get(LearningTrack.class, learningTrackId);
				if (canViewLearningTrack(learningTrack, loggedInUserId, loggedInCompany)) {
					response.setData(getLearningTrackDtoInDetail(learningTrack, loggedInUserId, false));
					responseMessage = SUCCESS;
					responseStatusCode = 200;
				} else {
					responseMessage = UNAUTHORIZED;
					responseStatusCode = 403;
				}
			} else {
				responseMessage = UNAUTHORIZED;
				responseStatusCode = 401;
			}
		} else {
			responseMessage = INVALID_INPUT;
			responseStatusCode = 500;
		}
		response.setMessage(responseMessage);
		response.setStatusCode(responseStatusCode);
		return response;
	}

	@SuppressWarnings("unchecked")
	@Override
	public XtremandResponse getPartnerActivities(Pagination pagination) {
		XtremandResponse response = new XtremandResponse();
		String responseMessage = FAILED;
		Integer responseStatusCode = 400;
		if (pagination != null && pagination.getUserId() != null && pagination.getUserId() > 0
				&& pagination.getLearningTrackId() != null && pagination.getLearningTrackId() > 0
				&& pagination.getPartnerId() != null && pagination.getPartnerId() > 0) {
			Integer loggedInUserId = pagination.getUserId();
			CompanyProfile loggedInCompany = userService.getCompanyProfileByUser(loggedInUserId);
			if (loggedInCompany != null) {
				LearningTrack learningTrack = genericDAO.get(LearningTrack.class, pagination.getLearningTrackId());
				if (canViewAnalytics(learningTrack, loggedInUserId, loggedInCompany)) {
					Map<String, Object> resultMap = lmsDAO.getPartnerActivities(pagination);
					List<LearningTrackPartnerActivityDto> activitiesDto = new ArrayList<LearningTrackPartnerActivityDto>();
					if (resultMap != null) {
						List<LearningTrackPartnerActivity> activities = (List<LearningTrackPartnerActivity>) resultMap
								.get("data");
						if (activities != null && !activities.isEmpty()) {
							for (LearningTrackPartnerActivity activity : activities) {
								LearningTrackPartnerActivityDto activityDto = new LearningTrackPartnerActivityDto();
								if (activity.getLearningTrackContent().getDam() != null) {
									activityDto.setType(activity.getType());
									activityDto.setCreatedTime(DateUtils.getUtcString(activity.getCreatedTime()));
									activityDto
											.setAssetName(activity.getLearningTrackContent().getDam().getAssetName());
									VideoFile videoFile = activity.getLearningTrackContent().getDam().getVideoFile();
									if (videoFile != null) {
										activityDto.setAssetThumbnail(serverPath + videoFile.getImageUri());
									} else {
										activityDto.setAssetThumbnail(
												activity.getLearningTrackContent().getDam().getThumbnailPath());
									}
									activityDto.setTypeQuiz(false);
								} else if (activity.getLearningTrackContent().getQuiz() != null) {
									activityDto.setType(activity.getType());
									activityDto.setCreatedTime(DateUtils.getUtcString(activity.getCreatedTime()));
									activityDto
											.setAssetName(activity.getLearningTrackContent().getQuiz().getFormName());
									activityDto.setTypeQuiz(true);
									if (PartnerActivityType.SUBMITTED.equals(activity.getType())) {
										FormSubmit formSubmit = formDao.getLearningTrackFormSubmissionByFormID(
												learningTrack.getId(), pagination.getPartnerId(),
												activity.getLearningTrackContent().getQuiz().getId());
										if (formSubmit != null) {
											activityDto.setLearningTrackScore(formSubmit.getScore());
											activityDto.setLearningTrackMaxScore(formSubmit.getForm().getMaxScore());
											activityDto.setQuizId(formSubmit.getForm().getId());
											activityDto.setLearningTrackId(learningTrack.getId());
										}
									}
								}
								activityDto.setFeaturedImage(learningTrack.getFeaturedImage());
								activitiesDto.add(activityDto);
							}
						}
					}
					resultMap.put("data", activitiesDto);
					response.setData(resultMap);
					responseMessage = SUCCESS;
					responseStatusCode = 200;
				} else {
					responseMessage = UNAUTHORIZED;
					responseStatusCode = 401;
				}
			} else {
				responseMessage = UNAUTHORIZED;
				responseStatusCode = 401;
			}
		} else {
			responseMessage = INVALID_INPUT;
			responseStatusCode = 500;
		}
		response.setMessage(responseMessage);
		response.setStatusCode(responseStatusCode);
		return response;
	}

	@Override
	public XtremandResponse downloadPDF(Integer learningTrackId, Integer contentId, Integer userId) throws IOException {
		XtremandResponse response = new XtremandResponse();
		String responseMessage = FAILED;
		Integer responseStatusCode = 400;
		if (learningTrackId != null && learningTrackId > 0 && userId != null && userId > 0 && contentId != null
				&& contentId > 0) {
			Integer loggedInUserId = userId;
			User user = userService.loadUser(Arrays.asList(new Criteria("userId", OPERATION_NAME.eq, loggedInUserId)),
					new FindLevel[] { FindLevel.COMPANY_PROFILE });
			LearningTrack learningTrack = genericDAO.get(LearningTrack.class, learningTrackId);
			CompanyProfile loggedInCompany = user.getCompanyProfile();
			if (loggedInCompany != null && canViewLearningTrack(learningTrack, loggedInUserId, loggedInCompany)) {
				Dam damAsset = genericDAO.get(Dam.class, contentId);
				if (damAsset != null) {
					DamDownloadDTO damDownloadDTO = new DamDownloadDTO();
					damDownloadDTO.setCompanyLogo(damAsset.getCompanyProfile().getCompanyLogoPath());
					damDownloadDTO.setCreatedBy(damAsset.getCreatedBy());
					damDownloadDTO.setFileName(damAsset.getAssetName());
					damDownloadDTO.setHtmlBody(damAsset.getHtmlBody());
					damDownloadDTO.setPageOrientation(damAsset.getPageOrientation());
					damDownloadDTO.setPageSize(damAsset.getPageSize());
					if (!org.springframework.util.StringUtils.hasText(damAsset.getPageSize())) {
						damAsset.setPageSize("A4");
						damDownloadDTO.setPageSize("A4");
					}
					boolean isPartnerDownloading = false;
					Integer loggedInUserCompanyId = loggedInCompany.getId();
					Integer assetCreatedByCompanyId = damAsset.getCompanyProfile().getId();
					if (loggedInUserCompanyId != null && assetCreatedByCompanyId != null
							&& !loggedInUserCompanyId.equals(assetCreatedByCompanyId)) {
						isPartnerDownloading = true;
						damDownloadDTO.setPartnerCompanyLogo(loggedInCompany.getCompanyLogoPath());
					}
					utilService.setTagsAndDownloadPDF(damDownloadDTO, damAsset.getAlias(), damAsset.getPageSize(),
							isPartnerDownloading, null, loggedInUserId);
					setAndUpdatePartnerProgress(learningTrackId, contentId, userId);
				}
				responseMessage = SUCCESS;
				responseStatusCode = 200;
			} else {
				responseMessage = UNAUTHORIZED;
				responseStatusCode = 401;
			}
		} else {
			responseMessage = INVALID_INPUT;
			responseStatusCode = 500;
		}
		response.setMessage(responseMessage);
		response.setStatusCode(responseStatusCode);
		return response;
	}

	private void setAndUpdatePartnerProgress(Integer learningTrackId, Integer contentId, Integer userId) {
		LearningTrackDto learningTrackDto = new LearningTrackDto();
		learningTrackDto.setId(learningTrackId);
		learningTrackDto.setUserId(userId);
		learningTrackDto.setContentId(contentId);
		learningTrackDto.setStatus(PartnerActivityType.DOWNLOADED);
		updatePartnerProgress(learningTrackDto);
	}

	@SuppressWarnings("unchecked")
	@Override
	public XtremandResponse getPartnerList(Pagination pagination) {
		XtremandResponse response = new XtremandResponse();
		String responseMessage = FAILED;
		Integer responseStatusCode = 400;
		if (pagination != null && pagination.getUserId() != null && pagination.getUserId() > 0) {
			CompanyProfile loggedInCompany = userService.getCompanyProfileByUser(pagination.getUserId());
			if (loggedInCompany != null) {
				pagination.setCompanyId(loggedInCompany.getId());
				String companyType = utilService.getCompanyType(loggedInCompany.getId());
				if (companyType != null && !companyType.equals(PARTNER)) {
					Map<String, Object> resultMap = partnershipDAO.getApprovedPartnershipsByVendorCompany(pagination);
					List<Partnership> partnerships = (List<Partnership>) resultMap.get("data");
					if (partnerships != null && !partnerships.isEmpty()) {
						List<PartnershipDTO> partnershipDtolist = new ArrayList<>();
						for (Partnership partnership : partnerships) {
							if (partnership != null && partnership.getPartnerCompany() != null) {
								PartnershipDTO partnershipDto = new PartnershipDTO();
								partnershipDto.setId(partnership.getId());
								partnershipDto.setPartnerCompanyName(partnership.getPartnerCompany().getCompanyName());
								partnershipDto.setPartnerCompanyId(partnership.getPartnerCompany().getId());
								partnershipDto
										.setPartnerCompanyLogo(partnership.getPartnerCompany().getCompanyLogoPath());

								User representingPartner = partnership.getRepresentingPartner();
								partnershipDto.setPartnerEmailId(representingPartner.getEmailId());
								partnershipDto.setPartnerFullName(getCustomerFullName(representingPartner));
								partnershipDtolist.add(partnershipDto);
							}
						}
						resultMap.put("data", partnershipDtolist);
						response.setData(resultMap);
						responseMessage = SUCCESS;
						responseStatusCode = 200;
					}
				} else {
					responseMessage = UNAUTHORIZED;
					responseStatusCode = 401;
				}
			} else {
				responseMessage = UNAUTHORIZED;
				responseStatusCode = 401;
			}
		} else {
			responseMessage = INVALID_INPUT;
			responseStatusCode = 500;
		}
		response.setMessage(responseMessage);
		response.setStatusCode(responseStatusCode);
		return response;
	}

	private LearningTrack saveAsPlaybook(LearningTrack learningTrack, CompanyProfile loggedInCompany,
			Integer loggedInUserId) {
		LearningTrack playbook = new LearningTrack();
		playbook.setType(LearningTrackType.PLAYBOOK);
		playbook.setTitle(XamplifyUtils.removeExtraSpace(learningTrack.getTitle()));
		playbook.setDescription(learningTrack.getDescription());
		playbook.setCompany(loggedInCompany);
		playbook.setFollowAssetSequence(false);
		playbook.setQuiz(null);
		playbook.setPublished(false);
		playbook.setCreatedBy(loggedInUserId);
		playbook.initialiseCommonFields(true, loggedInUserId);
		playbook.setAddedToQuickLinks(learningTrack.isAddedToQuickLinks());
		playbook.setTitle(XamplifyUtils.removeExtraSpace(learningTrack.getTitle()));
		LearningTrack exPlaybook = lmsDAO.getLearningTrackByTitle(learningTrack.getTitle(), loggedInCompany.getId(),
				LearningTrackType.PLAYBOOK);
		if (exPlaybook != null) {
			playbook.setTitle(XamplifyUtils.removeExtraSpace(playbook.getTitle() + "_" + System.currentTimeMillis()));
		}

		playbook.setSlug(learningTrack.getSlug());
		exPlaybook = lmsDAO.getLearningTrackBySlug(learningTrack.getSlug(), loggedInCompany.getId(),
				LearningTrackType.PLAYBOOK);
		if (exPlaybook != null) {
			playbook.setSlug(playbook.getSlug() + "_" + System.currentTimeMillis());
		}
		playbook.setGroupByAssets(learningTrack.isGroupByAssets());

		/** XNFR-824 **/
		setLearningTrackApprovalStatus(loggedInUserId, loggedInCompany.getId(), playbook);

		genericDAO.save(playbook);

		// Contents
		if (learningTrack.getContents() != null && !learningTrack.getContents().isEmpty()) {
			for (LearningTrackContent content : learningTrack.getContents()) {
				if (content.getDam() != null) {
					saveContent(playbook, content.getDam(), null, loggedInUserId, content.getDisplayIndex());
				}
			}
		}
		// Companies & Groups
		if (learningTrack.getVisibilityUsers() != null && !learningTrack.getVisibilityUsers().isEmpty()) {
			for (LearningTrackVisibility ltUser : learningTrack.getVisibilityUsers()) {
				LearningTrackVisibility visibilityUser = saveVisibilityUser(loggedInUserId, ltUser.getPartnership(),
						playbook, ltUser.getUser(), null, ltUser.isAssociatedThroughCompany());
				if (visibilityUser != null && ltUser.getGroups() != null && !ltUser.getGroups().isEmpty()) {
					for (LearningTrackVisibilityGroup visibilityGroup : ltUser.getGroups()) {
						if (visibilityGroup != null) {
							saveVisibilityGroup(visibilityGroup.getUserList(), visibilityUser, loggedInUserId);
						}
					}
				}
			}
		}
		// Tags
		if (learningTrack.getTags() != null && !learningTrack.getTags().isEmpty()) {
			for (LearningTrackTag tag : learningTrack.getTags()) {
				saveTag(loggedInUserId, playbook, tag.getTag());
			}
		}
		// Category
		CategoryModule categoryModule = categoryDao.getCategoryModuleByLearningTrackId(learningTrack.getId());
		if (categoryModule != null && categoryModule.getCategoryId() != null && categoryModule.getCategoryId() > 0) {
			saveCategory(categoryModule.getCategoryId(), loggedInUserId, playbook, LearningTrackType.PLAYBOOK);
		}

		/** XNFR-824 **/
		commentDao.createApprovalStatusHistory(playbook.getId(), loggedInUserId, ModuleType.PLAYBOOK);

		return playbook;
	}

	@Override
	public XtremandResponse saveAsPlaybook(LearningTrackDto learningTrackDto) {
		XtremandResponse response = new XtremandResponse();
		String responseMessage = FAILED;
		Integer responseStatusCode = 400;
		if (learningTrackDto.getId() != null && learningTrackDto.getId() > 0 && learningTrackDto.getUserId() != null
				&& learningTrackDto.getUserId() > 0) {
			CompanyProfile loggedInCompany = userService.getCompanyProfileByUser(learningTrackDto.getUserId());
			if (loggedInCompany != null) {
				LearningTrack learningTrack = genericDAO.get(LearningTrack.class, learningTrackDto.getId());
				if (learningTrack.getCompany().getId() == loggedInCompany.getId()) {
					LearningTrack playbook = saveAsPlaybook(learningTrack, loggedInCompany,
							learningTrackDto.getUserId());
					if (playbook != null) {
						String fileName = learningTrack.getFeaturedImage();
						if (!StringUtils.isBlank(fileName)) {
							String destinationFileName = playbook.getId() + "."
									+ fileName.substring(fileName.lastIndexOf('.') + 1);
							String sourceFileName = fileName.substring(fileName.lastIndexOf('/') + 1);
							String filePath = amazonWebService.copyLMSFeaturedImage(sourceFileName, destinationFileName,
									loggedInCompany.getId());
							if (!StringUtils.isBlank(filePath)) {
								playbook.setFeaturedImage(filePath);
							}
						}
						response.setData(getLearningTrackDto(playbook, learningTrackDto.getUserId()));
					}
					responseMessage = SUCCESS;
					responseStatusCode = 200;
				} else {
					responseMessage = UNAUTHORIZED;
					responseStatusCode = 401;
				}
			} else {
				responseMessage = UNAUTHORIZED;
				responseStatusCode = 401;
			}
		} else {
			responseMessage = INVALID_INPUT;
			responseStatusCode = 500;
		}
		response.setMessage(responseMessage);
		response.setStatusCode(responseStatusCode);
		return response;
	}

	@Override
	public XtremandResponse validateTitle(LearningTrackDto learningTrackDto) {
		XtremandResponse response = new XtremandResponse();
		String responseMessage = FAILED;
		Integer responseStatusCode = 400;
		boolean isValid = false;
		if (learningTrackDto != null && learningTrackDto.getUserId() != null && learningTrackDto.getUserId() > 0
				&& StringUtils.isNotBlank(learningTrackDto.getTitle())) {
			CompanyProfile loggedInCompany = userService.getCompanyProfileByUser(learningTrackDto.getUserId());
			if (loggedInCompany != null) {
				String companyType = utilService.getCompanyType(loggedInCompany.getId());
				if (companyType != null && !companyType.equals(PARTNER)) {
					if (learningTrackDto.getType() == null) {
						learningTrackDto.setType(LearningTrackType.TRACK);
					}
					LearningTrack learningTrack = lmsDAO.getLearningTrackByTitle(learningTrackDto.getTitle(),
							loggedInCompany.getId(), learningTrackDto.getType());
					if (learningTrack == null) {
						isValid = true;
					}
					responseMessage = SUCCESS;
					responseStatusCode = 200;
				} else {
					responseMessage = UNAUTHORIZED;
					responseStatusCode = 401;
				}
			} else {
				responseMessage = UNAUTHORIZED;
				responseStatusCode = 401;
			}
		} else {
			responseMessage = INVALID_INPUT;
			responseStatusCode = 500;
		}
		response.setData(isValid);
		response.setMessage(responseMessage);
		response.setStatusCode(responseStatusCode);
		return response;
	}

	@Override
	public XtremandResponse getPartnerQuizAnalytics(LearningTrackDto learningTrackDto) {
		XtremandResponse response = new XtremandResponse();
		String responseMessage = FAILED;
		Integer responseStatusCode = 400;
		if (learningTrackDto != null && learningTrackDto.getId() != null && learningTrackDto.getId() > 0
				&& learningTrackDto.getQuizId() != null && learningTrackDto.getQuizId() > 0
				&& learningTrackDto.getPartnershipId() != null && learningTrackDto.getPartnershipId() > 0) {
			Integer loggedInUserId = learningTrackDto.getUserId();
			CompanyProfile loggedInCompany = userService.getCompanyProfileByUser(loggedInUserId);
			if (loggedInCompany != null) {
				LearningTrack learningTrack = genericDAO.get(LearningTrack.class, learningTrackDto.getId());
				if (canViewAnalytics(learningTrack, loggedInUserId, loggedInCompany)
						|| checkVisibility(learningTrack, loggedInCompany.getId(), loggedInUserId)) {
					FormSubmit formSubmit = formSubmitDao.getFormSubmitByTrackId(learningTrackDto.getPartnershipId(),
							learningTrackDto.getId(), learningTrackDto.getQuizId());
					if (formSubmit != null && formSubmit.getId() != null && formSubmit.getId() > 0) {
						FormSubmitAnswerDTO dto = new FormSubmitAnswerDTO();
						Map<Integer, FormSubmitAnswerDTO> data = formSubmitService
								.getQuizFormSubmittedData(formSubmit.getId());
						dto.setSubmittedData(data);
						dto.setScore(formSubmit.getScore());
						dto.setMaxScore(formSubmit.getForm().getMaxScore());
						response.setData(dto);
						responseMessage = SUCCESS;
						responseStatusCode = 200;
					} else {
						responseMessage = RECORD_NOT_FOUND;
						responseStatusCode = 404;
					}
				} else {
					responseMessage = UNAUTHORIZED;
					responseStatusCode = 403;
				}
			} else {
				responseMessage = UNAUTHORIZED;
				responseStatusCode = 401;
			}
		} else {
			responseMessage = INVALID_INPUT;
			responseStatusCode = 500;
		}
		response.setMessage(responseMessage);
		response.setStatusCode(responseStatusCode);
		return response;
	}

	@Override
	public XtremandResponse addExistingQuiz() {
		try {
			XtremandResponse response = new XtremandResponse();
			List<LearningTrack> learningTrackList = lmsDAO.getAllTracksWithQuiz();
			// Integer learningTrackId = 1493;
			// List<LearningTrack> learningTrackList =
			// lmsDAO.getAllTracksWithQuizByTrackID(learningTrackId);
			List<LearningTrackContent> learningTrackContentList = new ArrayList<>();
			List<LearningTrackPartnerActivity> partnerActivityList = new ArrayList<>();
			if (learningTrackList != null && !learningTrackList.isEmpty() && learningTrackList.size() > 0) {
				logger.info("TOTAL TRACKS COUNT ---- " + learningTrackList.size());
				int count = 0;
				int activityCount = 0;
				for (LearningTrack learningTrack : learningTrackList) {
					if (learningTrack != null && learningTrack.getId() > 0) {
						logger.info("TRACK ID---" + learningTrack.getId());
						/* Moving quiz to content table */
						LearningTrackContent learningTrackContent = new LearningTrackContent();
						Integer maxDisplayIndex = lmsDAO.getMaxDisplayIndex(learningTrack.getId());
						if (maxDisplayIndex == null) {
							maxDisplayIndex = 0;
						}
						learningTrackContent.setQuiz(learningTrack.getQuiz());
						learningTrackContent.setLearningTrack(learningTrack);
						learningTrackContent.setDisplayIndex(maxDisplayIndex + 1);
						learningTrackContent.setCreatedBy(learningTrack.getCreatedBy());
						learningTrackContent.setCreatedTime(learningTrack.getCreatedTime());
						learningTrackContent.setUpdatedTime(learningTrack.getUpdatedTime());
						Set<LearningTrackVisibility> visibilityUsers = learningTrack.getVisibilityUsers();
						if (visibilityUsers != null && !visibilityUsers.isEmpty() && visibilityUsers.size() > 0) {
							for (LearningTrackVisibility learningTrackVisibility : visibilityUsers) {
								if (learningTrackVisibility.getUser() != null && learningTrack.getQuiz() != null) {
									FormSubmit formSubmit = formDao.getLearningTrackFormSubmissionByFormID(
											learningTrack.getId(), learningTrackVisibility.getUser().getUserId(),
											learningTrack.getQuiz().getId());
									if (formSubmit != null) {
										LearningTrackPartnerActivity partnerActivity = new LearningTrackPartnerActivity();
										partnerActivity.setLearningTrackContent(learningTrackContent);
										partnerActivity.setVisibilityUser(learningTrackVisibility);
										partnerActivity.setType(PartnerActivityType.SUBMITTED);
										partnerActivity.setCreatedBy(learningTrackVisibility.getUser().getUserId());
										partnerActivity.setUpdatedBy(learningTrackVisibility.getUser().getUserId());
										partnerActivity.setCreatedTime(formSubmit.getSubmittedOn());
										partnerActivity.setUpdatedTime(formSubmit.getSubmittedOn());
										partnerActivityList.add(partnerActivity);
										activityCount++;
										logger.info("ACTIVITY COUNT ---- " + activityCount + "--- ACTIVITY USER ID---"
												+ learningTrackVisibility.getUser());
									}
								}
							}
						}
						learningTrackContentList.add(learningTrackContent);
						count++;
						logger.info("TRACK COUNT ---- " + count);
					}
				}
				logger.info("TOTAL CONTENT COUNT ---- " + count);
				lmsDAO.saveAllContent(learningTrackContentList);
				lmsDAO.saveAllPartnerActivity(partnerActivityList);
				logger.info("TOTAL CONTENT LIST COUNT ---- " + learningTrackContentList.size());
				logger.info("TOTAL ACTIVITY LIST COUNT ---- " + partnerActivityList.size());
				response.setMessage(
						"SUCCESSFULL ----- track count -- " + count + " ------ activityCount---- " + activityCount);
			}
			return response;
		} catch (ResponseUtilException e) {
			throw new ResponseUtilException(e.getMessage());
		}

	}

	/**** XNFR-342 ****/
	@Override
	public XtremandResponse findAllUnPublishedTracksOrPlayBooks(Pageable pageable, BindingResult result,
			Integer loggedInUserId, Integer userListId, Integer userListUserId, String type) {
		XtremandResponse response = new XtremandResponse();
		Integer companyId = userDao.getCompanyIdByUserId(loggedInUserId);
		Pagination pagination = utilService.setPageableParameters(pageable, loggedInUserId);
		List<Integer> trackOrPlayBookIds = lmsDAO.findUnPublishedTrackOrPlayBookIdsByCompanyId(companyId, type);
		pagination.setCompanyId(companyId);
		addPublishedTrackOrPlayBookIds(userListId, userListUserId, trackOrPlayBookIds);
		pagination.setIds(trackOrPlayBookIds);
		if (XamplifyUtils.isNotEmptyList(trackOrPlayBookIds)) {
			response.setData(lmsDAO.findAllUnPublishedAndFilteredPublishedTracksOrPlayBooks(pagination,
					pageable.getSearch(), type));
		} else {
			response.setData(
					paginationUtil.returnEmptyList(new HashMap<>(), new ArrayList<ShareLearningTrackResponseDTO>()));
		}
		XamplifyUtils.addSuccessStatus(response);
		return response;
	}

	private void addPublishedTrackOrPlayBookIds(Integer userListId, Integer userListUserId,
			List<Integer> trackOrPlayBookIds) {
		List<PublishedContentIdAndUserListIdDetailsDTO> publishedTrackOrPlayBookDetails = lmsDAO
				.findAllPublishedTrackOrPlayBooksByUserListId(userListId);
		if (XamplifyUtils.isValidInteger(userListId)) {
			List<Integer> userListIds = new ArrayList<>();
			userListIds.add(userListId);
			List<UserListAndUserId> userListAndUserIdDtos = userListDao.findUserIdsAndUserListIds(userListIds);
			List<Integer> deactivatedPartners = utilDao
					.findDeactivedPartnersByCompanyId(userListAndUserIdDtos.get(0).getCompanyId());
			userListAndUserIdDtos.removeIf(user -> deactivatedPartners.contains(user.getUserId()));
			Integer usersCount = userListAndUserIdDtos.size();
			for (PublishedContentIdAndUserListIdDetailsDTO publishedContentIdAndUserListIdDetailsDTO : publishedTrackOrPlayBookDetails) {
				List<Integer> publishedUserListUserIds = publishedContentIdAndUserListIdDetailsDTO.getUserIds();
				boolean isTrackOrPlayBookPublishedWithPartnerList = publishedUserListUserIds != null
						&& !publishedUserListUserIds.isEmpty() && usersCount.equals(publishedUserListUserIds.size());
				Integer publishedTrackOrPlayBookId = publishedContentIdAndUserListIdDetailsDTO.getId();
				boolean isUnPublishedTrackOrPlayBook = publishedUserListUserIds != null
						&& !publishedUserListUserIds.isEmpty() && publishedUserListUserIds.indexOf(userListUserId) < 0;
				if (isUnPublishedTrackOrPlayBook && !isTrackOrPlayBookPublishedWithPartnerList) {
					trackOrPlayBookIds.add(publishedTrackOrPlayBookId);
				}
			}
		} else {
			if (XamplifyUtils.isNotEmptyList(publishedTrackOrPlayBookDetails)) {
				trackOrPlayBookIds.addAll(publishedTrackOrPlayBookDetails.stream()
						.map(PublishedContentIdAndUserListIdDetailsDTO::getId).collect(Collectors.toList()));
			}
		}
	}

	/**** XNFR-342 ****/
	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public XtremandResponse shareSelectedTracksOrPlayBooks(ShareContentRequestDTO shareContentRequestDTO, String type) {
		if (LearningTrackType.TRACK.name().equals(type)) {
			boolean lmsAccess = utilDao.hasLmsAccessByUserId(shareContentRequestDTO.getLoggedInUserId());
			if (lmsAccess) {
				lmsDAO.updateIsPublishedToPartnerListByIds(shareContentRequestDTO.getTrackOrPlaybookIds(), true);
				asyncComponent.publishLmsToNewlyAddedPartners(shareContentRequestDTO, lmsAccess);
			}
		} else if (LearningTrackType.PLAYBOOK.name().equals(type)) {
			boolean playbookAccess = utilDao.hasPlaybookAccessByUserId(shareContentRequestDTO.getLoggedInUserId());
			if (playbookAccess) {
				lmsDAO.updateIsPublishedToPartnerListByIds(shareContentRequestDTO.getTrackOrPlaybookIds(), true);
				asyncComponent.publishPlayBooksToNewlyAddedPartners(shareContentRequestDTO, playbookAccess);
			}
		}
		XtremandResponse response = new XtremandResponse();
		response.setStatusCode(200);
		response.setAccess(true);
		if (LearningTrackType.TRACK.name().equals(type)) {
			response.setMessage("Track(s) Shared Successfully.");
		} else if (LearningTrackType.PLAYBOOK.name().equals(type)) {
			response.setMessage("Play Book(s) Shared Successfully.");
		}
		return response;
	}

	/**** XNFR-342 ****/
	@Override
	public XtremandResponse findPublishedPartnerIdsByUserListIdAndId(Integer userListId, Integer id) {
		XtremandResponse response = new XtremandResponse();
		List<Integer> partnerIds = lmsDAO.findPublishedPartnerIdsByUserListIdAndId(userListId, id);
		if (XamplifyUtils.isNotEmptyList(partnerIds)) {
			response.setData(partnerIds);
		} else {
			response.setData(new ArrayList<Integer>());
		}
		Set<Integer> publishedPartnershipIds = new HashSet<>();
		publishedPartnershipIds.addAll(lmsDAO.findPublishedPartnershipIds(id));
		Map<String, Object> map = new HashMap<>();
		map.put("partnershipIds", publishedPartnershipIds);
		response.setMap(map);
		return response;
	}

	@SuppressWarnings("unchecked")
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public XtremandResponse updateLmsPublishedValues() {
		XtremandResponse response = new XtremandResponse();
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString("select id from xt_learning_track where is_published");
		List<Integer> trackOrPlayBookIds = (List<Integer>) hibernateSQLQueryResultUtilDao
				.returnList(hibernateSQLQueryResultRequestDTO);
		int totalRecordsCounter = 0;
		if (XamplifyUtils.isNotEmptyList(trackOrPlayBookIds)) {
			List<LearningTrack> learningTracks = lmsDAO
					.findByIds(XamplifyUtils.convertListToSetElements(trackOrPlayBookIds));
			int totalLearningTracks = learningTracks.size();
			int learningTracksCounter = 0;
			int leftTracks = 0;
			for (LearningTrack learningTrack : learningTracks) {
				Set<LearningTrackVisibility> learningTrackVisibilities = learningTrack.getVisibilityUsers();
				int totalVisibilityRows = learningTrackVisibilities.size();
				int visibilityCounter = 0;
				for (LearningTrackVisibility learningTrackVisibility : learningTrackVisibilities) {
					learningTrackVisibility.setPublished(true);
					learningTrackVisibility.setPublishedOn(learningTrackVisibility.getCreatedTime());
					visibilityCounter++;
					int leftVisibilityItems = totalVisibilityRows - visibilityCounter;
					String debugMessage = "Visibility Items Left:" + leftVisibilityItems
							+ " & Learning Tracks Items Left:" + leftTracks;
					logger.debug(debugMessage);
					totalRecordsCounter++;
				}
				learningTracksCounter++;
				leftTracks = totalLearningTracks - learningTracksCounter;
				String itemsLeftMessage = "Learning Tracks Items Left" + leftTracks;
				logger.debug(itemsLeftMessage);
			}
		}
		response.setMessage(totalRecordsCounter + " Records Updated Successfully");
		return response;
	}

	/** XNFR-745 **/
	@Override
	public XtremandResponse getGroupedAssetsBySlug(LearningTrackType type, Integer companyId, String slug,
			String sortKey) {
		XtremandResponse response = new XtremandResponse();

		if (!checkInputParametersAreValid(companyId, slug, sortKey)) {
			XamplifyUtils.addErorMessageWithStatusCode(response, "Invalid Parameters", 401);
		} else {
			PreviewPlaybookResponseDTO previewPlaybookResponseDTO = lmsDAO.getPlaybookBySlug(type.getType(), companyId,
					slug);
			if (previewPlaybookResponseDTO != null
					&& XamplifyUtils.isValidInteger(previewPlaybookResponseDTO.getId())) {
				List<PlaybookContentCategoryListDTO> playbookContentCategoryListDTOs = lmsDAO
						.getCategoryNamesWithDamIdsForPalybookById(previewPlaybookResponseDTO.getId(), sortKey);
				populateAssetsByCategory(previewPlaybookResponseDTO, playbookContentCategoryListDTOs);
			}
			response.setData(previewPlaybookResponseDTO);
			XamplifyUtils.addSuccessStatus(response);
		}
		return response;
	}

	private boolean checkInputParametersAreValid(Integer companyId, String slug, String sortKey) {
		return XamplifyUtils.isValidInteger(companyId) && XamplifyUtils.isValidString(slug)
				&& XamplifyUtils.isValidString(sortKey);
	}

	/** XNFR-745 **/
	private void populateAssetsByCategory(PreviewPlaybookResponseDTO previewPlaybookResponseDTO,
			List<PlaybookContentCategoryListDTO> playbookContentCategoryListDTOs) {

		if (playbookContentCategoryListDTOs != null && !playbookContentCategoryListDTOs.isEmpty()) {
			for (PlaybookContentCategoryListDTO playbookContentCategoryListDTO : playbookContentCategoryListDTOs) {
				List<Integer> damIds = XamplifyUtils
						.convertStringToIntegerArrayList(playbookContentCategoryListDTO.getDamIds());
				if (XamplifyUtils.isNotEmptyList(damIds)
						&& XamplifyUtils.isValidInteger(previewPlaybookResponseDTO.getId())) {
					List<PlaybookAssetResponseDTO> playbookAssetResponseDTO = lmsDAO
							.getAssetsWithCategoryIdForPlaybooksByDamId(damIds, serverPath,
									previewPlaybookResponseDTO.getId());
					addProxyToAssetPath(playbookAssetResponseDTO);
					for (PlaybookAssetResponseDTO playbookContent : playbookAssetResponseDTO) {
						List<String> partnerActivities = lmsDAO
								.getPartnerActivityType(playbookContent.getLearningTrackContentMappingId());
						for (String activityType : partnerActivities) {
							if (FINISHED_STATUS_LIST.contains(activityType)) {
								playbookContent.setFinished(true);
								break;
							} else if ("OPENED".equals(activityType)) {
								playbookContent.setOpened(true);
							}
						}
					}
					if (playbookAssetResponseDTO != null) {
						playbookContentCategoryListDTO.setDam(playbookAssetResponseDTO);
					}
				}
			}
			previewPlaybookResponseDTO.setContents(playbookContentCategoryListDTOs);
		}
	}

	private void addProxyToAssetPath(List<PlaybookAssetResponseDTO> playbookAssetResponseDTO) {
		List<String> availableImageFileTypes = fileUtil.getArrayList(imageFileTypes);
		List<String> availabletextFileTypes = fileUtil.getArrayList(contentPreviewForTextView);
		List<String> availableContentPreviewTypes = fileUtil.getArrayList(contentPreviewSupportedFileFormats);
		for (PlaybookAssetResponseDTO playbookAssetResponse : playbookAssetResponseDTO) {
			playbookAssetResponse
					.setTextFileType(availabletextFileTypes.contains(playbookAssetResponse.getAssetType()));
			playbookAssetResponse
					.setImageFileType(availableImageFileTypes.contains(playbookAssetResponse.getAssetType()));
			playbookAssetResponse
					.setContentPreviewType(availableContentPreviewTypes.contains(playbookAssetResponse.getAssetType()));
			if (XamplifyUtils.isValidString(playbookAssetResponse.getAssetPath())
					&& (playbookAssetResponse.getAssetType().equals("pdf")
							|| availableImageFileTypes.contains(playbookAssetResponse.getAssetType())
							|| availabletextFileTypes.contains(playbookAssetResponse.getAssetType())
							|| playbookAssetResponse.getAssetType().equals("csv")
							|| playbookAssetResponse.getAssetType().equals("mp3"))) {
				String encodedUrl = null;
				try {
					encodedUrl = URLEncoder.encode(playbookAssetResponse.getAssetPath(), StandardCharsets.UTF_8.name());
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
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
				playbookAssetResponse.setAssetProxyPath(proxyUrl);
//		        playbookAssetResponse.setAssetPath(proxyUrl);
			}
		}
	}

	/** XNFR-745 **/
	@Override
	public XtremandResponse checkGroupByAssetsEnabled(String type, Integer companyId, String slug) {
		XtremandResponse response = new XtremandResponse();
		boolean isGroupByAssetsEnabled = false;
		if (XamplifyUtils.isValidInteger(companyId) && XamplifyUtils.isValidString(slug)) {
			isGroupByAssetsEnabled = lmsDAO.checkGroupByAssetsEnabled(type, companyId, slug);
			XamplifyUtils.addSuccessStatus(response);
		} else {
			XamplifyUtils.addErorMessageWithStatusCode(response, "Invalid Parameters", 401);
		}
		response.setData(isGroupByAssetsEnabled);
		return response;
	}

	/******** XNFR-750 ****/
	@Override
	public XtremandResponse publishTrackOrPlayBookToPartnerCompany(Integer userListId, Integer partnerUserId,
			Integer trackOrPlayBookId, Integer loggedInUserId) {
		XtremandResponse xtremandResponse = new XtremandResponse();
		Integer vendorCompanyId = userListDao.getCompanyIdByUserListId(userListId);
		Integer partnershipId = partnershipDAO.getPartnershipIdByPartnerCompanyUserId(partnerUserId, vendorCompanyId);
		if (XamplifyUtils.isValidInteger(partnershipId)) {
			boolean rowExistsInLearningTrackVisibility = lmsDAO.isRowPresentInLearningTrackVisiblity(trackOrPlayBookId,
					partnershipId);
			if (rowExistsInLearningTrackVisibility) {
				lmsDAO.updatePublishedStatusByLearningTrackIdAndPartnershipId(trackOrPlayBookId, partnershipId);
				XamplifyUtils.addSuccessStatusWithMessage(xtremandResponse, "Published Successfully");
			} else {
				LearningTrack trackOrPlayBook = lmsDAO.findById(trackOrPlayBookId);
				contentService.processAndSavePartnerVisibility(userListId, loggedInUserId, partnerUserId, partnershipId,
						trackOrPlayBook);
				XamplifyUtils.addSuccessStatusWithMessage(xtremandResponse, "Published Successfully");
			}
		} else {
			return addErrorResponseAndLogTheError(userListId, partnerUserId, trackOrPlayBookId, loggedInUserId);
		}
		return xtremandResponse;
	}

	private void saveLearningTrackVisbilityGroup(Integer userListId, Integer trackOrPlayBookId, Integer loggedInUserId,
			Integer partnershipId) {
		List<Integer> visibilityIds = lmsDAO.findVisiblityIds(trackOrPlayBookId, partnershipId);
		if (XamplifyUtils.isNotEmptyList(visibilityIds)) {
			for (Integer visbilityId : visibilityIds) {
				LearningTrackVisibilityGroup learningTrackVisibilityGroup = new LearningTrackVisibilityGroup();
				LearningTrackVisibility learningTrackVisibility = new LearningTrackVisibility();
				learningTrackVisibility.setId(visbilityId);
				learningTrackVisibilityGroup.setLearningTrackVisibility(learningTrackVisibility);
				UserList userList = new UserList();
				userList.setId(userListId);
				learningTrackVisibilityGroup.setUserList(userList);
				learningTrackVisibilityGroup.setCreatedBy(loggedInUserId);
				learningTrackVisibilityGroup.setUpdatedBy(loggedInUserId);
				learningTrackVisibilityGroup.setCreatedTime(new Date());
				learningTrackVisibilityGroup.setUpdatedTime(new Date());
				genericDAO.save(learningTrackVisibilityGroup);
			}
		}
	}

	private XtremandResponse addErrorResponseAndLogTheError(Integer userListId, Integer partnerUserId,
			Integer trackOrPlayBookId, Integer loggedInUserId) {
		String errorMessage = "User List Id : " + userListId + " Partner User Id : " + partnerUserId
				+ " Track/PlayBook Id : " + trackOrPlayBookId + " Logged In User Id : " + loggedInUserId;
		logger.error(
				"Unable to publish track / playbook for the below details.The partnership does not exits. {} at {}",
				errorMessage, new Date());
		return XamplifyUtils.buildErrorResponse(400, "Partnership does not exits.Please contact admin.");
	}

	@Override
	public XtremandResponse addPartnerGroup(Integer userListId, Integer partnershipId, Integer trackOrPlayBookId,
			Integer loggedInUserId) {
		XtremandResponse xtremandResponse = new XtremandResponse();
		saveLearningTrackVisbilityGroup(userListId, trackOrPlayBookId, loggedInUserId, partnershipId);
		XamplifyUtils.addSuccessStatusWithMessage(xtremandResponse, "Group Added Successfully.");
		return xtremandResponse;
	}

	/** XNFR-824 **/
	private void setLearningTrackApprovalStatus(Integer loggedInUserId, Integer companyId,
			LearningTrack learningTrack) {
		if (XamplifyUtils.isValidInteger(loggedInUserId) && XamplifyUtils.isValidInteger(companyId)) {
			boolean isApprovalRequiredForTracks = userDao.checkIfTracksApprovalRequiredByCompanyId(companyId);
			boolean isApprovalRequiredForPlaybooks = userDao.checkIfPlaybooksApprovalRequiredByCompanyId(companyId);
			boolean isApprovalPrivilegeManager = approveDao.isApprovalPrivilegeManager(loggedInUserId);
			boolean isTrackApprover = approveDao.checkIsTrackApproverByTeamMemberIdAndCompanyId(loggedInUserId,
					companyId);
			boolean isPlaybookApprover = approveDao.checkIsPlaybookApproverByTeamMemberIdAndCompanyId(loggedInUserId,
					companyId);

			if (isApprovalPrivilegeManager
					|| (LearningTrackType.TRACK.equals(learningTrack.getType())
							&& ((!isApprovalRequiredForTracks) || (isTrackApprover)))
					|| (LearningTrackType.PLAYBOOK.equals(learningTrack.getType())
							&& ((!isApprovalRequiredForPlaybooks) || (isPlaybookApprover)))) {
				learningTrack.setApprovalStatus(ApprovalStatusType.APPROVED);
			} else {
				learningTrack.setApprovalStatus(ApprovalStatusType.CREATED);
			}
			learningTrack.setApprovalStatusUpdatedBy(loggedInUserId);
			learningTrack.setApprovalStatusUpdatedTime(new Date());
		}
	}

	@Override
	public XtremandResponse getPreviewBySlug(LearningTrackType type, Integer companyId, String slug,
			Integer loggedInUserId) {
		XtremandResponse response = new XtremandResponse();
		String responseMessage;
		Integer responseStatusCode;
		if (XamplifyUtils.isValidInteger(loggedInUserId) && XamplifyUtils.isValidString(slug)) {
			CompanyProfile loggedInCompany = userService.getCompanyProfileByUser(loggedInUserId);
			if (loggedInCompany != null) {
				LearningTrack learningTrack = lmsDAO.getLearningTrackBySlug(slug, companyId, type);
				if (canViewLearningTrack(learningTrack, loggedInUserId, loggedInCompany)) {
					response.setData(getLearningTrackDtoInDetail(learningTrack, loggedInUserId, false));
					responseMessage = SUCCESS;
					responseStatusCode = 200;
				} else {
					responseMessage = UNAUTHORIZED;
					responseStatusCode = 403;
				}
			} else {
				responseMessage = UNAUTHORIZED;
				responseStatusCode = 401;
			}
		} else {
			responseMessage = INVALID_INPUT;
			responseStatusCode = 500;
		}
		response.setMessage(responseMessage);
		response.setStatusCode(responseStatusCode);
		return response;
	}

	private void setContentsForPlaybookAndTracks(LearningTrack learningTrack, LearningTrackDto learningTrackDto,
			Integer loggedInUserId) {
		Integer learningTrackId = learningTrack.getId();
		List<LearningTrackContentDto> trackContentDtos = lmsDAO.getTrackContentByLearningTrackId(learningTrackId);

		if (!XamplifyUtils.isNotEmptyList(trackContentDtos)) {
			learningTrackDto.setContents(Collections.emptyList());
			return;
		}

		List<LearningTrackContentDto> contentDtoList = new ArrayList<>();
		for (LearningTrackContentDto contentDto : trackContentDtos) {
			if (XamplifyUtils.isValidInteger(contentDto.getDamId())) {
				processAssetContent(contentDto, learningTrack, learningTrackId);
			} else if (XamplifyUtils.isValidInteger(contentDto.getQuizId())) {
				processQuizContent(contentDto, learningTrackId, loggedInUserId);
			}
			contentDtoList.add(contentDto);
		}

		learningTrackDto.setContents(contentDtoList);
	}

	private void processAssetContent(LearningTrackContentDto contentDto, LearningTrack learningTrack,
			Integer learningTrackId) {
		DamListDTO damDto = lmsDAO.getAssetDetailsByDamIdAndTrackId(contentDto.getDamId(), serverPath, learningTrackId);
		addProxyToPDFAssetPath(damDto);
		contentDto.setDam(damDto);
		contentDto.setTypeQuizId(false);
		contentDto.setOpened(false);

		if (LearningTrackType.TRACK == learningTrack.getType()) {
			List<String> partnerActivities = lmsDAO.getPartnerActivityType(contentDto.getId());
			if (!XamplifyUtils.isNotEmptyList(partnerActivities))
				return;

			for (String activityType : partnerActivities) {
				if (FINISHED_STATUS_LIST.contains(activityType)) {
					contentDto.setFinished(true);
					break;
				} else if ("OPENED".equals(activityType)) {
					contentDto.setOpened(true);
				}
			}
		}
		contentDto.setId(damDto.getId());
	}

	private void processQuizContent(LearningTrackContentDto contentDto, Integer learningTrackId,
			Integer loggedInUserId) {
		Integer quizId = contentDto.getQuizId();
		FormDTO quiz = lmsDAO.findQuizDetailsByQuizId(quizId);
		quiz.setSelected(true);

		contentDto.setQuiz(quiz);
		contentDto.setTypeQuizId(true);

		List<String> partnerActivities = lmsDAO.getPartnerActivityType(contentDto.getId());
		if (XamplifyUtils.isNotEmptyList(partnerActivities)) {
			boolean isFinished = lmsDAO.getLearningTrackFormSubmissionByFormID(learningTrackId, loggedInUserId, quizId);
			contentDto.setFinished(isFinished);
		} else {
			contentDto.setFinished(false);
		}
		contentDto.setId(quiz.getId());
	}

	private void addProxyToPDFAssetPath(DamListDTO damDto) {
		List<String> availableImageFileTypes = fileUtil.getArrayList(imageFileTypes);
		List<String> availabletextFileTypes = fileUtil.getArrayList(contentPreviewForTextView);
		List<String> availableContentPreviewTypes = fileUtil.getArrayList(contentPreviewSupportedFileFormats);
		damDto.setTextFileType(availabletextFileTypes.contains(damDto.getAssetType()));
		damDto.setImageFileType(availableImageFileTypes.contains(damDto.getAssetType()));
		damDto.setContentPreviewType(availableContentPreviewTypes.contains(damDto.getAssetType()));
		if (XamplifyUtils.isValidString(damDto.getAssetPath())
				&& (damDto.getAssetType().equals("pdf") || availableImageFileTypes.contains(damDto.getAssetType())
						|| availabletextFileTypes.contains(damDto.getAssetType()) || damDto.getAssetType().equals("csv")
						|| damDto.getAssetType().equals("mp3"))) {
			String encodedUrl = null;
			try {
				encodedUrl = URLEncoder.encode(damDto.getAssetPath(), StandardCharsets.UTF_8.name());
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
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
//			damDto.setAssetPath(proxyUrl);
		}
	}

	// XNFR-1032
	public XtremandResponse getContentCounts(Integer loggedInUserId, String vendorCompanyProfileName) {
		XtremandResponse response = new XtremandResponse();
		response.setStatusCode(400);
		Integer vendorCompanyId = 0;
		boolean hasAccesssToPartner = false;
		if (XamplifyUtils.isValidInteger(loggedInUserId)) {
			if (XamplifyUtils.isValidString(vendorCompanyProfileName)) {
				vendorCompanyId = userDao.getCompanyIdByProfileName(vendorCompanyProfileName);
			}

			Integer companyId = userDao.getCompanyIdByUserId(loggedInUserId);
			if (companyId != null && companyId > 0) {
				Map<String, Object> map = lmsDAO.getContentCounts(companyId, loggedInUserId, vendorCompanyId,
						hasAccesssToPartner);
				response.setMap(map);
				response.setStatusCode(200);
			}
		}
		return response;
	}

	@Override
	public XtremandResponse getManageContentCounts(Integer loggedInUserId, String moduleContentType) {
		XtremandResponse response = new XtremandResponse();
		response.setStatusCode(400);
		if (XamplifyUtils.isValidInteger(loggedInUserId)) {
			Integer companyId = userDao.getCompanyIdByUserId(loggedInUserId);
			if (XamplifyUtils.isValidInteger(companyId)) {
				Map<String, Object> map = lmsDAO.getManageContentCounts(companyId, loggedInUserId, moduleContentType);
				response.setMap(map);
				response.setStatusCode(200);
			}
		}
		return response;
	}

	@Override
	public XtremandResponse getManageSharedContentCounts(Integer loggedInUserId, String moduleContentType,
			String vendorCompanyProfileName) {
		XtremandResponse response = new XtremandResponse();
		response.setStatusCode(400);
		Integer vendorCompanyId = 0;
		if (XamplifyUtils.isValidInteger(loggedInUserId)) {
			if (XamplifyUtils.isValidString(vendorCompanyProfileName)) {
				vendorCompanyId = userDao.getCompanyIdByProfileName(vendorCompanyProfileName);
			}
			Integer companyId = userDao.getCompanyIdByUserId(loggedInUserId);
			if (companyId != null && companyId > 0) {
				Map<String, Object> map = lmsDAO.getManageSharedContentCounts(companyId, loggedInUserId,
						moduleContentType, vendorCompanyId);
				response.setMap(map);
				response.setStatusCode(200);
			}
		}
		return response;
	}

	@Override
	public XtremandResponse checkAWSCredentials() {
		XtremandResponse response = new XtremandResponse();
		boolean isValidAWSCredentials = amazonWebService.validateAWSCredentials();
		response.setData(isValidAWSCredentials);
		XamplifyUtils.addSuccessStatus(response);
		return response;
	}

}
