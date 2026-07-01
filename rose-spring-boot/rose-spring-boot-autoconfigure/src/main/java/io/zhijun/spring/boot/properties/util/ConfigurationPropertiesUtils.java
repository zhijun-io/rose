package io.zhijun.spring.boot.properties.util;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.AnnotationUtils;

/**
 * {@link ConfigurationProperties} 工具类
 */
public abstract class ConfigurationPropertiesUtils {

    public static final Class<ConfigurationProperties> CONFIGURATION_PROPERTIES_CLASS = ConfigurationProperties.class;

    /**
     * Find an annotation of {@link ConfigurationProperties} from the specified {@link Bindable}.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * @ConfigurationProperties(prefix = "app.config")
     * public class AppConfig {
     *     private String name;
     *     // getters and setters
     * }
     *
     * Bindable<AppConfig> bindable = Bindable.of(AppConfig.class);
     * ConfigurationProperties props = ConfigurationPropertiesUtils.findConfigurationProperties(bindable);
     * // props will contain the annotation with prefix "app.config"
     * }</pre>
     *
     * <h4>Another example with nested properties</h4>
     * <pre>{@code
     * public class ParentConfig {
     *     @ConfigurationProperties(prefix = "parent.child")
     *     private ChildConfig child;
     *     // getters and setters
     * }
     *
     * Bindable<ChildConfig> bindable = Bindable.of(ChildConfig.class);
     * ConfigurationProperties props = ConfigurationPropertiesUtils.findConfigurationProperties(bindable);
     * // props will contain the annotation with prefix "parent.child"
     * }</pre>
     *
     * @param bindable {@link Bindable}
     * @return an annotation of {@link ConfigurationProperties} if present
     * @throws NullPointerException if bindable is null
     */
    public static ConfigurationProperties findConfigurationProperties(Bindable bindable) {
        ConfigurationProperties configurationProperties = (ConfigurationProperties) bindable.getAnnotation(CONFIGURATION_PROPERTIES_CLASS);
        if (configurationProperties == null) {
            ResolvableType type = bindable.getType();
            Class<?> bindableType = type.resolve();
            if (bindableType != null) {
                configurationProperties = AnnotationUtils.findAnnotation(bindableType, CONFIGURATION_PROPERTIES_CLASS);
            }
        }
        return configurationProperties;
    }

    private ConfigurationPropertiesUtils() {
    }
}
