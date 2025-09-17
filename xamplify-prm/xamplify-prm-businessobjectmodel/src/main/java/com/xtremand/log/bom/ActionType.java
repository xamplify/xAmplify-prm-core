package com.xtremand.log.bom;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name="xt_action_type")
@Entity
public class ActionType {
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name="action_id")
	private Integer actionId;
	@Column(name="action_type")
	private String actionType;
	@Column(name="action_name")
	private String actionName;
	public Integer getActionId() {
		return actionId;
	}
	public void setActionId(Integer actionId) {
		this.actionId = actionId;
	}
	public String getActionType() {
		return actionType;
	}
	public void setActionType(String actionType) {
		this.actionType = actionType;
	}
	public String getActionName() {
		return actionName;
	}
	public void setActionName(String actionName) {
		this.actionName = actionName;
	}
}
