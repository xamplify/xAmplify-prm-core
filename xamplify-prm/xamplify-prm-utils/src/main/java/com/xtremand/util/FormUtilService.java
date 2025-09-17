package com.xtremand.util;

import java.util.Set;

import org.jsoup.nodes.Document;

import com.xtremand.common.bom.FormLink;

public interface FormUtilService {
	public FormLink getFormLinks(FormLink formLink);

	public Set<String> getAllLinks(Document document);
	
	
}
