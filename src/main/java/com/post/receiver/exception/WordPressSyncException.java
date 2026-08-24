package com.post.receiver.exception;

public class WordPressSyncException extends RuntimeException {

    private final int statusCode;

    public WordPressSyncException(String message) {
        this(message, 502);
    }

    public WordPressSyncException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public WordPressSyncException(String message, Throwable cause) {
        super(message, cause);
        this.statusCode = 502;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
