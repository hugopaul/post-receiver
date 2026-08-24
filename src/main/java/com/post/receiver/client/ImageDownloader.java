package com.post.receiver.client;

import com.post.receiver.dto.client.DownloadedImage;
import com.post.receiver.exception.WordPressSyncException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Component
public class ImageDownloader {

    private static final Logger log = LoggerFactory.getLogger(ImageDownloader.class);

    private static final Map<String, String> EXTENSION_CONTENT_TYPES = Map.of(
            "jpg", "image/jpeg",
            "jpeg", "image/jpeg",
            "png", "image/png",
            "gif", "image/gif",
            "webp", "image/webp",
            "svg", "image/svg+xml",
            "bmp", "image/bmp"
    );

    private final RestClient restClient;

    public ImageDownloader(@Qualifier("imageRestClient") RestClient imageRestClient) {
        this.restClient = imageRestClient;
    }

    public DownloadedImage download(String url) {
        log.info("Baixando imagem destacada: {}", url);
        try {
            ResponseEntity<byte[]> response = restClient.get()
                    .uri(URI.create(url))
                    .retrieve()
                    .toEntity(byte[].class);
            return toDownloadedImage(url, response);
        } catch (WordPressSyncException e) {
            throw e;
        } catch (Exception e) {
            throw new WordPressSyncException("Falha ao baixar imagem destacada: " + url, e);
        }
    }

    private DownloadedImage toDownloadedImage(String url, ResponseEntity<byte[]> response) {
        byte[] body = response.getBody();
        if (body == null || body.length == 0) {
            throw new WordPressSyncException("Imagem destacada veio vazia: " + url);
        }
        String filename = filenameFromUrl(url);
        String contentType = Optional.ofNullable(response.getHeaders().getContentType())
                .map(MediaType::toString)
                .filter(type -> type.startsWith("image/"))
                .map(type -> type.split(";")[0].trim())
                .orElseGet(() -> contentTypeFromFilename(filename));
        log.info("Imagem baixada: {} ({} bytes, {})", filename, body.length, contentType);
        return new DownloadedImage(body, filename, contentType);
    }

    static String filenameFromUrl(String url) {
        try {
            URI uri = URI.create(url);
            String path = uri.getPath();
            if (path == null || path.isBlank() || !path.contains("/")) {
                return "featured.jpg";
            }
            String name = path.substring(path.lastIndexOf('/') + 1);
            if (name.isBlank()) {
                return "featured.jpg";
            }
            return URLDecoder.decode(name, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return "featured.jpg";
        }
    }

    static String contentTypeFromFilename(String filename) {
        String lower = filename.toLowerCase(Locale.ROOT);
        int dot = lower.lastIndexOf('.');
        if (dot < 0) {
            return "image/jpeg";
        }
        return EXTENSION_CONTENT_TYPES.getOrDefault(lower.substring(dot + 1), "image/jpeg");
    }
}
