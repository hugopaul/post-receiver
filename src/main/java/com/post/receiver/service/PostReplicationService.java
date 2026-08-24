package com.post.receiver.service;

import com.post.receiver.client.WordPressApiClient;
import com.post.receiver.domain.SourceSite;
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

    public PostReplicationService(CategorySyncService categorySyncService,
                                  TagSyncService tagSyncService,
                                  MediaSyncService mediaSyncService,
                                  PostSyncService postSyncService,
                                  MetaSyncService metaSyncService,
                                  WordPressApiClient wordPressApiClient,
                                  ObjectProvider<WordPressMetaRepository> metaRepository) {
        this.categorySyncService = categorySyncService;
        this.tagSyncService = tagSyncService;
        this.mediaSyncService = mediaSyncService;
        this.postSyncService = postSyncService;
        this.metaSyncService = metaSyncService;
        this.wordPressApiClient = wordPressApiClient;
        this.metaRepository = Optional.ofNullable(metaRepository.getIfAvailable());
    }

    public SyncResult sincronizar(WordPressWebhookPayload payload, SourceSite sourceSite) {
        validar(payload, sourceSite);

        SourcePost post = payload.post();
        if (shouldSkip(post)) {
            log.info("Post origem {} ignorado (type={}, status={})", post.id(), post.type(), post.status());
            return SyncResult.skipped(sourceSite, post.id(), post.title(),
                    "Post ignorado: type=" + post.type() + " status=" + post.status());
        }

        log.info("Iniciando sync [{}] origem={} título={}", sourceSite.displayName(), post.id(), post.title());

        Map<Long, Long> categoryMap = categorySyncService.sincronizar(payload.categories());
        Map<Long, Long> tagMap = tagSyncService.sincronizar(payload.tags());

        Optional<Long> existingPostId = findExistingPost(sourceSite, post);
        if ("trash".equalsIgnoreCase(post.status()) && existingPostId.isEmpty()) {
            log.info("Post origem {} está em trash e ainda não existe no destino; nada a criar", post.id());
            return SyncResult.skipped(sourceSite, post.id(), post.title(), "Post em trash sem correspondente no destino");
        }

        Long featuredMediaId = mediaSyncService.sincronizar(payload.featuredImage(), existingPostId.orElse(null));

        WpPostResponse destination = postSyncService.sincronizar(
                post,
                new ArrayList<>(categoryMap.values()),
                new ArrayList<>(tagMap.values()),
                featuredMediaId,
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
        log.info("Sync concluído [{}] origem={} destino={} ação={}",
                sourceSite.displayName(), post.id(), destination.id(), created ? "CREATE" : "UPDATE");

        if (created) {
            return SyncResult.created(sourceSite, post.id(), destination.id(), destination.link(), post.title());
        }
        return SyncResult.updated(sourceSite, post.id(), destination.id(), destination.link(), post.title());
    }

    private Optional<Long> findExistingPost(SourceSite sourceSite, SourcePost post) {
        if (metaRepository.isPresent()) {
            Optional<Long> fromMeta = metaRepository.get().findPostIdBySource(sourceSite, post.id());
            if (fromMeta.isPresent()) {
                log.info("Post destino {} localizado via _source_post_id={}", fromMeta.get(), post.id());
                return fromMeta;
            }
        }

        Optional<Long> fromSlug = wordPressApiClient.findPostBySlug(post.slug()).map(WpPostResponse::id);
        if (fromSlug.isPresent()) {
            log.warn("Post destino {} localizado apenas pelo slug '{}'. "
                            + "Ative wordpress.mysql.enabled para identificar origem com segurança.",
                    fromSlug.get(), post.slug());
        }
        return fromSlug;
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
}
