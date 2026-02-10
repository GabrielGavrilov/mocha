package com.gabrielgavrilov.mocha.exceptions;

public class NotFound extends HttpException {

    public NotFound(String message) {
        super(404, "Not Found", message);
    }

    public NotFound() {
        super(404, "Not Found", null);
    }

}
