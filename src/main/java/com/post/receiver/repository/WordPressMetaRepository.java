package com.post.receiver.repository;

import com.post.receiver.config.WordPressProperties;
import com.post.receiver.domain.SourceSite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@ConditionalOnProperty(prefix = "wordpress.mysql", name = "enabled", havingValue = "true")
public class WordPressMetaRepository {

    private static final Logger log = LoggerFactory.getLogger(WordPressMetaRepository.class);

    public static final String SOURCE_SITE = "_source_site";
    public static final String SOURCE_POST_ID = "_source_post_id";
    public static final String SOURCE_FEATURED_IMAGE_URL = "_source_featured_image_url";

    private final JdbcTemplate jdbcTemplate;
    private final String prefix;

    public WordPressMetaRepository(JdbcTemplate wordpressJdbcTemplate, WordPressProperties properties) {
        this.jdbcTemplate = wordpressJdbcTemplate;
        this.prefix = sanitizePrefix(properties.getMysql().getTablePrefix());
    }

    public Optional<Long> findPostIdBySource(SourceSite sourceSite, Long sourcePostId) {
        String sql = """
                SELECT m1.post_id
                FROM %1$spostmeta m1
                INNER JOIN %1$spostmeta m2 ON m1.post_id = m2.post_id
                INNER JOIN %1$sposts p ON p.ID = m1.post_id
                WHERE m1.meta_key = ?
                  AND m1.meta_value = ?
                  AND m2.meta_key = ?
                  AND m2.meta_value = ?
                  AND p.post_type = 'post'
                  AND p.post_status <> 'auto-draft'
                LIMIT 1
                """.formatted(prefix);
        try {
            Long postId = jdbcTemplate.queryForObject(
                    sql,
                    Long.class,
                    SOURCE_POST_ID,
                    String.valueOf(sourcePostId),
                    SOURCE_SITE,
                    sourceSite.code()
            );
            return Optional.ofNullable(postId);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Optional<String> findMeta(long postId, String key) {
        String sql = "SELECT meta_value FROM " + prefix + "postmeta WHERE post_id = ? AND meta_key = ? LIMIT 1";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, String.class, postId, key));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public void upsertMeta(long postId, String key, String value) {
        String selectSql = "SELECT meta_id FROM " + prefix + "postmeta WHERE post_id = ? AND meta_key = ? LIMIT 1";
        Long metaId = jdbcTemplate.query(selectSql, rs -> rs.next() ? rs.getLong("meta_id") : null, postId, key);
        if (metaId == null) {
            jdbcTemplate.update(
                    "INSERT INTO " + prefix + "postmeta (post_id, meta_key, meta_value) VALUES (?, ?, ?)",
                    postId,
                    key,
                    value
            );
            log.debug("Meta inserido {}={} no post {}", key, value, postId);
            return;
        }
        jdbcTemplate.update(
                "UPDATE " + prefix + "postmeta SET meta_value = ? WHERE meta_id = ?",
                value,
                metaId
        );
        log.debug("Meta atualizado {}={} no post {}", key, value, postId);
    }

    private static String sanitizePrefix(String prefix) {
        if (prefix == null || prefix.isBlank()) {
            return "wp_";
        }
        if (!prefix.matches("[A-Za-z0-9_]+")) {
            throw new IllegalArgumentException("Prefixo de tabela WordPress inválido: " + prefix);
        }
        return prefix;
    }
}
