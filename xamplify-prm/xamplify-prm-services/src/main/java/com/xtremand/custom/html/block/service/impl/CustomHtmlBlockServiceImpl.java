package com.xtremand.custom.html.block.service.impl;

import java.util.Date;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.xtremand.common.bom.Pagination;
import com.xtremand.custom.html.block.bom.CustomHtmlBlock;
import com.xtremand.custom.html.block.dao.CustomHtmlblockDAO;
import com.xtremand.custom.html.block.dto.CustomHtmlBlockDTO;
import com.xtremand.custom.html.block.service.CustomHtmlBlockService;
import com.xtremand.dao.util.GenericDAO;
import com.xtremand.formbeans.XtremandResponse;
import com.xtremand.user.dao.UserDAO;
import com.xtremand.util.XamplifyUtils;
import com.xtremand.util.dto.Pageable;
import com.xtremand.util.service.UtilService;

@Service
@Transactional
public class CustomHtmlBlockServiceImpl implements CustomHtmlBlockService {

	@Autowired
	private CustomHtmlblockDAO customHtmlBlockDao;

	@Autowired
	private UtilService utilService;

	@Autowired
	private GenericDAO genericDao;

	@Autowired
	private UserDAO userDao;

	@Override
	public XtremandResponse findPaginatedCustomHtmls(Pageable pageable, Integer loggedInUserId) {
		XtremandResponse response = new XtremandResponse();
		if (XamplifyUtils.isValidInteger(loggedInUserId)) {
			Integer companyId = userDao.getCompanyIdByUserId(loggedInUserId);
			Pagination pagination = utilService.setPageableParameters(pageable, loggedInUserId);
			pagination.setUserId(loggedInUserId);
			pagination.setCompanyId(companyId);
			Map<String, Object> map = customHtmlBlockDao.findPaginatedCustomHtmls(pagination);
			response.setData(map);
			XamplifyUtils.addSuccessStatus(response);
		}
		return response;
	}

	@Override
	public XtremandResponse save(CustomHtmlBlockDTO customHtmlBlockDto) {
		XtremandResponse response = new XtremandResponse();
		Integer loggedInUserId = customHtmlBlockDto.getLoggedInUserId();
		String title = customHtmlBlockDto.getTitle();
		if (XamplifyUtils.isValidInteger(loggedInUserId)) {
			Integer companyId = userDao.getCompanyIdByUserId(loggedInUserId);
			boolean isTitleExist = customHtmlBlockDao.isTitleExist(title, companyId, null);
			if (isTitleExist) {
				XamplifyUtils.addErorMessageWithStatusCode(response, title + " Already Exists", 413);
			} else if (title.length() > 55) {
				XamplifyUtils.addErorMessageWithStatusCode(response, "The maximum character limit is 55", 413);
			} else {
				CustomHtmlBlock customHtmlBlock = new CustomHtmlBlock();
				customHtmlBlock.setTitle(title);
				customHtmlBlock.setHtmlBody(customHtmlBlockDto.getHtmlBody());
				customHtmlBlock.setLeftHtmlBody(customHtmlBlockDto.getLeftHtmlBody());
				customHtmlBlock.setRightHtmlBody(customHtmlBlockDto.getRightHtmlBody());
				customHtmlBlock.setCompanyId(companyId);
				customHtmlBlock.setCreatedUserId(loggedInUserId);
				customHtmlBlock.setCreatedTime(new Date());
				customHtmlBlock.setSelected(customHtmlBlockDto.isSelected());
				customHtmlBlock.setLayoutSize(customHtmlBlockDto.getLayoutSize());
				customHtmlBlock.setTitleVisible(customHtmlBlockDto.isTitleVisible());
				genericDao.save(customHtmlBlock);
				XamplifyUtils.addSuccessStatusWithMessage(response,
						"Your custom block has been saved successfully");
			}
		}
		return response;
	}

	@Override
	public XtremandResponse findById(Integer id, Integer loggedInUserId) {
		XtremandResponse response = new XtremandResponse();
		if (XamplifyUtils.isValidInteger(loggedInUserId)) {
			Integer companyId = userDao.getCompanyIdByUserId(loggedInUserId);
			response.setData(customHtmlBlockDao.findById(id, companyId));
			XamplifyUtils.addSuccessStatus(response);
		}
		return response;
	}

	@Override
	public XtremandResponse update(CustomHtmlBlockDTO customHtmlBlockDto) {
		XtremandResponse response = new XtremandResponse();
		Integer loggedInUserId = customHtmlBlockDto.getLoggedInUserId();
		Integer customHtmlBlockId = customHtmlBlockDto.getId();
		String title = customHtmlBlockDto.getTitle();
		if (XamplifyUtils.isValidInteger(loggedInUserId)) {
			Integer companyId = userDao.getCompanyIdByUserId(loggedInUserId);
			boolean isTitleExist = customHtmlBlockDao.isTitleExist(title, companyId, customHtmlBlockId);
			if (isTitleExist) {
				XamplifyUtils.addErorMessageWithStatusCode(response, title + " Already Exists", 413);
			} else if (title.length() > 55) {
				XamplifyUtils.addErorMessageWithStatusCode(response, "The maximum character limit is 55", 413);
			} else {
				CustomHtmlBlock customHtmlBlock = genericDao.get(CustomHtmlBlock.class, customHtmlBlockDto.getId());
				customHtmlBlock.setTitle(title);
				customHtmlBlock.setHtmlBody(customHtmlBlockDto.getHtmlBody());
				customHtmlBlock.setLeftHtmlBody(customHtmlBlockDto.getLeftHtmlBody());
				customHtmlBlock.setRightHtmlBody(customHtmlBlockDto.getRightHtmlBody());
				customHtmlBlock.setLayoutSize(customHtmlBlockDto.getLayoutSize());
				customHtmlBlock.setUpdatedUserId(loggedInUserId);
				customHtmlBlock.setUpdatedTime(new Date());
				customHtmlBlock.setSelected(customHtmlBlockDto.isSelected());
				customHtmlBlock.setTitleVisible(customHtmlBlockDto.isTitleVisible());
				XamplifyUtils.addSuccessStatusWithMessage(response,
						"Your custom block has been updated successfully");
			}
		}
		return response;
	}

	@Override
	public XtremandResponse delete(Integer id, Integer loggedInUserId) {
		XtremandResponse response = new XtremandResponse();
		if (XamplifyUtils.isValidInteger(loggedInUserId)) {
			Integer companyId = userDao.getCompanyIdByUserId(loggedInUserId);
			customHtmlBlockDao.delete(id, companyId);
			XamplifyUtils.addSuccessStatusWithMessage(response, "Custom block deleted successfully");
		}
		return response;
	}

	@Override
	public XtremandResponse updateSelectedHtmlBlock(CustomHtmlBlockDTO customHtmlBlockDto) {
		XtremandResponse response = new XtremandResponse();
		Integer loggedInUserId = customHtmlBlockDto.getLoggedInUserId();
		Set<CustomHtmlBlockDTO> customHtmlBlockDtos = customHtmlBlockDto.getCustomHtmlBlockDtos();
		if (XamplifyUtils.isValidInteger(loggedInUserId)) {
			for (CustomHtmlBlockDTO htmlBlockDto : customHtmlBlockDtos) {
				CustomHtmlBlock customHtmlBlock = genericDao.get(CustomHtmlBlock.class, htmlBlockDto.getId());
				customHtmlBlock.setSelected(htmlBlockDto.isSelected());
			}
			XamplifyUtils.addSuccessStatusWithMessage(response, "Updated successfully");
		}
		return response;
	}

}
