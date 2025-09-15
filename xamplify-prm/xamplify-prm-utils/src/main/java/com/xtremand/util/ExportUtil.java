package com.xtremand.util;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.net.ssl.HttpsURLConnection;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.xtremand.campaign.exception.XamplifyDataAccessException;

@Component
public class ExportUtil {

	private static final Logger logger = LoggerFactory.getLogger(ExportUtil.class);

	@Value("${server_path}")
	private String serverPath;

	@Value("${spring.profiles.active}")
	private String profiles;

	@Value("${landscape}")
	private String landscape;

	@Autowired
	private XamplifyUtil xamplifyUtil;

	public void downloadFile(HttpServletResponse response, String updatedTemplateBody, String completeFileName,
			String fileNameToDownload) throws IOException {
		Files.write(Paths.get(completeFileName), updatedTemplateBody.getBytes());
		try (InputStream inputStream = new FileInputStream(completeFileName)) {
			response.setContentType("application/force-download");
			response.setHeader("Content-Disposition", "attachment; filename=" + fileNameToDownload);
			IOUtils.copy(inputStream, response.getOutputStream());
			response.flushBuffer();
		} catch (IOException e) {
			throw new XamplifyDataAccessException(e);
		} finally {
			Path filePath = Paths.get(completeFileName);
			Files.delete(filePath);
		}
	}

	public void downloadPdf(HttpServletResponse response, String completeFileName, String fileNameToDownload,
			String htmlFileName) throws IOException {
		response.setContentType("application/pdf");
		response.setHeader("Content-disposition", "attachment;filename=" + fileNameToDownload);
		String filePath = "";
		if ("dev".equals(profiles)) {
			filePath = "D:\\" + completeFileName;
		} else {
			filePath = completeFileName;
		}
		File f = new File(filePath);
		try (FileInputStream fileIn = new FileInputStream(f)) {
			DataOutputStream os = new DataOutputStream(response.getOutputStream());
			response.setHeader("Content-Length", String.valueOf(f.length()));
			byte[] buffer = new byte[1024];
			int len = 0;
			while ((len = fileIn.read(buffer)) >= 0) {
				os.write(buffer, 0, len);
			}
		} // fileIn is closed
		catch (IOException e) {
			throw new XamplifyDataAccessException(e);
		} finally {
			Files.delete(Paths.get(completeFileName));
			if (StringUtils.hasText(htmlFileName)) {
				Files.delete(Paths.get(htmlFileName));
			}

		}
	}

	public void generatePdfByHtmlFilePath(String htmlFileCompleteName, String pdfFileCompleteName,
			String pdfFileNameToDownload, HttpServletResponse response, String updatedTemplateBody) throws IOException {
		Files.write(Paths.get(htmlFileCompleteName), updatedTemplateBody.getBytes());
		File htmlSource = new File(htmlFileCompleteName);
		File pdfDest = new File(pdfFileCompleteName);
		ConverterProperties converterProperties = new ConverterProperties();
		FileInputStream inputStream = new FileInputStream(htmlSource);
		HtmlConverter.convertToPdf(inputStream, new FileOutputStream(pdfDest), converterProperties);
		inputStream.close();
		downloadPdf(response, pdfFileCompleteName, pdfFileNameToDownload, htmlFileCompleteName);

	}

	public void generatePdfByHtmlString(String pdfFileCompleteName, String pdfFileNameToDownload,
			HttpServletResponse response, String updatedTemplateBody) throws IOException {
		File pdfDest = new File(pdfFileCompleteName);
		HtmlConverter.convertToPdf(updatedTemplateBody, new FileOutputStream(pdfDest));
		downloadPdf(response, pdfFileCompleteName, pdfFileNameToDownload, null);
	}

	public void generatePdfBySelectedCustomSize(String htmlFileCompleteName, String pdfFileCompleteName,
			String pdfFileNameToDownload, HttpServletResponse response, String updatedTemplateBody, String pageSize,
			String pageOrientation) throws IOException {
		boolean isPortraitMode = !landscape.equalsIgnoreCase(pageOrientation);
		Files.write(Paths.get(htmlFileCompleteName), updatedTemplateBody.getBytes());
		File htmlSource = new File(htmlFileCompleteName);
		File pdfDest = new File(pdfFileCompleteName);
		PdfDocument pdf = new PdfDocument(new PdfWriter(pdfDest));
		switch (pageSize) {
		case "A0":
			setPortraitOrLandScapeSize(isPortraitMode, pdf, PageSize.A0);
			break;
		case "A1":
			setPortraitOrLandScapeSize(isPortraitMode, pdf, PageSize.A1);
			break;

		case "A2":
			setPortraitOrLandScapeSize(isPortraitMode, pdf, PageSize.A2);
			break;

		case "A3":
			setPortraitOrLandScapeSize(isPortraitMode, pdf, PageSize.A3);
			break;

		case "A4":
			setPortraitOrLandScapeSize(isPortraitMode, pdf, PageSize.A4);
			break;

		case "A5":
			setPortraitOrLandScapeSize(isPortraitMode, pdf, PageSize.A5);
			break;

		case "A6":
			setPortraitOrLandScapeSize(isPortraitMode, pdf, PageSize.A6);
			break;

		case "A7":
			setPortraitOrLandScapeSize(isPortraitMode, pdf, PageSize.A7);
			break;

		case "A8":
			setPortraitOrLandScapeSize(isPortraitMode, pdf, PageSize.A8);
			break;

		case "A9":
			setPortraitOrLandScapeSize(isPortraitMode, pdf, PageSize.A9);
			break;

		case "A10":
			setPortraitOrLandScapeSize(isPortraitMode, pdf, PageSize.A10);
			break;

		case "B0":
			setPortraitOrLandScapeSize(isPortraitMode, pdf, PageSize.B0);
			break;

		case "B1":
			setPortraitOrLandScapeSize(isPortraitMode, pdf, PageSize.B1);
			break;

		case "B2":
			setPortraitOrLandScapeSize(isPortraitMode, pdf, PageSize.B2);
			break;

		case "B3":
			setPortraitOrLandScapeSize(isPortraitMode, pdf, PageSize.B3);
			break;

		case "B4":
			setPortraitOrLandScapeSize(isPortraitMode, pdf, PageSize.B4);
			break;

		case "B5":
			setPortraitOrLandScapeSize(isPortraitMode, pdf, PageSize.B5);
			break;

		case "B6":
			setPortraitOrLandScapeSize(isPortraitMode, pdf, PageSize.B6);
			break;

		case "B7":
			setPortraitOrLandScapeSize(isPortraitMode, pdf, PageSize.B7);
			break;

		case "B8":
			setPortraitOrLandScapeSize(isPortraitMode, pdf, PageSize.B8);
			break;

		case "B9":
			setPortraitOrLandScapeSize(isPortraitMode, pdf, PageSize.B9);
			break;

		case "B10":
			setPortraitOrLandScapeSize(isPortraitMode, pdf, PageSize.B10);
			break;

		default:
			setPortraitOrLandScapeSize(isPortraitMode, pdf, PageSize.A4);
			break;
		}

		InputStream stream = new FileInputStream(htmlSource);
		ConverterProperties converterProperties = new ConverterProperties();
		HtmlConverter.convertToPdf(stream, pdf, converterProperties);
		stream.close();
		downloadPdf(response, pdfFileCompleteName, pdfFileNameToDownload, htmlFileCompleteName);
	}

	private void setPortraitOrLandScapeSize(boolean isPortraitMode, PdfDocument pdf, PageSize a0) {
		if (isPortraitMode) {
			pdf.setDefaultPageSize(a0);
		} else {
			pdf.setDefaultPageSize(a0.rotate());
		}
	}

	/******* XNFR-525 *****/
	public void downloadPng(HttpServletResponse response, String updatedTemplateBody, String completeFileName,
			String fileNameToDownload) throws IOException {
		try {
			logger.debug("Generating image...");
			HttpsURLConnection connection = xamplifyUtil.generateImageByHtmlBody(updatedTemplateBody);
			int responseCode = connection.getResponseCode();
			if (responseCode == 200) {
				InputStream inStream = connection.getInputStream();
				if (inStream != null) {
					File targetFile = new File(completeFileName);
					FileUtils.copyInputStreamToFile(inStream, targetFile);
					response.setContentType("application/force-download");
					response.setHeader("Content-Disposition", "attachment; filename=" + fileNameToDownload);
					writeFile(response, completeFileName);
				}
			} else {
				String errorMessage = "Failed to generate image - BEE Response Code : " + responseCode;
				response.setContentType("text/plain");
				response.setHeader("Content-Disposition", null);
				logger.error(errorMessage);
			}
		} catch (IOException e) {
			response.setContentType("text/plain");
			response.setHeader("Content-Disposition", null);
			String ioExceptionErrorMessage = "Failed to generate image : " + e.getMessage();
			logger.error(ioExceptionErrorMessage);
		}
	}

	/******* XNFR-525 *****/
	private void writeFile(HttpServletResponse response, String completeFileName) throws IOException {
		File f = new File(completeFileName);
		try (FileInputStream fileIn = new FileInputStream(f)) {
			DataOutputStream os = new DataOutputStream(response.getOutputStream());
			response.setHeader("Content-Length", String.valueOf(f.length()));
			byte[] buffer = new byte[1024];
			int len = 0;
			while ((len = fileIn.read(buffer)) >= 0) {
				os.write(buffer, 0, len);
			}
		} // fileIn is closed
		catch (IOException e) {
			response.setContentType("text/plain");
			response.setHeader("Content-Disposition", null);
			throw new XamplifyDataAccessException(e);
		} finally {
			Path filePath = Paths.get(completeFileName);
			Files.delete(filePath);

		}
	}
}
