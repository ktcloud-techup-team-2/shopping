package com.kt.common;

import com.kt.common.api.CustomException;
import com.kt.common.api.ErrorCode;

public class Preconditions {
    public static void validate (boolean expression, ErrorCode errorCode) {
        if (!expression) {
            throw new CustomException(errorCode);
        }
    }
}
