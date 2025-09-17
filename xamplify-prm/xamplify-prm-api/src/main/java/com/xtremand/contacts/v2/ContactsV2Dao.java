package com.xtremand.contacts.v2;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.xtremand.common.bom.Pagination;
import com.xtremand.contacts.dto.ContactsRequestDTO;
import com.xtremand.formbeans.UserDTO;
import com.xtremand.user.bom.UserList;
import com.xtremand.user.bom.UserUserList;
import com.xtremand.user.list.dto.ProcessingUserListsDTO;

public interface ContactsV2Dao {

	Integer getDefaultMasterContactListIdByCompanyId(Integer companyId);

	void save(ContactsRequestDTO contactsRequestDTO, UserList userList, UserList defaultMasterContactList,
			Set<Integer> allUserListIds);

	void updateUserListStatusAsCompleted(List<Integer> allUserListIdsArrayList);

	Integer getContactCompanyIdByNameAndCompanyId(String contactCompanyName, Integer companyId);

	ProcessingUserListsDTO getUserListNameById(Integer userListId);

	void updateContactList(Integer userListId, Integer loggedInUserId, String csvFilePath);

	UserDTO getEmailIdAndEmailValidationIndStatusByEmailId(String emailId);

	void deleteCompanyUserUserList(Integer id);

	Integer findCompanyUserUserListId(Integer associatedCompanyId);

	UserUserList findCompanyUserUserList(Integer userId, Integer vendorCompanyId);

	UserUserList findUserUserListsByUserIdAndUserList(Integer loggedInUserId, Integer userListId);

	void updateUserUserListFields(String contactCompanyName, Integer contactCompanyId, Integer vendorCompanyId,
			Integer userId);

	Map<String, Object> findUserListContacts(Pagination pagination);

	Integer findUserListContactsCount(Integer companyId, Integer userListId, String contactType);

}
