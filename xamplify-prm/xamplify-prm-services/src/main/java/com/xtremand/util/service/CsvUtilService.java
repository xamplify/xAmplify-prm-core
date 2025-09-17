package com.xtremand.util.service;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringEscapeUtils;
import org.springframework.stereotype.Service;

import com.xtremand.campaign.dto.ClickedUrlAnalyticsDTO;
import com.xtremand.campaign.exception.XamplifyDataAccessException;
import com.xtremand.flexi.fields.dto.FlexiFieldResponseDTO;
import com.xtremand.user.bom.AllUsersView;
import com.xtremand.userlist.exception.UserListException;
import com.xtremand.util.XamplifyUtils;

@Service
public class CsvUtilService {

	private String contentDisposition = "Content-Disposition";

	private String filePrepender = "attachment; file=";

	private String textCsv = "text/csv";

	public void downloadCsv(HttpServletResponse response, String fileName, List<ClickedUrlAnalyticsDTO> data) {

		try {
			response.setContentType(textCsv);
			response.setHeader(contentDisposition, filePrepender + fileName + ".csv");
			PrintWriter writer = response.getWriter();
			writer.write("URL, COUNT\n");
			for (ClickedUrlAnalyticsDTO dto : data) {
				writer.write(dto.getUrl() + "," + dto.getClickedCount() + "\n");
			}
		} catch (IOException e) {
			throw new XamplifyDataAccessException(e);
		}
	}

	public void downloadUsers(HttpServletResponse response, String fileName, List<AllUsersView> data) {

		try {
			response.setContentType(textCsv);
			response.setHeader(contentDisposition, filePrepender + fileName + ".csv");
			String emptyString = "";
			String seperator = ",";
			String emptyData = seperator + emptyString + seperator + emptyString + seperator + emptyString + seperator
					+ emptyString + seperator + emptyString + seperator + emptyString;
			PrintWriter writer = response.getWriter();
			writer.write(
					"FIRSTNAME, LASTNAME, COMPANY, JOBTITLE, EMAILID, ADDRESS, CITY, STATE,  ZIP CODE, COUNTRY,  MOBILE NUMBER\n");
			for (AllUsersView user : data) {
				writer.write(user.getFirstName() + seperator + user.getLastName() + seperator + emptyString + seperator
						+ emptyString + seperator + user.getEmailId() + emptyData + "\n");
			}
		} catch (IOException e) {
			throw new XamplifyDataAccessException(e);
		}
	}


	public void downLoadToCSV(HttpServletResponse response, String fileName, Map<String, String> fieldHeaderMapping,
			List<?> data) {
		try {
			response.setContentType(textCsv);
			response.setHeader(contentDisposition, "attachment; file=" + fileName + ".csv");
			PrintWriter writer = response.getWriter();

			// Write Headers
			String headers = String.join(",", fieldHeaderMapping.keySet());
			writer.write(headers + "\n");

			for (Object dataItem : data) {
				String row = "";
				for (String key : fieldHeaderMapping.keySet()) {
					Method method = dataItem.getClass().getMethod(fieldHeaderMapping.get(key));
					String value = StringEscapeUtils.escapeCsv(String.valueOf( method.invoke(dataItem)));
					if (value == null) {
						value = "";
					}
					row += value + ",";
				}
				writer.write(row + "\n");
			}

		} catch (IOException | NoSuchMethodException | SecurityException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException e) {
			throw new XamplifyDataAccessException(e);
		}
	}

	public void downloadDefaultCsv(HttpServletResponse response, String header) {
		try {
			response.setContentType(textCsv);
			PrintWriter writer = response.getWriter();
			writer.write(header);
		} catch (IOException e) {
			throw new XamplifyDataAccessException(e);
		}
	}

	public HttpServletResponse downloadPartnerListCsv(String fileName, HttpServletResponse response) {
		try {
			List<String[]> data = new ArrayList<>();
			data.add(new String[] { "FIRSTNAME", "LASTNAME","ACCOUNT NAME","ACCOUNT OWNER", "ACCOUNT SUB TYPE",
					"COMPANY", "COMPANY DOMAIN", "JOBTITLE", "EMAILID","WEBSITE", "VERTICAL", "REGION","TERRITORY", "TYPE", "CATEGORY",
					"ADDRESS", "CITY", "STATE", "ZIP", "COUNTRY", "MOBILE NUMBER" });

			return XamplifyUtils.generateCSV(fileName, response, data);
		} catch (IOException ioe) {
			throw new UserListException(ioe.getMessage());
		}
	}
	
	public HttpServletResponse downloadContactListCsv(String fileName, List<FlexiFieldResponseDTO> flexiFields,
			HttpServletResponse response) {
		try {
			List<String> fieldNames = new ArrayList<>();
			fieldNames.addAll(XamplifyUtils.defaultContactCsvHeaderColumns());
			fieldNames.add("CONTACT STATUS");
			if (XamplifyUtils.isNotEmptyList(flexiFields)) {
				fieldNames.addAll(XamplifyUtils.getFlexiFieldNames(flexiFields));
			}

			List<String[]> data = new ArrayList<>();
			data.add(fieldNames.toArray(new String[0]));
			
			return XamplifyUtils.generateCSV(fileName, response, data);
		} catch (IOException ioe) {
			throw new UserListException(ioe.getMessage());
		}
	}

}
