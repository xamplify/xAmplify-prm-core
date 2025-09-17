package com.xtremand.user.bom;

import javax.persistence.AssociationOverride;
import javax.persistence.AssociationOverrides;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = "xt_user_subscription")
@AssociationOverrides({
@AssociationOverride(name = "userCustomerId.user", 
			joinColumns = @JoinColumn(name = "user_id")),
			@AssociationOverride(name = "userCustomerId.customer", 
			joinColumns = @JoinColumn(name = "customer_id")) })
public class UserCustomer {
	@EmbeddedId
	private UserCustomerId userCustomerId = new UserCustomerId();
	
	@Column(name="is_org_admin", nullable = false)
	private boolean orgAdmin;

	public UserCustomer() {
		// TODO Auto-generated constructor stub
	}
	
	public UserCustomer(User user, User customer, boolean isOrgAdmin) {
		this.userCustomerId.setUser(user);
		this.userCustomerId.setCustomer(customer);
		this.orgAdmin = isOrgAdmin;
	}
	
	public UserCustomerId getUserCustomerId() {
		return userCustomerId;
	}

	public void setUserCustomerId(UserCustomerId userCustomerId) {
		this.userCustomerId = userCustomerId;
	}

	public boolean isOrgAdmin() {
		return orgAdmin;
	}

	public void setOrgAdmin(boolean orgAdmin) {
		this.orgAdmin = orgAdmin;
	}

	@Transient
	public User getUser(){
		return getUserCustomerId().getUser();
	}
	public void setUser(User user){
		getUserCustomerId().setUser(user);
	}
	
	@Transient
	public User getCustomer(){
		return getUserCustomerId().getCustomer();
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((userCustomerId == null) ? 0 : userCustomerId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof UserCustomer))
			return false;
		UserCustomer other = (UserCustomer) obj;
		if (userCustomerId == null) {
			if (other.userCustomerId != null)
				return false;
		} else if (!userCustomerId.equals(other.userCustomerId))
			return false;
		return true;
	}

	public void setCustomer(User customer){
		getUserCustomerId().setCustomer(customer);
	}

	
}

