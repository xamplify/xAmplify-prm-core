package com.xtremand.common.bom;

import javax.persistence.Transient;

/**
 * Criteria contains the search information and can be passed from UI 
 * @author Ramesh
 * @since 8/16/2016
 * @version 1.0
 */
public class Criteria {
	private String property;
	private Object value1;
	private Object value2;
	private Object[] value3;
	OPERATION_NAME operationName;
	
	@Transient
	private String operation;

	public enum OPERATION_NAME{
		eq, ne,like,ilike,gt,ge,lt,le,isNull,isNotNull,isEmpty,isNotEmpty,between, in
	}
	
	public Criteria(){
		
	}
	
	public static OPERATION_NAME getOperationNameEnum(String operation) {
		switch (operation) {
		case "eq":
			return OPERATION_NAME.eq;
		case "like":
			return OPERATION_NAME.like;
		case "gt":
			return OPERATION_NAME.gt;
		case "lt":
			return OPERATION_NAME.lt;
		default:
			return null;
		}
	}
	
	public Criteria(String property, OPERATION_NAME operationName){
		super();
		this.property = property;
		this.operationName = operationName;
	}
	
	public Criteria(String property, OPERATION_NAME operationName, Object value1) {
		super();
		this.property = property;
		this.value1 = value1;
		this.operationName = operationName;
	}

	public Criteria(String property, OPERATION_NAME operationName, Object value1, Object value2) {
		super();
		this.property = property;
		this.value1 = value1;
		this.value2 = value2;
		this.operationName = operationName;
	}
	
	public Criteria(String property, OPERATION_NAME operationName, Object[] value3) {
		super();
		this.property = property;
		this.value3 = value3;
		this.operationName = operationName;
	}

	public OPERATION_NAME getOperationName() {
		return operationName;
	}

	public void setOperationName(OPERATION_NAME operationName) {
		this.operationName = operationName;
	}

	public String getProperty() {
		return property;
	}

	public void setProperty(String property) {
		this.property = property;
	}

	public Object getValue1() {
		return value1;
	}

	public void setValue1(Object value1) {
		this.value1 = value1;
	}

	public Object getValue2() {
		return value2;
	}

	public void setValue2(Object value2) {
		this.value2 = value2;
	}

	public Object[] getValue3() {
		return value3;
	}

	public void setValue3(Object[] value3) {
		this.value3 = value3;
	}

	public String getOperation() {
		return operation;
	}

	public void setOperation(String operation) {
		this.operation = operation;
	}
}