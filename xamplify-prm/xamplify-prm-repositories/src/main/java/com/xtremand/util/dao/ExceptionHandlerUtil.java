package com.xtremand.util.dao;

import org.hibernate.HibernateException;
import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;

public class ExceptionHandlerUtil {

	private static final Logger logger = LoggerFactory.getLogger(ExceptionHandlerUtil.class);

	private ExceptionHandlerUtil() {
		throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
	}

	public static void handleException(Exception exception) {
		if (exception instanceof ConstraintViolationException) {
			logger.debug("ConstraintViolationException Of {}", exception.getMessage());
		} else if (exception instanceof HibernateException) {
			logger.debug("Hibernate Exception Of {}", exception.getMessage());
		} else if (exception instanceof DataIntegrityViolationException) {
			logger.debug("DataIntegrityViolationException Of {}", exception.getMessage());
		} else {
			logger.debug("Exception Of handleException() {}", exception.getMessage());
		}
	}

}
