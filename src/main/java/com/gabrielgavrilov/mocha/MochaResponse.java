package com.gabrielgavrilov.mocha;

public class MochaResponse {

    public StringBuilder header = new StringBuilder();
    public StringBuilder body = new StringBuilder();

    public MochaResponse() {
        this.header.append("HTTP/1.0 200 OK\r\n");
        this.addHeader("Content-Type", "application/json");
    }

    public MochaResponse(int statusCode, String statusText) {
        this.header.append(String.format("HTTP/1.0 %d %s", statusCode, statusText));
        this.addHeader("Content-Type", "application/json");
    }

    public void addHeader(String header, String value) {
        this.header.append(header + ": " + value + "\r\n");
    }

    public void setCookie(String name, String value) {
        addHeader("Set-Cookie", name+"="+value);
    }

    public void send(String data) {
        this.body.append(data);
        appendEmpty();
    }

    private void appendEmpty() {
        this.header.append("\r\n");
    }
}
