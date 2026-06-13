package io.zhijun.dev.services.mysql;

import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

import io.zhijun.dev.services.core.container.ContainerConfigurer;
import io.zhijun.dev.services.core.util.ContainerUtils;

/**
 * MySQL container configured for Rose Dev Services.
 */
final class RoseMySqlContainer extends MySQLContainer<RoseMySqlContainer> {

    static final String COMPATIBLE_IMAGE_NAME = "mysql";

    private final MySqlDevServicesProperties properties;

    RoseMySqlContainer(MySqlDevServicesProperties properties) {
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
