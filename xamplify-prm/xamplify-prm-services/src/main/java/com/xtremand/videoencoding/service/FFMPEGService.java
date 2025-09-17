package com.xtremand.videoencoding.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.xtremand.util.XamplifyUtil;
import com.xtremand.videoencoding.exception.VideoEncodingException;

@Component
public class FFMPEGService {
	private static final Logger logger = LoggerFactory.getLogger(FFMPEGService.class);

	@Value("${path.ffmpeg}")
	private String ffmpegPath;

	@Value("${path.ffprobe}")
	String ffprobePath;

	@Autowired
	private XamplifyUtil xamplifyUtil;

	public void executeFFMPEGCommand(String command, FFMPEGStatus status) throws VideoEncodingException {
		logger.debug("executing ffmpeg command {} ", command);
		Process encoderProcess = null;

		Runtime run = Runtime.getRuntime();
		try {
			if (xamplifyUtil.isDev()) {
				encoderProcess = run.exec(ffmpegPath + " " + command);
			} else {
				encoderProcess = run.exec("ffmpeg " + command);
			}

			WorkerViewStreamGobbler errGob = new WorkerViewStreamGobbler("encode", encoderProcess.getErrorStream(),
					status);
			WorkerViewStreamGobbler outGob = new WorkerViewStreamGobbler("encode", encoderProcess.getInputStream(),
					status);

			errGob.start();
			outGob.start();

			encoderProcess.waitFor();
			logger.debug("executing ffmpeg command completed");
		} catch (Exception e) {
			logger.error("something went wrong " + e.getMessage());
			throw new VideoEncodingException(e.getMessage());
		} finally {
			if (encoderProcess != null) {
				encoderProcess.destroy();
			}
			if (run != null) {
				run.freeMemory();
			}
		}
	}

	public JSONObject executeFFProbeCommand(String command) throws VideoEncodingException {
		logger.debug("executing ffprobe command");
		Process encoderProcess = null;
		Runtime run = Runtime.getRuntime();
		JSONObject jsonObject = null;
		try {
			if (xamplifyUtil.isDev()) {
				encoderProcess = run.exec(ffprobePath + " " + command);
			} else {
				encoderProcess = run.exec("ffprobe " + command);
			}

			BufferedReader streamReader = new BufferedReader(
					new InputStreamReader(encoderProcess.getInputStream(), "UTF-8"));
			StringBuilder responseStrBuilder = new StringBuilder();

			String inputStr;
			while ((inputStr = streamReader.readLine()) != null)
				responseStrBuilder.append(inputStr);

			jsonObject = new JSONObject(responseStrBuilder.toString());

			logger.debug("executing ffprobe command completed");
		} catch (Exception e) {
			logger.error("something went wrong " + e.getMessage());
			throw new VideoEncodingException(e.getMessage());
		} finally {
			if (encoderProcess != null) {
				encoderProcess.destroy();
			}
			if (run != null) {
				run.freeMemory();
			}
		}
		return jsonObject;
	}
}
