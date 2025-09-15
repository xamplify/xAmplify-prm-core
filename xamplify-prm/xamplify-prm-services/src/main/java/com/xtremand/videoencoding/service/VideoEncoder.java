package com.xtremand.videoencoding.service;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.xtremand.video.formbeans.VideoFileUploadForm;
import com.xtremand.videoencoding.exception.VideoEncodingException;

/**
 *
 * @author 
 */
@Component
public class VideoEncoder{
	
	private  static final  Logger logger = LoggerFactory.getLogger(VideoEncoder.class); 
	
	@Value("${outFiles}")
	String outputFormat;
	
	@Value("${images_path}")
	String images_path;
	
	@Value("${server_path}")
	String server_path;
	
	@Value("${separator}")
	String sep;
	
	@Autowired
	FFMPEGService ffmpegService;

	String inputFile;
	FFMPEGStatus status;
	final int MAX_GIF_FILES = 3;
	
	public VideoFileUploadForm encodeVideoFile(FFMPEGStatus status, VideoFileUploadForm form) throws VideoEncodingException{
		this.inputFile = form.getVideoPath();
		this.status = status;
		try{
			if(!inputFile.endsWith(outputFormat)){
				String outFilePath = inputFile.substring(0, inputFile.lastIndexOf('.'))+outputFormat;

				logger.info("converting "+inputFile+" to "+outFilePath);

				String command = " -i " + inputFile + " -y -ar 44100 -b 1200K -ab 128K -vcodec h264 -acodec aac -strict -2 " + outFilePath;
				
				logger.info("command :" + command);
				ffmpegService.executeFFMPEGCommand(command, status);

				new File(inputFile).delete();
				
				this.inputFile = outFilePath;
				
				logger.info("convertion completed.");
			}
			
			form.setVideoPath(this.inputFile);
			
			setVideoProperties(form);

			addPlayIconToVideo(form);

		}catch(Exception e){
			logger.error(e.getMessage());
			throw new VideoEncodingException(e.getMessage());
		}
		return form;
	}

	private void addPlayIconToVideo(VideoFileUploadForm form) throws VideoEncodingException {
		String outFile = inputFile.substring(0, inputFile.lastIndexOf('.'))+"_play"+outputFormat;
		logger.info("addPlayIconToVideo called with infile = "+inputFile+" and outFile : "+outFile);
		
		String command = " -i "+inputFile+" -i "+images_path+"play.png -filter_complex overlay='(main_w-overlay_w)/2:(main_h-overlay_h)/2' -codec:a copy "+outFile;
		
		ffmpegService.executeFFMPEGCommand(command, status);
		logger.info("successfully added play icon to the video "+outFile);
		
		form.setGifFiles(generateGIFFiles(outFile));
	}
	
	private List<String> generateGIFFiles(String filePath) throws VideoEncodingException{
		List<String> gifFiles = new ArrayList<>();
		for(int i = 1; i<MAX_GIF_FILES+1; i++){
			
			String outputFile = filePath.substring(0, filePath.lastIndexOf("."))+i+".gif";
			outputFile = outputFile.replaceFirst("videos", "images");
			String command = "-i "+filePath+" -ss "+(i*5)+" -t 5 -vf scale=480:-1:flags=lanczos,fps=10 "+outputFile;
			
			ffmpegService.executeFFMPEGCommand(command, status);
			
			String imagesSerPath = server_path+outputFile.substring(outputFile.indexOf("images"));
			gifFiles.add(imagesSerPath);
		}
		return gifFiles;
	}
	
	private void setVideoProperties(VideoFileUploadForm form) throws VideoEncodingException{
		try{
			String command = " -v error -of flat=s=_ -select_streams v:0 -show_entries stream=height,width,duration -print_format json "+form.getVideoPath();
			
			JSONObject jsonObject = ffmpegService.executeFFProbeCommand(command);
			
			logger.info(jsonObject.toString());
			
			JSONArray array = jsonObject.getJSONArray("streams");
			JSONObject object = array.getJSONObject(0); 
			
			form.setVideoLength(Double.valueOf(object.getDouble("duration")).intValue());
			form.setBitRate(object.getInt("height"));
			form.setWidth(object.getInt("width"));
			
		}catch(Exception e){
			throw new VideoEncodingException("error occured while fetching height, width, duration");
		}
	}
	
	public String generateThumbnailImage(String videoPath, String timeStamp, Integer videoId) throws VideoEncodingException{
		String imagePath = videoPath.substring(0, videoPath.lastIndexOf(sep));
		
		imagePath = imagePath+sep+videoId+"_"+Calendar.getInstance().getTimeInMillis()+timeStamp.replaceAll(":", "")+".jpg";
		imagePath = imagePath.replaceAll("videos", "images");
		
		String command = "-i "+videoPath+" -ss "+timeStamp+" -vframes 1 "+imagePath;
		ffmpegService.executeFFMPEGCommand(command, status);

		return server_path+imagePath.substring(imagePath.lastIndexOf("images"));
	}
}
