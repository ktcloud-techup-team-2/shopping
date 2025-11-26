package com.kt.common.api;

import static java.util.stream.Collectors.*;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.nio.file.AccessDeniedException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	// 별도 커스텀으로 처리한 예외
	@ExceptionHandler(CustomException.class)
	public ProblemDetail handleBusiness(CustomException ex) {
		log.warn("[BUSINESS] code={} msg={}", ex.getErrorCode(), ex.getMessage());

		return problemOf(
			ex.getErrorCode(),
			ex.getErrorCode().getMessage() // 또는 ex.getMessage()
		);
	}

	// @Valid DTO 검증 실패
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
		var fieldErrors = ex.getBindingResult().getFieldErrors();

		String summary = fieldErrors.stream()
			.map(fe -> "%s: %s (rejected: %s)".formatted(
				fe.getField(),
				fe.getDefaultMessage(),
				fe.getRejectedValue()
			))
			.collect(joining(", "));

		log.warn("[VALIDATION] fieldErrors = {}", summary);

		ErrorCode code = ErrorCode.COMMON_VALIDATION_FAILED;
		ProblemDetail pd = problemOf(code, code.getMessage());

		Map<String, String> errorMap = new HashMap<>();
		for (FieldError fe : fieldErrors) {
			errorMap.put(fe.getField(), fe.getDefaultMessage());
		}
		pd.setProperty("errors", errorMap);

		return pd;
	}

	// @Validated + @RequestParam, @PathVariable 검증 실패
	@ExceptionHandler(ConstraintViolationException.class)
	public ProblemDetail handleConstraint(ConstraintViolationException ex) {
		log.warn("[CONSTRAINT] msg={}", ex.getMessage());

		ErrorCode code = ErrorCode.COMMON_VALIDATION_FAILED;
		ProblemDetail pd = problemOf(
			code,
			code.getMessage()
		);

		Map<String, String> violations = new HashMap<>();
		for (ConstraintViolation<?> v : ex.getConstraintViolations()) {
			violations.put(v.getPropertyPath().toString(), v.getMessage());
		}
		pd.setProperty("errors", violations);
		return pd;
	}

	// JSON 파싱/역직렬화 예외
	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ProblemDetail handleBadBody(HttpMessageNotReadableException ex) {
		log.warn("[BAD_BODY] msg={}", ex.getMessage());

		return problemOf(
			ErrorCode.COMMON_INVALID_ARGUMENT,
			"요청 본문을 읽을 수 없습니다."
		);
	}

	// 권한 예외
	@ExceptionHandler(AccessDeniedException.class)
	public ProblemDetail handleAccessDenied(AccessDeniedException ex) {
		log.warn("[FORBIDDEN] msg={}", ex.getMessage());

		return problemOf(
			ErrorCode.AUTH_FORBIDDEN,
			ErrorCode.AUTH_FORBIDDEN.getMessage()
		);
	}

	// 그 외 예외
	@ExceptionHandler(Exception.class)
	public ProblemDetail handleUnexpected(Exception ex) {
		log.error("[UNEXPECTED] msg={}", ex.getMessage(), ex);

		return problemOf(
			ErrorCode.INTERNAL_ERROR,
			ErrorCode.INTERNAL_ERROR.getMessage()
		);
	}

	private ProblemDetail problemOf(ErrorCode code, String detail) {
		ProblemDetail pd = ProblemDetail.forStatusAndDetail(code.getStatus(), detail);
		pd.setTitle(code.name());
		pd.setType(null);
		return pd;
	}
}