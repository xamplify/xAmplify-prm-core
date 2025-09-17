package com.xtremand.flexi.fields.service;

import java.util.List;

import org.springframework.validation.BindingResult;

import com.xtremand.flexi.fields.dto.FlexiFieldRequestDTO;
import com.xtremand.flexi.fields.dto.FlexiFieldResponseDTO;
import com.xtremand.formbeans.XtremandResponse;
import com.xtremand.lead.dto.PipelineStageDto;
import com.xtremand.util.dto.Pageable;

public interface FlexiFieldService {

	List<FlexiFieldResponseDTO> getAllFlexiFields(Integer loggedInUserId);

	XtremandResponse save(FlexiFieldRequestDTO customFieldRequestDTO, BindingResult result);

	FlexiFieldResponseDTO getById(Integer id, Integer loggedInUserId);

	XtremandResponse delete(Integer id, Integer loggedInUserId);

	XtremandResponse update(FlexiFieldRequestDTO customFieldRequestDTO, BindingResult result);

	XtremandResponse findPaginatedCustomFields(Pageable pageable,Integer loggedInUserId, BindingResult result);

	XtremandResponse findContactStatusStages(Integer loggedInUserId);

	XtremandResponse saveOrUpdate(List<PipelineStageDto> pipelineStageDto, Integer loggedInUserId);

	XtremandResponse deleteContactStatusStage(Integer id, Integer loggedInUserId);

}
