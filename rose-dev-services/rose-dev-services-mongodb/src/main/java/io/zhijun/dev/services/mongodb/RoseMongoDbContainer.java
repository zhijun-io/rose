package io.zhijun.dev.services.mongodb;

import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.utility.DockerImageName;

import io.zhijun.dev.services.core.container.ContainerConfigurer;
import io.zhijun.dev.services.core.util.ContainerUtils;

/**
 * MongoDB container configured for Rose Dev Services.
 */
final class RoseMongoDbContainer extends MongoDBContainer {

    static final String COMPATIBLE_IMAGE_NAME = "mongo";

    static final int MONGODB_PORT = 27017;

    private final MongoDbDevServicesProperties properties;

    RoseMongoDbContainer(MongoDbDevServicesProperties properties) {
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
