package com.xtremand.common.bom;

import java.io.Serializable;

public interface Template extends Serializable{
	String getBody();
	Integer getId();
	String getSubject();
}
