package com.post.receiver.dto.webhook;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import tools.jackson.databind.annotation.JsonDeserialize;

@JsonIgnoreProperties(ignoreUnknown = true)
public record FeaturedImage(
        @JsonDeserialize(using = FlexibleLongDeserializer.class)
        Long id,
        String url
) {
}
