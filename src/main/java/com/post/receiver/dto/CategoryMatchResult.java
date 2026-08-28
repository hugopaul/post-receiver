package com.post.receiver.dto;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public record CategoryMatchResult(
        Map<Long, Long> sourceToDestinationIds,
        List<String> found,
        List<String> missing,
        List<String> fromTags,
        List<Long> extraDestinationIds,
        Set<String> foundSlugs
) {
    public CategoryMatchResult(Map<Long, Long> sourceToDestinationIds, List<String> found, List<String> missing) {
        this(sourceToDestinationIds, found, missing, List.of(), List.of(), Set.of());
    }

    public boolean hasExistingCategory() {
        return sourceToDestinationIds != null && !sourceToDestinationIds.isEmpty();
    }

    public List<Long> destinationIds() {
        LinkedHashSet<Long> ids = new LinkedHashSet<>();
        if (sourceToDestinationIds != null) {
            ids.addAll(sourceToDestinationIds.values());
        }
        if (extraDestinationIds != null) {
            ids.addAll(extraDestinationIds);
        }
        return new ArrayList<>(ids);
    }
}
