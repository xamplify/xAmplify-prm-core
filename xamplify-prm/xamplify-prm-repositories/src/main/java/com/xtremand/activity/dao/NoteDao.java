package com.xtremand.activity.dao;

import java.util.List;
import java.util.Map;

import com.xtremand.activity.dto.NoteDTO;
import com.xtremand.activity.dto.NoteResponseDTO;
import com.xtremand.common.bom.Pagination;

public interface NoteDao {

	NoteResponseDTO getNoteById(Integer id);

	List<NoteResponseDTO> getNotesDataByUserId(Integer userId);

	void update(Integer id, NoteDTO noteDTO);

	void delete(Integer loggedInUserId, Integer id);

	Map<String, Object> getPaginatedNotes(Pagination pagination, String searchKey);
	
	List<NoteDTO> fetchNotesForConatctAgent(String dynamicQueryCondition, Integer companyId);

    List<NoteDTO> fetchNotesForConatctAgentOnContact(Integer companyId, Integer contactId);
	
}
