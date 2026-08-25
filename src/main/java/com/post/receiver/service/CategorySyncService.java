package com.post.receiver.service;

import com.post.receiver.client.WordPressApiClient;
import com.post.receiver.dto.CategoryMatchResult;
import com.post.receiver.dto.webhook.SourceTerm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class CategorySyncService {

    private static final Logger log = LoggerFactory.getLogger(CategorySyncService.class);

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
                log.warn("Categoria sem slug ignorada: {}", label);
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
                        found.add(label + " -> destino id=" + destinationId);
                        log.info("Categoria já cadastrada no destino slug={} id={}", slug, destinationId);
                    }, () -> {
                        missing.add(label);
                        log.warn("Categoria não cadastrada no Dentro do Eixo: {}", label);
                    });
        }

        return new CategoryMatchResult(sourceToDestination, found, missing);
    }

    private static String describe(SourceTerm category) {
        String name = category.name() == null || category.name().isBlank() ? "(sem nome)" : category.name();
        String slug = category.slug() == null || category.slug().isBlank() ? "(sem slug)" : category.slug();
        return name + " (" + slug + ")";
    }
}
