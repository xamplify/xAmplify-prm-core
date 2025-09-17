package com.xtremand.util.dto;

import java.io.Serializable;

import com.xtremand.user.bom.Role;

import lombok.Data;

@Data
public final class ModuleIdList implements Serializable {
	/**
	* 
	*/
	private static final long serialVersionUID = -8046153886513500274L;

	public static final Integer DAM_MODULE = Role.DAM.getRoleId();

	public static final Integer MDF_MODULE = Role.MDF.getRoleId();

	public static final Integer LEARNING_TRACK = Role.LEARNING_TRACK.getRoleId();

	public static final Integer PLAY_BOOK = Role.PLAY_BOOK.getRoleId();

	public static final Integer FORM = Role.FORM_ROLE.getRoleId();

	public static final Integer PARTNERS = Role.PARTNERS.getRoleId();

	public static final Integer SHARE_LEADS = Role.SHARE_LEADS.getRoleId();

	public static final Integer LEADS = 111;

	public static final Integer DEALS = 112;

	public static final Integer TEAM_MEMBER = 113;

	public static final Integer COMPANIES = 114;
	
	public static final Integer APPROVAL_HUB = 33;

}
