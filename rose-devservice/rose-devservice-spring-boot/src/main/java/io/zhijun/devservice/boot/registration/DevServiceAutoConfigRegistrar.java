package io.zhijun.devservice.boot.registration;

import java.lang.reflect.Field;

import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;

import io.zhijun.devservice.core.api.config.JdbcDevServiceProperties;

/**
 * 共享的 DevService Registrar，通过反射读取各模块的 {@code DESCRIPTOR} 静态字段，
 * 自动执行注册逻辑。消除 10 个模块中重复的内部类模板。
 */
public final class DevServiceAutoConfigRegistrar extends DevServiceRegistrar {

    private static final String DESCRIPTOR_FIELD_NAME = "DESCRIPTOR";

    private Object descriptor;

    @Override
    public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
        try {
            Class<?> configClass = Class.forName(metadata.getClassName());
            Field field = configClass.getDeclaredField(DESCRIPTOR_FIELD_NAME);
            field.setAccessible(true);
            this.descriptor = field.get(null);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(
                    "Cannot read DESCRIPTOR from " + metadata.getClassName(), e);
        }
        super.registerBeanDefinitions(metadata, registry);
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected void registerDevServices(DevServiceRegistry registry, Environment environment) {
        if (descriptor instanceof JdbcDevServiceConnectorDescriptor) {
            JdbcDevServiceConnectorDescriptor d = (JdbcDevServiceConnectorDescriptor) descriptor;
            registry.registerDevServiceProvider(d.serviceName(), d.category());
            JdbcDevServiceProperties properties = (JdbcDevServiceProperties)
                    bindProperties(d.configPrefix(), d.propertiesType());
            registry.registerDevService(
                    d.serviceName(),
                    d.displayName(),
                    d.containerClass(),
                    () -> (org.testcontainers.containers.Container) d.createContainer(properties));
            addDynamicProperty("spring.datasource.url", () -> runningJdbcContainer(d).getJdbcUrl());
            addDynamicProperty("spring.datasource.username", () -> runningJdbcContainer(d).getUsername());
            addDynamicProperty("spring.datasource.password", () -> runningJdbcContainer(d).getPassword());
        } else if (descriptor instanceof DevServiceConnectorDescriptor) {
            DevServiceConnectorDescriptor d = (DevServiceConnectorDescriptor) descriptor;
            registry.registerDevServiceProvider(d.serviceName(), d.category());
            Object properties = bindProperties(d.configPrefix(), d.propertiesType());
            registry.registerDevService(
                    d.serviceName(),
                    d.displayName(),
                    d.containerClass(),
                    () -> (org.testcontainers.containers.Container) d.createContainer(properties));
            ContainerDevServiceRegistrar callbackRegistrar = new ContainerDevServiceRegistrar(d) {};
            callbackRegistrar.setBeanFactory(getBeanFactory());
            callbackRegistrar.setEnvironment(environment);
            d.applyDynamicProperties(callbackRegistrar);
        } else {
            throw new IllegalStateException("Unexpected DESCRIPTOR type: "
                    + (descriptor == null ? "null" : descriptor.getClass()));
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private org.testcontainers.containers.JdbcDatabaseContainer runningJdbcContainer(
            JdbcDevServiceConnectorDescriptor d) {
        org.testcontainers.containers.JdbcDatabaseContainer container =
                (org.testcontainers.containers.JdbcDatabaseContainer)
                        getBeanFactory().getBean(d.containerClass());
        ensureContainerStarted(container, d.serviceName());
        return container;
    }
}
