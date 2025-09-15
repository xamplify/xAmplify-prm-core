package com.xtremand.user.bom;

import java.util.function.Predicate;

import org.apache.commons.lang3.StringUtils;

public class UserPredicates {
	
	public static Predicate<User> predicateCountryEq(String country) {
		return p -> StringUtils.equalsIgnoreCase(p.getCountry(), country) ;
	}

	public static Predicate<User> predicateCountryLike(String country) {
		return p -> StringUtils.containsIgnoreCase(p.getCountry(), country);
	}
	public static Predicate<User> predicateContactCompanyEq(String contactCompany) {
		return p ->  StringUtils.equalsIgnoreCase(p.getContactCompany(), contactCompany ) ;
	}

	public static Predicate<User> predicateContactCompanyLike(String contactCompany) {
		return p ->  StringUtils.containsIgnoreCase(p.getContactCompany(), contactCompany) ;
	}
	public static Predicate<User> predicateJobTitleEq(String jobTitle) {
		return p ->  StringUtils.equalsIgnoreCase(p.getJobTitle(), jobTitle) ;
	}

	public static Predicate<User> predicateJobTitleLike(String jobTitle) {
		return p ->  StringUtils.containsIgnoreCase(p.getJobTitle(), jobTitle) ;
	}

	public static Predicate<User> predicateFirstNameEq(String firstName) {
		return p -> StringUtils.equalsIgnoreCase(p.getFirstName(), firstName) ;
	}

	public static Predicate<User> predicateFirstNameLike(String firstName) {
		return p ->  StringUtils.containsIgnoreCase(p.getFirstName(), firstName);
	}
	
	public static Predicate<User> predicateLastNameEq(String lastName) {
		return p -> StringUtils.equalsIgnoreCase(p.getLastName(), lastName) ;
	}

	public static Predicate<User> predicateLastNameLike(String lastName) {
		return p ->  StringUtils.containsIgnoreCase(p.getLastName(), lastName);
	}
	public static Predicate<User> predicateMobileNumberEq(String mobileNumber) {
		return p -> StringUtils.equalsIgnoreCase(p.getMobileNumber(), mobileNumber) ;
	}

	public static Predicate<User> predicateMobileNumberLike(String mobileNumber) {
		return p ->  StringUtils.containsIgnoreCase(p.getMobileNumber(), mobileNumber);
	}
	
	public static Predicate<User> predicateCityEq(String city) {
		return p -> StringUtils.equalsIgnoreCase(p.getCity(), city) ;
	}

	public static Predicate<User> predicateCityLike(String city) {
		return p ->  StringUtils.containsIgnoreCase(p.getCity(), city);
	}
	public static Predicate<User> predicateDescriptionEq(String description) {
		return p -> StringUtils.equalsIgnoreCase(p.getDescription(), description) ;
	}

	public static Predicate<User> predicateDescriptionLike(String description) {
		return p ->  StringUtils.containsIgnoreCase(p.getDescription(), description);
	}
	public static Predicate<User> predicateEmailIdEq(String emailId) {
		return p -> StringUtils.equalsIgnoreCase(p.getEmailId(), emailId) ;
	}

	public static Predicate<User> predicateEmailIdLike(String emailId) {
		return p ->  StringUtils.containsIgnoreCase(p.getEmailId(), emailId);
	}
	public static Predicate<User> predicateVerticalEq(String vertical) {
		return p -> StringUtils.equalsIgnoreCase(p.getVertical(), vertical) ;
	}

	public static Predicate<User> predicateVerticalLike(String vertical) {
		return p ->  StringUtils.containsIgnoreCase(p.getVertical(), vertical);
	}
	public static Predicate<User> predicateRegionEq(String region) {
		return p -> StringUtils.equalsIgnoreCase(p.getRegion(), region) ;
	}

	public static Predicate<User> predicateRegionLike(String region) {
		return p ->  StringUtils.containsIgnoreCase(p.getRegion(), region);
	}
	public static Predicate<User> predicatePartnerTypeEq(String partnerType) {
		return p -> StringUtils.equalsIgnoreCase(p.getPartnerType(), partnerType) ;
	}

	public static Predicate<User> predicatePartnerTypeLike(String partnerType) {
		return p ->  StringUtils.containsIgnoreCase(p.getPartnerType(), partnerType);
	}
	public static Predicate<User> predicateCategoryEq(String category) {
		return p -> StringUtils.equalsIgnoreCase(p.getCategory(), category) ;
	}

	public static Predicate<User> predicateCategoryLike(String category) {
		return p ->  StringUtils.containsIgnoreCase(p.getCategory(), category);
	}
	public static Predicate<User> predicateStateEq(String state) {
		return p -> StringUtils.equalsIgnoreCase(p.getState(), state) ;
	}

	public static Predicate<User> predicateStateLike(String state) {
		return p ->  StringUtils.containsIgnoreCase(p.getState(), state);
	}
	
}
