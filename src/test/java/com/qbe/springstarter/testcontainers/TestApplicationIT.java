package com.qbe.springstarter.testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationContext;

class TestApplicationIT extends TestAbstractIntegration {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private CacheManager cacheManager;

    @Test
    void contextLoads() throws Exception {
        assertThat(applicationContext).isNotNull();
        assertThat(dataSource).isNotNull();
        assertThat(cacheManager).isNotNull();
        assertThat(dataSource.getConnection().isValid(5)).isTrue();
    }
}
