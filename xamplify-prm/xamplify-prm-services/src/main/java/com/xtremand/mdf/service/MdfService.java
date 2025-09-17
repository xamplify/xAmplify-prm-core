package com.xtremand.mdf.service;

import org.springframework.web.multipart.MultipartFile;

import com.xtremand.common.bom.Pagination;
import com.xtremand.form.dto.FormDTO;
import com.xtremand.formbeans.XtremandResponse;
import com.xtremand.mdf.bom.MdfDetails;
import com.xtremand.mdf.dto.MdfDetailsDTO;
import com.xtremand.mdf.dto.MdfRequestCommentDTO;
import com.xtremand.mdf.dto.MdfRequestPostDTO;
import com.xtremand.mdf.dto.MdfRequestUploadDTO;
import com.xtremand.mdf.dto.MdfRequestViewDTO;
import com.xtremand.vanity.url.dto.VanityUrlDetailsDTO;

public interface MdfService {

	public void save(MdfDetails marketDevelopementFundsCredit);

	public XtremandResponse getVendorMdfAmountTilesInfo(Integer vendorCompanyId, Integer loggedInUserId,boolean applyFilter);

	public XtremandResponse listPartners(Pagination pagination);

	public XtremandResponse updateMdfAmount(MdfDetailsDTO mdfDetailsDTO);

	public XtremandResponse getMdfRequestsPartnerTiles(VanityUrlDetailsDTO vanityUrlDetailsDto);
	
	public XtremandResponse getPartnerMdfAmountTilesInfo(Integer vendorCompanyId,Integer partnerCompanyId);

	public XtremandResponse listVendorsAndRequestsCountByPartnerCompanyId(Pagination pagination);

	public XtremandResponse getMdfRequestForm(Integer vendorCompanyId, boolean createRequest);

	public XtremandResponse createMdfForm(FormDTO formDto);

	public XtremandResponse updateMdfForm(FormDTO formDto);

	public XtremandResponse saveMdfRequest(MdfRequestPostDTO mdfRequestPostDTO);

	public XtremandResponse listMdfFormDetails(Pagination pagination);

	public XtremandResponse getMdfRequestTilesInfoForVendors(Integer vendorCompanyId, Integer loggedInUserId, boolean teamMemberFilter);

	public XtremandResponse getRequestDetailsById(Integer id,Integer loggedInUserCompanyId);

	public XtremandResponse updateMdfRequest(MdfRequestViewDTO mdfRequestViewDTO);

	public XtremandResponse getRequestDetailsAndTimeLineHistory(Integer id, Integer loggedInUserCompanyId);

	public XtremandResponse getPartnerAndMdfAmountDetails(Integer partnershipId);

	public XtremandResponse getMdfDetailsTimeLineHistory(Integer id, Integer loggedInUserCompanyId);
	
	public XtremandResponse getMdfRequestTimeLineHistory(Integer id, Integer loggedInUserCompanyId);

	public XtremandResponse uploadRequestDocuments(MultipartFile file,MdfRequestUploadDTO mdfRequestUploadDTO);

	public XtremandResponse listRequestDocuments(Pagination pagination);

	public String getMdfDocumentAwsFilePathByAlias(String alias);

	public XtremandResponse saveComment(MdfRequestCommentDTO mdfRequestCommentDTO);
	
	public XtremandResponse listComments(Integer requestId);


}
