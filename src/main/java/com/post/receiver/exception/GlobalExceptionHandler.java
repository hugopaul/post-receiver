package com.post.receiver.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.ResourceAccessException;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(InvalidWebhookException.class)
    public ResponseEntity<Map<String, String>> handleInvalidWebhook(InvalidWebhookException ex) {
        log.warn("Webhook inválido: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", ex.getMessage()
        ));
    }

    @ExceptionHandler(WordPressSyncException.class)
    public ResponseEntity<Map<String, String>> handleSync(WordPressSyncException ex) {
        log.error("Falha ao sincronizar post: {}", ex.getMessage());
        int status = ex.getStatusCode() >= 400 ? ex.getStatusCode() : 502;
        return ResponseEntity.status(status).body(Map.of(
                "status", "error",
                "message", ex.getMessage()
        ));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> handleUnreadable(HttpMessageNotReadableException ex) {
        log.warn("JSON inválido no webhook: {}", ex.getMostSpecificCause().getMessage());
        return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", "Payload JSON inválido"
        ));
    }

    @ExceptionHandler(ResourceAccessException.class)
    public ResponseEntity<Map<String, String>> handleWordpressUnreachable(ResourceAccessException ex) {
        log.error("WordPress destino inacessível: {}", ex.getMessage());
        return ResponseEntity.status(502).body(Map.of(
                "status", "error",
                "message", "Não foi possível conectar ao WordPress destino"
        ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleUnexpected(Exception ex) {
        log.error("Erro inesperado no post-receiver", ex);
        return ResponseEntity.internalServerError().body(Map.of(
                "status", "error",
                "message", "Erro interno ao processar o webhook"
        ));
    }
}
