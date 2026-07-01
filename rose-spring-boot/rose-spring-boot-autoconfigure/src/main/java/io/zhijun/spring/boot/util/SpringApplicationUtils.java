package io.zhijun.spring.boot.util;

import io.zhijun.core.util.FormatUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.PropertyResolver;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import static io.zhijun.spring.boot.constants.PropertyConstants.*;
import static java.util.Collections.unmodifiableSet;
import static java.util.Locale.ENGLISH;
import static org.springframework.util.StringUtils.hasText;

/**
 * {@link SpringApplication} 工具类
 */
public abstract class SpringApplicationUtils {

    private static final Logger logger = LoggerFactory.getLogger(SpringApplicationUtils.class);

    private static final Set<String> defaultPropertiesResources = new LinkedHashSet<>();

    //TODO
//    static {
//        addShutdownHookCallback(defaultPropertiesResources::clear);
//    }

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
        String messagePattern = "SpringApplication: main class : '{}', web type : '{}', sources : {}, all sources : {}, additional profiles : {}, initializers : {}, listeners : {}, args : {}, context id : '{}', log : {}";

        Object[] arguments = ArrayUtils.toArray(springApplication.getMainApplicationClass(),
            springApplication.getWebApplicationType(),
            springApplication.getSources(),
            springApplication.getAllSources(),
            springApplication.getAdditionalProfiles(),
            springApplication.getInitializers(),
            springApplication.getListeners(),
            Arrays.toString(args),
            context == null ? "N/A" : context.getId(),
            FormatUtils.format(pattern, patternArgs));

        String level = getLoggingLevel(context);

        switch (level) {
            case "TRACE":
                if (logger.isTraceEnabled()) {
                    logger.trace(messagePattern, arguments);
                }
                break;
            case "DEBUG":
                if (logger.isDebugEnabled()) {
                    logger.debug(messagePattern, arguments);
                }
                break;
            case "INFO":
                logger.info(messagePattern, arguments);
                break;
            case "WARN":
                logger.warn(messagePattern, arguments);
                break;
            case "ERROR":
                logger.error(messagePattern, arguments);
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
