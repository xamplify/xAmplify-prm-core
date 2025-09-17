package com.xtremand.gdpr.setting.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.xtremand.common.bom.CompanyProfile;
import com.xtremand.dao.util.GenericDAO;
import com.xtremand.formbeans.XtremandResponse;
import com.xtremand.gdpr.setting.bom.GdprSetting;
import com.xtremand.gdpr.setting.bom.GdprSettingView;
import com.xtremand.gdpr.setting.dao.GdprSettingDao;
import com.xtremand.gdpr.setting.dto.GdprSettingDTO;
import com.xtremand.gdpr.setting.dto.LegalBasisDTO;
import com.xtremand.gdpr.setting.dto.LegalBasisSaveRequest;
import com.xtremand.gdpr.setting.exception.GdprSettingDataAccessException;
import com.xtremand.gdpr.setting.service.GdprSettingService;
import com.xtremand.user.bom.LegalBasis;
import com.xtremand.user.service.UserService;

@Service
@Transactional
public class GdprSettingServiceImpl implements GdprSettingService {
	
	@Autowired
	private GdprSettingDao gdprSettingDao;
	
	@Autowired
	UserService userService;
	
	@Autowired
    private GenericDAO genericDAO;
	
	@Override
	public XtremandResponse save(GdprSettingDTO gdprSettingDto) {
		try {
			XtremandResponse response = new XtremandResponse();
			GdprSetting gdprSetting = new GdprSetting();
			BeanUtils.copyProperties(gdprSettingDto, gdprSetting);
			CompanyProfile companyProfile = new CompanyProfile();
			companyProfile.setId(gdprSettingDto.getCompanyId());
			gdprSetting.setCompanyProfile(companyProfile);
			gdprSetting.setCreatedTime(new Date());
			gdprSettingDao.save(gdprSetting);
			response.setStatusCode(200);
			response.setMessage("Settings Added Successfully");
			return response;
		} catch(GdprSettingDataAccessException ge){
			throw new GdprSettingDataAccessException(ge);
		}
		catch (Exception e) {
			throw new GdprSettingDataAccessException(e);
		}

	}

	@Override
	public XtremandResponse getByCompanyId(Integer companyId) {
		try {
			XtremandResponse response = new XtremandResponse();
			GdprSettingView gdprSettingView = gdprSettingDao.getByCompanyId(companyId);
			if(gdprSettingView!=null){
				response.setStatusCode(200);
				response.setData(gdprSettingView);
			}else{
				response.setStatusCode(404);
				response.setMessage("No settings found for this company");
			}
			return response;
		} catch(GdprSettingDataAccessException ge){
			throw new GdprSettingDataAccessException(ge);
		}
		catch (Exception e) {
			throw new GdprSettingDataAccessException(e);
		}

	}

	@Override
	public XtremandResponse update(GdprSettingDTO gdprSettingDto) {
		try {
			XtremandResponse response = new XtremandResponse();
			GdprSetting gdprSetting = gdprSettingDao.getSettingByCompanyId(gdprSettingDto.getCompanyId());
			gdprSetting.setUpdatedTime(new Date());
			gdprSetting.setUpdatedUserId(gdprSettingDto.getUpdatedUserId());
			gdprSetting.setGdprStatus(gdprSettingDto.isGdprStatus());
			gdprSetting.setAllowMarketingEmails(gdprSettingDto.isAllowMarketingEmails());
			gdprSetting.setUnsubscribeStatus(gdprSettingDto.isUnsubscribeStatus());
			gdprSetting.setFormStatus(gdprSettingDto.isFormStatus());
			gdprSetting.setTermsAndConditionStatus(gdprSettingDto.isTermsAndConditionStatus());
			gdprSetting.setDeleteContactStatus(gdprSettingDto.isDeleteContactStatus());
			gdprSetting.setEventStatus(gdprSettingDto.isEventStatus());
			response.setStatusCode(200);
			response.setMessage("Settings Updated Successfully");
			return response;
		} catch(GdprSettingDataAccessException ge){
			throw new GdprSettingDataAccessException(ge);
		}
		catch (Exception e) {
			throw new GdprSettingDataAccessException(e);
		}

	}

	@Override
	public XtremandResponse getLegalBasis(Integer companyId) {
		XtremandResponse response = new XtremandResponse();
		List<LegalBasis> legalBasisList = gdprSettingDao.getLegalBasisListForCompany(companyId);
		if(legalBasisList != null){
			response.setStatusCode(200);
			response.setData(getLegalBasisDTOList(legalBasisList));
		}
		return response;
	}

	private List<LegalBasisDTO> getLegalBasisDTOList(List<LegalBasis> legalBasisList) {
		List<LegalBasisDTO> legalBasisDTOList = null;
		if(legalBasisList != null){
			legalBasisDTOList = new ArrayList<LegalBasisDTO>();
			for(LegalBasis legalBasis : legalBasisList){
				if(legalBasis != null){
					LegalBasisDTO legalBasisDTO = new LegalBasisDTO();
					BeanUtils.copyProperties(legalBasis, legalBasisDTO);
					legalBasisDTOList.add(legalBasisDTO);
				}
			}
		}
		return legalBasisDTOList;
	}

	@Override
	public XtremandResponse saveLegalBasis(LegalBasisSaveRequest request) {
		XtremandResponse response = new XtremandResponse();
		response.setMessage("Fail");
		if(request != null){
			Integer userId = request.getUserId();
			Integer companyId = request.getCompanyId();
			List<LegalBasisDTO> legalBasisDTOList = request.getLegalBasis();
			if(userId != null && userId > 0 && companyId != null && companyId > 0 && legalBasisDTOList != null){
				List<LegalBasis> existingLegalBasisList = gdprSettingDao.getLegalBasisAddedByCompanyId(companyId);
				Map<Integer, LegalBasis> existingLegalBasisMap = existingLegalBasisList.stream()
						.collect(Collectors.toMap(LegalBasis::getId, c -> c));
				Set<Integer> newIds = new HashSet<>(); 
				for (LegalBasisDTO legalBasisDTO : legalBasisDTOList) {
					if (legalBasisDTO != null) {
						if (legalBasisDTO.getId() != null) {
							updateLegalBasis(existingLegalBasisMap.get(legalBasisDTO.getId()), legalBasisDTO, userId);
							newIds.add(legalBasisDTO.getId());
						} else {
							createLegalBasis(companyId, userId, legalBasisDTO);
						}
					}
				}
				Set<Integer> existingIds = existingLegalBasisMap.keySet();
				existingIds.removeAll(newIds);
				for(Integer existingId : existingIds){
					gdprSettingDao.deleteLegalBasis(existingLegalBasisMap.get(existingId));
				}
				response.setStatusCode(200);
				response.setMessage("Success");
			}
		}
		return response;
	}

	private void updateLegalBasis(LegalBasis legalBasis, LegalBasisDTO legalBasisDTO, Integer userId) {
		boolean isUpdated = false;
		if (!legalBasis.getName().equals(legalBasisDTO.getName())) {
			legalBasis.setName(legalBasisDTO.getName());
			isUpdated = true;
		}
		if (!legalBasis.getDescription().equals(legalBasisDTO.getDescription())) {
			legalBasis.setDescription(legalBasisDTO.getDescription());
			isUpdated = true;
		}
		if(isUpdated){
			legalBasis.initialiseCommonFields(false, userId);
		}
	}

	private void createLegalBasis(Integer companyId, Integer userId, LegalBasisDTO legalBasisDTO) {
		LegalBasis legalBasis = new LegalBasis();
		CompanyProfile companyProfile = new CompanyProfile();
		companyProfile.setId(companyId);
		legalBasis.setCompany(companyProfile);
		legalBasis.setCreatedBy(userId);
		legalBasis.setName(legalBasisDTO.getName());
		legalBasis.setDescription(legalBasisDTO.getDescription());
		legalBasis.setDefault(false);
		legalBasis.initialiseCommonFields(true, userId);
		genericDAO.save(legalBasis);
	}

	@Override
	public boolean isGdprEnabled(Integer companyId) {
		boolean isGdprEnabled = true;
		GdprSettingView gdprSetting = gdprSettingDao.getByCompanyId(companyId);
		if(gdprSetting != null){
			isGdprEnabled = gdprSetting.isGdprStatus();
		}
		return isGdprEnabled;
	}

	@Override
	public List<LegalBasis> getSelectByDefaultLegalBasis() {
		return gdprSettingDao.getSelectByDefaultLegalBasis();
	}
	
	@Override
	public void removeLegalBasis(List<Integer> userIdsList, List<Integer> userListIds){
		gdprSettingDao.removeLegalBasis(userIdsList, userListIds);
	}

}
