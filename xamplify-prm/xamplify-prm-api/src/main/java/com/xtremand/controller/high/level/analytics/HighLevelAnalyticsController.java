package com.xtremand.controller.high.level.analytics;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.xtremand.campaign.exception.XamplifyDataAccessException;
import com.xtremand.exception.DuplicateEntryException;
import com.xtremand.formbeans.XtremandResponse;
import com.xtremand.high.level.analytics.service.HighLevelAnalyticsService;
import com.xtremand.mail.service.AsyncComponent;
import com.xtremand.util.dto.DownloadRequestPostDTO;
import com.xtremand.vanity.url.dto.VanityUrlDetailsDTO;

@RestController
@RequestMapping("/highlevel/analytics/")
public class HighLevelAnalyticsController {

	@Autowired
	private HighLevelAnalyticsService highLevelAnalyticsService;

	@Autowired
	private AsyncComponent asyncComponent;

	/********** High Level Analytics ***************/
	@PostMapping(value = "/getActiveAndInActivePartnersForDonut")
	public ResponseEntity<XtremandResponse> getActiveAndInActivePartnersForDonutChart(
			@RequestBody VanityUrlDetailsDTO vanityUrlDetailsDto) {
		return new ResponseEntity<>(
				highLevelAnalyticsService.getActiveAndInActivePartnersForDonutChart(vanityUrlDetailsDto),
				HttpStatus.OK);
	}

	@PostMapping(value = "/detailReports")
	public ResponseEntity<XtremandResponse> getHighLevelAnalyticsDetailReports(
			@RequestBody VanityUrlDetailsDTO vanityUrlDetailsDto) {
		return new ResponseEntity<>(
				highLevelAnalyticsService.getHighLevelAnalyticsDetailReportsForTiles(vanityUrlDetailsDto),
				HttpStatus.OK);
	}

	@PostMapping
	@RequestMapping(value = "/saveDownloadRequest")
	public ResponseEntity<XtremandResponse> saveDownloadRequest(
			@RequestBody DownloadRequestPostDTO downloadRequestPostDTO) {
		boolean exception = false;
		XtremandResponse response = new XtremandResponse();
		try {
			response = highLevelAnalyticsService.saveDownloadRequest(downloadRequestPostDTO);
			return ResponseEntity.ok(response);
		} catch (DuplicateEntryException e) {
			exception = true;
			throw new DuplicateEntryException(e.getMessage());
		} catch (XamplifyDataAccessException u) {
			exception = true;
			throw new XamplifyDataAccessException(u);
		} catch (Exception e) {
			exception = true;
			throw new XamplifyDataAccessException(e);
		} finally {
			if (!exception && response.getStatusCode() == 200) {
				VanityUrlDetailsDTO vanityUrlDetailsDTO = new VanityUrlDetailsDTO();
				vanityUrlDetailsDTO.setUserId(downloadRequestPostDTO.getUserId());
				vanityUrlDetailsDTO.setApplyFilter(downloadRequestPostDTO.isApplyFilter());
				Integer id = (Integer) response.getData();
				asyncComponent.generateHighLevelAnalyticsExcelAndSendEmailNotification(id, vanityUrlDetailsDTO);
			}
		}
	}

}
