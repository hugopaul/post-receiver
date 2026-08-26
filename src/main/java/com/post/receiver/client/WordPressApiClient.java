package com.post.receiver.client;

import com.post.receiver.config.WordPressProperties;
import com.post.receiver.dto.wordpress.WpMediaResponse;
import com.post.receiver.dto.wordpress.WpPostRequest;
import com.post.receiver.dto.wordpress.WpPostResponse;
import com.post.receiver.dto.wordpress.WpTermRequest;
import com.post.receiver.dto.wordpress.WpTermResponse;
import com.post.receiver.exception.WordPressSyncException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@Component
public class WordPressApiClient {

    private final RestClient restClient;
    private final WordPressProperties properties;

    public WordPressApiClient(@Qualifier("wordpressRestClient") RestClient wordpressRestClient,
                              WordPressProperties properties) {
        this.restClient = wordpressRestClient;
        this.properties = properties;
    }

    public Optional<WpTermResponse> findCategoryBySlug(String slug) {
        return firstTerm(getTerms("/categories", slug));
    }

    public WpTermResponse createCategory(String name, String slug) {
        return createTerm("/categories", name, slug);
    }

    public Optional<WpTermResponse> findTagBySlug(String slug) {
        return firstTerm(getTerms("/tags", slug));
    }

    public WpTermResponse createTag(String name, String slug) {
        return createTerm("/tags", name, slug);
    }

    public Optional<WpPostResponse> findPostBySlug(String slug) {
        if (slug == null || slug.isBlank()) {
            return Optional.empty();
        }
        WpPostResponse[] posts = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(restPath() + "/posts")
                        .queryParam("slug", slug)
                        .queryParam("status", "publish,future,draft,pending,private")
                        .build())
                .headers(this::applyAuth)
                .retrieve()
                .onStatus(status -> status.isError(), WordPressApiClient::throwOnError)
                .body(WpPostResponse[].class);
        if (posts == null || posts.length == 0) {
            return Optional.empty();
        }
        return Optional.of(posts[0]);
    }

    public WpPostResponse getPost(long id) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(restPath() + "/posts/{id}")
                        .queryParam("context", "edit")
                        .build(id))
                .headers(this::applyAuth)
                .retrieve()
                .onStatus(status -> status.isError(), WordPressApiClient::throwOnError)
                .body(WpPostResponse.class);
    }

    public WpPostResponse createPost(WpPostRequest request) {
        return restClient.post()
                .uri(restPath() + "/posts")
                .headers(this::applyAuth)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .onStatus(status -> status.isError(), WordPressApiClient::throwOnError)
                .body(WpPostResponse.class);
    }

    public WpPostResponse updatePost(long id, WpPostRequest request) {
        return restClient.post()
                .uri(restPath() + "/posts/{id}", id)
                .headers(this::applyAuth)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .onStatus(status -> status.isError(), WordPressApiClient::throwOnError)
                .body(WpPostResponse.class);
    }

    public WpMediaResponse uploadMedia(byte[] bytes, String filename, String contentType) {
        MediaType mediaType = parseMediaType(contentType);
        return restClient.post()
                .uri(restPath() + "/media")
                .headers(headers -> {
                    applyAuth(headers);
                    headers.set(HttpHeaders.CONTENT_DISPOSITION, contentDisposition(filename));
                })
                .contentType(mediaType)
                .body(bytes)
                .retrieve()
                .onStatus(status -> status.isError(), WordPressApiClient::throwOnError)
                .body(WpMediaResponse.class);
    }

    private List<WpTermResponse> getTerms(String resource, String slug) {
        WpTermResponse[] terms = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(restPath() + resource)
                        .queryParam("slug", slug)
                        .build())
                .headers(this::applyAuth)
                .retrieve()
                .onStatus(status -> status.isError(), WordPressApiClient::throwOnError)
                .body(WpTermResponse[].class);
        if (terms == null) {
            return List.of();
        }
        return Arrays.asList(terms);
    }

    private WpTermResponse createTerm(String resource, String name, String slug) {
        String normalizedSlug = (slug == null || slug.isBlank()) ? null : slug;
        return restClient.post()
                .uri(restPath() + resource)
                .headers(this::applyAuth)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new WpTermRequest(name, normalizedSlug))
                .retrieve()
                .onStatus(status -> status.isError(), WordPressApiClient::throwOnError)
                .body(WpTermResponse.class);
    }

    private Optional<WpTermResponse> firstTerm(List<WpTermResponse> terms) {
        if (terms == null || terms.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(terms.getFirst());
    }

    private void applyAuth(HttpHeaders headers) {
        String username = properties.getDestination().getUsername();
        String password = properties.getDestination().getApplicationPassword();
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            throw new WordPressSyncException(
                    "Credenciais do WordPress destino não configuradas (WP_USERNAME / WP_APP_PASSWORD)",
                    500
            );
        }
        String token = username + ":" + password;
        String encoded = Base64.getEncoder().encodeToString(token.getBytes(StandardCharsets.UTF_8));
        headers.set(HttpHeaders.AUTHORIZATION, "Basic " + encoded);
    }

    private String restPath() {
        String path = properties.getDestination().getRestPath();
        if (path == null || path.isBlank()) {
            return "/wp-json/wp/v2";
        }
        return path.startsWith("/") ? path : "/" + path;
    }

    private static MediaType parseMediaType(String contentType) {
        if (contentType == null || contentType.isBlank()) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
        try {
            return MediaType.parseMediaType(contentType);
        } catch (Exception e) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
    }

    private static String contentDisposition(String filename) {
        String safe = filename.replace("\"", "");
        return "attachment; filename=\"" + safe + "\"";
    }

    private static void throwOnError(HttpRequest request, ClientHttpResponse response) throws IOException {
        String body = new String(response.getBody().readAllBytes(), StandardCharsets.UTF_8);
        String message = "Erro na API WordPress %s %s -> %s: %s".formatted(
                request.getMethod(),
                request.getURI(),
                response.getStatusCode(),
                body
        );
        throw new WordPressSyncException(message, response.getStatusCode().value());
    }
}
