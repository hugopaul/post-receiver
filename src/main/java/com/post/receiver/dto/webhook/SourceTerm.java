package com.post.receiver.dto.webhook;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import tools.jackson.databind.annotation.JsonDeserialize;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SourceTerm(
        @JsonProperty("term_id")
        @JsonDeserialize(using = FlexibleLongDeserializer.class)
        Long termId,
        String name,
        String slug,
        @JsonDeserialize(using = FlexibleLongDeserializer.class)
        Long parent
) {
}
