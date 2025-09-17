package com.xtremand.video.service;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

import com.xtremand.common.bom.CompanyProfile;
import com.xtremand.common.bom.Criteria;
import com.xtremand.common.bom.FindLevel;
import com.xtremand.common.bom.Pagination;
import com.xtremand.dam.dto.DamUploadPostDTO;
import com.xtremand.formbeans.VideoFileDTO;
import com.xtremand.formbeans.XtremandResponse;
import com.xtremand.user.bom.User;
import com.xtremand.video.bom.CloudContent;
import com.xtremand.video.bom.VideoCategory;
import com.xtremand.video.bom.VideoDefaultSettings;
import com.xtremand.video.bom.VideoFile;
import com.xtremand.video.exception.VideoDataAccessException;
import com.xtremand.video.formbeans.VideoFileUploadForm;
import com.xtremand.videoencoding.service.FFMPEGStatus;

public interface VideoService {
	public VideoFile findByPrimaryKey(Serializable pk, FindLevel[] levels) throws VideoDataAccessException;

	public Collection<VideoFile> find(List<Criteria> criterias, FindLevel[] levels) throws VideoDataAccessException;

	public XtremandResponse saveVideo(MultipartFile file, Integer userId) throws VideoDataAccessException;

	public VideoFileUploadForm processVideo(String videoFilePath, Integer userId, FFMPEGStatus status);

	public VideoFile updateVideo(final VideoFileUploadForm videoFileUploadForm, Integer userId)
			throws VideoDataAccessException;

	public Map<String, List<?>> getVideos(User user) throws VideoDataAccessException;

	public VideoFileDTO getVideoByAlias(Integer userId, String alias, VideoFile.TYPE viewBy, String companyProfileName)
			throws VideoDataAccessException;

	public VideoFileDTO getVideo(List<Criteria> criterias, VideoFile.TYPE viewBy) throws VideoDataAccessException;

	public List<VideoCategory> getCategories() throws VideoDataAccessException;

	public Map<String, Object> listVideos(Integer userId, Pagination pagination, Integer categoryId)
			throws VideoDataAccessException;

	public XtremandResponse statusChange(Integer videoId, Integer userId) throws VideoDataAccessException;

	public XtremandResponse saveVideo(String downloadLink, String fileName, String oauthToken, Integer userId) throws VideoDataAccessException;

	public XtremandResponse saveRecordedVideo(MultipartFile file, Integer userId);

	public String uploadOwnThumbnail(MultipartFile imageFile, Integer userId) throws VideoDataAccessException;

	public String uploadBrandingLogo(MultipartFile imageFile, Integer userId, boolean videoDefaultSetting);

	public Map saveBrandingLogo(String brandingLogoPath, String brandingLogoDescUri, Integer userId);

	public List<String> getVideoTitles(Integer userId) throws VideoDataAccessException;

	public void updateVideoDefaultSettings(CompanyProfile companyProfile, VideoDefaultSettings videoDefaultSettings)
			throws VideoDataAccessException;

	public VideoDefaultSettings getVideoDefaultSettings(Integer companyId) throws VideoDataAccessException;

	public Integer getVideosCount(Integer userId) throws VideoDataAccessException;

	public Integer getVideosViewsCount(Integer userId) throws VideoDataAccessException;

	public Map<String, Object> getMonthWiseCountryWiseVideoViews(String alias) throws VideoDataAccessException;

	public Map<String, Object> getWatchedFullyMinutesWatchedVideoViews(String alias) throws VideoDataAccessException;

	public Map<String, Object> getVideoReportData(User user, Pagination pagination, Integer categoryId)
			throws VideoDataAccessException;

	public VideoFileDTO getVideoFileDTO(VideoFile videoFile, Integer userId);

	public Map<String, Object> getChannelVideos(Pagination pagination, Integer categoryId, Integer userId);

	public Integer getChannelVideosViewsCount(Integer userId);

	public List<VideoFileDTO> listPublicVideosByCompany(Integer companyId);

	public XtremandResponse uploadFile(List<CloudContent> files, Integer userId);

	public XtremandResponse hasVideoAccess(Integer userId);

	public Map<String, Object> loadVideos(Integer userId, Pagination pagination, Integer categoryId);

	public Map<String, Object> processVideo1(String videoFilePath, DamUploadPostDTO damUploadPostDTO,
			MultipartFile thumbnailFile, List<String> tags);

	public String getVideoPath(Integer videoId);

	public List<Integer> getChannelVideosIds(Pagination pagination, Integer categoryId, Integer userId);

	public VideoFileDTO getVideoById(Integer userId, Integer videoId, VideoFile.TYPE viewBy)
			throws VideoDataAccessException;

	public VideoFile findByAlias(String alias);

	public XtremandResponse findVideos(Pagination pagination);

	public XtremandResponse findVideoFileByDamIdAndUserId(Integer id, Integer userId);
}
