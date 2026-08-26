package com.post.receiver.dto;

public record MediaSyncResult(Long id, String status) {

    public static MediaSyncResult none() {
        return new MediaSyncResult(null, "ausente");
    }

    public static MediaSyncResult reused(Long id) {
        return new MediaSyncResult(id, "reutilizada#" + id);
    }

    public static MediaSyncResult uploaded(Long id) {
        return new MediaSyncResult(id, "enviada#" + id);
    }

    public static MediaSyncResult failed(String reason) {
        return new MediaSyncResult(null, "falhou (" + reason + ")");
    }
}
