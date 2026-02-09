package com.xtremand.video.service.impl;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.math.BigInteger;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.text.SimpleDateFormat;
import java.time.Year;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpSession;

import org.apache.commons.collections.MultiMap;
import org.apache.commons.collections.map.MultiValueMap;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.jcodec.api.FrameGrab;
import org.jcodec.api.JCodecException;
import org.jcodec.common.model.ColorSpace;
import org.jcodec.common.model.Picture;
import org.jcodec.scale.ColorUtil;
import org.jcodec.scale.Transform;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.xtremand.approve.dao.ApproveDAO;
import com.xtremand.aws.AmazonWebModel;
import com.xtremand.aws.AmazonWebService;
import com.xtremand.category.bom.CategoryModuleEnum;
import com.xtremand.category.dao.CategoryDao;
import com.xtremand.common.bom.CompanyProfile;
import com.xtremand.common.bom.Criteria;
import com.xtremand.common.bom.Criteria.OPERATION_NAME;
import com.xtremand.common.bom.FindLevel;
import com.xtremand.common.bom.Pagination;
import com.xtremand.dam.dao.DamDao;
import com.xtremand.dam.dto.DamUploadPostDTO;
import com.xtremand.dao.util.GenericDAO;
import com.xtremand.formbeans.VideoFileDTO;
import com.xtremand.formbeans.XtremandResponse;
import com.xtremand.log.dao.XtremandLogDAO;
import com.xtremand.log.service.XamplifyLogService;
import com.xtremand.mail.service.AsyncComponent;
import com.xtremand.mail.service.EmailConstants;
import com.xtremand.mail.service.MailService;
import com.xtremand.partnership.bom.Partnership;
import com.xtremand.partnership.dao.PartnershipDAO;
import com.xtremand.user.bom.Role;
import com.xtremand.user.bom.User;
import com.xtremand.user.dao.UserDAO;
import com.xtremand.user.service.UserService;
import com.xtremand.util.CustomValidatonException;
import com.xtremand.util.DateUtils;
import com.xtremand.util.XamplifyUtils;
import com.xtremand.util.dao.HibernateSQLQueryResultUtilDao;
import com.xtremand.util.dao.UtilDao;
import com.xtremand.util.dto.HibernateSQLQueryResultRequestDTO;
import com.xtremand.util.dto.QueryParameterDTO;
import com.xtremand.util.dto.VideoDTO;
import com.xtremand.util.service.UtilService;
import com.xtremand.video.bom.CallAction;
import com.xtremand.video.bom.CloudContent;
import com.xtremand.video.bom.VideoCategory;
import com.xtremand.video.bom.VideoControl;
import com.xtremand.video.bom.VideoDefaultSettings;
import com.xtremand.video.bom.VideoDetails;
import com.xtremand.video.bom.VideoFile;
import com.xtremand.video.bom.VideoFile.TYPE;
import com.xtremand.video.bom.VideoFile.VideoStatus;
import com.xtremand.video.bom.VideoImage;
import com.xtremand.video.bom.VideoTag;
import com.xtremand.video.dao.VideoDao;
import com.xtremand.video.exception.VideoDataAccessException;
import com.xtremand.video.formbeans.VideoFileUploadForm;
import com.xtremand.video.service.VideoService;
import com.xtremand.videoencoding.service.FFMPEGStatus;
import com.xtremand.videoencoding.service.VideoBitRateConverter;
import com.xtremand.videoencoding.service.VideoEncoder;
import com.xtremand.white.labeled.dao.WhiteLabeledAssetDao;
import com.xtremand.white.labeled.dto.DamVideoDTO;
import com.xtremand.white.labeled.dto.WhiteLabeledContentDTO;

@Service("videoService")
@Transactional
public class VideoServiceImpl implements VideoService {

	private static final Logger logger = LoggerFactory.getLogger(VideoServiceImpl.class);

	@Autowired
	private AmazonWebService amazonWebService;

	@Autowired
	private UserService userService;

	@Autowired
	XamplifyLogService xamplifyLogService;

	@Autowired
	VideoEncoder videoEncoder;

	@Autowired
	VideoDao videoDAO;

	@Autowired
	GenericDAO genericDao;

	@Autowired
	VideoBitRateConverter videoBitRateConverter;

	@Autowired
	private UserDAO userDao;

	@Autowired
	private CategoryDao categoryDao;

	@Autowired
	PartnershipDAO partnershipDAO;

	@Autowired
	XtremandLogDAO xtremandLogDAO;

        @Autowired
        private UtilService utilService;

        @Autowired
        private UtilDao utilDao;

        @Autowired
        private DamDao damDao;

	@Autowired
	@Lazy
	private AsyncComponent asyncComponent;

	@Autowired
	private WhiteLabeledAssetDao whiteLabeledAssetDao;

	@Autowired
	private ApproveDAO approveDao;

	@Autowired
	private HibernateSQLQueryResultUtilDao hibernateSQLQueryResultUtilDao;

	@Value("${upload_content_path}")
	String uploadContentPath;

	@Value("${files.uploaded.successfully}")
	String filesUploadedSuccessfully;

	@Value("${videos_path}")
	String videos_path;

	@Value("${server_path}")
	String server_path;

	@Value("${images_path}")
	String images_path;

	@Value("${separator}")
	String sep;

	@Value("${specialCharacters}")
	String regex;

	@Value("${outFiles}")
	String outPutFormat;

	@Value("${video.file.types}")
	String videoFileTypesString;

	@Value("${file.type.error}")
	String fileTypeError;

	@Value("${default.player.color}")
	private String defaultPlayerColor;

	@Value("${default.controller.color}")
	private String defaultControllerColor;

	@Value("${tooltip.disabled.white.label}")
	private String whiteLabeledDisabledToolTipMessage;

	@Value("${tooltip.white.label}")
	private String whiteLabeledToolTipMessage;

	@Autowired
	private MailService mailService;

	int imageWidth = 1280;
	int imageHeight = 720;

	int thumbnailImageWidth = 640;
	int thumbnailImageHeight = 360;

	private static final String CREATED_FOR_COMPANY = "createdForCompany";

	public XtremandResponse saveVideo(MultipartFile file, Integer userId) throws VideoDataAccessException {
		XtremandResponse response = new XtremandResponse();
		try {
			if (utilService.hasVideoAccess(userId)) {
				User user = genericDao.get(User.class, userId);
				final String path = new SimpleDateFormat("ddMMyyyy").format(new Date());

				String videoPath = videos_path + userId + sep + path;
				File videoDir = new File(videoPath);

				if (!videoDir.exists()) {
					videoDir.mkdirs();
				}
				String imagesRealPath = images_path + userId + sep + path;
				File imagesDir = new File(imagesRealPath);

				if (!imagesDir.exists()) {
					imagesDir.mkdirs();
				}
				String filePath = sep + file.getOriginalFilename().replaceAll(regex, "");

				String suffix = filePath.substring(filePath.lastIndexOf("."));
				String prefix = filePath.substring(0, filePath.lastIndexOf("."));
				filePath = prefix + String.valueOf(System.currentTimeMillis()) + suffix;

				File newFile = new File(videoPath + filePath);

				if (!newFile.exists()) {
					FileOutputStream fileOutSt = new FileOutputStream(newFile);
					fileOutSt.write(file.getBytes());
					fileOutSt.flush();
					fileOutSt.close();
				}

				String title = file.getOriginalFilename().replaceAll(regex, "");
				mailService.sendVideoUploadedMail(user, EmailConstants.VIDEO_UPLOADED,
						title.substring(0, title.lastIndexOf(".")));
				response.setData(userId + sep + path + filePath + "~" + title.substring(0, title.lastIndexOf(".")));
				response.setAccess(true);
			} else {
				response.setAccess(false);
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
			throw new VideoDataAccessException(e.getMessage());
		}
		return response;
	}

	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public VideoFileUploadForm processVideo(String videoFilePath, Integer userId, FFMPEGStatus status) {
		final String path = new SimpleDateFormat("ddMMyyyy").format(new Date());
		final User user = userService.findByPrimaryKey(userId,
				new com.xtremand.common.bom.FindLevel[] { FindLevel.COMPANY_PROFILE });
		VideoFileUploadForm form = new VideoFileUploadForm();
		VideoFile video = new VideoFile();
		try {
			String filePath = videoFilePath.split("~")[0];
			form.setVideoPath(videos_path + filePath);
			form = videoEncoder.encodeVideoFile(status, form);

			File file = new File(form.getVideoPath());

			video.setUri(form.getVideoPath().substring(form.getVideoPath().indexOf("videos")));
			video.setVideoSize(Double.parseDouble(String.valueOf(file.length())) / (1024 * 1024));
			int length = form.getVideoLength();
			video.setVideoLength(timeConversion(length));

			form.setTitle(videoFilePath.split("~")[1]);
			video.setViewBy(TYPE.DRAFT);
			video.setCustomer(user);

			String error = "";

			if ("".equals(error)) {
				List<String> values = new ArrayList<>();
				values.add(VideoFile.TYPE.PRIVATE.name());
				values.add(VideoFile.TYPE.PUBLIC.name());
				form.setViewByOptions(values);

				List<String> imageFiles = generateImages(user, path, file, length, form.getBitRate(), form.getWidth());
				form.setImageFiles(imageFiles);
				video.setVideoID(form.getTitle());
				video.setTitle(form.getTitle());
				video.setCategory(genericDao.get(VideoCategory.class, 108));
				video.setBitrate(1.2);
				video.setViews(0);
				video.setVideoStatus(VideoStatus.APPROVED);
				video.setAlias(UUID.randomUUID().toString());
				video.setDescription(form.getTitle());
				String firstImageFile = form.getImageFiles().get(0);
				video.setImageUri(firstImageFile.replaceFirst(server_path, ""));
				String firstGifFile = form.getGifFiles().get(0);
				video.setGifUri(firstGifFile.replaceFirst(server_path, ""));
				video.initialiseCommonFields(true, user.getUserId());
				VideoImage videoImage = new VideoImage(form.getImageFiles().get(0).replaceFirst(server_path, ""),
						form.getImageFiles().get(1).replaceFirst(server_path, ""),
						form.getImageFiles().get(2).replaceFirst(server_path, ""),
						form.getGifFiles().get(0).replaceFirst(server_path, ""),
						form.getGifFiles().get(1).replaceFirst(server_path, ""),
						form.getGifFiles().get(2).replaceFirst(server_path, ""));
				video.setVideoImage(videoImage);
				videoImage.setVideoFile(video);

				form.setCategoryId(1);
				form.setViewBy(VideoFile.TYPE.DRAFT);
				form.setImagePath(form.getImageFiles().get(0));
				form.setGifImagePath(form.getGifFiles().get(0));

				VideoDefaultSettings videoDefaultSettings = getVideoDefaultSettings(user.getCompanyProfile().getId());
				form.setPlayerColor(videoDefaultSettings.getPlayerColor());
				form.setEnableVideoController(videoDefaultSettings.isEnableVideoController());
				form.setControllerColor(videoDefaultSettings.getControllerColor());
				form.setAllowSharing(videoDefaultSettings.isAllowSharing());
				form.setEnableSettings(videoDefaultSettings.isEnableSettings());
				form.setAllowFullscreen(videoDefaultSettings.isAllowFullscreen());
				form.setAllowComments(videoDefaultSettings.isAllowComments());
				form.setAllowLikes(videoDefaultSettings.isAllowLikes());
				form.setEnableCasting(videoDefaultSettings.isEnableCasting());
				form.setAllowEmbed(videoDefaultSettings.isAllowEmbed());
				form.setTransparency(videoDefaultSettings.getTransparency());
				form.setIs360video(videoDefaultSettings.isIs360video());
				form.setBrandingLogoUri(videoDefaultSettings.getBrandingLogoUri());
				form.setBrandingLogoDescUri(videoDefaultSettings.getBrandingLogoDescUri());
				form.setStartOfVideo(true);
				form.setEndOfVideo(false);
				form.setCallACtion(false);
				form.setName(true);
				form.setSkip(true);
				form.setDefaultSetting(true);
				form.setEnableVideoCobrandingLogo(true);
				video.setProcessed(false);
				genericDao.save(video);
				form.setId(video.getId());
				form.setProcessed(false);
			} else {
				form.setError(error);
			}
			status.setStatus("Video processing complete!");

		} catch (Exception e) {
			e.printStackTrace();
			form.setError("Error occured : " + e.getMessage());
		}
		form.setVideoPath(server_path + video.getUri());
		return form;
	}

	private static String timeConversion(int totalSeconds) {

		final int MINUTES_IN_AN_HOUR = 60;
		final int SECONDS_IN_A_MINUTE = 60;

		int seconds = totalSeconds % SECONDS_IN_A_MINUTE;
		int totalMinutes = totalSeconds / SECONDS_IN_A_MINUTE;
		int minutes = totalMinutes % MINUTES_IN_AN_HOUR;
		int hours = totalMinutes / MINUTES_IN_AN_HOUR;

		return addZeros(hours) + ":" + addZeros(minutes) + ":" + addZeros(seconds);
	}

	private static String addZeros(int val) {
		return String.valueOf(val).length() == 1 ? "0" + String.valueOf(val) : String.valueOf(val);
	}

	private List<String> generateImages(User user, String path, File file, int length, int bitRate, int width)
			throws IOException, JCodecException {
		String imagesRealPath = images_path + user.getUserId() + sep + path;
		String imagesSerPath = server_path + "images" + sep + user.getUserId() + sep + path;

		List<String> files = new ArrayList<>();
		String iPath = String.valueOf(System.currentTimeMillis());

		for (int i = length / 3; i <= length; i = i + (length / 3)) {
			try {
				Picture frame = FrameGrab.getNativeFrame(file, i);
				Transform transform = ColorUtil.getTransform(frame.getColor(), ColorSpace.RGB);
				Picture rgb = Picture.create(frame.getWidth(), frame.getHeight(), ColorSpace.RGB);
				transform.transform(frame, rgb);
				BufferedImage dst = new BufferedImage(frame.getWidth(), frame.getHeight(),
						BufferedImage.TYPE_3BYTE_BGR);
				toBufferedImage(rgb, dst);

				File image = new File(imagesRealPath + sep + iPath + "_" + i + ".jpg");

				File bigImage = new File(imagesRealPath + sep + iPath + "_" + i + "_big.jpg");

				if (width < bitRate) {
					ImageIO.write(resizeImageWithHint(dst, width, bitRate), "jpg", image);
					ImageIO.write(resizeImageWithHint(dst, width, bitRate), "jpg", bigImage);
				} else {
					ImageIO.write(resizeImageWithHint(dst, imageWidth, imageHeight), "jpg", image);
					ImageIO.write(resizeImageWithHint(dst, thumbnailImageWidth, thumbnailImageHeight), "jpg", bigImage);
				}

				files.add(imagesSerPath + sep + iPath + "_" + i + ".jpg");
			} catch (Exception e) {
				logger.info(e.getMessage());
			}
		}
		return files;
	}

	public static void toBufferedImage(Picture src, BufferedImage dst) {
		byte[] data = ((DataBufferByte) dst.getRaster().getDataBuffer()).getData();
		int[] srcData = src.getPlaneData(0);
		for (int i = 0; i < data.length; i++) {
			data[i] = (byte) srcData[i];
		}
	}

	private BufferedImage resizeImageWithHint(BufferedImage originalImage, int width, int height) {

		BufferedImage resizedImage = new BufferedImage(width, height, originalImage.getType());
		Graphics2D g = resizedImage.createGraphics();
		g.drawImage(originalImage, 0, 0, width, height, null);
		g.dispose();
		g.setComposite(AlphaComposite.Src);

		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		return resizedImage;
	}

	@Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
	public VideoFile updateVideo(final VideoFileUploadForm videoFileUploadForm, Integer userId)
			throws VideoDataAccessException {
		logger.debug("save video called");

		final VideoFile video = videoDAO.findByPrimaryKey(videoFileUploadForm.getId(), new FindLevel[] {
				FindLevel.VIDEO_PATH, FindLevel.VIDEO_CONTROL, FindLevel.CALL_ACTION, FindLevel.VIDEO_TAGS });

		video.setTitle(videoFileUploadForm.getTitle());
		video.setVideoID(videoFileUploadForm.getTitle());
		video.setViewBy(videoFileUploadForm.getViewBy());
		video.setDescription(videoFileUploadForm.getDescription());
		User user = userService.loadUser(Arrays.asList(new Criteria("userId", OPERATION_NAME.eq, userId)),
				new FindLevel[] { FindLevel.SHALLOW });
		final String message = "";
		if (message.equals("")) {

			video.setCategory(genericDao.get(VideoCategory.class, videoFileUploadForm.getCategoryId()));

			try {
				setImageFiles(videoFileUploadForm, video);
			} catch (IOException e) {
				throw new VideoDataAccessException(e.getMessage());
			}

			setVideoTags(videoFileUploadForm.getTags(), video);
			if (!XamplifyUtils.isValidInteger(videoFileUploadForm.getTransparency())) {
				videoFileUploadForm.setTransparency(100);
			}
			if (video.getVideoControl() == null) {
				VideoControl videoControl = new VideoControl(videoFileUploadForm.getPlayerColor(),
						videoFileUploadForm.isEnableVideoController(), videoFileUploadForm.getControllerColor(),
						videoFileUploadForm.isAllowSharing(), videoFileUploadForm.isEnableSettings(),
						videoFileUploadForm.isAllowFullscreen(), videoFileUploadForm.isAllowComments(),
						videoFileUploadForm.isAllowLikes(), videoFileUploadForm.isEnableCasting(),
						videoFileUploadForm.isAllowEmbed(), videoFileUploadForm.getTransparency(),
						videoFileUploadForm.isIs360video(), videoFileUploadForm.isDefaultSetting(),
						videoFileUploadForm.getBrandingLogoUri(), videoFileUploadForm.getBrandingLogoDescUri(),
						videoFileUploadForm.isEnableVideoCobrandingLogo());
				video.setVideoControl(videoControl);
				videoControl.setVideoFile(video);
			} else if (video.getVideoControl() != null) {
				VideoControl exvideoControl = video.getVideoControl();
				exvideoControl.setPlayerColor(videoFileUploadForm.getPlayerColor());
				exvideoControl.setEnableVideoController(videoFileUploadForm.isEnableVideoController());
				exvideoControl.setControllerColor(videoFileUploadForm.getControllerColor());
				exvideoControl.setAllowSharing(videoFileUploadForm.isAllowSharing());
				exvideoControl.setEnableSettings(videoFileUploadForm.isEnableSettings());
				exvideoControl.setAllowFullscreen(videoFileUploadForm.isAllowFullscreen());
				exvideoControl.setAllowComments(videoFileUploadForm.isAllowComments());
				exvideoControl.setAllowLikes(videoFileUploadForm.isAllowLikes());
				exvideoControl.setEnableCasting(videoFileUploadForm.isEnableCasting());
				exvideoControl.setAllowEmbed(videoFileUploadForm.isAllowEmbed());
				exvideoControl.setIs360video(videoFileUploadForm.isIs360video());
				exvideoControl.setTransparency(videoFileUploadForm.getTransparency());
				exvideoControl.setDefaultSetting(videoFileUploadForm.isDefaultSetting());
				exvideoControl.setBrandingLogoUri(videoFileUploadForm.getBrandingLogoUri());
				exvideoControl.setBrandingLogoDescUri(videoFileUploadForm.getBrandingLogoDescUri());
				exvideoControl.setEnableVideoCobrandingLogo(videoFileUploadForm.isEnableVideoCobrandingLogo());
				video.setVideoControl(exvideoControl);
				exvideoControl.setVideoFile(video);
			}

			if (video.getCallAction() == null) {
				CallAction callAction = new CallAction(videoFileUploadForm.isName(), videoFileUploadForm.isSkip(),
						videoFileUploadForm.getUpperText(), videoFileUploadForm.getLowerText(),
						videoFileUploadForm.isStartOfVideo(), videoFileUploadForm.isEndOfVideo(),
						videoFileUploadForm.isCallACtion());
				video.setCallAction(callAction);
				callAction.setVideoFile(video);
			} else if (videoFileUploadForm.getAction().equalsIgnoreCase("update")) {
				CallAction excallAction = video.getCallAction();
				excallAction.setName(videoFileUploadForm.isName());
				excallAction.setSkip(videoFileUploadForm.isSkip());
				excallAction.setUpperText(videoFileUploadForm.getUpperText());
				excallAction.setLowerText(videoFileUploadForm.getLowerText());
				excallAction.setStartOfVideo(videoFileUploadForm.isStartOfVideo());
				excallAction.setEndOfVideo(videoFileUploadForm.isEndOfVideo());
				excallAction.setCallACtion(videoFileUploadForm.isCallACtion());
				video.setCallAction(excallAction);
				excallAction.setVideoFile(video);
			}

			video.initialiseCommonFields(false, user.getUserId());

			logger.info("Mobinar saved with id " + video.getVideoID() + " user " + user.getUserName());

		} else {
			logger.error("Error Occurred " + message);
			throw new VideoDataAccessException(message);
		}
		return video;
	}

	public VideoFileDTO getVideoFileDTO(VideoFile videoFile, Integer userId) {
		VideoFileDTO videoFileDTO = new VideoFileDTO();
		if (utilService.hasVideoAccess(userId)) {
			videoFileDTO.setId(videoFile.getId());
			videoFileDTO.setTitle(videoFile.getTitle());
			videoFileDTO.setUploadedBy(videoFile.getCustomer().getUserName());
			videoFileDTO.setUploadedDate((DateUtils.convertToOnlyDate(videoFile.getUpdatedTime())));
			videoFileDTO.setDescription(videoFile.getDescription());
			videoFileDTO.setVideoLength(videoFile.getVideoLength());
			videoFileDTO.setAlias(videoFile.getAlias());
			videoFileDTO.setVideoPath(videoFile.getFullVideoPath().replace(".mp4", ".m3u8"));
			videoFileDTO.setImagePath(videoFile.getFullImagePath());
			videoFileDTO.setCategoryId(videoFile.getCategory().getId());
			videoFileDTO.setPlayerColor(videoFile.getVideoControl().getPlayerColor());
			videoFileDTO.setControllerColor(videoFile.getVideoControl().getControllerColor());
			videoFileDTO.setEnableSettings(videoFile.getVideoControl().isEnableSettings());
			videoFileDTO.setAllowSharing(videoFile.getVideoControl().isAllowSharing());
			videoFileDTO.setAllowFullscreen(videoFile.getVideoControl().isAllowFullscreen());
			videoFileDTO.setAllowComments(videoFile.getVideoControl().isAllowComments());
			videoFileDTO.setAllowLikes(videoFile.getVideoControl().isAllowLikes());
			videoFileDTO.setEnableCasting(videoFile.getVideoControl().isEnableCasting());
			videoFileDTO.setAllowEmbed(videoFile.getVideoControl().isAllowEmbed());
			videoFileDTO.setName(videoFile.getCallAction().isName());
			videoFileDTO.setSkip(videoFile.getCallAction().isSkip());
			videoFileDTO.setEnableVideoController(videoFile.getVideoControl().isEnableVideoController());
			videoFileDTO.setAccess(true);
		} else {
			videoFileDTO.setAccess(false);
		}
		return videoFileDTO;
	}

	public void updateLeftAccountStats(VideoFile videoFile, HttpSession session) {
		@SuppressWarnings("unchecked")
		Map<String, Integer> stats = (Map<String, Integer>) session.getAttribute("accountStats");
		switch (videoFile.getViewBy()) {
		case PUBLIC:
			stats.put("publicMobinars", stats.get("publicMobinars") + 1);
			break;
		case PRIVATE:
			stats.put("privateMobinars", stats.get("privateMobinars") + 1);
			break;
		default:
			break;
		}
		stats.put("diskspace", stats.get("diskspace") + videoFile.getVideoSize().intValue());
		String[] length = videoFile.getVideoLength().split(":");
		int totalMin = stats.get("mobinarLength");
		if (!"00".equals(length[0])) {
			totalMin = totalMin + Integer.parseInt(length[0]) * 60;
		}
		if (!"00".equals(length[1])) {
			totalMin = totalMin + Integer.parseInt(length[1]);
		}
		if (!"00".equals(length[2])) {
			totalMin = totalMin + Integer.parseInt(length[0]) / 60;
		}
		stats.put("mobinarLength", +totalMin);
	}

	private void setImageFiles(VideoFileUploadForm videoFileUploadForm, VideoFile video) throws IOException {
		final String path = new SimpleDateFormat("ddMMyyyy").format(new Date());

		MultipartFile imageFile = videoFileUploadForm.getImageFile();

		if (imageFile != null && imageFile.getOriginalFilename() != null
				&& !imageFile.getOriginalFilename().trim().isEmpty()) {
			generateThumbnail(imageFile, video.getCustomer().getUserId());
			video.setImageUri("images" + sep + video.getCustomer().getUserId() + sep + path + sep
					+ imageFile.getOriginalFilename().replaceAll(regex, ""));
			video.setCustomThumbnailUploaded(true);
		} else if (videoFileUploadForm.getImagePath() != null && videoFileUploadForm.getImagePath().length() > 0) {
			video.setImageUri(videoFileUploadForm.getImagePath()
					.substring(videoFileUploadForm.getImagePath().indexOf("images" + sep)));
		}

		if (videoFileUploadForm.getGifImagePath() != null) {
			video.setGifUri(videoFileUploadForm.getGifImagePath()
					.substring(videoFileUploadForm.getGifImagePath().indexOf("images" + sep)));
		}
	}

	public void generateThumbnail(MultipartFile imageFile, Integer customerId) throws IOException {
		String path = new SimpleDateFormat("ddMMyyyy").format(new Date());
		if (imageFile != null && imageFile.getOriginalFilename() != null
				&& !imageFile.getOriginalFilename().trim().isEmpty()) {
			String imagePath = images_path + customerId + sep + path;
			String imageFilePath = imagePath + sep + imageFile.getOriginalFilename().replaceAll(regex, "");
			File newImageFile = new File(imageFilePath);
			BufferedImage bufferedImage = toBufferedImg(imageFile);
			String bigImageFilePath = imageFilePath.substring(0, imageFilePath.lastIndexOf(".")) + "_big.jpg";
			File bigImageFile = new File(bigImageFilePath);
			BufferedImage resizedBuffBigImage = resizeImageWithHint(bufferedImage, thumbnailImageWidth,
					thumbnailImageHeight);
			ImageIO.write(resizedBuffBigImage, "jpg", bigImageFile);
			BufferedImage resizedBuffImage = resizeImageWithHint(bufferedImage, imageWidth, imageHeight);
			ImageIO.write(resizedBuffImage, "jpg", newImageFile);
		}
	}

	public String uploadOwnThumbnail(MultipartFile imageFile, Integer userId) throws VideoDataAccessException {
		final String path = new SimpleDateFormat("ddMMyyyy").format(new Date());
		String ownThumbnailPath = null;
		try {
			if (imageFile != null && imageFile.getOriginalFilename() != null
					&& !imageFile.getOriginalFilename().trim().isEmpty()) {
				String imagePath = images_path + userId + sep + path;

				File imageDir = new File(imagePath);
				if (!imageDir.exists()) {
					imageDir.mkdirs();
				}

				String imageFilePath = imagePath + sep + imageFile.getOriginalFilename().replaceAll(regex, "");
				File newImageFile = new File(imageFilePath);

				BufferedImage bufferedImage = toBufferedImg(imageFile);

				String bigImageFilePath = imageFilePath.substring(0, imageFilePath.lastIndexOf(".")) + "_big.jpg";
				File bigImageFile = new File(bigImageFilePath);
				BufferedImage resizedBuffBigImage = resizeImageWithHint(bufferedImage, thumbnailImageWidth,
						thumbnailImageHeight);

				ImageIO.write(resizedBuffBigImage, "jpg", bigImageFile);

				BufferedImage resizedBuffImage = resizeImageWithHint(bufferedImage, imageWidth, imageHeight);

				ImageIO.write(resizedBuffImage, "jpg", newImageFile);

				ownThumbnailPath = "images" + sep + userId + sep + path + sep
						+ imageFile.getOriginalFilename().replaceAll(regex, "");

			}
		} catch (IOException e) {
			throw new VideoDataAccessException(e.getMessage());
		}
		return ownThumbnailPath;
	}

	@Override
	public String uploadBrandingLogo(MultipartFile imageFile, Integer userId, boolean videoDefaultSetting) {
		final String path = new SimpleDateFormat("ddMMyyyy").format(new Date());
		String brandingLogoPath = null;
		try {
			if (imageFile != null && imageFile.getOriginalFilename() != null
					&& !imageFile.getOriginalFilename().trim().isEmpty()) {
				String imagePath = images_path + userId + sep + path;
				File imageDir = new File(imagePath);
				if (!imageDir.exists()) {
					imageDir.mkdirs();
				}
				String imageFilePath = imagePath + sep + imageFile.getOriginalFilename().replaceAll(regex, "");
				File newImageFile = new File(imageFilePath);
				if (!newImageFile.exists()) {
					FileOutputStream fileOutputStream = new FileOutputStream(newImageFile);
					fileOutputStream.write(imageFile.getBytes());
					fileOutputStream.flush();
					fileOutputStream.close();
				}

				/*
				 * BufferedImage bufferedImage = toBufferedImg(imageFile); BufferedImage
				 * resizedBuffImage = resizeImageWithHint(bufferedImage, IMG_WIDTH, IMG_HEIGHT);
				 * ImageIO.write(resizedBuffImage, "jpg", newImageFile);
				 */
				brandingLogoPath = "images" + sep + userId + sep + path + sep
						+ imageFile.getOriginalFilename().replaceAll(regex, "");
				/*
				 * if(videoDefaultSetting) saveBrandingLogo(brandingLogoPath, userId);
				 */
			}
		} catch (IOException e) {
			throw new VideoDataAccessException(e.getMessage());
		} catch (Exception e) {
			throw new VideoDataAccessException(e.getMessage());
		}
		return brandingLogoPath;
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public Map saveBrandingLogo(String brandingLogoPath, String brandingLogoDescUri, Integer userId) {
		Map<String, Object> map = new HashMap<String, Object>();
		try {
			User user = userService.loadUser(Arrays.asList(new Criteria("userId", OPERATION_NAME.eq, userId)),
					new FindLevel[] { FindLevel.ROLES, FindLevel.COMPANY_PROFILE });
			if (user.getRoles().stream()
					.anyMatch((role) -> (Role.PRM_ROLE.getRoleId().equals(role.getRoleId())
							|| (role.getRoleId()) == Role.ALL_ROLES.getRoleId()
							|| (role.getRoleId()) == Role.COMPANY_PARTNER.getRoleId()))) {
				VideoDefaultSettings videoDefaultSettings = getVideoDefaultSettings(user.getCompanyProfile().getId());
				videoDefaultSettings.setBrandingLogoUri(brandingLogoPath);
				videoDefaultSettings.setBrandingLogoDescUri(brandingLogoDescUri);
			}

			map.put("brandingLogoPath", brandingLogoPath);
			map.put("brandingLogoDescUri", brandingLogoDescUri);
		} catch (Exception e) {
			logger.error("Error In saveBrandingLogo(" + brandingLogoPath + "," + brandingLogoDescUri + "," + userId
					+ ")" + e);
		}
		return map;
	}

	private BufferedImage toBufferedImg(MultipartFile img) throws IOException {

		BufferedImage in = ImageIO.read(img.getInputStream());

		BufferedImage newImage = new BufferedImage(in.getWidth(), in.getHeight(), BufferedImage.TYPE_3BYTE_BGR);

		Graphics2D g = newImage.createGraphics();
		g.drawImage(in, 0, 0, null);
		g.dispose();
		return newImage;
	}

	private void setVideoTags(List<String> tags, VideoFile video) {
		video.getVideoTags().clear();
		if (tags.size() > 0) {
			for (String tag : tags) {
				video.getVideoTags().add(new VideoTag(video.getId(), tag));
			}
		}
	}

	@Override
	public VideoFile findByPrimaryKey(Serializable pk, FindLevel[] levels) throws VideoDataAccessException {
		return videoDAO.findByPrimaryKey(pk, levels);
	}

	@Override
	public Collection<VideoFile> find(List<Criteria> criterias, FindLevel[] levels) throws VideoDataAccessException {
		return videoDAO.find(criterias, levels);
	}

	@Override
	public Map<String, List<?>> getVideos(User user) throws VideoDataAccessException {
		Criteria criteria = new Criteria("customer.userId", OPERATION_NAME.eq, user.getUserId());
		List<Criteria> criterias = Arrays.asList(criteria);
		Collection<VideoFile> videoFiles = videoDAO.find(criterias,
				new FindLevel[] { FindLevel.VIDEO_PATH, FindLevel.VIDEO_TAGS });
		Map<String, List<?>> resultMap = new HashMap<String, List<?>>();
		List<VideoFileDTO> updatedVideoFiles = new ArrayList<VideoFileDTO>(0);
		if (videoFiles != null && !videoFiles.isEmpty()) {
			Iterator<VideoFile> itr = videoFiles.iterator();
			while (itr.hasNext()) {
				VideoFile videoFile = itr.next();
				VideoFileDTO videoFileDTO = new VideoFileDTO();
				videoFileDTO.setId(videoFile.getId());
				videoFileDTO.setTitle(videoFile.getTitle());
				videoFileDTO.setUploadedBy(videoFile.getCustomer().getUserName());
				videoFileDTO.setUploadedDate((DateUtils.convertToOnlyDate(videoFile.getUpdatedTime())));
				videoFileDTO.setDescription(videoFile.getDescription());
				videoFileDTO.setVideoLength(videoFile.getVideoLength());
				videoFileDTO.setAlias(videoFile.getAlias());
				videoFileDTO.setVideoPath(videoFile.getFullVideoPath().replace(".mp4", ".m3u8"));
				videoFileDTO.setImagePath(videoFile.getFullImagePath());
				videoFileDTO.setCategoryId(videoFile.getCategory().getId());
				updatedVideoFiles.add(videoFileDTO);
			}
		}
		resultMap.put("listOfMobinars", updatedVideoFiles);
		resultMap.put("categories", getCategories());
		return resultMap;
	}

	@Override
	public VideoFileDTO getVideoByAlias(Integer userId, String alias, VideoFile.TYPE viewBy, String companyProfileName)
			throws VideoDataAccessException {
		VideoFileDTO videoFileDTO = null;
		boolean hasPartnerAccess = false;
		if (utilService.hasVideoAccess(userId) || hasPartnerAccess) {
			Criteria criteria = new Criteria("alias", OPERATION_NAME.eq, alias);
			List<Criteria> criterias = Arrays.asList(criteria);
			videoFileDTO = getVideo(criterias, viewBy);
			videoFileDTO.setAccess(true);
		} else {
			videoFileDTO = new VideoFileDTO();
			videoFileDTO.setAccess(false);
		}
		return videoFileDTO;
	}

	@Override
	public VideoFileDTO getVideo(List<Criteria> criterias, VideoFile.TYPE viewBy) throws VideoDataAccessException {
		Collection<VideoFile> videoFiles = null;

		videoFiles = videoDAO.find(criterias, new FindLevel[] { FindLevel.VIDEO_TAGS, FindLevel.VIDEO_PATH,
				FindLevel.VIDEO_IMAGE, FindLevel.VIDEO_CONTROL });

		VideoFileDTO videoFileDTO = null;
		if (videoFiles != null && !videoFiles.isEmpty()) {
			VideoFile videoFile = videoFiles.iterator().next();
			final User user = userService.findByPrimaryKey(videoFile.getCustomer().getUserId(),
					new FindLevel[] { FindLevel.COMPANY_PROFILE });
			VideoDefaultSettings videoDefaultSettings = getVideoDefaultSettings(user.getCompanyProfile().getId());
			videoFileDTO = new VideoFileDTO();
			videoFileDTO.setId(videoFile.getId());
			if (videoFile.getViewBy() == TYPE.DRAFT) {
				videoFileDTO.setCategoryId(1);
				videoFileDTO.setViewBy(VideoFile.TYPE.DRAFT.name());

				videoFileDTO.setPlayerColor(videoDefaultSettings.getPlayerColor());
				videoFileDTO.setEnableVideoController(videoDefaultSettings.isEnableVideoController());
				videoFileDTO.setControllerColor(videoDefaultSettings.getControllerColor());
				videoFileDTO.setAllowSharing(videoDefaultSettings.isAllowSharing());
				videoFileDTO.setEnableSettings(videoDefaultSettings.isEnableSettings());
				videoFileDTO.setAllowFullscreen(videoDefaultSettings.isAllowFullscreen());
				videoFileDTO.setAllowComments(videoDefaultSettings.isAllowComments());
				videoFileDTO.setAllowLikes(videoDefaultSettings.isAllowLikes());
				videoFileDTO.setEnableCasting(videoDefaultSettings.isEnableCasting());
				videoFileDTO.setAllowEmbed(videoDefaultSettings.isAllowEmbed());
				videoFileDTO.setTransparency(videoDefaultSettings.getTransparency());
				videoFileDTO.setIs360video(videoDefaultSettings.isIs360video());
				videoFileDTO.setBrandingLogoUri(user.getCompanyProfile().getCompanyLogoPath());
				videoFileDTO.setBrandingLogoDescUri(utilService.getWebsite(user.getCompanyProfile().getWebsite()));

				videoFileDTO.setName(true);
				videoFileDTO.setSkip(true);
				videoFileDTO.setStartOfVideo(true);
				videoFileDTO.setEndOfVideo(false);
				videoFileDTO.setCallACtion(false);
				videoFileDTO.setDefaultSetting(true);
				videoFileDTO.setEnableVideoCobrandingLogo(true);
			} else {
				videoFileDTO.setCategoryId(videoFile.getCategory().getId());
				videoFileDTO.setViewBy(videoFile.getViewBy().name());
				VideoControl videoControl = videoFile.getVideoControl();
				boolean defaultSetting = videoControl.isDefaultSetting();
				videoFileDTO.setPlayerColor(
						defaultSetting == true ? videoDefaultSettings.getPlayerColor() : videoControl.getPlayerColor());
				videoFileDTO.setEnableVideoController(
						defaultSetting == true ? videoDefaultSettings.isEnableVideoController()
								: videoControl.isEnableVideoController());
				videoFileDTO.setControllerColor(defaultSetting == true ? videoDefaultSettings.getControllerColor()
						: videoControl.getControllerColor());
				videoFileDTO.setAllowSharing(
						defaultSetting == true ? videoDefaultSettings.isAllowSharing() : videoControl.isAllowSharing());
				videoFileDTO.setEnableSettings(defaultSetting == true ? videoDefaultSettings.isEnableSettings()
						: videoControl.isEnableSettings());
				videoFileDTO.setAllowFullscreen(defaultSetting == true ? videoDefaultSettings.isAllowFullscreen()
						: videoControl.isAllowFullscreen());
				videoFileDTO.setAllowComments(defaultSetting == true ? videoDefaultSettings.isAllowComments()
						: videoControl.isAllowComments());
				videoFileDTO.setAllowLikes(
						defaultSetting == true ? videoDefaultSettings.isAllowLikes() : videoControl.isAllowLikes());
				videoFileDTO.setEnableCasting(defaultSetting == true ? videoDefaultSettings.isEnableCasting()
						: videoControl.isEnableCasting());
				videoFileDTO.setAllowEmbed(
						defaultSetting == true ? videoDefaultSettings.isAllowEmbed() : videoControl.isAllowEmbed());
				videoFileDTO.setTransparency(defaultSetting == true ? videoDefaultSettings.getTransparency()
						: videoControl.getTransparency());
				videoFileDTO.setIs360video(
						defaultSetting == true ? videoDefaultSettings.isIs360video() : videoControl.isIs360video());
				videoFileDTO.setDefaultSetting(defaultSetting);
				videoFileDTO.setEnableVideoCobrandingLogo(videoControl.isEnableVideoCobrandingLogo());
				CallAction callAction = videoFile.getCallAction();
				videoFileDTO.setName(callAction.isName());
				videoFileDTO.setSkip(callAction.isSkip());
				videoFileDTO.setUpperText(callAction.getUpperText());
				videoFileDTO.setLowerText(callAction.getLowerText());
				videoFileDTO.setStartOfVideo(callAction.isStartOfVideo());
				videoFileDTO.setEndOfVideo(callAction.isEndOfVideo());
				videoFileDTO.setCallACtion(callAction.isCallACtion());
				List<String> videoTagsList = new ArrayList<String>();
				for (VideoTag VideoTag : videoFile.getVideoTags()) {
					if (StringUtils.hasText(VideoTag.getTag())) {
						videoTagsList.add(VideoTag.getTag());
					}
				}
				videoFileDTO.setTags(videoTagsList);

				videoFileDTO.setBrandingLogoUri(defaultSetting == true ? user.getCompanyProfile().getCompanyLogoPath()
						: videoControl.getBrandingLogoUri());
				String brandingLogoDescUri = defaultSetting ? user.getCompanyProfile().getWebsite()
						: videoControl.getBrandingLogoDescUri();
				videoFileDTO.setBrandingLogoDescUri(utilService.getWebsite(brandingLogoDescUri));
			}
			List<String> videoTagsList = new ArrayList<String>();
			for (VideoTag VideoTag : videoFile.getVideoTags()) {
				videoTagsList.add(VideoTag.getTag());
			}
			videoFileDTO.setTags(videoTagsList);
			videoFileDTO.setCategory(videoFile.getCategory());
			videoFileDTO.setTitle(videoFile.getTitle());
			videoFileDTO.setGifImagePath(videoFile.getFullGifPath());
			videoFileDTO.setUploadedBy(videoFile.getCustomer().getUserName());
			videoFileDTO.setUploadedDate(DateUtils.convertToOnlyDate(videoFile.getCreatedTime()));
			videoFileDTO.setDescription(videoFile.getDescription());
			videoFileDTO.setVideoLength(videoFile.getVideoLength());
			videoFileDTO.setAlias(videoFile.getAlias());
			videoFileDTO.setVideoPath(videoFile.getFullVideoPath().replace(".mp4", ".m3u8"));
			videoFileDTO.setImagePath(videoFile.getFullImagePath());
			VideoImage videoImage = videoFile.getVideoImage();
			videoFileDTO.getImageFiles()
					.add(videoImage.getImage1() != null && videoImage.getImage1().length() > 0
							? server_path + "/" + videoImage.getImage1()
							: "");
			videoFileDTO.getImageFiles()
					.add(videoImage.getImage2() != null && videoImage.getImage2().length() > 0
							? server_path + "/" + videoImage.getImage2()
							: "");
			videoFileDTO.getImageFiles()
					.add(videoImage.getImage3() != null && videoImage.getImage3().length() > 0
							? server_path + "/" + videoImage.getImage3()
							: "");
			videoFileDTO.getGifFiles().add(server_path + "/" + videoImage.getGif1());
			videoFileDTO.getGifFiles().add(server_path + "/" + videoImage.getGif2());
			videoFileDTO.getGifFiles().add(server_path + "/" + videoImage.getGif3());
			videoFileDTO.setCategories(getCategories());
			videoFileDTO.setViews(videoFile.getViews());
			videoFileDTO.setUploadedUserName(XamplifyUtils.setDisplayName(videoFile.getCustomer()));
			videoFileDTO.setProcessed(videoFile.isProcessed());

			/******** XNFR-169 *********/
			Integer damId = damDao.getDamIdByVideoId(videoFile.getId());
			Integer folderId = categoryDao.getCategoryIdByType(damId, CategoryModuleEnum.DAM.name());
			videoFileDTO.setFolderId(folderId);

			/********** XNFR-255 *******/
			WhiteLabeledContentDTO whiteLabeledContentDTO = whiteLabeledAssetDao
					.findSharedByVendorCompanyNameByAssetId(damId);
			if (whiteLabeledContentDTO != null) {
				videoFileDTO.setWhiteLabeledAssetReceivedFromVendor(true);
				videoFileDTO.setWhiteLabeledAssetSharedByVendorCompanyName(
						whiteLabeledContentDTO.getWhiteLabeledContentSharedByVendorCompanyName());
			}
			/********** XNFR-255 *******/
		} else {
			throw new CustomValidatonException("Sorry! This video has been deleted.");
		}
		return videoFileDTO;
	}

	public List<VideoCategory> getCategories() {

		return genericDao.load(VideoCategory.class);

	}

	@SuppressWarnings("unchecked")
	private List<VideoCategory> getCategoriesByUser(Integer userId, VideoFile.TYPE type) {
		List<Integer> userIdList = userService.getCompanyUserIds(userId);
		Integer[] userIdArray = ArrayUtils.toObject(userIdList.stream().mapToInt(i -> i).toArray());

		List<Criteria> criterias = new ArrayList<Criteria>();
		Criteria criteria = new Criteria("customer.userId", OPERATION_NAME.in, userIdArray);
		criterias.add(criteria);
		if (type != null) {
			Criteria criteriaObject = new Criteria("viewBy", OPERATION_NAME.eq, type);
			criterias.add(criteriaObject);
		}

		Map<String, Object> resultMapObj = videoDAO.find(criterias, new FindLevel[] {}, null);
		List<VideoFile> videoFiles = (List<VideoFile>) resultMapObj.get("videoFiles");
		List<VideoCategory> categoriesList = new ArrayList<VideoCategory>();
		for (VideoFile videoFile : videoFiles) {
			categoriesList.add(videoFile.getCategory());
		}
		return categoriesList;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> listVideos(Integer userId, Pagination pagination, Integer categoryId)
			throws VideoDataAccessException {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		final User user = userService.findByPrimaryKey(userId, new FindLevel[] { FindLevel.COMPANY_PROFILE });
		try {
			List<Integer> userIdList = userService.getCompanyUserIds(userId);
			Integer[] userIdArray = ArrayUtils.toObject(userIdList.stream().mapToInt(i -> i).toArray());

			List<Criteria> criterias = new ArrayList<Criteria>();
			Criteria criteria = new Criteria("customer.userId", OPERATION_NAME.in, userIdArray);
			criterias.add(criteria);
			if (categoryId != 0) {
				Criteria criteriaObj = new Criteria("category.id", OPERATION_NAME.eq, categoryId);
				criterias.add(criteriaObj);
			}
			Map<String, Object> resultMapObj = videoDAO.find(criterias,
					new FindLevel[] { FindLevel.VIDEO_PATH, FindLevel.VIDEO_CONTROL }, pagination);

			List<VideoFileDTO> updatedVideoFiles = new ArrayList<VideoFileDTO>(0);
			List<VideoFile> videoFiles = (List<VideoFile>) resultMapObj.get("videoFiles");

			if (videoFiles != null && !videoFiles.isEmpty()) {
				Iterator<VideoFile> itr = videoFiles.iterator();
				while (itr.hasNext()) {
					VideoFile videoFile = itr.next();
					VideoFileDTO videoFileDTO = new VideoFileDTO();
					videoFileDTO.setId(videoFile.getId());
					videoFileDTO.setTitle(videoFile.getTitle());
					videoFileDTO.setUploadedBy(videoFile.getCustomer().getUserName());
					videoFileDTO.setUploadedUserId(videoFile.getCustomer().getUserId());
					videoFileDTO.setUploadedUserName(XamplifyUtils.setDisplayName(videoFile.getCustomer()));
					// videoFileDTO.setUploadedDate((DateUtils.convertToOnlyDate(videoFile.getUpdatedTime())));
					videoFileDTO.setUploadedDate(DateUtils.getUtcString(videoFile.getCreatedTime()));
					videoFileDTO.setDescription(videoFile.getDescription());
					videoFileDTO.setVideoLength(videoFile.getVideoLength());
					videoFileDTO.setAlias(videoFile.getAlias());
					videoFileDTO.setVideoPath(videoFile.getFullVideoPath().replace(".mp4", ".m3u8"));
					videoFileDTO.setImagePath(videoFile.getFullImagePath());
					videoFileDTO.setCategoryId(videoFile.getCategory().getId());
					videoFileDTO.setViewBy(videoFile.getViewBy().name());
					videoFileDTO.setCategory(videoFile.getCategory());
					videoFileDTO.setVideoStatus(videoFile.getVideoStatus().name());
					videoFileDTO.setGifImagePath(videoFile.getFullGifPath());
					Integer totalViews = xtremandLogDAO.totalVideoViewsCount(videoFileDTO.getId());
					videoFileDTO.setViews(totalViews);
					if (videoFile.getViewBy() == TYPE.DRAFT) {
						VideoDefaultSettings videoDefaultSettings = getVideoDefaultSettings(
								user.getCompanyProfile().getId());
						videoFileDTO.setPlayerColor(videoDefaultSettings.getPlayerColor());
						videoFileDTO.setTransparency(videoDefaultSettings.getTransparency());
						videoFileDTO.setControllerColor(videoDefaultSettings.getControllerColor());
						videoFileDTO.setIs360video(videoDefaultSettings.isIs360video());
						videoFileDTO.setDefaultSetting(true);
					} else {
						VideoControl videoControl = videoFile.getVideoControl();
						videoFileDTO.setTransparency(videoControl.getTransparency());
						videoFileDTO.setPlayerColor(videoControl.getPlayerColor());
						videoFileDTO.setControllerColor(videoControl.getControllerColor());
						videoFileDTO.setIs360video(videoControl.isIs360video());
						videoFileDTO.setDefaultSetting(videoControl.isDefaultSetting());
					}
					videoFileDTO.setCompanyName(user.getCompanyProfile().getCompanyName());
					videoFileDTO.setProcessed(videoFile.isProcessed());
					updatedVideoFiles.add(videoFileDTO);
				}
			}
			List<VideoCategory> categoriesList = getCategoriesByUser(userId, null);
			Set<VideoCategory> categoriessSet = new HashSet<VideoCategory>();
			categoriessSet.addAll(categoriesList);
			List<VideoCategory> categories = new ArrayList<VideoCategory>(categoriessSet);

			resultMap.put("listOfMobinars", updatedVideoFiles);
			resultMap.put("totalRecords", resultMapObj.get("totalRecords"));
			resultMap.put("categories", categories);
			return resultMap;
		} catch (Exception e) {
			logger.error("Error In listVideos()", e.getCause());
			throw new VideoDataAccessException(e.getMessage());
		}
	}

	@Override
	public Map<String, Object> loadVideos(Integer userId, Pagination pagination, Integer categoryId) {
		Integer companyId = userDao.getCompanyIdByUserId(userId);
		String companyProfileName = utilDao.getPrmCompanyProfileName();
		boolean hasPartnerAccess = false;
		List<Criteria> criterias = new ArrayList<>();
		Criteria criteria = new Criteria("companyId", OPERATION_NAME.eq, companyId);
		criterias.add(criteria);
		if (hasPartnerAccess) {
			Integer vendorCompanyId = userDao.getCompanyIdByProfileName(companyProfileName);
			Criteria createdForCompanyCriteria = new Criteria(CREATED_FOR_COMPANY, OPERATION_NAME.eq, vendorCompanyId);
			criterias.add(createdForCompanyCriteria);
		} else {
			Criteria createdForCompanyCriteria = new Criteria(CREATED_FOR_COMPANY, OPERATION_NAME.isNull);
			criterias.add(createdForCompanyCriteria);
		}
		if (categoryId != 0) {
			Criteria criteriaObj = new Criteria("categoryId", OPERATION_NAME.eq, categoryId);
			criterias.add(criteriaObj);
		}
		Map<String, Object> resultMap = loadVideos(criterias, companyId, pagination);
		List<VideoCategory> videoCategoriesList = videoDAO.listVideoCategories(companyId);
		resultMap.put("categories", videoCategoriesList);
		return resultMap;
	}

	@SuppressWarnings("unchecked")
	public Map<String, Object> loadVideos(List<Criteria> criterias, Integer companyId, Pagination pagination) {
		Map<String, Object> resultMap = videoDAO.listVideos(criterias, pagination);
		List<VideoDetails> videoDetailsList = (List<VideoDetails>) resultMap.get("list");
		for (VideoDetails videoDetails : videoDetailsList) {
			videoDetails.setUploadedDate(DateUtils.getUtcString(videoDetails.getCreatedTime()));
			videoDetails.setImagePath(server_path + videoDetails.getImagePath());
			/********** XNFR-255 *******/
			Integer receivedWhiteLabeledAssetId = damDao.getDamIdByVideoId(videoDetails.getId());
			if (receivedWhiteLabeledAssetId != null && receivedWhiteLabeledAssetId > 0) {
				WhiteLabeledContentDTO whiteLabeledContentDTO = whiteLabeledAssetDao
						.findSharedByVendorCompanyNameByAssetId(receivedWhiteLabeledAssetId);
				if (whiteLabeledContentDTO != null) {
					videoDetails.setWhiteLabeledAssetReceivedFromVendor(true);
					videoDetails.setWhiteLabeledAssetSharedByVendorCompanyName(
							whiteLabeledContentDTO.getWhiteLabeledContentSharedByVendorCompanyName());
				}
				/********** XNFR-255 *******/
			}

		}
		resultMap.put("list", videoDetailsList);
		return resultMap;
	}

	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public XtremandResponse statusChange(Integer videoId, Integer userId) throws VideoDataAccessException {
		XtremandResponse response = new XtremandResponse();
		try {
			if (utilService.hasVideoAccess(userId)) {
				Criteria criteria = new Criteria("id", OPERATION_NAME.eq, videoId);
				List<Criteria> criterias = Arrays.asList(criteria);
				Collection<VideoFile> videoFiles = videoDAO.find(criterias, new FindLevel[] { FindLevel.CAMPAIGNS });
				VideoFile video = videoFiles.iterator().next();
				deleteVideo(video);

				File imageFile = new File(images_path + sep + video.getImageUri());
				if (imageFile.exists()) {
					imageFile.delete();
					logger.debug("image got deleted ");
				}
				response.setMessage("Video " + video.getTitle() + " has been deleted");
				response.setAccess(true);
				response.setStatusCode(200);
			} else {
				response.setAccess(false);
			}
		} catch (Exception e) {
			logger.error("Error occurred while deleting the video id: " + videoId + " exception is " + e.getCause());
			throw new VideoDataAccessException(e.getMessage());
		}
		return response;
	}

	// @CacheEvict(value="videoCache", allEntries=true)
	public void deleteVideo(VideoFile videoFile) {
		videoDAO.deleteByPrimaryKey(videoFile.getId());
	}

	public void deleteAllFilesExceptSource(String filePath) {
		String videoFileName = filePath.substring(filePath.lastIndexOf('/') + 1, filePath.lastIndexOf('.'));
		File folder = new File(filePath.substring(0, filePath.lastIndexOf('/')));

		FilenameFilter fileNameFilter = new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				if (name.contains(videoFileName))
					return true;
				else
					return false;
			}
		};
		File[] files = folder.listFiles(fileNameFilter);
		if (files != null) {
			for (File file : files) {
				if (!file.getName().equalsIgnoreCase(videoFileName + outPutFormat))
					file.delete();
			}
		}

	}

	public XtremandResponse saveVideo(String downloadLink, String fileName, String oauthToken, Integer userId)
			throws VideoDataAccessException {
		XtremandResponse xtremandResponse = new XtremandResponse();
		try {
			if (utilService.hasVideoAccess(userId)) {
				User user = genericDao.get(User.class, userId);
				String title = fileName;
				final String path = new SimpleDateFormat("ddMMyyyy").format(new Date());

				String videoPath = videos_path + userId + sep + path;
				File videoDir = new File(videoPath);

				if (!videoDir.exists()) {
					videoDir.mkdirs();
				}
				String imagesRealPath = images_path + userId + sep + path;
				File imagesDir = new File(imagesRealPath);

				if (!imagesDir.exists()) {
					imagesDir.mkdirs();
				}
				String filePath = sep + fileName.replaceAll(regex, "");

				String suffix = filePath.substring(filePath.lastIndexOf("."));
				String prefix = filePath.substring(0, filePath.lastIndexOf("."));
				filePath = prefix + String.valueOf(System.currentTimeMillis()) + suffix;

				File newFile = new File(videoPath + filePath);

				if (!newFile.exists() && oauthToken == null) {
					downloadLink = downloadLink.replaceAll(" ", "%20");
					URL url = new URL(downloadLink);
					ReadableByteChannel rbc = Channels.newChannel(url.openStream());
					FileOutputStream fos = new FileOutputStream(newFile);
					fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
					fos.close();
					rbc.close();

				} else if (!newFile.exists() && oauthToken != null) {
					HttpClient httpclient = HttpClientBuilder.create().build();
					HttpGet httpget = new HttpGet(downloadLink);
					httpget.addHeader("Authorization", "Bearer " + oauthToken);
					HttpResponse response = httpclient.execute(httpget);

					HttpEntity entity = response.getEntity();
					if (entity != null) {
						InputStream instream = entity.getContent();
						try {
							BufferedInputStream bis = new BufferedInputStream(instream);
							BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(newFile));
							int inByte;
							while ((inByte = bis.read()) != -1) {
								bos.write(inByte);
							}
							bis.close();
							bos.close();
						} catch (IOException ex) {
							throw ex;
						} catch (RuntimeException ex) {
							httpget.abort();
							throw ex;
						} finally {
							instream.close();
						}
						httpclient.getConnectionManager().shutdown();
					}
				}
				mailService.sendVideoUploadedMail(user, EmailConstants.VIDEO_UPLOADED,
						title.substring(0, title.lastIndexOf(".")));
				xtremandResponse
						.setData(userId + sep + path + filePath + "~" + title.substring(0, title.lastIndexOf(".")));
				xtremandResponse.setAccess(true);
			} else {
				xtremandResponse.setAccess(false);
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
			throw new VideoDataAccessException(e.getMessage());
		}
		return xtremandResponse;
	}

	@Override
	public XtremandResponse uploadFile(List<CloudContent> files, Integer userId) {
		XtremandResponse response = new XtremandResponse();
		try {
			if (!files.isEmpty()) {
				List<AmazonWebModel> amazonWebModels = new ArrayList<>();
				Integer companyId = userDao.findByPrimaryKey(userId, new FindLevel[] { FindLevel.COMPANY_PROFILE })
						.getCompanyProfile().getId();
				List<String> videoTypes = new ArrayList<>(
						Arrays.asList(videoFileTypesString.replace(" ", "").trim().split(",")));
				validateVideoFiles(files, videoTypes, response);
				// validateFileSizes(files, response);
				// if(response.getStatusCode()!=1024 && response.getStatusCode()!=1025){
				final String currentDate = new SimpleDateFormat("ddMMyyyy").format(new Date());
				List<String> existingFileNames = amazonWebService.getListObjects(companyId).stream()
						.map(x -> x.getKey().substring(x.getKey().lastIndexOf('/') + 1).trim())
						.collect(Collectors.toList());
				for (CloudContent file : files) {
					String fileName = file.getFileName().replaceAll(regex, "");
					;
					AmazonWebModel amazonWebModel = new AmazonWebModel();
					String folderPath = uploadContentPath + currentDate + sep + userId;
					File imageDir = new File(folderPath);
					if (!imageDir.exists()) {
						imageDir.mkdirs();
					}
					String imageFilePath = folderPath + sep + fileName;
					File newImageFile = new File(imageFilePath);
					String oauthToken = file.getOauthToken();
					if (!newImageFile.exists() && oauthToken == null) {
						URL url = new URL(file.getDownloadLink());
						ReadableByteChannel rbc = Channels.newChannel(url.openStream());
						FileOutputStream fos = new FileOutputStream(newImageFile);
						fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
						fos.close();
						rbc.close();

					} else if (!newImageFile.exists() && oauthToken != null) {
						HttpClient httpclient = HttpClientBuilder.create().build();
						HttpGet httpget = new HttpGet(file.getDownloadLink());
						httpget.addHeader("Authorization", "Bearer " + oauthToken);
						HttpResponse httpResponse = httpclient.execute(httpget);

						HttpEntity entity = httpResponse.getEntity();
						if (entity != null) {
							InputStream instream = entity.getContent();
							try {
								BufferedInputStream bis = new BufferedInputStream(instream);
								BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(newImageFile));
								int inByte;
								while ((inByte = bis.read()) != -1) {
									bos.write(inByte);
								}
								bis.close();
								bos.close();
							} catch (IOException ex) {
								throw ex;
							} catch (RuntimeException ex) {
								httpget.abort();
								throw ex;
							} finally {
								instream.close();
							}
							httpclient.getConnectionManager().shutdown();
						}
					}
					amazonWebModel.setCompanyId(companyId);
					amazonWebModel.setFilePath(imageFilePath);
					amazonWebModel.setFileName(XamplifyUtils.addIndexToExistingFileNames(fileName, existingFileNames));
					amazonWebModels.add(amazonWebModel);
				}
				amazonWebService.uploadFiles(amazonWebModels);
				response.setStatusCode(1020);
				response.setMessage(filesUploadedSuccessfully);
				// }

			}
		} catch (Exception e) {
			logger.error("uploadFile(" + files + "," + userId + ")", e);
		}
		return response;
	}

	private void validateVideoFiles(List<CloudContent> files, List<String> videoTypes, XtremandResponse response) {
		List<String> fileTypes = files.stream()
				.map(urEntity -> urEntity.getFileName().substring(urEntity.getFileName().lastIndexOf('.') + 1).trim())
				.collect(Collectors.toList());
		for (String fileType : fileTypes) {
			if (videoTypes.indexOf(fileType.toUpperCase().trim()) > -1) {
				response.setStatusCode(1024);
				response.setMessage(fileTypeError);
				break;
			}

		}
	}

	@Override
	public XtremandResponse saveRecordedVideo(MultipartFile file, Integer userId) {
		XtremandResponse response = new XtremandResponse();

		User user = userService.loadUser(Arrays.asList(new Criteria("userId", OPERATION_NAME.eq, userId)),
				new FindLevel[] { FindLevel.SHALLOW });
		logger.debug("Saving Recorded Video File Of " + user.getUserId() + " :: File is" + file.getName());
		try {
			if (utilService.hasVideoAccess(userId)) {
				final String path = new SimpleDateFormat("ddMMyyyy").format(new Date());
				String userPath = user.getUserId() + sep + path;
				String videoPath = videos_path + userPath;

				File videoDir = new File(videoPath);

				if (!videoDir.exists()) {
					videoDir.mkdirs();
				}

				String imagesRealPath = images_path + user.getUserId() + sep + path;
				File imagesDir = new File(imagesRealPath);

				if (!imagesDir.exists()) {
					imagesDir.mkdirs();
				}
				String nanoSec = String.valueOf(System.nanoTime());
				transferRecoredFile(file, videoPath, nanoSec, user.getUserId());
				videoPath = userPath + "/output_" + nanoSec + ".webm";
				String title = "recorded_video.webm";
				response.setData(videoPath + "~" + title.substring(0, title.lastIndexOf(".")));
				response.setAccess(true);
			} else {
				response.setAccess(false);
			}

		} catch (VideoDataAccessException v) {
			logger.error("Error Occured While Saving Recorded File for " + user.getUserId() + " And File is"
					+ file.getName(), v);
			throw new VideoDataAccessException(v.getMessage());
		}
		return response;
	}

	private void transferRecoredFile(MultipartFile file, String videoPath, String nanoSec, Integer userId) {
		try {
			file.transferTo(new File(videoPath + sep + "output_" + nanoSec + ".webm"));
		} catch (IllegalStateException | IOException e) {
			logger.error("Error Occured While Transfering File for " + userId + " And File is" + file.getName(), e);
			throw new VideoDataAccessException(e.getMessage());
		}
	}

	@Override
	public List<String> getVideoTitles(Integer userId) throws VideoDataAccessException {
		List<Integer> userIdList = userService.getCompanyUserIds(userId);
		Integer[] userIdArray = ArrayUtils.toObject(userIdList.stream().mapToInt(i -> i).toArray());
		return videoDAO.getVideoTitles(userIdArray);
	}

	@Override
	public void updateVideoDefaultSettings(CompanyProfile companyProfile, VideoDefaultSettings videoDefaultSettings)
			throws VideoDataAccessException {
		VideoDefaultSettings videoDefaultSettingsFromDb = videoDAO.getVideoDefaultSettings(companyProfile.getId());
		if (videoDefaultSettingsFromDb == null) {
			videoDefaultSettingsFromDb = new VideoDefaultSettings();
			videoDefaultSettingsFromDb.setCompanyProfile(companyProfile);
			videoDefaultSettingsFromDb.setPlayerColor(defaultPlayerColor);
			videoDefaultSettingsFromDb.setEnableVideoController(true);
			videoDefaultSettingsFromDb.setControllerColor(defaultControllerColor);
			videoDefaultSettingsFromDb.setAllowSharing(true);
			videoDefaultSettingsFromDb.setEnableSettings(true);
			videoDefaultSettingsFromDb.setAllowFullscreen(true);
			videoDefaultSettingsFromDb.setAllowComments(true);
			videoDefaultSettingsFromDb.setAllowLikes(true);
			videoDefaultSettingsFromDb.setEnableCasting(true);
			videoDefaultSettingsFromDb.setAllowEmbed(true);
			videoDefaultSettingsFromDb.setTransparency(100);
			videoDefaultSettingsFromDb.setIs360video(false);
			genericDao.save(videoDefaultSettingsFromDb);
		} else {
			String playerColor = videoDefaultSettings.getPlayerColor();
			String controllerColor = videoDefaultSettings.getControllerColor();
			if (!StringUtils.hasText(playerColor)) {
				playerColor = defaultPlayerColor;
			}
			if (!StringUtils.hasText(controllerColor)) {
				controllerColor = defaultControllerColor;
			}
			videoDefaultSettingsFromDb.setPlayerColor(playerColor);
			videoDefaultSettingsFromDb.setEnableVideoController(videoDefaultSettings.isEnableVideoController());
			videoDefaultSettingsFromDb.setControllerColor(controllerColor);
			videoDefaultSettingsFromDb.setAllowSharing(videoDefaultSettings.isAllowSharing());
			videoDefaultSettingsFromDb.setEnableSettings(videoDefaultSettings.isEnableSettings());
			videoDefaultSettingsFromDb.setAllowFullscreen(videoDefaultSettings.isAllowFullscreen());
			videoDefaultSettingsFromDb.setAllowComments(videoDefaultSettings.isAllowComments());
			videoDefaultSettingsFromDb.setAllowLikes(videoDefaultSettings.isAllowLikes());
			videoDefaultSettingsFromDb.setEnableCasting(videoDefaultSettings.isEnableCasting());
			videoDefaultSettingsFromDb.setAllowEmbed(videoDefaultSettings.isAllowEmbed());
			videoDefaultSettingsFromDb.setTransparency(videoDefaultSettings.getTransparency());
			videoDefaultSettingsFromDb.setIs360video(videoDefaultSettings.isIs360video());
		}
	}

	@Override
	public VideoDefaultSettings getVideoDefaultSettings(Integer companyId) throws VideoDataAccessException {
		return videoDAO.getVideoDefaultSettings(companyId);
	}

	@Override
	public Integer getVideosCount(Integer userId) throws VideoDataAccessException {
		List<Integer> userIdList = userService.getCompanyUserIds(userId);
		Integer[] userIdArray = ArrayUtils.toObject(userIdList.stream().mapToInt(i -> i).toArray());
		return videoDAO.getVideosCount(userIdArray);
	}

	@Override
	public Integer getVideosViewsCount(Integer userId) throws VideoDataAccessException {
		List<Integer> userIdList = userService.getCompanyUserIds(userId);
		Integer[] userIdArray = ArrayUtils.toObject(userIdList.stream().mapToInt(i -> i).toArray());
		return videoDAO.getVideosViewsCount(userIdArray);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> getMonthWiseCountryWiseVideoViews(String alias) throws VideoDataAccessException {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Criteria criteria = new Criteria("alias", OPERATION_NAME.eq, alias);
		List<Criteria> criterias = Arrays.asList(criteria);
		VideoFile videoFile = videoDAO.find(criterias, new FindLevel[] { FindLevel.SHALLOW }).iterator().next();
		logger.debug("views : " + videoFile.getViews());

		List<Integer> monthlyViews = new ArrayList<Integer>();
		int year = Year.now().getValue();
		for (int i = 1; i <= 12; i++) {
			YearMonth yearMonth = YearMonth.of(year, i);
			Integer viewsCount = videoDAO.monthWiseVideoViewsCount(videoFile.getId(), yearMonth.atDay(1).toString(),
					yearMonth.atEndOfMonth().toString());
			monthlyViews.add(viewsCount);
		}

		String[] months = { "01/" + year, "02/" + year, "03/" + year, "04/" + year, "05/" + year, "06/" + year,
				"07/" + year, "08/" + year, "09/" + year, "10/" + year, "11/" + year, "12/" + year };
		List<String> monthsList = Arrays.asList(months);

		List<Object[]> list = videoDAO.countryWiseVideoViewsCount(videoFile.getId());
		MultiMap multiMap = new MultiValueMap();
		HashMap<String, Integer> countrywiseViews = new HashMap<>();
		for (Object[] row : list) {
			multiMap.put((String) row[0], ((BigInteger) row[1]).intValue());
		}
		Set<String> keys = multiMap.keySet();
		for (String key : keys) {
			ArrayList<Integer> countlist = (ArrayList<Integer>) multiMap.get(key);
			int[] arr = countlist.stream().filter(i -> i != null).mapToInt(i -> i).toArray();
			countrywiseViews.put(key, IntStream.of(arr).sum());
		}

		JSONArray countrywiseViewsJsonArray = new JSONArray();
		for (Map.Entry<String, Integer> entry : countrywiseViews.entrySet()) {
			JSONArray json = new JSONArray();
			json.put(entry.getKey().toLowerCase());
			json.put(entry.getValue());
			countrywiseViewsJsonArray.put(json);
		}
		resultMap.put("views", videoFile.getViews());
		resultMap.put("monthlyViews", monthlyViews);
		resultMap.put("months", monthsList);
		resultMap.put("countrywiseViews", countrywiseViewsJsonArray);

		return resultMap;
	}

	@Override
	public Map<String, Object> getWatchedFullyMinutesWatchedVideoViews(String alias) throws VideoDataAccessException {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Criteria criteria = new Criteria("alias", OPERATION_NAME.eq, alias);
		List<Criteria> criterias = Arrays.asList(criteria);
		VideoFile videoFile = videoDAO.find(criterias, new FindLevel[] { FindLevel.SHALLOW }).iterator().next();
		Integer fullyWatchedViews = videoDAO.watchedFullyVideoViewsCount(videoFile.getId());
		logger.debug("fullywatchedusers: " + fullyWatchedViews);
		Integer totalViews = videoFile.getViews();
		Float percentage = null;
		if (totalViews != 0) {
			percentage = (float) ((fullyWatchedViews * 100) / totalViews);
		} else if (totalViews == 0) {
			percentage = (float) Math.round(0);
		}
		logger.debug("percentage ; " + percentage);

		List<String> names = new ArrayList<String>();
		List<Double> minutesWatched = new ArrayList<>();
		List<Object[]> list = xamplifyLogService.listTotalMunutesWatchedByTop10Users(videoFile.getId());
		for (int i = 0; i < list.size(); i++) {
			Object[] row = list.get(i);
			names.add(row[0].toString());
			minutesWatched.add(((Number) row[2]).doubleValue());
		}

		Integer totalViewsCount = xamplifyLogService.totalVideoViewsCount(videoFile.getId());

		resultMap.put("watchedfullypercentage", percentage);
		resultMap.put("names", names);
		resultMap.put("minutesWatched", minutesWatched);
		resultMap.put("totalViewsCount", totalViewsCount);
		return resultMap;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> getVideoReportData(User user, Pagination pagination, Integer categoryId)
			throws VideoDataAccessException {
		List<Criteria> criterias = new ArrayList<Criteria>();
		Criteria criteria = new Criteria("customer.userId", OPERATION_NAME.eq, user.getUserId());
		criterias.add(criteria);
		if (categoryId != 0) {
			Criteria criteriaObj = new Criteria("category.id", OPERATION_NAME.eq, categoryId);
			criterias.add(criteriaObj);
		}
		Map<String, Object> resultMapObj = videoDAO.find(criterias,
				new FindLevel[] { FindLevel.VIDEO_PATH, FindLevel.VIDEO_CONTROL }, pagination);
		Map<String, Object> resultMap = new HashMap<String, Object>();
		List<VideoFileDTO> updatedVideoFiles = new ArrayList<VideoFileDTO>(0);
		List<VideoFile> videoFiles = (List<VideoFile>) resultMapObj.get("videoFiles");

		if (videoFiles != null && !videoFiles.isEmpty()) {
			Iterator<VideoFile> itr = videoFiles.iterator();
			while (itr.hasNext()) {
				VideoFile videoFile = itr.next();
				VideoFileDTO videoFileDTO = new VideoFileDTO();
				videoFileDTO.setId(videoFile.getId());
				videoFileDTO.setTitle(videoFile.getTitle());
				videoFileDTO.setUploadedBy(videoFile.getCustomer().getUserName());
				videoFileDTO.setUploadedDate((DateUtils.convertToOnlyDate(videoFile.getUpdatedTime())));
				videoFileDTO.setDescription(videoFile.getDescription());
				videoFileDTO.setVideoLength(videoFile.getVideoLength());
				videoFileDTO.setAlias(videoFile.getAlias());
				videoFileDTO.setVideoPath(videoFile.getFullVideoPath().replace(".mp4", ".m3u8"));
				videoFileDTO.setImagePath(videoFile.getFullImagePath());
				videoFileDTO.setCategoryId(videoFile.getCategory().getId());

				if (videoFile.getViewBy() == TYPE.DRAFT) {
					VideoDefaultSettings videoDefaultSettings = getVideoDefaultSettings(
							user.getCompanyProfile().getId());
					videoFileDTO.setPlayerColor(videoDefaultSettings.getPlayerColor());
					videoFileDTO.setTransparency(videoDefaultSettings.getTransparency());
					videoFileDTO.setControllerColor(videoDefaultSettings.getControllerColor());
					videoFileDTO.setIs360video(videoDefaultSettings.isIs360video());
					videoFileDTO.setDefaultSetting(true);
				} else {
					VideoControl videoControl = videoFile.getVideoControl();
					videoFileDTO.setTransparency(videoControl.getTransparency());
					videoFileDTO.setPlayerColor(videoControl.getPlayerColor());
					videoFileDTO.setControllerColor(videoControl.getControllerColor());
					videoFileDTO.setIs360video(videoControl.isIs360video());
					videoFileDTO.setDefaultSetting(videoControl.isDefaultSetting());
				}
				videoFileDTO.setViewBy(videoFile.getViewBy().name());
				videoFileDTO.setCategory(videoFile.getCategory());
				videoFileDTO.setVideoStatus(videoFile.getVideoStatus().name());
				videoFileDTO.setGifImagePath(videoFile.getFullGifPath());
				videoFileDTO.setIs360video(
						videoFile.getVideoControl() != null ? videoFile.getVideoControl().isIs360video() : false);
				videoFileDTO.setDefaultSetting(
						videoFile.getVideoControl() != null ? videoFile.getVideoControl().isDefaultSetting() : false);
				videoFileDTO.setViews(videoFile.getViews());
				videoFileDTO.setLeads(0);
				videoFileDTO.setFavourites(0);
				videoFileDTO.setComments(0);
				videoFileDTO.setLikes(0);
				videoFileDTO.setDislikes(0);
				videoFileDTO.setWatchedFully((Float) ((getWatchedFullyMinutesWatchedVideoViews(videoFile.getAlias()))
						.get("watchedfullypercentage")));
				updatedVideoFiles.add(videoFileDTO);
			}
		}
		List<VideoCategory> categoriesList = getCategoriesByUser(user.getUserId(),
				pagination.getFilterBy() != null ? VideoFile.TYPE.valueOf(pagination.getFilterBy().toUpperCase())
						: null);
		Set<VideoCategory> categoriessSet = new HashSet<VideoCategory>();
		categoriessSet.addAll(categoriesList);
		List<VideoCategory> categories = new ArrayList<VideoCategory>(categoriessSet);

		resultMap.put("listOfMobinars", updatedVideoFiles);
		resultMap.put("totalRecords", resultMapObj.get("totalRecords"));
		resultMap.put("categories", categories);
		return resultMap;
	}

	@Override
	public Map<String, Object> getChannelVideos(Pagination pagination, Integer categoryId, Integer userId) {
		logger.info("getChannelVideoFiles called with userId : " + userId);

		User user = userService.loadUser(Arrays.asList(new Criteria("userId", OPERATION_NAME.eq, userId)),
				new FindLevel[] { FindLevel.ROLES });
		CompanyProfile company = user.getCompanyProfile();
		List<Partnership> partnerships = partnershipDAO.getApprovedPartnershipsByPartnerCompany(company);
		List<Integer> companyIds = new ArrayList<>();
		if (partnerships != null && !partnerships.isEmpty()) {
			for (Partnership partnership : partnerships) {
				CompanyProfile vendorCompany = partnership.getVendorCompany();
				if (vendorCompany != null) {
					companyIds.add(vendorCompany.getId());
				}
			}
		}
		List<Integer> videoIdsList = new ArrayList<>();
		if (!(companyIds.isEmpty())) {
			videoIdsList = videoDAO.getChannelVideos(userId, companyIds, true, company.getId());
		}

		if (!videoIdsList.isEmpty()) {

			List<Criteria> criterias = new ArrayList<>();
			Criteria criteria = new Criteria("id", OPERATION_NAME.in, videoIdsList.toArray());
			criterias.add(criteria);
			if (categoryId != 0) {
				Criteria criteriaObj = new Criteria("categoryId", OPERATION_NAME.eq, categoryId);
				criterias.add(criteriaObj);
			}

			Map<String, Object> resultMap = loadVideos(criterias, company.getId(), pagination);
			List<VideoCategory> videoCategoriesList = videoDAO.listVideoCategories(videoIdsList);
			resultMap.put("categories", videoCategoriesList);
			return resultMap;
		} else {
			Map<String, Object> resultMap = new HashMap<>();
			resultMap.put("list", Collections.EMPTY_LIST);
			resultMap.put("totalRecords", 0);
			resultMap.put("categories", Collections.EMPTY_LIST);
			return resultMap;
		}
	}

	@Override
	public Integer getChannelVideosViewsCount(Integer userId) {
		logger.info("getChannelVideoFiles called with userId : " + userId);
		User user = userService.loadUser(Arrays.asList(new Criteria("userId", OPERATION_NAME.eq, userId)),
				new FindLevel[] { FindLevel.ROLES });
		CompanyProfile company = user.getCompanyProfile();
		List<Partnership> partnerships = partnershipDAO.getApprovedPartnershipsByPartnerCompany(company);

		List<Integer> companyIds = new ArrayList<Integer>();
		if (partnerships != null && !partnerships.isEmpty()) {
			for (Partnership partnership : partnerships) {
				CompanyProfile vendorCompany = partnership.getVendorCompany();
				if (vendorCompany != null) {
					companyIds.add(vendorCompany.getId());
				}
			}
		}

		if (!(companyIds.isEmpty())) {
			List<Integer> videoIdsList = videoDAO.getChannelVideos(userId, companyIds, true, company.getId());
			Integer[] videoIdArray = ArrayUtils.toObject(videoIdsList.stream().mapToInt(i -> i).toArray());
			return ArrayUtils.isNotEmpty(videoIdArray) ? videoDAO.getChannelVideosViewsCount(videoIdArray) : 0;
		} else {
			return 0;
		}
	}

	@Override
	public List<VideoFileDTO> listPublicVideosByCompany(Integer companyId) {
		List<VideoFileDTO> videos = new ArrayList<>(0);
		List<Integer> userIds = userService.getCompanyUsers(companyId);
		Integer[] userIdsArray = new Integer[userIds.size()];
		userIdsArray = userIds.toArray(userIdsArray);
		List<Object[]> result = videoDAO.listPublicVideosByCompany(userIdsArray);
		for (Object[] row : result) {
			VideoFileDTO videoFileDTO = new VideoFileDTO();
			videoFileDTO.setId(Integer.valueOf(row[0].toString()));
			videoFileDTO.setTitle(row[1].toString());
			videos.add(videoFileDTO);
		}
		return videos;
	}

	@Override
	public XtremandResponse hasVideoAccess(Integer userId) {
		XtremandResponse response = new XtremandResponse();
		response.setAccess(utilService.hasVideoAccess(userId));
		return response;
	}

	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public Map<String, Object> processVideo1(String videoFilePath, DamUploadPostDTO damUploadPostDTO,
			MultipartFile thumbnailFile, List<String> tags) {
		Map<String, Object> resultMap = new HashMap<>();
		Integer userId = damUploadPostDTO.getLoggedInUserId();
		final String path = new SimpleDateFormat("ddMMyyyy").format(new Date());
		FFMPEGStatus status = new FFMPEGStatus();
		final User user = userService.findByPrimaryKey(userId,
				new com.xtremand.common.bom.FindLevel[] { FindLevel.COMPANY_PROFILE });
		VideoFileUploadForm form = new VideoFileUploadForm();

		VideoFile video = new VideoFile();
		try {
			String filePath = videoFilePath.split("~")[0];
			form.setVideoPath(videos_path + filePath);
			form = videoEncoder.encodeVideoFile(status, form);

			File file = new File(form.getVideoPath());

			/******** XNFR-434 ******/ /** XNFR-885 **/
			if (damUploadPostDTO.isReplaceAsset() && !damUploadPostDTO.isSendForReApproval()) {
				video = setVideoPropertiesAndReplaceVideo(damUploadPostDTO, path, user, form, file);
			} else {
				setVideoPropertiesAndSave(damUploadPostDTO, thumbnailFile, tags, userId, path, user, form, video, file);
			}

			final int currentBitRate = form.getBitRate();
			final String finalPath = form.getVideoPath();

			resultMap.put("finalPath", finalPath);
			resultMap.put("currentBitRate", currentBitRate);
			resultMap.put("videoFile", video);
			resultMap.put("loggedInUser", user);

			form.setId(video.getId());
			form.setProcessed(false);

			status.setStatus("Video processing complete!");

		} catch (Exception e) {
			e.printStackTrace();
			form.setError("Error occured : " + e.getMessage());
		}
		form.setVideoPath(server_path + video.getUri());
		return resultMap;
	}

	/***** XNFR-434 ****/
	private VideoFile setVideoPropertiesAndReplaceVideo(DamUploadPostDTO damUploadPostDTO, final String path,
			final User user, VideoFileUploadForm form, File file) throws IOException, JCodecException {
		VideoFile video;
		video = genericDao.get(VideoFile.class, damUploadPostDTO.getVideoId());
		video.setUri(form.getVideoPath().substring(form.getVideoPath().indexOf("videos")));
		video.setVideoSize(Double.parseDouble(String.valueOf(file.length())) / (1024 * 1024));
		video.setProcessed(false);
		int length = form.getVideoLength();
		video.setVideoLength(timeConversion(length));
		List<String> imageFilesList = generateImages(user, path, file, length, form.getBitRate(), form.getWidth());
		String firstImageFile = "";
		String secondImageFile = "";
		String thirdImageFile = "";

		if (!imageFilesList.isEmpty()) {
			try {
				firstImageFile = imageFilesList.get(0);
				secondImageFile = imageFilesList.get(1);
				thirdImageFile = imageFilesList.get(2);
			} catch (Exception e) {
				logger.info(e.getMessage());
			}
		}

		if (firstImageFile.length() > 0) {
			video.setImageUri(firstImageFile.replaceFirst(server_path, ""));
		}
		String firstGifFile = form.getGifFiles().get(0);
		video.setGifUri(firstGifFile.replaceFirst(server_path, ""));

		VideoImage videoImage = video.getVideoImage();
		if (videoImage != null) {
			String gif1 = form.getGifFiles().get(0).replaceFirst(server_path, "");
			String gif2 = form.getGifFiles().get(1).replaceFirst(server_path, "");
			String gif3 = form.getGifFiles().get(2).replaceFirst(server_path, "");

			if (!imageFilesList.isEmpty()) {
				String image1 = firstImageFile.replaceFirst(server_path, "");
				String image2 = secondImageFile.replaceFirst(server_path, "");
				String image3 = thirdImageFile.replaceFirst(server_path, "");
				videoImage.setImage1(image1);
				videoImage.setImage2(image2);
				videoImage.setImage3(image3);
			} else {
				videoImage.setImage1("");
				videoImage.setImage2("");
				videoImage.setImage3("");
			}
			videoImage.setGif1(gif1);
			videoImage.setGif2(gif2);
			videoImage.setGif3(gif3);
			video.setVideoImage(videoImage);
		}
		return video;
	}

	private void setVideoPropertiesAndSave(DamUploadPostDTO damUploadPostDTO, MultipartFile thumbnailFile,
			List<String> tags, Integer userId, final String path, final User user, VideoFileUploadForm form,
			VideoFile video, File file) throws IOException, JCodecException {
		video.setUri(form.getVideoPath().substring(form.getVideoPath().indexOf("videos")));
		form.setTitle(damUploadPostDTO.getAssetName());
		video.setVideoSize(Double.parseDouble(String.valueOf(file.length())) / (1024 * 1024));
		int length = form.getVideoLength();
		video.setVideoLength(timeConversion(length));
		video.setViewBy(TYPE.PRIVATE);
		video.setCustomer(user);

		List<String> values = new ArrayList<>();
		values.add(VideoFile.TYPE.PRIVATE.name());
		values.add(VideoFile.TYPE.PUBLIC.name());
		form.setViewByOptions(values);

		List<String> imageFiles = generateImages(user, path, file, length, form.getBitRate(), form.getWidth());
		form.setImageFiles(imageFiles);
		video.setVideoID(form.getTitle());
		video.setTitle(damUploadPostDTO.getAssetName());
		video.setCategory(genericDao.get(VideoCategory.class, 108));
		video.setBitrate(1.2);
		video.setViews(0);
		video.setVideoStatus(VideoStatus.APPROVED);
		video.setAlias(UUID.randomUUID().toString());
//		String plainDescription = damUploadPostDTO.getDescription() != null
//				? Jsoup.parse(damUploadPostDTO.getDescription()).text()
//				: null;
		String plainDescription = damUploadPostDTO.getDescription() != null
				? damUploadPostDTO.getDescription().replaceAll("\\<.*?\\>", "")
				: null;
		video.setDescription(utilService.replacedDescription(plainDescription));
		List<String> imageFilesList = form.getImageFiles();
		String firstImageFile = "";
		String secondImageFile = "";
		String thirdImageFile = "";

		if (!imageFilesList.isEmpty()) {
			try {
				firstImageFile = imageFilesList.get(0);
				secondImageFile = imageFilesList.get(1);
				thirdImageFile = imageFilesList.get(2);
			} catch (Exception e) {
				logger.info(e.getMessage());
			}
		}

		if (firstImageFile.length() > 0) {
			video.setImageUri(firstImageFile.replaceFirst(server_path, ""));
		}
		String firstGifFile = form.getGifFiles().get(0);
		video.setGifUri(firstGifFile.replaceFirst(server_path, ""));
		video.initialiseCommonFields(true, user.getUserId());

		VideoImage videoImage = null;
		if (!imageFilesList.isEmpty()) {
			videoImage = new VideoImage(firstImageFile.replaceFirst(server_path, ""),
					secondImageFile.replaceFirst(server_path, ""), thirdImageFile.replaceFirst(server_path, ""),
					form.getGifFiles().get(0).replaceFirst(server_path, ""),
					form.getGifFiles().get(1).replaceFirst(server_path, ""),
					form.getGifFiles().get(2).replaceFirst(server_path, ""));
		} else {
			videoImage = new VideoImage("", "", "", form.getGifFiles().get(0).replaceFirst(server_path, ""),
					form.getGifFiles().get(1).replaceFirst(server_path, ""),
					form.getGifFiles().get(2).replaceFirst(server_path, ""));
		}
		video.setVideoImage(videoImage);
		videoImage.setVideoFile(video);

		form.setCategoryId(1);
		form.setViewBy(VideoFile.TYPE.DRAFT);
		form.setImagePath(firstImageFile);
		form.setGifImagePath(form.getGifFiles().get(0));

		VideoDefaultSettings videoDefaultSettings = getVideoDefaultSettings(user.getCompanyProfile().getId());
		String playerColor = videoDefaultSettings.getPlayerColor();
		Integer transparency = videoDefaultSettings.getTransparency();
		String controllerColor = videoDefaultSettings.getControllerColor();
		videoDefaultSettings
				.setPlayerColor(XamplifyUtils.isValidString(playerColor) ? playerColor : defaultPlayerColor);
		videoDefaultSettings.setTransparency(XamplifyUtils.isValidInteger(transparency) ? transparency : 100);
		videoDefaultSettings.setControllerColor(
				XamplifyUtils.isValidString(controllerColor) ? controllerColor : defaultControllerColor);
		setDefaultVideoControl(video, videoDefaultSettings);
		setDefaultCallAction(video);
		video.setProcessed(false);
		genericDao.save(video);
		if (thumbnailFile != null) {
			generateThumbnail(thumbnailFile, userId);
			video.setImageUri("images" + sep + video.getCustomer().getUserId() + sep + path + sep
					+ thumbnailFile.getOriginalFilename().replaceAll(regex, ""));
			video.setCustomThumbnailUploaded(true);
		}
		setVideoTags(tags, video);
	}

	@Override
	public String getVideoPath(Integer videoId) {
		String videoPath = videoDAO.getVideoPath(videoId);
		return server_path + "/" + videoPath;
	}

	public List<Integer> getChannelVideosIds(Pagination pagination, Integer categoryId, Integer userId) {
		logger.info("getChannelVideoFiles called with userId : " + userId);
		User user = userService.loadUser(Arrays.asList(new Criteria("userId", OPERATION_NAME.eq, userId)),
				new FindLevel[] { FindLevel.COMPANY_PROFILE });
		CompanyProfile company = user.getCompanyProfile();
		List<Partnership> partnerships = partnershipDAO.getApprovedPartnershipsByPartnerCompany(company);
		List<Integer> companyIds = new ArrayList<>();
		if (partnerships != null && !partnerships.isEmpty()) {
			for (Partnership partnership : partnerships) {
				CompanyProfile vendorCompany = partnership.getVendorCompany();
				if (vendorCompany != null) {
					companyIds.add(vendorCompany.getId());
				}
			}
		}
		List<Integer> videoIdsList = new ArrayList<>();
		if (!(companyIds.isEmpty())) {
			videoIdsList = videoDAO.getChannelVideos(userId, companyIds, true, company.getId());
		}

		return videoIdsList;
	}

	public void setDefaultVideoControl(VideoFile videoFile, VideoDefaultSettings videoDefaultSettings) {
		VideoControl videoControl = new VideoControl(videoDefaultSettings.getPlayerColor(),
				videoDefaultSettings.isEnableVideoController(), videoDefaultSettings.getControllerColor(),
				videoDefaultSettings.isAllowSharing(), videoDefaultSettings.isEnableSettings(),
				videoDefaultSettings.isAllowFullscreen(), videoDefaultSettings.isAllowComments(),
				videoDefaultSettings.isAllowLikes(), videoDefaultSettings.isEnableCasting(),
				videoDefaultSettings.isAllowEmbed(), videoDefaultSettings.getTransparency(),
				videoDefaultSettings.isIs360video(), true, videoDefaultSettings.getBrandingLogoUri(),
				videoDefaultSettings.getBrandingLogoDescUri(), true);
		videoFile.setVideoControl(videoControl);
		videoControl.setVideoFile(videoFile);
	}

	public void setDefaultCallAction(VideoFile videoFile) {
		CallAction callAction = new CallAction(true, true, "", "", true, false, false);
		videoFile.setCallAction(callAction);
		callAction.setVideoFile(videoFile);
	}

	@Override
	public VideoFileDTO getVideoById(Integer userId, Integer videoId, VideoFile.TYPE viewBy)
			throws VideoDataAccessException {
		VideoFileDTO videoFileDTO = new VideoFileDTO();
		Criteria criteria = new Criteria("id", OPERATION_NAME.eq, videoId);
		List<Criteria> criterias = Arrays.asList(criteria);
		videoFileDTO = getVideo(criterias, viewBy);
		videoFileDTO.setAccess(true);
		/*** XNFR-255 ***/
		DamVideoDTO damVideoDTO = damDao.findDamAndVideoDetailsByVideoId(videoId);
		videoFileDTO.setShareAsWhiteLabeledAsset(damVideoDTO.isShareAsWhiteLabeledAsset());
		videoFileDTO.setDisableWhiteLabelOption(damVideoDTO.isShareAsWhiteLabeledAsset());
		videoFileDTO.setWhiteLabeledToolTipMessage(whiteLabeledToolTipMessage);
		if (damVideoDTO.isShareAsWhiteLabeledAsset()) {
			videoFileDTO.setWhiteLabeledToolTipMessage(whiteLabeledDisabledToolTipMessage);
		}
		videoFileDTO.setAddedToQuickLinks(damVideoDTO.isAddedToQuickLinks());
		Integer damId = damVideoDTO.getDamId();
		videoFileDTO.setDamId(damId);
		videoFileDTO.setSlug(damVideoDTO.getSlug());
		/*** Added On 02/08/2023 ****/
		boolean isPublishedToPartnerGroups = damDao.isPublishedToPartnerGroups(damId);
		videoFileDTO.setPublishedToPartnerGroups(isPublishedToPartnerGroups);
		List<Integer> partnershipIds = damDao.findPartnershipIdsForPartnerCompaniesOptionByDamId(damId);
		if (partnershipIds != null && !partnershipIds.isEmpty()) {
			videoFileDTO.setPartnershipIds(partnershipIds);
		} else {
			videoFileDTO.setPartnershipIds(new ArrayList<>());
		}
		List<Integer> partnerIds = damDao.findPublishedPartnerIdsByDamId(damId);
		if (partnerIds != null && !partnerIds.isEmpty()) {
			videoFileDTO.setPartnerIds(partnerIds);
		} else {
			videoFileDTO.setPartnerIds(new ArrayList<>());
		}
		List<Integer> publishedPartnerGroupIds = damDao.findPublishedPartnerGroupIdsByDamId(damId);
		if (publishedPartnerGroupIds != null && !publishedPartnerGroupIds.isEmpty()) {
			videoFileDTO.setPartnerGroupIds(publishedPartnerGroupIds);
		} else {
			videoFileDTO.setPartnerGroupIds(new ArrayList<>());
		}
		/*** Added On 02/08/2023 ****/
		/*** XNFR-255 ***/

		/*** XNFR-342 ****/
		boolean isVideoPublished = damDao.isAssetPublished(damId);
		videoFileDTO.setPublished(isVideoPublished);
		/*** XNFR-342 ****/
		/** XNFR-884 **/
		videoFileDTO.setApprovalStatus(damVideoDTO.getApprovalStatus());
		boolean createdByAnyApprover = approveDao.checkIsAssetApproverByTeamMemberId(damVideoDTO.getCreatedBy());
		videoFileDTO.setCreatedByAnyApprover(createdByAnyApprover);
		return videoFileDTO;
	}

	@Override
	public VideoFile findByAlias(String alias) {
		return videoDAO.findByAlias(alias);
	}

	@SuppressWarnings("unchecked")
	@Override
	public XtremandResponse findVideos(Pagination pagination) {
		XtremandResponse response = new XtremandResponse();
		Integer userId = pagination.getUserId();
		String companyProfileName = utilDao.getPrmCompanyProfileName();
		if (XamplifyUtils.isValidString(companyProfileName)) {
			pagination.setVendorCompanyId(userDao.getCompanyIdByProfileName(companyProfileName));
		}
		Integer companyId = userDao.getCompanyIdByUserId(userId);
		pagination.setCompanyId(companyId);
		Map<String, Object> map = videoDAO.findVideos(pagination);
		List<VideoDTO> videoDtos = (List<VideoDTO>) map.get("list");
		for (VideoDTO videoDTO : videoDtos) {
			videoDTO.setUploadedDate(DateUtils.getUtcString(videoDTO.getCreatedTime()));
			videoDTO.setImagePath(server_path + videoDTO.getImagePath());
			/********** XNFR-255 *******/
			Integer receivedWhiteLabeledAssetId = damDao.getDamIdByVideoId(videoDTO.getId());
			if (receivedWhiteLabeledAssetId != null && receivedWhiteLabeledAssetId > 0) {
				WhiteLabeledContentDTO whiteLabeledContentDTO = whiteLabeledAssetDao
						.findSharedByVendorCompanyNameByAssetId(receivedWhiteLabeledAssetId);
				if (whiteLabeledContentDTO != null) {
					videoDTO.setWhiteLabeledAssetReceivedFromVendor(true);
					videoDTO.setWhiteLabeledAssetSharedByVendorCompanyName(
							whiteLabeledContentDTO.getWhiteLabeledContentSharedByVendorCompanyName());
				}
				/********** XNFR-255 *******/
			}
		}
		response.setStatusCode(200);
		List<VideoCategory> videoCategoriesList = videoDAO.listVideoCategories(companyId);
		map.put("categories", videoCategoriesList);
		response.setData(map);
		return response;
	}

	@Override
	public XtremandResponse findVideoFileByDamIdAndUserId(Integer id, Integer userId) {
		XtremandResponse response = new XtremandResponse();
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(
				"select v.alias from xt_video_files v,xt_dam d where d.video_id = v.id and d.id = :damId");
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("damId", id));
		String alias = (String) hibernateSQLQueryResultUtilDao.getUniqueResult(hibernateSQLQueryResultRequestDTO);
		if (StringUtils.hasText(alias)) {
			VideoFileDTO videoFileDTO = getVideoByAlias(userId, alias, VideoFile.TYPE.DRAFT, "");
			response.setData(videoFileDTO);
			response.setStatusCode(200);
			response.setAccess(videoFileDTO.isAccess());
		} else {
			response.setStatusCode(404);
		}
		return response;
	}

}