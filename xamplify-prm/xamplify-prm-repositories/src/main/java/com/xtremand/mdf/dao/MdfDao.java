package com.xtremand.mdf.dao;

import java.util.List;
import java.util.Map;

import com.xtremand.common.bom.Pagination;
import com.xtremand.formbeans.UserDTO;
import com.xtremand.mdf.bom.MdfDetails;
import com.xtremand.mdf.bom.MdfRequest;
import com.xtremand.mdf.bom.MdfRequestHistory;
import com.xtremand.mdf.dto.MdfAmountTilesDTO;
import com.xtremand.mdf.dto.MdfDetailsTimeLineDTO;
import com.xtremand.mdf.dto.MdfParnterDTO;
import com.xtremand.mdf.dto.MdfRequestCommentDTO;
import com.xtremand.mdf.dto.MdfRequestTilesDTO;
import com.xtremand.mdf.dto.MdfRequestTimeLineDTO;
import com.xtremand.mdf.dto.MdfUserDTO;
import com.xtremand.mdf.dto.VendorMdfAmountTilesDTO;
import com.xtremand.mdf.dto.VendorMdfRequestTilesDTO;

public interface MdfDao {
	
	void save(Object clazz);

	MdfDetails getMdfDetailsByPartnershipId(Integer partnershipId);
	
	VendorMdfAmountTilesDTO getVendorMdfAmountTilesInfo(Integer vendorCompanyId, Integer loggedInUserId, boolean applyFilter);
	
	Map<String,Object> listPartners(Pagination pagination);

	MdfAmountTilesDTO getPartnerMdfAmountTilesInfo(Integer vendorCompanyId,Integer partnerCompanyId);
	
	MdfParnterDTO getPartnerAndMdfAmountDetails(Integer partnershipId);
	
	MdfRequestTilesDTO getMdfRequestsPartnerTilesForXamplifyLogin(Integer partnerCompanyId);
	
	MdfRequestTilesDTO getMdfRequestsPartnerTilesForVanityLogin(Integer partnerCompanyId,Integer vendorCompanyId);
	
	Map<String, Object> listVendorsAndRequestsCountByPartnerCompanyId(Pagination pagination);
	
	void save(MdfRequest mdfRequest);
	
	void save(MdfRequestHistory mdfRequestHistory);

	VendorMdfRequestTilesDTO getMdfRequestTilesInfoForVendors(Integer vendorCompanyId, Integer loggedInUserId, boolean addFilter);
	
	MdfRequest getMdfRequestById(Integer id);
	
	List<String> listTitleAndEventDateAndRequestAmountByRequestId(Integer requestId);
	
	public boolean validateRequestId(Integer requestId,Integer loggedInUserCompanyId);
	
	public MdfUserDTO getMdfVendorDetails(Integer vendorCompanyId);
	
	public List<Object[]> getMdfOwnerNameAndContactCompany(Integer vendorCompanyId,List<Integer> userIds);
	
	public MdfUserDTO getMdfRequestOwnerDetails(Integer userId);
	
	public MdfUserDTO getPartnerManagerDetails(Integer companyId);
	
	public List<Object[]> getVendorCompanyIdAndPartnerCompanyIdByRequestId(Integer requestId);
	
	public Double getSumOfAllocationAmountByRequestId(Integer requestId);
	
	public Double getSumOfReimbursementAmountByRequestId(Integer requestId);
	
	public boolean validateMdfDetailsId(Integer mdfDetailsId,Integer loggedInUserCompanyId);
	
	MdfParnterDTO getPartnerAndMdfAmountDetailsByMdfDetailsId(Integer mdfDetailsId);
	
	List<MdfDetailsTimeLineDTO> listMdfDetailsTimeLineHistory(Integer mdfDetailsId);
	
	List<MdfRequestTimeLineDTO> listMdfRequestTimeLineHistory(Integer requestId);
	
	Map<String,Object> listRequestDocuments(Pagination pagination);

	String getMdfDocumentAwsFilePathByAlias(String alias);
	
	List<MdfRequestCommentDTO> listMdfRequestComments(Integer requestId);
	
	List<UserDTO> listPartnerDetailsFromUserList(Integer partnershipId,Integer userId);

	String getPartnerCompanyNameByVendorCompanyId(Integer vendorCompanyId,Integer requestCreatedBy);

	String getPartnerDisplayNameForMdfRequest(Integer partnershipId, Integer requestCreatedBy);
	
	String getPartnerDisplayNameForMdfEmailNotification(Integer partnershipId, Integer requestCreatedBy);
	
	List<String> listAllRequestTitlesByPartnershipId(Integer partnershipId);
	
	Double getSumOfAllocationAmountByPartnershipId(Integer partnershipId);
	
	List<String> getUserEmailIdsForDownloadingDocuments(String alias);




	

}
