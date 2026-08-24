package com.post.receiver.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "wordpress")
public class WordPressProperties {

    private Destination destination = new Destination();
    private Mysql mysql = new Mysql();
    private Meta meta = new Meta();

    public Destination getDestination() {
        return destination;
    }

    public void setDestination(Destination destination) {
        this.destination = destination;
    }

    public Mysql getMysql() {
        return mysql;
    }

    public void setMysql(Mysql mysql) {
        this.mysql = mysql;
    }

    public Meta getMeta() {
        return meta;
    }

    public void setMeta(Meta meta) {
        this.meta = meta;
    }

    public static class Destination {
        private String baseUrl = "https://dentrodoeixo.com.br";
        private String username = "";
        private String applicationPassword = "";
        private String restPath = "/wp-json/wp/v2";
        private int connectTimeoutSeconds = 15;
        private int readTimeoutSeconds = 120;

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getApplicationPassword() {
            return applicationPassword;
        }

        public void setApplicationPassword(String applicationPassword) {
            this.applicationPassword = applicationPassword;
        }

        public String getRestPath() {
            return restPath;
        }

        public void setRestPath(String restPath) {
            this.restPath = restPath;
        }

        public int getConnectTimeoutSeconds() {
            return connectTimeoutSeconds;
        }

        public void setConnectTimeoutSeconds(int connectTimeoutSeconds) {
            this.connectTimeoutSeconds = connectTimeoutSeconds;
        }

        public int getReadTimeoutSeconds() {
            return readTimeoutSeconds;
        }

        public void setReadTimeoutSeconds(int readTimeoutSeconds) {
            this.readTimeoutSeconds = readTimeoutSeconds;
        }
    }

    public static class Mysql {
        private boolean enabled = false;
        private String url = "jdbc:mysql://127.0.0.1:3306/wordpress?useSSL=false&allowPublicKeyRetrieval=true&characterEncoding=utf8";
        private String username = "root";
        private String password = "";
        private String tablePrefix = "wp_";

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getTablePrefix() {
            return tablePrefix;
        }

        public void setTablePrefix(String tablePrefix) {
            this.tablePrefix = tablePrefix;
        }
    }

    public static class Meta {
        private List<String> allowedKeys = new ArrayList<>(List.of(
                "_wp_page_template",
                "site-sidebar-layout",
                "site-content-layout",
                "site-content-style",
                "site-sidebar-style",
                "theme-transparent-header-meta",
                "astra-migrate-meta-layouts",
                "cta-cta_manchete",
                "rank_math_primary_category",
                "rank_math_seo_score"
        ));
        private List<String> ignoredKeys = new ArrayList<>(List.of(
                "_edit_lock",
                "_edit_last",
                "_thumbnail_id",
                "_wp_old_slug",
                "_encloseme",
                "_pingme",
                "rank_math_internal_links_processed",
                "rank_math_contentai_score",
                "rank_math_analytic_object_id",
                "_elementor_page_assets"
        ));
        private List<String> ignoredPrefixes = new ArrayList<>(List.of(
                "_oembed_",
                "_oembed_time_"
        ));

        public List<String> getAllowedKeys() {
            return allowedKeys;
        }

        public void setAllowedKeys(List<String> allowedKeys) {
            this.allowedKeys = allowedKeys;
        }

        public List<String> getIgnoredKeys() {
            return ignoredKeys;
        }

        public void setIgnoredKeys(List<String> ignoredKeys) {
            this.ignoredKeys = ignoredKeys;
        }

        public List<String> getIgnoredPrefixes() {
            return ignoredPrefixes;
        }

        public void setIgnoredPrefixes(List<String> ignoredPrefixes) {
            this.ignoredPrefixes = ignoredPrefixes;
        }
    }
}
