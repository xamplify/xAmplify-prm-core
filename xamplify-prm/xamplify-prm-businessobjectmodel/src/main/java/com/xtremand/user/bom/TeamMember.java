package com.xtremand.user.bom;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import com.xtremand.common.bom.PartnerTeamMemberViewType;
import com.xtremand.team.member.group.bom.TeamMemberGroupUserMapping;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "xt_team_member")
public class TeamMember implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6529844193648698619L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "team_member_id_seq")
	@SequenceGenerator(name = "team_member_id_seq", sequenceName = "team_member_id_seq", allocationSize = 1)
	@Column(name = "id")
	private Integer id;

	@ManyToOne
	@JoinColumn(name = "org_admin_id")
	private User orgAdmin;

	@ManyToOne
	@JoinColumn(name = "team_member_id")
	private User teamMember;

	@Column(name = "status")
	@org.hibernate.annotations.Type(type = "com.xtremand.user.bom.TeamMemberTypeEnum")
	private TeamMemberStatus teamMemberStatus;

	@Column(name = "created_time", columnDefinition = "DATETIME")
	@Temporal(TemporalType.TIMESTAMP)
	private Date createdTime;

	@Column(name = "updated_time", columnDefinition = "DATETIME", nullable = true)
	@Temporal(TemporalType.TIMESTAMP)
	private Date updatedTime;

	@Column(name = "company_id")
	private Integer companyId;

	@OneToOne(mappedBy = "teamMember", cascade = CascadeType.ALL)
	private TeamMemberGroupUserMapping teamMemberGroupUserMapping;

	@Column(name = "is_second_admin")
	private boolean secondAdmin;

	/****** XNFR-314 ******/
	@OneToOne(mappedBy = "teamMember", cascade = CascadeType.ALL)
	private TeamMemberPartnerFilterOption teamMemberPartnerFilterOption;

	/****** XNFR-454 ******/
	@Column(name = "is_added_through_sign_up_link")
	@Getter
	@Setter
	private boolean addedThroughSignUpLink;
	
	@Column(name = "is_added_through_oauth_sso")
	@Getter
	@Setter
	private boolean addedThroughOAuthSSO;

	@Column(name = "is_added_through_invitation")
	@Getter
	@Setter
	private boolean addedThroughInvitation;

	/**** XNFR-107 ********/
	@Transient
	private UserList teamMemberUserList;
	
	@Getter
	@Setter
	@OneToMany(mappedBy = "teamMember", cascade = CascadeType.ALL)
	private Set<PartnerTeamMemberViewType> partnerTeamMemberViewType;
	
	/** XNFR-821 **/
	@Getter
	@Setter
	@Column(name = "is_asset_approver")
	private boolean assetApprover;
	
	@Getter
	@Setter
	@Column(name = "is_track_approver")
	private boolean trackApprover;
	
	@Getter
	@Setter
	@Column(name = "is_playbook_approver")
	private boolean playbookApprover;

	public boolean isSecondAdmin() {
		return secondAdmin;
	}

	public void setSecondAdmin(boolean secondAdmin) {
		this.secondAdmin = secondAdmin;
	}

	public TeamMemberGroupUserMapping getTeamMemberGroupUserMapping() {
		return teamMemberGroupUserMapping;
	}

	public void setTeamMemberGroupUserMapping(TeamMemberGroupUserMapping teamMemberGroupUserMapping) {
		this.teamMemberGroupUserMapping = teamMemberGroupUserMapping;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public TeamMemberStatus getTeamMemberStatus() {
		return teamMemberStatus;
	}

	public void setTeamMemberStatus(TeamMemberStatus teamMemberStatus) {
		this.teamMemberStatus = teamMemberStatus;
	}

	public Date getCreatedTime() {
		return createdTime;
	}

	public void setCreatedTime(Date createdTime) {
		this.createdTime = createdTime;
	}

	public Date getUpdatedTime() {
		return updatedTime;
	}

	public void setUpdatedTime(Date updatedTime) {
		this.updatedTime = updatedTime;
	}

	public User getOrgAdmin() {
		return orgAdmin;
	}

	public void setOrgAdmin(User orgAdmin) {
		this.orgAdmin = orgAdmin;
	}

	public User getTeamMember() {
		return teamMember;
	}

	public void setTeamMember(User teamMember) {
		this.teamMember = teamMember;
	}

	public Integer getCompanyId() {
		return companyId;
	}

	public void setCompanyId(Integer companyId) {
		this.companyId = companyId;
	}

	public UserList getTeamMemberUserList() {
		return teamMemberUserList;
	}

	public void setTeamMemberUserList(UserList teamMemberUserList) {
		this.teamMemberUserList = teamMemberUserList;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

}
