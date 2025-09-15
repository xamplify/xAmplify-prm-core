package com.xtremand.util.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class ActiveThreadsDTO implements Serializable {
	/**
	* 
	*/
	private static final long serialVersionUID = -5935971065898604121L;

	private String threadName;

	private String threadGroupName;

	private boolean alive;

	private boolean background;

	private boolean threadPoolThread;

	private long threadId;

	private int activeCount;
	
	private int priority;

}
