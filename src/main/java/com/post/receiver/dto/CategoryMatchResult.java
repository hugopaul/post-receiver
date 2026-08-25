package com.post.receiver.dto;

import java.util.List;
import java.util.Map;

public record CategoryMatchResult(
        Map<Long, Long> sourceToDestinationIds,
        List<String> found,
        List<String> missing
) {
    public boolean hasExistingCategory() {
        return sourceToDestinationIds != null && !sourceToDestinationIds.isEmpty();
    }
}
