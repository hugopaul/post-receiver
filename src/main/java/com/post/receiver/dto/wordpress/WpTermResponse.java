package com.post.receiver.dto.wordpress;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record WpTermResponse(
        Long id,
        String name,
        String slug,
        Long parent
) {
}
