package com.post.receiver.service;

import com.post.receiver.client.WordPressApiClient;
import com.post.receiver.dto.webhook.SourcePost;
import com.post.receiver.dto.wordpress.WpPostRequest;
import com.post.receiver.dto.wordpress.WpPostResponse;
import com.post.receiver.exception.WordPressSyncException;
import com.post.receiver.util.WordPressDates;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PostSyncService {

    private final WordPressApiClient wordPressApiClient;

    public PostSyncService(WordPressApiClient wordPressApiClient) {
        this.wordPressApiClient = wordPressApiClient;
    }

    public WpPostResponse sincronizar(SourcePost post,
                                      List<Long> categoryIds,
                                      List<Long> tagIds,
                                      Long featuredMediaId,
                                      Long existingPostId) {
        WpPostRequest request = new WpPostRequest(
                post.title(),
                post.content(),
                nullToEmpty(post.excerpt()),
                post.status(),
                post.slug(),
                post.commentStatus(),
                post.pingStatus(),
                WordPressDates.toIsoLocal(post.date()),
                WordPressDates.toIsoLocal(post.dateGmt()),
                categoryIds,
                tagIds,
                featuredMediaId
        );

        if (existingPostId != null) {
            WpPostResponse updated = wordPressApiClient.updatePost(existingPostId, request);
            requireId(updated, "atualizar");
            return updated;
        }

        WpPostResponse created = wordPressApiClient.createPost(request);
        requireId(created, "criar");
        return created;
    }

    private static void requireId(WpPostResponse response, String action) {
        if (response == null || response.id() == null) {
            throw new WordPressSyncException("WordPress destino não retornou ID ao " + action + " o post");
        }
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
