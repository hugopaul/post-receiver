package com.post.receiver.dto.webhook;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SourceTerm(
        @JsonProperty("term_id") Long termId,
        String name,
        String slug,
        Long parent
) {
}
