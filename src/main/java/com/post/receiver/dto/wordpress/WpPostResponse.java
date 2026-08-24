package com.post.receiver.dto.wordpress;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record WpPostResponse(
        Long id,
        String slug,
        String status,
        String link,
        @JsonProperty("featured_media") Long featuredMedia
) {
}
