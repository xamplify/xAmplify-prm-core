package com.xtremand.non.transactional.service;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.xtremand.non.transactional.dao.XamplifyNonTransactionalDao;

@Service
public class XamplifyNonTransactionalService {

	@Autowired
	private XamplifyNonTransactionalDao xamplifyNonTransactionalDao;

	public void updateIsPublishedToPartnerListByIds(Set<Integer> damIds, boolean value) {
		xamplifyNonTransactionalDao.updateIsPublishedToPartnerListByIds(damIds, value);
	}

}
