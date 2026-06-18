package io.zhijun.boot.autoconfigure;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.boot.autoconfigure.AutoConfigurationImportFilter;
import org.springframework.boot.autoconfigure.AutoConfigurationMetadata;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * Merges {@link RoseAutoConfigurationExcludeProperties#EXCLUDE} from all property sources
 * and filters excluded auto-configuration classes.
 */
public final class RoseAutoConfigurationImportFilter implements AutoConfigurationImportFilter, EnvironmentAware {

    private Set<String> excludedAutoConfigurationClasses = Collections.emptySet();

    @Override
    public boolean[] match(String[] autoConfigurationClasses, AutoConfigurationMetadata autoConfigurationMetadata) {
        boolean[] results = new boolean[autoConfigurationClasses.length];
        for (int i = 0; i < autoConfigurationClasses.length; i++) {
            results[i] = !isExcluded(autoConfigurationClasses[i]);
        }
        return results;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.excludedAutoConfigurationClasses = getExcludedAutoConfigurationClasses(environment);
    }

    /**
     * Returns all excluded auto-configuration class names from the environment.
     */
    public static Set<String> getExcludedAutoConfigurationClasses(Environment environment) {
        Assert.isInstanceOf(ConfigurableEnvironment.class, environment, "environment must be configurable");
        ConfigurableEnvironment configurableEnvironment = (ConfigurableEnvironment) environment;
        Set<String> allExcludedClasses = new LinkedHashSet<String>();
        addExcludedAutoConfigurationClasses(environment,
                getExcludedAutoConfigurationClassesFromPropertySources(configurableEnvironment), allExcludedClasses);
        addExcludedAutoConfigurationClasses(environment,
                getExcludedAutoConfigurationClassesFromBinder(configurableEnvironment), allExcludedClasses);
        if (allExcludedClasses.isEmpty()) {
            return Collections.emptySet();
        }
        return Collections.unmodifiableSet(allExcludedClasses);
    }

    /**
     * Adds a class name to exclude at runtime.
     */
    public static void addExcludedAutoConfigurationClass(Environment environment, String className) {
        addExcludedAutoConfigurationClasses(environment, Collections.singleton(className));
    }

    /**
     * Adds class names to exclude at runtime.
     */
    public static void addExcludedAutoConfigurationClasses(Environment environment, Iterable<String> classNames) {
        ExcludedAutoConfigurationClassesPropertySource propertySource =
                ExcludedAutoConfigurationClassesPropertySource.get(environment);
        propertySource.addClasses(classNames);
    }

    private static void addExcludedAutoConfigurationClasses(Environment environment, String[] excludedClasses,
            Set<String> allExcludedClasses) {
        for (String excludedClass : excludedClasses) {
            if (!StringUtils.hasText(excludedClass)) {
                continue;
            }
            String resolvedExcludeClass = environment.resolvePlaceholders(excludedClass);
            allExcludedClasses.addAll(StringUtils.commaDelimitedListToSet(resolvedExcludeClass));
        }
    }

    private static String[] getExcludedAutoConfigurationClassesFromPropertySources(ConfigurableEnvironment environment) {
        Set<String> excludedClasses = new LinkedHashSet<String>();
        MutablePropertySources propertySources = environment.getPropertySources();
        for (PropertySource<?> propertySource : propertySources) {
            Object property = propertySource.getProperty(RoseAutoConfigurationExcludeProperties.EXCLUDE);
            if (property instanceof String) {
                String resolvedExclude = environment.resolvePlaceholders((String) property);
                excludedClasses.addAll(StringUtils.commaDelimitedListToSet(resolvedExclude));
            }
        }
        return excludedClasses.isEmpty() ? new String[0] : excludedClasses.toArray(new String[excludedClasses.size()]);
    }

    private static String[] getExcludedAutoConfigurationClassesFromBinder(ConfigurableEnvironment environment) {
        return Binder.get(environment)
                .bind(RoseAutoConfigurationExcludeProperties.EXCLUDE, String[].class)
                .orElse(new String[0]);
    }

    private boolean isExcluded(String autoConfigurationClassName) {
        return StringUtils.hasText(autoConfigurationClassName)
                && excludedAutoConfigurationClasses.contains(autoConfigurationClassName);
    }

    private static final class ExcludedAutoConfigurationClassesPropertySource
            extends PropertySource<Set<String>> {

        private static final String NAME = RoseAutoConfigurationExcludeProperties.EXCLUDE;

        private ExcludedAutoConfigurationClassesPropertySource() {
            super(NAME, new LinkedHashSet<String>());
        }

        @Override
        public Object getProperty(String name) {
            if (RoseAutoConfigurationExcludeProperties.EXCLUDE.equals(name)) {
                return StringUtils.collectionToCommaDelimitedString(this.source);
            }
            return null;
        }

        private void addClasses(Iterable<String> classNames) {
            for (String className : classNames) {
                if (StringUtils.hasText(className)) {
                    this.source.add(className);
                }
            }
        }

        private static ExcludedAutoConfigurationClassesPropertySource get(Environment environment) {
            Assert.isInstanceOf(ConfigurableEnvironment.class, environment, "environment must be configurable");
            MutablePropertySources propertySources = ((ConfigurableEnvironment) environment).getPropertySources();
            ExcludedAutoConfigurationClassesPropertySource propertySource =
                    (ExcludedAutoConfigurationClassesPropertySource) propertySources.get(NAME);
            if (propertySource == null) {
                propertySource = new ExcludedAutoConfigurationClassesPropertySource();
                propertySources.addFirst(propertySource);
            }
            return propertySource;
        }
    }
}
