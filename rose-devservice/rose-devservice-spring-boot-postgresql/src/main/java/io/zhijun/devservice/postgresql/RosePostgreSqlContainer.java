package io.zhijun.devservice.postgresql;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import io.zhijun.devservice.core.container.ContainerConfigurer;
import io.zhijun.devservice.core.util.ContainerUtils;

/**
 * PostgreSQL container configured for Rose DevService.
 */
final class RosePostgreSqlContainer extends PostgreSQLContainer<RosePostgreSqlContainer> {

    static final String COMPATIBLE_IMAGE_NAME = "postgres";

    static final String READY_REGEX = ".*database system is ready to accept connections.*\\s";
    static final String SKIPPING_INITIALIZATION_REGEX =
            ".*PostgreSQL Database directory appears to contain a database; Skipping initialization:*\\s";

    private final PostgresqlDevServiceProperties properties;

    RosePostgreSqlContainer(PostgresqlDevServiceProperties properties) {
        super(DockerImageName.parse(properties.getImageName()).asCompatibleSubstituteFor(COMPATIBLE_IMAGE_NAME));
        this.properties = properties;

        this.waitingFor(Wait.forLogMessage(
                "(" + READY_REGEX + "|" + SKIPPING_INITIALIZATION_REGEX + ")", 2));

        ContainerConfigurer.base(this, properties);
        ContainerConfigurer.jdbc(this, properties);
    }

    @Override
    protected void configure() {
        super.configure();

        if (ContainerUtils.isValidPort(properties.getPort())) {
            addFixedExposedPort(properties.getPort(), POSTGRESQL_PORT);
        }
    }
}
