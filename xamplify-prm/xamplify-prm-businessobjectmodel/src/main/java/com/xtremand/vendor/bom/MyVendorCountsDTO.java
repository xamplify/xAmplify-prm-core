package com.xtremand.vendor.bom;

import java.io.Serializable;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;

@Data
public class MyVendorCountsDTO implements Comparable<MyVendorCountsDTO>, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4146909896982302246L;

	private String name;

	@Getter(value = AccessLevel.NONE)
	private Integer count;

	@Override
	public int compareTo(MyVendorCountsDTO myVendorsCountDTO) {
		return this.getCount().compareTo(((MyVendorCountsDTO) myVendorsCountDTO).getCount());
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public Integer getCount() {
		return count!=null ? count:0;
	}

}
