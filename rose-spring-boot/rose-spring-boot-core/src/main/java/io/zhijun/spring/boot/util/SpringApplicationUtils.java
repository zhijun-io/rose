package io.zhijun.spring.boot.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.PropertyResolver;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;

import java.util.LinkedHashSet;
import java.util.Set;

import static io.zhijun.spring.boot.constants.PropertyConstants.ROSE_SPRING_BOOT_PROPERTY_NAME_PREFIX;
import static java.util.Collections.unmodifiableSet;
import static java.util.Locale.ENGLISH;
import static org.springframework.util.StringUtils.hasText;

/**
 * {@link SpringApplication} 工具类
 */
public abstract class SpringApplicationUtils {

    private static final Logger logger = LoggerFactory.getLogger(SpringApplicationUtils.class);

    public static final String LOGGING_LEVEL_PROPERTY_NAME = ROSE_SPRING_BOOT_PROPERTY_NAME_PREFIX + "logging-level";

    public static final String DEFAULT_LOGGING_LEVEL = "INFO";

    private static final Set<String> defaultPropertiesResources = new LinkedHashSet<>();

    public static void addDefaultPropertiesResource(String resourceLocation) {
        if (hasText(resourceLocation)) {
            defaultPropertiesResources.add(resourceLocation);
        }
    }

    public static void addDefaultPropertiesResources(String... resourceLocations) {
        if (resourceLocations != null) {
            for (String resource : resourceLocations) {
                addDefaultPropertiesResource(resource);
            }
        }
    }

    public static Set<String> getDefaultPropertiesResources() {
        return unmodifiableSet(defaultPropertiesResources);
    }

    public static ResourceLoader getResourceLoader(SpringApplication springApplication) {
        ResourceLoader resourceLoader = springApplication.getResourceLoader();
        if (resourceLoader == null) {
            resourceLoader = new DefaultResourceLoader(springApplication.getClassLoader());
        }
        return resourceLoader;
    }

    public static String getLoggingLevel(ConfigurableApplicationContext context) {
        return getLoggingLevel(context == null ? null : context.getEnvironment());
    }

    public static String getLoggingLevel(PropertyResolver propertyResolver) {
        String level = propertyResolver == null ? DEFAULT_LOGGING_LEVEL :
                propertyResolver.getProperty(LOGGING_LEVEL_PROPERTY_NAME, DEFAULT_LOGGING_LEVEL);
        return level.toUpperCase(ENGLISH);
    }

    public static void log(SpringApplication springApplication, String[] args, String pattern, Object... patternArgs) {
        log(springApplication, args, null, pattern, patternArgs);
    }

    public static void log(SpringApplication springApplication, String[] args, ConfigurableApplicationContext context,
                           String pattern, Object... patternArgs) {
        String message = "SpringApplication: main class: '{}', web type: '{}', args: {}, context id: '{}', log: {}";

        Object[] arguments = {
                springApplication.getMainApplicationClass(),
                springApplication.getWebApplicationType(),
                java.util.Arrays.toString(args),
                context == null ? "N/A" : context.getId(),
                pattern
        };

        String level = getLoggingLevel(context);

        switch (level) {
            case "TRACE":
                if (logger.isTraceEnabled()) {
                    logger.trace(message, arguments);
                }
                break;
            case "DEBUG":
                if (logger.isDebugEnabled()) {
                    logger.debug(message, arguments);
                }
                break;
            case "INFO":
                logger.info(message, arguments);
                break;
            case "WARN":
                logger.warn(message, arguments);
                break;
            case "ERROR":
                logger.error(message, arguments);
                break;
            default:
                if (logger.isTraceEnabled()) {
                    logger.trace("The logger is off");
                }
                break;
        }
    }

    private SpringApplicationUtils() {
    }
}
