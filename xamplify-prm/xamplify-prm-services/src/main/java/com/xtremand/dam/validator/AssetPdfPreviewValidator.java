package com.xtremand.dam.validator;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.xtremand.common.bom.CompanyProfile;
import com.xtremand.common.bom.FindLevel;
import com.xtremand.dam.dao.DamDao;
import com.xtremand.dam.dto.AssetPdfPreviewRequestDTO;
import com.xtremand.lms.bom.LearningTrack;
import com.xtremand.lms.bom.LearningTrackVisibility;
import com.xtremand.lms.dao.LMSDAO;
import com.xtremand.lms.dto.LearningTrackContentDto;
import com.xtremand.partnership.bom.Partnership;
import com.xtremand.partnership.dao.PartnershipDAO;
import com.xtremand.user.bom.Role;
import com.xtremand.user.dao.UserDAO;
import com.xtremand.util.XamplifyUtils;
import com.xtremand.util.dao.UtilDao;
import com.xtremand.util.dto.XamplifyConstants;
import com.xtremand.util.dto.XamplifyUtilValidator;

@Component
public class AssetPdfPreviewValidator implements Validator {

	@Autowired
	private XamplifyUtilValidator xamplifyUtilValidator;

	@Autowired
	private UserDAO userDao;

	@Autowired
	private DamDao damDao;

	@Autowired
	private UtilDao utilDao;

	@Autowired
	private LMSDAO lmsDao;

	@Autowired
	private PartnershipDAO partnershipDao;

	@Override
	public boolean supports(Class<?> clazz) {
		return AssetPdfPreviewValidator.class.isAssignableFrom(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		AssetPdfPreviewRequestDTO assetPdfPreviewRequestDTO = (AssetPdfPreviewRequestDTO) target;

		Integer id = assetPdfPreviewRequestDTO.getId();
		boolean isTrackOrPlayBookPdfPreview = assetPdfPreviewRequestDTO.isTrackOrPlayBookPdfPreview();
		Integer userId = assetPdfPreviewRequestDTO.getUserId();

		xamplifyUtilValidator.validateIdParameter(errors, id);

		xamplifyUtilValidator.validateUserIdParameter(errors, userId);

		if (!errors.hasErrors()) {
			if (isTrackOrPlayBookPdfPreview) {
				Integer learningTrackContentMappingId = id;
				assetPdfPreviewRequestDTO.setId(null);
				validateTracksOrPlayBooksPdfPreviewForPartnerLogin(learningTrackContentMappingId, userId,
						assetPdfPreviewRequestDTO);
			} else {
				validatePdfViewAccessForVendor(id, userId);
			}
		}

	}

	private void validateTracksOrPlayBooksPdfPreviewForPartnerLogin(Integer learningTrackContentId, Integer userId,
			AssetPdfPreviewRequestDTO assetPdfPreviewRequestDTO) {
		CompanyProfile loggedInCompany = userDao.findByPrimaryKey(userId, new FindLevel[] { FindLevel.COMPANY_PROFILE })
				.getCompanyProfile();
		LearningTrackContentDto learningTrackContentDto = lmsDao.getLearningTrackIdByContentId(learningTrackContentId);
		if (learningTrackContentDto != null) {
			Integer learningTrackId = learningTrackContentDto.getId();
			Integer damId = learningTrackContentDto.getDamId();
			assetPdfPreviewRequestDTO.setId(damId);
			LearningTrack learningTrack = lmsDao.findById(learningTrackId);
			boolean hasAccess = canViewLearningTrack(learningTrack, userId, loggedInCompany);
			if (!hasAccess) {
				throw new AccessDeniedException(XamplifyConstants.PAGE_NOT_FOUND_MESSAGE);
			}
		} else {
			throw new AccessDeniedException(XamplifyConstants.PAGE_NOT_FOUND_MESSAGE);
		}

	}

	private void validatePdfViewAccessForVendor(Integer id, Integer userId) {
		Integer userCompanyId = userDao.getCompanyIdByUserId(userId);
		Integer assetPdfCompanyId = damDao.getCompanyIdById(id);
		boolean isValidUserCompanyId = XamplifyUtils.isValidInteger(userCompanyId);
		boolean isValidCompanyId = XamplifyUtils.isValidInteger(assetPdfCompanyId);
		boolean isCompanyIdsMatched = isValidUserCompanyId && isValidCompanyId
				&& userCompanyId.equals(assetPdfCompanyId);
		if (isCompanyIdsMatched) {
			boolean hasAccessToViewAsset = false;
			List<String> roleNames = userDao.listRolesByUserId(userId);
			boolean damAccess = utilDao.hasDamAccessByUserId(userId);
			boolean isDamRole = roleNames.indexOf(Role.DAM.getRoleName()) > -1;
			boolean isTrackRole = roleNames.indexOf(Role.LEARNING_TRACK.getRoleName()) > -1;
			boolean isPlayBookRole = roleNames.indexOf(Role.PLAY_BOOK.getRoleName()) > -1;
			boolean isAnyAdmin = XamplifyUtils.hasAnyAdminRole(roleNames);
			hasAccessToViewAsset = damAccess && (isDamRole || isTrackRole || isPlayBookRole || isAnyAdmin);
			if (!hasAccessToViewAsset) {
				throw new AccessDeniedException(XamplifyConstants.PAGE_NOT_FOUND_MESSAGE);
			}

		} else {
			throw new AccessDeniedException(XamplifyConstants.PAGE_NOT_FOUND_MESSAGE);
		}
	}

	public boolean checkVisibility(LearningTrack learningTrack, Integer loggedInCompanyId, Integer loggedInUserId) {
		if (learningTrack.isPublished()) {
			Partnership partnership = partnershipDao.checkPartnership(learningTrack.getCompany().getId(),
					loggedInCompanyId);
			if (partnership != null) {
				LearningTrackVisibility visibilityUser = lmsDao.getVisibilityUser(loggedInUserId, partnership.getId(),
						learningTrack.getId());
				if (visibilityUser != null) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean canViewLearningTrack(LearningTrack learningTrack, Integer loggedInUserId,
			CompanyProfile loggedInCompany) {
		boolean canView = false;
		if (learningTrack != null && loggedInUserId != null && loggedInCompany != null) {
			boolean isUserCompanyMatched = loggedInCompany.getId().equals(learningTrack.getCompany().getId());
			boolean isVisibilityExists = checkVisibility(learningTrack, loggedInCompany.getId(), loggedInUserId);
			canView = isUserCompanyMatched || isVisibilityExists;
		}
		return canView;
	}

}
