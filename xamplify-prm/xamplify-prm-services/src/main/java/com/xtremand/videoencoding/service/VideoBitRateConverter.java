package com.xtremand.videoencoding.service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.xtremand.common.bom.FindLevel;
import com.xtremand.dam.bom.Dam;
import com.xtremand.dam.dao.DamDao;
import com.xtremand.dao.util.GenericDAO;
import com.xtremand.mail.service.EmailConstants;
import com.xtremand.mail.service.MailService;
import com.xtremand.team.member.dto.RoleDisplayDTO;
import com.xtremand.user.bom.User;
import com.xtremand.util.XamplifyUtils;
import com.xtremand.util.service.UtilService;
import com.xtremand.video.bom.VideoFile;
import com.xtremand.video.dao.VideoDao;
import com.xtremand.videoencoding.exception.VideoEncodingException;

@Component
public class VideoBitRateConverter {
	private static final Logger logger = LoggerFactory.getLogger(VideoBitRateConverter.class);

	@Value("${outFiles}")
	String outPutFormat;

	@Value("${bitRates}")
	String bitRates;

	@Autowired
	FFMPEGService ffmpegService;

	String inFile;
	FFMPEGStatus status;
	List<String> bitRateList;

	@Autowired
	private MailService mailService;

	@Autowired
	GenericDAO genericDAO;

	@Autowired
	UtilService utilService;

	@Autowired
	VideoDao videoDAO;

	@Autowired
	private DamDao damDao;
	

	private static final String ONE_ZERO_EIGHT_ZERO_P = "1920*1080";

	private static final String SEVENT_TWENTY_P = "1280*720";

	private static final String FOUR_EIGHTY_P = "854*480";

	private static final String THREE_SIXTY_P = "640*360";

	private static final String TWO_FORTY_P = "426*240";

	public void process(String inFile, int currentBitRate, FFMPEGStatus status, VideoFile video, User user, Dam dam) {
		try {
			this.inFile = inFile;
			this.status = status;
			logger.info("BitRate conversion started for file : {}", inFile);

			this.bitRateList = removeHigherBitRates(currentBitRate);

			createBitRateFiles();

			createM3U8andTSFiles();

			createCommonM3U8File();

			VideoFile videoObj = videoDAO.findByPrimaryKey(video.getId(), new FindLevel[] { FindLevel.VIDEO_PATH });
			videoObj.setProcessed(true);
			genericDAO.merge(videoObj);
			damDao.updateDamProcessingStatus(dam.getId(), user.getUserId());
			/** XNFR-885 **/
			if (XamplifyUtils.isValidInteger(dam.getApprovalReferenceId())) {
				damDao.updateDamProcessingStatus(dam.getApprovalReferenceId(), user.getUserId());
			}
			RoleDisplayDTO roleDisplayDto = utilService.getRoleDetailsByUserId(user.getUserId());
			if (!roleDisplayDto.isPrm() && !roleDisplayDto.isPrmAndPartner()
					&& !roleDisplayDto.isPrmAndPartnerTeamMember() && !roleDisplayDto.isPrmOrPrmAndPartnerCompany()
					&& !roleDisplayDto.isPrmSuperVisor() && !roleDisplayDto.isPrmTeamMember()) {
				mailService.sendProcessCompletedMail(user, EmailConstants.VIDEO_PROCESS_COMPLETED, videoObj);
			} else {
				mailService.sendProcessCompletedMail(user, EmailConstants.PRM_VIDEO_PROCESS_COMPLETED, videoObj);
			}

		} catch (Exception e) {
			logger.debug(e.getMessage());
		} finally {
			logger.info("BitRate conversion completed for file : {} ", inFile);
                }
        }


	private void createCommonM3U8File() throws IOException {
		/*
		 * #EXTM3U #EXT-X-STREAM-INF:PROGRAM-ID=1,BANDWIDTH=256000
		 * infographwithintro240.m3u8 #EXT-X-STREAM-INF:PROGRAM-ID=1,BANDWIDTH=512000
		 * infographwithintro360.m3u8 #EXT-X-STREAM-INF:PROGRAM-ID=1,BANDWIDTH=1024000
		 * infographwithintro480.m3u8 #EXT-X-STREAM-INF:PROGRAM-ID=1,BANDWIDTH=1536000
		 * infographwithintro720.m3u8 #EXT-X-STREAM-INF:PROGRAM-ID=1,BANDWIDTH=2048000
		 * infographwithintro1080.m3u8
		 */

		StringBuilder mainm3u8Str = new StringBuilder("#EXTM3U");
		mainm3u8Str.append("\n");

		for (String bitRate : bitRateList) {
			String ext = bitRate.substring(bitRate.indexOf("*") + 1);
			File file = new File(inFile.substring(0, inFile.lastIndexOf(".")) + ext + "_mobinar.m3u8");

			Map<Integer, Integer> bandwidthMap = new HashMap<>();
			bandwidthMap.put(240, 256000);
			bandwidthMap.put(360, 512000);
			bandwidthMap.put(480, 1024000);
			bandwidthMap.put(720, 1536000);
			bandwidthMap.put(1080, 2048000);

			Map<Integer, String> videoPixelMap = new HashMap<Integer, String>();
			videoPixelMap.put(240, "426x240");
			videoPixelMap.put(360, "640x360");
			videoPixelMap.put(480, "854x480");
			videoPixelMap.put(720, "1280x720");
			videoPixelMap.put(1080, "1920x1080");

			mainm3u8Str.append("#EXT-X-STREAM-INF:PROGRAM-ID=1,BANDWIDTH=" + bandwidthMap.get(Integer.parseInt(ext))
					+ ",RESOLUTION=" + videoPixelMap.get(Integer.parseInt(ext)));

			/*
			 * if("240".equals(ext)){
			 * mainm3u8Str.append("#EXT-X-STREAM-INF:PROGRAM-ID=1,BANDWIDTH=256000"); }
			 * if("360".equals(ext)){
			 * mainm3u8Str.append("#EXT-X-STREAM-INF:PROGRAM-ID=1,BANDWIDTH=512000"); }
			 * if("480".equals(ext)){
			 * mainm3u8Str.append("#EXT-X-STREAM-INF:PROGRAM-ID=1,BANDWIDTH=1024000"); }
			 * if("720".equals(ext)){
			 * mainm3u8Str.append("#EXT-X-STREAM-INF:PROGRAM-ID=1,BANDWIDTH=1536000"); }
			 * if("1080".equals(ext)){
			 * mainm3u8Str.append("#EXT-X-STREAM-INF:PROGRAM-ID=1,BANDWIDTH=2048000"); }
			 */
			mainm3u8Str.append("\n");
			mainm3u8Str.append(file.getName());
			mainm3u8Str.append("\n");
		}

		File mainM3U8File = new File(inFile.substring(0, inFile.lastIndexOf(".")) + "_mobinar.m3u8");
		FileWriter writer = new FileWriter(mainM3U8File);
		writer.write(mainm3u8Str.toString());
		writer.close();
	}

	private void createM3U8andTSFiles() throws VideoEncodingException {
		for (String bitRate : bitRateList) {
			String ext = bitRate.substring(bitRate.indexOf("*") + 1) + outPutFormat;

			String outfile = inFile.substring(0, inFile.lastIndexOf(".")) + ext;

			String m3u8outfile = outfile.substring(0, outfile.lastIndexOf(".")) + "_mobinar.m3u8";
			String tsOutfile = outfile.substring(0, outfile.lastIndexOf(".")) + "%03d.ts";

			String m3u8Command = " -i " + outfile
					+ " -codec copy -vbsf h264_mp4toannexb -map 0 -f segment -segment_list  " + m3u8outfile
					+ " -segment_time 5 " + tsOutfile;

			ffmpegService.executeFFMPEGCommand(m3u8Command, status);
		}

	}

	private void createBitRateFiles() throws VideoEncodingException {
		for (String bitRate : bitRateList) {
			String ext = bitRate.substring(bitRate.indexOf("*") + 1) + outPutFormat;

			String outfile = inFile.substring(0, inFile.lastIndexOf(".")) + ext;

			String cmd = " -i " + inFile + " -vf scale=" + bitRate + " -acodec aac -strict -2 " + outfile;

			ffmpegService.executeFFMPEGCommand(cmd, status);
		}
	}

	public void createBitRateFiles(String filePath) throws VideoEncodingException {
		this.bitRateList = Arrays.asList(ONE_ZERO_EIGHT_ZERO_P, SEVENT_TWENTY_P, FOUR_EIGHTY_P, THREE_SIXTY_P,
				TWO_FORTY_P);
		for (String bitRate : bitRateList) {
			String ext = bitRate.substring(bitRate.indexOf("*") + 1) + outPutFormat;
			String outfile = filePath.substring(0, filePath.lastIndexOf(".")) + ext;
			String cmd = " -i " + filePath + " -vf scale=" + bitRate + " -acodec aac -strict -2 " + outfile;
			ffmpegService.executeFFMPEGCommand(cmd, new FFMPEGStatus());
		}
	}

	private List<String> removeHigherBitRates(int currentBitRate) {
		List<String> rates = new ArrayList<String>();
		for (String bitRate : bitRates.split(",")) {
			if (Integer.parseInt(bitRate.substring(bitRate.indexOf("*") + 1)) <= currentBitRate) {
				rates.add(bitRate);
			}
		}
		return rates;
	}
}
