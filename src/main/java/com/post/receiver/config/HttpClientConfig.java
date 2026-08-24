package com.post.receiver.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;

@Configuration
@EnableConfigurationProperties(WordPressProperties.class)
public class HttpClientConfig {

    @Bean
    @Qualifier("wordpressRestClient")
    public RestClient wordpressRestClient(WordPressProperties properties) {
        WordPressProperties.Destination destination = properties.getDestination();
        String baseUrl = trimTrailingSlash(destination.getBaseUrl());

        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory(destination))
                .defaultHeader("User-Agent", "PostReceiver/1.0 (WordPress sync)")
                .defaultHeader("Accept", "application/json")
                .build();
    }

    @Bean
    @Qualifier("imageRestClient")
    public RestClient imageRestClient(WordPressProperties properties) {
        WordPressProperties.Destination destination = properties.getDestination();
        return RestClient.builder()
                .requestFactory(requestFactory(destination))
                .defaultHeader("User-Agent", "PostReceiver/1.0 (WordPress sync)")
                .build();
    }

    private static JdkClientHttpRequestFactory requestFactory(WordPressProperties.Destination destination) {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(destination.getConnectTimeoutSeconds()))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(Duration.ofSeconds(destination.getReadTimeoutSeconds()));
        return factory;
    }

    private static String trimTrailingSlash(String url) {
        if (url == null || url.isBlank()) {
            return "";
        }
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }
}
