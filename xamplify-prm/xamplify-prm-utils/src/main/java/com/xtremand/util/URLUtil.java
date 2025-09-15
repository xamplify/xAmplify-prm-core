package com.xtremand.util;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class URLUtil {
	protected static Logger logger = LoggerFactory.getLogger(URLUtil.class);

	public static boolean isValid(String urlString) {
		try {
			try {
				new URL(urlString).toURI();
			} catch (Exception e) {
				return false;
			}
			final URL url = new URL(urlString);
			HttpURLConnection huc = (HttpURLConnection) url.openConnection();
			huc.setRequestMethod("HEAD");
			int responseCode = huc.getResponseCode();

			return (responseCode != 404);
		}

		catch (Exception e) {
			logger.warn(e.getMessage());
			return false;
		}
	}

	public static Map<String, String> parseMetaTags(String url) {
		try {
			String validUrl = null;
			Map<String, String> map = null;
			String[] urls = StringUtils.split(url);
			for (String url_ : urls) {
				if (isValid(url_)) {
					validUrl = url_;
					break;
				}
			}
			if(validUrl != null) {
				Document doc = Jsoup.connect(validUrl).timeout(10000).validateTLSCertificates(false).ignoreContentType(true)
						.userAgent("Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:25.0) Gecko/20100101 Firefox/25.0").get();
				map = new HashMap<>();
				map.put("pickedUrl", url);
				Elements ogTags = doc.select("meta[property^=og:]");
				if (ogTags.isEmpty()) {
					return map;
				}
				map.put("title", doc.title());
				for (int i = 0; i < ogTags.size(); i++) {
					Element tag = ogTags.get(i);

					String text = tag.attr("property");
					if ("og:image".equals(text)) {
						map.put("image", tag.attr("content"));
					} else if ("og:description".equals(text)) {
						map.put("description", tag.attr("content"));
					} else if ("og:title".equals(text)) {
						map.put("title", tag.attr("content"));
					}
				}
		
			}
			return map;		
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

}
