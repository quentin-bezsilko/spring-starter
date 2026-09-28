package com.qbe.springstarter.testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

class TestLiquibaseIT extends TestAbstractIntegration {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void shouldCreateLiquibaseTables() {
        Integer count = jdbcTemplate.queryForObject(
                """
                select count(*)
                from information_schema.tables
                where table_name in (
                    'databasechangelog',
                    'databasechangeloglock'
                )
                """,
                Integer.class);

        assertThat(count).isEqualTo(2);
    }

    @Test
    void shouldCreateSampleEntityTable() {
        Integer count = jdbcTemplate.queryForObject(
                """
                select count(*)
                from information_schema.tables
                where table_schema = 'mydb'
                  and table_name = 'sample_entity'
                """,
                Integer.class);

        assertThat(count).isEqualTo(1);
    }

    @Test
    void shouldExecuteLiquibaseChangeSets() {
        Integer count = jdbcTemplate.queryForObject(
                """
                select count(*)
                from databasechangelog
                """,
                Integer.class);

        assertThat(count).isNotNull().isGreaterThan(0);
    }
}
