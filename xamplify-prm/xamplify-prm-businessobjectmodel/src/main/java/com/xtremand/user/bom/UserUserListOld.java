package com.xtremand.user.bom;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

//@Entity
//@Table(name = "xt_user_userlist")
/*@AssociationOverrides({
@AssociationOverride(name = "userUserListId.user", 
			joinColumns = @JoinColumn(name = "user_id")),
			@AssociationOverride(name = "userUserListId.userList", 
			joinColumns = @JoinColumn(name = "user_list_id")) })*/
public class UserUserListOld implements Serializable{
	
	@EmbeddedId
	private UserUserListId userUserListId = new UserUserListId();
	
	@ManyToOne
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;
	
	private String description;
	private String country;
	private String city;
	private String address;
	
	@Column(name="contact_company")
	private String contactCompany;
	
	@Column(name="job_title")
	private String jobTitle;
	
	@Column(name="firstname")
	private String firstName;
	
	@Column(name="lastname")
	private String lastName;
	
	@Column(name="mobile_number") 
	private String mobileNumber;
	
	@Column(name="state")
	private String state;
	
	@Column(name="zip")
	private String zipCode;
	
	private String vertical;
	private String region;
	@Column(name="partner_type")
	private String partnerType;
	private String category ;
	
	/*
	 * @ManyToMany(fetch=FetchType.LAZY)
	 * 
	 * @JoinTable(name = "xt_user_legal_basis", joinColumns = {@JoinColumn(name =
	 * "user_userlist_id")}, inverseJoinColumns = @JoinColumn(name =
	 * "legal_basis_id")) private List<LegalBasis> legalBasis;
	 */
	
	public UserUserListOld() {
	}
	
	public UserUserListOld(User user, UserList userList, String description, String country, String city, String address, String contactCompany, String jobTitle, String firstName,
			String lastName, String mobileNumber) {
		this.userUserListId.setUser(user);
		this.userUserListId.setUserList(userList);
		this.description = description;
		this.country = country;
		this.city = city;
		this.address = address;
		this.contactCompany = contactCompany;
		this.jobTitle = jobTitle;
		this.firstName = firstName;
		this.lastName = lastName;
		this.mobileNumber = mobileNumber;
	}

	public UserUserListId getUserUserListId() {
		return userUserListId;
	}

	public void setUserUserListId(UserUserListId userUserListId) {
		this.userUserListId = userUserListId;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getContactCompany() {
		return contactCompany;
	}

	public void setContactCompany(String contactCompany) {
		this.contactCompany = contactCompany;
	}

	public String getJobTitle() {
		return jobTitle;
	}

	public void setJobTitle(String jobTitle) {
		this.jobTitle = jobTitle;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getMobileNumber() {
		return mobileNumber;
	}

	public void setMobileNumber(String mobileNumber) {
		this.mobileNumber = mobileNumber;
	}
	
	@Transient
	public User getUser(){
		return getUserUserListId().getUser();
	}
	public void setUser(User user){
		getUserUserListId().setUser(user);
	}
	
	@Transient
	public UserList getUserList(){
		return getUserUserListId().getUserList();
	}
	
	public void setUserList(UserList userList){
		getUserUserListId().setUserList(userList);
	}
	
	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getZipCode() {
		return zipCode;
	}

	public void setZipCode(String zipCode) {
		this.zipCode = zipCode;
	}
	
	public String getVertical() {
		return vertical;
	}

	public void setVertical(String vertical) {
		this.vertical = vertical;
	}

	public String getRegion() {
		return region;
	}

	public void setRegion(String region) {
		this.region = region;
	}

	public String getPartnerType() {
		return partnerType;
	}

	public void setPartnerType(String partnerType) {
		this.partnerType = partnerType;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}
	
	/*
	 * public List<LegalBasis> getLegalBasis() { return legalBasis; }
	 * 
	 * public void setLegalBasis(List<LegalBasis> legalBasis) { this.legalBasis =
	 * legalBasis; }
	 */


	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		UserUserListOld that = (UserUserListOld) o;
		
		if (getUserUserListId() != null ? !getUserUserListId().equals(that.getUserUserListId())
				: that.getUserUserListId() != null)
			return false;

		return true;
	}

	public int hashCode() {
		return (getUserUserListId() != null ? getUserUserListId().hashCode() : 0);
	}

	
}
