package io.zhijun.devservice.boot.registration;

import java.util.function.Function;

import org.testcontainers.containers.Container;
import org.testcontainers.lifecycle.Startable;

import io.zhijun.core.annotation.Incubating;

/**
 * Declarative metadata for a non-JDBC dev service connector.
 */
@Incubating
public final class DevServiceConnectorDescriptor<P, C extends Container<?> & Startable> {

    private final Class<P> propertiesType;
    private final String configPrefix;
    private final String serviceName;
    private final String displayName;
    private final String category;
    private final Class<C> containerClass;
    private final Function<P, C> containerFactory;
    private final DynamicPropertyRegistrar<P, C> dynamicProperties;

    @FunctionalInterface
    public interface DynamicPropertyRegistrar<P, C extends Container<?> & Startable> {
        void register(ContainerDevServiceRegistrar<P, C> registrar);
    }

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

    String category() {
        return category;
    }

    Class<C> containerClass() {
        return containerClass;
    }

    Function<P, C> containerFactory() {
        return containerFactory;
    }

    DynamicPropertyRegistrar<P, C> dynamicProperties() {
        return dynamicProperties;
    }

    public static final class Builder<P, C extends Container<?> & Startable> {

        private Class<P> propertiesType;
        private String configPrefix;
        private String serviceName;
        private String displayName;
        private String category;
        private Class<C> containerClass;
        private Function<P, C> containerFactory;
        private DynamicPropertyRegistrar<P, C> dynamicProperties;

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

        public Builder<P, C> category(String category) {
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

        public Builder<P, C> dynamicProperties(DynamicPropertyRegistrar<P, C> dynamicProperties) {
            this.dynamicProperties = dynamicProperties;
            return this;
        }

        public DevServiceConnectorDescriptor<P, C> build() {
            if (propertiesType == null || configPrefix == null || serviceName == null || displayName == null
                    || category == null || containerClass == null || containerFactory == null) {
                throw new IllegalStateException("DevServiceConnectorDescriptor is missing required fields");
            }
            return new DevServiceConnectorDescriptor<P, C>(this);
        }
    }

}
