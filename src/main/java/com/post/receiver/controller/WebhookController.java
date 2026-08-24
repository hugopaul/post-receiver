package com.post.receiver.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/webhook")
public class WebhookController {
    
    private static final Logger log = LoggerFactory.getLogger(WebhookController.class);

    /**
     * Endpoint para receber posts do WordPress do site Acorda DF
     */
    @PostMapping("/acordadf")
    public ResponseEntity<String> receiveAcordaDFPost(@RequestBody Map<String, Object> payload) {
        log.info("========================================");
        log.info("POST RECEBIDO DO SITE: ACORDA DF");
        log.info("========================================");
        logPostDetails(payload);
        log.info("========================================");
        return ResponseEntity.status(HttpStatus.OK).body("Post do Acorda DF recebido com sucesso");
    }

    /**
     * Endpoint para receber posts do WordPress do site DF Mobilidade
     */
    @PostMapping("/dfmobilidade")
    public ResponseEntity<String> receiveDFMobilidadePost(@RequestBody Map<String, Object> payload) {
        log.info("========================================");
        log.info("POST RECEBIDO DO SITE: DF MOBILIDADE");
        log.info("========================================");
        logPostDetails(payload);
        log.info("========================================");
        return ResponseEntity.status(HttpStatus.OK).body("Post do DF Mobilidade recebido com sucesso");
    }

    /**
     * Método auxiliar para logar os detalhes do payload
     */
    private void logPostDetails(Map<String, Object> payload) {
        log.info("Payload completo: {}", payload);
        payload.forEach((key, value) -> {
            if (value instanceof String) {
                log.info("{}: {}", key, truncateContent((String) value, 200));
            } else {
                log.info("{}: {}", key, value);
            }
        });
    }

    /**
     * Trunca o conteúdo para logging
     */
    private String truncateContent(String content, int maxLength) {
        if (content == null) {
            return "";
        }
        if (content.length() > maxLength) {
            return content.substring(0, maxLength) + "...";
        }
        return content;
    }
}

