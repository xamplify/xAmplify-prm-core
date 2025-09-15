package com.xtremand.company.dao;

import java.util.List;
import java.util.Map;

import com.xtremand.common.bom.Pagination;
import com.xtremand.company.bom.Company;
import com.xtremand.company.dto.CompanyDTO;
import com.xtremand.util.dto.UserUserListDTO;

public interface CompanyDAO {

	List<CompanyDTO> getCompaniesForDropdown(Integer companyId);

	Company getCompanyByName(String name, Integer companyId);

	Map<String, Object> getCompanies(Pagination pagination);

	Object getCounts(Integer companyId);

	List<UserUserListDTO> getAllCompanyContactsForMigration(Integer companyId);

	void updateCompanyNamesForExistingContacts(Integer companyId);
	
	void updateSyncStatus(Integer companyId,Boolean isSync);
	
	public boolean isMergeCompaniesInProgress(Integer companyId);
	
	public void updateMergeCompaniesInProgress(Integer companyId, Boolean mergeCompaniesInProgress);
	
	public List<String> getDuplicateCompanies(Integer companyId);

	public List<Company> getAllCompaniesByName(String name, Integer companyId);
	
	public void mergeCompanyContacts(Integer parentCompanyId, Integer parentCompanyContactListId,
			String parentCompanyName, List<Integer> mergingCompanyIds, Integer loggedInCompanyId);
	
	public void updateCompanyOnAllContacts(Integer parentCompanyId, String parentCompanyName,
			List<Integer> mergingCompanyIds);
	
	public void deleteCompanyContactLists(List<Integer> companyIds);
	
	public void deleteCompanies(List<Integer> companyIds);

	void updateSyncCompaniesInProgress(Integer companyId, boolean isSyncCompaniesInProgress);

	boolean isSyncCompaniesInProgress(Integer companyId);
	
	public List<UserUserListDTO> getUserUserListsToHandleCompany(Integer userListId);

	public void updateCompanyIdOnAllContacts(Integer userId, String companyName, Integer companyId, Integer loggedInCompanyId);

	public void deleteOtherCompanyContacts(Integer loggedInCompanyId);
	
	public String fetchWebsiteUsingCompanyId(Integer companyId);

}
