package com.post.receiver.dto.wordpress;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record WpPostRequest(
        String title,
        String content,
        String excerpt,
        String status,
        String slug,
        @JsonProperty("comment_status") String commentStatus,
        @JsonProperty("ping_status") String pingStatus,
        String date,
        @JsonProperty("date_gmt") String dateGmt,
        List<Long> categories,
        List<Long> tags,
        @JsonProperty("featured_media") Long featuredMedia
) {
}
