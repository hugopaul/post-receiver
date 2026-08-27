package com.post.receiver.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableConfigurationProperties(CorsProperties.class)
public class WebCorsConfig implements WebMvcConfigurer {

    private final CorsProperties corsProperties;

    public WebCorsConfig(CorsProperties corsProperties) {
        this.corsProperties = corsProperties;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        if (corsProperties.getAllowedOrigins() == null || corsProperties.getAllowedOrigins().isEmpty()) {
            return;
        }
        registry.addMapping("/**")
                .allowedOrigins(corsProperties.getAllowedOrigins().toArray(String[]::new))
                .allowedMethods("GET", "POST", "OPTIONS")
                .allowedHeaders("*");
    }
}
