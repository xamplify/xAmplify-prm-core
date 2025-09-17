package com.xtremand.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.xtremand.campaign.exception.XamplifyDataAccessException;
import com.xtremand.contacts.dto.ContactFieldsDTO;
import com.xtremand.flexi.fields.dto.FlexiFieldRequestDTO;
import com.xtremand.form.submit.dto.FormSubmitDTO;
import com.xtremand.formbeans.UserDTO;
import com.xtremand.formbeans.XtremandResponse;
import com.xtremand.mail.bom.Logo;
import com.xtremand.mail.bom.UnZippedHtml;

import au.com.bytecode.opencsv.CSVWriter;

@Component
public class FileUtil {

	@Value("${separator}")
	String sep;

	@Value("${server_path}")
	String serverPath;

	@Value("${local.server.vod.path}")
	private String localServerVodPath;

	@Value("${server.video.gif}")
	String videoGifPath;

	@Value("${dev.cobranding.image}")
	String devCoBrandingImage;

	@Value("${prod.cobranding.image}")
	String prodCoBrandingImage;

	@Value("${release.cobranding.image}")
	String releaseCoBrandingImage;

	@Value("${dev.video.gif.tag}")
	String devVideoGifTag;

	@Value("${prod.video.gif.tag}")
	String prodVideoGifTag;

	@Value("${release.video.gif.tag}")
	String releaseVideoGifTag;

	@Value("${dev.campaign.url}")
	String devCampaignUrl;

	@Value("${prod.campaign.url}")
	String prodCampaignUrl;

	@Value("${release.campaign.url}")
	String releaseCampaignUrl;

	@Value("${server.cobranding.image}")
	String coBrandingImage;

	@Value("${image.file.types}")
	String imageFileTypes;

	@Value("${special.characters}")
	String specialCharacters;

	@Value("${formFilesPath}")
	String formFilesPath;

	@Value("${spring.profiles.active}")
	private String profiles;

	@Value("${media_base_path}")
	String mediaBasePath;

	@Value("${dam.file.max.size}")
	String damMaxFileSizeInString;

	@Value("${highLevelAnalytics.download.path}")
	private String highLevelAnalyticsExcelDownloadPath;

	@Value("${specialCharacters}")
	String regex;

	@Value("${contacts.csv.path}")
	private String contactsCsvPath;

	@Value("${media_base_path}")
	private String vodPath;

	@Value("${signatures.path}")
	private String signaturesPath;

	private static final String CURRENT_DATE = new SimpleDateFormat("ddMMyyyy").format(new Date());

	private static final Logger logger = LoggerFactory.getLogger(FileUtil.class);

	public UnZippedHtml copyZipFile(MultipartFile file, String zipDirectoryPath, String unzippedDirectoryPath) {
		String zipFilePath = "";
		File zipDirectory = new File(zipDirectoryPath);
		if (!zipDirectory.exists()) {
			zipDirectory.mkdirs();
		}
		String fileName = file.getOriginalFilename();
		String updatedFileName = updateOriginalFileName(fileName);
		zipFilePath = zipDirectoryPath + sep + updatedFileName;
		File zipFile = new File(zipFilePath);
		try (FileOutputStream fos = new FileOutputStream(zipFile)) {
			fos.write(file.getBytes());
			fos.flush();
		} catch (IOException e) {
			logger.error("Error In copyZipFile(" + file + "," + zipDirectoryPath + ")", e);
			throw new XamplifyDataAccessException(e.getMessage());
		}
		return unZipFile(zipFile, unzippedDirectoryPath);

	}

	public UnZippedHtml unZipFile(File file, String unzippedDirectoryPath) {
		UnZippedHtml html = new UnZippedHtml();
		File temp = new File(unzippedDirectoryPath);
		temp.mkdir();
		ZipFile zipFile = null;
		try {
			zipFile = new ZipFile(file);
			// get an enumeration of the ZIP file entries
			Enumeration<? extends ZipEntry> e = zipFile.entries();
			boolean isHtmlFound = false;
			List<String> imagesPath = new ArrayList<>();
			List<String> cssPaths = new ArrayList<>();
			while (e.hasMoreElements()) {
				ZipEntry entry = e.nextElement();
				if (entry.getName().indexOf("__MACOSX/") < 0) {
					File destinationPath = new File(unzippedDirectoryPath, entry.getName());
					// create parent directories
					destinationPath.getParentFile().mkdirs();
					// if the entry is a file extract it
					isHtmlFound = validateMultipleHtmls(html, isHtmlFound, entry, destinationPath);
					String path = destinationPath.getPath();
					String updatedDestinationPath = path.substring(path.lastIndexOf("/email-templates") + 1);
					addImagePaths(imagesPath, entry, updatedDestinationPath);
					addCssPaths(cssPaths, entry, updatedDestinationPath);
					if (entry.isDirectory()) {
						continue;
					} else {
						extractAllFiles(zipFile, entry, destinationPath);
					}
				}

			}
			html.setImages(imagesPath);
			html.setCssPath(cssPaths);
		} catch (IOException ioe) {
			logger.error("Error opening zip file(" + unzippedDirectoryPath + ")" + ioe);
		} finally {
			try {
				if (zipFile != null) {
					zipFile.close();
				}
			} catch (IOException ioe) {
				logger.error("Error closing zip file(" + unzippedDirectoryPath + ")" + ioe);
			}
		}
		return html;
	}

	private void addImagePaths(List<String> imagesPath, ZipEntry entry, String destinationPath) {
		boolean isJpg = entry.getName().endsWith("JPG") || entry.getName().endsWith("jpg")
				|| entry.getName().endsWith("gif");
		boolean isPng = entry.getName().endsWith("png") || entry.getName().endsWith("PNG");
		boolean isJpeg = entry.getName().endsWith("JPEG") || entry.getName().endsWith("jpeg")
				|| entry.getName().endsWith("pjpeg");
		if (isJpg || isPng || isJpeg) {
			imagesPath.add(destinationPath);
		}
	}

	private void addCssPaths(List<String> cssPaths, ZipEntry entry, String destinationPath) {
		if (entry.getName().endsWith("css")) {
			cssPaths.add(destinationPath);
		}
	}

	private boolean validateMultipleHtmls(UnZippedHtml html, boolean isHtmlFound, ZipEntry entry,
			File destinationPath) {
		if (entry.getName().endsWith("html") || entry.getName().endsWith("htm")) {
			if (isHtmlFound) {
				html.setMultipleHtmlsFound(true);
			} else {
				html.setHtmlFilePath(destinationPath.getPath());
				html.setMultipleHtmlsFound(false);
				isHtmlFound = true;
			}
		}
		return isHtmlFound;
	}

	private void extractAllFiles(ZipFile zipFile, ZipEntry entry, File destinationPath) throws IOException {
		BufferedInputStream bis = new BufferedInputStream(zipFile.getInputStream(entry));

		int b;
		byte buffer[] = new byte[1024];

		FileOutputStream fos = new FileOutputStream(destinationPath);

		BufferedOutputStream bos = new BufferedOutputStream(fos, 1024);

		while ((b = bis.read(buffer, 0, 1024)) != -1) {
			bos.write(buffer, 0, b);
		}

		bos.close();
		bis.close();
	}

	public String updateOriginalFileName(String fileName) {
		String appender = ".";
		return updateWithExtension(fileName, appender);
	}

	public String generateThumbnailName(String fileName) {
		String appender = "_thumbnail.";
		return updateWithExtension(fileName, appender);
	}

	private String updateWithExtension(String fileName, String appender) {
		String name = fileName.substring(0, fileName.lastIndexOf('.')) + "_" + System.currentTimeMillis();
		String extension = fileName.substring(fileName.lastIndexOf('.') + 1);
		return name + appender + extension;
	}

	public Logo validateTags(String html) {
		Logo logo = new Logo();
		Document doc = Jsoup.parse(html);
		Elements imgs = doc.select("img");
		int coBrandingLogsCount = 0;
		int videoTagsCount = 0;
		for (Element img : imgs) {
			String srcStr = img.attr("src").trim();
			coBrandingLogsCount = getCoBrandingLogosCount(srcStr, coBrandingLogsCount);
			videoTagsCount = getVideoTagsCount(srcStr, videoTagsCount);
		}
		logo.setCoBrandingLogsCount(coBrandingLogsCount);
		logo.setVideoTagsCount(videoTagsCount);
		return logo;
	}

	private int getCoBrandingLogosCount(String srcStr, Integer coBrandingLogsCount) {
		if (devCoBrandingImage.equals(srcStr) || prodCoBrandingImage.equals(srcStr)
				|| releaseCoBrandingImage.equals(srcStr)) {
			coBrandingLogsCount++;
		}
		return coBrandingLogsCount;
	}

	private int getVideoTagsCount(String srcStr, Integer videoTagsCount) {
		if (devVideoGifTag.equals(srcStr) || prodVideoGifTag.equals(srcStr) || releaseVideoGifTag.equals(srcStr)) {
			videoTagsCount++;
		}
		return videoTagsCount;
	}

	public String addDefaultGifPath() {
		return "<br><img src=" + serverPath + videoGifPath + ">";
	}

	public String addCoBrandingLogo() {
		return "<br><img src=" + serverPath + coBrandingImage + " style=\"background-color:black\">";
	}

	public String updateGifImageTagWithAnchor(String body) {
		Document doc = Jsoup.parse(body);
		String docHtml = doc.html();
		String imageTagPrefix = "<img src=\"";
		Elements imgs = doc.select("img");
		for (Element img : imgs) {
			String srcStr = img.attr("src").trim();
			if (devVideoGifTag.equals(srcStr)) {
				docHtml = docHtml.replaceAll(imageTagPrefix + devVideoGifTag + "\">", devCampaignUrl);
			} else if (prodVideoGifTag.equals(srcStr)) {
				docHtml = docHtml.replaceAll(imageTagPrefix + prodVideoGifTag + "\">", prodCampaignUrl);
			} else if (releaseVideoGifTag.equals(srcStr)) {
				docHtml = docHtml.replaceAll(imageTagPrefix + releaseVideoGifTag + "\">", releaseCampaignUrl);
			}
		}
		return docHtml;
	}

	public void createDirectory(String filePath) {
		File directory = new File(filePath);
		if (!directory.exists()) {
			directory.mkdirs();
		}
	}

	public String updateFileNameWithUserId(String originalFileName, Integer userId) {
		String modifiedName = originalFileName.replaceAll(specialCharacters, "");
		String fileName = modifiedName.substring(0, modifiedName.lastIndexOf('.'));
		String type = modifiedName.substring(modifiedName.lastIndexOf('.'));
		return fileName + "_" + userId + type;
	}

	public List<String> getArrayList(String string) {
		return new ArrayList<>(Arrays.asList(string.toLowerCase().replace(" ", "").trim().split(",")));
	}

	public String getFileExtension(MultipartFile file) {
		return getFileExtension(file.getOriginalFilename());
	}

	public String getFileExtension(String fileName) {
		if (fileName.lastIndexOf('.') != -1 && fileName.lastIndexOf('.') != 0)
			return (fileName.substring(fileName.lastIndexOf('.') + 1)).toLowerCase();
		else
			return "";
	}

	public XtremandResponse validateFileType(XtremandResponse response, MultipartFile file) {
		String extension = getFileExtension(file);
		if (!StringUtils.hasText(extension)) {
			response.setStatusCode(400);
			response.setMessage("File extension missing for " + file.getOriginalFilename());
			return response;
		}
		if (getArrayList(imageFileTypes).indexOf(extension.toLowerCase()) < 0) {
			response.setStatusCode(1024);
			response.setMessage("Invalid file type " + file.getOriginalFilename());
			return response;
		}
		response.setStatusCode(200);
		return response;
	}

	public String formFilePath(MultipartFile file, FormSubmitDTO formSubmitDTO) {
		Integer formId = formSubmitDTO.getId();
		final String dateFolder = getDateFolderPattern();
		if (formId != null) {
			String directoryPath = mediaBasePath + formFilesPath + sep + formId + sep + dateFolder;
			File directory = new File(directoryPath);
			if (!directory.exists()) {
				directory.mkdirs();
			}
			String fileName = file.getOriginalFilename();
			String updatedFileName = updateOriginalFileName(fileName);
			String completeFilePath = directoryPath + sep + updatedFileName;
			File uploadedFile = new File(completeFilePath);
			try (FileOutputStream fos = new FileOutputStream(uploadedFile)) {
				fos.write(file.getBytes());
				fos.flush();
			} catch (IOException e) {
				logger.error("Error In formFilePath(" + file + "," + directoryPath + "," + completeFilePath + ")", e);
				throw new XamplifyDataAccessException(e);
			}
			return completeFilePath.replace(mediaBasePath, "");
		} else {
			return "";
		}

	}

	private String getDateFolderPattern() {
		return new SimpleDateFormat("ddMMyyyy").format(new Date()) + sep + System.currentTimeMillis();
	}

	public void validateFileSize(MultipartFile file, XtremandResponse response, String module) {
		long size = file.getSize();
		long fileSize = size / 1024;
		Integer maxFileSize = null;
		String fileSizeInString = "DAM".equals(module) ? damMaxFileSizeInString : "1024";
		if ("DAM".equals(module)) {
			maxFileSize = Integer.valueOf(fileSizeInString);
		}
		if (fileSize > maxFileSize) {
			response.setStatusCode(400);
			response.setMessage("The maximum file size is " + fileSizeInString + "  MB.");
		}
	}

	public boolean isVideoFile(String fileName) {
		String[] videoTypesArr = { "m4v", "mkv", "avi", "mpg", "mp4", "flv", "mov", "wmv", "divx", "f4v", "mpeg", "vob",
				"xvid", "webm" };
		List<String> allowedVideoTypes = new ArrayList<>();
		for (String videoType : videoTypesArr) {
			allowedVideoTypes.add(videoType);
		}
		String fileType = getFileExtension(fileName);
		boolean isVideoFile = allowedVideoTypes.contains(fileType);
		return isVideoFile;
	}

	public boolean isVideoFileByType(String fileType) {
		String[] videoTypesArr = { "m4v", "mkv", "avi", "mpg", "mp4", "flv", "mov", "wmv", "divx", "f4v", "mpeg", "vob",
				"xvid", "webm" };
		List<String> allowedVideoTypes = new ArrayList<String>();
		for (String videoType : videoTypesArr) {
			allowedVideoTypes.add(videoType);
		}
		boolean isVideoFile = allowedVideoTypes.contains(fileType);
		return isVideoFile;
	}

	public String createHighLevelAnalyticsDirectory(Integer id, Integer userId, XSSFWorkbook workbook) {
		String directoryPath = highLevelAnalyticsExcelDownloadPath + sep + userId + sep + id;
		File directory = new File(directoryPath);
		if (!directory.exists()) {
			directory.mkdirs();
		}
		String excelFilePath = directoryPath + sep + "High-Level-Analytics.xlsx";
		File newexcelFile = new File(excelFilePath);
		try {
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			workbook.write(output);
			FileOutputStream outstream = new FileOutputStream(newexcelFile);
			workbook.write(outstream);
			outstream.close();
		} catch (Exception e) {
			throw new XamplifyDataAccessException(e);
		}
		return excelFilePath;

	}

	/*** XNFR-459 ****/
	public String uploadFileToXamplifyServerAndGetPath(MultipartFile file, String moduleName, String updatedFileName,
			Integer id) {
		String folderPath = mediaBasePath + sep + moduleName + sep + id;
		File imageDir = new File(folderPath);
		if (!imageDir.exists()) {
			imageDir.mkdirs();
		}
		String uploadedImageFilePath = folderPath + sep + updatedFileName;
		File newImageFile = new File(uploadedImageFilePath);
		if (!newImageFile.exists()) {
			try (FileOutputStream fileOutputStream = new FileOutputStream(newImageFile)) {
				fileOutputStream.write(file.getBytes());
				fileOutputStream.flush();
			} catch (Exception e) {
				logger.error("Error In Copying File To {} ", folderPath);
			}
		}
		return uploadedImageFilePath;
	}

	/*** XNFR-459 ****/
	public String updateFileNameWithTimeStampAndRemoveSpecialCharacters(String string) {
		return updateOriginalFileName(string).replaceAll(regex, "");
	}

	public String createContactsCsvDirectory() {
		String path = contactsCsvPath + sep + CURRENT_DATE + sep;
		createDirectory(path);
		return path;
	}

	public String uploadContactsCsvFile(String userListName, Set<UserDTO> users, Integer companyId) {
		List<String[]> list = new ArrayList<>();
		String[] headers = { "FIRSTNAME", "LASTNAME", "COMPANY", "JOBTITLE", "EMAILID", "ADDRESS", "CITY", "STATE",
				"ZIP CODE", "COUNTRY", "MOBILE NUMBER" };
		list.add(headers);
		logger.debug("Adding Data To Csv File.");
		for (UserDTO contactDto : users) {
			String[] input = { contactDto.getFirstName(), contactDto.getLastName(), contactDto.getContactCompany(),
					contactDto.getJobTitle(), contactDto.getEmailId(), contactDto.getAddress(), contactDto.getCity(),
					contactDto.getState(), contactDto.getZipCode(), contactDto.getCountry(),
					contactDto.getMobileNumber() };
			list.add(input);
		}
		String directoryPath = createContactsCsvDirectory();
		String updatedFileName = updateFileNameWithTimeStampAndRemoveSpecialCharacters(
				companyId + "-" + userListName + "contacts.csv");
		String fullPath = directoryPath + updatedFileName;
		if (XamplifyUtils.isNotEmptyList(list)) {
			try (CSVWriter writer = new CSVWriter(new FileWriter(fullPath))) {
				writer.writeAll(list);
				String debugMessage = "Data written to csv file : " + directoryPath;
				logger.debug(debugMessage);
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
		return fullPath.replace(vodPath, "");

	}

	public String uploadContactsToCsvFile(String userListName, Set<ContactFieldsDTO> users, Integer companyId) {
		List<String[]> list = new ArrayList<>();
		List<String> headersArrayList = new ArrayList<>();
		headersArrayList.add("FIRSTNAME");
		headersArrayList.add("LASTNAME");
		headersArrayList.add("COMPANY");
		headersArrayList.add("JOBTITLE");
		headersArrayList.add("EMAILID");
		headersArrayList.add("ADDRESS");
		headersArrayList.add("CITY");
		headersArrayList.add("STATE");
		headersArrayList.add("ZIP CODE");
		headersArrayList.add("COUNTRY");
		headersArrayList.add("MOBILE NUMBER");

		int counter = 1;
		for (ContactFieldsDTO contactDto : users) {
			for (FlexiFieldRequestDTO flexiFieldRequestDTO : contactDto.getFlexiFields()) {
				String fieldName = flexiFieldRequestDTO.getFieldName();
				headersArrayList.add(fieldName);
			}
			counter++;
			if (counter > 1) {
				break;
			}
		}

		String[] headers = XamplifyUtils.convertArrayListToStringArray(headersArrayList);
		list.add(headers);

		logger.debug("Adding Data To Csv File.");
		for (ContactFieldsDTO contactDto : users) {

			List<String> rowsData = new ArrayList<>();
			rowsData.add(contactDto.getFirstName());
			rowsData.add(contactDto.getLastName());
			rowsData.add(contactDto.getContactCompany());
			rowsData.add(contactDto.getJobTitle());
			rowsData.add(contactDto.getEmailId());
			rowsData.add(contactDto.getAddress());
			rowsData.add(contactDto.getCity());
			rowsData.add(contactDto.getState());
			rowsData.add(contactDto.getZipCode());
			rowsData.add(contactDto.getCountry());
			rowsData.add(contactDto.getMobileNumber());

			for (FlexiFieldRequestDTO flexiFieldRequestDTO : contactDto.getFlexiFields()) {
				String fieldValue = flexiFieldRequestDTO.getFieldValue();
				rowsData.add(fieldValue);
			}
			String[] rows = XamplifyUtils.convertArrayListToStringArray(rowsData);
			list.add(rows);
		}

		String directoryPath = createContactsCsvDirectory();
		String updatedFileName = updateFileNameWithTimeStampAndRemoveSpecialCharacters(
				companyId + "-" + userListName + "contacts.csv");
		String fullPath = directoryPath + updatedFileName;
		if (XamplifyUtils.isNotEmptyList(list)) {
			try (CSVWriter writer = new CSVWriter(new FileWriter(fullPath))) {
				writer.writeAll(list);
				String debugMessage = "Data written to csv file : " + directoryPath;
				logger.debug(debugMessage);
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
		return fullPath.replace(vodPath, "");
	}

	public String createSignatureFolderPath(String userAlias) {
		String path = signaturesPath + sep + userAlias;
		createDirectory(path);
		return path;
	}

	public String getPathForModifiedSharedAssetPdfPath(String moduleName, String modifiedPdfFileName,
			Integer loggedInUserId) {
		String folderPath = mediaBasePath + sep + moduleName + sep + loggedInUserId;
		File imageDir = new File(folderPath);
		if (!imageDir.exists()) {
			imageDir.mkdirs();
		}
		String uploadedImageFilePath = folderPath + sep + modifiedPdfFileName;
		File newImageFile = new File(uploadedImageFilePath);
		if (!newImageFile.exists()) {
			try (FileOutputStream fileOutputStream = new FileOutputStream(newImageFile)) {
				fileOutputStream.flush();
			} catch (Exception e) {
				logger.error("Error In Copying File To {} ", folderPath);
			}
		}
		return uploadedImageFilePath;

	}

	public File convertMultipartFileToFile(MultipartFile multipartFile) {
	    File convertedFile = null;
	    try {
	        convertedFile = File.createTempFile("uploaded-", ".pdf");
	        try (FileOutputStream fos = new FileOutputStream(convertedFile)) {
	            fos.write(multipartFile.getBytes());
	        }
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	    return convertedFile;
	}

}
