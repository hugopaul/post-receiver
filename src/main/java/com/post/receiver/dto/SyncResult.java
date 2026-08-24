package com.post.receiver.dto;

import com.post.receiver.domain.SourceSite;

public record SyncResult(
        String status,
        String sourceSite,
        Long sourcePostId,
        Long destinationPostId,
        String destinationUrl,
        String title
) {
    public static SyncResult created(SourceSite sourceSite, Long sourcePostId, Long destinationPostId,
                                     String destinationUrl, String title) {
        return new SyncResult("created", sourceSite.code(), sourcePostId, destinationPostId, destinationUrl, title);
    }

    public static SyncResult updated(SourceSite sourceSite, Long sourcePostId, Long destinationPostId,
                                     String destinationUrl, String title) {
        return new SyncResult("updated", sourceSite.code(), sourcePostId, destinationPostId, destinationUrl, title);
    }

    public static SyncResult skipped(SourceSite sourceSite, Long sourcePostId, String title, String reason) {
        return new SyncResult("skipped", sourceSite.code(), sourcePostId, null, reason, title);
    }
}
