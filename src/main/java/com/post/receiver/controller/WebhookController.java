package com.post.receiver.controller;

import com.post.receiver.domain.SourceSite;
import com.post.receiver.dto.SyncResult;
import com.post.receiver.dto.webhook.WordPressWebhookPayload;
import com.post.receiver.service.PostReplicationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;

import java.util.Map;

@RestController
@RequestMapping("/api/webhook")
public class WebhookController {

    private static final Logger log = LoggerFactory.getLogger(WebhookController.class);

    private final PostReplicationService postReplicationService;

    public WebhookController(PostReplicationService postReplicationService) {
        this.postReplicationService = postReplicationService;
    }

    @PostMapping("/acordadf")
    public ResponseEntity<SyncResult> receiveAcordaDFPost(@RequestBody WordPressWebhookPayload payload) {
        log.info("Webhook recebido do Acorda DF");
        return ResponseEntity.ok(postReplicationService.sincronizar(payload, SourceSite.ACORDA_DF));
    }

    @PostMapping("/dfmobilidade")
    public ResponseEntity<Map<String, String>> receiveDFMobilidadePost(@RequestBody JsonNode payload) {
        log.info("========================================");
        log.info("WEBHOOK BRUTO DO DF MOBILIDADE (somente log, sem sync)");
        log.info("========================================");
        log.info("Payload DF Mobilidade: {}", payload == null ? "null" : payload.toPrettyString());
        log.info("========================================");
        return ResponseEntity.ok(Map.of(
                "status", "logged",
                "message", "Payload do DF Mobilidade registrado nos logs"
        ));
    }
}
