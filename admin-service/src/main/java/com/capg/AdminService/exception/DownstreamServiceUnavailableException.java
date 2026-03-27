package com.capg.AdminService.exception;

public class DownstreamServiceUnavailableException extends RuntimeException {

    public DownstreamServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
