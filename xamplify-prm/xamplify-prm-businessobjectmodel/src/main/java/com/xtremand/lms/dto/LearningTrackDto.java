package com.xtremand.lms.dto;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.xtremand.category.dto.CategoryDTO;
import com.xtremand.formbeans.UserListDTO;
import com.xtremand.lms.bom.LearningTrackType;
import com.xtremand.lms.bom.PartnerActivityType;
import com.xtremand.partner.journey.dto.WorkflowRequestDTO;
import com.xtremand.partnership.bom.PartnershipDTO;
import com.xtremand.tag.dto.TagDTO;

import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class LearningTrackDto {

	private Integer id;
	private String title;
	private String description;
	private String slug;
	private Integer userId;
	private String featuredImage;
	private boolean published = false;
	private Set<Integer> groupIds;
	private Set<Integer> partnershipIds;
	private Set<Integer> userIds;
	private Set<Integer> tagIds;
	private Integer categoryId;
	private Integer contentId;
	private Integer createdByCompanyId;
	private PartnerActivityType status;
	private boolean followAssetSequence = false;

	private boolean canUpdate = false;
	private boolean canDelete = false;
	private boolean canPublish = false;
	private boolean removeFeaturedImage = false;

	private List<PartnershipDTO> companies;
	private List<UserListDTO> groups;
	private List<TagDTO> tags;
	private CategoryDTO category;
	private Integer progress;
	private String createdTime;
	private String publishedOn;
	private String createdByName;
	private String categoryName;
	private Integer score;
	private Integer maxScore;
	boolean enableQuiz = false;

	private LearningTrackType type;

	private List<String> tagNames;
	private Integer partnershipId;

	/* XNFR-223 */
	private List<LearningTrackContentDto> contents;
	private Map<Integer, LearningTrackContentDto> contentAndQuizData;
	private boolean typeQuizId = false;
	private Integer quizId;
	private boolean hasDamContent = true;

	/*** XNFR-327 ****/
	private boolean sendPublishNotification;
	private boolean publishingOrWhiteLabelingInProgress;
	private boolean publishingToPartnerList;
	/*** XNFR-523 ****/
	private boolean trackUpdatedEmailNotification;
	@JsonIgnore
	private boolean publishedTrackUpdated;
	@JsonIgnore
	private List<Integer> previouslySelectedContentIds = new ArrayList<>();
	@JsonIgnore
	private List<Integer> previouslySelectedVisibilityUserIds = new ArrayList<>();
	@JsonIgnore
	private List<Integer> progressedVisibilityUserIds = new ArrayList<>();

	/*** XNFR-523 ****/
	private boolean addedToQuickLinks;
	private boolean groupByAssets;

	/** XNFR-824 **/
	private String approvalStatus;
	private Integer approvalStatusUpdatedBy;
	private Date approvalStatusUpdatedTimeInString;
	private boolean createdByAnyApprovalManagerOrApprover;
	private Integer createdById;
    private String expireDate; //XNFR-897
    /** XNFR-921 **/
	private List<WorkflowRequestDTO> workflowDtos = new ArrayList<>();
    private List<Integer> deletedWorkflowIds = new ArrayList<>();
    
	private String cdnFeaturedImage;

    private String partnerStatus;
}
