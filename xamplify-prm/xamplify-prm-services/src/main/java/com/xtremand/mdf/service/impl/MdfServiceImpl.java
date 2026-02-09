package com.xtremand.mdf.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.xtremand.aws.AWSInputDTO;
import com.xtremand.aws.AmazonWebService;
import com.xtremand.common.bom.CompanyProfile;
import com.xtremand.common.bom.Criteria;
import com.xtremand.common.bom.FindLevel;
import com.xtremand.common.bom.Pagination;
import com.xtremand.form.bom.Form;
import com.xtremand.form.bom.FormTypeEnum;
import com.xtremand.form.dao.FormDao;
import com.xtremand.form.dto.FormDTO;
import com.xtremand.form.service.FormService;
import com.xtremand.form.submit.dao.FormSubmitDao;
import com.xtremand.form.submit.dto.FormSubmitDTO;
import com.xtremand.form.submit.service.FormSubmitService;
import com.xtremand.formbeans.ErrorResponse;
import com.xtremand.formbeans.UserDTO;
import com.xtremand.formbeans.XtremandResponse;
import com.xtremand.mail.service.AsyncComponent;
import com.xtremand.mdf.bom.MdfAmountType;
import com.xtremand.mdf.bom.MdfDetails;
import com.xtremand.mdf.bom.MdfDetailsHistory;
import com.xtremand.mdf.bom.MdfRequest;
import com.xtremand.mdf.bom.MdfRequestComment;
import com.xtremand.mdf.bom.MdfRequestDocument;
import com.xtremand.mdf.bom.MdfRequestHistory;
import com.xtremand.mdf.bom.MdfWorkFlowStepType;
import com.xtremand.mdf.dao.MdfDao;
import com.xtremand.mdf.dto.MdfAmountTilesDTO;
import com.xtremand.mdf.dto.MdfDetailsDTO;
import com.xtremand.mdf.dto.MdfDetailsTimeLineDTO;
import com.xtremand.mdf.dto.MdfParnterDTO;
import com.xtremand.mdf.dto.MdfRequestCommentDTO;
import com.xtremand.mdf.dto.MdfRequestPostDTO;
import com.xtremand.mdf.dto.MdfRequestTimeLineDTO;
import com.xtremand.mdf.dto.MdfRequestUploadDTO;
import com.xtremand.mdf.dto.MdfRequestViewDTO;
import com.xtremand.mdf.dto.MdfUserDTO;
import com.xtremand.mdf.dto.VendorMdfAmountTilesDTO;
import com.xtremand.mdf.exception.DuplicateRequestTitleException;
import com.xtremand.mdf.exception.MdfDataAccessException;
import com.xtremand.mdf.service.MdfService;
import com.xtremand.partnership.bom.Partnership;
import com.xtremand.partnership.dao.PartnershipDAO;
import com.xtremand.user.dao.UserDAO;
import com.xtremand.util.DateUtils;
import com.xtremand.util.GenerateRandomPassword;
import com.xtremand.util.XamplifyUtil;
import com.xtremand.util.XamplifyUtils;
import com.xtremand.util.dto.DateDTO;
import com.xtremand.util.service.UtilService;
import com.xtremand.vanity.url.dto.VanityUrlDetailsDTO;

@Service
@Transactional
public class MdfServiceImpl implements MdfService {

	private static final String MDF_AMOUNT = "mdfAmount";

	private static final Logger logger = LoggerFactory.getLogger(MdfServiceImpl.class);

	private static final String ALLOCATION_DATE_IN_STRING = "allocationDateInString";

	private static final String INVALID_DATE_FORMAT = "Invalid Date Format";

	@Autowired
	private MdfDao mdfDao;

	@Autowired
	private PartnershipDAO partnershipDAO;

	@Autowired
	private UtilService utilService;

	@Autowired
	private UserDAO userDao;

	@Autowired
	private FormService formService;

	@Autowired
	private FormSubmitService formSubmitService;

	@Autowired
	private FormSubmitDao formSubmitDao;

	@Autowired
	private FormDao formDao;

	@Autowired
	private XamplifyUtil xamplifyUtil;

	@Value("${amazon.mdf.requests.folder}")
	private String mdfRequestFolderName;

	@Value("${mdf.request.file.max.size}")
	private String maxFileSizeInString;

	@Value("${web_url}")
	private String webUrl;

	@Autowired
	private AmazonWebService amazonWebService;

	@Autowired
	private AsyncComponent asyncComponent;

	@Override
	public void save(MdfDetails marketDevelopementFundsCredit) {
		mdfDao.save(marketDevelopementFundsCredit);

	}

	@Override
	public XtremandResponse getVendorMdfAmountTilesInfo(Integer vendorCompanyId, Integer loggedInUserId,
			boolean applyFilter) {
		try {
			XtremandResponse response = new XtremandResponse();
			VendorMdfAmountTilesDTO vendorMdfAmountTilesDTO = mdfDao.getVendorMdfAmountTilesInfo(vendorCompanyId,
					loggedInUserId, applyFilter);
			if (vendorMdfAmountTilesDTO != null) {
				response.setData(vendorMdfAmountTilesDTO);
			} else {
				response.setData(VendorMdfAmountTilesDTO.setDefaultData());
			}
			response.setStatusCode(200);
			response.setAccess(true);
			return response;
		} catch (MdfDataAccessException mex) {
			throw new MdfDataAccessException(mex);
		} catch (Exception e) {
			throw new MdfDataAccessException(e);
		}
	}

	@Override
	public XtremandResponse listPartners(Pagination pagination) {
		try {
			XtremandResponse response = new XtremandResponse();
			response.setAccess(true);
			response.setStatusCode(200);
			response.setData(mdfDao.listPartners(pagination));
			return response;
		} catch (MdfDataAccessException mex) {
			throw new MdfDataAccessException(mex);
		} catch (Exception e) {
			throw new MdfDataAccessException(e);
		}
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public XtremandResponse updateMdfAmount(MdfDetailsDTO mdfDetailsDTO) {
		try {
			XtremandResponse response = new XtremandResponse();
			List<ErrorResponse> errorResponses = new ArrayList<>();
			MdfDetails mdfDetails = new MdfDetails();
			Integer partnershipId = mdfDetailsDTO.getPartnershipId();
			String mdfAmountTypeInString = mdfDetailsDTO.getMdfAmountTypeInString();
			boolean fundAdded = MdfAmountType.FUND_ADDED.name().equals(mdfAmountTypeInString);
			boolean fundRemoved = MdfAmountType.FUND_REMOVED.name().equals(mdfAmountTypeInString);
			if (fundAdded) {
				mdfDetails.setMdfAmountType(MdfAmountType.FUND_ADDED);
			} else if (fundRemoved) {
				mdfDetails.setMdfAmountType(MdfAmountType.FUND_REMOVED);
			}

			Partnership partnership = partnershipDAO.getPartnershipById(partnershipId);
			validateMdfAmount(mdfDetailsDTO, errorResponses, fundRemoved, partnership);

			DateDTO allocatinDateDTO = DateUtils
					.convertStringToDateMDYFormat(mdfDetailsDTO.getAllocationDateInString());
			Date allocationDate = allocatinDateDTO.getDate();
			DateDTO expirationDateDTO = DateUtils
					.convertStringToDateMDYFormat(mdfDetailsDTO.getExpirationDateInString());
			Date expirationDate = expirationDateDTO.getDate();

			validateAllocationAmountDateFormat(errorResponses, allocatinDateDTO);

			validateExpirationDateFormat(errorResponses, expirationDateDTO);

			if (fundAdded && allocatinDateDTO.isValidDate() && expirationDateDTO.isValidDate() && allocationDate != null
					&& expirationDate != null && expirationDate.before(allocationDate)) {
				ErrorResponse dateErrorResponse = new ErrorResponse("expirationDateInString",
						"Expiration date should be after Allocation Date");
				errorResponses.add(dateErrorResponse);
			}

			if (errorResponses.isEmpty()) {
				if ("DEACTIVATED".equalsIgnoreCase(String.valueOf(partnership.getStatus()))) {
					response.setStatusCode(401);
					response.setMessage("Partnership is deactivated");
				} else {
					saveMdfDetailsAndHistoryData(mdfDetailsDTO, response, mdfDetails, fundAdded, allocationDate,
							expirationDate);
				}
			} else {
				response.setStatusCode(400);
			}
			response.setErrorResponses(errorResponses);
			return response;
		} catch (MdfDataAccessException mex) {
			throw new MdfDataAccessException(mex);
		} catch (Exception e) {
			throw new MdfDataAccessException(e);
		}
	}

	private void validateExpirationDateFormat(List<ErrorResponse> errorResponses, DateDTO expirationDateDTO) {
		if (!expirationDateDTO.isValidDate()) {
			ErrorResponse expirationDateError = new ErrorResponse("expirationDateInString", INVALID_DATE_FORMAT);
			errorResponses.add(expirationDateError);
		}
	}

	private void validateAllocationAmountDateFormat(List<ErrorResponse> errorResponses, DateDTO allocatinDateDTO) {
		if (!allocatinDateDTO.isValidDate()) {
			ErrorResponse allocationDateError = new ErrorResponse(ALLOCATION_DATE_IN_STRING, INVALID_DATE_FORMAT);
			errorResponses.add(allocationDateError);
		}
	}

	private void saveMdfDetailsAndHistoryData(MdfDetailsDTO mdfDetailsDTO, XtremandResponse response,
			MdfDetails mdfDetails, boolean fundAdded, Date allocationDate, Date expirationDate) {
		response.setStatusCode(200);
		BeanUtils.copyProperties(mdfDetailsDTO, mdfDetails);
		mdfDetails.setCreatedTime(new Date());
		if (fundAdded) {
			mdfDetails.setAllocationDate(allocationDate);
			mdfDetails.setExpirationDate(expirationDate);
		}
		Partnership partnership = new Partnership();
		Integer partnershipId = mdfDetailsDTO.getPartnershipId();
		partnership.setId(partnershipId);
		mdfDetails.setPartnership(partnership);

		MdfDetails mdfDetailsByPartnershipId = mdfDao.getMdfDetailsByPartnershipId(partnershipId);
		Integer companyId = userDao.getCompanyIdByUserId(mdfDetailsDTO.getCreatedBy());
		CompanyProfile vendorCompany = new CompanyProfile();
		vendorCompany.setId(companyId);
		mdfDetails.setCompanyProfile(vendorCompany);
		if (mdfDetailsByPartnershipId != null) {
			mdfDetailsByPartnershipId.setMdfAmount(mdfDetails.getMdfAmount());
			mdfDetailsByPartnershipId.setAllocationDate(mdfDetails.getAllocationDate());
			mdfDetailsByPartnershipId.setExpirationDate(mdfDetailsByPartnershipId.getExpirationDate());
			mdfDetailsByPartnershipId.setMdfAmountType(mdfDetails.getMdfAmountType());
			mdfDetailsByPartnershipId.setDescription(mdfDetails.getDescription());
			mdfDetailsByPartnershipId.setUpdatedBy(mdfDetails.getCreatedBy());
			mdfDetailsByPartnershipId.setUpdatedTime(new Date());
			saveMdfDetailsHistory(mdfDetailsByPartnershipId, partnership, true);
		} else {
			mdfDao.save(mdfDetails);
			saveMdfDetailsHistory(mdfDetails, partnership, false);
		}
		asyncComponent.sendMdfAmountNotification(mdfDetails);
	}

	private void saveMdfDetailsHistory(MdfDetails mdfDetails, Partnership partnership, boolean isUpdate) {
		MdfDetailsHistory mdfDetailsHistory = new MdfDetailsHistory();
		mdfDetailsHistory.setMarketDevelopementFundsDetails(mdfDetails);
		mdfDetailsHistory.setPartnership(partnership);
		mdfDetailsHistory.setAllocationDate(mdfDetails.getAllocationDate());
		mdfDetailsHistory.setExpirationDate(mdfDetails.getExpirationDate());
		mdfDetailsHistory.setMdfAmount(mdfDetails.getMdfAmount());
		mdfDetailsHistory.setMdfAmountType(mdfDetails.getMdfAmountType());
		mdfDetailsHistory.setDescription(mdfDetails.getDescription());
		mdfDetailsHistory.setCreatedBy(mdfDetails.getCreatedBy());
		mdfDetailsHistory.setCreatedTime(mdfDetails.getCreatedTime());
		mdfDetailsHistory.setCompanyProfile(mdfDetails.getCompanyProfile());
		if (isUpdate) {
			mdfDetailsHistory.setUpdatedBy(mdfDetails.getUpdatedBy());
			mdfDetailsHistory.setUpdatedTime(mdfDetails.getUpdatedTime());
		}
		mdfDao.save(mdfDetailsHistory);
	}

	private void validateMdfAmount(MdfDetailsDTO mdfDetailsDTO, List<ErrorResponse> errorResponses,
			boolean fundRemoved, Partnership partnership) {
		Double mdfAmount = mdfDetailsDTO.getMdfAmount();
		if (mdfAmount == null || mdfAmount.equals(0.00)) {
			ErrorResponse mdfAmountError = new ErrorResponse(MDF_AMOUNT, "Please Enter Amount");
			errorResponses.add(mdfAmountError);
		} else {
			if (fundRemoved) {
				Integer partnershipId = mdfDetailsDTO.getPartnershipId();
				Double amountFromUI = mdfDetailsDTO.getMdfAmount();
				MdfAmountTilesDTO mdfAmountTilesDTO = mdfDao.getPartnerMdfAmountTilesInfo(
						partnership.getVendorCompany().getId(), partnership.getPartnerCompany().getId());
				Double availableBalance = mdfAmountTilesDTO.getAvailableBalance();
				Double calculatedAvailableBalance = availableBalance - amountFromUI;
				Double sumOfAllocationAmount = mdfDao.getSumOfAllocationAmountByPartnershipId(partnershipId);
				if (calculatedAvailableBalance < 0) {
					ErrorResponse mdfAmountError = new ErrorResponse(MDF_AMOUNT, "Invalid Amount");
					errorResponses.add(mdfAmountError);
				} else if (calculatedAvailableBalance < sumOfAllocationAmount) {
					Double removableAmount = availableBalance - sumOfAllocationAmount;
					if (removableAmount > 0) {
						ErrorResponse mdfAmountError = new ErrorResponse(MDF_AMOUNT, "Maximum  $ " + removableAmount
								+ " can be removed.Because $ " + sumOfAllocationAmount + " already allocated");
						errorResponses.add(mdfAmountError);
					} else {
						ErrorResponse mdfAmountError = new ErrorResponse(MDF_AMOUNT,
								"$ " + sumOfAllocationAmount + " is already allocated.So fund cannot be removed");
						errorResponses.add(mdfAmountError);
					}
				}

			}

		}
	}

	@Override
	public XtremandResponse getMdfRequestsPartnerTiles(VanityUrlDetailsDTO vanityUrlDetailsDto) {
		try {
			XtremandResponse response = new XtremandResponse();
			response.setStatusCode(200);
			utilService.isVanityUrlFilterApplicable(vanityUrlDetailsDto);
			if (vanityUrlDetailsDto.isPartnerLoggedInThroughVanityUrl()) {
				Integer partnerCompanyId = vanityUrlDetailsDto.getLoggedInUserCompanyId();
				Integer vendorCompanyId = vanityUrlDetailsDto.getVendorCompanyId();
				response.setData(mdfDao.getMdfRequestsPartnerTilesForVanityLogin(partnerCompanyId, vendorCompanyId));
			} else {
				Integer loginAsUserId = vanityUrlDetailsDto.getLoginAsUserId();
				boolean loginAsPartner = XamplifyUtils.isLoginAsPartner(loginAsUserId);
				Integer partnerCompanyId = userDao.getCompanyIdByUserId(vanityUrlDetailsDto.getUserId());
				if (loginAsPartner) {
					Integer vendorCompanyId = userDao.getCompanyIdByUserId(loginAsUserId);
					response.setData(
							mdfDao.getMdfRequestsPartnerTilesForVanityLogin(partnerCompanyId, vendorCompanyId));
				} else {
					response.setData(mdfDao.getMdfRequestsPartnerTilesForXamplifyLogin(partnerCompanyId));
				}
			}
			return response;
		} catch (MdfDataAccessException mex) {
			throw new MdfDataAccessException(mex);
		} catch (Exception e) {
			throw new MdfDataAccessException(e);
		}
	}

	@Override
	public XtremandResponse getPartnerMdfAmountTilesInfo(Integer vendorCompanyId, Integer partnerCompanyId) {
		try {
			XtremandResponse response = new XtremandResponse();
			CompanyProfile companyProfile = new CompanyProfile();
			companyProfile.setId(partnerCompanyId);
			List<Integer> vendorCompanyIds = partnershipDAO.getVendorCompanyIdsByPartnerCompany(companyProfile);
			if (vendorCompanyIds.indexOf(vendorCompanyId) > -1) {
				response.setStatusCode(200);
				response.setData(mdfDao.getPartnerMdfAmountTilesInfo(vendorCompanyId, partnerCompanyId));
			} else {
				response.setStatusCode(404);
			}
			return response;
		} catch (MdfDataAccessException mex) {
			throw new MdfDataAccessException(mex);
		} catch (Exception e) {
			throw new MdfDataAccessException(e);
		}
	}

	@Override
	public XtremandResponse listVendorsAndRequestsCountByPartnerCompanyId(Pagination pagination) {
		try {
			XtremandResponse response = new XtremandResponse();
			response.setStatusCode(200);
			response.setData(mdfDao.listVendorsAndRequestsCountByPartnerCompanyId(pagination));
			return response;
		} catch (MdfDataAccessException mex) {
			throw new MdfDataAccessException(mex);
		} catch (Exception e) {
			throw new MdfDataAccessException(e);
		}
	}

	@Override
	public XtremandResponse getMdfRequestForm(Integer vendorCompanyId, boolean createRequest) {
		try {
			return formService.getMdfFormByCompanyId(vendorCompanyId, createRequest);
		} catch (MdfDataAccessException mex) {
			throw new MdfDataAccessException(mex);
		} catch (Exception e) {
			throw new MdfDataAccessException(e);
		}

	}

	@Override
	public XtremandResponse createMdfForm(FormDTO formDto) {
		try {
			Integer userId = userDao.getUserIdByEmail(formDto.getUserName());
			String companyProfileName = userDao.getCompanyProfileNameByUserId(userId);
			formDto.setName(companyProfileName + "-mdf-request-form");
			formDto.setFormType(FormTypeEnum.MDF_REQUEST_FORM);
			formDto.setCreatedBy(userId);
			formDto.setBackgroundColor("#ffffff");
			formDto.setLabelColor("#000");
			formDto.setButtonValueColor("#000");
			formDto.setTitleColor("#000");
			formDto.setBorderColor("#ddd");
			formDto.setBorderColor("#F1F3FA");
			formDto.setDescriptionColor("#000");
			formDto.setCreatingMdfForm(true);
			return formService.save(formDto, null);
		} catch (MdfDataAccessException mex) {
			throw new MdfDataAccessException(mex);
		} catch (Exception e) {
			throw new MdfDataAccessException(e);
		}

	}

	@Override
	public XtremandResponse updateMdfForm(FormDTO formDto) {
		try {
			return formService.update(formDto, null);
		} catch (MdfDataAccessException mex) {
			throw new MdfDataAccessException(mex);
		} catch (Exception e) {
			throw new MdfDataAccessException(e);
		}

	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public XtremandResponse saveMdfRequest(MdfRequestPostDTO mdfRequestPostDTO) {
		try {
			XtremandResponse response = new XtremandResponse();
			response.setStatusCode(200);
			Partnership partnership = new Partnership();
			Integer vendorCompanyId = mdfRequestPostDTO.getVendorCompanyId();
			Integer partnerCompanyId = mdfRequestPostDTO.getPartnerCompanyId();
			Integer partnershipId = partnershipDAO.getPartnershipIdByVendorCompanyIdAndPartnerCompanyId(vendorCompanyId,
					partnerCompanyId);
			partnership.setId(partnershipId);

			MdfRequest mdfRequest = saveMdfRequest(mdfRequestPostDTO, partnership);
			saveMdfRequestHistory(partnership, mdfRequest);

			FormSubmitDTO formSubmitDTO = mdfRequestPostDTO.getFormSubmitDto();
			formSubmitDTO.setFormType(FormTypeEnum.MDF_REQUEST_FORM);
			mdfRequest.setVendorCompanyId(vendorCompanyId);
			mdfRequest.setPartnerCompanyId(partnerCompanyId);
			saveFormData(mdfRequest, formSubmitDTO);
			return response;
		} catch (DuplicateRequestTitleException e) {
			throw new DuplicateRequestTitleException(e.getMessage());
		} catch (MdfDataAccessException mex) {
			throw new MdfDataAccessException(mex);
		} catch (Exception e) {
			throw new MdfDataAccessException(e);
		}

	}

	private void saveFormData(MdfRequest mdfRequest, FormSubmitDTO formSubmitDTO) {
		try {
			formSubmitService.save(formSubmitDTO, mdfRequest);
		} catch (DuplicateRequestTitleException e) {
			throw new DuplicateRequestTitleException(e.getMessage());
		}
	}

	private void saveMdfRequestHistory(Partnership partnership, MdfRequest mdfRequest) {
		MdfRequestHistory mdfRequestHistory = new MdfRequestHistory();
		mdfRequestHistory.setMdfRequest(mdfRequest);
		mdfRequestHistory.setPartnership(partnership);
		mdfRequestHistory.setCreatedBy(mdfRequest.getCreatedBy());
		mdfRequestHistory.setCreatedTime(mdfRequest.getCreatedTime());
		mdfRequestHistory.setMdfWorkFlowStepType(mdfRequest.getMdfWorkFlowStepType());
		mdfDao.save(mdfRequestHistory);
	}

	private MdfRequest saveMdfRequest(MdfRequestPostDTO mdfRequestPostDTO, Partnership partnership) {
		MdfRequest mdfRequest = new MdfRequest();
		mdfRequest.setCreatedBy(mdfRequestPostDTO.getUserId());
		mdfRequest.setCreatedTime(new Date());
		mdfRequest.setMdfWorkFlowStepType(MdfWorkFlowStepType.NEW_REQUEST);
		mdfRequest.setPartnership(partnership);
		mdfDao.save(mdfRequest);
		return mdfRequest;
	}

	@Override
	public XtremandResponse listMdfFormDetails(Pagination pagination) {
		try {
			XtremandResponse response = new XtremandResponse();
			response.setStatusCode(200);
			List<Criteria> criterias = new ArrayList<>();
			Integer vendorCompanyId = pagination.getVendorCompanyId();
			Form form = formDao.getMDFFormByCompanyId(vendorCompanyId);
			if (form != null) {
				pagination.setCompanyId(form.getId());
				pagination.setMdfForm(true);
				pagination.setFormName(form.getFormName());
				Map<String, Object> formsMap = formSubmitDao.findMdfFormSubmittedDetails(criterias,
						new FindLevel[] { FindLevel.SHALLOW }, pagination);
				response.setData(formsMap);
			} else {
				response.setStatusCode(404);
				response.setMessage("Default Mdf Form Not Found");
			}
			return response;
		} catch (MdfDataAccessException mex) {
			throw new MdfDataAccessException(mex);
		} catch (Exception e) {
			throw new MdfDataAccessException(e);
		}

	}

	@Override
	public XtremandResponse getMdfRequestTilesInfoForVendors(Integer vendorCompanyId, Integer loggedInUserId,
			boolean teamMemberFilter) {
		try {
			XtremandResponse response = new XtremandResponse();
			response.setStatusCode(200);
			response.setData(
					mdfDao.getMdfRequestTilesInfoForVendors(vendorCompanyId, loggedInUserId, teamMemberFilter));
			return response;
		} catch (MdfDataAccessException mex) {
			throw new MdfDataAccessException(mex);
		} catch (Exception e) {
			throw new MdfDataAccessException(e);
		}
	}

	@Override
	public XtremandResponse getRequestDetailsById(Integer id, Integer loggedInUserCompanyId) {
		try {
			XtremandResponse response = new XtremandResponse();
			boolean validRequestId = mdfDao.validateRequestId(id, loggedInUserCompanyId);
			if (validRequestId) {
				response.setStatusCode(200);
				HashMap<String, Object> map = new HashMap<>();
				MdfRequest request = setMdfRequestDetails(id, map);
				setPartnerMdfBalances(id, map, request);
				setVendorContact(loggedInUserCompanyId, map);
				setMdfRequestOwnerAndPartnerManager(loggedInUserCompanyId, map, request);
				response.setMap(map);
			} else {
				response.setStatusCode(404);
				response.setMessage("Invalid Request");
			}
			return response;
		} catch (MdfDataAccessException mex) {
			throw new MdfDataAccessException(mex);
		} catch (Exception e) {
			throw new MdfDataAccessException(e);
		}
	}

	private void setMdfRequestOwnerAndPartnerManager(Integer loggedInUserCompanyId, HashMap<String, Object> map,
			MdfRequest request) {
		List<Integer> userIds = new ArrayList<>();
		userIds.add(request.getCreatedBy());
		Integer partnerId = request.getPartnership().getRepresentingPartner().getUserId();
		userIds.add(partnerId);
		List<Object[]> getMdfOwnerNameAndContactCompany = mdfDao.getMdfOwnerNameAndContactCompany(loggedInUserCompanyId,
				userIds);
		String fullName = "";
		String contactCompany = "";
		Integer userId = 0;
		if (!getMdfOwnerNameAndContactCompany.isEmpty()) {
			userId = (Integer) getMdfOwnerNameAndContactCompany.get(0)[0];
			fullName = (String) getMdfOwnerNameAndContactCompany.get(0)[1];
			contactCompany = (String) getMdfOwnerNameAndContactCompany.get(0)[2];
		}
		setMdfRequestOwner(map, request, fullName, contactCompany, userId);
		setPartnerManager(map, request, fullName, contactCompany, userId);
	}

	private void setMdfRequestOwner(HashMap<String, Object> map, MdfRequest request, String fullName,
			String contactCompany, Integer userId) {
		MdfUserDTO mdfUserDto = mdfDao.getMdfRequestOwnerDetails(request.getCreatedBy());
		mdfUserDto.setCompanyName(contactCompany);
		if (userId.equals(request.getCreatedBy())) {
			mdfUserDto.setFullName(fullName);
		}
		mdfUserDto.setCompanyName(contactCompany);
		mdfUserDto.setProfilePicturePath(xamplifyUtil.getCompleteImagePath(mdfUserDto.getProfilePicturePath()));
		map.put("mdfRequestOwner", mdfUserDto);
	}

	private void setPartnerManager(HashMap<String, Object> map, MdfRequest request, String fullName,
			String contactCompany, Integer userId) {
		MdfUserDTO partnerManager = mdfDao
				.getPartnerManagerDetails(request.getPartnership().getPartnerCompany().getId());
		partnerManager.setCompanyName(contactCompany);
		if (partnerManager.getUserId().equals(userId)) {
			partnerManager.setFullName(fullName);
		}
		partnerManager.setProfilePicturePath(xamplifyUtil.getCompleteImagePath(partnerManager.getProfilePicturePath()));
		partnerManager.setPartnerStatus(String.valueOf(request.getPartnership().getStatus()));
		map.put("partnerManager", partnerManager);
	}

	private void setPartnerMdfBalances(Integer id, HashMap<String, Object> map, MdfRequest request) {
		Partnership partnerShip = request.getPartnership();
		if (partnerShip != null && partnerShip.getVendorCompany() != null && partnerShip.getPartnerCompany() != null) {
			Integer vendorCompanyId = partnerShip.getVendorCompany().getId();
			Integer partnerCompanyId = partnerShip.getPartnerCompany().getId();
			map.put("partnerMdfBalances", mdfDao.getPartnerMdfAmountTilesInfo(vendorCompanyId, partnerCompanyId));
		} else {
			map.put("partnerMdfBalances", MdfAmountTilesDTO.setDefaultData());
			logger.error("Error In Getting Partner MDF Balances By Request Id:- {} & Partnership:- {}", id,
					partnerShip);
		}
	}

	private MdfRequest setMdfRequestDetails(Integer id, HashMap<String, Object> map) {
		MdfRequest request = mdfDao.getMdfRequestById(id);
		MdfRequestViewDTO mdfRequestViewDTO = new MdfRequestViewDTO();
		BeanUtils.copyProperties(request, mdfRequestViewDTO);
		mdfRequestViewDTO.setRequestCreatedDateInString(DateUtils.getMdfCreatedDateInString(request.getCreatedTime()));
		mdfRequestViewDTO.setAllocationDateInString(DateUtils.convertToOnlyDate(request.getAllocationDate()));
		mdfRequestViewDTO.setAllocationExpirationDateInString(
				DateUtils.convertToOnlyDate(request.getAllocationExpirationDate()));
		mdfRequestViewDTO.setSumOfAllocationAmount(mdfDao.getSumOfAllocationAmountByRequestId(id));
		mdfRequestViewDTO.setSumOfReimbursementAmount(mdfDao.getSumOfReimbursementAmountByRequestId(id));
		mdfRequestViewDTO.setPartnershipId(request.getPartnership().getId());
		mdfRequestViewDTO.setAllocationAmount(Double.valueOf(0));
		xamplifyUtil.getMdfRequestStatusInStringAndNumber(request.getMdfWorkFlowStepType().name(), mdfRequestViewDTO);
		List<String> formValues = mdfDao.listTitleAndEventDateAndRequestAmountByRequestId(id);
		if (!formValues.isEmpty()) {
			mdfRequestViewDTO.setTitle(formValues.get(0));
			DateDTO dateDTO = DateUtils.convertStringToDateMDYFormat(formValues.get(1));
			Date eventDate = dateDTO.getDate();
			String eventDateInString = DateUtils.getMdfCreatedDateInString(eventDate);
			mdfRequestViewDTO.setEventDateInString(eventDateInString);
			mdfRequestViewDTO.setRequestAmount(XamplifyUtils.convertStringToDouble(formValues.get(2)));
		}
		map.put("requestDetails", mdfRequestViewDTO);
		return request;
	}

	private void setVendorContact(Integer loggedInUserCompanyId, HashMap<String, Object> map) {
		MdfUserDTO vendorContact = mdfDao.getMdfVendorDetails(loggedInUserCompanyId);
		String completeImagePath = xamplifyUtil.getCompleteImagePath(vendorContact.getProfilePicturePath());
		vendorContact.setProfilePicturePath(completeImagePath);
		map.put("vendorContact", vendorContact);
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public XtremandResponse updateMdfRequest(MdfRequestViewDTO mdfRequestViewDTO) {
		try {
			XtremandResponse response = new XtremandResponse();
			List<ErrorResponse> errorResponses = new ArrayList<>();
			Integer statusInInteger = mdfRequestViewDTO.getStatusInInteger();
			Integer requestId = mdfRequestViewDTO.getId();
			List<Object[]> vendorCompanyIdPartnerCompanyId = mdfDao
					.getVendorCompanyIdAndPartnerCompanyIdByRequestId(requestId);
			Integer vendorCompanyId = 0;
			Integer partnerCompanyId = 0;
			if (vendorCompanyIdPartnerCompanyId != null && !vendorCompanyIdPartnerCompanyId.isEmpty()) {
				vendorCompanyId = (Integer) vendorCompanyIdPartnerCompanyId.get(0)[0];
				partnerCompanyId = (Integer) vendorCompanyIdPartnerCompanyId.get(0)[1];
			}
			validateRequest(mdfRequestViewDTO, errorResponses, statusInInteger, vendorCompanyId, partnerCompanyId);
			if (errorResponses.isEmpty()) {
				response.setStatusCode(200);
				updateMdfRequestAndRequestHistory(mdfRequestViewDTO, vendorCompanyId, partnerCompanyId);
			} else {
				response.setStatusCode(400);
			}
			response.setErrorResponses(errorResponses);
			return response;
		} catch (MdfDataAccessException mex) {
			throw new MdfDataAccessException(mex);
		} catch (Exception e) {
			throw new MdfDataAccessException(e);
		}
	}

	private void validateRequest(MdfRequestViewDTO mdfRequestViewDTO, List<ErrorResponse> errorResponses,
			Integer statusInInteger, Integer vendorCompanyId, Integer partnerCompanyId) {
		if (statusInInteger.equals(1)) {
			ErrorResponse statusError = new ErrorResponse("status", "Please change the status");
			errorResponses.add(statusError);
		} else if (statusInInteger.equals(2) || statusInInteger.equals(3)) {
			Double allocationAmount = mdfRequestViewDTO.getAllocationAmount();
			String allocationDateInString = mdfRequestViewDTO.getAllocationDateInString();
			String allocationExpirationDateInString = mdfRequestViewDTO.getAllocationExpirationDateInString();
			MdfAmountTilesDTO partnerMdfAmountDetails = mdfDao.getPartnerMdfAmountTilesInfo(vendorCompanyId,
					partnerCompanyId);
			validateAllocationAmount(errorResponses, allocationAmount, partnerMdfAmountDetails, mdfRequestViewDTO);
			validateRequestAmount(mdfRequestViewDTO, errorResponses, partnerMdfAmountDetails);
			validateAllocationDate(errorResponses, allocationDateInString);
			validateAllocationExpirationDate(errorResponses, allocationDateInString, allocationExpirationDateInString);
		} else if (statusInInteger.equals(4)) {
			validateReimbursementAmount(mdfRequestViewDTO, errorResponses);
		}
	}

	private void validateReimbursementAmount(MdfRequestViewDTO mdfRequestViewDTO, List<ErrorResponse> errorResponses) {
		Double reimbursementAmount = mdfRequestViewDTO.getReimbursementAmount();
		if (reimbursementAmount == null || reimbursementAmount <= 0) {
			ErrorResponse reimbursementError = new ErrorResponse("reimbursementAmount", "Please enter amount");
			errorResponses.add(reimbursementError);
		} else if (reimbursementAmount > 0) {
			Double sumOfAllocationAmount = mdfDao.getSumOfAllocationAmountByRequestId(mdfRequestViewDTO.getId());
			Double sumOfReimbursementAmount = mdfDao.getSumOfReimbursementAmountByRequestId(mdfRequestViewDTO.getId());
			Double calculatedReimbursementAmount = sumOfReimbursementAmount + reimbursementAmount;
			if (calculatedReimbursementAmount > sumOfAllocationAmount) {
				ErrorResponse reimbursementError = new ErrorResponse("reimbursementAmount",
						"Reimbursement amount is greater than Allocated Amount");
				errorResponses.add(reimbursementError);
			}
		}
	}

	private void validateAllocationAmount(List<ErrorResponse> errorResponses, Double allocationAmount,
			MdfAmountTilesDTO partnerMdfAmountDetails, MdfRequestViewDTO mdfRequestViewDTO) {
		if (allocationAmount == null || allocationAmount <= 0) {
			ErrorResponse allocationAmountError = new ErrorResponse("allocationAmount", "Please enter amount");
			errorResponses.add(allocationAmountError);
		} else if (allocationAmount > 0) {
			Double sumOfAllocationAmount = mdfDao.getSumOfAllocationAmountByRequestId(mdfRequestViewDTO.getId());
			Double calculatedAllocationAmount = sumOfAllocationAmount + allocationAmount;
			boolean validAllocationAmount = calculatedAllocationAmount > partnerMdfAmountDetails.getTotalBalance();
			if (validAllocationAmount) {
				ErrorResponse allocationAmountError = new ErrorResponse("allocationAmount",
						"Allocated Amount is greater than Total MDF Balance");
				errorResponses.add(allocationAmountError);
			}
		}
	}

	private void validateRequestAmount(MdfRequestViewDTO mdfRequestViewDTO, List<ErrorResponse> errorResponses,
			MdfAmountTilesDTO partnerMdfAmountDetails) {
		Double requestedAmount = mdfRequestViewDTO.getRequestAmount();
		boolean validRequestedAmount = requestedAmount > partnerMdfAmountDetails.getTotalBalance();
		if (validRequestedAmount) {
			ErrorResponse requestAmountError = new ErrorResponse("requestAmount",
					"Requested Amount is greater than Total MDF Balance");
			errorResponses.add(requestAmountError);
		}
	}

	private void validateAllocationDate(List<ErrorResponse> errorResponses, String allocationDateInString) {
		if (!StringUtils.hasText(allocationDateInString)) {
			ErrorResponse allocationDateError = new ErrorResponse(ALLOCATION_DATE_IN_STRING, "Please enter date");
			errorResponses.add(allocationDateError);
		}
	}

	private void validateAllocationExpirationDate(List<ErrorResponse> errorResponses, String allocationDateInString,
			String allocationExpirationDateInString) {
		if (StringUtils.hasText(allocationDateInString) && StringUtils.hasText(allocationExpirationDateInString)) {
			DateDTO allocationDateDto = DateUtils.convertStringToDateMDYFormat(allocationDateInString);
			boolean isValidAllocationDate = allocationDateDto.isValidDate();
			if (!isValidAllocationDate) {
				ErrorResponse dateErrorResponse = new ErrorResponse(ALLOCATION_DATE_IN_STRING, INVALID_DATE_FORMAT);
				errorResponses.add(dateErrorResponse);
			}
			DateDTO allocationExpirationDateDto = DateUtils
					.convertStringToDateMDYFormat(allocationExpirationDateInString);
			boolean isValidAllocationExpirationDate = allocationExpirationDateDto.isValidDate();
			if (!isValidAllocationExpirationDate) {
				ErrorResponse dateErrorResponse = new ErrorResponse("allocationExpirationDateInString",
						INVALID_DATE_FORMAT);
				errorResponses.add(dateErrorResponse);
			}
			Date allocationDate = allocationDateDto.getDate();
			Date allocationExpirationDate = allocationExpirationDateDto.getDate();
			if (isValidAllocationDate && isValidAllocationExpirationDate && allocationDate != null
					&& allocationExpirationDate != null && allocationExpirationDate.before(allocationDate)) {
				ErrorResponse dateErrorResponse = new ErrorResponse("allocationExpirationDateInString",
						"Expiration date should be after Allocation Date");
				errorResponses.add(dateErrorResponse);
			}
		}
	}

	private void updateMdfRequestAndRequestHistory(MdfRequestViewDTO mdfRequestViewDTO, Integer vendorCompanyId,
			Integer partnerCompanyId) {
		Integer requestId = mdfRequestViewDTO.getId();
		Integer statusInInteger = mdfRequestViewDTO.getStatusInInteger();
		MdfRequest mdfRequest = mdfDao.getMdfRequestById(requestId);
		setMdfWorkFlowStepType(statusInInteger, mdfRequest);
		Date allocationDate = DateUtils.convertStringToDateMDYFormat(mdfRequestViewDTO.getAllocationDateInString())
				.getDate();
		Date allocationExpirationDate = DateUtils
				.convertStringToDateMDYFormat(mdfRequestViewDTO.getAllocationExpirationDateInString()).getDate();
		mdfRequest.setAllocationDate(allocationDate);
		if (statusInInteger.equals(1) || statusInInteger.equals(2) || statusInInteger.equals(3)) {
			mdfRequest.setAllocationAmount(mdfRequestViewDTO.getAllocationAmount());
			mdfRequest.setAllocationExpirationDate(allocationExpirationDate);
		}
		if (statusInInteger.equals(4)) {
			mdfRequest.setReimbursementAmount(mdfRequestViewDTO.getReimbursementAmount());
		}
		mdfRequest.setDescription(mdfRequestViewDTO.getDescription());
		mdfRequest.setUpdatedBy(mdfRequestViewDTO.getLoggedInUserId());
		mdfRequest.setUpdatedTime(new Date());
		updateMdfRequestHistory(statusInInteger, vendorCompanyId, partnerCompanyId, mdfRequest);
		MdfRequest mdfRequestForNotification = mdfRequest;
		asyncComponent.sendMdfRequestStatusChangedNotification(mdfRequestViewDTO, mdfRequestForNotification);
	}

	private void updateMdfRequestHistory(Integer statusInInteger, Integer vendorCompanyId, Integer partnerCompanyId,
			MdfRequest mdfRequest) {
		Partnership partnership = new Partnership();
		Integer partnershipId = partnershipDAO.getPartnershipIdByVendorCompanyIdAndPartnerCompanyId(vendorCompanyId,
				partnerCompanyId);
		partnership.setId(partnershipId);
		MdfRequestHistory mdfRequestHistory = new MdfRequestHistory();
		mdfRequestHistory.setMdfRequest(mdfRequest);
		mdfRequestHistory.setPartnership(partnership);
		mdfRequestHistory.setCreatedBy(mdfRequest.getCreatedBy());
		mdfRequestHistory.setCreatedTime(mdfRequest.getCreatedTime());
		mdfRequestHistory.setMdfWorkFlowStepType(mdfRequest.getMdfWorkFlowStepType());
		mdfRequestHistory.setAllocationDate(mdfRequest.getAllocationDate());
		if (statusInInteger.equals(1) || statusInInteger.equals(2) || statusInInteger.equals(3)) {
			mdfRequestHistory.setAllocationAmount(mdfRequest.getAllocationAmount());
			mdfRequestHistory.setAllocationExpirationDate(mdfRequest.getAllocationExpirationDate());
		}
		if (statusInInteger.equals(4)) {
			mdfRequestHistory.setReimbursementAmount(mdfRequest.getReimbursementAmount());
		}
		mdfRequestHistory.setDescription(mdfRequest.getDescription());
		mdfRequestHistory.setUpdatedBy(mdfRequest.getUpdatedBy());
		mdfRequestHistory.setUpdatedTime(new Date());
		mdfDao.save(mdfRequestHistory);
	}

	private void setMdfWorkFlowStepType(Integer statusInInteger, MdfRequest mdfRequest) {
		if (statusInInteger.equals(1)) {
			mdfRequest.setMdfWorkFlowStepType(MdfWorkFlowStepType.NEW_REQUEST);
		} else if (statusInInteger.equals(2)) {
			mdfRequest.setMdfWorkFlowStepType(MdfWorkFlowStepType.IN_PROGRESS);
		} else if (statusInInteger.equals(3)) {
			mdfRequest.setMdfWorkFlowStepType(MdfWorkFlowStepType.PRE_APPROVED);
		} else if (statusInInteger.equals(4)) {
			mdfRequest.setMdfWorkFlowStepType(MdfWorkFlowStepType.REIMBURSEMENT_ISSUED);
		} else if (statusInInteger.equals(5)) {
			mdfRequest.setMdfWorkFlowStepType(MdfWorkFlowStepType.REQUEST_DECLINED);
		} else if (statusInInteger.equals(6)) {
			mdfRequest.setMdfWorkFlowStepType(MdfWorkFlowStepType.REQUEST_EXPIRED);
		} else if (statusInInteger.equals(7)) {
			mdfRequest.setMdfWorkFlowStepType(MdfWorkFlowStepType.REQUEST_DECLINED);
		}
	}

	@Override
	public XtremandResponse getRequestDetailsAndTimeLineHistory(Integer id, Integer loggedInUserCompanyId) {
		try {
			XtremandResponse response = new XtremandResponse();
			boolean validRequestId = mdfDao.validateRequestId(id, loggedInUserCompanyId);
			if (validRequestId) {
				response.setStatusCode(200);
				HashMap<String, Object> map = new HashMap<>();
				setMdfRequestDetails(id, map);
				map.put("requestHistory", setRequestHistoryDtos(id));
				response.setMap(map);
			} else {
				response.setStatusCode(404);
				response.setMessage("Invalid Request");
			}
			return response;
		} catch (MdfDataAccessException mex) {
			throw new MdfDataAccessException(mex);
		} catch (Exception e) {
			throw new MdfDataAccessException(e);
		}
	}

	@Override
	public XtremandResponse getPartnerAndMdfAmountDetails(Integer partnershipId) {
		try {
			XtremandResponse response = new XtremandResponse();
			response.setData(mdfDao.getPartnerAndMdfAmountDetails(partnershipId));
			response.setStatusCode(200);
			return response;
		} catch (MdfDataAccessException mex) {
			throw new MdfDataAccessException(mex);
		} catch (Exception e) {
			throw new MdfDataAccessException(e);
		}
	}

	@Override
	public XtremandResponse getMdfDetailsTimeLineHistory(Integer id, Integer loggedInUserCompanyId) {
		try {
			XtremandResponse response = new XtremandResponse();
			if (id != null && id > 0 && loggedInUserCompanyId != null && loggedInUserCompanyId > 0) {
				boolean validMdfDetailsId = mdfDao.validateMdfDetailsId(id, loggedInUserCompanyId);
				if (validMdfDetailsId) {
					MdfParnterDTO mdfParnterDTO = mdfDao.getPartnerAndMdfAmountDetailsByMdfDetailsId(id);
					HashMap<String, Object> map = new HashMap<>();
					map.put("partnerMdfAmountDetails", mdfParnterDTO);
					List<MdfDetailsTimeLineDTO> updatedTimeLineDTOs = new ArrayList<>();
					setMdfTimeLineDtos(id, updatedTimeLineDTOs);
					map.put("timeLineHistory", updatedTimeLineDTOs);
					response.setMap(map);
					response.setStatusCode(200);
				} else {
					invalidInput(response);
				}
			} else {
				invalidInput(response);
			}

			return response;
		} catch (MdfDataAccessException mex) {
			throw new MdfDataAccessException(mex);
		} catch (Exception e) {
			throw new MdfDataAccessException(e);
		}
	}

	private void setMdfTimeLineDtos(Integer id, List<MdfDetailsTimeLineDTO> updatedTimeLineDTOs) {
		List<MdfDetailsTimeLineDTO> timeLineDTOs = mdfDao.listMdfDetailsTimeLineHistory(id);
		for (MdfDetailsTimeLineDTO mdfDetailsTimeLineDTO : timeLineDTOs) {
			MdfDetailsTimeLineDTO updatedTimeLineDto = new MdfDetailsTimeLineDTO();
			BeanUtils.copyProperties(mdfDetailsTimeLineDTO, updatedTimeLineDto);
			String allocationDateInString = DateUtils
					.getMdfCreatedDateInString(mdfDetailsTimeLineDTO.getAllocationDate());
			String expirationDateInString = DateUtils
					.getMdfCreatedDateInString(mdfDetailsTimeLineDTO.getExpirationDate());
			updatedTimeLineDto.setAllocationDateInString(allocationDateInString);
			updatedTimeLineDto.setExpirationDateInString(expirationDateInString);
			updatedTimeLineDto.setCreatedTimeInString(
					DateUtils.getMdfCreatedDateInString(mdfDetailsTimeLineDTO.getCreatedTime()));
			if (StringUtils.hasText(mdfDetailsTimeLineDTO.getUpdaterEmailId())) {
				updatedTimeLineDto.setFullName(mdfDetailsTimeLineDTO.getUpdaterDisplayName());
				updatedTimeLineDto.setProfilePicturePath(
						xamplifyUtil.getCompleteImagePath(mdfDetailsTimeLineDTO.getUpdaterProfilePicturePath()));
			} else {
				updatedTimeLineDto.setFullName(mdfDetailsTimeLineDTO.getCreatorDisplayName());
				updatedTimeLineDto.setProfilePicturePath(
						xamplifyUtil.getCompleteImagePath(mdfDetailsTimeLineDTO.getCreatorProfilePicturePath()));
			}
			if (mdfDetailsTimeLineDTO.getUpdatedTime() != null) {
				updatedTimeLineDto
						.setCreatedTimeInUTCString(DateUtils.getUtcString(mdfDetailsTimeLineDTO.getUpdatedTime()));
			} else {
				updatedTimeLineDto
						.setCreatedTimeInUTCString(DateUtils.getUtcString(mdfDetailsTimeLineDTO.getCreatedTime()));
			}
			updatedTimeLineDto
					.setMdfAmountType(xamplifyUtil.getMdfAmountTypeInString(mdfDetailsTimeLineDTO.getMdfAmountType()));
			updatedTimeLineDTOs.add(updatedTimeLineDto);
		}
	}

	private void invalidInput(XtremandResponse response) {
		response.setStatusCode(404);
		response.setMessage("Invalid Input");
	}

	@Override
	public XtremandResponse getMdfRequestTimeLineHistory(Integer id, Integer loggedInUserCompanyId) {
		try {
			XtremandResponse response = new XtremandResponse();
			if (id != null && id > 0 && loggedInUserCompanyId != null && loggedInUserCompanyId > 0) {
				boolean validRequestId = mdfDao.validateRequestId(id, loggedInUserCompanyId);
				if (validRequestId) {
					response.setData(setRequestHistoryDtos(id));
					response.setStatusCode(200);
				} else {
					invalidInput(response);
				}
			} else {
				invalidInput(response);
			}
			return response;
		} catch (MdfDataAccessException mex) {
			throw new MdfDataAccessException(mex);
		} catch (Exception e) {
			throw new MdfDataAccessException(e);
		}
	}

	private List<MdfRequestTimeLineDTO> setRequestHistoryDtos(Integer id) {
		List<MdfRequestTimeLineDTO> timeLineHistory = mdfDao.listMdfRequestTimeLineHistory(id);
		List<MdfRequestTimeLineDTO> updatedTimeLineHistory = new ArrayList<>();
		for (MdfRequestTimeLineDTO timeLineDto : timeLineHistory) {
			MdfRequestTimeLineDTO updatedTimeLineDto = new MdfRequestTimeLineDTO();
			BeanUtils.copyProperties(timeLineDto, updatedTimeLineDto);
			String allocationDateInString = DateUtils.getMdfCreatedDateInString(timeLineDto.getAllocationDate());
			String expirationDateInString = DateUtils
					.getMdfCreatedDateInString(timeLineDto.getAllocationExpirationDate());
			updatedTimeLineDto.setAllocationDateInString(allocationDateInString);
			updatedTimeLineDto.setExpirationDateInString(expirationDateInString);
			updatedTimeLineDto
					.setCreatedTimeInString(DateUtils.getMdfCreatedDateInString(timeLineDto.getCreatedTime()));
			if (StringUtils.hasText(timeLineDto.getUpdaterEmailId())) {
				updatedTimeLineDto.setFullName(timeLineDto.getUpdaterDisplayName());
				updatedTimeLineDto.setProfilePicturePath(
						xamplifyUtil.getCompleteImagePath(timeLineDto.getUpdaterProfilePicturePath()));
			} else {
				updatedTimeLineDto.setFullName(timeLineDto.getCreatorDisplayName());
				Integer partnershipId = updatedTimeLineDto.getPartnershipId();
				Integer userId = updatedTimeLineDto.getUserId();
				List<UserDTO> userDtos = mdfDao.listPartnerDetailsFromUserList(partnershipId, userId);
				if (userDtos != null && !userDtos.isEmpty()) {
					UserDTO userDto = userDtos.get(0);
					updatedTimeLineDto.setFullName(userDto.getFullName());
				}

				updatedTimeLineDto.setProfilePicturePath(
						xamplifyUtil.getCompleteImagePath(timeLineDto.getCreatorProfilePicturePath()));
			}
			if (timeLineDto.getUpdatedTime() != null) {
				updatedTimeLineDto.setCreatedTimeInUTCString(DateUtils.getUtcString(timeLineDto.getUpdatedTime()));
			} else {
				updatedTimeLineDto.setCreatedTimeInUTCString(DateUtils.getUtcString(timeLineDto.getCreatedTime()));
			}
			MdfRequestViewDTO mdfRequestViewDTO = new MdfRequestViewDTO();
			xamplifyUtil.getMdfRequestStatusInStringAndNumber(timeLineDto.getStatus(), mdfRequestViewDTO);
			updatedTimeLineDto.setStatus(mdfRequestViewDTO.getMdfWorkFlowStepTypeInString());
			updatedTimeLineDto.setStatusInInteger(mdfRequestViewDTO.getStatusInInteger());
			updatedTimeLineHistory.add(updatedTimeLineDto);
		}
		return updatedTimeLineHistory;
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public XtremandResponse uploadRequestDocuments(MultipartFile file, MdfRequestUploadDTO mdfRequestUploadDTO) {
		try {
			XtremandResponse response = new XtremandResponse();
			Integer id = mdfRequestUploadDTO.getRequestId();
			Integer loggedInUserId = mdfRequestUploadDTO.getLoggedInUserId();
			Integer companyId = userDao.getCompanyIdByUserId(loggedInUserId);
			if (id != null && id > 0 && loggedInUserId != null && loggedInUserId > 0 && companyId != null
					&& companyId > 0) {
				boolean validRequestId = mdfDao.validateRequestId(id, companyId);
				if (validRequestId) {
					validateFileSize(file, response);
					uploadDocument(file, mdfRequestUploadDTO, response, id, loggedInUserId, companyId);
				} else {
					invalidInput(response);
				}
			} else {
				invalidInput(response);
			}
			return response;
		} catch (MdfDataAccessException mex) {
			throw new MdfDataAccessException(mex);
		} catch (Exception e) {
			throw new MdfDataAccessException(e);
		}
	}

	private void validateFileSize(MultipartFile file, XtremandResponse response) {
		long size = file.getSize();
		long fileSize = size / 1024;
		Integer maxFileSize = Integer.valueOf(maxFileSizeInString);
		if (fileSize > maxFileSize) {
			response.setStatusCode(400);
			response.setMessage("The maximum file size is 10 MB.");
		}
	}

	private void uploadDocument(MultipartFile file, MdfRequestUploadDTO mdfRequestUploadDTO, XtremandResponse response,
			Integer id, Integer loggedInUserId, Integer companyId) {
		if (response.getStatusCode() != 400) {
			String filePathSuffix = mdfRequestFolderName + '/' + companyId + '/' + id;
			AWSInputDTO awsInputDTO = new AWSInputDTO();
			awsInputDTO.setOriginalFie(file);
			awsInputDTO.setUserId(loggedInUserId);
			awsInputDTO.setFilePathSuffix(filePathSuffix);
			String awsFilePath = amazonWebService.uploadFileToAwsAndGetPath(awsInputDTO).getFilePath();
			MdfRequestDocument mdfRequestDocument = new MdfRequestDocument();
			String fileName = file.getOriginalFilename();
			mdfRequestDocument.setFileName(fileName);
			mdfRequestDocument.setFilePath(awsFilePath);
			mdfRequestDocument.setUploadedBy(loggedInUserId);
			mdfRequestDocument.setUploadedTime(new Date());
			mdfRequestDocument.setDescription(mdfRequestUploadDTO.getDescription());
			GenerateRandomPassword password = new GenerateRandomPassword();
			mdfRequestDocument.setFilePathAlias(password.getPassword());
			MdfRequest mdfRequest = new MdfRequest();
			mdfRequest.setId(id);
			mdfRequestDocument.setMdfRequest(mdfRequest);
			mdfDao.save(mdfRequestDocument);
			response.setStatusCode(200);
			asyncComponent.sendMdfRequestDocumentUploadedNotification(fileName, loggedInUserId, id);
		}

	}

	@Override
	public XtremandResponse listRequestDocuments(Pagination pagination) {
		try {
			XtremandResponse response = new XtremandResponse();
			response.setData(mdfDao.listRequestDocuments(pagination));
			response.setStatusCode(200);
			return response;
		} catch (MdfDataAccessException mex) {
			throw new MdfDataAccessException(mex);
		} catch (Exception e) {
			throw new MdfDataAccessException(e);
		}
	}

	@Override
	public String getMdfDocumentAwsFilePathByAlias(String alias) {
		try {
			String userName = utilService.getLoggedInUserName();
			List<String> userEmailIds = mdfDao.getUserEmailIdsForDownloadingDocuments(alias);
			if (userEmailIds.indexOf(userName) > -1) {
				return mdfDao.getMdfDocumentAwsFilePathByAlias(alias);
			} else {
				return mdfDao.getMdfDocumentAwsFilePathByAlias(alias);
				// return webUrl + "404";
			}

		} catch (MdfDataAccessException mex) {
			throw new MdfDataAccessException(mex);
		} catch (Exception e) {
			throw new MdfDataAccessException(e);
		}
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public XtremandResponse saveComment(MdfRequestCommentDTO mdfRequestCommentDTO) {
		try {
			XtremandResponse response = new XtremandResponse();
			MdfRequestComment mdfRequestComment = new MdfRequestComment();
			BeanUtils.copyProperties(mdfRequestCommentDTO, mdfRequestComment);
			MdfRequest mdfRequest = new MdfRequest();
			mdfRequest.setId(mdfRequestCommentDTO.getRequestId());
			mdfRequestComment.setMdfRequest(mdfRequest);
			mdfRequestComment.setCompanyId(userDao.getCompanyIdByUserId(mdfRequestCommentDTO.getCommentedBy()));
			mdfRequestComment.setCreatedTime(new Date());
			mdfDao.save(mdfRequestComment);
			response.setStatusCode(200);
			return response;

		} catch (MdfDataAccessException mex) {
			throw new MdfDataAccessException(mex);
		} catch (Exception e) {
			throw new MdfDataAccessException(e);
		}
	}

	@Override
	public XtremandResponse listComments(Integer requestId) {
		try {
			XtremandResponse response = new XtremandResponse();
			List<MdfRequestCommentDTO> comments = mdfDao.listMdfRequestComments(requestId);
			List<MdfRequestCommentDTO> updatedComments = new ArrayList<>();
			for (MdfRequestCommentDTO commentDTO : comments) {
				MdfRequestCommentDTO updatedCommentDto = new MdfRequestCommentDTO();
				BeanUtils.copyProperties(commentDTO, updatedCommentDto);
				updatedCommentDto
						.setProfilePicturePath(xamplifyUtil.getCompleteImagePath(commentDTO.getProfilePicturePath()));
				updatedCommentDto.setCommentedOnInUTCString(DateUtils.getUtcString(commentDTO.getCreatedTime()));
				Integer userId = commentDTO.getUserId();
				Integer requestCreatedBy = commentDTO.getRequestCreatedBy();
				if (userId.equals(requestCreatedBy)) {
					Integer partnershipId = commentDTO.getPartnershipId();
					List<UserDTO> userDtos = mdfDao.listPartnerDetailsFromUserList(partnershipId, userId);
					if (userDtos != null && !userDtos.isEmpty()) {
						UserDTO userDto = userDtos.get(0);
						updatedCommentDto.setFullName(userDto.getFullName());
					}
				}
				updatedComments.add(updatedCommentDto);
			}
			response.setData(updatedComments);
			return response;
		} catch (MdfDataAccessException mex) {
			throw new MdfDataAccessException(mex);
		} catch (Exception e) {
			throw new MdfDataAccessException(e);
		}
	}

}
