package io.zhijun.spring.core.binder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;

import io.zhijun.spring.core.binder.ConfigurationBeanCustomizer;
import io.zhijun.spring.core.binder.ConfigurationBeanBindingSupport;

class ConfigurationBeanBindingPostProcessorTests {

    private ConfigurableListableBeanFactory beanFactory;

    private ConfigurationBeanBindingPostProcessor postProcessor;

    @BeforeEach
    void setUp() {
        beanFactory = mock(ConfigurableListableBeanFactory.class);
        postProcessor = new ConfigurationBeanBindingPostProcessor();
        postProcessor.setBeanFactory(beanFactory);
    }

    @Test
    void skipsNonConfigurationBeans() {
        BeanDefinition bd = BeanDefinitionBuilder.rootBeanDefinition(String.class).getBeanDefinition();
        when(beanFactory.containsBeanDefinition("test")).thenReturn(true);
        when(beanFactory.getBeanDefinition("test")).thenReturn(bd);
        when(beanFactory.getBean("test")).thenReturn("test");

        postProcessor.postProcessAfterInitialization("test", "test");
    }

    @Test
    void skipsBeansWithoutDefinition() {
        when(beanFactory.containsBeanDefinition("unknown")).thenReturn(false);
        postProcessor.postProcessAfterInitialization(new Object(), "unknown");
    }

    @Test
    void rebindsConfigurationBean() {
        BeanDefinition bd = BeanDefinitionBuilder.rootBeanDefinition(TestConfig.class).getBeanDefinition();
        bd.setAttribute(ConfigurationBeanBindingSupport.CONFIGURATION_BINDING_PREFIX, "app.test");
        bd.setAttribute(ConfigurationBeanBindingSupport.CONFIGURATION_BINDING_MULTIPLE, false);
        ((org.springframework.beans.factory.support.AbstractBeanDefinition) bd).setSource(ConfigurationBeanBindingSupport.CONFIGURATION_BEAN_SOURCE);
        bd.setAttribute(ConfigurationBeanBindingPostProcessor.IGNORE_UNKNOWN_FIELDS_ATTRIBUTE_NAME, true);
        bd.setAttribute(ConfigurationBeanBindingPostProcessor.IGNORE_INVALID_FIELDS_ATTRIBUTE_NAME, true);
        bd.setAttribute(ConfigurationBeanBindingPostProcessor.CONFIGURATION_PROPERTIES_ATTRIBUTE_NAME,
                Collections.singletonMap("key", "value"));

        when(beanFactory.containsBeanDefinition("testConfig")).thenReturn(true);
        when(beanFactory.getBeanDefinition("testConfig")).thenReturn(bd);
        when(beanFactory.getBean("testConfig")).thenReturn(new TestConfig());
        when(beanFactory.getBeanNamesForType(ConfigurationBeanCustomizer.class))
                .thenReturn(new String[0]);

        postProcessor.postProcessAfterInitialization(new TestConfig(), "testConfig");
    }

    public static class TestConfig {
        private String key;
        public String getKey() { return key; }
        public void setKey(String key) { this.key = key; }
    }
}
