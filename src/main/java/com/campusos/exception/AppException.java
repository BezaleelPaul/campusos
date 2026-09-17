package com.campusos.exception;

public class AppException extends RuntimeException {
    public AppException(String msg) { super(msg); }
    public AppException(String msg, Throwable cause) { super(msg, cause); }
}
