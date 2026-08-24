package com.post.receiver.dto.wordpress;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record WpMediaResponse(
        Long id,
        @JsonProperty("source_url") String sourceUrl,
        @JsonProperty("mime_type") String mimeType
) {
}
