package com.xtremand.custom.link.dao;

import java.util.List;
import java.util.Map;

import com.custom.link.dto.CustomLinkRequestDTO;
import com.custom.link.dto.CustomLinkResponseDTO;
import com.xtremand.common.bom.Pagination;

public interface CustomLinkDao {

	List<String> findAllTitlesAndExcludeTitleById(CustomLinkRequestDTO customLinkRequestDTO);

	List<String> findAllTitles(CustomLinkRequestDTO customLinkRequestDTO);

	Map<String, Object> findAll(Pagination pagination, String search, List<String> types);

	CustomLinkResponseDTO findById(Integer id);

	List<Integer> findIdsByCompanyId(Integer companyId);

	void delete(Integer id);

	boolean isMaximumDashboardBannersUploaded(Integer companyId);

	boolean isDefaultDashboardBannerExists(Integer companyId);

	boolean isDefaultHelpBannerExists(Integer companyId);

	public List<Integer> findDashboardBannerIdsByCompanyId(Integer companyId);

}
