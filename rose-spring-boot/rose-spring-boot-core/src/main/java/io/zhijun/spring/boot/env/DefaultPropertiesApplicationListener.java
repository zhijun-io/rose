package io.zhijun.spring.boot.env;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import static org.springframework.core.io.support.SpringFactoriesLoader.loadFactories;

/**
 * 监听 {@link ApplicationEnvironmentPreparedEvent}，通过 SPI 加载 {@link DefaultPropertiesPostProcessor} 并合并默认属性。
 */
public class DefaultPropertiesApplicationListener
        implements ApplicationListener<ApplicationEnvironmentPreparedEvent>, Ordered {

    public static final int DEFAULT_ORDER = LOWEST_PRECEDENCE - 1;

    private static final Logger logger = LoggerFactory.getLogger(DefaultPropertiesApplicationListener.class);

    private int order;

    public DefaultPropertiesApplicationListener() {
        this.order = DEFAULT_ORDER;
    }

    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
        ConfigurableEnvironment environment = event.getEnvironment();
        SpringApplication springApplication = event.getSpringApplication();
        processDefaultProperties(environment, springApplication);
    }

    private void processDefaultProperties(ConfigurableEnvironment environment, SpringApplication springApplication) {
        Map<String, Object> defaultProperties = getOrCreateDefaultProperties(environment);
        postProcessDefaultProperties(springApplication, defaultProperties);
        logDefaultProperties(springApplication, defaultProperties);
    }

    private void postProcessDefaultProperties(SpringApplication springApplication,
                                              Map<String, Object> defaultProperties) {
        ResourceLoader resourceLoader = getResourceLoader(springApplication);
        PropertySourceLoaders propertySourceLoaders = new PropertySourceLoaders(resourceLoader);
        ClassLoader classLoader = resourceLoader.getClassLoader();

        List<DefaultPropertiesPostProcessor> processors =
                loadFactories(DefaultPropertiesPostProcessor.class, classLoader);

        for (DefaultPropertiesPostProcessor processor : processors) {
            postProcessByProcessor(processor, propertySourceLoaders, resourceLoader, defaultProperties);
        }
    }

    private void postProcessByProcessor(DefaultPropertiesPostProcessor processor,
                                        PropertySourceLoaders propertySourceLoaders,
                                        ResourceLoader resourceLoader,
                                        Map<String, Object> defaultProperties) {
        Set<String> resourcePaths = new LinkedHashSet<String>();
        processor.initializeResources(resourcePaths);
        loadFromResources(resourcePaths, propertySourceLoaders, resourceLoader, defaultProperties);
        processor.postProcess(defaultProperties);
    }

    private void loadFromResources(Collection<String> resourcePaths,
                                   PropertySourceLoaders propertySourceLoaders,
                                   ResourceLoader resourceLoader,
                                   Map<String, Object> defaultProperties) {
        if (resourcePaths.isEmpty()) {
            return;
        }
        ResourcePatternResolver resolver = getResourcePatternResolver(resourceLoader);
        for (String path : resourcePaths) {
            try {
                Resource[] resources = resolver.getResources(path);
                for (Resource resource : resources) {
                    loadFromResource(resource, propertySourceLoaders, defaultProperties);
                }
            } catch (IOException e) {
                logger.warn("Default properties resource [{}] not found", path, e);
            }
        }
    }

    private void loadFromResource(Resource resource,
                                  PropertySourceLoaders propertySourceLoaders,
                                  Map<String, Object> defaultProperties) throws IOException {
        URL url = resource.getURL();
        String location = url.getPath();
        List<PropertySource<?>> sources = propertySourceLoaders.load(location, resource);
        for (PropertySource<?> ps : sources) {
            if (!(ps instanceof EnumerablePropertySource)) {
                continue;
            }
            EnumerablePropertySource<?> eps = (EnumerablePropertySource<?>) ps;
            for (String name : eps.getPropertyNames()) {
                Object value = eps.getProperty(name);
                Object old = defaultProperties.putIfAbsent(name, value);
                if (old != null) {
                    logger.warn("Default property '{}' already exists (old: '{}'), new: '{}' skipped",
                            name, old, value);
                }
            }
        }
    }

    private static Map<String, Object> getOrCreateDefaultProperties(ConfigurableEnvironment environment) {
        MutablePropertySources sources = environment.getPropertySources();
        PropertySource<?> ps = sources.get("defaultProperties");
        if (ps instanceof MapPropertySource) {
            return ((MapPropertySource) ps).getSource();
        }
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        sources.addLast(new MapPropertySource("defaultProperties", map));
        return map;
    }

    private static ResourcePatternResolver getResourcePatternResolver(ResourceLoader resourceLoader) {
        if (resourceLoader instanceof ResourcePatternResolver) {
            return (ResourcePatternResolver) resourceLoader;
        }
        return new PathMatchingResourcePatternResolver(resourceLoader);
    }

    private static ResourceLoader getResourceLoader(SpringApplication springApplication) {
        ResourceLoader resourceLoader = springApplication.getResourceLoader();
        return resourceLoader != null ? resourceLoader : new DefaultResourceLoader(springApplication.getClassLoader());
    }

    private void logDefaultProperties(SpringApplication springApplication, Map<String, Object> defaultProperties) {
        if (!logger.isTraceEnabled()) {
            return;
        }
        logger.trace("SpringApplication[sources: {}] defaultProperties:", springApplication.getSources());
        for (Map.Entry<String, Object> entry : defaultProperties.entrySet()) {
            logger.trace("  '{}' = {}", entry.getKey(), entry.getValue());
        }
    }

    @Override
    public int getOrder() {
        return this.order;
    }

    public void setOrder(int order) {
        this.order = order;
    }
}
