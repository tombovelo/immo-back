package com.immo.error;

import java.time.LocalDateTime;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ApiBaseError {

    private String message;
    private int status;
    private LocalDateTime timestamp = LocalDateTime.now();

    protected ApiBaseError(String message, int status) {
        this.message = message;
        this.status = status;
    }
}