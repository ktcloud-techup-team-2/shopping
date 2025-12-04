package com.kt.common;

import java.util.Objects;

import com.kt.common.api.CustomException;
import com.kt.common.api.ErrorCode;

public class Preconditions {
    public static void validate (boolean expression, ErrorCode errorCode) {
        if (!expression) throw new CustomException(errorCode);
    }

    public static void nullValidate (Object value, ErrorCode errorCode) {
        if (Objects.isNull(value)) throw new CustomException(errorCode);
    }
}
