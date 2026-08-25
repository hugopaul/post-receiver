package com.post.receiver.dto.webhook;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import tools.jackson.databind.annotation.JsonDeserialize;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SourcePost(
        @JsonProperty("ID")
        @JsonDeserialize(using = FlexibleLongDeserializer.class)
        Long id,
        @JsonProperty("post_author")
        @JsonDeserialize(using = FlexibleLongDeserializer.class)
        Long author,
        @JsonProperty("post_date") String date,
        @JsonProperty("post_date_gmt") String dateGmt,
        @JsonProperty("post_content") String content,
        @JsonProperty("post_title") String title,
        @JsonProperty("post_excerpt") String excerpt,
        @JsonProperty("post_status") String status,
        @JsonProperty("comment_status") String commentStatus,
        @JsonProperty("ping_status") String pingStatus,
        @JsonProperty("post_name") String slug,
        @JsonProperty("post_modified") String modified,
        @JsonProperty("post_modified_gmt") String modifiedGmt,
        @JsonProperty("post_type") String type
) {
}
