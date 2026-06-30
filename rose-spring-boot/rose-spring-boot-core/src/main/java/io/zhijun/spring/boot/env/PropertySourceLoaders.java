package io.zhijun.spring.boot.env;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.env.PropertySourceLoader;
import org.springframework.boot.origin.OriginLookup;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.StringUtils;

import static org.springframework.core.io.support.SpringFactoriesLoader.loadFactories;

/**
 * 复合 {@link PropertySourceLoader}，聚合 Spring Boot 所有 PropertySourceLoader 实现。
 *
 * <p>支持加载多格式资源文件（properties、xml、yml、yaml），
 * 并提供 OriginLookup 追踪能力。
 */
public class PropertySourceLoaders implements PropertySourceLoader {

    private static final Logger logger = LoggerFactory.getLogger(PropertySourceLoaders.class);

    private final ResourceLoader resourceLoader;

    private final List<PropertySourceLoader> loaders;

    public PropertySourceLoaders() {
        this(Thread.currentThread().getContextClassLoader());
    }

    public PropertySourceLoaders(ClassLoader classLoader) {
        this(new DefaultResourceLoader(classLoader));
    }

    public PropertySourceLoaders(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
        this.loaders = loadFactories(PropertySourceLoader.class, resourceLoader.getClassLoader());
    }

    @Override
    public String[] getFileExtensions() {
        return loaders.stream()
                .map(PropertySourceLoader::getFileExtensions)
                .map(Arrays::asList)
                .flatMap(List::stream)
                .toArray(String[]::new);
    }

    @Override
    public List<PropertySource<?>> load(String name, Resource resource) throws IOException {
        List<PropertySource<?>> propertySources = new LinkedList<>();
        URL url = resource.getURL();
        for (PropertySourceLoader loader : loaders) {
            if (supports(loader, url)) {
                propertySources.addAll(loader.load(name, resource));
            }
        }
        return propertySources;
    }

    public PropertySource<?> reloadAsOriginTracked(PropertySource<?> propertySource) throws IOException {
        if (propertySource instanceof OriginLookup) {
            logger.trace("PropertySource[name: '{}', class: '{}'] is already an OriginLookup",
                    propertySource.getName(), propertySource.getClass().getName());
            return propertySource;
        }
        String name = propertySource.getName();
        String location = substringBetween(name, "[", "]");
        if (StringUtils.hasText(location)) {
            return loadAsOriginTracked(name, location);
        }
        return propertySource;
    }

    public PropertySource<?> loadAsOriginTracked(String name, String location) throws IOException {
        Resource resource = resourceLoader.getResource(location);
        List<PropertySource<?>> propertySources = load(name, resource);
        for (PropertySource<?> ps : propertySources) {
            if (ps instanceof OriginLookup) {
                return ps;
            }
        }
        return null;
    }

    private boolean supports(PropertySourceLoader loader, URL resourceURL) {
        String[] fileExtensions = loader.getFileExtensions();
        String path = resourceURL.getPath();
        for (String ext : fileExtensions) {
            if (path.endsWith(ext)) {
                return true;
            }
        }
        return false;
    }

    private static String substringBetween(String str, String open, String close) {
        if (str == null || open == null || close == null) {
            return null;
        }
        int start = str.indexOf(open);
        if (start == -1) {
            return null;
        }
        int end = str.indexOf(close, start + open.length());
        if (end == -1) {
            return null;
        }
        return str.substring(start + open.length(), end);
    }
}
