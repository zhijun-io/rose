package io.zhijun.spring.webmvc.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;
import org.springframework.validation.DataBinder;
import org.springframework.web.accept.ContentNegotiationManager;
import org.springframework.web.accept.ContentNegotiationManagerFactoryBean;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.beans.PropertyEditorSupport;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import static org.springframework.util.ReflectionUtils.doWithFields;

/**
 * Configurable {@link ContentNegotiationManager} {@link WebMvcConfigurer}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ContentNegotiationManagerFactoryBean
 * @see ContentNegotiationManager
 * @see WebMvcConfigurer
 * @see DataBinder
 * @since 1.0.0
 */
public class ConfigurableContentNegotiationManagerWebMvcConfigurer implements WebMvcConfigurer, EnvironmentAware {

    private static final Logger logger = LoggerFactory.getLogger(ConfigurableContentNegotiationManagerWebMvcConfigurer.class);

    /**
     * The property name prefix of {@link ContentNegotiationManager} : "rose.spring.webmvc.content-negotiation."
     */
    static final String PROPERTY_NAME_PREFIX = "rose.spring.webmvc.content-negotiation.";

    static final Class<ContentNegotiationManagerFactoryBean> FACTORY_BEAN_FIELD_CLASS =
            ContentNegotiationManagerFactoryBean.class;

    private Map<String, Object> propertyValues;

    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        doWithFields(configurer.getClass(), field -> {
            boolean accessible = field.isAccessible();
            try {
                if (!accessible) {
                    field.setAccessible(true);
                }
                ContentNegotiationManagerFactoryBean factoryBean = (ContentNegotiationManagerFactoryBean) field.get(configurer);
                configureContentNegotiationManagerFactoryBean(factoryBean);
            } finally {
                if (!accessible) {
                    field.setAccessible(accessible);
                }
            }
        }, field -> {
            Class<?> fieldType = field.getType();
            return FACTORY_BEAN_FIELD_CLASS.isAssignableFrom(fieldType);
        });
    }

    @Override
    public void setEnvironment(Environment environment) {
        ConfigurableEnvironment configurableEnvironment = (ConfigurableEnvironment) environment;
        Map<String, Object> properties = getSubProperties(configurableEnvironment, PROPERTY_NAME_PREFIX);
        this.propertyValues = properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.propertyValues = properties;
    }

    private void configureContentNegotiationManagerFactoryBean(ContentNegotiationManagerFactoryBean factoryBean) {

        DataBinder dataBinder = new DataBinder(factoryBean);

        dataBinder.setDisallowedFields("contentNegotiationManager", "servletContext");

        dataBinder.setAutoGrowNestedPaths(true);

        dataBinder.registerCustomEditor(Map.class, "mediaTypes", new MediaTypesMapPropertyEditor());

        MutablePropertyValues propertyValues = new MutablePropertyValues();

        propertyValues.addPropertyValues(this.propertyValues);

        dataBinder.bind(propertyValues);
    }

    static Map<String, Object> getSubProperties(ConfigurableEnvironment environment, String prefix) {
        Map<String, Object> subProperties = new LinkedHashMap<>();
        String normalizedPrefix = prefix.endsWith(".") ? prefix : prefix + ".";

        for (PropertySource<?> source : environment.getPropertySources()) {
            if (source instanceof EnumerablePropertySource) {
                EnumerablePropertySource<?> enumerableSource = (EnumerablePropertySource<?>) source;
                for (String name : enumerableSource.getPropertyNames()) {
                    String subName = name.startsWith(normalizedPrefix)
                            ? name.substring(normalizedPrefix.length())
                            : null;
                    if (subName != null && subName.length() > 0 && !subProperties.containsKey(subName)) {
                        Object value = source.getProperty(name);
                        subProperties.put(subName, value);
                    }
                }
            }
        }

        return subProperties;
    }

    static class MediaTypesMapPropertyEditor extends PropertyEditorSupport {

        private final ObjectMapper objectMapper = new ObjectMapper();

        @Override
        public void setAsText(String text) throws IllegalArgumentException {
            if (text.indexOf('{') == 0) {
                try {
                    Properties mediaTypes = objectMapper.readValue(text, Properties.class);
                    setValue(mediaTypes);
                } catch (Exception e) {
                    throw new IllegalArgumentException("Failed to parse media types JSON: " + text, e);
                }
            } else {
                setValue(text);
            }
        }
    }

}
