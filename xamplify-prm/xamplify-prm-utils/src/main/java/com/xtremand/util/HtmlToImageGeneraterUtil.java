package com.xtremand.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.LoadState;

@Component
public class HtmlToImageGeneraterUtil {
	
	private  static final  Logger logger = LoggerFactory.getLogger(HtmlToImageGeneraterUtil.class);

	    public static InputStream generateImageFromHTML(String htmlBody, String navigationUrl, String chromeBinariPath) {
	        InputStream imageStream = null;
	        logger.debug("generating Image From HTML");
	        try (Playwright playwright = Playwright.create();
	             Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
	                     .setHeadless(true)
	                     .setExecutablePath(Paths.get(chromeBinariPath))
	                     .setArgs(Arrays.asList(
	                             "--disable-features=SameSiteByDefaultCookies",
	                             "--disable-features=CookiesWithoutSameSiteMustBeSecure",
	                             "--disable-web-security",
	                             "--allow-running-insecure-content",
	                             "--disable-site-isolation-trials"
	                     )));
	             BrowserContext context = browser.newContext(new Browser.NewContextOptions().setAcceptDownloads(true));
	             Page page = context.newPage()) {

	            page.setDefaultTimeout(60000);
	            context.grantPermissions(Arrays.asList("geolocation", "clipboard-read", "clipboard-write"));

	            // Navigate to the URL or fallback URL
	            if (StringUtils.hasText(navigationUrl)) {
	                page.navigate(navigationUrl, new Page.NavigateOptions().setTimeout(60000));
	            } 

	            // Set the content and prepare the page
	            page.setContent(htmlBody);
	            page.evaluate("document.querySelectorAll('iframe').forEach(iframe => { iframe.setAttribute('sandbox', 'allow-same-origin allow-scripts'); });");
	            page.waitForLoadState(LoadState.LOAD, new Page.WaitForLoadStateOptions().setTimeout(60000));

	            // Create a temporary file for the screenshot
	            Path tempFile = Files.createTempFile("screenshot", ".png");
	            try {
	                Thread.sleep(10000); // Optional: Wait for dynamic content to load
	                page.screenshot(new Page.ScreenshotOptions().setPath(tempFile).setFullPage(true));

	                // Convert the screenshot to InputStream
	                ByteArrayOutputStream baos = new ByteArrayOutputStream();
	                Files.copy(tempFile, baos);
	                imageStream = new ByteArrayInputStream(baos.toByteArray());
	            } finally {
	                Files.deleteIfExists(tempFile); // Clean up the temporary file
	            }

	        } catch (InterruptedException e) {
                logger.error(e.getMessage(), e);
	            Thread.currentThread().interrupt(); // Restore interrupted status
	        } catch (Exception e) {
                logger.error(e.getMessage(), e);
	            e.printStackTrace(); // Log exceptions
	        }

	        return imageStream;
	    }
}