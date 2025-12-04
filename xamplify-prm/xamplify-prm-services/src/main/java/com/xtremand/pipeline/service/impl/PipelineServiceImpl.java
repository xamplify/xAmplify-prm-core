package com.xtremand.pipeline.service.impl;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.xtremand.common.bom.CompanyProfile;
import com.xtremand.common.bom.Criteria;
import com.xtremand.common.bom.Criteria.OPERATION_NAME;
import com.xtremand.common.bom.FindLevel;
import com.xtremand.common.bom.Pagination;
import com.xtremand.dao.util.GenericDAO;
import com.xtremand.deal.bom.Deal;
import com.xtremand.formbeans.XtremandResponse;
import com.xtremand.integration.bom.Integration;
import com.xtremand.integration.bom.Integration.IntegrationType;
import com.xtremand.integration.dao.IntegrationDao;
import com.xtremand.integration.dto.IntegrationDTO;
import com.xtremand.lead.bom.Lead;
import com.xtremand.lead.bom.Pipeline;
import com.xtremand.lead.bom.PipelineStage;
import com.xtremand.lead.bom.PipelineType;
import com.xtremand.lead.dto.PipelineDto;
import com.xtremand.lead.dto.PipelineRequestDTO;
import com.xtremand.lead.dto.PipelineResponseDTO;
import com.xtremand.lead.dto.PipelineStageDto;
import com.xtremand.lead.dto.PipelineStageResponseDTO;
import com.xtremand.partnership.bom.Partnership;
import com.xtremand.partnership.dao.PartnershipDAO;
import com.xtremand.pipeline.dao.PipelineDAO;
import com.xtremand.pipeline.service.PipelineService;
import com.xtremand.team.dao.TeamDao;
import com.xtremand.user.bom.Role;
import com.xtremand.user.bom.User;
import com.xtremand.user.dao.UserDAO;
import com.xtremand.user.service.UserService;
import com.xtremand.util.DateUtils;
import com.xtremand.util.XamplifyUtils;
import com.xtremand.util.dto.XamplifyConstants;

@Service("PipelineService")
@Transactional
public class PipelineServiceImpl implements PipelineService {
	private static final Logger logger = LoggerFactory.getLogger(PipelineServiceImpl.class);

	private static final String UNAUTHORIZED = "UnAuthorized";
	private static final String SUCCESS = "Success";
	private static final String FAILED = "Failed";
	private static final String INVALID_INPUT = "Invalid Input";
	private static final String DUPLICATE_NAME = "Duplicate Pipeline Name";
	private static final String DUPLICATE_STAGE_NAME = "Duplicate Stage Name(s) are not allowed";
	private static final String NOT_FOUND = "Not Found";
	private static final String INVALID_NAME = "Please Enter Stage Name(s).";
	private String pipeLineCannotBeMarkedErrorMessage = "This pipeline can not be marked as private as your partners created deal(s) on this pipeline.";

	@Autowired
	private UserService userService;

	@Autowired
	PartnershipDAO partnershipDAO;

	@Autowired
	PipelineDAO pipelineDAO;

	@Autowired
	private UserDAO userDao;

	@Autowired
	private GenericDAO genericDAO;

	@Autowired
	private TeamDao teamDao;

	@Autowired
	private IntegrationDao integrationDao;

	@Value("${pipeline.delete.campaign.exist.error}")
	private String CAMPAIGN_EXIST_LIST_ERROR;

	@Value("${pipeline.delete.lead.exist.error}")
	private String LEAD_EXIST_LIST_ERROR;

	@Value("${pipeline.delete.deal.exist.error}")
	private String DEAL_EXIST_LIST_ERROR;

	@Value("${salesforce.refreshtoken.expired}")
	private String REFRESH_TOKEN_EXPIRED;

	@Override
	public XtremandResponse getPipeLines(Integer loggedInUserId, Integer companyId, PipelineType type) {
		XtremandResponse response = new XtremandResponse();
		String responseMessage = FAILED;
		Integer responseStatusCode = 400;
		if (loggedInUserId != null && loggedInUserId > 0 && type != null) {
			Integer loggedInCompanyId = userService.getCompanyIdByUserId(loggedInUserId);
			if (loggedInCompanyId != null) {
				if (companyId == null) {
					companyId = loggedInCompanyId;
				}
				List<Pipeline> pipelines = null;
				if (!loggedInCompanyId.equals(companyId)) {
					Partnership partnership = partnershipDAO.checkPartnership(companyId, loggedInCompanyId);
					if (partnership != null) {
						pipelines = pipelineDAO.getPipeLines(companyId, type, false);
					} else {
						responseMessage = UNAUTHORIZED;
						responseStatusCode = 401;
					}
				} else {
					pipelines = pipelineDAO.getPipeLines(companyId, type, null);
				}
				response.setData(getPipelineDtoList(pipelines, loggedInCompanyId, true));
				responseMessage = SUCCESS;
				responseStatusCode = 200;
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
	public Map<String, Object> getPipeLinesForVendor(Pagination pagination) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		if (pagination != null && pagination.getUserId() != null && pagination.getUserId() > 0
				&& !StringUtils.isBlank(pagination.getPipelineType())) {
			Integer companyId = userService.getCompanyIdByUserId(pagination.getUserId());
			if (companyId != null) {
				pagination.setCompanyId(companyId);
				resultMap = pipelineDAO.getPipeLinesForVendor(pagination);
				if (resultMap != null && !resultMap.isEmpty() && resultMap.get("data") != null) {
					List<Pipeline> pipelines = (List<Pipeline>) resultMap.get("data");
					if (pipelines != null && !pipelines.isEmpty()) {
						resultMap.put("data", getPipelineDtoList(pipelines, pagination.getUserId(), true));
					}
				}
			}
		}
		return resultMap;
	}

	private List<PipelineDto> getPipelineDtoList(List<Pipeline> pipelines, Integer loggedInUserId, boolean inDetail) {
		List<PipelineDto> pipelineDtoList = new ArrayList<>();
		if (pipelines != null) {
			for (Pipeline pipeline : pipelines) {
				PipelineDto pipelineDto = getPipelineDto(pipeline, loggedInUserId, inDetail);
				if (pipelineDto != null) {
					pipelineDtoList.add(pipelineDto);
				}
			}
		}
		return pipelineDtoList;
	}

	private PipelineDto getPipelineDto(Pipeline pipeline, Integer loggedInUserId, boolean inDetail) {
		PipelineDto pipelineDto = null;
		if (pipeline != null) {
			pipelineDto = new PipelineDto();
			pipelineDto.setId(pipeline.getId());
			pipelineDto.setName(pipeline.getName());
			pipelineDto.setType(pipeline.getType().name().toLowerCase());
			pipelineDto.setPrivate(pipeline.isPrivate());
			pipelineDto.setCompanyId(pipeline.getCompany().getId());
			pipelineDto.setSalesforcePipeline(pipeline.isSalesforcePipeline());
			pipelineDto.setDefault(pipeline.isDefault());
			pipelineDto.setIntegrationType(pipeline.getIntegrationType().name());

			if (inDetail) {
				Boolean canUpdate = canUpdatePipeline(pipeline, loggedInUserId);
				pipelineDto.setCanUpdate(canUpdate);
				pipelineDto.setCanDelete(canDeletePipeline(pipeline, loggedInUserId));
				pipelineDto.setCanDeleteStages(canDeleteStages(pipeline, loggedInUserId));
				Boolean isCRMPipeline = isCRMPipeline(pipeline, loggedInUserId);
				pipelineDto.setCanSync(isCRMPipeline);
				pipelineDto.setCrmPipeline(isCRMPipeline);
				Boolean enablePrivateCheckBox = setEnablePrivateCheckBox(pipeline, loggedInUserId);
				pipelineDto.setEnablePrivateCheckBox(enablePrivateCheckBox);
				Boolean showSinglePublicPipelineMessage = (pipeline.getIntegrationType().name().equals("HUBSPOT")
						|| pipeline.getIntegrationType().name().equals("PIPEDRIVE")) && !enablePrivateCheckBox;
				pipelineDto.setShowSinglePublicPipelineMessage(showSinglePublicPipelineMessage);
				fillCreatedByDetailsInDTO(pipelineDto, pipeline);
				pipelineDto.setCreatedTime(DateUtils.getUtcString(pipeline.getCreatedTime()));
			}

			List<PipelineStageDto> stagesDtoList = new ArrayList<>();
			List<PipelineStage> stages = pipeline.getStages();
			for (PipelineStage stage : stages) {
				if (canViewStage(pipeline, loggedInUserId, stage)) {
					PipelineStageDto stageDto = new PipelineStageDto();
					stageDto.setId(stage.getId());
					stageDto.setStageName(stage.getStageName());
					stageDto.setDefaultStage(stage.isDefaultStage());
					stageDto.setWon(stage.isWon());
					stageDto.setLost(stage.isLost());
					stageDto.setDisplayIndex(stage.getDisplayIndex());
					stageDto.setCanDelete(canDeleteStage(stage, pipeline.getType()));
					stageDto.setNonInteractive(stage.isNonInteractive());
					stageDto.setPrivate(stage.isPrivate());
					stagesDtoList.add(stageDto);
				}
			}
			pipelineDto.setStages(stagesDtoList);
		}
		return pipelineDto;
	}

	private boolean canViewStage(Pipeline pipeline, Integer loggedInUserId, PipelineStage stage) {
		boolean canView = false;
		Integer loggedInCompanyId = userService.getCompanyIdByUserId(loggedInUserId);
		if (stage != null) {
			if (pipeline.getCompany().getId().equals(loggedInCompanyId)) {
				canView = true;
			} else {
				if (!stage.isPrivate()) {
					canView = true;
				}
			}
		}
		return canView;
	}

	private Boolean setEnablePrivateCheckBox(Pipeline pipeline, Integer loggedInUserId) {
		boolean enablePrivateCheckBox = false;
		Integer loggedInCompanyId = userService.getCompanyIdByUserId(loggedInUserId);
		if (loggedInCompanyId != null && pipeline != null) {
			enablePrivateCheckBox = true;
		}
		return enablePrivateCheckBox;
	}

	private boolean canDeleteStage(PipelineStage stage, PipelineType pipelineType) {
		boolean canDelete = false;
		List<Lead> leadsOnStage = null;
		List<Deal> dealsOnStage = null;
		if (pipelineType == PipelineType.LEAD) {
			leadsOnStage = stage.getLeads();
		} else {
			dealsOnStage = stage.getDeals();
		}

		if ((leadsOnStage == null || (leadsOnStage != null && leadsOnStage.isEmpty()))
				&& (dealsOnStage == null || (dealsOnStage != null && dealsOnStage.isEmpty()))) {
			canDelete = true;
		}
		return canDelete;

	}

	private void fillCreatedByDetailsInDTO(PipelineDto pipelineDto, Pipeline pipeline) {
		User createdUser = userService.loadUser(
				Arrays.asList(new Criteria("userId", OPERATION_NAME.eq, pipeline.getCreatedBy())),
				new FindLevel[] { FindLevel.COMPANY_PROFILE });
		if (createdUser != null) {
			String name = "";
			if (createdUser.getFirstName() != null) {
				name = createdUser.getFirstName() + " ";
			}
			if (createdUser.getLastName() != null) {
				name = name + createdUser.getLastName();
			}

			pipelineDto.setCreatedByName(name);
			pipelineDto.setCreatedByEmail(createdUser.getEmailId());
		}
	}

	private boolean canDeleteStages(Pipeline pipeline, Integer loggedInUserId) {
		Boolean canDeleteStages = false;
		if (pipeline != null && loggedInUserId != null) {
			List<Lead> leadsOnPipeline = null;
			List<Deal> dealsOnPipeline = null;
			if (pipeline.getType() == PipelineType.LEAD) {
				leadsOnPipeline = pipeline.getLeads();
			} else {
				dealsOnPipeline = pipeline.getDeals();
			}
			if ((leadsOnPipeline == null || (leadsOnPipeline != null && leadsOnPipeline.isEmpty()))
					&& (dealsOnPipeline == null || (dealsOnPipeline != null && dealsOnPipeline.isEmpty()))) {
				canDeleteStages = true;
			}
		}
		return canDeleteStages;
	}

	@Override
	public XtremandResponse savePipeline(PipelineDto pipelineDto) {
		XtremandResponse response = new XtremandResponse();
		String responseMessage = FAILED;
		Integer responseStatusCode = 400;
		if (hasValidStageNames(pipelineDto.getStages())) {
			if (validateSavePipelineRequest(pipelineDto)) {
				Integer loggedInUserId = pipelineDto.getUserId();
				CompanyProfile loggedInCompany = userService.getCompanyProfileByUser(loggedInUserId);
				if (loggedInCompany != null && checkLoggedInUserRole(loggedInUserId)) {
					String pipelineName = pipelineDto.getName().trim();
					Pipeline existingPipeline = pipelineDAO.getPipeLineByName(loggedInCompany.getId(), pipelineName,
							PipelineType.valueOf(pipelineDto.getType()));
					if (existingPipeline == null) {
						if (!haveDuplicates(pipelineDto.getStages())) {
							createPipeline(pipelineDto, loggedInUserId, loggedInCompany);
							responseMessage = SUCCESS;
							responseStatusCode = 200;
						} else {
							responseMessage = DUPLICATE_STAGE_NAME;
							responseStatusCode = 500;
						}
					} else {
						responseMessage = DUPLICATE_NAME;
						responseStatusCode = 500;
					}
				} else {
					responseMessage = UNAUTHORIZED;
					responseStatusCode = 401;
				}
			} else {
				responseMessage = INVALID_INPUT;
				responseStatusCode = 500;
			}
		} else {
			responseMessage = INVALID_NAME;
			responseStatusCode = 500;
		}
		response.setMessage(responseMessage);
		response.setStatusCode(responseStatusCode);
		return response;
	}

	private void createPipeline(PipelineDto pipelineDto, Integer loggedInUserId, CompanyProfile loggedInCompany) {
		Pipeline pipeline = new Pipeline();
		pipeline.setName(pipelineDto.getName());
		pipeline.setType(PipelineType.valueOf(pipelineDto.getType()));
		pipeline.setCompany(loggedInCompany);
		pipeline.setCreatedBy(loggedInUserId);
		pipeline.setPrivate(pipelineDto.isPrivate());
		pipeline.setIntegrationType(IntegrationType.XAMPLIFY);
		pipeline.initialiseCommonFields(true, loggedInUserId);
		genericDAO.save(pipeline);
		int stageindex = 1;
		for (PipelineStageDto stageDto : pipelineDto.getStages()) {
			if (stageDto != null && !StringUtils.isBlank(stageDto.getStageName())) {
				stageDto.setDisplayIndex(stageindex++);
				createStage(stageDto, pipeline, loggedInUserId);
			}
		}
	}

	private void createStage(PipelineStageDto stageDto, Pipeline pipeline, Integer loggedInUserId) {
		PipelineStage stage = new PipelineStage();
		try {
			BeanUtils.copyProperties(stage, stageDto);
			if (stage.isWon()) {
				stage.setLost(false);
			}
			stage.setPipeline(pipeline);
			stage.setCreatedBy(loggedInUserId);
			stage.initialiseCommonFields(true, loggedInUserId);
			genericDAO.save(stage);
		} catch (IllegalAccessException | InvocationTargetException e) {
			logger.error("Error in createStage() " + e.getMessage());
		}
	}

	private boolean checkLoggedInUserRole(Integer userId) {
		boolean isTeamMember = teamDao.isTeamMember(userId);
		if (isTeamMember) {
			Integer superiorId = teamDao.getOrgAdminIdByTeamMemberId(userId);
			List<Integer> roles = userDao.getRoleIdsByUserId(superiorId);
			return isAnyAdmin(roles);
		} else {
			List<Integer> roles = userDao.getRoleIdsByUserId(userId);
			return isAnyAdmin(roles);
		}
	}

	private boolean isAnyAdmin(List<Integer> roles) {
		return roles.contains(Role.PRM_ROLE.getRoleId()) || roles.contains(Role.COMPANY_PARTNER.getRoleId());
	}

	@Override
	public XtremandResponse updatePipeline(PipelineDto pipelineDto) {
		XtremandResponse response = new XtremandResponse();
		String responseMessage = FAILED;
		Integer responseStatusCode = 400;
		if (hasValidStageNames(pipelineDto.getStages())) {
			if (validateUpdatePipelineRequest(pipelineDto)) {
				Integer loggedInUserId = pipelineDto.getUserId();
				Pipeline pipeLine = genericDAO.get(Pipeline.class, pipelineDto.getId());
				CompanyProfile loggedInCompany = userService.getCompanyProfileByUser(loggedInUserId);
				if (loggedInCompany != null && canUpdatePipeline(pipeLine, loggedInUserId)
						&& checkLoggedInUserRole(loggedInUserId)) {
					Pipeline duplicatePipeline = pipelineDAO.getPipeLineByName(loggedInCompany.getId(),
							pipelineDto.getName(), pipeLine.getType());
					if (duplicatePipeline == null || duplicatePipeline.getId() == pipeLine.getId()) {

						if (!haveDuplicates(pipelineDto.getStages())) {
							if (!hasDealsOnPrivatePipeline(pipeLine, pipelineDto)) {
								pipelineDAO.clearDisplayIndex(pipeLine.getId());
								pipeLine = genericDAO.get(Pipeline.class, pipelineDto.getId());
								updatePipeline(pipeLine, pipelineDto, loggedInUserId);
								responseMessage = SUCCESS;
								responseStatusCode = 200;
							} else {
								responseMessage = pipeLineCannotBeMarkedErrorMessage;
								responseStatusCode = 500;
							}
						} else {
							responseMessage = DUPLICATE_STAGE_NAME;
							responseStatusCode = 500;
						}
					} else {
						responseMessage = DUPLICATE_NAME;
						responseStatusCode = 500;
					}
				} else {
					responseMessage = UNAUTHORIZED;
					responseStatusCode = 401;
				}
			} else {
				responseMessage = INVALID_INPUT;
				responseStatusCode = 500;
			}
		} else {
			responseMessage = INVALID_NAME;
			responseStatusCode = 500;
		}
		response.setMessage(responseMessage);
		response.setStatusCode(responseStatusCode);
		return response;
	}

	private boolean hasDealsOnPrivatePipeline(Pipeline pipeline, PipelineDto pipelineDto) {
		boolean hasDeals = false;
		if (!pipeline.isPrivate() && pipelineDto.isPrivate()) {
			if (pipeline.getType().equals(PipelineType.DEAL)) {
				hasDeals = pipelineDAO.hasPartnerCreatedDealsOnPipeline(pipeline.getId(),
						pipeline.getCompany().getId());
			} else {
				hasDeals = pipelineDAO.hasPartnerCreatedLeadsOnPipeline(pipeline.getId(),
						pipeline.getCompany().getId());
			}

			if (hasDeals) {
				pipeLineCannotBeMarkedErrorMessage = "This pipeline can not be marked as private as your partners created "
						+ pipeline.getType().name().toLowerCase() + "(s) on this pipeline.";
			}
		}
		if (!hasDeals) {
			Map<Integer, PipelineStage> existingStagesMap = pipeline.getStages().stream()
					.collect(Collectors.toMap(PipelineStage::getId, c -> c));
			List<PipelineStageDto> newStages = pipelineDto.getStages();
			if (newStages != null && !newStages.isEmpty()) {
				for (PipelineStageDto newStage : newStages) {
					if (newStage != null && newStage.getId() != null && newStage.getId() > 0 && newStage.isPrivate()) {

						PipelineStage existingStage = existingStagesMap.get(newStage.getId());
						if (existingStage != null && !existingStage.isPrivate()) {
							if (pipeline.getType().equals(PipelineType.DEAL)) {
								hasDeals = pipelineDAO.hasPartnerCreatedDealsOnPipelineStage(newStage.getId(),
										pipeline.getCompany().getId());
							} else {
								hasDeals = pipelineDAO.hasPartnerCreatedLeadsOnPipelineStage(newStage.getId(),
										pipeline.getCompany().getId());
							}
							if (hasDeals) {
								pipeLineCannotBeMarkedErrorMessage = newStage.getStageName()
										+ " can not be marked as private as your partners created "
										+ pipeline.getType().name().toLowerCase() + "(s) on this stage.";
								break;
							}
						}
					}
				}
			}
		}
		return hasDeals;
	}

	private void updatePipeline(Pipeline pipeline, PipelineDto pipelineDto, Integer loggedInUserId) {
		pipeline.setName(pipelineDto.getName());
		pipeline.setPrivate(pipelineDto.isPrivate());
		List<PipelineStage> existingStages = pipeline.getStages();
		Map<Integer, PipelineStage> existingStagesMap = existingStages.stream()
				.collect(Collectors.toMap(PipelineStage::getId, c -> c));
		List<PipelineStageDto> pipelineStageDtoList = pipelineDto.getStages();
		Set<Integer> retainIds = new HashSet<>();
		// clearDisplayIndex(pipelineStageDtoList, existingStagesMap);
		int stageIndex = 1;
		for (PipelineStageDto pipelineStageDto : pipelineStageDtoList) {
			if (pipelineStageDto != null) {
				pipelineStageDto.setDisplayIndex(stageIndex++);
				if (pipelineStageDto.getId() != null) {
					updatePipelineStage(existingStagesMap.get(pipelineStageDto.getId()), pipelineStageDto,
							loggedInUserId);
					retainIds.add(pipelineStageDto.getId());
				} else {
					createStage(pipelineStageDto, pipeline, loggedInUserId);
				}
			}
		}

		Set<Integer> existingIds = existingStagesMap.keySet();
		existingIds.removeAll(retainIds);
		for (Integer existingId : existingIds) {
			genericDAO.remove(existingStagesMap.get(existingId));
		}

	}

	private void updatePipelineStage(PipelineStage pipelineStage, PipelineStageDto pipelineStageDto,
			Integer loggedInUserId) {
		if (!StringUtils.isBlank(pipelineStageDto.getStageName())
				&& !pipelineStage.getStageName().equals(pipelineStageDto.getStageName())) {
			pipelineStage.setStageName(pipelineStageDto.getStageName());
		}
		if (pipelineStage.isDefaultStage() != pipelineStageDto.isDefaultStage()) {
			pipelineStage.setDefaultStage(pipelineStageDto.isDefaultStage());
		}
		if (pipelineStage.isLost() != pipelineStageDto.isLost()) {
			pipelineStage.setLost(pipelineStageDto.isLost());
		}
		if (pipelineStage.isWon() != pipelineStageDto.isWon()) {
			pipelineStage.setWon(pipelineStageDto.isWon());
		}

		if (pipelineStage.isNonInteractive() != pipelineStageDto.isNonInteractive()) {
			pipelineStage.setNonInteractive(pipelineStageDto.isNonInteractive());
		}
		if (pipelineStage.isPrivate() != pipelineStageDto.isPrivate()) {
			pipelineStage.setPrivate((pipelineStageDto.isPrivate()));
		}
		pipelineStage.setDisplayIndex(pipelineStageDto.getDisplayIndex());
		pipelineStage.initialiseCommonFields(false, loggedInUserId);
	}

	private boolean haveDuplicates(List<PipelineStageDto> stageDtoList) {
		boolean haveDuplicates = false;
		List<String> stageNames = stageDtoList.stream()
				.map(PipelinestageDto -> PipelinestageDto.getStageName().toLowerCase()).collect(Collectors.toList());
		Set<String> stageNameSet = new HashSet<String>(stageNames);
		if (stageNames.size() != stageNameSet.size()) {
			haveDuplicates = true;
		}
		return haveDuplicates;
	}

	private boolean validateSavePipelineRequest(PipelineDto pipelineDto) {
		boolean valid = false;
		if (pipelineDto != null && pipelineDto.getUserId() != null && pipelineDto.getUserId() > 0
				&& !StringUtils.isBlank(pipelineDto.getName()) && !StringUtils.isBlank(pipelineDto.getType())
				&& pipelineDto.getStages() != null && !pipelineDto.getStages().isEmpty()) {
			validateStages(pipelineDto.getStages());
			if (hasValidStageNames(pipelineDto.getStages()) && validateDefaultStage(pipelineDto.getStages())) {
				valid = true;
			}
		}
		return valid;
	}

	private boolean validateStages(List<PipelineStageDto> stages) {
		boolean isValid = true;
		int defaultStages = 0;
		if (stages != null && !stages.isEmpty()) {
			// int displayIndexes[] = new int[stages.size()];
			HashSet<Object> displayIndexes = new HashSet<>();
			for (PipelineStageDto stage : stages) {
				if (stage != null) {
					if (stage.getStageName() == null || stage.getStageName().trim().isEmpty()) {
						isValid = false;
						break;
					}
					if (stage.isDefaultStage()) {
						defaultStages++;
					}
					displayIndexes.add(stage.getDisplayIndex());
				}
			}
			if (defaultStages != 1) {
				isValid = false;
			}
		}
		return isValid;
	}

	private boolean validateDefaultStage(List<PipelineStageDto> stages) {
		int defaultStages = stages.stream().map(PipelineStageDto::isDefaultStage).filter(x -> (x))
				.collect(Collectors.toList()).size();
		if (defaultStages == 0) {
			stages.get(0).setDefaultStage(true);
			defaultStages = 1;
		}
		return (defaultStages == 1);
	}

	private boolean hasValidStageNames(List<PipelineStageDto> stages) {
		stages.removeAll(Collections.singleton(null));

		List<String> invalidNames = stages.stream().map(PipelineStageDto::getStageName)
				.filter(x -> (x == null || x.trim().isEmpty())).collect(Collectors.toList());
		return invalidNames.isEmpty();
	}

	private boolean validateUpdatePipelineRequest(PipelineDto pipelineDto) {
		boolean valid = false;
		if (pipelineDto != null && pipelineDto.getUserId() != null && pipelineDto.getUserId() > 0
				&& pipelineDto.getId() != null && pipelineDto.getId() > 0 && !StringUtils.isBlank(pipelineDto.getName())
				&& pipelineDto.getStages() != null && !pipelineDto.getStages().isEmpty()) {
			if (hasValidStageNames(pipelineDto.getStages()) && validateDefaultStage(pipelineDto.getStages())) {
				valid = true;
			}
		}
		return valid;
	}

	private Boolean canUpdatePipeline(Pipeline pipeline, Integer loggedInUserId) {
		Boolean canUpdate = false;
		if (pipeline != null && loggedInUserId != null && !pipeline.isDefault()) {
			List<Integer> superiorIds = partnershipDAO.getSuperiorIds(pipeline.getCompany().getId());
			// List<Integer> adminIds =
			// userService.getOrgAdminIds(socialStatus.getUserId());
			if (superiorIds.contains(loggedInUserId)
					|| pipeline.getCreatedBy().intValue() == loggedInUserId.intValue()) {
				canUpdate = true;
			}
		}
		return canUpdate;
	}

	private Boolean canDeletePipeline(Pipeline pipeline, Integer loggedInUserId) {
		Boolean canDelete = false;
		if (pipeline != null && loggedInUserId != null && !isCRMPipeline(pipeline, loggedInUserId)
				&& !pipeline.isDefault()) {
			List<Integer> superiorIds = partnershipDAO.getSuperiorIds(pipeline.getCompany().getId());
			// List<Integer> adminIds =
			// userService.getOrgAdminIds(socialStatus.getUserId());
			if (superiorIds.contains(loggedInUserId)
					|| pipeline.getCreatedBy().intValue() == loggedInUserId.intValue()) {
				canDelete = true;
			}
		}
		return canDelete;
	}

	@Override
	public XtremandResponse deletePipeline(PipelineDto pipelineDto) {
		XtremandResponse response = new XtremandResponse();
		String responseMessage = FAILED;
		Integer responseStatusCode = 400;
		if (pipelineDto != null && pipelineDto.getId() != null && pipelineDto.getId() > 0
				&& pipelineDto.getUserId() != null && pipelineDto.getUserId() > 0) {
			Integer loggedInUserId = pipelineDto.getUserId();
			Pipeline pipeLine = genericDAO.get(Pipeline.class, pipelineDto.getId());
			if (canDeletePipeline(pipeLine, loggedInUserId) && checkLoggedInUserRole(loggedInUserId)) {
				List<Lead> leadsOnPipeline = null;
				List<Deal> dealsOnPipeline = null;
				if (pipeLine.getType() == PipelineType.LEAD) {
					leadsOnPipeline = pipeLine.getLeads();
				} else {
					dealsOnPipeline = pipeLine.getDeals();
				}
				if ((leadsOnPipeline == null || (leadsOnPipeline != null && leadsOnPipeline.isEmpty()))
						&& (dealsOnPipeline == null || (dealsOnPipeline != null && dealsOnPipeline.isEmpty()))) {
					genericDAO.remove(pipeLine);
					responseMessage = SUCCESS;
					responseStatusCode = 200;
				} else {
					if (pipeLine.getType() == PipelineType.LEAD) {
						responseMessage = LEAD_EXIST_LIST_ERROR;
					} else {
						responseMessage = DEAL_EXIST_LIST_ERROR;
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
	public XtremandResponse getPipeLine(Integer loggedInUserId, Integer pipelineId) {
		XtremandResponse response = new XtremandResponse();
		String responseMessage = FAILED;
		Integer responseStatusCode = 400;
		if (pipelineId != null && pipelineId > 0 && loggedInUserId != null && loggedInUserId > 0) {
			Pipeline pipeline = genericDAO.get(Pipeline.class, pipelineId);
			if (canViewPipeline(pipeline, loggedInUserId)) {
				response.setData(getPipelineDto(pipeline, loggedInUserId, true));
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

	private boolean canViewPipeline(Pipeline pipeline, Integer loggedInUserId) {
		boolean canViewPipeline = false;
		Integer loggedInCompanyId = userService.getCompanyIdByUserId(loggedInUserId);
		if (loggedInCompanyId != null) {
			if (loggedInCompanyId.intValue() != pipeline.getCompany().getId().intValue()) {
				if (!pipeline.isPrivate()) {
					Partnership partnership = partnershipDAO.checkPartnership(pipeline.getCompany().getId(),
							loggedInCompanyId);
					if (partnership != null) {
						canViewPipeline = true;
					}
				}
			} else {
				canViewPipeline = true;
			}
		}
		return canViewPipeline;
	}

	private boolean isCRMPipeline(Pipeline pipeline, Integer loggedInUserId) {
		boolean isCRMPipeline = false;
		Integer loggedInCompanyId = userService.getCompanyIdByUserId(loggedInUserId);
		if (loggedInCompanyId != null && pipeline != null) {
			List<String> crmIntegrations = Arrays.asList("HUBSPOT", "SALESFORCE", "MICROSOFT", "PIPEDRIVE",
					"CONNECTWISE", "HALOPSA", "ZOHO");
			if ((pipeline.isSalesforcePipeline() || crmIntegrations.contains(pipeline.getIntegrationType().name()))
					&& loggedInCompanyId.intValue() == pipeline.getCompany().getId().intValue()) {
				isCRMPipeline = true;
			}
		}
		return isCRMPipeline;
	}

	@Override
	public XtremandResponse getPipelinesByIntegrationType(Integer loggedInUserId, Integer companyId, PipelineType type,
			IntegrationType integrationType, Long halopsaTicketTypeId) {
		XtremandResponse response = new XtremandResponse();
		String responseMessage = FAILED;
		Integer responseStatusCode = 400;
		if (loggedInUserId != null && loggedInUserId > 0 && companyId != null && companyId > 0 && type != null) {
			Integer loggedInCompanyId = userService.getCompanyIdByUserId(loggedInUserId);
			Integration integration = integrationDao.getActiveCRMIntegration(companyId);
			if (integration != null && integration.getType() != null) {
				integrationType = integration.getType();
			}
			if (loggedInCompanyId != null) {
				List<Pipeline> pipelines = null;
				if (!loggedInCompanyId.equals(companyId)) {
					Partnership partnership = partnershipDAO.checkPartnership(companyId, loggedInCompanyId);
					if (partnership != null) {

						pipelines = pipelineDAO.getPipelinesByIntegrationType(companyId, type, integrationType, false);

					} else {
						responseMessage = UNAUTHORIZED;
						responseStatusCode = 401;
					}
				} else {
					pipelines = pipelineDAO.getPipelinesByIntegrationType(companyId, type, integrationType, null);
				}
				if (pipelines != null && !pipelines.isEmpty()) {
					response.setData(getPipelineDtoList(pipelines, loggedInUserId, true));
					responseMessage = SUCCESS;
					responseStatusCode = 200;
				} else {
					responseMessage = NOT_FOUND;
					responseStatusCode = 404;
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
	public XtremandResponse getPipelinesByIntegrationType(Integer loggedInUserId, PipelineType type,
			IntegrationType integrationType, Long halopsaTicketTypeId) {
		XtremandResponse response = new XtremandResponse();
		String responseMessage = FAILED;
		Integer responseStatusCode = 400;
		List<Pipeline> pipelines = null;
		if (loggedInUserId != null && loggedInUserId > 0 && type != null) {
			User loggedInUser = userService.loadUser(
					Arrays.asList(new Criteria("userId", OPERATION_NAME.eq, loggedInUserId)),
					new FindLevel[] { FindLevel.COMPANY_PROFILE });
			if (loggedInUser != null && loggedInUser.getCompanyProfile() != null
					&& loggedInUser.getCompanyProfile().getId() != null
					&& loggedInUser.getCompanyProfile().getId() > 0) {
				pipelines = pipelineDAO.getPipelinesByIntegrationType(loggedInUser.getCompanyProfile().getId(), type,
						integrationType, null);
				if (pipelines != null && !pipelines.isEmpty()) {
					response.setData(getPipelineDtoList(pipelines, loggedInUserId, true));
					responseMessage = SUCCESS;
					responseStatusCode = 200;
				} else {
					responseMessage = NOT_FOUND;
					responseStatusCode = 404;
				}
			} else {
				responseMessage = INVALID_INPUT;
				responseStatusCode = 500;
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
	public XtremandResponse getPipelinesForCompanyByIntegrationType(Integer loggedInUserId, Integer companyId,
			PipelineType type, IntegrationType integrationType) {
		XtremandResponse response = new XtremandResponse();
		String responseMessage = FAILED;
		Integer responseStatusCode = 400;
		if (loggedInUserId != null && loggedInUserId > 0 && type != null) {
			User loggedInUser = userService.loadUser(
					Arrays.asList(new Criteria("userId", OPERATION_NAME.eq, loggedInUserId)),
					new FindLevel[] { FindLevel.COMPANY_PROFILE });
			if (loggedInUser != null && loggedInUser.getCompanyProfile() != null
					&& loggedInUser.getCompanyProfile().getId() != null
					&& loggedInUser.getCompanyProfile().getId() > 0) {
				Boolean isPrivate = null;
				if (!loggedInUser.getCompanyProfile().getId().equals(companyId)) {
					isPrivate = false;
				}
				List<Pipeline> pipelines = pipelineDAO.getPipelinesByIntegrationType(companyId, type, integrationType,
						isPrivate);
				if (pipelines != null && !pipelines.isEmpty()) {
					response.setData(getPipelineDtoList(pipelines, loggedInUserId, false));
					responseMessage = SUCCESS;
					responseStatusCode = 200;
				} else {
					responseMessage = NOT_FOUND;
					responseStatusCode = 404;
				}
			} else {
				responseMessage = INVALID_INPUT;
				responseStatusCode = 500;
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
	public XtremandResponse findLeadPipeLines(PipelineRequestDTO pipelineRequestDTO) {
		XtremandResponse response = new XtremandResponse();
		Map<String, Object> map = new HashMap<>();
		String externalPipelineId = null;
		String ticketTypeId = null;
		List<PipelineResponseDTO> pipeLines = new ArrayList<>();
		getActiveCRMAndFindLeadPipeLines(pipelineRequestDTO, externalPipelineId, pipeLines);
		IntegrationDTO integrationDTO = getActiveCrmDetailsToShowPipelineAndStage(pipelineRequestDTO);
		map.put(XamplifyConstants.LIST, pipeLines);
		map.put(XamplifyConstants.TOTAL_RECORDS, pipeLines.size());
		map.put("showCreatedByPipelineAndStage", integrationDTO.isShowCreatedByLeadPipelineAndStage());
		map.put("showCreatedByPipelineAndStageOnTop", integrationDTO.isShowCreatedByLeadPipelineAndStageOnTop());
		map.put("ticketTypeId", ticketTypeId);
		response.setData(map);
		XamplifyUtils.addSuccessStatus(response);
		return response;
	}

	private void getActiveCRMAndFindLeadPipeLines(PipelineRequestDTO pipelineRequestDTO, String externalPipelineId,
			List<PipelineResponseDTO> pipeLines) {
		String activeCRM = pipelineDAO.getActiveCRM(pipelineRequestDTO);
		pipeLines.addAll(pipelineDAO.findLeadPipeLinesByActiveCRM(pipelineRequestDTO, activeCRM, externalPipelineId));
	}

	@Override
	public XtremandResponse findPipelineStages(Integer pipelineId, Integer loggedInUserId) {
		XtremandResponse response = new XtremandResponse();
		Map<String, Object> map = new HashMap<>();
		boolean isPrivateStage = false;
		if (pipelineId != null && pipelineId > 0 && loggedInUserId != null && loggedInUserId > 0) {
			Pipeline pipeline = genericDAO.get(Pipeline.class, pipelineId);
			Integer loggedInCompanyId = userDao.getCompanyIdByUserId(loggedInUserId);
			if (pipeline != null && !pipeline.getCompany().getId().equals(loggedInCompanyId)) {
				isPrivateStage = true;
			}
		}
		List<PipelineStageResponseDTO> pipelineStages = pipelineDAO.findPipelineStagesByPipelineId(pipelineId,
				isPrivateStage);
		map.put(XamplifyConstants.LIST, pipelineStages);
		map.put(XamplifyConstants.TOTAL_RECORDS, pipelineStages.size());
		response.setData(map);
		XamplifyUtils.addSuccessStatus(response);
		return response;
	}

	private IntegrationDTO getActiveCrmDetailsToShowPipelineAndStage(PipelineRequestDTO pipelineRequestDTO) {
		IntegrationDTO integrationDTO = new IntegrationDTO();
		Integer vendorCompanyId = pipelineRequestDTO.getVendorCompanyId();
		setCreatedForActiveCRMProperty(integrationDTO, vendorCompanyId);
		Integer partnerCompanyId = userDao.getCompanyIdByUserId(pipelineRequestDTO.getLoggedInUserId());
		if (vendorCompanyId.equals(partnerCompanyId)) {
			setCreatedByActiveCRMPropertyByPartnerIntegrationType(integrationDTO, partnerCompanyId);
			integrationDTO.setCreatedByActiveCRM(true);
			integrationDTO.setShowCreatedByLeadPipelineAndStageOnTop(true);
			boolean isCreatedByActiveCRM = integrationDTO.isCreatedByActiveCRM();
			boolean isCreatedForActiveCRM = integrationDTO.isCreatedForActiveCRM();
			integrationDTO.setShowCreatedByLeadPipelineAndStage(isCreatedByActiveCRM && isCreatedForActiveCRM);
		}
		return integrationDTO;
	}

	private void setCreatedByActiveCRMPropertyByPartnerIntegrationType(IntegrationDTO integrationDTO,
			Integer partnerCompanyId) {
		String partnerIntegrationType = integrationDao.getActiveIntegrationTypeByCompanyId(partnerCompanyId);
		if (partnerIntegrationType != null) {
			integrationDTO.setCreatedByActiveCRMType(IntegrationType.valueOf(partnerIntegrationType.toUpperCase()));
		} else {
			integrationDTO.setCreatedByActiveCRMType(IntegrationType.XAMPLIFY);
		}
	}

	private void setCreatedForActiveCRMProperty(IntegrationDTO integrationDTO, Integer vendorCompanyId) {
		if (XamplifyUtils.isValidInteger(vendorCompanyId)) {
				integrationDTO.setCreatedForActiveCRMType(IntegrationType.XAMPLIFY);
			   integrationDTO.setCreatedForActiveCRM(true);
		}
	}

	@Override
	public XtremandResponse findLeadPipelinesForPartner(PipelineRequestDTO pipelineRequestDTO) {
		XtremandResponse response = new XtremandResponse();
		Map<String, Object> map = new HashMap<>();
		String externalPipelineId = null;
		List<PipelineResponseDTO> pipeLines = new ArrayList<>();
		Integer partnerCompanyId = userDao.getCompanyIdByUserId(pipelineRequestDTO.getLoggedInUserId());
		if (partnerCompanyId.equals(pipelineRequestDTO.getVendorCompanyId())) {
			pipelineRequestDTO.setVendorCompanyId(partnerCompanyId);
			getActiveCRMAndFindLeadPipeLines(pipelineRequestDTO, externalPipelineId, pipeLines);
			map.put(XamplifyConstants.LIST, pipeLines);
			map.put(XamplifyConstants.TOTAL_RECORDS, pipeLines.size());
		}
		response.setData(map);
		XamplifyUtils.addSuccessStatus(response);
		return response;
	}

	@Override
	public XtremandResponse findLeadAndDealPipeLinesToCreateCampaign(Integer loggedInUserId) {
		XtremandResponse response = new XtremandResponse();
		response.setMessage(FAILED);
		response.setStatusCode(400);
		if (XamplifyUtils.isValidInteger(loggedInUserId)) {
			Integer companyId = userDao.getCompanyIdByUserId(loggedInUserId);
			findLeadAndDealPipelinesByCompanyId(response, companyId);
		} else {
			response.setMessage(INVALID_INPUT);
			response.setStatusCode(500);
		}
		return response;
	}

	private void findLeadAndDealPipelinesByCompanyId(XtremandResponse response, Integer companyId) {
		if (XamplifyUtils.isValidInteger(companyId)) {
			Map<String, Object> responseData = new HashMap<>();
			String activeCRMIntegration = integrationDao.getActiveIntegrationTypeByCompanyId(companyId);
			boolean isValidActiveCRMIntegration = org.springframework.util.StringUtils.hasText(activeCRMIntegration);
			List<PipelineResponseDTO> leadPipelines = new ArrayList<>();
			List<PipelineResponseDTO> dealPipeLines = new ArrayList<>();
			if (isValidActiveCRMIntegration) {
				leadPipelines.addAll(pipelineDAO.findPipelinesByCompanyIdAndPipeLineTypeAndIntegrationType(companyId,
						PipelineType.LEAD, activeCRMIntegration));
				dealPipeLines.addAll(pipelineDAO.findPipelinesByCompanyIdAndPipeLineTypeAndIntegrationType(companyId,
						PipelineType.DEAL, activeCRMIntegration));
			} else {
				leadPipelines.addAll(
						pipelineDAO.findPipeLinesByCompanyIdAndPipeLineType(companyId, PipelineType.LEAD, false));
				dealPipeLines.addAll(
						pipelineDAO.findPipeLinesByCompanyIdAndPipeLineType(companyId, PipelineType.DEAL, false));
			}
			responseData.put("leadPipelines", leadPipelines);
			responseData.put("dealPipelines", dealPipeLines);

			response.setData(responseData);
			response.setMessage(SUCCESS);
			response.setStatusCode(200);
		} else {
			response.setMessage(UNAUTHORIZED);
			response.setStatusCode(401);
		}
	}

	@Override
	public XtremandResponse findPipelinesForCRMSettings(PipelineRequestDTO pipelineRequestDTO) {
		XtremandResponse response = new XtremandResponse();
		response.setMessage(FAILED);
		response.setStatusCode(400);
		List<PipelineResponseDTO> pipelines = new ArrayList<>();
		if (XamplifyUtils.isValidInteger(pipelineRequestDTO.getLoggedInUserId())) {
			Integer companyId = userDao.getCompanyIdByUserId(pipelineRequestDTO.getLoggedInUserId());
			if (XamplifyUtils.isValidInteger(companyId)) {
				pipelines = pipelineDAO.findPipelineForCRMSettings(pipelineRequestDTO, companyId);
				response.setData(pipelines);
				response.setMessage(SUCCESS);
				response.setStatusCode(200);
			} else {
				response.setMessage(UNAUTHORIZED);
				response.setStatusCode(401);
			}
		} else {
			response.setMessage(INVALID_INPUT);
			response.setStatusCode(500);
		}
		return response;
	}

}
