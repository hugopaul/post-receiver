package com.post.receiver.service;

import com.post.receiver.client.WordPressApiClient;
import com.post.receiver.dto.webhook.SourceTerm;
import com.post.receiver.dto.wordpress.WpTermResponse;
import com.post.receiver.exception.WordPressSyncException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

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

    public Map<Long, Long> sincronizar(List<SourceTerm> categories) {
        Map<Long, Long> sourceToDestination = new LinkedHashMap<>();
        if (categories == null || categories.isEmpty()) {
            return sourceToDestination;
        }
        for (SourceTerm category : categories) {
            if (category == null || category.name() == null || category.name().isBlank()) {
                continue;
            }
            Long destinationId = ensureCategory(category);
            if (category.termId() != null) {
                sourceToDestination.put(category.termId(), destinationId);
            } else {
                sourceToDestination.put(destinationId, destinationId);
            }
        }
        log.info("Categorias sincronizadas: {}", sourceToDestination);
        return sourceToDestination;
    }

    private Long ensureCategory(SourceTerm category) {
        String slug = category.slug();
        if (slug != null && !slug.isBlank()) {
            return wordPressApiClient.findCategoryBySlug(slug)
                    .map(existing -> {
                        log.info("Categoria existente slug={} id={}", slug, existing.id());
                        return existing.id();
                    })
                    .orElseGet(() -> create(category.name(), slug));
        }
        return create(category.name(), null);
    }

    private Long create(String name, String slug) {
        WpTermResponse created = wordPressApiClient.createCategory(name, slug);
        if (created == null || created.id() == null) {
            throw new WordPressSyncException("WordPress destino não retornou ID ao criar categoria " + name);
        }
        log.info("Categoria criada name={} slug={} id={}", name, slug, created.id());
        return created.id();
    }
}
