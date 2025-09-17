package com.xtremand.mail.service;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Async;

import com.xtremand.activity.dto.ActivityAWSDTO;
import com.xtremand.approve.dao.ApproveDAO;
import com.xtremand.approve.dto.ApprovalPrivilegesEmailNotificationDTO;
import com.xtremand.approve.dto.PendingApprovalDamAndLmsDTO;
import com.xtremand.campaign.bom.DownloadDataInfo.DownloadItem;
import com.xtremand.common.bom.CompanyProfile;
import com.xtremand.common.bom.Pagination;
import com.xtremand.content.service.ContentService;
import com.xtremand.dam.bom.Dam;
import com.xtremand.dam.dto.ApprovalStatusHistoryDTO;
import com.xtremand.dam.dto.DamAwsDTO;
import com.xtremand.dam.dto.DamPostDTO;
import com.xtremand.dam.dto.DamUploadPostDTO;
import com.xtremand.dashboard.button.service.DashboardButtonService;
import com.xtremand.dashboard.button.service.DashboardButtonsAsyncService;
import com.xtremand.dashboard.buttons.dto.DashboardButtonsDTO;
import com.xtremand.dashboard.buttons.dto.DashboardButtonsToPartnersDTO;
import com.xtremand.dashboard.buttons.dto.PublishedDashboardButtonDetailsDTO;
import com.xtremand.deal.dto.DealDto;
import com.xtremand.domain.dto.DomainMediaResourceDTO;
import com.xtremand.form.dto.FormDataForLeadNotification;
import com.xtremand.form.submit.bom.FormSubmit;
import com.xtremand.form.submit.dto.FormSubmitDTO;
import com.xtremand.formbeans.AddPartnerResponseDTO;
import com.xtremand.formbeans.EmailDTO;
import com.xtremand.formbeans.LeadDTO;
import com.xtremand.formbeans.UserDTO;
import com.xtremand.formbeans.UserListDTO;
import com.xtremand.formbeans.UserListPaginationWrapper;
import com.xtremand.formbeans.VendorInvitationDTO;
import com.xtremand.lead.dto.LeadDto;
import com.xtremand.lms.dto.LearningTrackDto;
import com.xtremand.mail.service.MailService.EmailBuilder;
import com.xtremand.mdf.bom.MdfDetails;
import com.xtremand.mdf.bom.MdfRequest;
import com.xtremand.mdf.dto.MdfRequestViewDTO;
import com.xtremand.partner.journey.dto.WorkflowRequestDTO;
import com.xtremand.signup.dto.SignUpRequestDTO;
import com.xtremand.team.member.dto.PartnerPrimaryAdminUpdateDto;
import com.xtremand.team.member.dto.TeamMemberDTO;
import com.xtremand.thread.info.service.ThreadCountService;
import com.xtremand.user.bom.ShareLeadsDTO;
import com.xtremand.user.bom.User;
import com.xtremand.user.bom.UserList;
import com.xtremand.user.list.dto.CopiedUserListUsersDTO;
import com.xtremand.user.list.dto.CopyGroupUsersDTO;
import com.xtremand.util.XamplifyUtils;
import com.xtremand.util.bom.ModuleType;
import com.xtremand.util.dto.ActiveAndInActivePartnerEmailNotificationDTO;
import com.xtremand.util.dto.LoginAsEmailNotificationDTO;
import com.xtremand.util.dto.ShareContentRequestDTO;
import com.xtremand.util.dto.UserListOperationsAsyncDTO;
import com.xtremand.util.service.ThymeLeafService;
import com.xtremand.vanity.url.dto.VanityUrlDetailsDTO;
import com.xtremand.video.bom.VideoFile;
import com.xtremand.videoencoding.service.FFMPEGStatus;

@Configuration
public class AsyncComponent {

	private static final Logger logger = LoggerFactory.getLogger(AsyncComponent.class);

	private static final String STARTED_AT = " started at ";

	@Autowired
	private AsyncService asyncService;

	@Autowired
	private ThymeLeafService thymeleafService;

	@Autowired
	private ThreadCountService threadCountService;

	@Autowired
	private ContentService contentService;

	@Autowired
	private DashboardButtonsAsyncService dashboardButtonsAsyncService;

	@Autowired
	private DashboardButtonService dashboardButtonService;

	@Autowired
	private ApproveDAO approveDAO;

	@Async(value = "teamMember")
	public void sendTeamMemberEmailsAsync(List<User> teamMembers, User orgAdmin, String body,
			boolean resendingInvitation) {
		asyncService.sendTeamMemberEmailsAsync(teamMembers, orgAdmin, body, resendingInvitation);
	}

	@Async(value = "formLeadsNotification")
	public void sendFormLeadsNotification(FormSubmit formSubmit, FormSubmitDTO formSubmitDTO,
			List<FormDataForLeadNotification> formDataForLeadNotifications) {
		asyncService.sendFormLeadsNotification(formSubmit, formSubmitDTO, formDataForLeadNotifications);
	}

	@Async(value = "partnerEmailNotification")
	public void sendPartnerMail(User user, int templateId, User customer, Integer userListId) {
		asyncService.sendPartnerMail(user, templateId, customer, userListId);
	}

	@Async(value = "partnerOrContactNotification")
	public void sendPartnerMail(List<User> partnersList, int templateId, User customer, Integer userListId) {
		asyncService.sendPartnerMail(partnersList, templateId, customer, userListId);
	}

	private long getStartTime() {
		return System.currentTimeMillis();
	}

	private void getExecutionTime(long startTime, String methodName) {
		long stopTime = getStartTime();
		long elapsedTime = stopTime - startTime;
		long minutes = TimeUnit.MILLISECONDS.toMinutes(elapsedTime);
		String debugMessage = methodName + " Completed In " + minutes + " minutes at " + new Date()
				+ "***Active Thread Count*****" + Thread.activeCount();
		logger.debug(debugMessage);
	}

	@Async(value = "vendorInvitation")
	public void sendVendorInvitation(VendorInvitationDTO vendorInvitationDTO, User sender,
			List<User> sendPartnerMailsList) {
		asyncService.sendVendorInvitation(vendorInvitationDTO, sender, sendPartnerMailsList);
	}

	public void sendUserWelcomeEmailThroughAdmin(EmailDTO emailDTO) {
		asyncService.sendUserWelcomeEmailThroughAdmin(emailDTO);
	}

	@Async(value = "processVideo")
	public void processVideo(String finalPath, int currentBitRate, FFMPEGStatus status, VideoFile video, User user,
			Dam dam, DamUploadPostDTO damUploadPostDTO) {
		String debugMessage = "*************Video processing is started for " + video.getTitle() + " ["
				+ user.getEmailId() + "," + user.getUserId() + "]";
		logger.debug(debugMessage);
		asyncService.processVideo(finalPath, currentBitRate, video, user, dam, damUploadPostDTO);
	}

	@Async(value = "teamMemberEmailsHistory")
	public void saveTeamMemberEmailsHistory(EmailBuilder builder) {
		asyncService.saveTeamMemberEmailsHistory(builder);
	}

	@Async(value = "newMdfRequest")
	public void sendNewMdfRequestNotification(MdfRequest mdfRequest, String requestTitle, String requestedAmount) {
		asyncService.sendNewMdfRequestNotification(mdfRequest, requestTitle, requestedAmount);
	}

	@Async(value = "fundAmountUpdated")
	public void sendMdfAmountNotification(MdfDetails mdfDetails) {
		asyncService.sendMdfAmountNotification(mdfDetails);
	}

	@Async(value = "mdfRequestDocumentsUploaded")
	public void sendMdfRequestDocumentUploadedNotification(String fileName, Integer loggedInUserId, Integer requestId) {
		asyncService.sendMdfRequestDocumentUploadedNotification(fileName, loggedInUserId, requestId);
	}

	@Async(value = "mdfRequestStatusChanged")
	public void sendMdfRequestStatusChangedNotification(MdfRequestViewDTO mdfRequestViewDTO, MdfRequest mdfRequest) {
		asyncService.sendMdfRequestStatusChangedNotification(mdfRequestViewDTO, mdfRequest);
	}

	@Async(value = "uploadAsset")
	public void uploadAsset(DamAwsDTO damAwsDTO, DamUploadPostDTO damUploadPostDTO) {
		asyncService.uploadAsset(damAwsDTO, damUploadPostDTO);
	}

	@Async(value = "lMSPublishedNotification")
	public void sendLMSPublishedNotification(Integer learningTrackId, Integer loggedInUserId) {
		asyncService.sendLMSPublishedNotification(learningTrackId, loggedInUserId);
	}

	@Async("publishDamToTeamMemberPartnerListPartners")
	public void publishDamToTeamMemberPartnerListPartners(Set<Integer> userListIds, User loggedInUser,
			Set<UserDTO> partners) {
		asyncService.publishDamToTeamMemberPartnerListPartners(userListIds, loggedInUser, partners);
	}

	@Async("publishLmsToNewUsersInUserList")
	public void publishLMSToNewUsersInUserList(Set<Integer> userListIds, User loggedInUser, Set<UserDTO> partners) {
		asyncService.publishLmsToNewUsersInUserList(userListIds, loggedInUser, partners);
	}

	@Async("deleteLMSForDeletedUsersInUserList")
	public void deleteLMSForDeletedUsersInUserList(Integer userListId, List<Integer> removePartnerIds) {
		asyncService.deleteLMSForDeletedUsersInUserList(userListId, removePartnerIds);
	}

	@Async("publishLMSToNewTeamMembers")
	public void publishLMSToNewTeamMembers(List<TeamMemberDTO> teamMemberDTOs, Integer loggedInUserId) {
		asyncService.publishLMSToNewTeamMembers(teamMemberDTOs, loggedInUserId);
	}

	@Async(value = "vendorInvitation")
	public void sendVendorInvitationEmailToSuperAdmin(VendorInvitationDTO vendorInvitationDTO, User sender) {
		thymeleafService.sendReferAVendorInvitationEmailNotification(vendorInvitationDTO, sender);
	}


	@Async(value = "activeAndInActivePartnerNotification")
	public void sendActiveAndInactivePartnerNotification(
			ActiveAndInActivePartnerEmailNotificationDTO activeAndInActivePartnerEmailNotificationDTO) {
		asyncService.sendActiveAndInactivePartnerNotification(activeAndInActivePartnerEmailNotificationDTO);
	}

	@Async(value = "activeAndInActivePartnerNotification")
	public void sendActivePartnerNotification(
			ActiveAndInActivePartnerEmailNotificationDTO activeAndInActivePartnerEmailNotificationDTO) {
		asyncService.sendActivePartnerNotification(activeAndInActivePartnerEmailNotificationDTO);
	}

	@Async(value = "activeAndInActivePartnerNotification")
	public void sendInactivePartnerNotification(
			ActiveAndInActivePartnerEmailNotificationDTO activeAndInActivePartnerEmailNotificationDTO) {
		asyncService.sendInactivePartnerNotification(activeAndInActivePartnerEmailNotificationDTO);
	}

	@Async(value = "uploadThumbnail")
	public void uploadThumbnail(DamAwsDTO damAwsDTO, String awsFilePath) {
		asyncService.uploadThumbnail(damAwsDTO, awsFilePath);
	}

	@Async(value = "downloadHighLevelAnalytics")
	public void generateHighLevelAnalyticsExcelAndSendEmailNotification(Integer id,
			VanityUrlDetailsDTO vanityUrlDetailsDTO) {
		asyncService.generateHighLevelAnalyticsExcelAndSendEmailNotification(id, vanityUrlDetailsDTO);
	}

	@Async("ThreadCountExecutor")
	public void getThreadCountDetails() {
		threadCountService.getThreadCountDetails();
	}

	/****** XNFR-211 ******/
	@Async(value = "createUsersInUserList")
	public void createUsersInUserList(Set<UserDTO> userDTOs, Integer userListId, Integer loggedInUserId,
			boolean isCreate, UserListDTO userlistDTO) {
		asyncService.createUsersInUserList(userDTOs, userListId, loggedInUserId, isCreate, userlistDTO);
	}

	/****** XNFR-224 ******/
	@Async
	public void sendLoginAsPartnerEmailNotification(LoginAsEmailNotificationDTO loginAsEmailNotificationDTO) {
		asyncService.sendLoginAsPartnerEmailNotification(loginAsEmailNotificationDTO);

	}

	@Async(value = "assetSharedNotification")
	public void sendAssetSharedNotificationEmailsToPartners(List<Integer> updatedPartnerIds, String companyName,
			String assetName, Integer userId, Map<Integer, LinkedList<Integer>> damPartnerIds) {
		asyncService.checkPartnerIdsAndSendAssetSharedEmailNotification(updatedPartnerIds, companyName, assetName, true,
				userId, damPartnerIds);

	}

	@Async(value = "publishAsset")
	public void publishAsset(DamUploadPostDTO damUploadPostDTO) {
		String methodName = "publishAsset(" + damUploadPostDTO.toString() + ")";
		String debugMessage = methodName + STARTED_AT + new Date();
		logger.debug(debugMessage);
		long startTime = getStartTime();
		asyncService.publishAsset(damUploadPostDTO);
		getExecutionTime(startTime, methodName);
	}

	@Async(value = "publishAsset")
	public void publishVideoAsset(DamUploadPostDTO damUploadPostDTO) {
		String methodName = "publishVideoAsset(" + damUploadPostDTO.toString() + ")";
		String debugMessage = methodName + STARTED_AT + new Date();
		logger.debug(debugMessage);
		long startTime = getStartTime();
		asyncService.publishVideoAsset(damUploadPostDTO);
		getExecutionTime(startTime, methodName);
	}

	@Async
	public void sendShareLeadEmailNotificationToPartner(ShareLeadsDTO shareLeadsDTO, String sharedLeadListName) {
		asyncService.sendShareLeadEmailNotificationToPartner(shareLeadsDTO, sharedLeadListName);
	}

	/**** XNFR-327 *******/
	@Async("publishTrackOrPlayBook")
	public void publishTrackOrPlayBook(LearningTrackDto learningTrackDto) {
		asyncService.publishTrackOrPlayBook(learningTrackDto);
	}

	/**** XNFR-327 *******/
	@Async("updatePublishTrackOrPlayBook")
	public void updatePublishTrackOrPlayBook(LearningTrackDto learningTrackDto) {
		asyncService.updatePublishTrackOrPlayBook(learningTrackDto);
	}

	/**** XNFR-342 *******/
	@Async("publishDamToNewlyAddedPartners")
	public void publishDamToNewlyAddedPartners(ShareContentRequestDTO shareContentRequestDTO, boolean damAccess) {
		String methodName = "publishDamToNewlyAddedPartners(" + shareContentRequestDTO.toString() + ")";
		String debugMessage = methodName + STARTED_AT + new Date();
		logger.debug(debugMessage);
		long startTime = getStartTime();
		asyncService.publishDamToNewlyAddedPartners(shareContentRequestDTO, damAccess);
		getExecutionTime(startTime, methodName);

	}

	/**** XNFR-342 *******/
	@Async("publishTracksToNewlyAddedPartners")
	public void publishLmsToNewlyAddedPartners(ShareContentRequestDTO shareContentRequestDTO, boolean lmsAccess) {
		String methodName = "publishLmsToNewlyAddedPartners(" + shareContentRequestDTO.toString() + ")";
		String debugMessage = methodName + STARTED_AT + new Date();
		logger.debug(debugMessage);
		long startTime = getStartTime();
		asyncService.publishTracksOrPlayBooksToNewlyAddedPartners(shareContentRequestDTO, lmsAccess);
		getExecutionTime(startTime, methodName);
	}

	/**** XNFR-342 *******/
	@Async("publishPlayBooksToNewlyAddedPartners")
	public void publishPlayBooksToNewlyAddedPartners(ShareContentRequestDTO shareContentRequestDTO,
			boolean playbookAccess) {
		String methodName = "publishPlayBooksToNewlyAddedPartners(" + shareContentRequestDTO.toString() + ")";
		String debugMessage = methodName + STARTED_AT + new Date();
		logger.debug(debugMessage);
		long startTime = getStartTime();
		asyncService.publishTracksOrPlayBooksToNewlyAddedPartners(shareContentRequestDTO, playbookAccess);
		getExecutionTime(startTime, methodName);
	}

	/***** XNFR-445 *****/
	@Async(value = "uploadCsvToAws")
	public void uploadCsvToAws(Integer userId, Pagination pagination, DownloadItem dataType,
			Integer downloadDataInfoId) {
		asyncService.uploadCsvToAws(userId, pagination, dataType, downloadDataInfoId);
	}

	@Async(value = "uploadUserList")
	public void uploadUserList(Integer userId, UserListPaginationWrapper userListPaginationWrapper,
			Integer downloadDataInfoId) {
		asyncService.uploadUserList(userId, userListPaginationWrapper, downloadDataInfoId);
	}

	/**** XNFR-434 *******/
	@Async(value = "replaceAsset")
	public void replaceAndPublishAsset(DamAwsDTO damAwsDTO, DamUploadPostDTO damUploadPostDTO) {
		asyncService.replaceAndPublishAsset(damAwsDTO, damUploadPostDTO);
	}

	/**** XNFR-434 *******/

	@Async(value = "newMdfRequest")
	public void sendTeamMemberSignedUpEmailNotificationsToAdmins(SignUpRequestDTO signUpRequestDTO) {
		thymeleafService.sendTeamMemberSignedUpEmailNotificationsToAdmins(signUpRequestDTO);

	}

	@Async(value = "publishContentToCopiedList")
	public void publishAndWhiteLabelContentToCopiedPartners(List<CopiedUserListUsersDTO> copiedUserListUsersDTOs,
			CopyGroupUsersDTO copyGroupUsersDTO) {
		contentService.publishAndWhiteLabelContentToCopiedList(copiedUserListUsersDTOs, copyGroupUsersDTO);

	}

	@Async(value = "newMdfRequest")
	public void sendPartnerSignedUpEmailNotifications(SignUpRequestDTO signUpRequestDTO) {
		thymeleafService.sendPartnerSignedUpEmailNotificationsToAdmins(signUpRequestDTO);
	}

	/**** XNFR-571 ****/
	@Async(value = "campaignEmailsForScheduler")
	public void publishDashboardButtons(DashboardButtonsDTO dbButtonsDto) {
		long startTime = getStartTime();
		asyncService.publishDashboardButtons(dbButtonsDto);
		getExecutionTime(startTime,
				"publishDashboardButtons(" + dbButtonsDto.getId() + "," + dbButtonsDto.getButtonTitle() + ")");

	}

	/**** XNFR-597 ****/
	@Async(value = "campaignEmailsForScheduler")
	public void publishDashboardButtonsToNewlyAddedPartners(UserListOperationsAsyncDTO userListOperationsAsyncDTO,
			Integer userId) {
		long startTime = getStartTime();
		List<PublishedDashboardButtonDetailsDTO> publishedDashboardButtons = dashboardButtonService
				.findPublishedDashboardButtonsAndUpdateStatus(userListOperationsAsyncDTO.getPartnerListIds(), userId);
		if (XamplifyUtils.isNotEmptyList(publishedDashboardButtons)) {
			dashboardButtonsAsyncService.findPartnerDetailsAndPublishWithNewlyAddedPartners(userListOperationsAsyncDTO,
					userId, publishedDashboardButtons);
			Set<Integer> dashboardButtonIds = publishedDashboardButtons.stream()
					.map(PublishedDashboardButtonDetailsDTO::getId).collect(Collectors.toSet());
			dashboardButtonService.updateStatus(dashboardButtonIds, false);
		}
		getExecutionTime(startTime, "publishDashboardButtonsToNewlyAddedPartners()");

	}

	/** XNFR-735 **/
	@Async(value = "uploadAttachmentFiles")
	public void uploadAttachmentFiles(List<ActivityAWSDTO> activityAWSDTOs) {
		asyncService.uploadAttachmentFiles(activityAWSDTOs);
	}


	@Async(value = "campaignEmailsForScheduler")
	public void shareDashboardButtonsToPartners(DashboardButtonsToPartnersDTO dashboardButtonsToPartnersDTO) {
		asyncService.distributeDashboardButtons(dashboardButtonsToPartnersDTO);

	}

	@Async(value = "deleteAttachmentFilesFromAWS")
	public void deleteAttachmentFilesFromAWS(List<String> awsFilePaths) {
		long startTime = getStartTime();
		asyncService.deleteAttachmentFilesFromAWS(awsFilePaths);
		getExecutionTime(startTime, "deleteAttachmentFilesFromAWS()");
	}

	/** XNFR-780 **/
	@Async(value = "domainMediaUploader")
	public void uploadDomainMediaResourcesToAWS(List<DomainMediaResourceDTO> domainMediaResourceDTOs) {
		asyncService.uploadDomainMediaResourcesToAWS(domainMediaResourceDTOs);
	}

	/** XNFR-781 **/
	@Async(value = "approvalStatusUpdatedNotifier")
	public void sendContentApprovalStatusEmailNotification(ApprovalStatusHistoryDTO damStatusHistoryDTO) {
		asyncService.sendContentApprovalStatusEmailNotification(damStatusHistoryDTO);
	}

	/** XNFR-821 **/
	@Async(value = "approvalPrivilegesUpdatedNotifier")
	public void sendTeamMemberPrivilegesUpdatedForApprovalProccessEmailNotification(
			List<ApprovalPrivilegesEmailNotificationDTO> approvalPrivilegesEmailNotificationDTOs,
			Integer loggedInUserId) {
		asyncService.sendTeamMemberPrivilegesUpdatedForApprovalProccessEmailNotification(
				approvalPrivilegesEmailNotificationDTOs, loggedInUserId);
	}

	/** XNFR-822 **/
	@Async(value = "sendRemainderEmailNotification")
	public void sendApprovalReminderNotificationForPendingContent(List<Integer> allApproversIds,
			PendingApprovalDamAndLmsDTO pendingApprovalDamAndLmsDTO) {
		long startTime = getStartTime();
		asyncService.sendApprovalReminderNotificationForPendingContent(allApproversIds, pendingApprovalDamAndLmsDTO);
		getExecutionTime(startTime, "sendApprovalReminderNotificationForPendingContent()");
	}

	@Async(value = "addPartnersToList")
	public void addPartnerToList(Set<UserDTO> partners, UserList createdList, Integer companyId,
			UserListDTO userListDTO) {
		asyncService.addPartnerToList(partners, createdList, companyId, userListDTO);

	}

	/** XNFR-878 **/
	@Async
	public void sendPartnerPrimaryAdminUpdateEmail(PartnerPrimaryAdminUpdateDto partnerPrimaryAdminUpdateDto) {
		thymeleafService.sendPartnerPrimaryAdminUpdateEmail(partnerPrimaryAdminUpdateDto);

	}

	/** XNFR-892 **/
	@Async(value = "sendLeadAddedOrUpdatedEmailToPartner")
	public void sendLeadAddedOrUpdatedEmailToPartner(LeadDto leadDto, boolean isUpdateLead) {
		asyncService.sendLeadAddedOrUpdatedEmailToPartner(leadDto, isUpdateLead);
	}

	@Async(value = "sendDealAddedOrUpdatedEmailToPartner")
	public void sendDealAddedOrUpdatedEmailToPartner(DealDto dealDto, boolean isUpdateDeal) {
		asyncService.sendDealAddedOrUpdatedEmailToPartner(dealDto, isUpdateDeal);
	}

	/** XNFR-891 **/
	@Async(value = "updatePartnerModuleAccesses")
	public void updatePartnerModulesAccess(List<AddPartnerResponseDTO> responseDTOList, Set<UserDTO> users) {
		asyncService.updatePartnerModulesAccess(responseDTOList, users);
	}

	@Async(value = "updatePartnershipCompanyIdByPartnerId")
	public void updatePartnershipCompanyIdByPartnerId(Integer partnerId, Integer partnerCompanyId) {
		asyncService.updatePartnershipCompanyIdByPartnerId(partnerId, partnerCompanyId);
	}

	/** XNFR-885 **/
	@Async(value = "approvalStatusUpdatedNotifier")
	public void handleWhiteLabeledAssetsAfterReApproval(List<Integer> whiteLabeledParentDamIds, Integer companyId,
			Integer loggedInUserId) {
		long startTime = getStartTime();
		asyncService.handleWhiteLabeledAssetsAfterReApproval(whiteLabeledParentDamIds, companyId, loggedInUserId);
		getExecutionTime(startTime, "handleWhiteLabeledAssetsAfterReApproval()");
	}

	@Async(value = "sendRemainderEmailNotification")
	public void sendNotificationForSendForApprovalAndReApprovalAssets(
			PendingApprovalDamAndLmsDTO pendingApprovalDamAndLmsDTO) {
		long startTime = getStartTime();
		List<Integer> allApproversIds = approveDAO.findAllApproversByModuleTypeAndCompanyId(
				pendingApprovalDamAndLmsDTO.getCompanyId(), ModuleType.DAM.name());
		asyncService.sendApprovalReminderNotificationForPendingContent(allApproversIds, pendingApprovalDamAndLmsDTO);
		getExecutionTime(startTime, "sendNotificationForSendForApprovalAndReApprovalAssets()");
	}

	/** XNFR-911 **/
	@Async(value = "sendLeadFieldUpdatedNotification")
	public void sendLeadFieldUpdatedNotification(Map<User, List<LeadDTO>> map) {
		if (!map.isEmpty()) {
			long startTime = getStartTime();
			asyncService.sendLeadFieldUpdatedNotification(map);
			getExecutionTime(startTime, "sendLeadFieldUpdatedNotification()");
		}
	}

	@Async(value = "sendPartnerSignatureRemainderEmailNotification")
	public void sendPartnerSignatureRemainderEmailNotification(Pagination pagination) {
		long startTime = getStartTime();
		asyncService.sendPartnerSignatureRemainderEmailNotification(pagination);
		getExecutionTime(startTime, "sendPartnerSignatureRemainderEmailNotification()");
	}

	@Async(value = "uploadPdfToAws")
	public void uploadDesignedPdfToAws(Integer damId) {
		asyncService.uploadDesignedPdfToAws(damId);
	}

	// XNFR-921
	@Async(value = "savePlaybookWorkFlows")
	public void savePlaybookWorkFlows(List<WorkflowRequestDTO> workflowRequestDTOs, Integer playbookId,
			List<Integer> deletedWorkFlowIds, String playbookName) {
		asyncService.savePlaybookWorkFlows(workflowRequestDTOs, playbookId, deletedWorkFlowIds, playbookName);
	}

	// XNFR-993
	@Async(value = "savePlaybookWorkflowEmailHistory")
	public void savePlaybookWorkflowEmailHistory(EmailBuilder builder) {
		asyncService.savePlaybookWorkflowEmailHistory(builder);
	}

	@Async
	public void saveCompanyDomainColors(CompanyProfile companyProfile) {
		asyncService.saveCompanyDomainColors(companyProfile);
	}

	@Async(value = "damBeeTemplateImage")
	public void generateThumbnailAndPublishToPartners(DamPostDTO damPostDTO) {
		long startTime = getStartTime();
		asyncService.generateThumbnailAndPublishToPartners(damPostDTO);
		getExecutionTime(startTime,
				"damBeeTemplateImage(" + damPostDTO.getName() + "," + damPostDTO.getCreatedBy() + ")");
	}

}