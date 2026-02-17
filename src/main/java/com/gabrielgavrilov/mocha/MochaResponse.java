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

    public void setHeader(String header, String value) {
        int index = this.header.indexOf(header);
        this.header.replace(
                index,
                this.header.indexOf("\r\n", index),
                String.format("%s: %s\r\n", header, value)
        );
    }

    public void setStatus(int statusCode, String statusText) {
        this.header.replace(
                0,
                this.header.indexOf("\r\n"),
                String.format("HTTP/1.0 %d %s\r\n", statusCode, statusText)
        );
    }

    public void send(String data) {
        this.body.append(data);
        appendEmpty();
    }

    private void appendEmpty() {
        this.header.append("\r\n");
    }
}
