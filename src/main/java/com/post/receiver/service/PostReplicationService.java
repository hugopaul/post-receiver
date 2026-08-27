package com.post.receiver.service;

import com.post.receiver.client.WordPressApiClient;
import com.post.receiver.domain.SourceSite;
import com.post.receiver.dto.CategoryMatchResult;
import com.post.receiver.dto.MediaSyncResult;
import com.post.receiver.dto.SyncResult;
import com.post.receiver.dto.webhook.SourcePost;
import com.post.receiver.dto.webhook.WordPressWebhookPayload;
import com.post.receiver.dto.wordpress.WpPostResponse;
import com.post.receiver.exception.InvalidWebhookException;
import com.post.receiver.repository.WordPressMetaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
public class PostReplicationService {

    private static final Logger log = LoggerFactory.getLogger(PostReplicationService.class);
    private static final Set<String> SKIP_STATUSES = Set.of("auto-draft", "inherit");

    private final CategorySyncService categorySyncService;
    private final TagSyncService tagSyncService;
    private final MediaSyncService mediaSyncService;
    private final PostSyncService postSyncService;
    private final MetaSyncService metaSyncService;
    private final WordPressApiClient wordPressApiClient;
    private final Optional<WordPressMetaRepository> metaRepository;
    private final SourcePostLockRegistry sourcePostLockRegistry;

    public PostReplicationService(CategorySyncService categorySyncService,
                                  TagSyncService tagSyncService,
                                  MediaSyncService mediaSyncService,
                                  PostSyncService postSyncService,
                                  MetaSyncService metaSyncService,
                                  WordPressApiClient wordPressApiClient,
                                  ObjectProvider<WordPressMetaRepository> metaRepository,
                                  SourcePostLockRegistry sourcePostLockRegistry) {
        this.categorySyncService = categorySyncService;
        this.tagSyncService = tagSyncService;
        this.mediaSyncService = mediaSyncService;
        this.postSyncService = postSyncService;
        this.metaSyncService = metaSyncService;
        this.wordPressApiClient = wordPressApiClient;
        this.metaRepository = Optional.ofNullable(metaRepository.getIfAvailable());
        this.sourcePostLockRegistry = sourcePostLockRegistry;
    }

    public SyncResult sincronizar(WordPressWebhookPayload payload, SourceSite sourceSite) {
        validar(payload, sourceSite);
        SourcePost post = payload.post();
        return sourcePostLockRegistry.withLock(sourceSite, post.id(), () -> sincronizarComLock(payload, sourceSite, post));
    }

    private SyncResult sincronizarComLock(WordPressWebhookPayload payload, SourceSite sourceSite, SourcePost post) {
        if (shouldSkip(post)) {
            String reason = "type=" + post.type() + " status=" + post.status();
            log.info(summarize(sourceSite, "SKIP", post, null, null, null, 0, null, reason));
            return SyncResult.skipped(sourceSite, post.id(), post.title(), "Post ignorado: " + reason);
        }

        CategoryMatchResult categoryMatch = categorySyncService.localizarExistentes(payload.categories());
        if (!categoryMatch.hasExistingCategory()) {
            String received = categoryMatch.missing().isEmpty()
                    ? "nenhuma categoria no payload"
                    : String.join(", ", categoryMatch.missing());
            String reason = "categoria não cadastrada no destino [" + received + "]";
            log.warn(summarize(sourceSite, "SKIP", post, null, categoryMatch, null, 0, null, reason));
            return SyncResult.skipped(sourceSite, post.id(), post.title(), reason);
        }

        Map<Long, Long> categoryMap = categoryMatch.sourceToDestinationIds();
        Map<Long, Long> tagMap = tagSyncService.sincronizar(payload.tags());

        Optional<Long> existingPostId = findExistingPost(sourceSite, post);
        if ("trash".equalsIgnoreCase(post.status()) && existingPostId.isEmpty()) {
            String reason = "post em trash sem correspondente no destino";
            log.info(summarize(sourceSite, "SKIP", post, null, categoryMatch, null, tagMap.size(), null, reason));
            return SyncResult.skipped(sourceSite, post.id(), post.title(), reason);
        }

        MediaSyncResult media = mediaSyncService.sincronizar(payload.featuredImage(), existingPostId.orElse(null));

        WpPostResponse destination = postSyncService.sincronizar(
                post,
                new ArrayList<>(categoryMap.values()),
                new ArrayList<>(tagMap.values()),
                media.id(),
                existingPostId.orElse(null)
        );

        metaSyncService.sincronizar(
                destination.id(),
                sourceSite,
                post.id(),
                payload.meta(),
                payload.featuredImage(),
                categoryMap
        );

        boolean created = existingPostId.isEmpty();
        String action = created ? "CREATE" : "UPDATE";
        log.info(summarize(sourceSite, action, post, destination, categoryMatch, media, tagMap.size(), destination.link(), null));

        if (created) {
            return SyncResult.created(sourceSite, post.id(), destination.id(), destination.link(), post.title());
        }
        return SyncResult.updated(sourceSite, post.id(), destination.id(), destination.link(), post.title());
    }

    private Optional<Long> findExistingPost(SourceSite sourceSite, SourcePost post) {
        if (metaRepository.isPresent()) {
            Optional<Long> fromMeta = metaRepository.get().findPostIdBySource(sourceSite, post.id());
            if (fromMeta.isPresent()) {
                return fromMeta;
            }
        }
        return wordPressApiClient.findPostBySlug(post.slug()).map(WpPostResponse::id);
    }

    private void validar(WordPressWebhookPayload payload, SourceSite sourceSite) {
        if (payload == null || payload.post() == null) {
            throw new InvalidWebhookException("Payload do " + sourceSite.displayName() + " sem objeto post");
        }
        SourcePost post = payload.post();
        if (post.id() == null) {
            throw new InvalidWebhookException("post.ID é obrigatório");
        }
        if (post.title() == null || post.title().isBlank()) {
            throw new InvalidWebhookException("post.post_title é obrigatório");
        }
    }

    private boolean shouldSkip(SourcePost post) {
        String type = post.type();
        if (type != null && !type.isBlank() && !"post".equalsIgnoreCase(type)) {
            return true;
        }
        String status = post.status();
        return status != null && SKIP_STATUSES.contains(status.toLowerCase());
    }

    private static String summarize(SourceSite sourceSite,
                                    String action,
                                    SourcePost post,
                                    WpPostResponse destination,
                                    CategoryMatchResult categories,
                                    MediaSyncResult media,
                                    int tagCount,
                                    String destinationUrl,
                                    String skipReason) {
        StringBuilder line = new StringBuilder();
        line.append('[').append(sourceSite.displayName()).append("] ")
                .append(action)
                .append(" origem=").append(post.id());
        if (destination != null && destination.id() != null) {
            line.append(" destino=").append(destination.id());
        }
        line.append(" \"").append(nullToEmpty(post.title())).append('"');
        if (categories != null) {
            if (!categories.found().isEmpty()) {
                line.append(" categorias=").append(join(categories.found()));
            }
            if (!categories.missing().isEmpty()) {
                line.append(" ignoradas=").append(join(categories.missing()));
            }
        }
        if (tagCount > 0) {
            line.append(" tags=").append(tagCount);
        }
        if (media != null) {
            line.append(" mídia=").append(media.status());
        }
        if (destinationUrl != null && !destinationUrl.isBlank()) {
            line.append(" url=").append(destinationUrl);
        }
        if (skipReason != null && !skipReason.isBlank()) {
            line.append(" motivo=").append(skipReason);
        }
        return line.toString();
    }

    private static String join(List<String> values) {
        return String.join(", ", values);
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
