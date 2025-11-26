package com.kt.common.api;

import org.springframework.http.ProblemDetail;

import java.util.Map;

public final class ProblemResponse {

	private ProblemResponse() {
	}

	public static ProblemDetail of(ErrorCode code) {
		return of(code, code.getMessage());
	}

	public static ProblemDetail of(ErrorCode code, String detail) {
		ProblemDetail pd = ProblemDetail.forStatusAndDetail(code.getStatus(), detail);
		pd.setTitle(code.name());
		pd.setType(null);
		return pd;
	}

	public static ProblemDetail of(ErrorCode code, String detail, Map<String, ?> properties) {
		ProblemDetail pd = of(code, detail);
		if (properties != null) {
			properties.forEach(pd::setProperty);
		}
		return pd;
	}
}
