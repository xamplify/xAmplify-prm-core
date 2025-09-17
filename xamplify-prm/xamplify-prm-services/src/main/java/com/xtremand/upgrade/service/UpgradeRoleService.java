package com.xtremand.upgrade.service;

import com.xtremand.common.bom.Pagination;
import com.xtremand.formbeans.XtremandResponse;
import com.xtremand.util.dto.UpgradeRoleEmailNotification;

public interface UpgradeRoleService {

	XtremandResponse saveRequest(Integer userId);
	
	XtremandResponse isRequestExists(Integer userId);

	XtremandResponse findAll(Pagination pagination);
	
	XtremandResponse upgradeToMarketing(Integer requestId);

	void sendEmailNotificationToSuperAdmin(Integer userId);
	
	void sendUpgradeSuccessEmailNotification(Integer userId);
	
	void sendUpgradeSuccessEmailNotification(UpgradeRoleEmailNotification upgradeRoleEmailNotification);


}
