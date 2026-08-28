package com.post.receiver.service;

import com.post.receiver.client.WordPressApiClient;
import com.post.receiver.dto.CategoryMatchResult;
import com.post.receiver.dto.webhook.SourceTerm;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

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
        Set<String> foundSlugs = new LinkedHashSet<>();

        if (categories == null || categories.isEmpty()) {
            return new CategoryMatchResult(sourceToDestination, found, missing, List.of(), List.of(), foundSlugs);
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
                        foundSlugs.add(normalizeSlug(slug));
                    }, () -> missing.add(label));
        }

        return new CategoryMatchResult(sourceToDestination, found, missing, List.of(), List.of(), foundSlugs);
    }

    public CategoryMatchResult complementarPorTags(CategoryMatchResult base, List<SourceTerm> tags) {
        if (base == null || tags == null || tags.isEmpty()) {
            return base;
        }

        Set<String> seenSlugs = new HashSet<>();
        if (base.foundSlugs() != null) {
            seenSlugs.addAll(base.foundSlugs());
        }
        List<String> fromTags = new ArrayList<>();
        LinkedHashSet<Long> extraIds = new LinkedHashSet<>();

        for (SourceTerm tag : tags) {
            if (tag == null) {
                continue;
            }
            String slug = tag.slug();
            if (slug == null || slug.isBlank()) {
                continue;
            }
            String normalized = normalizeSlug(slug);
            if (!seenSlugs.add(normalized)) {
                continue;
            }
            wordPressApiClient.findCategoryBySlug(slug).ifPresent(existing -> {
                extraIds.add(existing.id());
                fromTags.add(describe(tag));
            });
        }

        if (fromTags.isEmpty()) {
            return base;
        }

        Set<String> mergedSlugs = new LinkedHashSet<>();
        if (base.foundSlugs() != null) {
            mergedSlugs.addAll(base.foundSlugs());
        }
        mergedSlugs.addAll(seenSlugs);

        return new CategoryMatchResult(
                base.sourceToDestinationIds(),
                base.found(),
                base.missing(),
                List.copyOf(fromTags),
                List.copyOf(extraIds),
                mergedSlugs
        );
    }

    private static String normalizeSlug(String slug) {
        return slug.trim().toLowerCase(Locale.ROOT);
    }

    private static String describe(SourceTerm term) {
        String name = term.name() == null || term.name().isBlank() ? "(sem nome)" : term.name();
        String slug = term.slug() == null || term.slug().isBlank() ? "(sem slug)" : term.slug();
        return name + " (" + slug + ")";
    }
}
