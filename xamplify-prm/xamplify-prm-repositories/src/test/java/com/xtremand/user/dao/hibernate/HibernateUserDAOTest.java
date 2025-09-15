package com.xtremand.user.dao.hibernate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.xtremand.common.bom.Criteria;
import com.xtremand.common.bom.FindLevel;
import com.xtremand.common.bom.Criteria.OPERATION_NAME;
import com.xtremand.user.bom.User;
import com.xtremand.user.dao.UserDAO;
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"file:src/test/resources/spring/applicationContext.xml" })
@Transactional
public class HibernateUserDAOTest {
	Logger logger = LoggerFactory.getLogger(HibernateUserDAOTest.class);
	
	@Autowired
	UserDAO userDAO;
	
	@Test
	public void findByPrimaryKey_existing(){
		logger.debug("findByPrimaryKey_non_existing");
		User user = userDAO.findByPrimaryKey(24, new FindLevel[]{FindLevel.ROLES});
		Assert.assertNotNull(user);
		Assert.assertEquals(Long.valueOf(24), Long.valueOf(user.getUserId()));
		Assert.assertNotNull(user.getRoles());
	}
	
	@Test
	public void findByPrimaryKey_non_existing(){
		logger.debug("findByPrimaryKey_non_existing");
		User user = userDAO.findByPrimaryKey(9999999, new FindLevel[]{FindLevel.ROLES});
		Assert.assertNull(user);
	}
	
	@Test
	public void find(){
		logger.debug("find");
		Criteria criteria = new Criteria("userName", OPERATION_NAME.eq, "santhosh");
		Criteria criteria2 = new Criteria("userName", OPERATION_NAME.eq, "ramesh");
		List<Criteria> criterias = new ArrayList<>();
		criterias.add(criteria);
		criterias.add(criteria2);
		Collection<User> users =  userDAO.find(criterias, new FindLevel[]{FindLevel.ROLES});
		Assert.assertNotNull(users);
	}
}
