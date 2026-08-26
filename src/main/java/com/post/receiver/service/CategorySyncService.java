package com.post.receiver.service;

import com.post.receiver.client.WordPressApiClient;
import com.post.receiver.dto.CategoryMatchResult;
import com.post.receiver.dto.webhook.SourceTerm;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class CategorySyncService {

    private final WordPressApiClient wordPressApiClient;

    public CategorySyncService(WordPressApiClient wordPressApiClient) {
        this.wordPressApiClient = wordPressApiClient;
    }

    public CategoryMatchResult localizarExistentes(List<SourceTerm> categories) {
        Map<Long, Long> sourceToDestination = new LinkedHashMap<>();
        List<String> found = new ArrayList<>();
        List<String> missing = new ArrayList<>();

        if (categories == null || categories.isEmpty()) {
            return new CategoryMatchResult(sourceToDestination, found, missing);
        }

        for (SourceTerm category : categories) {
            if (category == null) {
                continue;
            }
            String label = describe(category);
            String slug = category.slug();
            if (slug == null || slug.isBlank()) {
                missing.add(label);
                continue;
            }

            wordPressApiClient.findCategoryBySlug(slug)
                    .ifPresentOrElse(existing -> {
                        Long destinationId = existing.id();
                        if (category.termId() != null) {
                            sourceToDestination.put(category.termId(), destinationId);
                        } else {
                            sourceToDestination.put(destinationId, destinationId);
                        }
                        found.add(label);
                    }, () -> missing.add(label));
        }

        return new CategoryMatchResult(sourceToDestination, found, missing);
    }

    private static String describe(SourceTerm category) {
        String name = category.name() == null || category.name().isBlank() ? "(sem nome)" : category.name();
        String slug = category.slug() == null || category.slug().isBlank() ? "(sem slug)" : category.slug();
        return name + " (" + slug + ")";
    }
}
