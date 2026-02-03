package com.gabrielgavrilov.mocha.exceptions;

public class HttpException extends RuntimeException {

    private final int statusCode;
    private String statusText;

    public HttpException(int statusCode, String statusText, String message) {
        super(message);
        this.statusCode = statusCode;
        this.statusText = statusText;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getStatusText() {
        return statusText;
    }

}
