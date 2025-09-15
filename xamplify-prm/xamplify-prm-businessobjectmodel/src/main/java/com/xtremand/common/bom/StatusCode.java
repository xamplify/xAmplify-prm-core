package com.xtremand.common.bom;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Immutable;

@Entity
@Immutable
@Table(name="xt_system_statuscode")
@Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
public class StatusCode {
	@Id
	@GeneratedValue
	@Column(name="id")
	private Integer id;

	@Column(name="statuscode")
	private Integer code;
	
	@Column(name="message")
	private String message;
	
	static  Map<Integer, StatusCode> statusCodesMap = new HashMap<>(0);

	
	public static void initialize(List<StatusCode> statusCodes){
		for(StatusCode statusCode : statusCodes){
			statusCodesMap.put(statusCode.getCode(), statusCode);
		}
	}
	
	public static  StatusCode getstatuscode(Integer statuscode){
		return statusCodesMap.get(statuscode);
	}


	public Integer getId() {
		return id;
	}


	public void setId(Integer id) {
		this.id = id;
	}


	public Integer getCode() {
		return code;
	}


	public void setCode(Integer code) {
		this.code = code;
	}


	public String getMessage() {
		return message;
	}


	public void setMessage(String message) {
		this.message = message;
	}

	public StatusCode() {
	}

	public StatusCode(Integer id, Integer code, String message) {
		super();
		this.id = id;
		this.code = code;
		this.message = message;
	}
	
}
