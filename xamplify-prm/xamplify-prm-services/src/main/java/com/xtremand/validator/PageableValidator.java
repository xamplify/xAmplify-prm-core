package com.xtremand.validator;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.validation.Validator;

import com.xtremand.common.bom.Pagination.SORTINGORDER;
import com.xtremand.util.XamplifyUtils;
import com.xtremand.util.dto.Pageable;
import com.xtremand.util.dto.XamplifyUtility;

@Component
public class PageableValidator implements Validator {

	@Value("${pageable.page.parameter}")
	private String pageParameter;

	@Value("${pageable.page.missing}")
	private String pageParameterMissing;

	@Value("${pageable.page.invalid}")
	private String invalidPageNumber;

	@Value("${pageable.size.parameter}")
	private String sizeParameter;

	@Value("${pageable.size.missing}")
	private String sizeParameterMissing;

	@Value("${pageable.size.invalid}")
	private String invalidSize;

	@Value("${pageable.size.max}")
	private String maxLimitReached;

	@Value("${pageable.sort.parameter}")
	private String sortParameter;

	@Value("${pageable.sort.missing}")
	private String sortParameterMissing;

	@Value("${pageable.sort.invalid}")
	private String invalidSort;

	@Override
	public boolean supports(Class<?> clazz) {
		return Pageable.class.isAssignableFrom(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		Pageable pageable = (Pageable) target;

		validatePage(errors, pageable);

		validateSize(errors, pageable);

		validateSort(errors, pageable);

	}

	private void validateSort(Errors errors, Pageable pageable) {
		String sort = pageable.getSort();
		boolean isEmptyString = "null".equals(sort);
		if (sort != null) {
			if (sort.equals(",")) {
				XamplifyUtility.setRejectedValue(errors, sortParameter, invalidSort);
			} else {
				List<String> sortColumnAndOrder = XamplifyUtils.convertStringToArrayList(sort);
				if (sortColumnAndOrder.size() < 2) {
					XamplifyUtility.setRejectedValue(errors, sortParameter, invalidSort+"");
				}
			}
		} else if (isEmptyString) {
			XamplifyUtility.setRejectedValue(errors, sortParameter, invalidSort);
		}
	}

	private void validateSize(Errors errors, Pageable pageable) {
		String sizeString = pageable.getSize();
		if (sizeString == null) {
			XamplifyUtility.setRejectedValue(errors, sizeParameter, sizeParameterMissing);
		} else {
			Integer limit = pageable.getLimit();
			if (limit != null && limit <= 0) {
				XamplifyUtility.setRejectedValue(errors, sizeParameter, invalidSize);
			} else if (limit != null && limit > 48) {
				XamplifyUtility.setRejectedValue(errors, sizeParameter, maxLimitReached);
			}
		}
	}

	private void validatePage(Errors errors, Pageable pageable) {
		String pageString = pageable.getPage();
		if (pageString == null) {
			XamplifyUtility.setRejectedValue(errors, pageParameter, pageParameterMissing);
		} else {
			Integer pageNumber = pageable.getPageNumber();
			if (pageNumber != null && pageNumber <= 0) {
				XamplifyUtility.setRejectedValue(errors, pageParameter, invalidPageNumber);
			}
		}
	}

	public void validatePagableParameters(Object target, Errors errors, String sortParameter, String sortColumnsString,
			String invalidSortColumn, String invalidSortMessage) {
		validate(target, errors);
		FieldError sortFieldError = errors.getFieldError(sortParameter);
		if (sortFieldError == null) {
			String sort = ((Pageable) target).getSort();
			if (sort != null) {
				List<String> sortColumnAndOrder = XamplifyUtils.convertStringToArrayList(sort);
				String sortColumn = sortColumnAndOrder.get(0);
				String sortOrder = sortColumnAndOrder.get(1);
				boolean ascendingOrder = SORTINGORDER.ASC.name().equalsIgnoreCase(sortOrder);
				boolean descendingOrder = SORTINGORDER.DESC.name().equalsIgnoreCase(sortOrder);
				if (ascendingOrder || descendingOrder) {
					List<String> sortColumns = XamplifyUtils.convertStringToArrayList(sortColumnsString);
					if (sortColumns.indexOf(sortColumn) < 0) {
						XamplifyUtility.setRejectedValue(errors, sortParameter, invalidSortColumn);
					}
				} else {
					XamplifyUtility.setRejectedValue(errors, sortParameter, invalidSortMessage);
				}
			}

		}
	}

	public void validatePagableParameters(Object target, Errors errors, String sortColumnsString) {
		validate(target, errors);
		FieldError sortFieldError = errors.getFieldError(sortParameter);
		if (sortFieldError == null) {
			String sort = ((Pageable) target).getSort();
			if (sort != null) {
				List<String> sortColumnAndOrder = XamplifyUtils.convertStringToArrayList(sort);
				String sortColumn = sortColumnAndOrder.get(0);
				String sortOrder = sortColumnAndOrder.get(1);
				boolean ascendingOrder = SORTINGORDER.ASC.name().equalsIgnoreCase(sortOrder);
				boolean descendingOrder = SORTINGORDER.DESC.name().equalsIgnoreCase(sortOrder);
				if (ascendingOrder || descendingOrder) {
					List<String> sortColumns = XamplifyUtils.convertStringToArrayList(sortColumnsString);
					if (sortColumns.indexOf(sortColumn) < 0) {
						String invalidSortColumn = "Invalid sort column.Available columns are " + sortColumnsString;
						XamplifyUtility.setRejectedValue(errors, sortParameter, invalidSortColumn);
					}
				} else {
					XamplifyUtility.setRejectedValue(errors, sortParameter, invalidSort);
				}
			}

		}
	}

}
