package com.xtremand.mail.service;

public class StatusCodeConstants {
	public static final int USERCREATED = 1001;
	public static final int DUPLICATE_USERNAME = 1002;
	public static final int DUPLICATE_EMAIL = 1003;
	public static final int NO_MOB_FOUND = 1004;
	public static final int LISTMOB_FOUND = 1005;
	public static final int NO_MOB_FOUND_ID = 1006;
	public static final int MOB_FOUND_ID = 1007;
	public static final int USERLIST_CREATED = 1008;
	public static final int CONTACTLIST_FOUND = 1009;
	public static final int NO_CONTACTLIST_FOUND = 1010;
	public static final int CONTACTLIST_REMOVED = 1011;
	public static final int FORGOTPWD_SUCCESS = 1012;
	public static final int FORGOT_EMAIL_NOTFOUND = 1013;
	public static final int USERS_FOUND_USERLIST = 1014;
	public static final int DUPL_USERLIST = 1015;
	public static final int INVALIDEMAIL = 1016;
	public static final int DUPLUSERLIST_INVALIDEMAIL = 1017;
	public static final int UPLOAD_VIDEO = 1018;
	public static final int PROCESS_VIDEO = 1019;
	public static final int SAVE_VIDEO = 1020;
	public static final int EDIT_VIDEO = 1021;
	public static final int ORG_ADMIN_CREATED = 1022;
	public static final int ORG_ADMIN_APPROVED = 1023;
	public static final int ORG_ADMIN_DELETED = 1024;
	public static final int USER_VERIFIED = 1025;
	public static final int USERLIST_UPDATED = 1026;
	public static final int PASSWORD_MISMATCH = 1027;
	public static final int PASSWORD_UPDATED = 1028;
	public static final int USER_UPDATED = 1029;
	public static final int USER_IMAGE_UPLOADED = 1030;
	public static final int USER_FOUND = 1031;
	public static final int PASSWORD_MATCHED = 1032;
	public static final int WRONG_PASSWORD = 1033;
	public static final int CONTACTS_FOUND_CRM = 1034;
	public static final int LISTVIEWS_FOUND = 1035;
	public static final int USER_REDITECTED = 1036;
	public static final int AUTHENTICATION_SUCCESS = 1037;
	public static final int DOWNLOAD_USERLIST_SUCCESS = 1038;
	public static final int SYNCHRONIZATION_SUCCESS = 1039;
	public static final int MOBINAR_DELETED = 1040;
	public static final int NO_CONTACTS_FOUND = 1041;
	public static final int All_CONTACTS_FOUND = 1042;
	public static final int INVALID_USERS_DELETED = 1043;
	public static final int USERLIST_CAMPAIGNS_ERR=1044;
	public static final int UPLOAD_OWN_THUMBNAIL_IMAGE = 1045;
	public static final int FORGOT_EMAIL_SIGNUP = 1046;
	public static final int FORGOT_EMAIL_ACTIVATION = 1047;
	public static final int DISABLED_ORG_ADMIN_SUCCESSFULLY = 1048;
	public static final int INVALID_VENDOR_EMAIL_ID = 1049;
	
	
	/*******Campaign***************/
	public static final int CAMPAIGN_CREATED_SUCCESSFULLY = 2000;
	public static final int CAMPAIGN_LIST_FOUND = 2002;
	public static final int CAMPAIGN_FOUND = 2003;
	public static final int CAMPAIGN_DELETED = 2004;
	public static final int CAMPAIGN_LIST_NAMES_FOUND = 2005;
	public static final int INVALID_CAMPAIGN_SCHEDULE_TIME = 2015;
	public static final int INVALID_AUTO_REPLY_SEND_TIME = 2016;
	public static final int TEST_EMAIL_SENT_SUCCESSFULLY = 2017;
	public static final int INVALID_COUTNRY = 2018;
	public static final int PARENT_CAMPAIGN_DELETED = 2019;
	public static final int SHARE_LEADS_LIST_ASSIGNED = 2020;
	public static final int CAMPAIGN_MAX_RECIPIENT_COUNT_REACHED = 2021;
	
	/*******Email Template*************/
	public static final int EMAIL_TEMPLATES_FOUND = 2006;
	public static final int EMAIL_TEMPLATE_SAVED = 2007;
	public static final int EMAIL_TEMPLATE_FOUND = 2008;
	public static final int EMAIL_TEMPLATE_UPDATED = 2009;
	public static final int EMAIL_TEMPLATE_DELETED = 2010;
	public static final int DUPLICATE_TEMPLATE_NAME_FOUND = 2011;
	public static final int EMAIL_TEMPLATE_NAMES_FOUND = 2012;
	public static final int CAMPAIGN_EMAIL_NOTIFICATIONS_LIST = 2013;
	public static final int CAMPAIGN_VIDEO_NOTIFICATIONS_LIST = 2014;
	/*************Team Members****************/
	public static final int TEAM_MEMBER_SAVED = 3000;
	public static final int TEAM_MEMBER_FOUND = 30001;
	public static final int TEAM_MEMBER_UPDATED = 3002;
	public static final int TEAM_MEMBER_APPROVED = 3003;
	public static final int TEAM_MEMBER_DECLINED = 3004;
	public static final int TEAM_MEMBER_DELETED = 3005;
	public static final int TEAM_MEMBERS_LIST_FOUND = 3006;
	public static final int ORG_ADMINS_EMAIL_IDS_LIST = 3007;
	public static final int MORE_THAN_TWO_ORG_ADMINS_FOUND = 3008;
	public static final int ORG_ADMINS_FOUND_FOR_ORGANIZATION = 3009;
	public static final int INVALID_TEAM_MEMBER_GROUP = 3010;

	/******Company Profile*************/
	public static final int COMPANY_PROFILE_FOUND = 4000;
	public static final int COMPANY_PROFILE_UPDATED = 4001;
	public static final int COMPANY_PROFILES_FOUND = 4002;
	public static final int DUPLICATE_COMPANY_NAME_FOUND = 4003;
	public static final int DUPLICATE_COMPANY_PROFILE_NAME_FOUND = 4004;
	public static final int COMPANY_NAMES_FOUND = 4005;
	public static final int COMPANY_NAME_PROFILES_FOUND = 4006;
	
	/******Deal Registration**********/
	public static final int DEAL_ADDED = 7000;
	public static final int DEAL_UPDATED = 7001;
	
	/******Marketo Integration**********/
	public static final int MARKETO_CREDENTIALS_FOUND = 8000;
	public static final int MARKETO_CREDENTIALS_NOT_FOUND = 8001;
	public static final int MARKETO_CREDENTIALS_INVALID = 8002;
	public static final int MARKETO_CREDENTIALS_SAVED = 8003;
	public static final int MARKETO_EMAIL_TEMPLATES_FOUND = 8004;
	public static final int MARKETO_EMAIL_TEMPLATES_NOT_FOUND = 8005;
	public static final int MARKETO_EMAIL_TEMPLATE_FOUND = 8006;
	public static final int MARKETO_EMAIL_TEMPLATE_NOT_FOUND = 8007;
	public static final int MARKETO_IMPORT_SUCCESS = 8008;
	public static final int MARKETO_INPUT_INVALID = 8009;
	public static final int MARKETO_IMPORT_FAIL = 8010;
	public static final int EXTERNAL_IMPORT_COMPLETE = 8011;
	public static final int MARKETO_EMAIL_TEMPLATE_SAVED = 8012;
	public static final int MARKETO_EMAIL_TEMPLATE_UPDATED = 8013;
	public static final int MARKETO_CONTACTS_FOUND = 8014;
	public static final int MARKETO_CONTACTS_NOT_FOUND = 8015;	
	public static final int MARKETO_PUSH_LEAD_SUCCESS = 8016;
	public static final int MARKETO_CUSTOM_OBJECTS_NOT_FOUND = 8019;
	public static final int MARKETO_CUSTOM_OBJECTS_FOUND = 8020;
	
	public static final int CONTACTLIST_NAME_DUPLICATE = 8017;
	public static final int CONTACTS_SAVE_SUCCESS = 8018;
	
	/******RSS Integration**********/
	public static final int RSS_COLLECTIONS_FOUND = 8100;
	public static final int RSS_COLLECTIONS_NOT_FOUND = 8101;
	public static final int RSS_FEEDS_FOUND = 8102;
	public static final int RSS_FEEDS_NOT_FOUND = 8103;
	public static final int RSS_SEARCH_DATA_FOUND = 8104;	
	public static final int RSS_SOURCES_FOUND = 8105;
	public static final int RSS_SOURCES_NOT_FOUND = 8106;
	public static final int RSS_ADD_TO_COLLECTION_SUCCESS = 8107;
	public static final int RSS_ADD_TO_COLLECTION_FAIL = 8108;
	public static final int RSS_DELETE_FROM_COLLECTION_SUCCESS = 8109;
	public static final int RSS_DELETE_FROM_COLLECTION_FAIL = 8110;
	public static final int RSS_COLLECTION_ADDED = 8111;
	public static final int RSS_COLLECTION_NOT_ADDED = 8112;
	public static final int RSS_DUPLICATE_COLLECTION_NAME = 8113;
	public static final int RSS_RENAME_SUCCESS = 8114;
	public static final int RSS_RENAME_FAIL = 8115;
	public static final int RSS_RENAME_AUTHORIZATION_FAIL = 8116;
	public static final int RSS_DELETE_SUCCESS = 8117;
	public static final int RSS_DELETE_FAIL = 8118;
	
	
	
}


