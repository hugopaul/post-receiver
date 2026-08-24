package com.post.receiver.dto.wordpress;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record WpTermRequest(
        String name,
        String slug
) {
}
