package com.xtremand.unsubscribe.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.xtremand.common.bom.Pagination;
import com.xtremand.formbeans.XtremandResponse;
import com.xtremand.unsubscribe.bom.UnsubscribePageDetails;
import com.xtremand.unsubscribe.bom.UnsubscribeReason;
import com.xtremand.unsubscribe.dao.UnsubscribeDao;
import com.xtremand.unsubscribe.dto.UnsubscribePageDetailsDTO;
import com.xtremand.unsubscribe.dto.UnsubscribeReasonDTO;
import com.xtremand.unsubscribe.exception.UnsubscribeDataAccessException;
import com.xtremand.unsubscribe.service.UnsubscribeService;
import com.xtremand.user.dao.UserDAO;

@Service
@Transactional
public class UnsubscribeServiceImpl implements UnsubscribeService {

	@Autowired
	private UnsubscribeDao unsubscribeDao;

	@Autowired
	private UserDAO userDao;

	@Override
	public XtremandResponse findAll(Pagination pagination) {
		try {
			XtremandResponse response = new XtremandResponse();
			response.setStatusCode(200);
			response.setData(unsubscribeDao.findAll(pagination));
			return response;
		} catch (UnsubscribeDataAccessException e) {
			throw new UnsubscribeDataAccessException(e);
		} catch (Exception ex) {
			throw new UnsubscribeDataAccessException(ex);
		}
	}

	@Override
	public XtremandResponse findById(Integer id) {
		try {
			XtremandResponse response = new XtremandResponse();
			UnsubscribeReason unsubscribeReason = unsubscribeDao.findById(id);
			response.setData(unsubscribeReason);
			response.setStatusCode(200);
			return response;
		} catch (UnsubscribeDataAccessException e) {
			throw new UnsubscribeDataAccessException(e);
		} catch (Exception ex) {
			throw new UnsubscribeDataAccessException(ex);
		}
	}

	@Override
	public XtremandResponse delete(Integer id) {
		try {
			XtremandResponse response = new XtremandResponse();
			unsubscribeDao.delete(id);
			response.setStatusCode(200);
			response.setMessage("Reason is deleted successfully.");
			return response;
		} catch (UnsubscribeDataAccessException e) {
			throw new UnsubscribeDataAccessException(e);
		} catch (Exception ex) {
			throw new UnsubscribeDataAccessException(ex);
		}
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public XtremandResponse save(UnsubscribeReasonDTO unsubscribeReasonDTO) {
		try {
			XtremandResponse response = new XtremandResponse();
			UnsubscribeReason unsubscribeReason = new UnsubscribeReason();
			BeanUtils.copyProperties(unsubscribeReasonDTO, unsubscribeReason);
			Integer companyId = userDao.getCompanyIdByUserId(unsubscribeReasonDTO.getCreatedUserId());
			unsubscribeReason.setCompanyId(companyId);
			unsubscribeReason.setCreatedTime(new Date());
			unsubscribeReason.setUpdatedTime(new Date());
			unsubscribeReason.setUpdatedUserId(unsubscribeReasonDTO.getCreatedUserId());
			unsubscribeDao.save(unsubscribeReason);
			response.setStatusCode(200);
			response.setMessage("Reason has been added successfully.");
			return response;
		} catch (UnsubscribeDataAccessException e) {
			throw new UnsubscribeDataAccessException(e);
		} catch (Exception ex) {
			throw new UnsubscribeDataAccessException(ex);
		}
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public XtremandResponse update(UnsubscribeReasonDTO unsubscribeReasonDTO) {
		try {
			XtremandResponse response = new XtremandResponse();
			UnsubscribeReason unsubscribeReason = unsubscribeDao.findById(unsubscribeReasonDTO.getId());
			unsubscribeReason.setReason(unsubscribeReasonDTO.getReason());
			unsubscribeReason.setCustomReason(unsubscribeReasonDTO.isCustomReason());
			unsubscribeReason.setUpdatedTime(new Date());
			unsubscribeReason.setUpdatedUserId(unsubscribeReasonDTO.getCreatedUserId());
			response.setStatusCode(200);
			response.setMessage("Reason is updated successfully.");
			return response;
		} catch (UnsubscribeDataAccessException e) {
			throw new UnsubscribeDataAccessException(e);
		} catch (Exception ex) {
			throw new UnsubscribeDataAccessException(ex);
		}
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public XtremandResponse addDefaultReasons() {
		try {
			XtremandResponse response = new XtremandResponse();
			List<Integer> companyIds = userDao.findAllCompanyIds();
			addReasonsAndHeaderAndFooterText(companyIds);
			response.setStatusCode(200);
			response.setMessage("Default Unsubscribe Reasons Are Added Successfully");
			return response;
		} catch (UnsubscribeDataAccessException e) {
			throw new UnsubscribeDataAccessException(e);
		} catch (Exception ex) {
			throw new UnsubscribeDataAccessException(ex);
		}
	}

	private void addReasonsAndHeaderAndFooterText(List<Integer> companyIds) {
		List<UnsubscribeReason> unsubscribeReasons = new ArrayList<>();
		List<UnsubscribePageDetails> unsubscribePageDetails = new ArrayList<>();
		for (Integer companyId : companyIds) {
			UnsubscribeReason unsubscribeReason1 = setReason(companyId,
					"Want to receive fewer messages", false);
			UnsubscribeReason unsubscribeReason2 = setReason(companyId, "No Longer participating in Channel", false);
			UnsubscribeReason unsubscribeReason3 = setReason(companyId, "I don't remember signing up to this list", false);
			UnsubscribeReason unsubscribeReason4 = setReason(companyId, "The content isn't really relevant for me",
					false);
			UnsubscribeReason unsubscribeReason5 = setReason(companyId, "Other", true);
			unsubscribeReasons.add(unsubscribeReason1);
			unsubscribeReasons.add(unsubscribeReason2);
			unsubscribeReasons.add(unsubscribeReason3);
			unsubscribeReasons.add(unsubscribeReason4);
			unsubscribeReasons.add(unsubscribeReason5);

			UnsubscribePageDetails unsubscribePageDetail = new UnsubscribePageDetails();
			unsubscribePageDetail.setCompanyId(companyId);
			unsubscribePageDetail.setCreatedTime(new Date());
			unsubscribePageDetail.setCreatedUserId(1);
			unsubscribePageDetail.setUpdatedTime(new Date());
			unsubscribePageDetail.setUpdatedUserId(1);
			unsubscribePageDetail.setHeaderText(
					"Sorry to see you go! To improve our services, please let us know why you are leaving");
			unsubscribePageDetail.setFooterText(
					"By unsubscribing, you confirm that you have read and agree to the <a href=\"https://xamplify.com/terms-of-uses/\" target=\"_blank\">xAmplify Terms of Use</a> and <a href=\"https://www.xamplify.com/privacy-policy/\" target=\"blank\" >Privacy Policy</a>."
							+ "Moreover, you have the right to request any data collected by xAmplify in pursuant to <a href=\"https://gdpr-info.eu/\" target=\"_blank\">GDPR</a> and <a href=\"https://www.caprivacy.org/\" target=\"_blank\">CCPA</a>");
			unsubscribePageDetails.add(unsubscribePageDetail);

		}
		unsubscribeDao.saveAll(unsubscribeReasons);
		unsubscribeDao.saveAll(unsubscribePageDetails);
	}

	private UnsubscribeReason setReason(Integer companyId, String reason, boolean customReason) {
		UnsubscribeReason unsubscribeReason = new UnsubscribeReason();
		unsubscribeReason.setCompanyId(companyId);
		unsubscribeReason.setReason(reason);
		unsubscribeReason.setCustomReason(customReason);
		unsubscribeReason.setCreatedUserId(1);
		unsubscribeReason.setCreatedTime(new Date());
		unsubscribeReason.setUpdatedTime(new Date());
		unsubscribeReason.setUpdatedUserId(1);
		return unsubscribeReason;
	}

	@Override
	public XtremandResponse findAll(Integer companyId) {
		try {
			XtremandResponse response = new XtremandResponse();
			response.setData(unsubscribeDao.findAll(companyId));
			response.setStatusCode(200);
			return response;
		} catch (UnsubscribeDataAccessException e) {
			throw new UnsubscribeDataAccessException(e);
		} catch (Exception ex) {
			throw new UnsubscribeDataAccessException(ex);
		}
	}

	@Override
	public XtremandResponse findUnsubscribePageDetailsByCompanyId(Integer userId) {
		try {
			XtremandResponse response = new XtremandResponse();
			UnsubscribePageDetails unsubscribePageDetails = findCompanyIdAndUnsubscribePageDetails(userId);
			if(unsubscribePageDetails!=null) {
				response.setData(unsubscribePageDetails);
			}else {
				response.setData(new UnsubscribePageDetails());
			}
			response.setStatusCode(200);
			return response;
		} catch (UnsubscribeDataAccessException e) {
			throw new UnsubscribeDataAccessException(e);
		} catch (Exception ex) {
			throw new UnsubscribeDataAccessException(ex);
		}
	}

	private UnsubscribePageDetails findCompanyIdAndUnsubscribePageDetails(Integer userId) {
		Integer companyId = userDao.getCompanyIdByUserId(userId);
		return unsubscribeDao.findUnsubscribePageDetailsByCompanyId(companyId);
	}

	@Override
	public XtremandResponse updateHeaderAndFooterText(UnsubscribePageDetailsDTO unsubscribePageDetailsDTO) {
		try {
			XtremandResponse response = new XtremandResponse();
			UnsubscribePageDetails unsubscribePageDetails = findCompanyIdAndUnsubscribePageDetails(
					unsubscribePageDetailsDTO.getUserId());
			unsubscribePageDetails.setHeaderText(unsubscribePageDetailsDTO.getHeaderText());
			unsubscribePageDetails.setFooterText(unsubscribePageDetailsDTO.getFooterText());
			unsubscribePageDetails.setHideFooterText(unsubscribePageDetailsDTO.isHideFooterText());
			unsubscribePageDetails.setHideHeaderText(unsubscribePageDetailsDTO.isHideHeaderText());
			unsubscribePageDetails.setUpdatedTime(new Date());
			unsubscribePageDetails.setUpdatedUserId(unsubscribePageDetailsDTO.getUserId());
			response.setStatusCode(200);
			return response;
		} catch (UnsubscribeDataAccessException e) {
			throw new UnsubscribeDataAccessException(e);
		} catch (Exception ex) {
			throw new UnsubscribeDataAccessException(ex);
		}
	}

	@Override
	public XtremandResponse findUnsubscribePageContent(Integer userId) {
		try {
			XtremandResponse response = new XtremandResponse();
			Integer companyId = userDao.getCompanyIdByUserId(userId);
			return findCompanyIdAndUnsubscribePageContent(response, companyId);
		} catch (UnsubscribeDataAccessException e) {
			throw new UnsubscribeDataAccessException(e);
		} catch (Exception ex) {
			throw new UnsubscribeDataAccessException(ex);
		}
	}

	private XtremandResponse findCompanyIdAndUnsubscribePageContent(XtremandResponse response, Integer companyId) {
		UnsubscribePageDetails unsubscribePageDetails = unsubscribeDao
				.findUnsubscribePageDetailsByCompanyId(companyId);
		List<UnsubscribeReason> unsubscribeReasons = unsubscribeDao.findAll(companyId);
		String companyLogoPath = userDao.findCompanyLogoPath(companyId);
		Map<String, Object> map = new HashMap<>();
		map.put("unsubscribePageDetails", unsubscribePageDetails);
		map.put("unsubscribeReasons", unsubscribeReasons);
		map.put("companyLogoPath", companyLogoPath);
		response.setData(map);
		response.setStatusCode(200);
		return response;
	}

	@Override
	public XtremandResponse findUnsubscribePageContentByCompanyId(Integer companyId) {
		try {
			XtremandResponse response = new XtremandResponse();
			return findCompanyIdAndUnsubscribePageContent(response, companyId);
		} catch (UnsubscribeDataAccessException e) {
			throw new UnsubscribeDataAccessException(e);
		} catch (Exception ex) {
			throw new UnsubscribeDataAccessException(ex);
		}
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public void addDefaultReasonsAndHeaderAndTextByCompanyId(Integer companyId) {
		addReasonsAndHeaderAndFooterText(Arrays.asList(companyId));
	}

}
