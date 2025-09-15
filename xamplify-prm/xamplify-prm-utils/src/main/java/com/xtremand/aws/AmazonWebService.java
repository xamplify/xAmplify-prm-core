package com.xtremand.aws;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.imageio.ImageIO;
import javax.net.ssl.HttpsURLConnection;

import org.apache.commons.io.FileUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.imgscalr.Scalr;
import org.imgscalr.Scalr.Method;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.CopyObjectRequest;
import com.amazonaws.services.s3.model.DeleteObjectsRequest;
import com.amazonaws.services.s3.model.DeleteObjectsRequest.KeyVersion;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.xtremand.activity.dto.ActivityAWSDTO;
import com.xtremand.campaign.exception.XamplifyDataAccessException;
import com.xtremand.dam.dto.DamAwsDTO;
import com.xtremand.dam.dto.DamUploadPostDTO;
import com.xtremand.domain.dto.DomainMediaResourceDTO;
import com.xtremand.util.DateUtils;
import com.xtremand.util.FileUtil;
import com.xtremand.util.XamplifyUtil;
import com.xtremand.util.XamplifyUtils;
import com.xtremand.util.dto.XamplifyConstants;

@Service
public class AmazonWebService {

	@Value("${amazon.access.id}")
	String amazonAccessKey;

	@Value("${amazon.secret.key}")
	String amazonsecretKey;

	@Value("${amazon.images.folder}")
	String imageFolder;

	@Value("${amazon.base.url}")
	String amazonBaseUrl;

	@Value("${amazon.env.folder}")
	String amazonEnvFolder;

	@Value("${amazon.email.templates.folder}")
	String amazonEmailTemplatesFolder;

	@Value("${amazon.landing.pages.folder}")
	String amazonLandingPagesFolder;

	@Value("${amazon.dam.bee.thumbnail.folder}")
	String amazonDamBeeThumbnailFolder;

	@Value("${amazon.campaign.email.templates.folder}")
	String amazonCampaignEmailTemplatesFolder;

	@Value("${amazon.previews.folder}")
	String amazonPreviewFolder;

	@Value("${spring.profiles.active}")
	private String profiles;

	@Value("${partner.email.template}")
	private String partnerEmailTemplate;

	@Value("${amazon.forms.folder}")
	String formFolder;

	@Value("${upload_content_path}")
	String uploadContentPath;

	@Value("${separator}")
	String sep;

	@Value("${specialCharacters}")
	String regex;

	@Value("${image.format.types}")
	String imageFormats;

	@Value("${amazon.bucket.name}")
	String amazonBucketName;

	@Value("${amazon.form.folder}")
	String amazonFormFolder;

	@Autowired
	private Environment environment;

	@Autowired
	private FileUtil fileUtil;

	@Autowired
	private XamplifyUtil xamplifyUtil;

	@Value("${amazon.lms.folder}")
	String amazonLmsFolder;

	@Value("${amazon.thumbnail.suffix}")
	private String amazonThumbnailSuffix;

	@Value("${amazon.thumbnails.folder}")
	String thumbnailFolder;

	@Value("${bee.message.key}")
	String beeMessageKey;

	@Value("#{new Integer('${image.width}')}")
	private Integer imageWidth;

	@Value("#{new Integer('${image.height}')}")
	private Integer imageHeight;
	
	/**XNFR-735**/
	@Value("${upload.attachment.path}")
	private String attachmentPath;
	
	@Value("${upload.company.domain.images.path}")
	private String uploadCompanyDomainImagesPath;
	
	@Value("${amazon.company.domain.brandfetch.folder}")
	private String amazonCompanyDomainBrandfetchFolder;

	DecimalFormat dec = new DecimalFormat("0.0");

	private static final Logger logger = LoggerFactory.getLogger(AmazonWebService.class);

	private static final String PRODUCTION = "production";

	public AmazonS3 getAmazonClient() {
		BasicAWSCredentials creds = new BasicAWSCredentials(amazonAccessKey, amazonsecretKey);
		return AmazonS3Client.builder().withRegion("us-east-1").withCredentials(new AWSStaticCredentialsProvider(creds))
				.build();
	}

	public List<String> uploadFiles(List<AmazonWebModel> amazonWebModels) {
		AmazonS3 s3Client = getAmazonClient();
		List<String> fileNames = new ArrayList<>();
		for (AmazonWebModel amazonWebModel : amazonWebModels) {
			String completeFileName = "";
			Integer companyId = amazonWebModel.getCompanyId();
			String category = amazonWebModel.getCategory();
			String uploadedFileName = amazonWebModel.getFileName();
			String uploadedFilePath = amazonWebModel.getFilePath();
			if (amazonWebModel.isGeneratedImages()) {
				if (PRODUCTION.equals(profiles)) {
					completeFileName = amazonPreviewFolder + companyId + xamplifyUtil.getSubFolderPath(category) + "/"
							+ uploadedFileName;
				} else {
					completeFileName = amazonEnvFolder + amazonPreviewFolder + companyId
							+ xamplifyUtil.getSubFolderPath(category) + "/" + uploadedFileName;
				}
			} else {
				completeFileName = amazonEnvFolder + imageFolder + companyId + xamplifyUtil.getSubFolderPath(category)
						+ "/" + uploadedFileName;
			}
			saveFileToAws(s3Client, completeFileName, uploadedFilePath, null);
			String debugMessage = "Image saved successfully." + completeFileName;
			logger.info(debugMessage);
			fileNames.add(completeFileName);
		}
		return fileNames;
	}

	private void saveFileToAws(AmazonS3 s3Client, String completeFileName, String uploadedFilePath, String fileType) {
		pushToAWS(s3Client, completeFileName, uploadedFilePath, fileType);
		Path filePath = Paths.get(uploadedFilePath);
		try {
			Files.delete(filePath);
			String debugMessage = filePath + " deleted successfully";
			logger.debug(debugMessage);
		} catch (IOException e) {
			logger.error("! Error Caught In Deleting File As The File Is Not Present !", e);
		}
	}

	private void pushToAWS(AmazonS3 s3Client, String completeFileName, String uploadedFilePath, String fileType) {
		PutObjectRequest request = new PutObjectRequest(getBucketName(), completeFileName, new File(uploadedFilePath));
		request.setCannedAcl(CannedAccessControlList.PublicRead);
		ObjectMetadata metadata = new ObjectMetadata();
		metadata.setContentType(XamplifyUtils.isValidString(fileType)? fileType : "plain/text");
		metadata.addUserMetadata("x-amz-meta-title", "someTitle");
		request.setMetadata(metadata);
		s3Client.putObject(request);
	}

	public List<AmazonWebModel> listItems(Integer companyId) {
		List<AmazonWebModel> list = new ArrayList<>();
		List<S3ObjectSummary> objects = getListObjects(companyId);
		for (S3ObjectSummary os : objects) {
			if (os.getSize() > 0) {
				AmazonWebModel awsWebModel = new AmazonWebModel();
				String key = "";
				if (StringUtils.hasText(os.getKey())) {
					key = os.getKey().replace("\\~", "%60").replace("@", "%40").replace("#", "%23").replace("$", "%24")
							.replace("^", "%5E").replace("&", "%26").replace("+", "%2B").replace("{", "%7B")
							.replace("=", "%3D").replace("[", "%5B").replace("]", "%5D").replace(";", "%3B")
							.replace(",", "%2C").replace("}", "%7D");
				} else {
					key = os.getKey();
				}
				String filePath = amazonBaseUrl + getBucketName() + '/' + os.getKey();
				String fileName = filePath.substring(filePath.lastIndexOf('/') + 1).trim();
				awsWebModel.setFilePath(filePath);
				awsWebModel.setDownloadLink(amazonBaseUrl + getBucketName() + '/' + key);
				awsWebModel.setFileName(fileName);
				String sizeInKb = dec.format(os.getSize() / 1024.0).concat(" KB");
				awsWebModel.setFilesizeInKb(sizeInKb);
				awsWebModel.setLastModifiedDate(os.getLastModified());
				String utcTimeString = DateUtils.getUtcTimeInString(os.getLastModified(),
						DateUtils.getServerTimeZone());
				awsWebModel.setUtcTimeString(utcTimeString);
				String type = fileName.substring(fileName.lastIndexOf('.') + 1).trim();
				awsWebModel.setType(type);
				awsWebModel.setKey(os.getKey());
				list.add(awsWebModel);
			}
		}
		return list;
	}

	public List<S3ObjectSummary> getListObjects(Integer companyId) {
		AmazonS3 amazonS3 = getAmazonClient();
		String folderPath = "";
		if (PRODUCTION.equals(profiles)) {
			folderPath = imageFolder + companyId;
		} else {
			folderPath = amazonEnvFolder + imageFolder + companyId;
		}
		ObjectListing result = amazonS3.listObjects(getBucketName(), folderPath);
		return result.getObjectSummaries();
	}

	public void deleteItem(AmazonWebModel amazonWebModel) {
		AmazonS3 amazonS3 = getAmazonClient();
		List<String> awsFileKeys = amazonWebModel.getAwsFileKeys();

		List<KeyVersion> keys = new ArrayList<>();
		for (String awsFileKey : awsFileKeys) {
			String key = amazonEnvFolder + imageFolder + amazonWebModel.getCompanyId()
					+ xamplifyUtil.getSubFolderPath(amazonWebModel.getCategory()) + "/" + awsFileKey;
			keys.add(new KeyVersion(key));
		}

		DeleteObjectsRequest request = new DeleteObjectsRequest(getBucketName()).withKeys(keys);
		amazonS3.deleteObjects(request);
	}

	public String getBucketName() {
		return environment.getProperty("amazon.bucket.name");
	}

	public String uploadFormContent(AmazonWebModel amazonWebModel, AmazonS3 amazonClient) {
		Integer companyId = amazonWebModel.getCompanyId();
		String uploadedFileName = amazonWebModel.getFileName();
		String uploadedFilePath = amazonWebModel.getFilePath();
		Integer formId = amazonWebModel.getFormId();
		String completeFileName = amazonEnvFolder + formFolder + '/' + companyId + '/' + "form-" + formId + '/'
				+ uploadedFileName;
		saveFileToAws(amazonClient, completeFileName, uploadedFilePath, null);
		return amazonBaseUrl + getBucketName() + '/' + completeFileName;
	}

	public FilePathAndThumbnailPath uploadFileToAwsAndGetPath(AWSInputDTO awsInputDTO) {
		try {
			MultipartFile file = awsInputDTO.getOriginalFie();
			Integer userId = awsInputDTO.getUserId();
			String filePathSuffix = awsInputDTO.getFilePathSuffix();
			String fileType = awsInputDTO.getFileType();
			MultipartFile thumbnailFile = awsInputDTO.getThumbnailFile();
			FilePathAndThumbnailPath filePathAndThumbnailPath = new FilePathAndThumbnailPath();
			String fileName = file.getOriginalFilename();
			CopiedFileDetails copiedFileDetails = new CopiedFileDetails();
			copyFileToXamplifyServer(file, userId, filePathSuffix, fileName, copiedFileDetails, false);
			AmazonS3 amazonClient = getAmazonClient();
			if (thumbnailFile != null) {
				generateThumbnailByFileAndPushToAWS(userId, filePathSuffix, thumbnailFile, filePathAndThumbnailPath,
						amazonClient);
			} else {
				generateThumbnailAndPushToAWS(filePathSuffix, fileType, filePathAndThumbnailPath, amazonClient,
						fileName, copiedFileDetails);
			}
			saveFileToAws(amazonClient, copiedFileDetails.getCompleteName(),
					copiedFileDetails.getCopiedImageFilePath(), null);
			filePathAndThumbnailPath
					.setFilePath(amazonBaseUrl + getBucketName() + '/' + copiedFileDetails.getCompleteName());
			return filePathAndThumbnailPath;
		} catch (FileNotFoundException e) {
			throw new XamplifyDataAccessException(e);
		} catch (IOException ex) {
			throw new XamplifyDataAccessException(ex);
		}

	}

	public FilePathAndThumbnailPath uploadFileToAwsAndGetPathNew(AWSInputDTO awsInputDTO) {
		try {
			Integer userId = awsInputDTO.getUserId();
			String filePathSuffix = awsInputDTO.getFilePathSuffix();
			MultipartFile thumbnailFile = awsInputDTO.getThumbnailFile();
			FilePathAndThumbnailPath filePathAndThumbnailPath = new FilePathAndThumbnailPath();
			if (thumbnailFile != null) {
				AmazonS3 amazonClient = getAmazonClient();
				generateThumbnailByFileAndPushToAWSNew(userId, filePathSuffix, thumbnailFile, filePathAndThumbnailPath,
						amazonClient);
			}
			return filePathAndThumbnailPath;
		} catch (Exception e) {
			throw new XamplifyDataAccessException(e);
		}
	}

	private void generateThumbnailByFileAndPushToAWS(Integer userId, String filePathSuffix, MultipartFile thumbnailFile,
			FilePathAndThumbnailPath filePathAndThumbnailPath, AmazonS3 amazonClient) {
		try {
			CopiedFileDetails copiedThumbnailDetails = new CopiedFileDetails();
			copyFileToXamplifyServer(thumbnailFile, userId, filePathSuffix, thumbnailFile.getOriginalFilename(),
					copiedThumbnailDetails, true);
			File copiedThumbnailFile = copiedThumbnailDetails.getFile();
			BufferedImage image = ImageIO.read(copiedThumbnailFile);
			if (image != null) {
				BufferedImage scaledImg = generateTrasparentThumbnailImage(image);
				File copyThumbnailFile = new File(copiedThumbnailDetails.getCopiedImageFilePath());
				ImageIO.write(scaledImg, "PNG", copyThumbnailFile);
				String completeThumbnailName = amazonEnvFolder + filePathSuffix + '/'
						+ copiedThumbnailDetails.getUpdatedFileName();
				saveFileToAws(amazonClient, completeThumbnailName, copiedThumbnailDetails.getCopiedImageFilePath(), null);
				filePathAndThumbnailPath
						.setThumbnailPath(amazonBaseUrl + getBucketName() + '/' + completeThumbnailName);
			}
		} catch (FileNotFoundException e) {
			throw new XamplifyDataAccessException(e);
		} catch (IOException ex) {
			throw new XamplifyDataAccessException(ex);
		}
	}

	private void generateThumbnailByFileAndPushToAWSNew(Integer userId, String filePathSuffix,
			MultipartFile thumbnailFile, FilePathAndThumbnailPath filePathAndThumbnailPath, AmazonS3 amazonClient) {
		try {
			CopiedFileDetails copiedThumbnailDetails = new CopiedFileDetails();
			copyFileToXamplifyServer(thumbnailFile, userId, filePathSuffix, thumbnailFile.getOriginalFilename(),
					copiedThumbnailDetails, false);
			File copiedThumbnailFile = copiedThumbnailDetails.getFile();
			BufferedImage image = ImageIO.read(copiedThumbnailFile);
			if (image != null) {
				BufferedImage scaledImg = generateTrasparentThumbnailImage(image);
				String updatedThumbnailName = copiedThumbnailDetails.getUpdatedFileName();
				String thumbnailFilePath = copiedThumbnailDetails.getFolderPath() + sep + updatedThumbnailName;
				File copyThumbnailFile = new File(thumbnailFilePath);
				ImageIO.write(scaledImg, "PNG", copyThumbnailFile);
				String completeThumbnailName = amazonEnvFolder + filePathSuffix + '/' + updatedThumbnailName
						+ amazonThumbnailSuffix;
				saveFileToAws(amazonClient, completeThumbnailName, thumbnailFilePath, null);
				filePathAndThumbnailPath
						.setThumbnailPath(amazonBaseUrl + getBucketName() + '/' + completeThumbnailName);
			}
		} catch (FileNotFoundException e) {
			throw new XamplifyDataAccessException(e);
		} catch (IOException ex) {
			throw new XamplifyDataAccessException(ex);
		}
	}

	private BufferedImage generateTrasparentThumbnailImage(BufferedImage image) {
		image = dropAlphaChannel(image);
		Dimension thumbnailDimension = getThumbnailDimesions(image.getWidth(), image.getHeight());
		int width = (int) thumbnailDimension.getWidth();
		int height = (int) thumbnailDimension.getHeight();
		return  Scalr.resize(image, Method.QUALITY, width, height, Scalr.OP_ANTIALIAS);
	}

	private BufferedImage dropAlphaChannel(BufferedImage originalImage) {
		BufferedImage thumbnailImage = new BufferedImage(originalImage.getWidth(), originalImage.getHeight(),
				BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = (Graphics2D) thumbnailImage.getGraphics();
		g2d.drawImage(originalImage, 0, 0, thumbnailImage.getWidth() - 1, thumbnailImage.getHeight() - 1, 0, 0,
				originalImage.getWidth() - 1, originalImage.getHeight() - 1, null);
		g2d.dispose();
		return thumbnailImage;
	}

	public void copyFileToXamplifyServer(MultipartFile file, Integer userId, String filePathSuffix, String fileName,
			CopiedFileDetails copiedFileDetails, boolean isThumbnail) throws IOException {
		final String currentDate = DateUtils.getDateFormatForUploadedFiles();
		String updatedFileName = "";
		if (isThumbnail) {
			updatedFileName = fileName;
		} else {
			updatedFileName = fileUtil.updateOriginalFileName(fileName).replaceAll(regex, "");
		}
		String folderPath = processAndSetFolderPath(userId, filePathSuffix, copiedFileDetails, currentDate,
				updatedFileName);
		File imageDir = new File(folderPath);
		if (!imageDir.exists()) {
			imageDir.mkdirs();
		}
		String imageFilePath = processAndSetImageFilePath(fileName, copiedFileDetails, updatedFileName, folderPath);
		File newImageFile = new File(imageFilePath);
		if (!newImageFile.exists()) {
			FileOutputStream fileOutputStream = new FileOutputStream(newImageFile);
			try {
				fileOutputStream.write(file.getBytes());
				fileOutputStream.flush();
				fileOutputStream.close();
			} finally {
				fileOutputStream.close();
			}
		}
		
		String completeFileName = "";
		if (copiedFileDetails.isFromDomainMediaResource()) {
			completeFileName = amazonCompanyDomainBrandfetchFolder + filePathSuffix + '/' + updatedFileName;
		} else {
			completeFileName = amazonEnvFolder + filePathSuffix + '/' + updatedFileName;
		}		
		copiedFileDetails.setCompleteName(completeFileName);
		copiedFileDetails.setFile(newImageFile);
		copiedFileDetails.setFolderPath(folderPath);
		copiedFileDetails.setCopiedImageFilePath(imageFilePath);
		copiedFileDetails.setUpdatedFileName(updatedFileName);
	}

	private String processAndSetFolderPath(Integer userId, String filePathSuffix, CopiedFileDetails copiedFileDetails,
			final String currentDate, String updatedFileName) {
		String folderPath = "";
		if (copiedFileDetails.isFromEmailActivity()) {
			folderPath = attachmentPath + userId + sep + updatedFileName;
		} else if (copiedFileDetails.isFromDomainMediaResource()) {
			folderPath = uploadCompanyDomainImagesPath + sep + filePathSuffix;
		} else {
			folderPath = uploadContentPath + currentDate + sep + userId;
		}
		return folderPath;
	}
	
	private String processAndSetImageFilePath(String fileName, CopiedFileDetails copiedFileDetails,
			String updatedFileName, String folderPath) {
		String imageFilePath = "";
		if (copiedFileDetails.isFromDomainMediaResource()) {
			imageFilePath = folderPath + sep + updatedFileName;
		} else {
			imageFilePath = folderPath + sep + fileName;
		}
		return imageFilePath;
	}

	@SuppressWarnings("deprecation")
	public void copyCloudFileToXamplifyServer(DamUploadPostDTO damUploadPostDTO, Integer userId, String filePathSuffix,
			String fileName, CopiedFileDetails copiedFileDetails, boolean isThumbnail) throws IOException {
		final String currentDate = DateUtils.getDateFormatForUploadedFiles();
		String updatedFileName = "";
		String downloadLink = damUploadPostDTO.getDownloadLink();
		if (isThumbnail) {
			updatedFileName = fileName;
		} else {
			updatedFileName = fileUtil.updateOriginalFileName(fileName).replaceAll(regex, "");
		}
		String folderPath = uploadContentPath + currentDate + sep + userId;
		File imageDir = new File(folderPath);
		if (!imageDir.exists()) {
			imageDir.mkdirs();
		}
		String imageFilePath = folderPath + sep + fileName;
		File newImageFile = new File(imageFilePath);

		if (!newImageFile.exists() && damUploadPostDTO.getOauthToken() == null) {
			downloadLink = downloadLink.replaceAll(" ", "%20");
			URL url = new URL(downloadLink);
			ReadableByteChannel rbc = Channels.newChannel(url.openStream());
			FileOutputStream fos = new FileOutputStream(newImageFile);
			fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
			fos.close();
			rbc.close();

		} else if (!newImageFile.exists() && damUploadPostDTO.getOauthToken() != null) {
			HttpClient httpclient = HttpClientBuilder.create().build();
			HttpGet httpget = new HttpGet(downloadLink);
			httpget.addHeader("Authorization", "Bearer " + damUploadPostDTO.getOauthToken());
			HttpResponse response = httpclient.execute(httpget);

			HttpEntity entity = response.getEntity();
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
		String completeFileName = amazonEnvFolder + filePathSuffix + '/' + updatedFileName;
		copiedFileDetails.setCompleteName(completeFileName);
		copiedFileDetails.setFile(newImageFile);
		copiedFileDetails.setFolderPath(folderPath);
		copiedFileDetails.setCopiedImageFilePath(imageFilePath);
		copiedFileDetails.setUpdatedFileName(updatedFileName);
	}

	public void copyFileToXampifyServerAndGenerateThumbnail(MultipartFile file, Integer userId, String filePathSuffix,
			String fileName, CopiedFileDetails copiedFileDetails, boolean isThumbnail) throws IOException {
		copyFileToXamplifyServer(file, userId, filePathSuffix, fileName, copiedFileDetails, isThumbnail);
		File copiedThumbnailFile = copiedFileDetails.getFile();
		String thumbnailFilePath = copiedFileDetails.getCopiedImageFilePath();
		String thumbnailName = copiedFileDetails.getUpdatedFileName();
		generateThumbnailAdnGetUpdatedName(file.getOriginalFilename(), filePathSuffix, copiedFileDetails,
				copiedThumbnailFile, thumbnailFilePath, thumbnailName);

	}

	public void generateThumbnailAdnGetUpdatedName(String fileName, String filePathSuffix,
			CopiedFileDetails copiedThumbnailDetails, File copiedThumbnailFile, String thumbnailFilePath,
			String thumbnailName) throws IOException {
		BufferedImage image = ImageIO.read(copiedThumbnailFile);
		if (image != null) {
			image = dropAlphaChannel(image);
			Dimension thumbnailDimension = getThumbnailDimesions(image.getWidth(), image.getHeight());
			int width = (int) thumbnailDimension.getWidth();
			int height = (int) thumbnailDimension.getHeight();
			BufferedImage scaledImg = Scalr.resize(image, Method.QUALITY, width, height, Scalr.OP_ANTIALIAS);
			File copyThumbnailFile = new File(thumbnailFilePath);
			ImageIO.write(scaledImg, "PNG", copyThumbnailFile);
			String completeThumbnailName = amazonEnvFolder + filePathSuffix + '/' + thumbnailName
					+ amazonThumbnailSuffix;
			copiedThumbnailDetails.setCompleteThumbnailName(completeThumbnailName);
		}
	}

	public void generateThumbnail(String fileName, String filePathSuffix, String fileType,
			CopiedFileDetails copiedAwsFileDetails) throws IOException {
		String folderPath = copiedAwsFileDetails.getFolderPath();
		File file = copiedAwsFileDetails.getFile();
		if (fileType != null && StringUtils.hasText(fileType)
				&& xamplifyUtil.convertStringToArrayListWithCommaSeperator(imageFormats).indexOf(fileType) > -1
				&& !"gif".equals(fileType)) {
			String thumbnailName = copiedAwsFileDetails.getUpdatedFileName();
			String thumbnailFilePath = folderPath + sep + thumbnailName;
			copiedAwsFileDetails.setThumbnailFilePath(thumbnailFilePath);
			generateThumbnailAdnGetUpdatedName(fileName, filePathSuffix, copiedAwsFileDetails, file, thumbnailFilePath,
					thumbnailName);
		} else {
			logger.debug("Invalid Image File");
		}

	}

	private void generateThumbnailAndPushToAWS(String filePathSuffix, String fileType,
			FilePathAndThumbnailPath filePathAndThumbnailPath, AmazonS3 amazonClient, String fileName,
			CopiedFileDetails copiedFileDetails) throws IOException {
		String folderPath = copiedFileDetails.getFolderPath();
		File file = copiedFileDetails.getFile();
		if (fileType != null && StringUtils.hasText(fileType)
				&& xamplifyUtil.convertStringToArrayListWithCommaSeperator(imageFormats).indexOf(fileType) > -1) {
			String thumbnailName = fileUtil.generateThumbnailName(fileName).replaceAll(regex, "");
			String thumbnailFilePath = folderPath + sep + thumbnailName;
			BufferedImage image = ImageIO.read(file);
			if (image != null) {
				image = dropAlphaChannel(image);
				Dimension thumbnailDimension = getThumbnailDimesions(image.getWidth(), image.getHeight());
				int width = (int) thumbnailDimension.getWidth();
				int height = (int) thumbnailDimension.getHeight();
				BufferedImage scaledImg = Scalr.resize(image, Method.QUALITY, width, height, Scalr.OP_ANTIALIAS);
				File thumbnailFile = new File(thumbnailFilePath);
				ImageIO.write(scaledImg, "PNG", thumbnailFile);
				String completeThumbnailName = amazonEnvFolder + filePathSuffix + '/' + thumbnailName;
				pushToAWS(amazonClient, completeThumbnailName, thumbnailFilePath, null);
				filePathAndThumbnailPath
						.setThumbnailPath(amazonBaseUrl + getBucketName() + '/' + completeThumbnailName);
			}
		} else {
			logger.debug("Invalid Image File");
		}
	}

	public FilePathAndThumbnailPath uploadAssetAndThumbnail(DamAwsDTO damAwsDTO) {
		AmazonS3 amazonClient = getAmazonClient();
		FilePathAndThumbnailPath filePathAndThumbnailPath = new FilePathAndThumbnailPath();
		String assetFileName = damAwsDTO.getCompleteAssetFileName();
		String thumbnailFileName = damAwsDTO.getCompleteThumbnailFileName();
		if(XamplifyUtils.isValidString(damAwsDTO.getFileType()) && damAwsDTO.getFileType().equals("pdf") && (damAwsDTO.isReplaceAsset() || damAwsDTO.getPartnerIds() != null || damAwsDTO.getPartnerGroupIds() != null)) {
			pushToAWS(amazonClient, assetFileName, damAwsDTO.getCopiedAssetFilePath(), null);
		} else {
			saveFileToAws(amazonClient, assetFileName, damAwsDTO.getCopiedAssetFilePath(), null);
		}
		filePathAndThumbnailPath.setFilePath(amazonBaseUrl + getBucketName() + '/' + assetFileName);
		String copiedThumbnailPath = damAwsDTO.getCopiedThumbnailFilePath();
		boolean isThumbnailFileExists = false;
		if (StringUtils.hasText(copiedThumbnailPath)) {
			File thumbnailFile = new File(copiedThumbnailPath);
			isThumbnailFileExists = thumbnailFile.exists();
		}
		if (StringUtils.hasText(thumbnailFileName) && isThumbnailFileExists) {
			saveFileToAws(amazonClient, thumbnailFileName, copiedThumbnailPath, null);
			filePathAndThumbnailPath.setThumbnailPath(amazonBaseUrl + getBucketName() + '/' + thumbnailFileName);
		}
		return filePathAndThumbnailPath;
	}

	public String uploadLMSFeaturedImage(String sourceFilePath, Integer companyId, String fileName) {
		AmazonS3 amazonClient = getAmazonClient();
		String destinationFileName = "";
		if (!PRODUCTION.equals(profiles)) {
			destinationFileName += amazonEnvFolder;
		}
		destinationFileName += amazonLmsFolder + companyId + "/" + fileName;
		saveFileToAws(amazonClient, destinationFileName, sourceFilePath, null);
		return amazonBaseUrl + amazonBucketName + "/" + destinationFileName;
	}

	public String copyLMSFeaturedImage(String sourceFileName, String destinationFileName, Integer companyId) {
		AmazonS3 amazonClient = getAmazonClient();
		String destinationFilePath = "";
		String sourceFilePath = "";
		if (!PRODUCTION.equals(profiles)) {
			destinationFilePath += amazonEnvFolder;
			sourceFilePath += amazonEnvFolder;
		}
		destinationFilePath += amazonLmsFolder + companyId + "/" + destinationFileName;
		sourceFilePath += amazonLmsFolder + companyId + "/" + sourceFileName;
		amazonClient.copyObject(amazonBucketName, sourceFilePath, amazonBucketName, destinationFilePath);
		return amazonBaseUrl + amazonBucketName + "/" + destinationFilePath;
	}

	public void deleteFeaturedImage(Integer companyId, String fileName) {
		AmazonS3 amazonS3 = getAmazonClient();
		String destinationFileName = "";
		if (!PRODUCTION.equals(profiles)) {
			destinationFileName += amazonEnvFolder;
		}
		destinationFileName += amazonLmsFolder + companyId + "/" + fileName;
		amazonS3.deleteObject(getBucketName(), destinationFileName);
	}

	public Dimension getThumbnailDimesions(int width, int height) {
		Dimension imgSize = new Dimension(width, height);
		Dimension boundary = new Dimension(imageWidth, imageHeight);

		int originalWidth = imgSize.width;
		int originalHeight = imgSize.height;
		int boundWidth = boundary.width;
		int boundHeight = boundary.height;
		int newWidth = originalWidth;
		int newHeight = originalHeight;

		// first check if we need to scale width
		if (originalWidth > boundWidth) {
			// scale width to fit
			newWidth = boundWidth;
			// scale height to maintain aspect ratio
			newHeight = (newWidth * originalHeight) / originalWidth;
		}

		// then check if we need to scale even with the new height
		if (newHeight > boundHeight) {
			// scale height to fit instead
			newHeight = boundHeight;
			// scale width to maintain aspect ratio
			newWidth = (newHeight * originalWidth) / originalHeight;
		}
		return new Dimension(newWidth, newHeight);

	}

	public String uploadPartnerTemplateImages(String filePathSuffix, String absolutePath, AmazonS3 amazonClient) {
		String completeThumbnailName = amazonEnvFolder + filePathSuffix + '/' + System.currentTimeMillis() + ".png";
		saveFileToAws(amazonClient, completeThumbnailName, absolutePath, null);
		return amazonBaseUrl + getBucketName() + '/' + completeThumbnailName;
	}

	public String uploadFormThumbnailImage(String sourceFilePath, String destinationFilePath) {
		AmazonS3 amazonClient = getAmazonClient();
		destinationFilePath = amazonEnvFolder + amazonFormFolder + destinationFilePath;
		saveFileToAws(amazonClient, destinationFilePath, sourceFilePath, null);
		return amazonBaseUrl + amazonBucketName + "/" + destinationFilePath;
	}

	public String copyFormThumbnailImage(String sourceFilePath, String destinationFilePath) {
		AmazonS3 amazonClient = getAmazonClient();
		destinationFilePath = amazonEnvFolder + amazonFormFolder + destinationFilePath;
		sourceFilePath = amazonEnvFolder + amazonFormFolder + sourceFilePath;
		amazonClient.copyObject(amazonBucketName, sourceFilePath, amazonBucketName, destinationFilePath);
		return amazonBaseUrl + amazonBucketName + "/" + destinationFilePath;
	}

	public void deleteFormThumbnailImage(String destinationFileName) {
		AmazonS3 amazonS3 = getAmazonClient();
		destinationFileName = amazonEnvFolder + amazonFormFolder + destinationFileName;
		amazonS3.deleteObject(getBucketName(), destinationFileName);
	}

	public FilePathAndThumbnailPath uploadThumbnail(DamAwsDTO damAwsDTO) {
		AmazonS3 amazonClient = getAmazonClient();
		FilePathAndThumbnailPath filePathAndThumbnailPath = new FilePathAndThumbnailPath();
		String thumbnailFileName = damAwsDTO.getCompleteThumbnailFileName();
		String copiedThumbnailPath = damAwsDTO.getCopiedThumbnailFilePath();
		boolean isThumbnailFileExists = false;
		if (StringUtils.hasText(copiedThumbnailPath)) {
			File thumbnailFile = new File(copiedThumbnailPath);
			isThumbnailFileExists = thumbnailFile.exists();
		}
		if (StringUtils.hasText(thumbnailFileName) && isThumbnailFileExists) {
			saveFileToAws(amazonClient, thumbnailFileName, copiedThumbnailPath, null);
			filePathAndThumbnailPath.setThumbnailPath(amazonBaseUrl + getBucketName() + '/' + thumbnailFileName);
		}
		return filePathAndThumbnailPath;
	}

	/*
	 * Inserting Content into the dam table API. This method needs to be removed
	 */
	public void generateContentThumbnail(String fileName, String filePathSuffix, String fileType,
			CopiedFileDetails copiedAwsFileDetails) throws IOException {
		String folderPath = copiedAwsFileDetails.getFolderPath();
		File file = copiedAwsFileDetails.getFile();
		if (fileType != null && StringUtils.hasText(fileType)
				&& xamplifyUtil.convertStringToArrayListWithCommaSeperator(imageFormats).indexOf(fileType) > -1
				&& !"gif".equals(fileType)) {
			String thumbnailName = fileName;
			String thumbnailFilePath = folderPath + sep + copiedAwsFileDetails.getUpdatedFileName();
			copiedAwsFileDetails.setThumbnailFilePath(thumbnailFilePath);
			generateThumbnailAdnGetUpdatedName(fileName, filePathSuffix, copiedAwsFileDetails, file, thumbnailFilePath,
					thumbnailName);
		} else {
			logger.debug("Invalid Image File");
		}

	}

	public void deleteThumbnail(AmazonWebModel amazonWebModel) {
		AmazonS3 amazonS3 = getAmazonClient();
		List<String> awsFileKeys = amazonWebModel.getAwsFileKeys();

		List<KeyVersion> keys = new ArrayList<>();
		for (String awsFileKey : awsFileKeys) {
			String key = amazonEnvFolder + thumbnailFolder + amazonWebModel.getCompanyId() + "/" + awsFileKey;
			keys.add(new KeyVersion(key));
		}

		DeleteObjectsRequest request = new DeleteObjectsRequest(getBucketName()).withKeys(keys);
		amazonS3.deleteObjects(request);
	}

	public void deleteAssetOrThumbnail(List<KeyVersion> keys) {
		if (keys != null && !keys.isEmpty()) {
			AmazonS3 amazonS3 = getAmazonClient();
			DeleteObjectsRequest request = new DeleteObjectsRequest(getBucketName()).withKeys(keys);
			amazonS3.deleteObjects(request);
		}

	}

	/******** XNFR-128 *********/
	public String uploadFileToAWS(AmazonWebModel amazonWebModel) {
		AmazonS3 s3Client = getAmazonClient();
		String completeFileName = "";
		String xamplifyFilePath = amazonWebModel.getFilePath();
		if (XamplifyConstants.DASHBOARD_BANNERS.equals(amazonWebModel.getCategory())) {
			completeFileName = amazonEnvFolder + XamplifyConstants.DASHBOARD_BANNERS + sep
					+ amazonWebModel.getCompanyId() + sep + amazonWebModel.getId() + sep + amazonWebModel.getFileName();
		} else if (XamplifyConstants.UPLOADED_CONTACTS.equals(amazonWebModel.getCategory())) {
			completeFileName = amazonEnvFolder + XamplifyConstants.UPLOADED_CONTACTS + sep
					+ amazonWebModel.getCompanyId() + sep + amazonWebModel.getId() + sep + amazonWebModel.getFileName();
		} else {
			completeFileName = amazonEnvFolder + amazonWebModel.getFolderSuffixPath();
		}
		saveFileToAws(s3Client, completeFileName, xamplifyFilePath, null);
		return getAwsFullPath(completeFileName);
	}

	private String getAwsFullPath(String completeFileName) {
		return amazonBaseUrl + amazonBucketName + "/" + completeFileName;
	}

	/*********** XNFR-255 *********/
	public String copyFormThumbnailImageFromOneFolderToAnotherFolder(String sourceThumbnailImagePath,
			Integer destinationThumbnailImageCompanyId) {
		String moduleFolder = amazonFormFolder;
		String destinationPathPrefix = amazonEnvFolder + moduleFolder + sep + destinationThumbnailImageCompanyId + sep
				+ "thumbnail_";
		return copyTheObject(sourceThumbnailImagePath, destinationThumbnailImageCompanyId, moduleFolder,
				destinationPathPrefix);

	}

	/*********** XNFR-255 *********/
	public String copyAssetImageFromOneFolderToAnotherFolder(String sourceThumbnailImagePath,
			Integer destinationThumbnailImageCompanyId) {
		String moduleFolder = "images";
		String destinationPathPrefix = amazonEnvFolder + moduleFolder + sep + destinationThumbnailImageCompanyId + sep;
		return copyTheObject(sourceThumbnailImagePath, destinationThumbnailImageCompanyId, moduleFolder,
				destinationPathPrefix);

	}

	/*********** XNFR-255 *********/
	public String copyAssetThumbnailImageFromOneFolderToAnotherFolder(String sourceThumbnailImagePath,
			Integer destinationThumbnailImageCompanyId) {
		String moduleFolder = "thumbnail";
		String destinationPathPrefix = amazonEnvFolder + moduleFolder + sep + destinationThumbnailImageCompanyId + sep
				+ "thumbnail_";
		return copyTheObject(sourceThumbnailImagePath, destinationThumbnailImageCompanyId, moduleFolder,
				destinationPathPrefix);

	}

	private String copyTheObject(String sourceThumbnailImagePath, Integer destinationThumbnailImageCompanyId,
			String moduleFolder, String destinationPathPrefix) {
		String destinationThumbnailPath = "";
		String errorMessage = "Error in copyTheObject(" + sourceThumbnailImagePath + ","
				+ destinationThumbnailImageCompanyId + "," + moduleFolder + "," + destinationPathPrefix + ")";
		try {
			if (StringUtils.hasText(sourceThumbnailImagePath)) {
				String thumbnailImageExtension = XamplifyUtils.getFileExtension(sourceThumbnailImagePath);
				String destinationPath = destinationPathPrefix + System.currentTimeMillis() + "."
						+ thumbnailImageExtension;
				String thumbnailImageS3BucketPath = "";
				if (xamplifyUtil.isProduction()) {
					/**** XBI-1925 ****/
					if (sourceThumbnailImagePath.indexOf("previews/") > -1) {
						thumbnailImageS3BucketPath = sourceThumbnailImagePath
								.substring(sourceThumbnailImagePath.lastIndexOf("previews/"));
					} else {
						thumbnailImageS3BucketPath = sourceThumbnailImagePath
								.substring(sourceThumbnailImagePath.lastIndexOf(moduleFolder));
					}
				} else {
					thumbnailImageS3BucketPath = sourceThumbnailImagePath
							.substring(sourceThumbnailImagePath.lastIndexOf(amazonEnvFolder));
				}
				String bucketName = getBucketName();
				CopyObjectRequest request = new CopyObjectRequest(bucketName, thumbnailImageS3BucketPath, bucketName,
						destinationPath);
				AmazonS3 s3Client = getAmazonClient();
				s3Client.copyObject(request);
				String awsPrefixPath = amazonBaseUrl + sep + bucketName + sep;
				destinationThumbnailPath = awsPrefixPath + destinationPath;
				String debugMessage = awsPrefixPath + sourceThumbnailImagePath + " copied to "
						+ destinationThumbnailPath;
				logger.debug(debugMessage);
			}
		} catch (AmazonServiceException e) {
			logger.error(errorMessage, e);
		} catch (SdkClientException e) {
			logger.error(errorMessage, e);
		} catch (Exception e) {
			logger.error(errorMessage, e);
		}
		return destinationThumbnailPath;
	}

	/********* XNFR-255 *******/
	public String generateThumbnailImageByHtmlBodyAndCompanyIdAndType(String htmlBody, Integer companyId, String type) {
		String thumbnailPath = "";
		if (companyId != null && companyId > 0 && StringUtils.hasText(htmlBody)) {
			String errorMessage = "Error in generateThumbnailImageByHtmlBodyAndCompanyId(" + companyId + ","
					+ thumbnailPath + ")";
			String fileName = companyId + "-" + System.currentTimeMillis() + "-white-labeled.png";
			try {
				HttpsURLConnection connection = xamplifyUtil.callBeeRestApi(htmlBody);
				int responseCode = connection.getResponseCode();
				if (responseCode == 200) {
					InputStream inStream = connection.getInputStream();
					if (inStream != null) {
						logger.debug("Saving generated image...");
						File tempFile = File.createTempFile("img_temp", null);
						FileUtils.copyInputStreamToFile(inStream, tempFile);
						List<AmazonWebModel> amazonWebModels = new ArrayList<>();
						AmazonWebModel amazonWebModel = new AmazonWebModel();
						amazonWebModel.setCompanyId(companyId);
						amazonWebModel.setFilePath(tempFile.getAbsolutePath());
						amazonWebModel.setFileName(fileName);
						amazonWebModel.setCategory(type);
						amazonWebModel.setGeneratedImages(true);
						amazonWebModels.add(amazonWebModel);
						uploadFiles(amazonWebModels);
						if (PRODUCTION.equals(profiles)) {
							thumbnailPath = amazonBaseUrl + amazonBucketName + "/" + amazonPreviewFolder + companyId
									+ xamplifyUtil.getSubFolderPath(type) + "/" + fileName;
						} else {
							thumbnailPath = amazonBaseUrl + amazonBucketName + "/" + amazonEnvFolder
									+ amazonPreviewFolder + companyId + xamplifyUtil.getSubFolderPath(type) + "/"
									+ fileName;
						}
						String debugMessage = fileName + " is successfully saved into " + thumbnailPath;
						logger.debug(debugMessage);
						inStream.close();
					}
				} else {
					logger.error("Failed to generate image - BEE Response Code : " + responseCode);
				}

			} catch (AmazonServiceException e) {
				logger.error(errorMessage, e);
			} catch (SdkClientException e) {
				logger.error(errorMessage, e);
			} catch (IOException e) {
				logger.error("Failed to generate image : " + e.getMessage());
			} catch (Exception e) {
				logger.error(errorMessage, e);
			}
		}
		if (!StringUtils.hasText(thumbnailPath)) {
			if ("whiteLabeledLandingPages" == type) {
				thumbnailPath = "assets/images/pages.png";
			}
		}
		return thumbnailPath;

	}

	public void uploadDataToAws(String amazonFilePath, String localFilePath) {
		AmazonS3 s3Client = getAmazonClient();
		saveFileToAws(s3Client, amazonFilePath, localFilePath, null);
	}
	
	/**XNFR-735**/
	public String uploadAttachmentAndGetFilePath(ActivityAWSDTO activityAWSDTO) {
		AmazonS3 amazonClient = getAmazonClient();
		String fileName = activityAWSDTO.getCompleteFileName();
		saveFileToAws(amazonClient, fileName, activityAWSDTO.getFilePath(), activityAWSDTO.getFileType());
		return amazonBaseUrl + getBucketName() + '/' + fileName;
	}
	
	public void deleteFilesByUsingFilePaths(List<String> filePaths) {
		AmazonS3 amazonS3 = getAmazonClient();
		String bucketName = getBucketName();
		for (String filePath : filePaths) {
			try {
				if (XamplifyUtils.isValidString(filePath)) {
					amazonS3.deleteObject(bucketName, filePath);
					String debugMessage = "Deleted file (" + filePath + ") from aws at " + new Date();
					logger.debug(debugMessage);
				}
			} catch (Exception e) {
				String debugMessage = "Failed to the file (" + filePath + ") from aws at " + new Date();
				logger.debug(debugMessage);
			}
		}
	}
	
	/** XNFR-780 **/
	public String uploadDomainMediaResourcesAndGetFilePath(DomainMediaResourceDTO domainMediaResourceDTO) {
		AmazonS3 amazonClient = getAmazonClient();
		String fileName = domainMediaResourceDTO.getCompleteFileName();
		saveFileToAws(amazonClient, fileName, domainMediaResourceDTO.getFilePath(), domainMediaResourceDTO.getFileType());
		return amazonBaseUrl + getBucketName() + '/' + fileName;
	}
	

	/** Implemented for PRM project **/
	public boolean validateAWSCredentials() {
		String accessKeyId = amazonAccessKey.trim();
		String secretAccessKey = amazonsecretKey.trim();
		AmazonS3 s3Client = null;
        try {
        	if (XamplifyUtils.isValidString(secretAccessKey) && XamplifyUtils.isValidString(accessKeyId)) {
        		BasicAWSCredentials creds = new BasicAWSCredentials(accessKeyId, secretAccessKey);
                s3Client = AmazonS3ClientBuilder.standard()
                        .withRegion("us-east-1")
                        .withCredentials(new AWSStaticCredentialsProvider(creds))
                        .build();
                s3Client.listBuckets();
                return true;
        	} else {
        		return false;
        	}
        } catch (Exception e) {
            logger.debug("Unexpected error validating AWS credentials. Message: {}, Timestamp: {}", e.getMessage(), new Date());
            return false;
        } finally {
            if (s3Client != null) {
                s3Client.shutdown();
            }
        }
    }
	
	
	
	
	
	
	
	
	

}
