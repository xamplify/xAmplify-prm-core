package com.xtremand.util;

import java.util.List;

public class BarChartResponseDTO {
	private List<String> dates;
	private List<Integer> views;

	/**
	 * @return the views
	 */
	public List<Integer> getViews() {
		return views;
	}

	/**
	 * @param views
	 *            the views to set
	 */
	public void setViews(List<Integer> views) {
		this.views = views;
	}

	/**
	 * @return the dates
	 */
	public List<String> getDates() {
		return dates;
	}

	/**
	 * @param dates
	 *            the dates to set
	 */
	public void setDates(List<String> dates) {
		this.dates = dates;
	}

}
