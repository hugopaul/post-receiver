package com.post.receiver.service;

import com.post.receiver.config.WordPressProperties;
import com.post.receiver.domain.SourceSite;
import com.post.receiver.dto.webhook.FeaturedImage;
import com.post.receiver.repository.WordPressMetaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class MetaSyncService {

    private static final Logger log = LoggerFactory.getLogger(MetaSyncService.class);

    private final Optional<WordPressMetaRepository> metaRepository;
    private final WordPressProperties properties;

    public MetaSyncService(ObjectProvider<WordPressMetaRepository> metaRepository, WordPressProperties properties) {
        this.metaRepository = Optional.ofNullable(metaRepository.getIfAvailable());
        this.properties = properties;
    }

    public void sincronizar(long destinationPostId,
                            SourceSite sourceSite,
                            Long sourcePostId,
                            Map<String, List<Object>> meta,
                            FeaturedImage featuredImage,
                            Map<Long, Long> categoryIdMap) {
        if (metaRepository.isEmpty()) {
            log.warn("MySQL do WordPress desabilitado: metas _source_site/_source_post_id não serão gravados. "
                    + "Updates futuros dependerão do slug.");
            return;
        }

        WordPressMetaRepository repository = metaRepository.get();
        repository.upsertMeta(destinationPostId, WordPressMetaRepository.SOURCE_SITE, sourceSite.code());
        repository.upsertMeta(destinationPostId, WordPressMetaRepository.SOURCE_POST_ID, String.valueOf(sourcePostId));

        if (featuredImage != null && featuredImage.url() != null && !featuredImage.url().isBlank()) {
            repository.upsertMeta(
                    destinationPostId,
                    WordPressMetaRepository.SOURCE_FEATURED_IMAGE_URL,
                    featuredImage.url()
            );
        }

        if (meta == null || meta.isEmpty()) {
            return;
        }

        WordPressProperties.Meta metaConfig = properties.getMeta();
        for (Map.Entry<String, List<Object>> entry : meta.entrySet()) {
            String key = entry.getKey();
            if (!shouldCopy(key, metaConfig)) {
                continue;
            }
            String value = firstValue(entry.getValue());
            if (value == null) {
                continue;
            }
            if ("rank_math_primary_category".equals(key)) {
                value = remapPrimaryCategory(value, categoryIdMap);
                if (value == null) {
                    log.warn("rank_math_primary_category {} não pôde ser remapado e será ignorado", entry.getValue());
                    continue;
                }
            }
            repository.upsertMeta(destinationPostId, key, value);
        }
        log.info("Metas copiados para o post destino {}", destinationPostId);
    }

    private boolean shouldCopy(String key, WordPressProperties.Meta metaConfig) {
        if (key == null || key.isBlank()) {
            return false;
        }
        if (metaConfig.getIgnoredKeys().contains(key)) {
            return false;
        }
        for (String prefix : metaConfig.getIgnoredPrefixes()) {
            if (key.startsWith(prefix)) {
                return false;
            }
        }
        return metaConfig.getAllowedKeys().contains(key);
    }

    private String remapPrimaryCategory(String sourceValue, Map<Long, Long> categoryIdMap) {
        try {
            Long sourceId = Long.valueOf(sourceValue);
            Long destinationId = categoryIdMap.get(sourceId);
            return destinationId == null ? null : String.valueOf(destinationId);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String firstValue(List<Object> values) {
        if (values == null || values.isEmpty() || values.getFirst() == null) {
            return null;
        }
        return String.valueOf(values.getFirst());
    }
}
