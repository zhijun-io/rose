package io.zhijun.devservice.boot.autoconfigure.postgresql;

import io.zhijun.devservice.core.api.config.BaseDevServiceProperties;
import io.zhijun.devservice.core.container.ContainerConfigurer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

/**
 * PostgreSQL container configured for Rose DevService.
 */
final class PostgresqlContainer extends PostgreSQLContainer<PostgresqlContainer> {

    static final String COMPATIBLE_IMAGE_NAME = DockerImageName.parse(PostgresqlDevServiceProperties.DEFAULT_IMAGE_NAME)
            .getUnversionedPart();

    static final String READY_REGEX = ".*database system is ready to accept connections.*\\s";
    static final String SKIPPING_INITIALIZATION_REGEX =
            ".*PostgreSQL Database directory appears to contain a database; Skipping initialization:*\\s";

    private final PostgresqlDevServiceProperties properties;

    PostgresqlContainer(PostgresqlDevServiceProperties properties) {
        super(DockerImageName.parse(properties.getImageName()).asCompatibleSubstituteFor(COMPATIBLE_IMAGE_NAME));
        this.properties = properties;

        this.waitingFor(Wait.forLogMessage("(" + READY_REGEX + "|" + SKIPPING_INITIALIZATION_REGEX + ")", 2));

        ContainerConfigurer.base(this, properties);
        ContainerConfigurer.jdbc(this, properties);
    }

    @Override
    protected void configure() {
        super.configure();

        if (BaseDevServiceProperties.isFixedPort(properties.getPort())) {
            addFixedExposedPort(properties.getPort(), POSTGRESQL_PORT);
        }
    }
}
