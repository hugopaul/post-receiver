package com.post.receiver.service;

import com.post.receiver.client.ImageDownloader;
import com.post.receiver.client.WordPressApiClient;
import com.post.receiver.dto.client.DownloadedImage;
import com.post.receiver.dto.webhook.FeaturedImage;
import com.post.receiver.dto.wordpress.WpMediaResponse;
import com.post.receiver.exception.WordPressSyncException;
import com.post.receiver.repository.WordPressMetaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class MediaSyncService {

    private static final Logger log = LoggerFactory.getLogger(MediaSyncService.class);

    private final ImageDownloader imageDownloader;
    private final WordPressApiClient wordPressApiClient;
    private final Optional<WordPressMetaRepository> metaRepository;

    public MediaSyncService(ImageDownloader imageDownloader,
                            WordPressApiClient wordPressApiClient,
                            ObjectProvider<WordPressMetaRepository> metaRepository) {
        this.imageDownloader = imageDownloader;
        this.wordPressApiClient = wordPressApiClient;
        this.metaRepository = Optional.ofNullable(metaRepository.getIfAvailable());
    }

    public Long sincronizar(FeaturedImage featuredImage, Long existingPostId) {
        if (featuredImage == null || featuredImage.url() == null || featuredImage.url().isBlank()) {
            log.info("Payload sem imagem destacada");
            return null;
        }

        if (existingPostId != null && sameImageAlreadySynced(existingPostId, featuredImage.url())) {
            Long currentMediaId = currentFeaturedMedia(existingPostId);
            if (currentMediaId != null && currentMediaId > 0) {
                log.info("Reutilizando mídia {} já sincronizada para o post {}", currentMediaId, existingPostId);
                return currentMediaId;
            }
        }

        try {
            DownloadedImage downloaded = imageDownloader.download(featuredImage.url());
            WpMediaResponse uploaded = wordPressApiClient.uploadMedia(
                    downloaded.bytes(),
                    downloaded.filename(),
                    downloaded.contentType()
            );
            if (uploaded == null || uploaded.id() == null) {
                throw new WordPressSyncException("WordPress destino não retornou ID ao criar mídia");
            }
            log.info("Mídia criada no destino: id={} urlOrigem={}", uploaded.id(), featuredImage.url());
            return uploaded.id();
        } catch (Exception e) {
            log.error("Falha ao sincronizar imagem destacada {}. O post seguirá sem atualizar a mídia.", featuredImage.url(), e);
            return null;
        }
    }

    private boolean sameImageAlreadySynced(Long existingPostId, String imageUrl) {
        return metaRepository
                .flatMap(repo -> repo.findMeta(existingPostId, WordPressMetaRepository.SOURCE_FEATURED_IMAGE_URL))
                .filter(imageUrl::equals)
                .isPresent();
    }

    private Long currentFeaturedMedia(Long existingPostId) {
        try {
            var post = wordPressApiClient.getPost(existingPostId);
            return post == null ? null : post.featuredMedia();
        } catch (Exception e) {
            log.warn("Não foi possível ler featured_media do post {}", existingPostId, e);
            return null;
        }
    }
}
