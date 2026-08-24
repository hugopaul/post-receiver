package com.post.receiver.dto.webhook;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record FeaturedImage(
        Long id,
        String url
) {
}
