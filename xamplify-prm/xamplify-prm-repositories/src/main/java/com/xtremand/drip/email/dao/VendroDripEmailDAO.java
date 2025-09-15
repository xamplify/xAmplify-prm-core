package com.xtremand.drip.email.dao;

import java.sql.Timestamp;
import java.util.List;

public interface VendroDripEmailDAO {

	public List<Object[]> listAllIntroEmailIds(Timestamp presentTs);

	public List<Object[]> listAllKickOffEmailIds(Timestamp presentTs);

	public List<Object[]> listAllInCompleteProfileEmailIds(Timestamp presentTs);

	public List<Object[]> listAllInCompleteProfileEmailIdsOfWeek(Timestamp presentTs);

	public List<Object[]> listAllInActiveProfileEmailIdsOfWeek(Timestamp presentTs, Integer weekCount);

	public List<Object[]> updateAllInActiveProfileEmailIds(Timestamp presentTs, Integer weekCount);

	public List<Object[]> listAllNotOnBoardedPartnersEmailIds(Timestamp presentTs, Integer actionType,
			Integer noOfDays);




}
