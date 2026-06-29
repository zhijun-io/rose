package io.zhijun.devservice.boot.autoconfigure.mysql;

import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

import io.zhijun.devservice.core.api.config.BaseDevServiceProperties;
import io.zhijun.devservice.core.container.ContainerConfigurer;

/**
 * MySQL container configured for Rose DevService.
 */
final class MySqlContainer extends MySQLContainer<MySqlContainer> {

    static final String COMPATIBLE_IMAGE_NAME =
            DockerImageName.parse(MySqlDevServiceProperties.DEFAULT_IMAGE_NAME).getUnversionedPart();

    private final MySqlDevServiceProperties properties;

    MySqlContainer(MySqlDevServiceProperties properties) {
        super(DockerImageName.parse(properties.getImageName()).asCompatibleSubstituteFor(COMPATIBLE_IMAGE_NAME));
        this.properties = properties;

        ContainerConfigurer.base(this, properties);
        ContainerConfigurer.jdbc(this, properties);
    }

    @Override
    protected void configure() {
        super.configure();

        if (BaseDevServiceProperties.isFixedPort(properties.getPort())) {
            addFixedExposedPort(properties.getPort(), MYSQL_PORT);
        }
    }
}
