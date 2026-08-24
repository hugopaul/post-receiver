package com.post.receiver.exception;

public class InvalidWebhookException extends WordPressSyncException {

    public InvalidWebhookException(String message) {
        super(message, 400);
    }
}
