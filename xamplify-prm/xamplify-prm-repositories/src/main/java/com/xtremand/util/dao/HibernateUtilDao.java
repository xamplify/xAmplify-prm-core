package com.xtremand.util.dao;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.Projection;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.springframework.stereotype.Component;

@Component
public class HibernateUtilDao {


	public List<?> getProjectionList(Criteria criteria, List<String> properties) {
		addProjections(criteria, properties);
		return criteria.list();
	}

	public void addProjections(Criteria criteria, List<String> properties) {
		ProjectionList projectionList = Projections.projectionList();
		for (String property : properties) {
			Projection projection = Projections.property(property);
			projectionList.add(projection);
		}
		criteria.setProjection(projectionList);
	}
	
	

}
