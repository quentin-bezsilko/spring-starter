package com.qbe.springstarter.testcontainers;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;

public final class Containers {

    private Containers() {}

    public static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17")
            .withDatabaseName("mydb")
            .withUsername("myuser")
            .withPassword("mypassword")
            .withReuse(true);

    public static final GenericContainer<?> REDIS = new GenericContainer<>("redis:8").withExposedPorts(6379);

    static {
        POSTGRES.start();
        REDIS.start();

        System.out.println("Postgres started : " + POSTGRES.getJdbcUrl());
        System.out.println("Redis started    : " + REDIS.getHost() + ":" + REDIS.getFirstMappedPort());
    }
}
