package com.xtremand.aop;

import java.util.Arrays;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import com.xtremand.campaign.exception.DealConstraintViolationException;
import com.xtremand.campaign.exception.XamplifyDataAccessException;
import com.xtremand.dao.exception.ObjectNotFoundException;
import com.xtremand.exception.DuplicateEntryException;
import com.xtremand.exception.EmailNotificationException;
import com.xtremand.exception.EntityNotFoundDatAccessException;
import com.xtremand.exception.ExternalRestApiServiceException;
import com.xtremand.gdpr.setting.exception.GdprSettingDataAccessException;
import com.xtremand.linkedin.exception.LinkedinDuplicateDataEntryException;
import com.xtremand.mail.exception.MailException;
import com.xtremand.mdf.exception.DuplicateRequestTitleException;
import com.xtremand.partner.bom.PartnerDataAccessException;
import com.xtremand.user.exception.TeamMemberDataAccessException;
import com.xtremand.user.exception.UserDataAccessException;
import com.xtremand.userlist.exception.UserListException;
import com.xtremand.util.BadRequestException;
import com.xtremand.util.CustomValidatonException;
import com.xtremand.util.exception.XAmplifyCustomException;
import com.xtremand.video.exception.VideoDataAccessException;

@Component
@Aspect
public class LoggingAspectJ {

	private static final Logger logger = LoggerFactory.getLogger(LoggingAspectJ.class);

	private static final String MESSAGE = "*****DO NOT THROW ERROR MESSAGE*******";

	/**
	 * Declaring around advice
	 * 
	 * @param joinPoint
	 * @throws Throwable
	 */
	@Around("logForAllMethods()")
	public Object aroundAdviceForAllMethods(ProceedingJoinPoint joinPoint) throws Throwable {
		String methodName = joinPoint.getTarget().getClass().getName() + "-" + joinPoint.getSignature().getName() + "("
				+ Arrays.toString(joinPoint.getArgs()) + ")";
		Object result = null;
		try {
			result = joinPoint.proceed();
		} catch (XamplifyDataAccessException | VideoDataAccessException | UserDataAccessException
				| PartnerDataAccessException | UserListException | TeamMemberDataAccessException e) {
			logger.error("Error In method " + methodName, e);
			throw new XamplifyDataAccessException(e.getMessage());
		} catch (CustomValidatonException | DealConstraintViolationException ex) {
			throw ex;
		} catch (UsernameNotFoundException ex) {
			throw new CustomValidatonException(ex.getMessage());
		} catch (BadRequestException bre) {
			throw new BadRequestException(bre.getMessage());
		} catch (EntityNotFoundDatAccessException entityNotFoundException) {
			throw new EntityNotFoundDatAccessException(entityNotFoundException.getMessage());
		} catch (ExternalRestApiServiceException exception) {
			throw new ExternalRestApiServiceException(exception.getMessage());
		} catch (ObjectNotFoundException objectNotFoundException) {
			throw new ObjectNotFoundException(objectNotFoundException.getMessage());
		} catch (GdprSettingDataAccessException gdprException) {
			logger.error("******GDPR Custom Exception*******" + methodName, gdprException);
			throw new XAmplifyCustomException(gdprException.getMessage());
		} catch (DuplicateEntryException duException) {
			throw new DuplicateEntryException(duException.getMessage());
		} catch (LinkedinDuplicateDataEntryException duException) {
			throw new LinkedinDuplicateDataEntryException(duException.getMessage());
		} catch (DataIntegrityViolationException die) {
			if (die.getMessage().contains("click_id_fk") || die.getMessage().contains("reply_id_fk")
					|| die.getMessage().contains("xt_event_leads_mail_history_reply_fk_Id")) {
				logger.debug(MESSAGE);
			}
		} catch (MailException | EmailNotificationException ex) {
			logger.debug(MESSAGE);
		} catch (DuplicateRequestTitleException drex) {
			throw new DuplicateRequestTitleException(drex.getMessage());
		} catch (AccessDeniedException accessDeniedException) {
			throw new AccessDeniedException(accessDeniedException.getMessage());
		} catch (Exception ex) {
			if (methodName.contains("launchSocialCampaigns") && ex.getMessage().contains(
					"org.springframework.transaction.UnexpectedRollbackException: Transaction rolled back because it has been marked as rollback-only")) {
				// Do Nothing
			} else {
				logger.error("Error In method " + methodName, ex);
				throw new XamplifyDataAccessException(ex.getMessage());
			}

		}
		logger.debug("*****************************************************");
		return result;
	}

	/**
	 * Declaring named pointcut
	 */
	@Pointcut("execution(* com.xtremand..*.*(..))")
	protected void logForAllMethods() {
	}
}
