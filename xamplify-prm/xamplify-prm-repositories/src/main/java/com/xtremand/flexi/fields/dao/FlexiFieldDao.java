package com.xtremand.flexi.fields.dao;

import java.util.List;
import java.util.Map;

import com.xtremand.common.bom.Pagination;
import com.xtremand.flexi.fields.bom.FlexiField;
import com.xtremand.flexi.fields.dto.FlexiFieldRequestDTO;
import com.xtremand.flexi.fields.dto.FlexiFieldResponseDTO;
import com.xtremand.lead.dto.PipelineStageDto;

public interface FlexiFieldDao {

	List<FlexiFieldResponseDTO> findAll(Integer loggedInUserId);

	List<String> findAllFlexiFieldNamesByCompanyIdAndExcludeFieldNameById(Integer companyId, Integer id);

	List<String> findAllFlexiFieldNamesByCompanyId(Integer companyId);

	FlexiField getById(Integer id);

	List<Integer> findIdsByCompanyId(Integer companyId);

	void delete(Integer id);

	void update(FlexiFieldRequestDTO customFieldRequestDTO);

	Map<String, Object> findPaginatedFlexiFields(Pagination pagination, String search);

	List<FlexiFieldRequestDTO> findFlexiFieldsBySelectedUserIdAndUserListId(Integer contactListId, Integer selectedUserId);

	List<PipelineStageDto> findContactStatusStages(Integer companyId);

	void deleteContactStatusStage(Integer id);

	boolean isValidContactStatusStageId(Integer id);

	Integer findContactStatusIdByUserIdAndUserListId(Integer userId, Integer userListId);

}
