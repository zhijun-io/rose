package io.zhijun.devservice.boot.registration;

import io.zhijun.devservice.core.api.provider.DevServiceCategory;
import org.testcontainers.containers.Container;
import org.testcontainers.lifecycle.Startable;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Declarative metadata for a non-JDBC dev service connector.
 */

public final class DevServiceConnectorDescriptor<P, C extends Container<?> & Startable> {

    private final Class<P> propertiesType;
    private final String configPrefix;
    private final String serviceName;
    private final String displayName;
    private final DevServiceCategory category;
    private final Class<C> containerClass;
    private final Function<P, C> containerFactory;
    private final Consumer<ContainerDevServiceRegistrar<P, C>> dynamicProperties;

    private DevServiceConnectorDescriptor(Builder<P, C> builder) {
        this.propertiesType = builder.propertiesType;
        this.configPrefix = builder.configPrefix;
        this.serviceName = builder.serviceName;
        this.displayName = builder.displayName;
        this.category = builder.category;
        this.containerClass = builder.containerClass;
        this.containerFactory = builder.containerFactory;
        this.dynamicProperties = builder.dynamicProperties;
    }

    public static <P, C extends Container<?> & Startable> Builder<P, C> builder() {
        return new Builder<P, C>();
    }

    Class<P> propertiesType() {
        return propertiesType;
    }

    String configPrefix() {
        return configPrefix;
    }

    String serviceName() {
        return serviceName;
    }

    String displayName() {
        return displayName;
    }

    DevServiceCategory category() {
        return category;
    }

    Class<C> containerClass() {
        return containerClass;
    }



    /**
     * Creates a container instance from the given properties (LoD-friendly).
     */
    C createContainer(P properties) {
        return containerFactory.apply(properties);
    }

    /**
     * Applies dynamic properties using the given registrar (LoD-friendly).
     */
    void applyDynamicProperties(ContainerDevServiceRegistrar<P, C> registrar) {
        if (dynamicProperties != null) {
            dynamicProperties.accept(registrar);
        }
    }

    public static final class Builder<P, C extends Container<?> & Startable> {

        private Class<P> propertiesType;
        private String configPrefix;
        private String serviceName;
        private String displayName;
        private DevServiceCategory category;
        private Class<C> containerClass;
        private Function<P, C> containerFactory;
        private Consumer<ContainerDevServiceRegistrar<P, C>> dynamicProperties;

        public Builder<P, C> propertiesType(Class<P> propertiesType) {
            this.propertiesType = propertiesType;
            return this;
        }

        public Builder<P, C> configPrefix(String configPrefix) {
            this.configPrefix = configPrefix;
            return this;
        }

        public Builder<P, C> serviceName(String serviceName) {
            this.serviceName = serviceName;
            return this;
        }

        public Builder<P, C> displayName(String displayName) {
            this.displayName = displayName;
            return this;
        }

        public Builder<P, C> category(DevServiceCategory category) {
            this.category = category;
            return this;
        }

        public Builder<P, C> containerClass(Class<C> containerClass) {
            this.containerClass = containerClass;
            return this;
        }

        public Builder<P, C> containerFactory(Function<P, C> containerFactory) {
            this.containerFactory = containerFactory;
            return this;
        }

        public Builder<P, C> dynamicProperties(Consumer<ContainerDevServiceRegistrar<P, C>> dynamicProperties) {
            this.dynamicProperties = dynamicProperties;
            return this;
        }

        public DevServiceConnectorDescriptor<P, C> build() {
            if (propertiesType == null
                    || configPrefix == null
                    || serviceName == null
                    || displayName == null
                    || category == null
                    || containerClass == null
                    || containerFactory == null) {
                throw new IllegalStateException("DevServiceConnectorDescriptor is missing required fields");
            }
            return new DevServiceConnectorDescriptor<P, C>(this);
        }
    }
}
