package com.post.receiver.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration
@ConditionalOnProperty(prefix = "wordpress.mysql", name = "enabled", havingValue = "true")
public class WordPressMySqlConfig {

    @Bean
    public DataSource wordpressDataSource(WordPressProperties properties) {
        WordPressProperties.Mysql mysql = properties.getMysql();
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setPoolName("wordpress-mysql");
        dataSource.setJdbcUrl(mysql.getUrl());
        dataSource.setUsername(mysql.getUsername());
        dataSource.setPassword(mysql.getPassword());
        dataSource.setMaximumPoolSize(5);
        dataSource.setInitializationFailTimeout(-1);
        return dataSource;
    }

    @Bean
    public JdbcTemplate wordpressJdbcTemplate(DataSource wordpressDataSource) {
        return new JdbcTemplate(wordpressDataSource);
    }
}
