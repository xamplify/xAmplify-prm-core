package com.xtremand.dao.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import com.xtremand.common.bom.Criteria;
import com.xtremand.common.bom.Criteria.OPERATION_NAME;
import com.xtremand.common.bom.FindLevel;
import com.xtremand.common.bom.Pagination;
/**
 * FinderDAO is an abstraction for all the Objects which can be searched/find from UI,
 * Should not contain any other methods except which are useful for Find/Search.
 * @author Ramesh
 * @since 8/16/2016
 * @version 1.0
 * @param <T>
 */
public interface FinderDAO<T> {
	/**
	 * This methods finds the T by using Primary Key and loads the associations based on the given Find Levels 
	 * @param pk
	 * @param levels
	 * @return
	 */
	T findByPrimaryKey(Serializable pk, FindLevel[] levels);
	/**
	 * This methods finds the Collection<T> by using Xtremand Criteria and loads the associations based on the given 
	 * Find Levels
	 * @param criteris
	 * @param levels
	 * @return
	 */
	Collection<T> find(List<Criteria> criterias, FindLevel[] levels);
	public Map<String, Object> find(List<Criteria> criterias, FindLevel[] levels, Pagination pagination);
	/**
	 * Default methods to generate Hibernate Criteria from Xtremand Criteria
	 * @param criterias
	 * @return
	 */
	default List<Criterion> generateCriteria(List<com.xtremand.common.bom.Criteria> criterias){

		List<Criterion> list = new ArrayList<>();

		for(com.xtremand.common.bom.Criteria criteria : criterias){

			if(criteria.getOperationName().equals(OPERATION_NAME.eq)){

				list.add(Restrictions.eq(criteria.getProperty(),criteria.getValue1()));

			}else if(criteria.getOperationName().equals(OPERATION_NAME.ne)){
				list.add(Restrictions.ne(criteria.getProperty(),criteria.getValue1()));

			}else if(criteria.getOperationName().equals(OPERATION_NAME.like)){
				list.add(Restrictions.like(criteria.getProperty(),"%"+criteria.getValue1()+"%"));

			}else if(criteria.getOperationName().equals(OPERATION_NAME.ilike)){
				list.add(Restrictions.ilike(criteria.getProperty(),criteria.getValue1()));

			}else if(criteria.getOperationName().equals(OPERATION_NAME.gt)){
				list.add(Restrictions.gt(criteria.getProperty(),criteria.getValue1()));

			}else if(criteria.getOperationName().equals(OPERATION_NAME.ge)){
				list.add(Restrictions.ge(criteria.getProperty(),criteria.getValue1()));

			}else if(criteria.getOperationName().equals(OPERATION_NAME.lt)){
				list.add(Restrictions.lt(criteria.getProperty(),criteria.getValue1()));

			}else if(criteria.getOperationName().equals(OPERATION_NAME.le)){
				list.add(Restrictions.le(criteria.getProperty(),criteria.getValue1()));

			}else if(criteria.getOperationName().equals(OPERATION_NAME.isNull)){
				list.add(Restrictions.isNull(criteria.getProperty()));

			}else if(criteria.getOperationName().equals(OPERATION_NAME.isNotNull)){
				list.add(Restrictions.isNotNull(criteria.getProperty()));

			}else if(criteria.getOperationName().equals(OPERATION_NAME.isEmpty)){
				list.add(Restrictions.isEmpty(criteria.getProperty()));

			}else if(criteria.getOperationName().equals(OPERATION_NAME.isNotEmpty)){
				list.add(Restrictions.isNotEmpty(criteria.getProperty()));

			}else if(criteria.getOperationName().equals(OPERATION_NAME.between)){
				list.add(Restrictions.between(criteria.getProperty(),criteria.getValue1(), criteria.getValue2()));
			}else if(criteria.getOperationName().equals(OPERATION_NAME.in)){
				list.add(Restrictions.in(criteria.getProperty(),criteria.getValue3()));
			}
		}
		return list;
	}

}