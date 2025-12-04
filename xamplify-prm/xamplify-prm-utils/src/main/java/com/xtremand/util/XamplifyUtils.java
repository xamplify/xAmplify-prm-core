package com.xtremand.util;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.ArrayUtils;
import org.hibernate.SQLQuery;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.xtremand.campaign.exception.XamplifyDataAccessException;
import com.xtremand.common.bom.CompanyProfile;
import com.xtremand.common.bom.Pagination;
import com.xtremand.custom.field.dto.ObjectType;
import com.xtremand.flexi.fields.dto.FlexiFieldResponseDTO;
import com.xtremand.form.bom.FormTypeEnum;
import com.xtremand.formbeans.UserDTO;
import com.xtremand.formbeans.XtremandResponse;
import com.xtremand.landing.page.analytics.bom.GeoLocationAnalyticsEnum;
import com.xtremand.log.bom.URLShortener;
import com.xtremand.mail.exception.MailException;
import com.xtremand.social.formbeans.MyMergeTagsInfo;
import com.xtremand.user.bom.Role;
import com.xtremand.user.bom.TeamMember;
import com.xtremand.user.bom.User;
import com.xtremand.user.bom.UserList;
import com.xtremand.user.bom.UserList.SocialNetwork;
import com.xtremand.user.bom.UserList.TYPE;
import com.xtremand.util.dto.AdminAndTeamMemberDetailsDTO;
import com.xtremand.util.dto.PaginatedDTO;
import com.xtremand.util.dto.XamplifyConstants;

import au.com.bytecode.opencsv.CSVWriter;

public class XamplifyUtils {

	private static final int BUFFER_SIZE = 4096;

	static final String ENUM_TYPES = "'" + GeoLocationAnalyticsEnum.LANDING_PAGE + "'" + "," + "'"
			+ GeoLocationAnalyticsEnum.CAMPAIGN_LANDING_PAGE + "'" + "," + "'"
			+ GeoLocationAnalyticsEnum.PARTNER_LANDING_PAGE + "'";

	private static final String ABOUT_US = "<<partnerAboutUs>>";

	private static final String CUSTOMER_FULL_NAME = "_CUSTOMER_FULL_NAME";

	private static final String REPLACE_THERE = "There";

	private static final String ADDRESS = "<<address>>";

	private static final String ZIPCODE = "<<zipcode>>";

	private static final String CITY = "<<city>>";

	private static final String STATE = "<<state>>";

	private static final String COUNTRY = "<<country>>";

	private static final String MOBILE_NUMBER = "<<mobileNumber>>";

	private static final String PARTNER_NAME = "<<PARTNER_NAME>>";

	private static final String VENDOR_COMPANY_NAME = "<<VENDOR_COMPANY_NAME>>";

	private static final String senderFullName = "<<senderFullName>>";

	private static final String FIRST_NAME_MERGE_TAG = "{{firstName}}";

	private static final String LAST_NAME_MERGE_TAG = "{{lastName}}";

	private static final String FULL_NAME_MERGE_TAG = "{{fullName}}";

	private static final String EMAIL_ID_MERGE_TAG = "{{emailId}}";

	private static final String COMPANY_NAME_MERGE_TAG = "{{companyName}}";

	private static final String ADDRESS_MERGE_TAG = "{{address}}";

	private static final String ZIPCODE_MERGE_TAG = "{{zipcode}}";

	private static final String CITY_MERGE_TAG = "{{city}}";

	private static final String STATE_MERGE_TAG = "{{state}}";

	private static final String COUNTRY_MERGE_TAG = "{{country}}";

	private static final String MOBILE_NUMBER_MERGE_TAG = "{{mobileNumber}}";

	private static final String ABOUT_US_MERGE_TAG = "{{partnerAboutUs}}";

	public static final SimpleDateFormat LAUNCH_TIME_FORMAT = new SimpleDateFormat("MM/dd/yyyy hh:mm a");

	public static final String TOTAL_RECORDS = "totalRecords";

	public static final String ORDER_BY = XamplifyConstants.ORDER_BY;

	public static final String CONTACTS = "contacts";

	private XamplifyUtils() {

	}

	public static String setDisplayName(User user) {
		if (user != null) {
			String firstName = user.getFirstName();
			String lastName = user.getLastName();
			if (StringUtils.hasText(firstName) || StringUtils.hasText(lastName)) {
				if (firstName == null) {
					firstName = "";
				}
				if (lastName == null) {
					lastName = "";
				}
				return firstName + " " + lastName;
			} else {
				return user.getEmailId();
			}
		} else {
			return "";
		}

	}

	public static String setDisplayName(String firstName, String lastName, String emailId) {
		if (StringUtils.hasText(firstName) || StringUtils.hasText(lastName)) {
			if (firstName == null) {
				firstName = "";
			}
			if (lastName == null) {
				lastName = "";
			}
			return firstName + " " + lastName;
		} else {
			return emailId;
		}

	}

	public static String setCompanyName(CompanyProfile companyProfile) {
		if (companyProfile != null) {
			return companyProfile.getCompanyName();
		} else {
			return "";
		}
	}

	public static BufferedImage toBufferedImg(MultipartFile img) throws IOException {

		BufferedImage in = ImageIO.read(img.getInputStream());

		BufferedImage newImage = new BufferedImage(in.getWidth(), in.getHeight(), BufferedImage.TYPE_3BYTE_BGR);

		Graphics2D g = newImage.createGraphics();
		g.drawImage(in, 0, 0, null);
		g.dispose();
		return newImage;
	}

	public static Map<String, Object> unzip(String zipFilePath, String destinationDirectory) throws IOException {
		Map<String, Object> map = new HashMap<>();
		List<String> imagesPath = new ArrayList<>();
		File destDir = new File(destinationDirectory);
		if (!destDir.exists()) {
			destDir.mkdir();
		}
		ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFilePath));
		ZipEntry entry = zipIn.getNextEntry();
		boolean isHtmlFound = false;
		// iterates over entries in the zip file
		// !("__MACOSX/".equals(entry.getName()))
		while (entry != null) {
			String filePath = destinationDirectory + "/" + entry.getName();
			String updateFilePath = filePath.substring(filePath.lastIndexOf("/email-templates") + 1);
			if (entry.getName().endsWith("html")) {
				if (isHtmlFound) {
					map.put("multipleHtmlsFound", true);
				} else {
					map.put("htmlFilePath", filePath);
					map.put("multipleHtmlsFound", false);
					isHtmlFound = true;
				}
			}
			boolean isJpg = entry.getName().endsWith("JPG") || entry.getName().endsWith("jpg")
					|| entry.getName().endsWith("gif");
			boolean isPng = entry.getName().endsWith("png") || entry.getName().endsWith("PNG");
			boolean isJpeg = entry.getName().endsWith("JPEG") || entry.getName().endsWith("jpeg")
					|| entry.getName().endsWith("pjpeg");
			if (isJpg || isPng || isJpeg) {
				imagesPath.add(updateFilePath);
			}
			if (!entry.isDirectory()) {
				// if the entry is a file, extracts it
				extractFile(zipIn, filePath);
			} else {
				// if the entry is a directory, make the directory
				File dir = new File(filePath);
				dir.mkdir();
			}
			zipIn.closeEntry();
			entry = zipIn.getNextEntry();
		}
		zipIn.close();
		map.put("imagesPath", imagesPath);
		return map;
	}

	private static void extractFile(ZipInputStream zipIn, String filePath) throws IOException {
		BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
		byte[] bytesIn = new byte[BUFFER_SIZE];
		int read = 0;
		while ((read = zipIn.read(bytesIn)) != -1) {
			bos.write(bytesIn, 0, read);
		}
		bos.close();
	}

	public static String replaceNullWithEmptyString(String string) {
		if (string != null) {
			return XamplifyUtils.escapeDollarSequece(string);
		} else {
			return "";
		}
	}

	public static String replaceNullWithThere(String string) {
		if (string != null) {
			return XamplifyUtils.escapeDollarSequece(string);
		} else {
			return REPLACE_THERE;
		}
	}

	public static List<Integer> removeDuplicatesAndNulls(List<Integer> ids) {
		try {
			if (XamplifyUtils.isNotEmptyList(ids)) {
				ids.removeAll(Collections.singleton(null));
				return new ArrayList<>(new HashSet<>(ids));
			} else {
				return Collections.emptyList();
			}
		} catch (Exception e) {
			throw new XamplifyDataAccessException(e.getMessage());
		}
	}

	public static String getFullName(User user) {
		String fullName = "";
		if (user != null) {
			String firstName = replaceNullWithEmptyString(user.getFirstName());
			String lastName = replaceNullWithEmptyString(user.getLastName());
			String middleName = replaceNullWithEmptyString(user.getMiddleName());
			if (StringUtils.hasText(firstName) || StringUtils.hasText(lastName) || StringUtils.hasText(middleName)) {
				fullName = firstName + " " + lastName + " " + middleName;
			} else if (!StringUtils.hasText(firstName) && !StringUtils.hasText(lastName)
					&& !StringUtils.hasText(middleName)) {
				fullName = user.getEmailId();
			}
		} else {
			fullName = "";
		}
		return fullName;
	}

	public static boolean isOnlyUser(User loggedInUser) {
		boolean isOnlyUser = false;
		if (loggedInUser != null) {
			List<Integer> roleIdsList = loggedInUser.getRoles().stream().map(Role::getRoleId)
					.collect(Collectors.toList());
			if (roleIdsList != null && roleIdsList.size() == 1) {
				isOnlyUser = roleIdsList.contains(3);
			}
		}
		return isOnlyUser;
	}

	public static boolean checkTag(String tag1, String tag2, String body) {
		return body.indexOf(tag1.trim()) < 0 && body.indexOf(tag2.trim()) < 0;
	}

	public static boolean checkImageTag(String tag1, String tag2, String tag3, String body) {
		return body.indexOf(tag1.trim()) < 0 && body.indexOf(tag2.trim()) < 0 && body.indexOf(tag3.trim()) < 0;
	}

	public static String replaceReceiverMergeTagsInfo(User user, String updatedBody) {
		updatedBody = replaceMergeTagPrefixer(updatedBody);
		return replaceReceiverMergeTagsWithoutReplacingSpecialCharacter(user, updatedBody);
	}

	public static String replaceReceiverMergeTagsWithoutReplacingSpecialCharacter(User user, String updatedBody) {
		if (user != null) {
			String firstName;
			String lastName;
			String fullName;
			String sir = REPLACE_THERE;
			if (StringUtils.hasText(user.getFirstName())) {
				firstName = user.getFirstName();
			} else {
				firstName = sir;
			}
			if (StringUtils.hasText(user.getLastName())) {
				lastName = user.getLastName();
			} else {
				lastName = sir;
			}
			fullName = firstName + " " + lastName;
			if (firstName.trim().equals(lastName.trim())) {
				fullName = firstName;
			}
			updatedBody = updatedBody.replace("<<firstName>>", XamplifyUtils.escapeDollarSequece(firstName));
			updatedBody = updatedBody.replace("<<lastName>>", XamplifyUtils.escapeDollarSequece(lastName));
			updatedBody = updatedBody.replace("<<fullName>>", XamplifyUtils.escapeDollarSequece(fullName));
			updatedBody = updatedBody.replace("<<emailId>>", XamplifyUtils.escapeDollarSequece(user.getEmailId()));
			updatedBody = updatedBody.replace("<<companyName>>", setCompanyName(user.getContactCompany()));
			updatedBody = updatedBody.replace(ADDRESS, XamplifyUtils.checkEmptyString(user.getAddress()));
			updatedBody = updatedBody.replace(CITY, XamplifyUtils.checkEmptyString(user.getCity()));
			updatedBody = updatedBody.replace(STATE, XamplifyUtils.checkEmptyString(user.getState()));
			updatedBody = updatedBody.replace(ZIPCODE, XamplifyUtils.checkEmptyString(user.getZipCode()));
			updatedBody = updatedBody.replace(COUNTRY, XamplifyUtils.checkEmptyString(user.getCountry()));
			updatedBody = updatedBody.replace(MOBILE_NUMBER, XamplifyUtils.checkEmptyString(user.getMobileNumber()));

			updatedBody = updatedBody.replace(FIRST_NAME_MERGE_TAG, XamplifyUtils.escapeDollarSequece(firstName));
			updatedBody = updatedBody.replace(LAST_NAME_MERGE_TAG, XamplifyUtils.escapeDollarSequece(lastName));
			updatedBody = updatedBody.replace(FULL_NAME_MERGE_TAG, XamplifyUtils.escapeDollarSequece(fullName));
			updatedBody = updatedBody.replace(EMAIL_ID_MERGE_TAG, XamplifyUtils.escapeDollarSequece(user.getEmailId()));
			updatedBody = updatedBody.replace(COMPANY_NAME_MERGE_TAG, setCompanyName(user.getContactCompany()));
			updatedBody = updatedBody.replace(ADDRESS_MERGE_TAG, XamplifyUtils.checkEmptyString(user.getAddress()));
			updatedBody = updatedBody.replace(CITY_MERGE_TAG, XamplifyUtils.checkEmptyString(user.getCity()));
			updatedBody = updatedBody.replace(STATE_MERGE_TAG, XamplifyUtils.checkEmptyString(user.getState()));
			updatedBody = updatedBody.replace(ZIPCODE_MERGE_TAG, XamplifyUtils.checkEmptyString(user.getZipCode()));
			updatedBody = updatedBody.replace(COUNTRY_MERGE_TAG, XamplifyUtils.checkEmptyString(user.getCountry()));
			updatedBody = updatedBody.replace(MOBILE_NUMBER_MERGE_TAG,
					XamplifyUtils.checkEmptyString(user.getMobileNumber()));

		} else {
			updatedBody = updatedBody.replace("<<firstName>>", "");
			updatedBody = updatedBody.replace("<<lastName>>", "");
			updatedBody = updatedBody.replace("<<fullName>>", "");
			updatedBody = updatedBody.replace("<<emailId>>", "");
			updatedBody = updatedBody.replace("<<companyName>>", "");
			updatedBody = updatedBody.replace(ADDRESS, "");
			updatedBody = updatedBody.replace(CITY, "");
			updatedBody = updatedBody.replace(STATE, "");
			updatedBody = updatedBody.replace(ZIPCODE, "");
			updatedBody = updatedBody.replace(COUNTRY, "");
			updatedBody = updatedBody.replace(MOBILE_NUMBER, "");
			updatedBody = updatedBody.replace(FIRST_NAME_MERGE_TAG, "");
			updatedBody = updatedBody.replace(LAST_NAME_MERGE_TAG, "");
			updatedBody = updatedBody.replace(FULL_NAME_MERGE_TAG, "");
			updatedBody = updatedBody.replace(EMAIL_ID_MERGE_TAG, "");
			updatedBody = updatedBody.replace(COMPANY_NAME_MERGE_TAG, "");
			updatedBody = updatedBody.replace(ADDRESS_MERGE_TAG, "");
			updatedBody = updatedBody.replace(CITY_MERGE_TAG, "");
			updatedBody = updatedBody.replace(STATE_MERGE_TAG, "");
			updatedBody = updatedBody.replace(ZIPCODE_MERGE_TAG, "");
			updatedBody = updatedBody.replace(COUNTRY_MERGE_TAG, "");
			updatedBody = updatedBody.replace(MOBILE_NUMBER_MERGE_TAG, "");
		}
		return updatedBody;
	}

	public static String replaceMergeTagPrefixer(String updatedBody) {
		updatedBody = updatedBody.replaceAll("\\{", "<");
		updatedBody = updatedBody.replaceAll("\\}", ">");
		return updatedBody;
	}

	public static String replaceSenderMergeTags(String updatedBody, MyMergeTagsInfo myMergeTagInfo) {
		updatedBody = updatedBody.replace("<<senderFirstName>>",
				XamplifyUtils.escapeDollarSequece(myMergeTagInfo.getMyFirstName()));
		updatedBody = updatedBody.replace("<<VENDOR_FULL_NAME>>",
				XamplifyUtils.escapeDollarSequece(myMergeTagInfo.getMyFullName()));
		updatedBody = updatedBody.replace("{{VENDOR_FULL_NAME}}",
				XamplifyUtils.escapeDollarSequece(myMergeTagInfo.getMyFullName()));
		updatedBody = updatedBody.replace("<<VENDOR_COMPANY_NAME>>",
				XamplifyUtils.escapeDollarSequece(myMergeTagInfo.getSenderCompany()));
		updatedBody = updatedBody.replace("{{VENDOR_COMPANY_NAME}}",
				XamplifyUtils.escapeDollarSequece(myMergeTagInfo.getSenderCompany()));
		updatedBody = updatedBody.replace("{{senderFirstName}}",
				XamplifyUtils.escapeDollarSequece(myMergeTagInfo.getMyFirstName()));
		updatedBody = updatedBody.replace("<<senderMiddleName>>",
				XamplifyUtils.escapeDollarSequece(myMergeTagInfo.getMyMiddleName()));
		updatedBody = updatedBody.replace("{{senderMiddleName}}",
				XamplifyUtils.escapeDollarSequece(myMergeTagInfo.getMyMiddleName()));
		updatedBody = updatedBody.replace("<<senderLastName>>",
				XamplifyUtils.escapeDollarSequece(myMergeTagInfo.getMyLastName()));
		updatedBody = updatedBody.replace("{{senderLastName}}",
				XamplifyUtils.escapeDollarSequece(myMergeTagInfo.getMyLastName()));
		updatedBody = updatedBody.replace("<<senderFullName>>",
				XamplifyUtils.escapeDollarSequece(myMergeTagInfo.getMyFullName()));
		updatedBody = updatedBody.replace("{{senderFullName}}",
				XamplifyUtils.escapeDollarSequece(myMergeTagInfo.getMyFullName()));
		updatedBody = updatedBody.replace("<<senderJobTitle>>",
				XamplifyUtils.escapeDollarSequece(myMergeTagInfo.getSenderJobTitle()));
		updatedBody = updatedBody.replace("{{senderJobTitle}}",
				XamplifyUtils.escapeDollarSequece(myMergeTagInfo.getSenderJobTitle()));
		updatedBody = updatedBody.replace("{{senderTitle}}",
				XamplifyUtils.escapeDollarSequece(myMergeTagInfo.getSenderJobTitle()));
		updatedBody = updatedBody.replace("<<senderEmailId>>",
				XamplifyUtils.escapeDollarSequece(myMergeTagInfo.getMyEmailId()));
		updatedBody = updatedBody.replace("{{senderEmailId}}",
				XamplifyUtils.escapeDollarSequece(myMergeTagInfo.getMyEmailId()));
		updatedBody = updatedBody.replace("<<senderContactNumber>>",
				XamplifyUtils.escapeDollarSequece(myMergeTagInfo.getMyContactNumber()));
		updatedBody = updatedBody.replace("{{senderContactNumber}}",
				XamplifyUtils.escapeDollarSequece(myMergeTagInfo.getMyContactNumber()));
		updatedBody = replacePageMergeTagsByMergeTagInfo(updatedBody, myMergeTagInfo);

		return updatedBody;
	}

	public static String replacePageMergeTagsByMergeTagInfo(String updatedBody, MyMergeTagsInfo myMergeTagInfo) {
		updatedBody = updatedBody.replace("<<senderCompanyUrl>>",
				XamplifyUtils.escapeDollarSequece(myMergeTagInfo.getMyCompanyUrl()));
		updatedBody = updatedBody.replace("{{senderCompanyUrl}}",
				XamplifyUtils.escapeDollarSequece(myMergeTagInfo.getMyCompanyUrl()));
		updatedBody = updatedBody.replace("<<senderCompanyContactNumber>>",
				XamplifyUtils.escapeDollarSequece(myMergeTagInfo.getMyCompanyContactNumber()));
		updatedBody = updatedBody.replace("<<senderCompany>>",
				XamplifyUtils.escapeDollarSequece(myMergeTagInfo.getSenderCompany()));
		updatedBody = updatedBody.replace("<<senderPrivacyPolicy>>", XamplifyUtils
				.escapeDollarSequece(XamplifyUtils.escapeDollarSequece(myMergeTagInfo.getPrivacyPolicy())));
		updatedBody = updatedBody.replace("<<senderAboutUs>>",
				XamplifyUtils.escapeDollarSequece(XamplifyUtils.escapeDollarSequece(myMergeTagInfo.getAboutUs())));
		updatedBody = updatedBody.replace("{{senderCompanyUrl}}",
				XamplifyUtils.escapeDollarSequece(myMergeTagInfo.getMyCompanyUrl()));
		updatedBody = updatedBody.replace("{{senderCompanyContactNumber}}",
				XamplifyUtils.escapeDollarSequece(myMergeTagInfo.getMyCompanyContactNumber()));
		updatedBody = updatedBody.replace("{{senderCompany}}",
				XamplifyUtils.escapeDollarSequece(myMergeTagInfo.getSenderCompany()));
		updatedBody = updatedBody.replace("{{senderPrivacyPolicy}}", XamplifyUtils
				.escapeDollarSequece(XamplifyUtils.escapeDollarSequece(myMergeTagInfo.getPrivacyPolicy())));
		updatedBody = updatedBody.replace("{{senderAboutUs}}",
				XamplifyUtils.escapeDollarSequece(XamplifyUtils.escapeDollarSequece(myMergeTagInfo.getAboutUs())));
		updatedBody = updatedBody.replace("<<senderEventUrl>>",
				XamplifyUtils.escapeDollarSequece(myMergeTagInfo.getEventUrl()));
		updatedBody = updatedBody.replace("{{senderEventUrl}}",
				XamplifyUtils.escapeDollarSequece(myMergeTagInfo.getEventUrl()));
		updatedBody = updatedBody.replace("<<senderCompanyAddress>>",
				XamplifyUtils.escapeDollarSequece(myMergeTagInfo.getMyCompanyAddress()));
		updatedBody = updatedBody.replace("{{senderCompanyAddress}}",
				XamplifyUtils.escapeDollarSequece(myMergeTagInfo.getMyCompanyAddress()));
		/***** XNFR-281 *****/
		/********* Instagram *********/
		String senderCompanyInstagramUrl = XamplifyUtils.escapeDollarSequece(myMergeTagInfo.getCompanyInstagramUrl());
		updatedBody = updatedBody.replace("<<senderCompanyInstagramUrl>>", senderCompanyInstagramUrl);
		updatedBody = updatedBody.replace("{{senderCompanyInstagramUrl}}", senderCompanyInstagramUrl);

		/******** Twitter *****/
		String senderCompanyTwitterUrl = XamplifyUtils.escapeDollarSequece(myMergeTagInfo.getCompanyTwitterUrl());
		updatedBody = updatedBody.replace("<<senderCompanyTwitterUrl>>", senderCompanyTwitterUrl);
		updatedBody = updatedBody.replace("{{senderCompanyTwitterUrl}}", senderCompanyTwitterUrl);

		/******** Google *****/
		String senderCompanyGoogleUrl = XamplifyUtils.escapeDollarSequece(myMergeTagInfo.getCompanyGoogleUrl());
		String senderCompanyGoogleUrlMergeTagKey = "senderCompanyGoogleUrl";
		updatedBody = updatedBody.replace("<<" + senderCompanyGoogleUrlMergeTagKey + ">>", senderCompanyGoogleUrl);
		updatedBody = updatedBody.replace("{{" + senderCompanyGoogleUrlMergeTagKey + "}}", senderCompanyGoogleUrl);

		/******** Facebook *****/
		String senderCompanyFacebookUrl = XamplifyUtils.escapeDollarSequece(myMergeTagInfo.getCompanyFacebookUrl());
		String senderCompanyFacebookUrlMergeTagKey = "senderCompanyFacebookUrl";
		updatedBody = updatedBody.replace("<<" + senderCompanyFacebookUrlMergeTagKey + ">>", senderCompanyFacebookUrl);
		updatedBody = updatedBody.replace("{{" + senderCompanyFacebookUrlMergeTagKey + "}}", senderCompanyFacebookUrl);

		/******** Linkedin *****/
		String senderCompanyLinkedinUrl = XamplifyUtils.escapeDollarSequece(myMergeTagInfo.getCompanyLinkedinUrl());
		String senderCompanyLinkedinUrlMergeTagKey = "senderCompanyLinkedinUrl";
		updatedBody = updatedBody.replace("<<" + senderCompanyLinkedinUrlMergeTagKey + ">>", senderCompanyLinkedinUrl);
		updatedBody = updatedBody.replace("{{" + senderCompanyLinkedinUrlMergeTagKey + "}}", senderCompanyLinkedinUrl);

		/***** XNFR-281 *****/
		return updatedBody;
	}

	public static String replaceSenderAndPartnerMergeTags(String updatedBody, MyMergeTagsInfo myMergeTagInfo,
			UserDTO sendor) {
		updatedBody = updatedBody.replace("{{senderFirstName}}",
				XamplifyUtils.escapeDollarSequece(sendor.getFirstName()));
		updatedBody = updatedBody.replace("{{senderMiddleName}}",
				XamplifyUtils.escapeDollarSequece(sendor.getMiddleName()));
		updatedBody = updatedBody.replace("{{senderLastName}}",
				XamplifyUtils.escapeDollarSequece(sendor.getLastName()));
		updatedBody = updatedBody.replace("{{senderFullName}}",
				XamplifyUtils.escapeDollarSequece(sendor.getFullName()));
		updatedBody = updatedBody.replace("{{senderJobTitle}}",
				XamplifyUtils.escapeDollarSequece(sendor.getJobTitle()));
		updatedBody = updatedBody.replace("{{senderEmailId}}", XamplifyUtils.escapeDollarSequece(sendor.getEmailId()));
		updatedBody = updatedBody.replace("{{senderContactNumber}}",
				XamplifyUtils.escapeDollarSequece(sendor.getMobileNumber()));
		updatedBody = updatedBody.replace("{{partnerCompanyName}}",
				XamplifyUtils.escapeDollarSequece(myMergeTagInfo.getSenderCompany()));
		updatedBody = updatedBody.replace("{{partnerCompanyEmailId}}",
				XamplifyUtils.escapeDollarSequece(myMergeTagInfo.getMyEmailId()));
		updatedBody = updatedBody.replace("{{partnerCompanyContactNumber}}",
				XamplifyUtils.escapeDollarSequece(myMergeTagInfo.getMyCompanyContactNumber()));
		updatedBody = updatedBody.replace("{{partnerCompanyUrl}}",
				XamplifyUtils.escapeDollarSequece(myMergeTagInfo.getMyCompanyUrl()));
		updatedBody = updatedBody.replace("{{partnerCompanyAddress}}",
				XamplifyUtils.escapeDollarSequece(myMergeTagInfo.getMyCompanyAddress()));
		updatedBody = updatedBody.replace("{{partnerCompanyAboutUs}}",
				XamplifyUtils.escapeDollarSequece(myMergeTagInfo.getAboutUs()));
		updatedBody = updatedBody.replace("{{senderCompanyUrl}}",
				XamplifyUtils.escapeDollarSequece(checkEmptyString(sendor.getWebsite())));
		updatedBody = updatedBody.replace("{{senderCompanyContactNumber}}",
				XamplifyUtils.escapeDollarSequece(sendor.getSenderCompanyContactNumber()));
		updatedBody = updatedBody.replace("{{senderCompany}}",
				XamplifyUtils.escapeDollarSequece(sendor.getCompanyName()));
		updatedBody = updatedBody.replace("{{senderPrivacyPolicy}}",
				XamplifyUtils.escapeDollarSequece(XamplifyUtils.escapeDollarSequece(sendor.getPrivacyPolicy())));
		updatedBody = updatedBody.replace("{{senderAboutUs}}",
				XamplifyUtils.escapeDollarSequece(XamplifyUtils.escapeDollarSequece(sendor.getAboutUs())));
		updatedBody = updatedBody.replace("{{senderEventUrl}}",
				XamplifyUtils.escapeDollarSequece(sendor.getEventUrl()));
		updatedBody = updatedBody.replace("{{senderCompanyAddress}}",
				XamplifyUtils.escapeDollarSequece(sendor.getSenderCompanyAddress()));

		/********* Instagram *********/
		String senderCompanyInstagramUrl = XamplifyUtils.escapeDollarSequece(sendor.getInstagramUrl());
		updatedBody = updatedBody.replace("{{senderCompanyInstagramUrl}}", senderCompanyInstagramUrl);

		/******** Twitter *****/
		String senderCompanyTwitterUrl = XamplifyUtils.escapeDollarSequece(sendor.getTwitterUrl());
		updatedBody = updatedBody.replace("{{senderCompanyTwitterUrl}}", senderCompanyTwitterUrl);

		/******** Google *****/
		String senderCompanyGoogleUrl = XamplifyUtils.escapeDollarSequece(sendor.getGooglePlusLink());
		updatedBody = updatedBody.replace("{{senderCompanyGoogleUrl}}", senderCompanyGoogleUrl);

		/******** Facebook *****/
		String senderCompanyFacebookUrl = XamplifyUtils.escapeDollarSequece(sendor.getFacebookLink());
		updatedBody = updatedBody.replace("{{senderCompanyFacebookUrl}}", senderCompanyFacebookUrl);

		/******** Linkedin *****/
		String senderCompanyLinkedinUrl = XamplifyUtils.escapeDollarSequece(sendor.getLinkedInLink());
		updatedBody = updatedBody.replace("{{senderCompanyLinkedinUrl}}", senderCompanyLinkedinUrl);
		return updatedBody;
	}

	public static String replacePageMergeTags(String updatedBody, User user) {
		MyMergeTagsInfo myMergeTagInfo = getMyMergeTagsData(user);
		return replacePageMergeTagsByMergeTagInfo(updatedBody, myMergeTagInfo);
	}

	private static String replaceAboutUsMergeTagWithEmptyString(String updatedBody) {
		updatedBody = updatedBody.replace(ABOUT_US, "");
		updatedBody = updatedBody.replace(ABOUT_US_MERGE_TAG, "");
		return updatedBody;
	}

	public static String setAboutUsMergeTagsInfo(String updatedBody, String aboutUs) {
		if (StringUtils.hasText(aboutUs)) {
			updatedBody = updatedBody.replace(ABOUT_US, XamplifyUtils.escapeDollarSequece(aboutUs));
			updatedBody = updatedBody.replace(ABOUT_US_MERGE_TAG, XamplifyUtils.escapeDollarSequece(aboutUs));
		} else {
			updatedBody = replaceAboutUsMergeTagWithEmptyString(updatedBody);
		}
		return updatedBody;
	}

	public static MyMergeTagsInfo getMyMergeTagsData(User user) {
		MyMergeTagsInfo myMergeTagInfo = new MyMergeTagsInfo();
		if (user != null) {
			String myFirstName = "";
			String myLastName = "";
			String myFullName = "";
			String middleName = "";
			myFirstName = checkEmptyString(user.getFirstName());
			myLastName = checkEmptyString(user.getLastName());
			middleName = checkEmptyString(user.getMiddleName());
			myFullName = myFirstName + " " + middleName + " " + myLastName;
			myMergeTagInfo.setMyFirstName(myFirstName);
			myMergeTagInfo.setMyLastName(myLastName);
			myMergeTagInfo.setMyMiddleName(middleName);
			myMergeTagInfo.setMyFullName(myFullName);
			myMergeTagInfo.setSenderJobTitle(checkEmptyString(user.getOccupation()));
			myMergeTagInfo.setMyEmailId(user.getEmailId());
			myMergeTagInfo.setMyContactNumber(checkEmptyString(user.getMobileNumber()));
			if (user.getCompanyProfile() != null) {
				CompanyProfile companyProfile = user.getCompanyProfile();
				myMergeTagInfo.setMyCompanyContactNumber(checkEmptyString(companyProfile.getPhone()));
				myMergeTagInfo.setMyCompanyUrl(checkEmptyString(companyProfile.getWebsite()));
				myMergeTagInfo.setSenderCompany(checkEmptyString(companyProfile.getCompanyName()));
				myMergeTagInfo.setAboutUs(checkEmptyString(companyProfile.getAboutUs()));
				myMergeTagInfo.setPrivacyPolicy(checkEmptyString(companyProfile.getPrivacyPolicy()));
				myMergeTagInfo.setEventUrl(checkEmptyString(companyProfile.getEventUrl()));
				String street = checkEmptyAddressTag(companyProfile.getStreet());
				String city = checkEmptyAddressTag(companyProfile.getCity());
				String state = checkEmptyAddressTag(companyProfile.getState());
				String zip = checkEmptyAddressTag(companyProfile.getZip());
				String country = checkEmptyString(companyProfile.getCountry());
				String address = checkEmptyString(street + city + state + zip + country);
				myMergeTagInfo.setMyCompanyAddress(address);
				/********* XNFR-281 ********/
				myMergeTagInfo.setCompanyInstagramUrl(checkEmptyString(companyProfile.getInstagramLink()));
				myMergeTagInfo.setCompanyTwitterUrl(checkEmptyString(companyProfile.getTwitterLink()));
				myMergeTagInfo.setCompanyGoogleUrl(checkEmptyString(companyProfile.getGooglePlusLink()));
				myMergeTagInfo.setCompanyFacebookUrl(checkEmptyString(companyProfile.getFacebookLink()));
				myMergeTagInfo.setCompanyLinkedinUrl(checkEmptyString(companyProfile.getLinkedInLink()));
				/********* XNFR-281 ********/

			}
		}

		return myMergeTagInfo;

	}

	public static Map<String, Object> extractAliasFromUrl(Map<String, Object> map, String param) {
		if (param.endsWith("=")) {
			String name = param.split("=")[0];
			map.put(name, "");
		} else {
			String name = param.split("=")[0];
			Object value = param.split("=")[1];
			map.put(name, value);
		}
		return map;
	}

	public static String addIndexToExistingFileNames(String fileName, List<String> existingFileNames) {
		String extension;
		String name;
		int idxOfDot = fileName.lastIndexOf('.'); // Get the last index of . to separate extension
		extension = fileName.substring(idxOfDot + 1);
		name = fileName.substring(0, idxOfDot);
		int counter = 1;
		while (existingFileNames.indexOf(fileName) > -1) {
			fileName = name + "_" + counter + "." + extension;
			counter++;
		}
		return fileName;

	}

	public static String getSortedOptionValues(Pagination pagination) {
		String sortOptionQueryString = "";
		if (pagination != null) {
			sortOptionQueryString = XamplifyConstants.ORDER_BY;
			if (XamplifyConstants.CREATED_TIME.equals(pagination.getSortcolumn())) {
				sortOptionQueryString += XamplifyConstants.LAUNCH_TIME + pagination.getSortingOrder();
			} else if ("campaign".equals(pagination.getSortcolumn())) {
				sortOptionQueryString += XamplifyConstants.CAMPAIGN_NAME + pagination.getSortingOrder();
			} else if ("launchedBy".equals(pagination.getSortcolumn())) {
				sortOptionQueryString += pagination.getSortcolumn() + " " + pagination.getSortingOrder();
			} else {
				sortOptionQueryString += XamplifyConstants.LAUNCH_TIME + " DESC";
			}
		}
		return sortOptionQueryString;
	}

	public static String sortDealCampaings(Pagination pagination) {
		String sortOptionQueryString = "";
		if (pagination != null) {
			sortOptionQueryString = ORDER_BY;
			if (XamplifyConstants.CREATED_TIME.equals(pagination.getSortcolumn())) {
				sortOptionQueryString += "c.launch_time " + pagination.getSortingOrder();
			} else if ("campaign".equals(pagination.getSortcolumn())) {
				sortOptionQueryString += "c.campaign_name " + pagination.getSortingOrder();
			} else if ("launchedBy".equals(pagination.getSortcolumn()) || "count".equals(pagination.getSortcolumn())) {
				sortOptionQueryString += pagination.getSortcolumn() + " " + pagination.getSortingOrder();
			} else {
				sortOptionQueryString += "count DESC";
			}
		}
		return sortOptionQueryString;
	}

	public static String sortDealPartnersValues(Pagination pagination) {
		String sortOptionQueryString = "";
		if (pagination != null) {
			sortOptionQueryString = " order by ";
			if ("emailId".equals(pagination.getSortcolumn())) {
				sortOptionQueryString += "u.email_id " + pagination.getSortingOrder();
			} else if ("firstName".equals(pagination.getSortcolumn())) {
				sortOptionQueryString += "u.firstname " + pagination.getSortingOrder();
			} else if ("lastName".equals(pagination.getSortcolumn())) {
				sortOptionQueryString += "u.lastname " + pagination.getSortingOrder();
			} else if ("count".equals(pagination.getSortcolumn())) {
				sortOptionQueryString += pagination.getSortcolumn() + " " + pagination.getSortingOrder();
			} else {
				sortOptionQueryString += "count DESC";
			}
		}
		return sortOptionQueryString;

	}

	public static String sortLeads(Pagination pagination) {
		String sortOptionQueryString = "";
		if (pagination != null) {
			sortOptionQueryString = " order by ";
			if ("title".equals(pagination.getSortcolumn())) {
				sortOptionQueryString += "d.title " + pagination.getSortingOrder();
			} else if ("campaignName".equals(pagination.getSortcolumn())) {
				sortOptionQueryString += "c.campaign_name " + pagination.getSortingOrder();
			} else if ("createdTime".equals(pagination.getSortcolumn())) {
				sortOptionQueryString += "d.created_time " + pagination.getSortingOrder();
			} else if ("emailId".equals(pagination.getSortcolumn())) {
				sortOptionQueryString += "u.email_id " + pagination.getSortingOrder();
			} else if ("firstName".equals(pagination.getSortcolumn())) {
				sortOptionQueryString += "d.first_name " + pagination.getSortingOrder();
			} else if ("lastName".equals(pagination.getSortcolumn())) {
				sortOptionQueryString += "d.last_name " + pagination.getSortingOrder();
			} else {
				sortOptionQueryString += "d.created_time DESC";
			}
		}
		return sortOptionQueryString;
	}

	public static String sortLeadsByNewAlias(Pagination pagination) {
		String sortOptionQueryString = "";
		if (pagination != null) {
			sortOptionQueryString = " order by ";
			if ("title".equals(pagination.getSortcolumn())) {
				sortOptionQueryString += "a.title " + pagination.getSortingOrder();
			} else if ("campaignName".equals(pagination.getSortcolumn())) {
				sortOptionQueryString += "a.campaign_name " + pagination.getSortingOrder();
			} else if ("createdTime".equals(pagination.getSortcolumn())) {
				sortOptionQueryString += "a.created_time " + pagination.getSortingOrder();
			} else if ("emailId".equals(pagination.getSortcolumn())) {
				sortOptionQueryString += "a.email_id " + pagination.getSortingOrder();
			} else if ("firstName".equals(pagination.getSortcolumn())) {
				sortOptionQueryString += "a.first_name " + pagination.getSortingOrder();
			} else if ("lastName".equals(pagination.getSortcolumn())) {
				sortOptionQueryString += "a.last_name " + pagination.getSortingOrder();
			} else {
				sortOptionQueryString += "a.created_time DESC";
			}
		}
		return sortOptionQueryString;
	}

	public static Map<String, String> getCampaignShortnerAlias(URLShortener uRLShortener) {
		String url = uRLShortener.getUrl();
		String[] params = url.split("&");
		Map<String, String> map = new HashMap<>();
		for (String param : params) {
			String[] arr = param.split("=", 2);
			String name = arr[0];
			String value = arr[1];
			map.put(name, value);
		}
		return map;
	}

	public static String getAnalyticsTypeFilter(String analyticsTypeInString) {
		String analyticsType = "";
		if ("Campaign&LandingPage".equals(analyticsTypeInString)) {
			analyticsType = ENUM_TYPES;
		} else if ("Campaign".equals(analyticsTypeInString)) {
			analyticsType = "'" + GeoLocationAnalyticsEnum.CAMPAIGN_LANDING_PAGE + "'" + "," + "'"
					+ GeoLocationAnalyticsEnum.PARTNER_LANDING_PAGE + "'";
		} else if ("PartnerLandingPage".equals(analyticsTypeInString)) {
			analyticsType = "'" + GeoLocationAnalyticsEnum.PARTNER_LANDING_PAGE + "'";
		} else {
			analyticsType = "'" + GeoLocationAnalyticsEnum.LANDING_PAGE + "'";
		}
		return analyticsType;
	}

	public static String getSharedPageAnalyticsEnumType() {
		return "'" + GeoLocationAnalyticsEnum.CAMPAIGN_LANDING_PAGE + "'" + "," + "'"
				+ GeoLocationAnalyticsEnum.PARTNER_LANDING_PAGE + "'";

	}

	public static String checkEmptyString(String string) {
		if (StringUtils.hasText(string)) {
			return XamplifyUtils.escapeDollarSequece(string);
		} else {
			return "";
		}
	}

	public static String checkEmptyAddressTag(String string) {
		if (StringUtils.hasText(string)) {
			return XamplifyUtils.escapeDollarSequece(string) + ",";
		} else {
			return "";
		}
	}

	private static String setCompanyName(String string) {
		if (StringUtils.hasText(string)) {
			return XamplifyUtils.escapeDollarSequece(string);
		} else {
			return "Company";
		}
	}

	public static String getAutoResponseType(Integer actionId) {
		String result = "";
		switch (actionId) {
		case 0:
			result = "Email is not opened";
			break;

		case 13:
			result = "Email is opened";
			break;

		case 16:
			result = "Send immediately after email is opened";
			break;

		case 22:
			result = "Email is not redistributed by the partner";
			break;

		case 1:
			result = "Video is played";
			break;

		case 17:
			result = "Send immediately after video is played";
			break;

		case 18:
			result = "Video is not played";
			break;

		case 23:
			result = "Video is not redistributed";
			break;

		case 19:
			result = "Send if not clicked";
			break;

		case 20:
			result = "Send immediately after clicked";
			break;

		case 21:
			result = "Schedule";
			break;

		case 24:
			result = "If partner has not shared event";
			break;

		case 25:
			result = "Send email if RSVP Yes";
			break;

		case 26:
			result = "Send email if RSVP No";
			break;

		case 27:
			result = "Send email if RSVP Maybe";
			break;

		case 28:
			result = "Send after event";
			break;

		case 33:
			result = "Send follow-up email";
			break;

		default:
			break;
		}
		return result;

	}

	public static boolean isUrlExists(String urlLink) throws IOException {
		try {
			URL url = new URL(urlLink);
			// We want to check the current URL
			HttpURLConnection.setFollowRedirects(false);

			HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

			// We don't need to get data
			httpURLConnection.setRequestMethod("HEAD");

			// Some websites don't like programmatic access so pretend to be a
			// browser
			httpURLConnection.setRequestProperty("User-Agent",
					"Mozilla/5.0 (Windows; U; Windows NT 6.0; en-US; rv:1.9.1.2) Gecko/20090729 Firefox/3.5.2 (.NET CLR 3.5.30729)");
			int responseCode = httpURLConnection.getResponseCode();

			// We only accept response code 200
			return responseCode == HttpURLConnection.HTTP_OK;
		} catch (UnknownHostException e) {
			throw new MailException(e);
		}
	}

	public static String replaceLast(String find, String replace, String string) {
		int lastIndex = string.lastIndexOf(find);

		if (lastIndex == -1) {
			return string;
		}

		String beginString = string.substring(0, lastIndex);
		String endString = string.substring(lastIndex + find.length());

		return beginString + replace + endString;
	}

	public static String replaceBetween(String input, String start, String end, boolean startInclusive,
			boolean endInclusive, String replaceWith) {
		start = Pattern.quote(start);
		end = Pattern.quote(end);
		return input.replaceAll("(" + start + ")" + ".*" + "(" + end + ")",
				(startInclusive ? "" : "$1") + replaceWith + (endInclusive ? "" : "$2"));
	}

	public static Set<String> convertListToSet(List<String> emailIds) {
		return new HashSet<>(emailIds);
	}

	public static Set<Integer> convertListToSetElements(List<Integer> ids) {
		return new HashSet<>(ids);
	}

	public static String escapeDollarSequece(String string) {
		if (string != null && StringUtils.hasText(string)) {
			string = string.replace("\\\\", "\\");
			return string.replaceAll("\\$", "\\\\\\$");
		} else {
			return "";
		}
	}

	public static String convertToLowerCaseAndReplaceSpace(String input) {
		if (StringUtils.hasText(input)) {
			input = input.replaceAll("\\s", "-");
			input = input.toLowerCase().trim();
			return input;
		} else {
			return input;
		}
	}

	public static String convertToLowerCaseAndExcludeSpace(String input) {
		if (StringUtils.hasText(input)) {
			input = input.replaceAll("\\s", "");
			input = input.toLowerCase();
			return input;
		} else {
			return input;
		}
	}

	public static String convertToLowerCase(String input) {
		if (StringUtils.hasText(input)) {
			input = input.toLowerCase();
			return input;
		} else {
			return input;
		}
	}

	public static String replaceCustomerFullName(User user, String body) {
		if (user != null) {
			String fullName = "";
			if (StringUtils.hasText(user.getFirstName())) {
				fullName = XamplifyUtils.escapeDollarSequece(user.getFirstName());
			}
			if (StringUtils.hasText(user.getLastName())) {
				if (!fullName.isEmpty()) {
					fullName += " ";
				}
				fullName += XamplifyUtils.escapeDollarSequece(user.getLastName());
			}
			if (fullName.isEmpty()) {
				fullName = REPLACE_THERE;
			}
			body = body.replace(CUSTOMER_FULL_NAME, fullName);
		}
		return body;
	}

	public static String replacePartnerFullName(User user, String body) {
		body = replaceMergeTagPrefixer(body);
		if (user != null) {
			String fullName = "";

			if (StringUtils.hasText(user.getFirstName())) {
				fullName = XamplifyUtils.escapeDollarSequece(user.getFirstName());
			}

			if (StringUtils.hasText(user.getLastName())) {
				if (!fullName.isEmpty()) {
					fullName += " ";
				}
				fullName += XamplifyUtils.escapeDollarSequece(user.getLastName());
			}

			if (fullName.isEmpty()) {
				fullName = REPLACE_THERE;
			}
			body = body.replace(PARTNER_NAME, fullName);
		}
		return body;
	}

	public static String replaceTemplateBody(User user, CompanyProfile companyProfile, String body, User customer) {
		if (companyProfile != null && companyProfile.getCompanyName() != null) {
			body = body.replace(VENDOR_COMPANY_NAME,
					XamplifyUtils.escapeDollarSequece(companyProfile.getCompanyName()));
		}
		if (customer != null) {

			String fullName = "";

			if (XamplifyUtils.isValidString(customer.getFirstName())) {
				fullName += customer.getFirstName();
			}

			if (XamplifyUtils.isValidString(customer.getMiddleName())) {
				fullName += " " + customer.getMiddleName();
			}

			if (XamplifyUtils.isValidString(customer.getLastName())) {
				fullName += " " + customer.getLastName();
			}

			if (XamplifyUtils.isValidString(senderFullName)) {
				body = body.replace(senderFullName, XamplifyUtils.escapeDollarSequece(fullName.trim()));
			}

			if (XamplifyUtils.isValidString(customer.getFirstName())) {
				body = body.replace("<<firstName>>", XamplifyUtils.escapeDollarSequece(customer.getFirstName()));
			}

			if (XamplifyUtils.isValidString(customer.getLastName())) {
				body = body.replace("<<lastName>>", XamplifyUtils.escapeDollarSequece(customer.getLastName()));
			} else {
				body = body.replace("<<lastName>>", REPLACE_THERE);
			}

			if (XamplifyUtils.isValidString(customer.getEmailId())) {
				body = body.replace("<<emailId>>", XamplifyUtils.escapeDollarSequece(customer.getEmailId()));
			}
		}

		return body;
	}

	public static String getCustomerFullName(User user) {
		if (user != null) {
			if (StringUtils.hasText(user.getFirstName())) {
				return XamplifyUtils.escapeDollarSequece(
						user.getFirstName() + " " + replaceNullWithEmptyString(user.getLastName()));
			} else {
				return REPLACE_THERE;
			}
		} else {
			return REPLACE_THERE;
		}

	}

	public static int getOrderForTotalLeads(int order) {
		return order + 1;
	}

	public static String[] convertArrayListToStringArray(List<String> headers) {
		return headers.toArray(new String[headers.size()]);
	}

	public static Set<Integer> getDescendingOrderHashSet(Set<Integer> ids) {
		Set<Integer> sorted = new TreeSet<>(new Comparator<Integer>() {
			@Override
			public int compare(Integer o1, Integer o2) {
				return o2.compareTo(o1);
			}
		});
		sorted.addAll(ids);
		return sorted;
	}

	public static void setMaxResultsForExportToExcel(Pagination pagination, SQLQuery formSubmitQuery,
			Integer totalRecords) {
		if (pagination.isExportToExcel()) {
			formSubmitQuery.setFirstResult((pagination.getPageIndex() - 1) * totalRecords);
			formSubmitQuery.setMaxResults(totalRecords);
		} else {
			formSubmitQuery.setFirstResult((pagination.getPageIndex() - 1) * pagination.getMaxResults());
			formSubmitQuery.setMaxResults(pagination.getMaxResults());
		}
	}

	public static List<String> findCommonElements(List<String> teamMemberDtoEmailIds, List<String> emailIds) {
		teamMemberDtoEmailIds.retainAll(emailIds);
		return teamMemberDtoEmailIds;
	}

	public static List<Integer> findCommonIntegers(List<Integer> list1, List<Integer> list2) {
		list1.retainAll(list2);
		return list1;
	}

	public static List<Integer> findUnMatchedElements(List<Integer> list1, List<Integer> list2) {
		List<Integer> common = new ArrayList<>(list1);
		common.retainAll(list2);

		List<Integer> unMatchedIds = new ArrayList<>();
		for (Integer element : list1)
			if (!common.contains(element))
				unMatchedIds.add(element);
		return unMatchedIds;
	}

	public static String convertListToString(List<String> input) {
		return String.join(",", input.stream().collect(Collectors.toSet()));
	}

	public static List<String> convertStringToArrayList(String string) {
		return new ArrayList<>(Arrays.asList(string.trim().split(",")));

	}

	public static List<String> replaceCommaInString(List<String> data) {
		List<String> updatedData = new ArrayList<>();
		for (String string : data) {
			if (string.contains(",")) {
				updatedData.add("\"" + string + "\"");
			} else {
				updatedData.add(string);
			}
		}
		return updatedData;
	}

	public static String frameVanityURL(String baseClientURL, String companyProfileName) {
		String[] urlArray = baseClientURL.split("//");
		StringBuilder sb = null;
		if (ArrayUtils.isNotEmpty(urlArray)) {
			sb = new StringBuilder(urlArray[0]);
			sb.append("//");
			sb.append(companyProfileName);
			sb.append(".");
			sb.append(urlArray[1]);
		}
		return sb != null ? sb.toString() : baseClientURL;
	}

	public static String addCommasToNumber(Double value) {
		if (value != null && value != 0) {
			DecimalFormat df = new DecimalFormat("#,###.00");
			return df.format(value);
		} else {
			return "";
		}

	}

	public static String getOrignialFileNameWithExtension(String value) {
		if (StringUtils.hasText(value)) {
			String awsFileName = value.substring(value.lastIndexOf('/') + 1);
			String trimmedFileName = awsFileName.substring(0, awsFileName.lastIndexOf('_'));
			String extension = awsFileName.substring(awsFileName.lastIndexOf('.'));
			return trimmedFileName + extension;
		} else {
			return "";
		}

	}

	public static Double convertStringToDouble(String string) {
		return Double.parseDouble(string);
	}

	public static String getFileExtension(String fileName) {
		return fileName.substring(fileName.lastIndexOf('.') + 1);
	}

	public static String convertStringToDoubleString(String string) {
		return addCommasToNumber(convertStringToDouble(string));
	}

	public static Set<String> findDuplicateStrings(List<String> inputs) {
		if (inputs != null && !inputs.isEmpty()) {
			return inputs.stream().filter(n -> inputs.stream().filter(x -> x.equalsIgnoreCase(n)).count() > 1)
					.map(String::toLowerCase).collect(Collectors.toSet());
		} else {
			return new HashSet<>();
		}
	}

	public static String convertSetToCommaSeperatedString(Set<String> set) {
		if (set != null && !set.isEmpty()) {
			return set.stream().map(String::toLowerCase).collect(Collectors.joining(","));
		} else {
			return "";
		}

	}

	public static List<Integer> convertSetToList(Set<Integer> set) {
		if (set != null && !set.isEmpty()) {
			List<Integer> aList = new ArrayList<>();
			aList.addAll(set);
			return aList;
		} else {
			return new ArrayList<>();
		}
	}

	public static List<String> convertSetToListOfString(Set<String> set) {
		if (set != null && !set.isEmpty()) {
			List<String> aList = new ArrayList<>();
			aList.addAll(set);
			return aList;
		} else {
			return new ArrayList<>();
		}
	}

	public static boolean hasSameElements(List<Integer> arrayList1, List<Integer> arrayList2) {
		Collections.sort(arrayList1);
		Collections.sort(arrayList2);
		return arrayList1.equals(arrayList2);
	}

	/**************** XNFR-98 ***************/
	public static void setTeamMemberUserList(Integer userId, User loggedInUser, TeamMember teamMember,
			UserList teamMemberUserList) {
		teamMemberUserList.setOwner(loggedInUser);
		teamMemberUserList.setCompany(loggedInUser.getCompanyProfile());
		teamMemberUserList.setUpdatedBy(userId);
		teamMemberUserList.setCreatedTime(new Date());
		teamMemberUserList.setUpdatedTime(new Date());
		teamMemberUserList.setSocialNetwork(SocialNetwork.MANUAL);
		teamMemberUserList.setContactType(TYPE.CONTACT);
		teamMemberUserList.setPartnerUserList(true);
		teamMemberUserList.setDefaultPartnerList(false);
		teamMemberUserList.setEmailValidationInd(true);
		teamMemberUserList.setModuleName("PARTNERS");
		teamMemberUserList.setTeamMemberPartnerList(true);
		teamMemberUserList.setPublicList(true);
		teamMemberUserList.setTeamMember(teamMember);
	}

	/**************** XNFR-107 ***************/
	public static AdminAndTeamMemberDetailsDTO getTeamMemberMasterPartnerListInfo(
			AdminAndTeamMemberDetailsDTO adminAndTeamMemberDetailsDTO) {
		String name;
		if (adminAndTeamMemberDetailsDTO != null) {
			String emailId = adminAndTeamMemberDetailsDTO.getEmailId().split("@")[0];
			String updatedEmailId = emailId;
			if (updatedEmailId.length() > 150) {
				updatedEmailId = emailId.substring(0, 150);
			}
			String fullName = adminAndTeamMemberDetailsDTO.getFirstName() + " "
					+ adminAndTeamMemberDetailsDTO.getLastName();
			String updatedFullName = fullName.trim();
			if (StringUtils.hasText(updatedFullName)) {
				if (updatedFullName.length() > 150) {
					updatedFullName = fullName.substring(0, 150);
				}
				name = updatedFullName + "-" + adminAndTeamMemberDetailsDTO.getId();
			} else {
				name = updatedEmailId + "-" + adminAndTeamMemberDetailsDTO.getId();
			}
			adminAndTeamMemberDetailsDTO.setFullName(name);
			return adminAndTeamMemberDetailsDTO;
		} else {
			return new AdminAndTeamMemberDetailsDTO();
		}
	}

	public static String replaceReceiverMergeTagsInfoForViewInBrowser(User user, String updatedBody) {
		if (user != null) {
			String firstName;
			String lastName;
			String fullName;
			String sir = REPLACE_THERE;
			if (StringUtils.hasText(user.getFirstName())) {
				firstName = user.getFirstName();
			} else {
				firstName = sir;
			}
			if (StringUtils.hasText(user.getLastName())) {
				lastName = user.getLastName();
			} else {
				lastName = sir;
			}
			fullName = firstName + " " + lastName;
			if (firstName.trim().equals(lastName.trim())) {
				fullName = firstName;
			}
			updatedBody = updatedBody.replace(FIRST_NAME_MERGE_TAG, XamplifyUtils.escapeDollarSequece(firstName));
			updatedBody = updatedBody.replace(LAST_NAME_MERGE_TAG, XamplifyUtils.escapeDollarSequece(lastName));
			updatedBody = updatedBody.replace(FULL_NAME_MERGE_TAG, XamplifyUtils.escapeDollarSequece(fullName));
			updatedBody = updatedBody.replace(EMAIL_ID_MERGE_TAG, XamplifyUtils.escapeDollarSequece(user.getEmailId()));
			updatedBody = updatedBody.replace(COMPANY_NAME_MERGE_TAG, setCompanyName(user.getContactCompany()));
			updatedBody = updatedBody.replace(ADDRESS_MERGE_TAG, XamplifyUtils.checkEmptyString(user.getAddress()));
			updatedBody = updatedBody.replace(CITY_MERGE_TAG, XamplifyUtils.checkEmptyString(user.getCity()));
			updatedBody = updatedBody.replace(STATE_MERGE_TAG, XamplifyUtils.checkEmptyString(user.getState()));
			updatedBody = updatedBody.replace(ZIPCODE_MERGE_TAG, XamplifyUtils.checkEmptyString(user.getZipCode()));
			updatedBody = updatedBody.replace(COUNTRY_MERGE_TAG, XamplifyUtils.checkEmptyString(user.getCountry()));
			updatedBody = updatedBody.replace(MOBILE_NUMBER_MERGE_TAG,
					XamplifyUtils.checkEmptyString(user.getMobileNumber()));
		} else {
			updatedBody = updatedBody.replace(FIRST_NAME_MERGE_TAG, "");
			updatedBody = updatedBody.replace(LAST_NAME_MERGE_TAG, "");
			updatedBody = updatedBody.replace(FULL_NAME_MERGE_TAG, "");
			updatedBody = updatedBody.replace(EMAIL_ID_MERGE_TAG, "");
			updatedBody = updatedBody.replace(COMPANY_NAME_MERGE_TAG, "");
			updatedBody = updatedBody.replace(ADDRESS_MERGE_TAG, "");
			updatedBody = updatedBody.replace(CITY_MERGE_TAG, "");
			updatedBody = updatedBody.replace(STATE_MERGE_TAG, "");
			updatedBody = updatedBody.replace(ZIPCODE_MERGE_TAG, "");
			updatedBody = updatedBody.replace(COUNTRY_MERGE_TAG, "");
			updatedBody = updatedBody.replace(MOBILE_NUMBER_MERGE_TAG, "");
		}
		return updatedBody;
	}

	public static String replaceSenderMergeTagsForViewInBrowser(String updatedBody, MyMergeTagsInfo myMergeTagInfo) {
		updatedBody = updatedBody.replace("{{senderFirstName}}",
				XamplifyUtils.escapeDollarSequece(myMergeTagInfo.getMyFirstName()));
		updatedBody = updatedBody.replace("{{senderMiddleName}}",
				XamplifyUtils.escapeDollarSequece(myMergeTagInfo.getMyMiddleName()));
		updatedBody = updatedBody.replace("{{senderLastName}}",
				XamplifyUtils.escapeDollarSequece(myMergeTagInfo.getMyLastName()));
		updatedBody = updatedBody.replace("{{senderFullName}}",
				XamplifyUtils.escapeDollarSequece(myMergeTagInfo.getMyFullName()));
		updatedBody = updatedBody.replace("{{senderTitle}}",
				XamplifyUtils.escapeDollarSequece(myMergeTagInfo.getSenderJobTitle()));
		updatedBody = updatedBody.replace("{{senderJobTitle}}",
				XamplifyUtils.escapeDollarSequece(myMergeTagInfo.getSenderJobTitle()));
		updatedBody = updatedBody.replace("{{senderEmailId}}",
				XamplifyUtils.escapeDollarSequece(myMergeTagInfo.getMyEmailId()));
		updatedBody = updatedBody.replace("{{senderContactNumber}}",
				XamplifyUtils.escapeDollarSequece(myMergeTagInfo.getMyContactNumber()));
		updatedBody = replacePageMergeTagsByMergeTagInfo(updatedBody, myMergeTagInfo);
		return updatedBody;
	}

	public static List<Integer> convertArrayIntsToArrayListIntegers(Integer[] array) {
		if (array.length > 0) {
			List<Integer> arrayList = new ArrayList<>();
			Collections.addAll(arrayList, array);
			return arrayList;
		} else {
			return Collections.emptyList();
		}

	}

	public static List<?> returnEmptyList() {
		return Collections.emptyList();
	}

	public static List<?> removeDuplicateElements(List<?> list) {
		return new ArrayList<>(new HashSet<>(list));

	}

	public static boolean isNotEmptyList(List<?> list) {
		return list != null && !list.isEmpty();
	}

	public static boolean isNotEmptySet(Set<?> set) {
		return set != null && !set.isEmpty();
	}

	public static boolean isValidInteger(Integer integer) {
		return integer != null && integer > 0;
	}

	public static boolean isValidString(String string) {
		return string != null && StringUtils.hasText(string.trim());
	}

	public static void addSuccessStatus(XtremandResponse response) {
		response.setStatusCode(HttpStatus.OK.value());
		response.setAccess(true);
	}

	public static XtremandResponse set404ErrorResponse() {
		XtremandResponse response = new XtremandResponse();
		response.setStatusCode(404);
		return response;
	}

	public static void addSuccessStatusWithMessage(XtremandResponse response, String message) {
		addSuccessStatus(response);
		response.setMessage(message);
	}

	public static XtremandResponse buildErrorResponse(int statusCode, String message) {
		XtremandResponse response = new XtremandResponse();
		response.setStatusCode(statusCode);
		response.setMessage(message);
		return response;
	}

	public static void addPaginatedDTO(XtremandResponse response, PaginatedDTO paginatedDTO) {
		addSuccessStatus(response);
		response.setData(paginatedDTO);
	}

	public static String getWelcomeDisplayNameForThymeLeafTemplate(String firstName, String lastName) {
		String displayName = XamplifyUtils.setDisplayName(firstName, lastName, "");
		String updatedDisplayName = StringUtils.hasText(displayName) ? displayName.trim() : "";
		String welcomeDisplayName = XamplifyUtils.escapeDollarSequece(updatedDisplayName);
		return welcomeDisplayName.trim();
	}

	public static long getStartTime() {
		return System.currentTimeMillis();
	}

	public static String getExecutionTimeWithActiveThreadsCount(long startTime, String methodName) {
		long stopTime = getStartTime();
		long elapsedTime = stopTime - startTime;

		long minutes = TimeUnit.MILLISECONDS.toMinutes(elapsedTime);
		long seconds = TimeUnit.MILLISECONDS.toSeconds(elapsedTime) % 60;

		String elapsedTimeMessage = (minutes > 0) ? minutes + " minutes" : seconds + " seconds";

		return String.format("Ended: %s Completed In %s at %s ***Active Thread Count: %d***", methodName,
				elapsedTimeMessage, new Date(), Thread.activeCount());

	}

	public static List<List<Integer>> getChunkedList(List<Integer> list) {
		int listSize = list.size();
		int chunkSize = 10000;
		return IntStream.range(0, (listSize - 1) / chunkSize + 1)
				.mapToObj(i -> list.subList(i *= chunkSize, listSize - chunkSize >= i ? i + chunkSize : listSize))
				.collect(Collectors.toList());
	}

	/**** XNFR-222 *****/
	public static XtremandResponse validateScheduledDateAndTime(String timeZone, String scheduledDateAndTimeInString,
			XtremandResponse response) {
		if (StringUtils.hasText(timeZone)) {
			String extractedTimeZoneId = timeZone.substring(timeZone.lastIndexOf(')') + 1).trim();
			ZonedDateTime scheduledDateAndTimeInUTC = DateUtils.convertCampaignDateAndTimeInUTC(extractedTimeZoneId,
					scheduledDateAndTimeInString);
			ZonedDateTime serverDateAndTimeInUTC = DateUtils.getServerTimeInUTC();
			String scheduledDateAndTimeInUTCString = DateUtils.convertUTCTimeInString(scheduledDateAndTimeInUTC);
			Integer comparedResult = scheduledDateAndTimeInUTC.compareTo(serverDateAndTimeInUTC);
			boolean invalidScheduledDateAndTime = comparedResult < 0 || comparedResult == 0;
			if (invalidScheduledDateAndTime) {
				response.setStatusCode(400);
				response.setMessage("Campaign cannot be scheduled for selected date & time");
			} else {
				response.setStatusCode(200);
				setLaunchTimeAndLaunchTimeInUTCStringData(scheduledDateAndTimeInString, response,
						scheduledDateAndTimeInUTCString);
			}
		} else {
			response.setStatusCode(400);
			response.setMessage("Please select country");
		}
		return response;
	}

	private static void setLaunchTimeAndLaunchTimeInUTCStringData(String scheduledDateAndTimeInString,
			XtremandResponse response, String scheduledDateAndTimeInUTCString) {
		Map<String, Object> map = new HashMap<>();
		try {
			Date launchTime = LAUNCH_TIME_FORMAT.parse(scheduledDateAndTimeInString);
			map.put("launchTime", launchTime);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		map.put("launchTimeInUTCString", scheduledDateAndTimeInUTCString);
		response.setMap(map);
	}

	public static boolean isLoginAsPartner(Integer loggedInAsPartnerId) {
		return loggedInAsPartnerId != null && loggedInAsPartnerId > 0;
	}

	public static String generateAlias() {
		GenerateRandomPassword password = new GenerateRandomPassword();
		return password.getPassword();
	}

	public static String getWhiteLabeledName(String name, String companyProfileName) {
		return name + "-" + companyProfileName + "-" + System.currentTimeMillis();
	}

	public static boolean isNumeric(String inputString) {
		try {
			Integer.parseInt(inputString);
			return true;
		} catch (NumberFormatException e) {
			System.err.println("Input String cannot be parsed to Integer.");
		}
		return false;
	}

	public static boolean isPartnerGroupOrPartnerCompanySelected(Set<Integer> partnerGroupIds,
			Set<Integer> partnerIds) {
		boolean partnerGroupIdsSelected = partnerGroupIds != null && !partnerGroupIds.isEmpty();
		boolean partnerIdsSelected = partnerIds != null && !partnerIds.isEmpty();
		return partnerGroupIdsSelected || partnerIdsSelected;
	}

	public static String replaceSpacesWithPlus(String input) {
		if (StringUtils.hasText(input)) {
			input = input.replaceAll("\\s", "+");
			input = input.toLowerCase();
			return input;
		} else {
			return input;
		}
	}

	public static String removeExtraSpace(String input) {
		if (StringUtils.hasText(input)) {
			return input.replaceAll("\\s+", " ").trim();
		} else {
			return input;
		}
	}

	public static String extractTimeZone(String timeZone) {
		if (StringUtils.hasText(timeZone)) {
			if (timeZone.startsWith("(")) {
				return timeZone.substring(timeZone.lastIndexOf(')') + 1).trim();
			} else {
				return timeZone;
			}
		} else {
			return "PST8PDT";
		}
	}

	public static String escapeSingleQuotesForSearchQuery(String searchKey) {
		if (StringUtils.hasText(searchKey)) {
			searchKey = searchKey.replace("'", "''");
			return searchKey;
		} else {
			return "";
		}

	}

	public static String addBackSlashToSpecialCharacters(String searchKey) {
		if (StringUtils.hasText(searchKey)) {
			if (searchKey.contains("%")) {
				searchKey = searchKey.replace("%", "\\%");
			}
			if (searchKey.contains("*")) {
				searchKey = searchKey.replace("*", "\\*");
			}
			if (searchKey.contains("_")) {
				searchKey = searchKey.replace("_", "\\_");
			}
			if (searchKey.contains("@")) {
				searchKey = searchKey.replace("@", "\\@");
			}

			return searchKey;
		} else {
			return "";
		}

	}

	public static Map<String, Object> convertStringToHashMap(String input) {
		Map<String, Object> map = new HashMap<>();
		if (StringUtils.hasText(input) && isValidJsonObject(input)) {
			map = Arrays.stream(input.substring(1, input.length() - 1).split(",")).map(s -> s.split("=", 2))
					.collect(Collectors.toMap(s -> s[0].trim(), s -> s[1].trim()));
		}
		return map;

	}

	public static boolean isValidJsonObject(String jsonInput) {
		try {
			new JSONObject(jsonInput);
		} catch (JSONException e) {
			return false;
		}
		return true;
	}

	public static List<?> returnQueryList(List<?> ids) {
		if (XamplifyUtils.isNotEmptyList(ids)) {
			return ids;
		} else {
			return Collections.emptyList();
		}

	}

	/********** XNFR-342 *******/
	public static boolean isUniqueArrayLists(List<?> list1, List<?> list2) {
		if (isNotEmptyList(list1) && isNotEmptyList(list2)) {
			return list1.equals(list2);
		} else {
			return false;
		}
	}

	/******* XNFR-423 *****/
	public static List<String> getCountryNames() {
		List<String> countryNames = new ArrayList<>();
		String[] locales = Locale.getISOCountries();
		for (String countryCode : locales) {
			Locale obj = new Locale("", countryCode);
			countryNames.add(obj.getDisplayCountry());
		}
		Collections.sort(countryNames);
		return countryNames;
	}

	/**** XBI-2085 ***/
	public static String appendDQ(String str) {
		return "\"" + str + "\"";
	}

	public static String replaceSingleQuote(String inputString) {
		if (inputString != null && StringUtils.hasText(inputString) && inputString.contains("'")) {
			inputString = inputString.replace("'", "''");
		}
		return inputString;
	}

	public static <T> Set<T> findDuplicateBySetAdd(List<T> list) {
		Set<T> items = new HashSet<>();
		return list.stream().filter(n -> !items.add(n)).collect(Collectors.toSet());

	}

	public static String getEmailDomain(String emailId) {
		return emailId.substring(emailId.indexOf("@") + 1).toLowerCase();
	}

	public static boolean hasAnyAdminRole(List<String> roles) {
		return roles.indexOf(Role.PRM_ROLE.getRoleName()) > -1;

	}

	public static boolean isPartnerAdmin(List<String> roleNames) {
		return roleNames.indexOf(Role.COMPANY_PARTNER.getRoleName()) > -1;
	}

	public static void getMemoryDetails() {

		System.out.println(" \t " + Runtime.getRuntime().freeMemory() + " \t \t " + Runtime.getRuntime().totalMemory()
				+ " \t \t " + Runtime.getRuntime().maxMemory());

	}

	/**** XNFR-496 *****/
	public static String replaceVanityTemplateMergeTags(String updatedHtmlBody, User user) {
		if (StringUtils.hasText(updatedHtmlBody) && user != null) {
			String myFirstName = "";
			String myLastName = "";
			String myFullName = "";
			String middleName = "";
			myFirstName = checkEmptyString(user.getFirstName());
			myLastName = checkEmptyString(user.getLastName());
			middleName = checkEmptyString(user.getMiddleName());
			myFullName = myFirstName + " " + middleName + " " + myLastName;
			String emailId = user.getEmailId();
			CompanyProfile companyProfile = user.getCompanyProfile();
			String companyName = companyProfile.getCompanyName();
			updatedHtmlBody = updatedHtmlBody.replace("{{senderCompanyName}}",
					XamplifyUtils.escapeDollarSequece(companyName));
			updatedHtmlBody = updatedHtmlBody.replace("{{companyName}}",
					XamplifyUtils.escapeDollarSequece(companyName));
			updatedHtmlBody = updatedHtmlBody.replace(FIRST_NAME_MERGE_TAG,
					XamplifyUtils.escapeDollarSequece(myFirstName));
			updatedHtmlBody = updatedHtmlBody.replace(LAST_NAME_MERGE_TAG,
					XamplifyUtils.escapeDollarSequece(myLastName));
			updatedHtmlBody = updatedHtmlBody.replace(FULL_NAME_MERGE_TAG,
					XamplifyUtils.escapeDollarSequece(myFullName));
			updatedHtmlBody = updatedHtmlBody.replace(EMAIL_ID_MERGE_TAG, emailId);

		}
		return updatedHtmlBody;

	}

	public static void addErorMessageWithStatusCode(XtremandResponse response, String message, Integer statusCode) {
		response.setMessage(message);
		response.setStatusCode(statusCode);
		response.setAccess(true);
	}

	public static Map<String, String> getShortenUrlLogMapByUrl(String shortenUrlAlias) {
		Map<String, String> map = new HashMap<>();
		String[] params = shortenUrlAlias.split("&");
		for (String param : params) {
			String[] arr = param.split("=", 2);
			String name = arr[0];
			String value = arr[1];
			map.put(name, value);
		}
		return map;
	}

	public static List<Object> convertSetToListOfObjects(Set<?> set) {
		if (set != null && !set.isEmpty()) {
			return new ArrayList<>(set);
		} else {
			return new ArrayList<>();
		}
	}

	public static boolean isMatched(List<String> list, String matchingText) {
		if (XamplifyUtils.isValidString(matchingText)) {
			matchingText = matchingText.trim().toLowerCase();
		}
		return isNotEmptyList(list) && list.stream().anyMatch(matchingText::equalsIgnoreCase);

	}

	public static List<String> defaultContactCsvHeaderColumns() {
		return Arrays.asList("FIRSTNAME", "LASTNAME", "COMPANY", "JOBTITLE", "EMAILID", "ADDRESS", "CITY", "STATE",
				"ZIP CODE", "COUNTRY", "MOBILE NUMBER");
	}

	public static List<String> getFlexiFieldNames(List<FlexiFieldResponseDTO> flexiFields) {
		return flexiFields.stream().map(flexiField -> flexiField.getFieldName().toUpperCase())
				.collect(Collectors.toList());
	}

	public static List<String> convertToLowerCaseStrings(List<String> list) {
		if (XamplifyUtils.isNotEmptyList(list)) {
			return list.stream().map(String::toLowerCase).collect(Collectors.toList());
		}
		return list;
	}

	public static HttpServletResponse generateCSV(String fileName, HttpServletResponse response, List<String[]> data)
			throws IOException {
		String csvFileName = fileName + ".csv";
		response.setContentType("text/csv");
		String headerKey = "Content-Disposition";
		String headerValue = String.format("attachment; filename=\"%s\"", csvFileName);
		response.setHeader(headerKey, headerValue);
		CSVWriter writer = new CSVWriter(response.getWriter());
		writer.writeAll(data);
		writer.flush();
		writer.close();
		return response;
	}

	public static boolean isCustomDomain(String companyProfileName) {
		return companyProfileName.contains(".");
	}

	public static String removeLastCharOptional(String string) {
		return Optional.ofNullable(string).filter(str -> str.length() != 0)
				.map(str -> str.substring(0, str.length() - 1)).orElse(string);
	}

	/** XNFR-745 **/
	public static List<Integer> convertStringToIntegerArrayList(String string) {
		try {
			if (!XamplifyUtils.isValidString(string)) {
				return Collections.emptyList();
			} else {
				return Arrays.stream(string.trim().split(",")).map(Integer::parseInt).collect(Collectors.toList());
			}
		} catch (Exception e) {
			return Collections.emptyList();
		}
	}

	public static void removeNullsFromSet(Set<Integer> partnerListIds) {
		partnerListIds.remove(null);
	}

	public static void removeNullsFromList(List<Integer> ids) {
		if (isNotEmptyList(ids)) {
			ids.remove(null);
		}
	}

	public static FormTypeEnum getFormTypeByObjectType(ObjectType objectType) {
		FormTypeEnum formTypeEnum = null;
		switch (objectType) {
		case LEAD:
			formTypeEnum = FormTypeEnum.CRM_LEAD_CUSTOM_FORM;
			break;
		case DEAL:
			formTypeEnum = FormTypeEnum.CRM_DEAL_CUSTOM_FORM;
			break;
		default:
			formTypeEnum = FormTypeEnum.CRM_LEAD_CUSTOM_FORM;
		}
		return formTypeEnum;
	}

	public static String getFormNameByFormType(FormTypeEnum formType) {
		String formName = "";
		switch (formType) {
		case XAMPLIFY_LEAD_CUSTOM_FORM:
			formName = "xAmplify Lead Custom Form";
			break;
		case XAMPLIFY_DEAL_CUSTOM_FORM:
			formName = "xAmplify Deal Custom Form";
			break;
		default:
			formName = "xAmplify Lead Custom Form";
		}
		return formName;
	}

	public static String replaceSpacesWithUnderScore(String input) {
		if (StringUtils.hasText(input)) {
			input = input.replaceAll("[^a-zA-Z0-9]", "_");
			return input;
		} else {
			return input;
		}
	}

	public static boolean isSuccessfulResponse(XtremandResponse response) {
		return response.getStatusCode() == 200;
	}

	public static String frameDateFilterQuery(Pagination pagination, String dateColumn) {
		String dateFilterQueryString = "";
		if (pagination.getFromDateFilter() != null && pagination.getToDateFilter() != null) {
			dateFilterQueryString = " and " + dateColumn + " between  TO_TIMESTAMP('" + pagination.getFromDateFilter()
					+ "', 'Dy Mon DD HH24:MI:SS ZZZ YYYY') and TO_TIMESTAMP('" + pagination.getToDateFilter()
					+ "', 'Dy Mon DD HH24:MI:SS ZZZ YYYY') ";
		}
		return dateFilterQueryString;
	}

	public static String getEmailBaseDomain(String emailId) {
		String emailBaseDomain = emailId.substring(emailId.indexOf("@") + 1, emailId.lastIndexOf(".")).toLowerCase();
		if (emailBaseDomain.contains(".")) {
			emailBaseDomain = getEmailBaseDomain(emailBaseDomain);
		}
		return emailBaseDomain;
	}

	public static List<List<String>> getStringChunkedList(List<String> list, Integer chunkSize) {
		int listSize = list.size();
		return IntStream.range(0, (listSize - 1) / chunkSize + 1)
				.mapToObj(i -> list.subList(i *= chunkSize, listSize - chunkSize >= i ? i + chunkSize : listSize))
				.collect(Collectors.toList());
	}
}
