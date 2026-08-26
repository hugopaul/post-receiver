package com.post.receiver.service;

import com.post.receiver.client.ImageDownloader;
import com.post.receiver.client.WordPressApiClient;
import com.post.receiver.dto.MediaSyncResult;
import com.post.receiver.dto.client.DownloadedImage;
import com.post.receiver.dto.webhook.FeaturedImage;
import com.post.receiver.dto.wordpress.WpMediaResponse;
import com.post.receiver.exception.WordPressSyncException;
import com.post.receiver.repository.WordPressMetaRepository;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class MediaSyncService {

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

    public MediaSyncResult sincronizar(FeaturedImage featuredImage, Long existingPostId) {
        if (featuredImage == null || featuredImage.url() == null || featuredImage.url().isBlank()) {
            return MediaSyncResult.none();
        }

        if (existingPostId != null && sameImageAlreadySynced(existingPostId, featuredImage.url())) {
            Long currentMediaId = currentFeaturedMedia(existingPostId);
            if (currentMediaId != null && currentMediaId > 0) {
                return MediaSyncResult.reused(currentMediaId);
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
            return MediaSyncResult.uploaded(uploaded.id());
        } catch (Exception e) {
            String reason = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
            return MediaSyncResult.failed(reason);
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
            return null;
        }
    }
}
