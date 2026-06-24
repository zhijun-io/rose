package io.zhijun.devservice.autoconfigure.mongodb;

import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.utility.DockerImageName;

import io.zhijun.devservice.container.ContainerConfigurer;
import io.zhijun.devservice.util.ContainerUtils;

/**
 * MongoDB container configured for Rose DevService.
 */
final class RoseMongoDbContainer extends MongoDBContainer {

    static final String COMPATIBLE_IMAGE_NAME = "mongo";

    static final int MONGODB_PORT = 27017;

    private final MongoDbDevServiceProperties properties;

    RoseMongoDbContainer(MongoDbDevServiceProperties properties) {
        super(DockerImageName.parse(properties.getImageName()).asCompatibleSubstituteFor(COMPATIBLE_IMAGE_NAME));
        this.properties = properties;

        ContainerConfigurer.base(this, properties);
    }

    @Override
    protected void configure() {
        super.configure();

        if (ContainerUtils.isValidPort(properties.getPort())) {
            addFixedExposedPort(properties.getPort(), MONGODB_PORT);
        }
    }
}
