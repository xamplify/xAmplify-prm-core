package com.xtremand.util.service.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.xtremand.common.bom.FormLink;
import com.xtremand.form.bom.Form;
import com.xtremand.form.dao.FormDao;
import com.xtremand.form.dto.FormDTO;
import com.xtremand.user.bom.UserList;
import com.xtremand.userlist.dao.UserListDAO;
import com.xtremand.util.FormUtilService;
import com.xtremand.util.XamplifyUtil;

@Service
@Transactional
public class FormUtilServiceImpl implements FormUtilService {

	@Autowired
	private FormDao formDao;


	@Autowired
	private UserListDAO userListDao;

	@Value("${web_url}")
	String webUrl;

	@Autowired
	private XamplifyUtil xamplifyUtil;

	@Override
	public FormLink getFormLinks(FormLink formLink) {
		Integer userId = formLink.getUserId();
		List<FormDTO> forms = formDao.listFormIdAndAliasesByUserId(userId);
		forms.addAll(formDao.findDefaultFormIdsAndAliases());
		/**** XNFR-428 ***/
		if (StringUtils.hasText(formLink.getVendorFormAlias())) {
			FormDTO newFormDto = new FormDTO();
			newFormDto.setId(formLink.getVendorFormId());
			newFormDto.setAlias(formLink.getVendorFormAlias());
			forms.add(newFormDto);
		}
		List<String> formAliases = forms.stream().map(FormDTO::getAlias).collect(Collectors.toList());
		formAliases.addAll(formDao.findDefaultFormAliases());
		String htmlBody = formLink.getBody();
		String updatedHtmlBody = htmlBody;
		Document doc = Jsoup.parse(htmlBody);
		updatedHtmlBody = iterateLinks(formLink, forms, formAliases, updatedHtmlBody, doc);
		formLink.setUpdatedHtmlBodyForLandingPage(updatedHtmlBody);
		return formLink;
	}

	private String iterateLinks(FormLink formLink, List<FormDTO> forms, List<String> formAliases,
			String updatedHtmlBody, Document doc) {
		List<String> allAliases = new ArrayList<>();
		Set<String> allTags = getAllLinks(doc);
		for (String ahref : allTags) {
			updatedHtmlBody = generateDynamicFormAliases(formLink, forms, formAliases, updatedHtmlBody, allAliases,
					ahref);
		}
		return updatedHtmlBody;
	}

	public Set<String> getAllLinks(Document doc) {
		Elements links = doc.select("a[href]");
		Elements iframes = doc.select("iframe");
		Set<String> ahrefTags = links.stream().map(element -> element.attr("href")).collect(Collectors.toSet());
		Set<String> iframeSources = iframes.stream().map(iframe -> iframe.attr("src")).collect(Collectors.toSet());
		Set<String> allTags = new HashSet<>();
		allTags.addAll(ahrefTags);
		allTags.addAll(iframeSources);
		allTags.remove(null);
		allTags.remove("");
		return allTags;
	}

	private String generateDynamicFormAliases(FormLink formLink, List<FormDTO> forms, List<String> formAliases,
			String updatedHtmlBody, List<String> allAliases, String ahref) {
		String baseFormUrl = "";
		if (StringUtils.hasText(formLink.getCompanyProfileName())) {
			baseFormUrl = xamplifyUtil.frameVanityURL(webUrl + "f/", formLink.getCompanyProfileName());
		} else {
			baseFormUrl = webUrl + "f/";
		}
		if (ahref.startsWith(baseFormUrl) && formAliases.indexOf(ahref.substring(ahref.lastIndexOf('/') + 1)) > -1) {
			FormDTO formDTO = forms.stream()
					.filter(u -> u.getAlias().equals(ahref.substring(ahref.lastIndexOf('/') + 1)))
					.collect(Collectors.toList()).get(0);
			if (formDTO.getId() != null && StringUtils.hasText(formDTO.getAlias())) {
				String formAliasFromDto = formDTO.getAlias();
				if (allAliases.indexOf(formAliasFromDto) < 0) {
					Form form = new Form();
					form.setAlias(formAliasFromDto);
					form.setId(formDTO.getId());

				}
				allAliases.add(formAliasFromDto);
			}
		}
		return updatedHtmlBody;
	}

	public String getFormContactListName(String listName, Integer companyId) {
		if (!StringUtils.isEmpty(listName)) {
			if (listName.length() >= 256) {
				listName = listName.substring(0, 255);
			}
			UserList userList = userListDao.getUserListByNameAndCompany(listName, companyId);
			if (userList != null) {
				if (listName.length() >= 220) {
					listName = listName.substring(0, 220);
				}
				listName = listName + System.currentTimeMillis();
			}
		}
		return listName;
	}

}
