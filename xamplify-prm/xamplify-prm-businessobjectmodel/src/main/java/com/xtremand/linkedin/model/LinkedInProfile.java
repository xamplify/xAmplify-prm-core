package com.xtremand.linkedin.model;

import java.util.List;

import lombok.Data;

@Data
public class LinkedInProfile {

	Name lastName;
	Name firstName;
	ProfilePicture profilePicture;
	private String id;

	private String profilePictureUrl;
	private String emailAddress;

	private String localizedFirstName;
	private String localizedLastName;
	
	List<LinkedInElement> elements;
	
	private boolean page = false;
	private Integer firstDegreeSize;
	
	public void initialize(){
		this.setLocalizedFirstName(this.getFirstName().getLocalized().getEn_US());
		this.setLocalizedLastName(this.getLastName().getLocalized().getEn_US());
		if(this.getProfilePicture()!=null) {
			DisplayImageObject displayImageObject = this.getProfilePicture().getDisplayImageObject();
			if(displayImageObject!=null) {
				List<DisplayImageElement> displayImageElements = displayImageObject.getElements();
				if(!displayImageElements.isEmpty()) {
					List<Identifier> identifiers = displayImageElements.get(0).getIdentifiers();
					if(!identifiers.isEmpty()) {
						this.setProfilePictureUrl(identifiers.get(0).getIdentifier());
					}
				}
			}
		}
	

	}

}