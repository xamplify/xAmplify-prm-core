package com.xtremand.dashboard.buttons.dto;

import java.io.Serializable;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class DashboardAlternateUrlDTO implements Serializable{

	private String name;
	private String id;
}
