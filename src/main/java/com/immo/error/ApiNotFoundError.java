package com.immo.error;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ApiNotFoundError extends ApiBaseError {

    public ApiNotFoundError() {
        super();
    }
    public ApiNotFoundError(String message, int status) {
        super(message, status);
    }
}
