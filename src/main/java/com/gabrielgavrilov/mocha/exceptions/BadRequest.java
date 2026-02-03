package com.gabrielgavrilov.mocha.exceptions;

public class BadRequest extends HttpException {

    public BadRequest(String message) {
        super(400, "Bad Request", message);
    }
}
