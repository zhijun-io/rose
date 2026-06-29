package io.zhijun.devservice.boot.registration;

import java.util.function.Function;

import org.testcontainers.containers.JdbcDatabaseContainer;

import io.zhijun.annotation.Incubating;
import io.zhijun.devservice.core.api.config.JdbcDevServiceProperties;
import io.zhijun.devservice.core.api.provider.DevServiceCategory;

/**
 * Declarative metadata for a JDBC dev service connector.
 */
@Incubating
public final class JdbcDevServiceConnectorDescriptor<
        P extends JdbcDevServiceProperties, C extends JdbcDatabaseContainer<?>> {

    private final Class<P> propertiesType;
    private final String configPrefix;
    private final String serviceName;
    private final String displayName;
    private final Class<C> containerClass;
    private final Function<P, C> containerFactory;

    private JdbcDevServiceConnectorDescriptor(Builder<P, C> builder) {
        this.propertiesType = builder.propertiesType;
        this.configPrefix = builder.configPrefix;
        this.serviceName = builder.serviceName;
        this.displayName = builder.displayName;
        this.containerClass = builder.containerClass;
        this.containerFactory = builder.containerFactory;
    }

    public static <P extends JdbcDevServiceProperties, C extends JdbcDatabaseContainer<?>> Builder<P, C> builder() {
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
        return DevServiceCategory.JDBC;
    }

    Class<C> containerClass() {
        return containerClass;
    }

    Function<P, C> containerFactory() {
        return containerFactory;
    }

    public static final class Builder<P extends JdbcDevServiceProperties, C extends JdbcDatabaseContainer<?>> {

        private Class<P> propertiesType;
        private String configPrefix;
        private String serviceName;
        private String displayName;
        private Class<C> containerClass;
        private Function<P, C> containerFactory;

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

        public Builder<P, C> containerClass(Class<C> containerClass) {
            this.containerClass = containerClass;
            return this;
        }

        public Builder<P, C> containerFactory(Function<P, C> containerFactory) {
            this.containerFactory = containerFactory;
            return this;
        }

        public JdbcDevServiceConnectorDescriptor<P, C> build() {
            if (propertiesType == null
                    || configPrefix == null
                    || serviceName == null
                    || displayName == null
                    || containerClass == null
                    || containerFactory == null) {
                throw new IllegalStateException("JdbcDevServiceConnectorDescriptor is missing required fields");
            }
            return new JdbcDevServiceConnectorDescriptor<P, C>(this);
        }
    }
}
