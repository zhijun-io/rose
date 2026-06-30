package io.zhijun.spring.boot.env.config;

import io.zhijun.spring.boot.env.PropertySourceLoaders;
import io.zhijun.spring.context.ConfigurableApplicationContextInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.env.OriginTrackedMapPropertySource;
import org.springframework.boot.origin.Origin;
import org.springframework.boot.origin.OriginTrackedValue;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.support.ResourcePropertySource;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.springframework.boot.origin.OriginTrackedValue.of;

/**
 * Origin Tracked 配置属性初始化器
 */
public class OriginTrackedConfigurationPropertyInitializer extends ConfigurableApplicationContextInitializer
{

    private static final Logger logger = LoggerFactory.getLogger(OriginTrackedConfigurationPropertyInitializer.class);

    private ConfigurableApplicationContext applicationContext;

    @Override
    protected void initialize(ConfigurableApplicationContext context, ConfigurableEnvironment environment) {
        this.applicationContext = context;
    }

    public void onBeanFactoryConfigurationFrozen(ConfigurableListableBeanFactory beanFactory) {
        if (applicationContext == null) {
            return;
        }
        ConfigurableEnvironment environment = applicationContext.getEnvironment();
        MutablePropertySources propertySources = environment.getPropertySources();
        initializePropertySources(propertySources);
    }

    void initializePropertySources(MutablePropertySources propertySources) {
        for (PropertySource propertySource : propertySources) {
            if (isPropertySourceCandidate(propertySource)) {
                String name = propertySource.getName();
                try {
                    PropertySource originTrackedPropertySource = createOriginTrackedPropertySource(propertySource);
                    propertySources.replace(name, originTrackedPropertySource);
                } catch (IOException e) {
                    logger.error("Failed to create origin tracked PropertySource[name: '{}', class: '{}']",
                            name, propertySource.getClass().getName(), e);
                }
            }
        }
    }

    private boolean isPropertySourceCandidate(PropertySource propertySource) {
        return (propertySource instanceof EnumerablePropertySource<?>)
                && !(propertySource instanceof OriginTrackedMapPropertySource);
    }

    PropertySource createOriginTrackedPropertySource(PropertySource propertySource) throws IOException {
        if (propertySource instanceof ResourcePropertySource) {
            return new PropertySourceLoaders(applicationContext.getClassLoader())
                    .reloadAsOriginTracked(propertySource);
        }

        EnumerablePropertySource<?> enumerablePropertySource = (EnumerablePropertySource<?>) propertySource;
        String[] propertyNames = enumerablePropertySource.getPropertyNames();
        Map<String, Object> source = new LinkedHashMap<>(propertyNames.length);
        for (String propertyName : propertyNames) {
            Object propertyValue = enumerablePropertySource.getProperty(propertyName);
            if (propertyValue != null && !(propertyValue instanceof OriginTrackedValue)) {
                Origin origin = new NamedOrigin(propertySource.getName());
                propertyValue = of(propertyValue, origin);
            }
            source.put(propertyName, propertyValue);
        }
        return new OriginTrackedMapPropertySource(propertySource.getName(), source);
    }

    static class NamedOrigin implements Origin {

        private final String name;

        NamedOrigin(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
