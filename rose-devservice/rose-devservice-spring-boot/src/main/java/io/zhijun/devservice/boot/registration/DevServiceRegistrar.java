package io.zhijun.devservice.boot.registration;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.DefaultPropertiesPropertySource;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.Assert;

import io.opentelemetry.api.trace.Tracer;

import io.zhijun.core.annotation.Incubating;

/**
 * Base registrar for dev service container beans.
 */
@Incubating
public abstract class DevServiceRegistrar implements ImportBeanDefinitionRegistrar, BeanFactoryAware, EnvironmentAware {

    public static final String DEV_SERVICES_REGISTRY_BEAN_NAME = "devServicesRegistry";

    private BeanFactory beanFactory;
    private Environment environment;

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        Assert.notNull(beanFactory, "beanFactory has not been initialized");
        Assert.notNull(environment, "environment has not been initialized");

        DevServiceRegistry devServiceRegistry = getOrCreateDevServicesRegistry(registry);
        registerDevServices(devServiceRegistry, environment);
    }

    private DevServiceRegistry getOrCreateDevServicesRegistry(BeanDefinitionRegistry beanDefinitionRegistry) {
        if (beanFactory != null && beanFactory.containsBean(DEV_SERVICES_REGISTRY_BEAN_NAME)) {
            return beanFactory.getBean(DEV_SERVICES_REGISTRY_BEAN_NAME, DevServiceRegistry.class);
        }

        DevServiceRegistry devServiceRegistry = new DevServiceRegistry(beanDefinitionRegistry);

        if (beanDefinitionRegistry instanceof DefaultListableBeanFactory) {
            DefaultListableBeanFactory beanFactoryRegistry = (DefaultListableBeanFactory) beanDefinitionRegistry;
            beanFactoryRegistry.registerSingleton(DEV_SERVICES_REGISTRY_BEAN_NAME, devServiceRegistry);
        }

        return devServiceRegistry;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    protected <T> T bindProperties(String prefix, Class<T> type) {
        Assert.notNull(environment, "environment has not been initialized");
        return Binder.get(environment).bindOrCreate(prefix, type);
    }

    protected void setDefaultProperties(Map<String, Object> defaults) {
        Assert.notNull(environment, "environment has not been initialized");

        if (environment instanceof ConfigurableEnvironment) {
            ConfigurableEnvironment configurableEnvironment = (ConfigurableEnvironment) environment;
            DefaultPropertiesPropertySource.addOrMerge(defaults, configurableEnvironment.getPropertySources());
        }
    }

    protected void setDefaultProperty(String key, Object value) {
        Map<String, Object> defaults = new HashMap<String, Object>();
        defaults.put(key, value);
        setDefaultProperties(defaults);
    }

    public void addDynamicProperty(String name, Supplier<Object> valueSupplier) {
        Assert.notNull(environment, "environment has not been initialized");
        Assert.state(environment instanceof ConfigurableEnvironment,
                "environment must be a ConfigurableEnvironment");
        DevServiceDynamicPropertySource.getOrCreate((ConfigurableEnvironment) environment)
                .add(name, valueSupplier);
    }

    protected BeanFactory getBeanFactory() {
        Assert.notNull(beanFactory, "beanFactory has not been initialized");
        return beanFactory;
    }

    protected void ensureContainerStarted(org.testcontainers.containers.Container<?> container, String serviceName) {
        Tracer tracer = getBeanFactory().getBeanProvider(Tracer.class).getIfAvailable();
        DevServiceContainerTracing.startIfNecessary(container, serviceName, tracer);
    }

    protected abstract void registerDevServices(DevServiceRegistry registry, Environment environment);
}
