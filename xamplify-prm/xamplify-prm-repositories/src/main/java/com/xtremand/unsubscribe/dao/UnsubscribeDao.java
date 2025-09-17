package com.xtremand.unsubscribe.dao;

import java.util.List;
import java.util.Map;

import com.xtremand.common.bom.Pagination;
import com.xtremand.unsubscribe.bom.UnsubscribePageDetails;
import com.xtremand.unsubscribe.bom.UnsubscribeReason;

public interface UnsubscribeDao {

	Map<String, Object> findAll(Pagination pagination);

	UnsubscribeReason findById(Integer id);

	void delete(Integer id);

	void saveAll(List<?> list);

	void save(UnsubscribeReason unsubscribeReason);

	List<UnsubscribeReason> findAll(Integer companyId);

	UnsubscribePageDetails findUnsubscribePageDetailsByCompanyId(Integer companyId);

	List<Integer> findUnsubscribeUserIdsByCompanyId(Integer companyId);

}
