package com.xtremand.util.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.xtremand.common.bom.StatusCode;
import com.xtremand.dao.util.GenericDAO;
import com.xtremand.user.bom.Role;
import com.xtremand.user.service.UserService;
import com.xtremand.video.bom.VideoCategory;
import com.xtremand.video.service.VideoService;

@Service
@Transactional(readOnly = true, rollbackFor = Exception.class)
public class ReferenceDataLoader implements ApplicationListener<ContextRefreshedEvent> {
	private static final Logger logger = LoggerFactory.getLogger(ReferenceDataLoader.class);

	@Autowired
	UserService userService;

	@Autowired
	VideoService videoService;

	@Autowired
	GenericDAO genericDao;

	public void init() {
		logger.info("loader started");

		genericDao.load(Role.class);
		logger.info("Roles loaded");

		genericDao.load(VideoCategory.class);
		logger.info("Categories loaded");

		List<StatusCode> statusCodelist = genericDao.load(StatusCode.class);
		logger.info("StatusCode loaded");

		logger.info("loader ended");
		StatusCode.initialize(statusCodelist);
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent arg0) {
		init();
	}
}
