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
public class TagSyncService {

    private static final Logger log = LoggerFactory.getLogger(TagSyncService.class);

    private final WordPressApiClient wordPressApiClient;

    public TagSyncService(WordPressApiClient wordPressApiClient) {
        this.wordPressApiClient = wordPressApiClient;
    }

    public Map<Long, Long> sincronizar(List<SourceTerm> tags) {
        Map<Long, Long> sourceToDestination = new LinkedHashMap<>();
        if (tags == null || tags.isEmpty()) {
            return sourceToDestination;
        }
        for (SourceTerm tag : tags) {
            if (tag == null || tag.name() == null || tag.name().isBlank()) {
                continue;
            }
            Long destinationId = ensureTag(tag);
            if (tag.termId() != null) {
                sourceToDestination.put(tag.termId(), destinationId);
            } else {
                sourceToDestination.put(destinationId, destinationId);
            }
        }
        log.info("Tags sincronizadas: {}", sourceToDestination);
        return sourceToDestination;
    }

    private Long ensureTag(SourceTerm tag) {
        String slug = tag.slug();
        if (slug != null && !slug.isBlank()) {
            return wordPressApiClient.findTagBySlug(slug)
                    .map(existing -> {
                        log.info("Tag existente slug={} id={}", slug, existing.id());
                        return existing.id();
                    })
                    .orElseGet(() -> create(tag.name(), slug));
        }
        return create(tag.name(), null);
    }

    private Long create(String name, String slug) {
        WpTermResponse created = wordPressApiClient.createTag(name, slug);
        if (created == null || created.id() == null) {
            throw new WordPressSyncException("WordPress destino não retornou ID ao criar tag " + name);
        }
        log.info("Tag criada name={} slug={} id={}", name, slug, created.id());
        return created.id();
    }
}
