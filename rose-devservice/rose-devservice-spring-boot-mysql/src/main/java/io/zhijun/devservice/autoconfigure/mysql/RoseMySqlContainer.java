package io.zhijun.devservice.autoconfigure.mysql;

import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

import io.zhijun.devservice.container.ContainerConfigurer;
import io.zhijun.devservice.util.ContainerUtils;

/**
 * MySQL container configured for Rose DevService.
 */
final class RoseMySqlContainer extends MySQLContainer<RoseMySqlContainer> {

    static final String COMPATIBLE_IMAGE_NAME = "mysql";

    private final MySqlDevServiceProperties properties;

    RoseMySqlContainer(MySqlDevServiceProperties properties) {
        super(DockerImageName.parse(properties.getImageName()).asCompatibleSubstituteFor(COMPATIBLE_IMAGE_NAME));
        this.properties = properties;

        ContainerConfigurer.base(this, properties);
        ContainerConfigurer.jdbc(this, properties);
    }

    @Override
    protected void configure() {
        super.configure();

        if (ContainerUtils.isValidPort(properties.getPort())) {
            addFixedExposedPort(properties.getPort(), MYSQL_PORT);
        }
    }
}
