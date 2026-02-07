package com.xtremand.video.dao;

import java.util.List;
import java.util.Map;

import com.xtremand.common.bom.Criteria;
import com.xtremand.common.bom.Pagination;
import com.xtremand.dao.util.FinderDAO;
import com.xtremand.video.bom.VideoCategory;
import com.xtremand.video.bom.VideoDefaultSettings;
import com.xtremand.video.bom.VideoFile;
import com.xtremand.video.exception.VideoDataAccessException;

public interface VideoDao extends FinderDAO<VideoFile> {
	public List<String> getViewByOptions(Integer userId);


	public Integer getVideoUsedLength(List<Integer> subAdminUserIds, Integer userId);

	public void deleteByPrimaryKey(Integer id);

	public List<String> getVideoTitles(Integer[] userIdArray);

	public VideoDefaultSettings getVideoDefaultSettings(Integer companyId);

	public Integer getVideosCount(Integer[] userIdArray);

	public Integer getVideosViewsCount(Integer[] userIdArray);

	public Integer getChannelVideosViewsCount(Integer[] videoIdArray);

	public Integer monthWiseVideoViewsCount(Integer videoId, String startDateOfMonth, String endDateOfMonth);

	public List countryWiseVideoViewsCount(Integer videoId) throws VideoDataAccessException;

	public Integer watchedFullyVideoViewsCount(Integer videoId) throws VideoDataAccessException;

	public List<Integer> getChannelVideos(Integer userId, List<Integer> companyIds, boolean isPartner,
			Integer loggedInUsercompanyId) throws VideoDataAccessException;

	public List<Object[]> listPublicVideosByCompany(Integer[] userIdArray);

	public Map<String, Object> listVideos(List<Criteria> criterias, Pagination pagination);

	public List<VideoCategory> listVideoCategories(Integer companyId);

	public List<VideoCategory> listVideoCategories(List<Integer> videoIds);

	public String getVideoPath(Integer videoId);

	public Integer getVideoTitleCount(String title, Integer companyId);

	public VideoFile findByAlias(String alias);

	public Map<String, Object> findVideos(Pagination pagination);

	public List<String> getTagNamesByVideoId(Integer videoId);

	public void deleteVideoRecordsByIds(List<Integer> ids);
}
