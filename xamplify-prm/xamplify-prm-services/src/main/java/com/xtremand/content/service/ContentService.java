package com.xtremand.content.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.sql.JoinType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.xtremand.dam.bom.Dam;
import com.xtremand.dam.bom.DamPartner;
import com.xtremand.dam.bom.DamPartnerGroupMapping;
import com.xtremand.dam.bom.DamPartnerGroupUserMapping;
import com.xtremand.dam.dao.DamDao;
import com.xtremand.dam.dto.DamPostDTO;
import com.xtremand.dam.dto.DamUploadPostDTO;
import com.xtremand.dashboard.button.dao.DashboardButtonDao;
import com.xtremand.dashboard.buttons.bom.DashboardButton;
import com.xtremand.dashboard.buttons.bom.DashboardButtonsPartnerGroupMapping;
import com.xtremand.dashboard.buttons.dto.PublishedDashboardButtonDetailsDTO;
import com.xtremand.lms.bom.LearningTrack;
import com.xtremand.lms.bom.LearningTrackVisibility;
import com.xtremand.lms.bom.LearningTrackVisibilityGroup;
import com.xtremand.lms.dao.LMSDAO;
import com.xtremand.partnership.bom.Partnership;
import com.xtremand.partnership.dao.PartnershipDAO;
import com.xtremand.team.dao.TeamDao;
import com.xtremand.user.bom.TeamMember;
import com.xtremand.user.bom.User;
import com.xtremand.user.bom.UserList;
import com.xtremand.user.bom.UserUserList;
import com.xtremand.user.dao.UserDAO;
import com.xtremand.user.list.dto.CopiedUserListUsersDTO;
import com.xtremand.user.list.dto.CopyGroupUsersDTO;
import com.xtremand.userlist.dao.UserListDAO;
import com.xtremand.util.PaginationUtil;
import com.xtremand.util.XamplifyUtils;
import com.xtremand.util.dao.ExceptionHandlerUtil;
import com.xtremand.util.dao.UtilDao;
import com.xtremand.util.dto.ViewTypePatchRequestDTO;
import com.xtremand.video.bom.VideoFile;
import com.xtremand.white.labeled.dto.DamVideoDTO;

@Service
/*********
 * Never Annotate @Transactional to this class and do not autowire other daos
 * also.If you do so,face the Consequences dear.
 *********/
public class ContentService {

	@Autowired
	private SessionFactory sessionFactory;

	@Autowired
	private PaginationUtil paginationUtil;

	@Autowired
	private LMSDAO lmsDao;

	@Autowired
	private PartnershipDAO partnershipDao;

	@Autowired
	private UserDAO userDao;

	@Autowired
	private TeamDao teamDao;

	@Autowired
	private DashboardButtonDao dashboardButtonDao;

	@Autowired
	private UserListDAO userListDao;

	@Autowired
	private DamDao damDao;

	@Autowired
	private UtilDao utilDao;

	private static final Logger logger = LoggerFactory.getLogger(ContentService.class);

	public void publishAndWhiteLabelContentToCopiedList(List<CopiedUserListUsersDTO> copiedUserListUsersDTOs,
			CopyGroupUsersDTO copyGroupUsersDTO) {

		long startTime = getStartTime();

		String methodName = "publishAndWhiteLabelContentToCopiedList()";

		String methodStartedDebugMessage = "Entered Into " + methodName + " at " + new Date();
		logger.debug(methodStartedDebugMessage);

		Set<Integer> selectedPartnerGroupIds = copiedUserListUsersDTOs.stream()
				.map(CopiedUserListUsersDTO::getUserListId).collect(Collectors.toSet());
		Integer vendorCompanyId = copiedUserListUsersDTOs.stream().map(CopiedUserListUsersDTO::getLoggedInUserCompanyId)
				.collect(Collectors.toList()).get(0);

		List<LearningTrack> trackOrPlayBooks = findTracksOrPlayBooksByGroupIds(selectedPartnerGroupIds);

		List<PublishedDashboardButtonDetailsDTO> publishedDashboardButtonDetailsDTOs = dashboardButtonDao
				.findPublishedDashboardButtonIdAndTitlesByPartnerListIdAndCompanyId(selectedPartnerGroupIds,
						vendorCompanyId);

		List<DamPostDTO> damPostDtos = findDamIdAndDamPartnerIds(selectedPartnerGroupIds);

		List<Integer> deactivatedPartners = utilDao.findDeactivedPartnersByCompanyId(vendorCompanyId);

		for (CopiedUserListUsersDTO copiedUserListUsersDTO : copiedUserListUsersDTOs) {
			Integer userListId = copiedUserListUsersDTO.getUserListId();
			Set<Integer> partnerIds = copiedUserListUsersDTO.getPartnerIds();
			Integer loggedInUserId = copiedUserListUsersDTO.getLoggedInUserId();

			for (Integer partnerId : partnerIds) {
				if (!deactivatedPartners.contains(partnerId)) {
					Integer partnershipId = partnershipDao.findPartnershipIdByPartnerIdAndVendorCompanyId(partnerId,
							vendorCompanyId);
					if (XamplifyUtils.isValidInteger(partnershipId)) {

						/*** Tracks / PlayBooks *****/
						shareTracksOrPlayBooksWithPartners(trackOrPlayBooks, userListId, loggedInUserId, partnerId,
								partnershipId);

						/**** Dashboard Buttons ****/
						shareDashboardButtonsWithPartners(publishedDashboardButtonDetailsDTOs, userListId,
								loggedInUserId, partnerId, partnershipId);

						/***** Assets *****/
						shareAssetsWithPartnersAndHandleWhiteLableAssets(damPostDtos, userListId, loggedInUserId,
								partnerId, partnershipId);
					}
				}
			}
		}

		long executionTimeInMinutes = getExecutionTime(startTime);

		String methodEndedMessage = "Exit From " + methodName + " at " + new Date()
				+ " and time taken to complete the method is " + executionTimeInMinutes + " minutes";

		logger.debug(methodEndedMessage);

		logger.debug("# E N D # at {}", new Date());

	}

	private void shareAssetsWithPartnersAndHandleWhiteLableAssets(List<DamPostDTO> damPostDtos, Integer userListId,
			Integer loggedInUserId, Integer partnerId, Integer partnershipId) {
		for (DamPostDTO damPostDTO : damPostDtos) {
			shareAssetsWithPartners(userListId, loggedInUserId, partnerId, partnershipId, damPostDTO);
			handleWhiteLabeledAssetSharing(loggedInUserId, partnershipId, damPostDTO.getId());

		}
	}

	private void shareAssetsWithPartners(Integer userListId, Integer loggedInUserId, Integer partnerId,
			Integer partnershipId, DamPostDTO damPostDTO) {
		Integer damId = damPostDTO.getId();
		String assetName = damPostDTO.getName();
		String assetNameWithId = assetName + " (" + damId + ")";
		Session damOpenSession = sessionFactory.openSession();
		try {
			DamPartner damPartner = new DamPartner();
			Dam dam = new Dam();
			dam.setId(damId);
			saveOrGetDamPartnerData(loggedInUserId, damPostDTO, damId, damOpenSession, damPartner, partnershipId, dam);
			saveDamPartnerGroupMapping(userListId, partnerId, damOpenSession, damPartner, assetNameWithId,
					partnershipId);
			damOpenSession.flush();
			damOpenSession.clear();
		} catch (ConstraintViolationException ce) {
			ExceptionHandlerUtil.handleException(ce);
		} catch (HibernateException he) {
			ExceptionHandlerUtil.handleException(he);
		} catch (DataIntegrityViolationException de) {
			ExceptionHandlerUtil.handleException(de);
		} catch (Exception e) {
			ExceptionHandlerUtil.handleException(e);
		} finally {
			damOpenSession.close();
			String debugMessage = "Asset :" + assetNameWithId + " is shared with " + partnerId;
			logger.debug(debugMessage);
		}
	}

	private void handleWhiteLabeledAssetSharing(Integer loggedInUserId, Integer partnershipId, Integer damId) {
		Dam dam = getDamById(damId);
		if (dam != null) {
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

	}

	private Dam getDamById(Integer damId) {
		Dam dam = null;
		Session damSession = sessionFactory.openSession();
		try {
			org.hibernate.Criteria criteria = damSession.createCriteria(Dam.class);
			criteria.add(Restrictions.eq("id", damId));
			dam = (Dam) criteria.uniqueResult();
			if (dam != null) {
				Hibernate.initialize(dam.getVideoFile());
				Hibernate.initialize(dam.getTags());
			}
			damSession.flush();
			damSession.clear();
		} catch (ConstraintViolationException ce) {
			ExceptionHandlerUtil.handleException(ce);
		} catch (HibernateException he) {
			ExceptionHandlerUtil.handleException(he);
		} catch (DataIntegrityViolationException de) {
			ExceptionHandlerUtil.handleException(de);
		} catch (Exception e) {
			ExceptionHandlerUtil.handleException(e);
		} finally {
			damSession.close();
		}
		return dam;
	}

	private void shareDashboardButtonsWithPartners(
			List<PublishedDashboardButtonDetailsDTO> publishedDashboardButtonDetailsDTOs, Integer userListId,
			Integer loggedInUserId, Integer partnerId, Integer partnershipId) {
		for (PublishedDashboardButtonDetailsDTO publishedDashboardButtonDetailsDTO : publishedDashboardButtonDetailsDTOs) {
			Integer dashboardButtonId = publishedDashboardButtonDetailsDTO.getId();
			String dashboardButtonTitle = publishedDashboardButtonDetailsDTO.getButtonTitle();
			Integer userUserListId = userListDao.getUserUserListIdByUserListIdAndUserId(userListId, partnerId);
			DashboardButtonsPartnerGroupMapping dashboardButtonPartnerGroupMapping = new DashboardButtonsPartnerGroupMapping();
			DashboardButton dashboardButton = new DashboardButton();
			dashboardButtonPartnerGroupMapping.setDashboardButton(dashboardButton);
			dashboardButton.setId(dashboardButtonId);
			Partnership partnership = new Partnership();
			dashboardButtonPartnerGroupMapping.setPartnership(partnership);
			partnership.setId(partnershipId);
			UserUserList userUserList = new UserUserList();
			userUserList.setId(userUserListId);
			dashboardButtonPartnerGroupMapping.setUserUserList(userUserList);
			dashboardButtonPartnerGroupMapping.setPublishedOn(new Date());
			User publishedBy = new User();
			publishedBy.setUserId(loggedInUserId);
			dashboardButtonPartnerGroupMapping.setPublishedBy(publishedBy);
			Session dashboardButtonOpenSession = sessionFactory.openSession();
			try {
				dashboardButtonOpenSession.save(dashboardButtonPartnerGroupMapping);
				dashboardButtonOpenSession.flush();
				dashboardButtonOpenSession.clear();
			} catch (ConstraintViolationException ce) {
				ExceptionHandlerUtil.handleException(ce);
			} catch (HibernateException he) {
				ExceptionHandlerUtil.handleException(he);
			} catch (DataIntegrityViolationException de) {
				ExceptionHandlerUtil.handleException(de);
			} catch (Exception e) {
				ExceptionHandlerUtil.handleException(e);
			} finally {
				dashboardButtonOpenSession.close();
				String debugMessage = "Dashboard Button :" + dashboardButtonTitle + "(" + dashboardButtonId
						+ ") is shared with " + partnerId;
				logger.debug(debugMessage);
			}

		}
	}

	private void shareTracksOrPlayBooksWithPartners(List<LearningTrack> trackOrPlayBooks, Integer userListId,
			Integer loggedInUserId, Integer partnerId, Integer partnershipId) {
		for (LearningTrack trackOrPlayBook : trackOrPlayBooks) {
			Integer trackOrPlayBookId = trackOrPlayBook.getId();
			List<Integer> visibilityIds = lmsDao.findVisiblityIds(trackOrPlayBookId, partnershipId);
			if (XamplifyUtils.isNotEmptyList(visibilityIds)) {
				saveLearningTrackVisibilityGroups(userListId, loggedInUserId, visibilityIds, trackOrPlayBook);
			} else {
				processAndSavePartnerVisibility(userListId, loggedInUserId, partnerId, partnershipId, trackOrPlayBook);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private List<LearningTrack> findTracksOrPlayBooksByGroupIds(Set<Integer> partnerGroupIds) {
		List<LearningTrack> learningTracks = new ArrayList<>();
		Session trackOrPlayBookOpenSession = sessionFactory.openSession();
		try {
			Criteria criteria = trackOrPlayBookOpenSession.createCriteria(LearningTrackVisibilityGroup.class, "VG");
			criteria.createAlias("VG.learningTrackVisibility", "V", JoinType.LEFT_OUTER_JOIN);
			criteria.add(Restrictions.in("VG.userList.id", partnerGroupIds));
			criteria.setProjection(Projections.distinct(Projections.property("V.learningTrack")));
			/*** XBI-4309 *****/
			criteria.createAlias("V.learningTrack", "learningTrack");
			criteria.add(Restrictions.or(Restrictions.isNull("learningTrack.expireDate"),
					Restrictions.gt("learningTrack.expireDate", new Date())));
			/*** XBI-4309 *****/
			List<Object> results = criteria.list();
			for (Object result : results) {
				LearningTrack track = (LearningTrack) result;
				Hibernate.initialize(track);
				learningTracks.add(track);
			}
			trackOrPlayBookOpenSession.flush();
			trackOrPlayBookOpenSession.clear();
		} catch (ConstraintViolationException ce) {
			ExceptionHandlerUtil.handleException(ce);
		} catch (HibernateException he) {
			ExceptionHandlerUtil.handleException(he);
		} catch (DataIntegrityViolationException de) {
			ExceptionHandlerUtil.handleException(de);
		} catch (Exception e) {
			ExceptionHandlerUtil.handleException(e);
		} finally {
			trackOrPlayBookOpenSession.close();
		}
		return learningTracks;

	}

	public void processAndSavePartnerVisibility(Integer userListId, Integer loggedInUserId, Integer partnerId,
			Integer partnershipId, LearningTrack trackOrPlayBook) {
		Integer partnerCompanyId = userDao.getCompanyIdByUserId(partnerId);
		Integer primaryAdminId = teamDao.findPrimaryAdminIdByCompanyId(partnerCompanyId);
		if (XamplifyUtils.isValidInteger(primaryAdminId)) {
			List<ViewTypePatchRequestDTO> teamMemberIdsAndUserIds = teamDao
					.findTeamMemberIdsAndUserIdsByCompanyId(partnerCompanyId);
			for (ViewTypePatchRequestDTO teamMemberIdsAndUserId : teamMemberIdsAndUserIds) {
				TeamMember teamMember = new TeamMember();
				teamMember.setId(teamMemberIdsAndUserId.getTeamMemberId());
				createAndSaveLearningTrackVisibility(userListId, loggedInUserId, partnershipId, trackOrPlayBook,
						teamMemberIdsAndUserId.getUserId(), teamMember);
			}
			createAndSaveLearningTrackVisibility(userListId, loggedInUserId, partnershipId, trackOrPlayBook,
					primaryAdminId, null);
		}
	}

	private void createAndSaveLearningTrackVisibility(Integer userListId, Integer loggedInUserId, Integer partnershipId,
			LearningTrack trackOrPlayBook, Integer primaryAdminOrUserId, TeamMember teamMember) {
		LearningTrackVisibility learningTrackVisibility = new LearningTrackVisibility();
		learningTrackVisibility.setLearningTrack(trackOrPlayBook);
		Partnership partnership = new Partnership();
		partnership.setId(partnershipId);
		learningTrackVisibility.setPartnership(partnership);
		User partnerUser = new User();
		partnerUser.setUserId(primaryAdminOrUserId);
		learningTrackVisibility.setTeamMember(teamMember);
		learningTrackVisibility.setUser(partnerUser);
		learningTrackVisibility.setCreatedBy(loggedInUserId);
		learningTrackVisibility.setUpdatedBy(loggedInUserId);
		learningTrackVisibility.setCreatedTime(new Date());
		learningTrackVisibility.setUpdatedTime(new Date());
		if (trackOrPlayBook.isPublished()) {
			learningTrackVisibility.setPublished(true);
			learningTrackVisibility.setPublishedOn(new Date());
		}
		Session learningTrackVisibilityOpenSession = sessionFactory.openSession();
		try {
			learningTrackVisibilityOpenSession.save(learningTrackVisibility);
			LearningTrackVisibilityGroup learningTrackVisibilityGroup = new LearningTrackVisibilityGroup();
			learningTrackVisibilityGroup.setLearningTrackVisibility(learningTrackVisibility);
			UserList userList = new UserList();
			userList.setId(userListId);
			learningTrackVisibilityGroup.setUserList(userList);
			learningTrackVisibilityGroup.setCreatedBy(loggedInUserId);
			learningTrackVisibilityGroup.setUpdatedBy(loggedInUserId);
			learningTrackVisibilityGroup.setCreatedTime(new Date());
			learningTrackVisibilityGroup.setUpdatedTime(new Date());
			learningTrackVisibilityOpenSession.save(learningTrackVisibilityGroup);
			learningTrackVisibilityOpenSession.flush();
			learningTrackVisibilityOpenSession.clear();
		} catch (ConstraintViolationException ce) {
			ExceptionHandlerUtil.handleException(ce);
		} catch (HibernateException he) {
			ExceptionHandlerUtil.handleException(he);
		} catch (DataIntegrityViolationException de) {
			ExceptionHandlerUtil.handleException(de);
		} catch (Exception e) {
			ExceptionHandlerUtil.handleException(e);
		} finally {
			learningTrackVisibilityOpenSession.close();
			String debugMessage = "Track / Play Book  :" + trackOrPlayBook.getTitle() + "(" + trackOrPlayBook.getId()
					+ ") is shared with " + primaryAdminOrUserId;
			logger.debug(debugMessage);
		}
	}

	private void saveLearningTrackVisibilityGroups(Integer userListId, Integer loggedInUserId,
			List<Integer> visibilityIds, LearningTrack trackOrPlayBook) {
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
			Session visibilityGroupOpenSession = sessionFactory.openSession();
			try {
				visibilityGroupOpenSession.save(learningTrackVisibilityGroup);
				visibilityGroupOpenSession.flush();
				visibilityGroupOpenSession.clear();
			} catch (ConstraintViolationException ce) {
				ExceptionHandlerUtil.handleException(ce);
			} catch (HibernateException he) {
				ExceptionHandlerUtil.handleException(he);
			} catch (DataIntegrityViolationException de) {
				ExceptionHandlerUtil.handleException(de);
			} catch (Exception e) {
				ExceptionHandlerUtil.handleException(e);
			} finally {
				visibilityGroupOpenSession.close();
				String debugMessage = "Track / Play Book  :" + trackOrPlayBook.getTitle() + "("
						+ trackOrPlayBook.getId() + ") is inserted into visibility group " + visbilityId;
				logger.debug(debugMessage);
			}

		}
	}

	private long getExecutionTime(long startTime) {
		long stopTime = getStartTime();
		long elapsedTime = stopTime - startTime;
		return TimeUnit.MILLISECONDS.toMinutes(elapsedTime);

	}

	private long getStartTime() {
		return System.currentTimeMillis();
	}

	private void saveOrGetDamPartnerData(Integer loggedInUserId, DamPostDTO damPostDTO, Integer damId,
			Session openSession, DamPartner damPartner, Integer partnershipId, Dam dam) {
		Integer damPartnerId = getDamPartnerId(damId, openSession, partnershipId);
		if (damPartnerId != null) {
			damPartner.setId(damPartnerId);
			String publishingDebugMessage = "Skipping damPartnerId " + damPartnerId + " Already Exists for dam Id "
					+ damId;
			logger.debug(publishingDebugMessage);
		} else {
			saveDamPartner(loggedInUserId, damPostDTO, openSession, damPartner, partnershipId, dam);
		}
	}

	private void saveDamPartnerGroupMapping(Integer userListId, Integer partnerId, Session openSession,
			DamPartner damPartner, String assetNameWithId, Integer partnershipId) {
		DamPartnerGroupMapping damPartnerGroupMapping = new DamPartnerGroupMapping();
		damPartnerGroupMapping.setDamPartner(damPartner);
		damPartnerGroupMapping.setCreatedTime(new Date());
		damPartnerGroupMapping.setUserId(partnerId);
		damPartnerGroupMapping.setUserListId(userListId);
		Integer damPartnerGroupId = (Integer) openSession.save(damPartnerGroupMapping);

		saveDamPartnerGroupUserMapping(openSession, partnershipId, partnerId, damPartnerGroupId);
		String debugMessage = assetNameWithId + " Published Successfully With Partner Id :  " + partnerId;
		logger.debug(debugMessage);
	}

	private void saveDamPartnerGroupUserMapping(Session session, Integer partnershipId, Integer partnerId,
			Integer damPartnerGroupId) {
		Integer partnerCompanyId = partnershipDao.getPartnerCompanyIdByPartnershipId(partnershipId);
		List<User> partnerCompanyUsers = XamplifyUtils.isValidInteger(partnerCompanyId)
				? userDao.getAllUsersByCompanyId(partnerCompanyId)
				: Collections.singletonList(createPartnerUser(partnerId));
		for (User companyUser : partnerCompanyUsers) {
			DamPartnerGroupUserMapping partnerGroupMapping = new DamPartnerGroupUserMapping();
			DamPartnerGroupMapping damPartnerGroup = new DamPartnerGroupMapping();
			damPartnerGroup.setId(damPartnerGroupId);
			partnerGroupMapping.setDamPartnerGroupMapping(damPartnerGroup);
			Partnership partnership = new Partnership();
			partnership.setId(partnershipId);
			partnerGroupMapping.setPartnership(partnership);
			partnerGroupMapping.setUser(companyUser);
			if (XamplifyUtils.isNotEmptyList(companyUser.getTeamMembers())) {
				partnerGroupMapping.setTeamMember(companyUser.getTeamMembers().get(0));
			}
			partnerGroupMapping.setCreatedTime(new Date());
			session.save(partnerGroupMapping);
		}
	}

	private User createPartnerUser(Integer partnerId) {
		User partnerUser = new User();
		partnerUser.setUserId(partnerId);
		return partnerUser;
	}

	private void saveDamPartner(Integer loggedInUserId, DamPostDTO damPostDTO, Session openSession,
			DamPartner damPartner, Integer partnershipId, Dam dam) {
		damPartner.setDam(dam);
		Partnership partnership = new Partnership();
		partnership.setId(partnershipId);
		damPartner.setPartnership(partnership);
		damPartner.setAlias(XamplifyUtils.generateAlias());
		damPartner.setHtmlBody(damPostDTO.getHtmlBody());
		damPartner.setJsonBody(damPostDTO.getJsonBody());
		damPartner.setPartnerGroupSelected(true);
		damPartner.setPublishedTime(new Date());
		damPartner.setPublishedBy(loggedInUserId);
		Integer damPartnerId = (Integer) openSession.save(damPartner);
		String debugMessage = "Data Successfully Inserted Into DamPartner with id " + damPartnerId;
		logger.debug(debugMessage);
	}

	private Integer getDamPartnerId(Integer damId, Session openSession, Integer partnershipId) {
		String damPartnerIdQueryString = "select id from xt_dam_partner where partnership_id = :partnershipId and dam_id = :damId";
		return (Integer) openSession.createSQLQuery(damPartnerIdQueryString)
				.setParameter("partnershipId", partnershipId).setParameter("damId", damId).uniqueResult();
	}

	@SuppressWarnings("unchecked")
	private List<DamPostDTO> findDamIdAndDamPartnerIds(Set<Integer> selectedPartnerGroupIds) {
		String queryString = "select distinct xd.id as \"id\", xd.asset_name as \"name\", xd.json_body as \"jsonBody\", xd.html_body as \"htmlBody\" from xt_dam xd,xt_dam_partner xdp,xt_dam_partner_group_mapping xdpgm \r\n"
				+ "where xdpgm.user_list_id in (:partnerGroupIds) and xdpgm.dam_partner_id  = xdp.id and xdp.is_partner_group_selected \r\n"
				+ "and xdp.dam_id = xd.id ";
		Session damOpenSession = sessionFactory.openSession();
		List<DamPostDTO> damDtos = new ArrayList<>();
		try {
			Query query = damOpenSession.createSQLQuery(queryString);
			query.setParameterList("partnerGroupIds", selectedPartnerGroupIds);
			damDtos = (List<DamPostDTO>) paginationUtil.getListDTO(DamPostDTO.class, query);
			damOpenSession.flush();
			damOpenSession.clear();
		} catch (ConstraintViolationException ce) {
			ExceptionHandlerUtil.handleException(ce);
		} catch (HibernateException he) {
			ExceptionHandlerUtil.handleException(he);
		} catch (DataIntegrityViolationException de) {
			ExceptionHandlerUtil.handleException(de);
		} catch (Exception e) {
			ExceptionHandlerUtil.handleException(e);
		} finally {
			damOpenSession.close();
		}
		return damDtos;

	}

}
