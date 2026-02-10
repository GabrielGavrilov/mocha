package com.gabrielgavrilov.mocha.exceptions;

public class InternalServerError extends HttpException {

    public InternalServerError(String message) {
        super(500, "Internal Server Error", message);
    }

}
