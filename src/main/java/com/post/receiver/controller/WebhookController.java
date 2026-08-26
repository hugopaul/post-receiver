package com.post.receiver.controller;

import com.post.receiver.domain.SourceSite;
import com.post.receiver.dto.SyncResult;
import com.post.receiver.dto.webhook.WordPressWebhookPayload;
import com.post.receiver.service.PostReplicationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/webhook")
public class WebhookController {

    private final PostReplicationService postReplicationService;

    public WebhookController(PostReplicationService postReplicationService) {
        this.postReplicationService = postReplicationService;
    }

    @PostMapping("/acordadf")
    public ResponseEntity<SyncResult> receiveAcordaDFPost(@RequestBody WordPressWebhookPayload payload) {
        return ResponseEntity.ok(postReplicationService.sincronizar(payload, SourceSite.ACORDA_DF));
    }

    @PostMapping("/dfmobilidade")
    public ResponseEntity<SyncResult> receiveDFMobilidadePost(@RequestBody WordPressWebhookPayload payload) {
        return ResponseEntity.ok(postReplicationService.sincronizar(payload, SourceSite.DF_MOBILIDADE));
    }
}
