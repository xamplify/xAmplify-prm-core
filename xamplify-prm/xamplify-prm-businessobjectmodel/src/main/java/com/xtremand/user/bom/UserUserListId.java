package com.xtremand.user.bom;

import java.io.Serializable;

import javax.persistence.Embeddable;
import javax.persistence.ManyToOne;

@Embeddable
public class UserUserListId implements Serializable{

	@ManyToOne
	private User user;
	
	@ManyToOne
	private UserList userList;
	
	public UserUserListId() {
		// TODO Auto-generated constructor stub
	}
	
	public UserUserListId(User user, UserList userList) {
		this.user = user;
		this.userList = userList;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public UserList getUserList() {
		return userList;
	}

	public void setUserList(UserList userList) {
		this.userList = userList;
	}
	
	public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserUserListId that = (UserUserListId) o;

        if (user != null ? !user.equals(that.user) : that.user != null) return false;
        if (userList != null ? !userList.equals(that.userList) : that.userList != null)
            return false;

        return true;
    }

    public int hashCode() {
        int result;
        result = (user != null ? user.hashCode() : 0);
        result = 31 * result + (userList != null ? userList.hashCode() : 0);
        return result;
    }
}
