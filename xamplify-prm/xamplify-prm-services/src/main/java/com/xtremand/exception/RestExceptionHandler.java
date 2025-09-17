package com.xtremand.exception;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.persistence.EntityNotFoundException;

import org.hibernate.HibernateException;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.social.InsufficientPermissionException;
import org.springframework.social.NotAuthorizedException;
import org.springframework.social.SocialException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.xtremand.campaign.exception.XamplifyDataAccessException;
import com.xtremand.custom.link.bom.CustomLinkType;
import com.xtremand.dao.exception.ObjectNotFoundException;
import com.xtremand.util.BadRequestException;
import com.xtremand.util.CustomValidatonException;
import com.xtremand.util.exception.XAmplifyCustomException;

@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
			HttpHeaders headers, HttpStatus status, WebRequest request) {
		return super.handleMethodArgumentNotValid(ex, headers, status, request);
	}

	@Override
	protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
			HttpHeaders headers, HttpStatus status, WebRequest request) {
		String error = "Malformed JSON request";
		List<?> enumValues = Arrays.asList(CustomLinkType.values());
		if (ex.getMessage().contains("Can not construct instance of com.xtremand.custom.link.bom.CustomLinkType")) {
			return buildResponseEntity(new ApiError(HttpStatus.NOT_ACCEPTABLE,
					"The value of type does not match any of the given options " + enumValues, ex));
		} else {
			return buildResponseEntity(new ApiError(HttpStatus.BAD_REQUEST, error, ex));
		}

	}

	private ResponseEntity<Object> buildResponseEntity(ApiError apiError) {
		return new ResponseEntity<>(apiError, apiError.getStatus());
	}

	@ExceptionHandler(EntityNotFoundException.class)
	protected ResponseEntity<Object> handleEntityNotFound(EntityNotFoundException ex) {
		ApiError apiError = new ApiError(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
		return buildResponseEntity(apiError);
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<Object> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
		String name = ex.getName();
		String type = ex.getRequiredType().getSimpleName();
		Object value = ex.getValue();
		String message = String.format("'%s' should be a valid '%s' and '%s' isn't", name, type, value);

		ApiError apiError = new ApiError(HttpStatus.METHOD_NOT_ALLOWED, message, ex);
		return buildResponseEntity(apiError);
	}

	@ExceptionHandler(NotAuthorizedException.class)
	protected ResponseEntity<Object> handleNotAuthorizedException(NotAuthorizedException ex) {
		return buildResponseEntity(new ApiError(HttpStatus.UNAUTHORIZED, ex));
	}

	@ExceptionHandler(InsufficientPermissionException.class)
	protected ResponseEntity<Object> handleInsufficientPermissionException(InsufficientPermissionException ex) {
		return buildResponseEntity(new ApiError(HttpStatus.FORBIDDEN, ex));
	}

	@ExceptionHandler(ObjectNotFoundException.class)
	protected ResponseEntity<Object> handleObjectNotFoundException(ObjectNotFoundException ex) {
		return buildResponseEntity(new ApiError(HttpStatus.NOT_FOUND, ex));
	}

	@ExceptionHandler(HibernateException.class)
	protected ResponseEntity<Object> handleHibernateException(HibernateException ex) {
		return buildResponseEntity(new ApiError(HttpStatus.INTERNAL_SERVER_ERROR, ex));
	}

	@ExceptionHandler(SocialException.class)
	protected ResponseEntity<Object> handleSocialException(SocialException ex) {
		return buildResponseEntity(new ApiError(HttpStatus.INTERNAL_SERVER_ERROR, ex));
	}

	@ExceptionHandler(XAmplifyCustomException.class)
	protected ResponseEntity<Object> handleXAmplifyCustomException(XAmplifyCustomException ex) {
		return buildResponseEntity(new ApiError(HttpStatus.INTERNAL_SERVER_ERROR, ex));
	}

	@ExceptionHandler(DuplicateEntryException.class)
	protected ResponseEntity<Object> handleDuplicateEntryException(DuplicateEntryException ex) {
		return buildResponseEntity(new ApiError(HttpStatus.CONFLICT, ex));
	}

	@ExceptionHandler(CustomValidatonException.class)
	protected ResponseEntity<Object> handleCustomValidatonException(CustomValidatonException ex) {
		return buildResponseEntity(new ApiError(HttpStatus.FORBIDDEN, ex));
	}

	@ExceptionHandler(BadRequestException.class)
	protected ResponseEntity<Object> handleBadRequestException(BadRequestException ex) {
		return buildResponseEntity(new ApiError(HttpStatus.BAD_REQUEST, ex));
	}

	@Override
	protected ResponseEntity<Object> handleMissingServletRequestParameter(MissingServletRequestParameterException ex,
			HttpHeaders headers, HttpStatus status, WebRequest request) {
		String error = ex.getParameterName() + " parameter is missing";
		return buildResponseEntity(new ApiError(HttpStatus.BAD_REQUEST, error, ex));
	}

	@ExceptionHandler(AccessDeniedException.class)
	public final ResponseEntity<Object> handleAccessDeniedException(AccessDeniedException ex, WebRequest request) {
		String error = ex.getMessage();
		return buildResponseEntity(new ApiError(HttpStatus.FORBIDDEN, error, ex));
	}

	@ExceptionHandler(XamplifyDataAccessException.class)
	public final ResponseEntity<Object> handleAccessDeniedException(XamplifyDataAccessException ex,
			WebRequest request) {
		String error = ex.getMessage();
		return buildResponseEntity(new ApiError(HttpStatus.INTERNAL_SERVER_ERROR, error, ex));
	}

	protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
			HttpStatus status) {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("timestamp", LocalDate.now());
		body.put("status", status.value());

		List<String> errors = ex.getBindingResult().getFieldErrors().stream()
				.map(DefaultMessageSourceResolvable::getDefaultMessage).collect(Collectors.toList());

		body.put("errors", errors);

		return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
	}

}
