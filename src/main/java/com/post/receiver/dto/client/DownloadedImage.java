package com.post.receiver.dto.client;

public record DownloadedImage(
        byte[] bytes,
        String filename,
        String contentType
) {
}
