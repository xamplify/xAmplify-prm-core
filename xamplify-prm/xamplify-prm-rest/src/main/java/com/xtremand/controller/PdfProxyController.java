package com.xtremand.controller;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.poi.hslf.usermodel.HSLFShape;
import org.apache.poi.hslf.usermodel.HSLFSlide;
import org.apache.poi.hslf.usermodel.HSLFSlideShow;
import org.apache.poi.hslf.usermodel.HSLFTextShape;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFShape;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFTextShape;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;



@RestController
@RequestMapping("/api/pdf")
@CrossOrigin(origins = "*")
public class PdfProxyController {

	@GetMapping("/proxy")
	public void proxyFile(@RequestParam String pdfUrl, HttpServletResponse response) {
		try {
			String decodedUrl = URLDecoder.decode(pdfUrl, StandardCharsets.UTF_8.toString());
			URL url = new URL(decodedUrl);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			response.setHeader("Access-Control-Allow-Origin", "*");
			response.setContentType(conn.getContentType());
			InputStream inputStream = conn.getInputStream();
			org.apache.commons.io.IOUtils.copy(inputStream, response.getOutputStream());
			response.flushBuffer();
		} catch (Exception e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		}
	}
	@GetMapping("/convertCsvToPdf")
    public void convertCsvToPdf(@RequestParam String pdfUrl, HttpServletResponse response) {
        try {
            getUrlConnection(pdfUrl, response);
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }
	private void getUrlConnection(String pdfUrl, HttpServletResponse response) throws IOException, DocumentException {
		String decodedUrl = URLDecoder.decode(pdfUrl, StandardCharsets.UTF_8.toString());
		URL url = new URL(decodedUrl);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("GET");
		InputStream inputStream = conn.getInputStream();
		String csvContent = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
		ByteArrayOutputStream pdfOutputStream = convertCsvToPdf(csvContent);
		response.setHeader("Access-Control-Allow-Origin", "*");
		response.setContentType("application/pdf");
		response.setHeader("Content-Disposition", "attachment; filename=converted.pdf");
		response.setContentLength(pdfOutputStream.size());
		OutputStream out = response.getOutputStream();
		pdfOutputStream.writeTo(out);
		out.flush();
	}
	private ByteArrayOutputStream convertCsvToPdf(String csvContent) throws DocumentException, IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Document document = new Document();
        PdfWriter.getInstance(document, outputStream);
        document.open();
        PdfPTable table = new PdfPTable(getColumnCount(csvContent));
        table.setWidthPercentage(100);
        BufferedReader reader = new BufferedReader(new StringReader(csvContent));
        String line;
        boolean isHeader = true;
        while ((line = reader.readLine()) != null) {
            String[] values = line.split(",");

            for (String value : values) {
                PdfPCell cell = new PdfPCell(new Phrase(value.trim()));
                cell.setPadding(5);
                if (isHeader) cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                table.addCell(cell);
            }
            isHeader = false;
        }
        reader.close();
        document.add(table);
        document.close();
        return outputStream;
    }

    private int getColumnCount(String csvContent) throws IOException {
        BufferedReader reader = new BufferedReader(new StringReader(csvContent));
        String firstLine = reader.readLine();
        reader.close();
        return firstLine != null ? firstLine.split(",").length : 1;
    }
    
    @GetMapping("/convertDocToPdf")
    public void convertDocToPdf(@RequestParam String pdfUrl, HttpServletResponse response) {
        try {
            String decodedUrl = java.net.URLDecoder.decode(pdfUrl, "UTF-8");
            java.net.URL url = new java.net.URL(decodedUrl);
            InputStream inputStream = url.openStream();
            ByteArrayOutputStream pdfOutputStream = convertToPdf(inputStream, pdfUrl);
            response.setHeader("Access-Control-Allow-Origin", "*");
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "attachment; filename=converted.pdf");
            response.setContentLength(pdfOutputStream.size());
            OutputStream out = response.getOutputStream();
            pdfOutputStream.writeTo(out);
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    private ByteArrayOutputStream convertToPdf(InputStream inputStream, String filename) throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Document document = new Document();
        PdfWriter.getInstance(document, outputStream);
        document.open();

        Font font = new Font(Font.FontFamily.HELVETICA, 12); 
        String fileExtension = filename.toLowerCase();

        if (fileExtension.endsWith(".docx")) {
            XWPFDocument docx = new XWPFDocument(inputStream);
            for (XWPFParagraph para : docx.getParagraphs()) {
                String text = para.getText().trim();
                if (!text.isEmpty()) {
                    document.add(new Paragraph(text, font));
                }
            }
            docx.close();
        } else if (fileExtension.endsWith(".doc")) {
            HWPFDocument doc = new HWPFDocument(inputStream);
            WordExtractor extractor = new WordExtractor(doc);
            for (String paragraph : extractor.getParagraphText()) {
                String text = paragraph.trim();
                if (!text.isEmpty()) {
                    document.add(new Paragraph(text, font));
                }
            }
            extractor.close();
            doc.close();
        } else {
            throw new IllegalArgumentException("Unsupported file format: " + filename);
        }

        document.close();
        return outputStream;
    }

    @GetMapping("/convertPptxToPdf")
    public void convertPptOrPptxToPdf(@RequestParam String pdfUrl, HttpServletResponse response) {
        try {
            String decodedUrl = URLDecoder.decode(pdfUrl, StandardCharsets.UTF_8.name());
            URL url = new URL(decodedUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            try (InputStream remoteInputStream = conn.getInputStream();
                 ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[8192];
                int len;
                while ((len = remoteInputStream.read(buffer)) != -1) {
                    baos.write(buffer, 0, len);
                }
                byte[] fileBytes = baos.toByteArray();
//                InputStream in = new ByteArrayInputStream(fileBytes);
                response.setHeader("Access-Control-Allow-Origin", "*");
                response.setContentType("application/pdf");
                Document document = new Document();
                PdfWriter.getInstance(document, response.getOutputStream());
                document.open();
                if (decodedUrl.toLowerCase().endsWith(".pptx")) {
                    try (XMLSlideShow pptx = new XMLSlideShow(new ByteArrayInputStream(fileBytes))) {
               
                        changePptxToPdf(document, pptx);
                    }
                } else if (decodedUrl.toLowerCase().endsWith(".ppt")) {
                    try (HSLFSlideShow ppt = new HSLFSlideShow(new ByteArrayInputStream(fileBytes))) {
                        changePptToPdf(document, ppt);
                    }
                } else {
                    document.add(new Paragraph("Unsupported file type."));
                }
                document.close();
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            e.printStackTrace();
        }
    }
	private void changePptToPdf(Document document, HSLFSlideShow ppt) throws DocumentException {
		int slideCount = 1;
		for (HSLFSlide slide : ppt.getSlides()) {
		    document.add(new Paragraph("Slide " + slideCount++ + ":"));
		    for (HSLFShape shape : slide.getShapes()) {
		        if (shape instanceof HSLFTextShape) {
		            String text = ((HSLFTextShape) shape).getText();
		            if (text != null && !text.trim().isEmpty()) {
		                document.add(new Paragraph(text));
		            }
		        }
		    }
		    document.newPage();
		}
	}
	private void changePptxToPdf(Document document, XMLSlideShow pptx) throws DocumentException {
		int slideCount = 1;
		for (XSLFSlide slide : pptx.getSlides()) {
		    document.add(new Paragraph("Slide " + slideCount++ + ":"));
		    for (XSLFShape shape : slide.getShapes()) {
		        if (shape instanceof XSLFTextShape) {
		            String text = ((XSLFTextShape) shape).getText();
		            if (text != null && !text.trim().isEmpty()) {
		                document.add(new Paragraph(text));
		            }
		        }
		    }
		    document.newPage();
		}
	}
	@GetMapping("/convert-xlsx-to-pdf")
	public void convertXlsxToPdf(@RequestParam String pdfUrl, HttpServletResponse response) {
		try (InputStream inputStream = new URL(pdfUrl).openStream();
				Workbook workbook = new XSSFWorkbook(inputStream)) {

			// Set response headers
			response.setContentType("application/pdf");
			response.setHeader("Content-Disposition", "attachment; filename=converted.xlsx.pdf");

			Document document = new Document();
			PdfWriter.getInstance(document, response.getOutputStream());
			document.open();

			BaseFont baseFont = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
			Font boldFont = new Font(baseFont, 12, Font.BOLD);
			Font regularFont = new Font(baseFont, 10, Font.NORMAL);

			for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
				Sheet sheet = workbook.getSheetAt(i);
				document.add(new Paragraph("Sheet: " + sheet.getSheetName(), boldFont));
				document.add(new Paragraph(" "));

				int maxCols = getMaxCols(sheet);
				PdfPTable table = new PdfPTable(maxCols);
				table.setWidthPercentage(100);

				for (Row row : sheet) {
					for (int col = 0; col < maxCols; col++) {
						Cell cell = row.getCell(col, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
						String value = getCellText(cell);
						table.addCell(new Phrase(value, regularFont));
					}
				}

				document.add(table);
				document.newPage();
			}

			document.close();
			workbook.close();

		} catch (Exception e) {
			throw new RuntimeException("Failed to convert XLSX to PDF", e);
		}
	}

	private int getMaxCols(Sheet sheet) {
		int max = 0;
		for (Row row : sheet) {
			if (row.getLastCellNum() > max) {
				max = row.getLastCellNum();
			}
		}
		return max;
	}

	private String getCellText(Cell cell) {
		switch (cell.getCellType()) {
		case STRING: return cell.getStringCellValue();
		case NUMERIC:
			return DateUtil.isCellDateFormatted(cell) ?
					cell.getDateCellValue().toString() :
						String.valueOf(cell.getNumericCellValue());
		case BOOLEAN: return String.valueOf(cell.getBooleanCellValue());
		case FORMULA: return cell.getCellFormula();
		default: return "";
		}
	}
}
