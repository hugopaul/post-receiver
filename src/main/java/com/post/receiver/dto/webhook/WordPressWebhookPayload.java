package com.post.receiver.dto.webhook;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record WordPressWebhookPayload(
        SourcePost post,
        List<SourceTerm> categories,
        List<SourceTerm> tags,
        @JsonProperty("category_ids") List<Long> categoryIds,
        @JsonProperty("tag_ids") List<Long> tagIds,
        @JsonProperty("featured_image") FeaturedImage featuredImage,
        Map<String, List<Object>> meta,
        String permalink
) {
}
