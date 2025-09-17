package com.xtremand.util.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class ModulesEmailNotification {


	private List<String> addedTeamMemberGroupsNotifications = new ArrayList<>();

	private List<String> addedTeamMemberEmailIdsNotifications = new ArrayList<>();

	private List<String> removedTeamMemberGroupsNotifications = new ArrayList<>();

	private List<String> removedTeamMemberEmailIdsNotifications = new ArrayList<>();

	public boolean sendNotification() {
		return !addedTeamMemberEmailIdsNotifications.isEmpty() || !addedTeamMemberGroupsNotifications.isEmpty()
				|| !removedTeamMemberEmailIdsNotifications.isEmpty() || !removedTeamMemberGroupsNotifications.isEmpty();
	}

	


}
