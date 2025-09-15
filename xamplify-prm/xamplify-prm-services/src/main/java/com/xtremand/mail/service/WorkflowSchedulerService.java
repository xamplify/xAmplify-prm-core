package com.xtremand.mail.service;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.xtremand.common.bom.Criteria;
import com.xtremand.common.bom.Criteria.OPERATION_NAME;
import com.xtremand.common.bom.FindLevel;
import com.xtremand.dao.util.GenericDAO;
import com.xtremand.formbeans.EmailTemplateDTO;
import com.xtremand.mail.bom.EmailTemplate;
import com.xtremand.partner.journey.bom.TriggerActionEnum;
import com.xtremand.partner.journey.bom.TriggerComponent;
import com.xtremand.partner.journey.bom.TriggerSubjectEnum;
import com.xtremand.partner.journey.bom.Workflow;
import com.xtremand.social.formbeans.MyMergeTagsInfo;
import com.xtremand.user.bom.User;
import com.xtremand.user.service.UserService;
import com.xtremand.util.XamplifyUtils;
import com.xtremand.util.dao.UtilDao;
import com.xtremand.util.service.UtilService;
import com.xtremand.workflow.dao.WorkflowDAO;

@Service
@Transactional
public class WorkflowSchedulerService {

	private Timestamp presentTs;

	@Autowired
	private GenericDAO genericDao;

	@Autowired
	private WorkflowDAO workflowDao;

	@Autowired
	private UtilDao utilDao;

	@Autowired
	private AsyncService asyncService;

	@Autowired
	private UserService userService;

	@Autowired
	private UtilService utilService;

	@Value("${web_url}")
	String webUrl;

	private static final Logger logger = LoggerFactory.getLogger(WorkflowSchedulerService.class);

	private static final String PRE_HEADER = "<p class='preheader' style='display: none !important; visibility: hidden; opacity: 0; color: transparent; height: 0; width: 0;'>{{dynamic_pre_header}}</p>";

	public void triggerWorkflowEmails() {
		logger.debug("triggerWorkflowEmails() is executed at {} ", new Date());
		this.presentTs = new Timestamp(System.currentTimeMillis());
		String loggerSuffix = " -(" + new Date() + ")";
		List<Workflow> workflows = workflowDao.getAllActiveWorkflows();
		if (workflows != null && !workflows.isEmpty()) {
			logger.debug("Total Workflow Count:-" + workflows.size() + loggerSuffix);
			for (Workflow workflow : workflows) {
				if (workflow != null) {
					TriggerComponent action = workflow.getTriggerAction();
					if (action != null) {
						TriggerActionEnum triggerAction = TriggerActionEnum.valueOf(action.getKey());
						switch (triggerAction) {
						case signed_up:
							sendSignedUpWorkflowEmails(workflow);
							break;
						case activated:
							sendActivatedWorkflowEmails(workflow);
							break;
						case redistributed_campaign:
							sendRedistributedCampaignWorkflowEmails(workflow);
							break;
						case created_company_profile:
							sendCreatedCompanyProfileWorkflowEmails(workflow);
							break;
						case created_lead:
							sendCreatedLeadWorkflowEmails(workflow);
							break;
						case created_deal:
							sendCreatedDealWorkflowEmails(workflow);
							break;
						case converted_lead:
							sendConvertedLeadWorkflowEmails(workflow);
							break;
						case closed_deal:
							sendClosedDealWorkflowEmails(workflow);
							break;
						case added_team_member:
							sendAddedTeamMemberWorkflowEmails(workflow);
							break;
						case added_contact:
							sendAddedContactWorkflowEmails(workflow);
							break;
						case completed_track:
							sendCompletedTrackWorkflowEmails(workflow);
							break;
						case completed_playbook:
							sendCompletedPlaybookWorkflowEmails(workflow);
							break;
						case viewed_track:
							sendViewedTrackWorkflowEmails(workflow);
							break;
						case viewed_playbook:
							sendViewedPlaybookWorkflowEmails(workflow);
							break;
						case viewed_pages:
							sendViewedPagesWorkflowEmails(workflow);
							break;
						case requested_mdf:
							sendRequestedMdfWorkflowEmails(workflow);
							break;
						case redistributed_sharelead:
							sendRedistributedShareLeadWorkflowEmails(workflow);
						}
					}
				}
			}
		}
	}

	private void sendRedistributedShareLeadWorkflowEmails(Workflow workflow) {
		TriggerComponent subject = workflow.getTriggerSubject();
		if (TriggerSubjectEnum.partner_has.name().equals(subject.getKey())) {
			sendPartnerHasRedistributedShareLeadWorkflowEmails(workflow);
		} else if (TriggerSubjectEnum.partner_has_not.name().equals(subject.getKey())) {
			sendPartnerHasNotRedistributedShareLeadWorkflowEmails(workflow);
		}
	}

	private void sendPartnerHasNotRedistributedShareLeadWorkflowEmails(Workflow workflow) {
		List<User> workflowUsers = workflowDao.listNotRedistributedShareLeadUsers(workflow);
		sendWorkflowEmails(workflow, workflowUsers);
	}

	private void sendPartnerHasRedistributedShareLeadWorkflowEmails(Workflow workflow) {
		List<User> workflowUsers = workflowDao.listRedistributedShareLeadUsers(workflow);
		sendWorkflowEmails(workflow, workflowUsers);
	}

	private void sendRequestedMdfWorkflowEmails(Workflow workflow) {
		TriggerComponent subject = workflow.getTriggerSubject();
		if (TriggerSubjectEnum.partner_has.name().equals(subject.getKey())) {
			sendPartnerHasRequestedMdfWorkflowEmails(workflow);
		} else if (TriggerSubjectEnum.partner_has_not.name().equals(subject.getKey())) {
			sendPartnerHasNotRequestedMdfWorkflowEmails(workflow);
		}
	}

	private void sendPartnerHasNotRequestedMdfWorkflowEmails(Workflow workflow) {
		List<User> workflowUsers = workflowDao.listNotRequestedMdfUsers(workflow);
		sendWorkflowEmails(workflow, workflowUsers);
	}

	private void sendPartnerHasRequestedMdfWorkflowEmails(Workflow workflow) {
		List<User> workflowUsers = workflowDao.listRequestedMdfUsers(workflow);
		sendWorkflowEmails(workflow, workflowUsers);
	}

	private void sendViewedPagesWorkflowEmails(Workflow workflow) {
		TriggerComponent subject = workflow.getTriggerSubject();
		if (TriggerSubjectEnum.partner_has.name().equals(subject.getKey())) {
			sendPartnerHasViewedPagesWorkflowEmails(workflow);
		} else if (TriggerSubjectEnum.partner_has_not.name().equals(subject.getKey())) {
			sendPartnerHasNotViewedPagesWorkflowEmails(workflow);
		}
	}

	private void sendPartnerHasNotViewedPagesWorkflowEmails(Workflow workflow) {
		List<User> workflowUsers = workflowDao.listNotViewedPagesUsers(workflow);
		sendWorkflowEmails(workflow, workflowUsers);
	}

	private void sendPartnerHasViewedPagesWorkflowEmails(Workflow workflow) {
		List<User> workflowUsers = workflowDao.listViewedPagesUsers(workflow);
		sendWorkflowEmails(workflow, workflowUsers);
	}

	private void sendViewedPlaybookWorkflowEmails(Workflow workflow) {
		TriggerComponent subject = workflow.getTriggerSubject();
		if (TriggerSubjectEnum.partner_has.name().equals(subject.getKey())) {
			sendPartnerHasViewedPlaybookWorkflowEmails(workflow);
		} else if (TriggerSubjectEnum.partner_has_not.name().equals(subject.getKey())) {
			sendPartnerHasNotViewedPlaybookWorkflowEmails(workflow);
		}
	}

	private void sendPartnerHasNotViewedPlaybookWorkflowEmails(Workflow workflow) {
		List<User> workflowUsers = workflowDao.listNotViewedPlaybookUsers(workflow);
		sendWorkflowEmails(workflow, workflowUsers);
	}

	private void sendPartnerHasViewedPlaybookWorkflowEmails(Workflow workflow) {
		List<User> workflowUsers = workflowDao.listViewedPlaybookUsers(workflow);
		sendWorkflowEmails(workflow, workflowUsers);
	}

	private void sendViewedTrackWorkflowEmails(Workflow workflow) {
		TriggerComponent subject = workflow.getTriggerSubject();
		if (TriggerSubjectEnum.partner_has.name().equals(subject.getKey())) {
			sendPartnerHasViewedTrackWorkflowEmails(workflow);
		} else if (TriggerSubjectEnum.partner_has_not.name().equals(subject.getKey())) {
			sendPartnerHasNotViewedTrackWorkflowEmails(workflow);
		}
	}

	private void sendPartnerHasNotViewedTrackWorkflowEmails(Workflow workflow) {
		List<User> workflowUsers = workflowDao.listNotViewedTrackUsers(workflow);
		sendWorkflowEmails(workflow, workflowUsers);
	}

	private void sendPartnerHasViewedTrackWorkflowEmails(Workflow workflow) {
		List<User> workflowUsers = workflowDao.listViewedTrackUsers(workflow);
		sendWorkflowEmails(workflow, workflowUsers);
	}

	private void sendCompletedPlaybookWorkflowEmails(Workflow workflow) {
		TriggerComponent subject = workflow.getTriggerSubject();
		if (TriggerSubjectEnum.partner_has.name().equals(subject.getKey())) {
			sendPartnerHasCompletedPlaybookWorkflowEmails(workflow);
		} else if (TriggerSubjectEnum.partner_has_not.name().equals(subject.getKey())) {
			sendPartnerHasNotCompletedPlaybookWorkflowEmails(workflow);
		}
	}

	private void sendPartnerHasNotCompletedPlaybookWorkflowEmails(Workflow workflow) {
		List<User> workflowUsers = workflowDao.listNotCompletedPlaybookUsers(workflow);
		sendWorkflowEmails(workflow, workflowUsers);
	}

	private void sendPartnerHasCompletedPlaybookWorkflowEmails(Workflow workflow) {
		List<User> workflowUsers = workflowDao.listCompletedPlaybookUsers(workflow);
		sendWorkflowEmails(workflow, workflowUsers);
	}

	private void sendCompletedTrackWorkflowEmails(Workflow workflow) {
		TriggerComponent subject = workflow.getTriggerSubject();
		if (TriggerSubjectEnum.partner_has.name().equals(subject.getKey())) {
			sendPartnerHasCompletedTrackWorkflowEmails(workflow);
		} else if (TriggerSubjectEnum.partner_has_not.name().equals(subject.getKey())) {
			sendPartnerHasNotCompletedTrackWorkflowEmails(workflow);
		}
	}

	private void sendPartnerHasNotCompletedTrackWorkflowEmails(Workflow workflow) {
		List<User> workflowUsers = workflowDao.listNotCompletedTrackUsers(workflow);
		sendWorkflowEmails(workflow, workflowUsers);
	}

	private void sendPartnerHasCompletedTrackWorkflowEmails(Workflow workflow) {
		List<User> workflowUsers = workflowDao.listCompletedTrackUsers(workflow);
		sendWorkflowEmails(workflow, workflowUsers);
	}

	private void sendAddedContactWorkflowEmails(Workflow workflow) {
		TriggerComponent subject = workflow.getTriggerSubject();
		if (TriggerSubjectEnum.partner_has.name().equals(subject.getKey())) {
			sendPartnerHasAddedContactWorkflowEmails(workflow);
		} else if (TriggerSubjectEnum.partner_has_not.name().equals(subject.getKey())) {
			sendPartnerHasNotAddedContactWorkflowEmails(workflow);
		}
	}

	private void sendPartnerHasNotAddedContactWorkflowEmails(Workflow workflow) {
		List<User> workflowUsers = workflowDao.listNotAddedContactUsers(workflow);
		sendWorkflowEmails(workflow, workflowUsers);
	}

	private void sendPartnerHasAddedContactWorkflowEmails(Workflow workflow) {
		List<User> workflowUsers = workflowDao.listAddedContactUsers(workflow);
		sendWorkflowEmails(workflow, workflowUsers);
	}

	private void sendAddedTeamMemberWorkflowEmails(Workflow workflow) {
		TriggerComponent subject = workflow.getTriggerSubject();
		if (TriggerSubjectEnum.partner_has.name().equals(subject.getKey())) {
			sendPartnerHasAddedTeamMemberWorkflowEmails(workflow);
		} else if (TriggerSubjectEnum.partner_has_not.name().equals(subject.getKey())) {
			sendPartnerHasNotAddedTeamMemberWorkflowEmails(workflow);
		}
	}

	private void sendPartnerHasNotAddedTeamMemberWorkflowEmails(Workflow workflow) {
		List<User> workflowUsers = workflowDao.listNotAddedTeamMemberUsers(workflow);
		sendWorkflowEmails(workflow, workflowUsers);
	}

	private void sendPartnerHasAddedTeamMemberWorkflowEmails(Workflow workflow) {
		List<User> workflowUsers = workflowDao.listAddedTeamMemberUsers(workflow);
		sendWorkflowEmails(workflow, workflowUsers);
	}

	private void sendClosedDealWorkflowEmails(Workflow workflow) {
		TriggerComponent subject = workflow.getTriggerSubject();
		if (TriggerSubjectEnum.partner_has.name().equals(subject.getKey())) {
			sendPartnerHasClosedDealWorkflowEmails(workflow);
		} else if (TriggerSubjectEnum.partner_has_not.name().equals(subject.getKey())) {
			sendPartnerHasNotClosedDealWorkflowEmails(workflow);
		}
	}

	private void sendPartnerHasNotClosedDealWorkflowEmails(Workflow workflow) {
		List<User> workflowUsers = workflowDao.listNotClosedDealUsers(workflow);
		sendWorkflowEmails(workflow, workflowUsers);
	}

	private void sendPartnerHasClosedDealWorkflowEmails(Workflow workflow) {
		List<User> workflowUsers = workflowDao.listClosedDealUsers(workflow);
		sendWorkflowEmails(workflow, workflowUsers);
	}

	private void sendConvertedLeadWorkflowEmails(Workflow workflow) {
		TriggerComponent subject = workflow.getTriggerSubject();
		if (TriggerSubjectEnum.partner_has.name().equals(subject.getKey())) {
			sendPartnerHasConvertedLeadWorkflowEmails(workflow);
		} else if (TriggerSubjectEnum.partner_has_not.name().equals(subject.getKey())) {
			sendPartnerHasNotConvertedLeadWorkflowEmails(workflow);
		}
	}

	private void sendPartnerHasNotConvertedLeadWorkflowEmails(Workflow workflow) {
		List<User> workflowUsers = workflowDao.listNotConvertedLeadUsers(workflow);
		sendWorkflowEmails(workflow, workflowUsers);
	}

	private void sendPartnerHasConvertedLeadWorkflowEmails(Workflow workflow) {
		List<User> workflowUsers = workflowDao.listConvertedLeadUsers(workflow);
		sendWorkflowEmails(workflow, workflowUsers);
	}

	private void sendCreatedDealWorkflowEmails(Workflow workflow) {
		TriggerComponent subject = workflow.getTriggerSubject();
		if (TriggerSubjectEnum.partner_has.name().equals(subject.getKey())) {
			sendPartnerHasCreatedDealWorkflowEmails(workflow);
		} else if (TriggerSubjectEnum.partner_has_not.name().equals(subject.getKey())) {
			sendPartnerHasNotCreatedDealWorkflowEmails(workflow);
		}
	}

	private void sendPartnerHasNotCreatedDealWorkflowEmails(Workflow workflow) {
		List<User> workflowUsers = workflowDao.listNotCreatedDealUsers(workflow);
		sendWorkflowEmails(workflow, workflowUsers);
	}

	private void sendPartnerHasCreatedDealWorkflowEmails(Workflow workflow) {
		List<User> workflowUsers = workflowDao.listCreatedDealUsers(workflow);
		sendWorkflowEmails(workflow, workflowUsers);
	}

	private void sendCreatedLeadWorkflowEmails(Workflow workflow) {
		TriggerComponent subject = workflow.getTriggerSubject();
		if (TriggerSubjectEnum.partner_has.name().equals(subject.getKey())) {
			sendPartnerHasCreatedLeadWorkflowEmails(workflow);
		} else if (TriggerSubjectEnum.partner_has_not.name().equals(subject.getKey())) {
			sendPartnerHasNotCreatedLeadWorkflowEmails(workflow);
		}
	}

	private void sendPartnerHasNotCreatedLeadWorkflowEmails(Workflow workflow) {
		List<User> workflowUsers = workflowDao.listNotCreatedLeadUsers(workflow);
		sendWorkflowEmails(workflow, workflowUsers);
	}

	private void sendPartnerHasCreatedLeadWorkflowEmails(Workflow workflow) {
		List<User> workflowUsers = workflowDao.listCreatedLeadUsers(workflow);
		sendWorkflowEmails(workflow, workflowUsers);
	}

	private void sendCreatedCompanyProfileWorkflowEmails(Workflow workflow) {
		TriggerComponent subject = workflow.getTriggerSubject();
		if (TriggerSubjectEnum.partner_has.name().equals(subject.getKey())) {
			sendPartnerHasCreatedCompanyProfileWorkflowEmails(workflow);
		} else if (TriggerSubjectEnum.partner_has_not.name().equals(subject.getKey())) {
			sendPartnerHasNotCreatedCompanyProfileWorkflowEmails(workflow);
		}
	}

	private void sendPartnerHasCreatedCompanyProfileWorkflowEmails(Workflow workflow) {
		List<User> workflowUsers = workflowDao.listCreatedCompanyProfileUsers(workflow);
		sendWorkflowEmails(workflow, workflowUsers);
	}

	private void sendPartnerHasNotCreatedCompanyProfileWorkflowEmails(Workflow workflow) {
		List<User> workflowUsers = workflowDao.listNotCreatedCompanyProfileUsers(workflow);
		sendWorkflowEmails(workflow, workflowUsers);
	}

	private void sendSignedUpWorkflowEmails(Workflow workflow) {
		TriggerComponent subject = workflow.getTriggerSubject();
		if (TriggerSubjectEnum.partner_has.name().equals(subject.getKey())) {
			sendPartnerHasSignedUpWorkflowEmails(workflow);
		} else if (TriggerSubjectEnum.partner_has_not.name().equals(subject.getKey())) {
			sendPartnerHasNotSignedUpWorkflowEmails(workflow);
		}
	}

	private void sendPartnerHasSignedUpWorkflowEmails(Workflow workflow) {
//		String loggerSuffix = " -(" + new Date() + ")";
		List<User> workflowUsers = workflowDao.listSignedUpUsers(workflow);
		sendWorkflowEmails(workflow, workflowUsers);
	}

	private void sendPartnerHasNotSignedUpWorkflowEmails(Workflow workflow) {
		String loggerSuffix = " -(" + new Date() + ")";
		List<User> workflowUsers = workflowDao.listNotSignedUpUsers(workflow);
		sendWorkflowEmails(workflow, workflowUsers);
	}

	private void sendRedistributedCampaignWorkflowEmails(Workflow workflow) {
		TriggerComponent subject = workflow.getTriggerSubject();
		if (TriggerSubjectEnum.partner_has.name().equals(subject.getKey())) {
			sendPartnerHasRedistributedCampaignWorkflowEmails(workflow);
		} else if (TriggerSubjectEnum.partner_has_not.name().equals(subject.getKey())) {
			sendPartnerHasNotRedistributedCampaignWorkflowEmails(workflow);
		}
	}

	private void sendPartnerHasRedistributedCampaignWorkflowEmails(Workflow workflow) {
		List<User> workflowUsers = workflowDao.listRedistributedCampaignUsers(workflow);
		sendWorkflowEmails(workflow, workflowUsers);
	}

	private void sendPartnerHasNotRedistributedCampaignWorkflowEmails(Workflow workflow) {
		List<User> workflowUsers = workflowDao.listNotRedistributedCampaignUsers(workflow);
		sendWorkflowEmails(workflow, workflowUsers);
	}

	private void sendActivatedWorkflowEmails(Workflow workflow) {
		TriggerComponent subject = workflow.getTriggerSubject();
		if (TriggerSubjectEnum.partner_has.name().equals(subject.getKey())) {
			sendPartnerHasActivatedWorkflowEmails(workflow);
		} else if (TriggerSubjectEnum.partner_has_not.name().equals(subject.getKey())) {
			sendPartnerHasNotActivatedWorkflowEmails(workflow);
		}
	}

	private void sendPartnerHasActivatedWorkflowEmails(Workflow workflow) {
//		String loggerSuffix = " -(" + new Date() + ")";
		List<User> workflowUsers = workflowDao.listActivatedUsers(workflow);
		sendWorkflowEmails(workflow, workflowUsers);
	}

	private void sendPartnerHasNotActivatedWorkflowEmails(Workflow workflow) {
//		String loggerSuffix = " -(" + new Date() + ")";
		List<User> workflowUsers = workflowDao.listNotActivatedUsers(workflow);
		sendWorkflowEmails(workflow, workflowUsers);
	}

	private void sendWorkflowEmails(Workflow workflow, List<User> workflowUsers) {
		if (workflowUsers != null && !workflowUsers.isEmpty()) {
			logger.debug("Processing Emails for Workflow Id :-" + workflow.getId());
			logger.debug("Total Emails count for Workflow Id :" + workflow.getId() + " :: " + workflowUsers.size());
			User sender = getSender(workflow);
			EmailTemplateDTO emailTemplateDTO = frameEmailTemplateDTO(workflow, sender);
			for (User receiver : workflowUsers) {
				asyncService.sendWorkflowEmail(sender, receiver, emailTemplateDTO);
			}
		}
	}

	private User getSender(Workflow workflow) {
		Integer adminId = utilDao.findAdminIdByCompanyId(workflow.getCompany().getId());
		User sender = userService.loadUser(Arrays.asList(new Criteria("userId", OPERATION_NAME.eq, adminId)),
				new FindLevel[] { FindLevel.COMPANY_PROFILE });
		return sender;
	}

	private EmailTemplateDTO frameEmailTemplateDTO(Workflow workflow, User sender) {
		EmailTemplateDTO emailTemplateDTO = new EmailTemplateDTO();
		MyMergeTagsInfo senderMergeTagInfo = XamplifyUtils.getMyMergeTagsData(sender);
		String subject = workflow.getNotificationSubject();
		subject = XamplifyUtils.replaceSenderMergeTags(subject, senderMergeTagInfo);
		emailTemplateDTO.setSubject(subject);

		String body = "";
		EmailTemplate template = workflow.getTemplate();
		if (template != null) {
			body = template.getBody();
		} else {
			body = workflow.getNotificationMessage();
		}
		body = XamplifyUtils.replaceSenderMergeTags(body, senderMergeTagInfo);
		body = utilService.addParametersToUnsubscribeLink(body, "\\{\\{unsubscribeLink\\}\\}", webUrl,
				sender.getAlias(), sender.getCompanyProfile().getId());
		body = utilService.replaceCompanyLogos(body, sender.getCompanyProfile().getCompanyLogoPath());
		if (StringUtils.hasText(workflow.getPreHeader())) {
			body = PRE_HEADER + body;
			body = body.replaceAll("\\{\\{dynamic_pre_header\\}\\}",
					XamplifyUtils.escapeDollarSequece(workflow.getPreHeader()));
		}
		emailTemplateDTO.setBody(body);
		return emailTemplateDTO;
	}

}
