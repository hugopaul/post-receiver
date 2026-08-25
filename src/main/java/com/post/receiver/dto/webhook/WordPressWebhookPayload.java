package com.post.receiver.dto.webhook;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import tools.jackson.databind.annotation.JsonDeserialize;

import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record WordPressWebhookPayload(
        SourcePost post,
        @JsonDeserialize(using = LenientListDeserializer.class)
        List<SourceTerm> categories,
        @JsonDeserialize(using = LenientListDeserializer.class)
        List<SourceTerm> tags,
        @JsonProperty("category_ids")
        @JsonDeserialize(using = LenientListDeserializer.class)
        List<Long> categoryIds,
        @JsonProperty("tag_ids")
        @JsonDeserialize(using = LenientListDeserializer.class)
        List<Long> tagIds,
        @JsonProperty("featured_image")
        @JsonDeserialize(using = LenientFeaturedImageDeserializer.class)
        FeaturedImage featuredImage,
        Map<String, List<Object>> meta,
        String permalink
) {
}
