package com.devhub.support;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.postgresql.PostgreSQLContainer;

@TestConfiguration(proxyBeanMethods = false)
public class CollectionContainerConfig {

    @Bean
    @ServiceConnection
    PostgreSQLContainer collectionPostgresContainer() {
        return new PostgreSQLContainer("postgres:18.4");
    }
}